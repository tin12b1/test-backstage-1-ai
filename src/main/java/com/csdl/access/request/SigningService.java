package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.SigningScope;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.RequestDetail;
import com.csdl.access.domain.RequestSignature;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.RequestDetailRepository;
import com.csdl.access.domain.repo.RequestSignatureRepository;
import com.csdl.access.integration.otp.OtpService;
import com.csdl.access.integration.otp.OtpVerifyResult;
import com.csdl.access.request.dto.SignResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service xu ly ky xac nhan OTP cho yeu cau truy cap CSDL.
 * Ho tro ky phieu chung (requester/co-signer) va ky ban giao (04B receiver).
 */
@Service
public class SigningService {

    private final AccessRequestRepository requestRepository;
    private final RequestSignatureRepository signatureRepository;
    private final RequestDetailRepository detailRepository;
    private final AppUserRepository userRepository;
    private final OtpService otpService;

    public SigningService(AccessRequestRepository requestRepository,
                          RequestSignatureRepository signatureRepository,
                          RequestDetailRepository detailRepository,
                          AppUserRepository userRepository,
                          OtpService otpService) {
        this.requestRepository = requestRepository;
        this.signatureRepository = signatureRepository;
        this.detailRepository = detailRepository;
        this.userRepository = userRepository;
        this.otpService = otpService;
    }

    /**
     * Ky xac nhan yeu cau bang OTP.
     *
     * @param requestId id phieu yeu cau
     * @param otp       ma OTP nguoi dung nhap
     * @param scope     pham vi ky (GENERAL hoac DETAIL)
     * @param detailId  id dong chi tiet (null neu ky GENERAL)
     * @param session   phien dang nhap hien tai
     * @return ket qua ky (thanh cong/that bai)
     */
    @Transactional
    public SignResult signRequest(Long requestId, String otp, SigningScope scope,
                                  Long detailId, UserSession session) {
        AccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));

        // Kiem tra user da ky phieu nay chua
        if (hasAlreadySigned(requestId, session.getUserId())) {
            throw new BusinessException("Ban da ky phieu nay roi.");
        }

        // Xac thuc OTP
        OtpVerifyResult otpResult = otpService.verifyOtp(
                session.getUsername(), otp, "SIGN_REQUEST", requestId);

        if (!otpResult.isSuccess()) {
            return new SignResult(false, "OTP khong hop le", null, null);
        }

        // Tao ban ghi chu ky
        LocalDateTime signedAt = LocalDateTime.now();
        RequestSignature sig = new RequestSignature();
        sig.setRequestId(requestId);
        sig.setDetailId(detailId);
        sig.setSignerUserId(session.getUserId());
        sig.setSignerRoleCode(session.getActiveRole() == null ? null : session.getActiveRole().name());
        sig.setSigningScope(scope);
        sig.setOtpTransactionId(otpResult.getTransactionId());
        sig.setSignedAt(signedAt);
        sig.setResult("SUCCESS");

        // Lay anh chu ky tu signature_image_id (set tu config)
        // signatureImageId duoc luu tren RequestSignature de truy xuat
        sig.setSignatureImageId(resolveSignatureImageId(session.getUserId()));
        signatureRepository.save(sig);

        // Tao URL anh chu ky de hien thi tren form
        String signatureImageUrl = sig.getSignatureImageId() != null
                ? "/api/signature-images/" + sig.getSignatureImageId()
                : null;

        return new SignResult(true, "Ky xac nhan thanh cong", signatureImageUrl, signedAt);
    }

    /**
     * Ky ban giao tai khoan (04B-BGTK) — nguoi nhan ky dong cua minh.
     *
     * @param requestId id phieu 04B
     * @param detailId  id dong chi tiet nguoi nhan can ky
     * @param otp       ma OTP
     * @param session   phien dang nhap
     * @return ket qua ky
     */
    @Transactional
    public SignResult signReceipt(Long requestId, Long detailId, String otp, UserSession session) {
        AccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));

        // Chi cho phep ky khi phieu o trang thai PENDING_RECEIPT
        if (request.getStatus() != RequestStatus.PENDING_RECEIPT) {
            throw new BusinessException("Phieu khong o trang thai cho nguoi nhan ky.");
        }

        // Kiem tra user da ky phieu nay chua
        if (hasAlreadySigned(requestId, session.getUserId())) {
            throw new BusinessException("Ban da ky phieu nay roi.");
        }

        // Xac thuc OTP
        OtpVerifyResult otpResult = otpService.verifyOtp(
                session.getUsername(), otp, "SIGN_RECEIPT", requestId);

        if (!otpResult.isSuccess()) {
            return new SignResult(false, "OTP khong hop le", null, null);
        }

        // Ghi nhan chu ky voi scope DETAIL cho dong cu the
        LocalDateTime signedAt = LocalDateTime.now();
        RequestSignature sig = new RequestSignature();
        sig.setRequestId(requestId);
        sig.setDetailId(detailId);
        sig.setSignerUserId(session.getUserId());
        sig.setSignerRoleCode(session.getActiveRole() == null ? null : session.getActiveRole().name());
        sig.setSigningScope(SigningScope.DETAIL);
        sig.setOtpTransactionId(otpResult.getTransactionId());
        sig.setSignedAt(signedAt);
        sig.setResult("SUCCESS");
        sig.setSignatureImageId(resolveSignatureImageId(session.getUserId()));
        signatureRepository.save(sig);

        // Kiem tra tat ca nguoi nhan da ky chua → neu da day du thi chuyen trang thai
        if (allReceiversSigned(requestId)) {
            request.setStatus(RequestStatus.PENDING_APPROVAL);
            requestRepository.save(request);
        }

        String signatureImageUrl = sig.getSignatureImageId() != null
                ? "/api/signature-images/" + sig.getSignatureImageId()
                : null;

        return new SignResult(true, "Ky xac nhan thanh cong", signatureImageUrl, signedAt);
    }

    /**
     * Kiem tra user da ky thanh cong phieu nay chua.
     *
     * @param requestId id phieu yeu cau
     * @param userId    id nguoi dung
     * @return true neu da co chu ky SUCCESS
     */
    public boolean hasAlreadySigned(Long requestId, Long userId) {
        return signatureRepository.existsByRequestIdAndSignerUserIdAndResult(
                requestId, userId, "SUCCESS");
    }

    /**
     * Kiem tra tat ca nguoi nhan (detail rows) cua phieu 04B da ky day du chua.
     * Moi dong detail phai co it nhat 1 chu ky SUCCESS tuong ung.
     *
     * @param requestId id phieu 04B
     * @return true neu tat ca dong detail deu da co chu ky
     */
    public boolean allReceiversSigned(Long requestId) {
        List<RequestDetail> details = detailRepository.findByRequestId(requestId);
        if (details.isEmpty()) {
            return false;
        }

        List<RequestSignature> signatures = signatureRepository.findByRequestId(requestId);

        for (RequestDetail detail : details) {
            boolean hasSig = signatures.stream()
                    .anyMatch(s -> detail.getId().equals(s.getDetailId())
                            && "SUCCESS".equals(s.getResult()));
            if (!hasSig) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lay signature_image_id tu AppUser (da luu san khi dang ky chu ky).
     * Tra ve null neu user chua co anh chu ky.
     */
    private Long resolveSignatureImageId(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getSignatureImageId())
                .orElse(null);
    }
}
