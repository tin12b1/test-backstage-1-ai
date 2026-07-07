package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.AppUser;
import com.csdl.access.domain.RequestDetail;
import com.csdl.access.domain.RequestSignature;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.RequestDetailRepository;
import com.csdl.access.domain.repo.RequestSignatureRepository;
import com.csdl.access.request.dto.DetailSigningStatus;
import com.csdl.access.request.dto.SigningStatusResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Xu ly dong thoi tren phieu chung (01-YCTC, 04A-YCTK, 04B-BGTK).
 * Su dung row-level locking (optimistic locking) va polling de dong bo trang thai ky.
 */
@Service
public class ConcurrencyHandler {

    private final AccessRequestRepository accessRequestRepository;
    private final RequestDetailRepository requestDetailRepository;
    private final RequestSignatureRepository requestSignatureRepository;
    private final AppUserRepository appUserRepository;

    public ConcurrencyHandler(AccessRequestRepository accessRequestRepository,
                              RequestDetailRepository requestDetailRepository,
                              RequestSignatureRepository requestSignatureRepository,
                              AppUserRepository appUserRepository) {
        this.accessRequestRepository = accessRequestRepository;
        this.requestDetailRepository = requestDetailRepository;
        this.requestSignatureRepository = requestSignatureRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Lay trang thai ky hien tai cua tat ca dong chi tiet tren phieu.
     * Dung cho polling endpoint — client goi dinh ky de cap nhat giao dien.
     */
    @Transactional(readOnly = true)
    public SigningStatusResponse getSigningStatus(Long requestId) {
        List<RequestDetail> details = requestDetailRepository.findByRequestId(requestId);

        // Load all successful signatures for this request that have a detailId
        List<RequestSignature> successSignatures =
                requestSignatureRepository.findByRequestIdAndDetailIdNotNullAndResult(requestId, "SUCCESS");

        // Map detailId -> signature for quick lookup
        Map<Long, RequestSignature> signatureByDetailId = successSignatures.stream()
                .collect(Collectors.toMap(
                        RequestSignature::getDetailId,
                        sig -> sig,
                        (a, b) -> a // in case of duplicates, take first
                ));

        List<DetailSigningStatus> statusList = new ArrayList<>();
        for (RequestDetail detail : details) {
            RequestSignature sig = signatureByDetailId.get(detail.getId());
            boolean signed = sig != null;

            // Resolve target user name
            String targetUserName = resolveUserName(detail.getTargetUserId());

            // Build signature image URL if signed
            String signatureImageUrl = null;
            if (signed && sig.getSignatureImageId() != null) {
                signatureImageUrl = "/api/signatures/images/" + sig.getSignatureImageId();
            }

            statusList.add(new DetailSigningStatus(
                    detail.getId(),
                    detail.getTargetUserId(),
                    targetUserName,
                    signed,
                    signed ? sig.getSignedAt() : null,
                    signatureImageUrl
            ));
        }

        return new SigningStatusResponse(requestId, statusList, LocalDateTime.now());
    }

    /**
     * Them dong chi tiet moi (co-signer them dong cua minh).
     * Kiem tra status = PENDING_SIGN, su dung optimistic locking tren request entity.
     */
    @Transactional
    public RequestDetail addDetailRow(Long requestId, DetailForm form, UserSession session) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy yêu cầu với ID: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING_SIGN) {
            throw new BusinessException("Chỉ được thêm dòng chi tiết khi phiếu đang ở trạng thái Chờ ký xác nhận");
        }

        RequestDetail detail = new RequestDetail();
        detail.setRequestId(requestId);
        detail.setSystemId(form.getSystemId());
        detail.setDatabaseId(form.getDatabaseId());
        detail.setObjectOwner(form.getObjectOwner());
        detail.setObjectName(form.getObjectName());
        detail.setObjectType(form.getObjectType());
        detail.setTargetUserId(session.getUserId());
        detail.setAccountOwnerName(form.getAccountOwnerName());
        detail.setAccountType(form.getAccountType());
        detail.setAccountAction(form.getAccountAction());
        detail.setAccessRights(form.getAccessRights());
        detail.setQueryAll(form.isQueryAll());
        detail.setPurpose(form.getPurpose());

        return requestDetailRepository.save(detail);
    }

    /**
     * Sua dong chi tiet cua chinh user (chua ky).
     * Kiem tra ownership va trang thai ky truoc khi cho phep sua.
     */
    @Transactional
    public RequestDetail updateOwnDetail(Long requestId, Long detailId, DetailForm form, UserSession session) {
        RequestDetail detail = requestDetailRepository.findById(detailId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy dòng chi tiết với ID: " + detailId));

        // Verify detail belongs to this request
        if (!detail.getRequestId().equals(requestId)) {
            throw new BusinessException("Dòng chi tiết không thuộc yêu cầu này");
        }

        // Verify ownership — only the target user can edit their own row
        if (!detail.getTargetUserId().equals(session.getUserId())) {
            throw new BusinessException("Bạn chỉ được sửa dòng chi tiết của mình");
        }

        // Verify not yet signed
        if (requestSignatureRepository.existsByDetailIdAndResult(detailId, "SUCCESS")) {
            throw new BusinessException("Không thể sửa dòng chi tiết đã được ký");
        }

        // Update fields from form
        detail.setSystemId(form.getSystemId());
        detail.setDatabaseId(form.getDatabaseId());
        detail.setObjectOwner(form.getObjectOwner());
        detail.setObjectName(form.getObjectName());
        detail.setObjectType(form.getObjectType());
        detail.setAccountOwnerName(form.getAccountOwnerName());
        detail.setAccountType(form.getAccountType());
        detail.setAccountAction(form.getAccountAction());
        detail.setAccessRights(form.getAccessRights());
        detail.setQueryAll(form.isQueryAll());
        detail.setPurpose(form.getPurpose());

        return requestDetailRepository.save(detail);
    }

    /**
     * Xoa dong chua ky — chi nguoi lap phieu (form creator) moi duoc xoa.
     */
    @Transactional
    public void deleteUnsignedDetail(Long requestId, Long detailId, UserSession session) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy yêu cầu với ID: " + requestId));

        // Only form creator can delete rows
        if (!request.getRequesterUserId().equals(session.getUserId())) {
            throw new BusinessException("Chỉ người lập phiếu mới được xóa dòng chi tiết");
        }

        RequestDetail detail = requestDetailRepository.findById(detailId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy dòng chi tiết với ID: " + detailId));

        // Verify detail belongs to this request
        if (!detail.getRequestId().equals(requestId)) {
            throw new BusinessException("Dòng chi tiết không thuộc yêu cầu này");
        }

        // Verify not yet signed
        if (requestSignatureRepository.existsByDetailIdAndResult(detailId, "SUCCESS")) {
            throw new BusinessException("Không thể xóa dòng chi tiết đã được ký");
        }

        requestDetailRepository.delete(detail);
    }

    /**
     * Xoa tat ca dong chua ky khi nguoi lap ky gui phieu.
     * Goi tu RequestService/SigningService truoc khi submit.
     */
    @Transactional
    public int removeAllUnsignedDetails(Long requestId) {
        return requestDetailRepository.deleteUnsignedByRequestId(requestId);
    }

    /**
     * Tra ten nguoi dung tu userId. Tra "Unknown" neu khong tim thay.
     */
    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        return appUserRepository.findById(userId)
                .map(AppUser::getFullName)
                .orElse("Unknown");
    }
}
