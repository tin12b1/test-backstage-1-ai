package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.audit.AuditService;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.SigningScope;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.*;
import com.csdl.access.domain.repo.*;
import com.csdl.access.integration.otp.OtpVerifyResult;
import com.csdl.access.integration.otp.OtpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Nghiep vu lap/sua/ky/huy/gui lai yeu cau (features/request-create.md).
 */
@Service
public class RequestService {

    private final AccessRequestRepository requestRepository;
    private final RequestDetailRepository detailRepository;
    private final RequestSignatureRepository signatureRepository;
    private final AppUserRepository userRepository;
    private final InformationSystemRepository systemRepository;
    private final DatabaseCatalogRepository databaseRepository;
    private final RequestCodeGenerator codeGenerator;
    private final EmergencyDebtService debtService;
    private final OtpService otpService;
    private final AuditService auditService;

    public RequestService(AccessRequestRepository requestRepository,
                          RequestDetailRepository detailRepository,
                          RequestSignatureRepository signatureRepository,
                          AppUserRepository userRepository,
                          InformationSystemRepository systemRepository,
                          DatabaseCatalogRepository databaseRepository,
                          RequestCodeGenerator codeGenerator,
                          EmergencyDebtService debtService,
                          OtpService otpService,
                          AuditService auditService) {
        this.requestRepository = requestRepository;
        this.detailRepository = detailRepository;
        this.signatureRepository = signatureRepository;
        this.userRepository = userRepository;
        this.systemRepository = systemRepository;
        this.databaseRepository = databaseRepository;
        this.codeGenerator = codeGenerator;
        this.debtService = debtService;
        this.otpService = otpService;
        this.auditService = auditService;
    }

    public List<AccessRequest> myRequests(UserSession session) {
        return requestRepository.findByRequesterUserIdOrderByCreatedAtDesc(session.getUserId());
    }

