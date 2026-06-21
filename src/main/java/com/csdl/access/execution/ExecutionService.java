package com.csdl.access.execution;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.audit.AuditService;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.enums.SigningScope;
import com.csdl.access.common.enums.WorkflowAction;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.RequestSignature;
import com.csdl.access.domain.WorkLog07;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.RequestSignatureRepository;
import com.csdl.access.domain.repo.WorkLog07Repository;
import com.csdl.access.integration.otp.OtpService;
import com.csdl.access.integration.otp.OtpVerifyResult;
import com.csdl.access.notification.NotificationEvent;
import com.csdl.access.notification.NotificationService;
import com.csdl.access.workflow.WorkflowHistoryService;
import com.csdl.access.workflow.WorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Bo phan Mo truy cap/DBA/Nguoi thuc hien ghi nhan thuc hien va hoan thanh phieu
 * (features/approval-processing.md, api-contract.md muc 5 - execute).
 */
@Service
public class ExecutionService {

    private final AccessRequestRepository requestRepository;
    private final RequestSignatureRepository signatureRepository;
    private final WorkLog07Repository workLogRepository;
    private final AppUserRepository userRepository;
    private final WorkflowService workflowService;
    private final WorkflowHistoryService historyService;
    private final NotificationService notificationService;
    private final OtpService otpService;
    private final AuditService auditService;

    public ExecutionService(AccessRequestRepository requestRepository,
                            RequestSignatureRepository signatureRepository,
                            WorkLog07Repository workLogRepository,
                            AppUserRepository userRepository,
                            WorkflowService workflowService,
                            WorkflowHistoryService historyService,
                            NotificationService notificationService,
                            OtpService otpService,
                            AuditService auditService) {
        this.requestRepository = requestRepository;
        this.signatureRepository = signatureRepository;
        this.workLogRepository = workLogRepository;
        this.userRepository = userRepository;
        this.workflowService = workflowService;
        this.historyService = historyService;
        this.notificationService = notificationService;
        this.otpService = otpService;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(Long id, String otp, String startTime, String endTime,
                        String note, UserSession session) {
        AccessRequest r = requestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));
        RoleCode role = session.getActiveRole();

        if (!workflowService.isCurrentActor(r, role)) {
            throw new BusinessException("Yeu cau khong o buoc xu ly cua vai tro hien hanh.");
        }
        if (!workflowService.isExecutionStep(r)) {
            throw new BusinessException("Yeu cau chua den buoc thuc hien.");
        }

        // Ky xac nhan thuc hien bang OTP.
        OtpVerifyResult result = otpService.verifyOtp(session.getUsername(), otp, "EXECUTION", id);
        RequestSignature sig = new RequestSignature();
        sig.setRequestId(id);
        sig.setSignerUserId(session.getUserId());
        sig.setSignerRoleCode(role.name());
        sig.setSigningScope(SigningScope.EXECUTION);
        sig.setOtpTransactionId(result.getTransactionId());
        sig.setSignedAt(LocalDateTime.now());
        sig.setResult(result.isSuccess() ? "SUCCESS" : "FAILED");
        userRepository.findById(session.getUserId())
                .ifPresent(u -> sig.setSignatureImageId(u.getSignatureImageId()));
        signatureRepository.save(sig);
        if (!result.isSuccess()) {
            throw new BusinessException("Ky xac nhan that bai: " + result.getMessage());
        }

        RequestStatus from = r.getStatus();
        String stepCode = r.getCurrentStepCode();
        workflowService.complete(r);
        requestRepository.save(r);

        // Nhat ky cong viec 07-NKCV.
        WorkLog07 log = new WorkLog07();
        log.setRequestId(id);
        log.setActorUserId(session.getUserId());
        log.setActorRoleCode(role.name());
        log.setWorkContent(note == null ? "Da thuc hien/mo truy cap" : note);
        log.setStartTime(parse(startTime));
        log.setEndTime(parse(endTime));
        workLogRepository.save(log);

        historyService.record(id, stepCode, from, RequestStatus.COMPLETED,
                session.getUserId(), role.name(), WorkflowAction.EXECUTE,
                note == null ? "Xac nhan thuc hien" : note);

        notificationService.notifyRequester(r, NotificationEvent.COMPLETED, session.getUsername());

        auditService.record(session.getUsername(), role.name(), "EXECUTE",
                "access_request", id, "Xac nhan thuc hien, hoan thanh phieu");
    }

    private LocalDateTime parse(String value) {
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
