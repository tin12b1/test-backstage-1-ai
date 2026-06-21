package com.csdl.access.approval;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.audit.AuditService;
import com.csdl.access.common.enums.*;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.*;
import com.csdl.access.domain.repo.*;
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
 * Phe duyet/ky xac nhan va chuyen tra yeu cau (features/approval-processing.md, ADR 0003).
 */
@Service
public class ApprovalService {

    private final AccessRequestRepository requestRepository;
    private final RequestSignatureRepository signatureRepository;
    private final WorkLog07Repository workLogRepository;
    private final AppUserRepository userRepository;
    private final WorkflowService workflowService;
    private final WorkflowHistoryService historyService;
    private final NotificationService notificationService;
    private final OtpService otpService;
    private final AuditService auditService;

    public ApprovalService(AccessRequestRepository requestRepository,
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
    public void approve(Long id, String otp, String comment, UserSession session) {
        AccessRequest r = load(id);
        RoleCode role = session.getActiveRole();
        assertCurrentActor(r, role);

        if (workflowService.isExecutionStep(r)) {
            throw new BusinessException("Buoc nay can xac nhan thuc hien, khong phai phe duyet.");
        }

        verifyOtpOrFail(r, id, otp, session, SigningScope.APPROVAL);

        RequestStatus from = r.getStatus();
        String stepCode = r.getCurrentStepCode();
        workflowService.advance(r);
        requestRepository.save(r);

        historyService.record(id, stepCode, from, r.getStatus(),
                session.getUserId(), role.name(), WorkflowAction.APPROVE,
                comment == null ? "Phe duyet" : comment);

        writeWorkLog(r, session, role, "Phe duyet/ky xac nhan: " + (comment == null ? "" : comment));

        notifyAfterApprove(r, role, session.getUsername());

        auditService.record(session.getUsername(), role.name(), "APPROVE",
                "access_request", id, "Phe duyet -> " + r.getStatus());
    }

    @Transactional
    public void returnRequest(Long id, String reason, UserSession session) {
        AccessRequest r = load(id);
        RoleCode role = session.getActiveRole();
        assertCurrentActor(r, role);

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("Phai nhap ly do khi chuyen tra.");
        }

        RequestStatus from = r.getStatus();
        String stepCode = r.getCurrentStepCode();

        // Chuyen tra ve nguoi lap, khong yeu cau OTP (ADR 0003).
        r.setStatus(RequestStatus.RETURNED);
        r.setCurrentActorType(ActorType.USER);
        r.setCurrentActorId(r.getRequesterUserId());
        r.setCurrentActorRole(RoleCode.REQUESTER.name());
        r.setCurrentStepCode(null);
        requestRepository.save(r);

        historyService.record(id, stepCode, from, RequestStatus.RETURNED,
                session.getUserId(), role.name(), WorkflowAction.RETURN, reason);

        notificationService.notifyRequester(r, NotificationEvent.REQUEST_RETURNED, session.getUsername());

        auditService.record(session.getUsername(), role.name(), "RETURN",
                "access_request", id, "Chuyen tra: " + reason);
    }

    private void notifyAfterApprove(AccessRequest r, RoleCode approver, String fromUser) {
        RoleCode nextRole = r.getCurrentActorRole() == null ? null
                : RoleCode.valueOf(r.getCurrentActorRole());
        NotificationEvent event;
        if (nextRole == RoleCode.ACCESS_TEAM || nextRole == RoleCode.DBA || nextRole == RoleCode.EXECUTOR) {
            event = NotificationEvent.SENT_TO_PROCESSING;
        } else if (approver == RoleCode.AUTHORITY) {
            event = NotificationEvent.AUTHORITY_APPROVED;
        } else if (approver == RoleCode.DEPT_MANAGER) {
            event = NotificationEvent.DEPT_APPROVED;
        } else {
            event = NotificationEvent.NEW_REQUEST_PENDING;
        }
        if (nextRole != null) {
            notificationService.notifyActorRole(r, event, nextRole, r.getCurrentUnitId(), fromUser);
        }
    }

    private void verifyOtpOrFail(AccessRequest r, Long id, String otp, UserSession session, SigningScope scope) {
        OtpVerifyResult result = otpService.verifyOtp(session.getUsername(), otp, scope.name(), id);
        RequestSignature sig = new RequestSignature();
        sig.setRequestId(id);
        sig.setSignerUserId(session.getUserId());
        sig.setSignerRoleCode(session.getActiveRole().name());
        sig.setSigningScope(scope);
        sig.setOtpTransactionId(result.getTransactionId());
        sig.setSignedAt(LocalDateTime.now());
        sig.setResult(result.isSuccess() ? "SUCCESS" : "FAILED");
        userRepository.findById(session.getUserId())
                .ifPresent(u -> sig.setSignatureImageId(u.getSignatureImageId()));
        signatureRepository.save(sig);
        if (!result.isSuccess()) {
            throw new BusinessException("Ky xac nhan that bai: " + result.getMessage());
        }
    }

    private void writeWorkLog(AccessRequest r, UserSession session, RoleCode role, String content) {
        WorkLog07 log = new WorkLog07();
        log.setRequestId(r.getId());
        log.setActorUserId(session.getUserId());
        log.setActorRoleCode(role.name());
        log.setWorkContent(content);
        workLogRepository.save(log);
    }

    private AccessRequest load(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));
    }

    private void assertCurrentActor(AccessRequest r, RoleCode role) {
        if (!workflowService.isCurrentActor(r, role)) {
            throw new BusinessException("Yeu cau khong o buoc xu ly cua vai tro hien hanh.");
        }
    }
}