    public AccessRequest get(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));
    }

    public List<RequestDetail> details(Long requestId) {
        return detailRepository.findByRequestId(requestId);
    }

    public List<RequestSignature> signatures(Long requestId) {
        return signatureRepository.findByRequestId(requestId);
    }

    @Transactional
    public AccessRequest createDraft(RequestForm form, UserSession session) {
        RequestType type = parseType(form.getRequestType());

        // Chan lap phieu moi neu dang no 05B (tru chinh phieu 05B).
        if (type != RequestType.HTKC_05B && debtService.hasOutstandingDebt(session.getUserId())) {
            throw new BusinessException(
                    "Ban dang no phieu 05B-HTKC chua hoan thien, khong the lap phieu moi.");
        }

        AppUser user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new BusinessException("Khong tim thay nguoi dung"));

        AccessRequest r = new AccessRequest();
        r.setRequestType(type);
        r.setStatus(RequestStatus.DRAFT);
        r.setRequesterUserId(user.getId());
        r.setRequesterUnitId(user.getUnitId());
        r.setRequesterDepartmentId(user.getDepartmentId());
        applyForm(r, form);
        r.setRequestCode(codeGenerator.generate(user.getUnitId()));
        r = requestRepository.save(r);

        saveDetails(r.getId(), form, type);

        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "CREATE_DRAFT", "access_request", r.getId(), "Tao nhap " + type.getFormCode());
        return r;
    }

    @Transactional
    public AccessRequest updateDraft(Long id, RequestForm form, UserSession session) {
        AccessRequest r = get(id);
        assertOwner(r, session);
        if (!r.getStatus().isEditable()) {
            throw new BusinessException("Phieu da gui phe duyet, khong the sua.");
        }
        applyForm(r, form);
        r = requestRepository.save(r);

        detailRepository.deleteByRequestId(id);
        saveDetails(id, form, r.getRequestType());

        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "UPDATE_DRAFT", "access_request", id, "Cap nhat nhap");
        return r;
    }

    @Transactional
    public OtpVerifyResult sign(Long id, String otp, SigningScope scope, Long detailId, UserSession session) {
        AccessRequest r = get(id);
        // Cho phep ky khi con o giai doan lap (DRAFT/RETURNED/SEND_FAILED).
        if (!r.getStatus().isEditable()) {
            throw new BusinessException("Phieu khong o trang thai cho ky.");
        }
        OtpVerifyResult result = otpService.verifyOtp(
                session.getUsername(), otp, "SIGN_REQUEST", id);

        RequestSignature sig = new RequestSignature();
        sig.setRequestId(id);
        sig.setDetailId(detailId);
        sig.setSignerUserId(session.getUserId());
        sig.setSignerRoleCode(session.getActiveRole() == null ? null : session.getActiveRole().name());
        sig.setSigningScope(scope);
        sig.setOtpTransactionId(result.getTransactionId());
        sig.setSignedAt(LocalDateTime.now());
        sig.setResult(result.isSuccess() ? "SUCCESS" : "FAILED");
        userRepository.findById(session.getUserId())
                .ifPresent(u -> sig.setSignatureImageId(u.getSignatureImageId()));
        signatureRepository.save(sig);

        auditService.record(session.getUsername(),
                sig.getSignerRoleCode(), "SIGN_REQUEST", "access_request", id,
                "Ky " + scope + " ket qua " + sig.getResult());

        if (!result.isSuccess()) {
            throw new BusinessException("Ky xac nhan that bai: " + result.getMessage());
        }
        return result;
    }

    @Transactional
    public void cancel(Long id, UserSession session) {
        AccessRequest r = get(id);
        assertOwner(r, session);
        if (!r.getStatus().isCancellable()) {
            throw new BusinessException("Chi duoc huy khi phieu chua duoc phe duyet.");
        }
        r.setStatus(RequestStatus.CANCELLED);
        r.setCancelledAt(LocalDateTime.now());
        requestRepository.save(r);
        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "CANCEL", "access_request", id, "Huy yeu cau");
    }

    private void assertOwner(AccessRequest r, UserSession session) {
        if (!r.getRequesterUserId().equals(session.getUserId())) {
            throw new BusinessException("Ban khong phai nguoi lap phieu nay.");
        }
    }

    private void applyForm(AccessRequest r, RequestForm form) {
        r.setShiftNo(form.getShiftNo());
        r.setAccessNo(form.getAccessNo());
        r.setSystemId(form.getSystemId());
        r.setDatabaseId(form.getDatabaseId());
        r.setReason(form.getReason());
        r.setExpectedExecutionDate(parseDateTime(form.getExpectedExecutionDate()));

        LocalDateTime start = parseDateTime(form.getStartTime());
        LocalDateTime end = parseDateTime(form.getEndTime());
        // Neu khong nhap thoi gian, suy ra theo ca lam viec.
        if ((start == null || end == null) && form.getShiftNo() != null) {
            LocalDateTime[] shift = shiftTimes(form.getShiftNo());
            if (start == null) {
                start = shift[0];
            }
            if (end == null) {
                end = shift[1];
            }
        }
        r.setStartTime(start);
        r.setEndTime(end);
    }

    /** Ca 1: 0-8h, Ca 2: 8-20h, Ca 3: 20-24h (request-create.md). */
    private LocalDateTime[] shiftTimes(int shift) {
        java.time.LocalDate today = java.time.LocalDate.now();
        switch (shift) {
            case 1:
                return new LocalDateTime[]{today.atTime(LocalTime.MIDNIGHT), today.atTime(8, 0)};
            case 2:
                return new LocalDateTime[]{today.atTime(8, 0), today.atTime(20, 0)};
            case 3:
                return new LocalDateTime[]{today.atTime(20, 0), today.atTime(23, 59)};
            default:
                return new LocalDateTime[]{today.atStartOfDay(), today.atTime(23, 59)};
        }
    }

    private void saveDetails(Long requestId, RequestForm form, RequestType type) {
        if (form.getDetails() == null) {
            return;
        }
        for (DetailForm d : form.getDetails()) {
            if (isBlankDetail(d)) {
                continue;
            }
            RequestDetail detail = new RequestDetail();
            detail.setRequestId(requestId);
            detail.setSystemId(d.getSystemId());
            detail.setDatabaseId(d.getDatabaseId());
            detail.setObjectOwner(d.getObjectOwner());
            detail.setObjectName(d.getObjectName());
            detail.setObjectType(d.getObjectType());
            detail.setTargetUserId(d.getTargetUserId());
            detail.setAccountOwnerName(d.getAccountOwnerName());
            detail.setAccountType(d.getAccountType());
            detail.setAccountAction(d.getAccountAction());
            detail.setQueryAll(d.isQueryAll());
            // QueryAll bat thi cac quyen chi tiet khong ap dung (05A-YCKC).
            detail.setAccessRights(d.isQueryAll() ? "QUERY_ALL" : d.getAccessRights());
            detail.setPurpose(d.getPurpose());
            detailRepository.save(detail);
        }
    }

    private boolean isBlankDetail(DetailForm d) {
        return (d.getDatabaseId() == null && d.getSystemId() == null)
                && isBlank(d.getObjectName())
                && isBlank(d.getAccountOwnerName())
                && d.getTargetUserId() == null
                && !d.isQueryAll();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private RequestType parseType(String code) {
        try {
            return RequestType.valueOf(code);
        } catch (Exception e) {
            throw new BusinessException("Loai phieu khong hop le: " + code);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
