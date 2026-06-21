package com.csdl.access.workflow;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.audit.AuditService;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.enums.WorkflowAction;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.EmergencyCompletionLink;
import com.csdl.access.domain.RequestDetail;
import com.csdl.access.domain.repo.*;
import com.csdl.access.notification.NotificationEvent;
import com.csdl.access.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Khoi tao luong xu ly khi nguoi lap gui phe duyet (features/request-create.md muc 14).
 * Kiem tra rang buoc nghiep vu, xac dinh don vi chu quan, bat dau workflow va gui email.
 */
@Service
public class RequestSubmissionService {

    private final AccessRequestRepository requestRepository;
    private final RequestDetailRepository detailRepository;
    private final RequestSignatureRepository signatureRepository;
    private final InformationSystemRepository systemRepository;
    private final DatabaseCatalogRepository databaseRepository;
    private final EmergencyCompletionLinkRepository linkRepository;
    private final WorkflowService workflowService;
    private final WorkflowHistoryService historyService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public RequestSubmissionService(AccessRequestRepository requestRepository,
                                    RequestDetailRepository detailRepository,
                                    RequestSignatureRepository signatureRepository,
                                    InformationSystemRepository systemRepository,
                                    DatabaseCatalogRepository databaseRepository,
                                    EmergencyCompletionLinkRepository linkRepository,
                                    WorkflowService workflowService,
                                    WorkflowHistoryService historyService,
                                    NotificationService notificationService,
                                    AuditService auditService) {
        this.requestRepository = requestRepository;
        this.detailRepository = detailRepository;
        this.signatureRepository = signatureRepository;
        this.systemRepository = systemRepository;
        this.databaseRepository = databaseRepository;
        this.linkRepository = linkRepository;
        this.workflowService = workflowService;
        this.historyService = historyService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public AccessRequest submit(Long requestId, Long emergencyRequestId, UserSession session) {
        AccessRequest r = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));

        if (!r.getRequesterUserId().equals(session.getUserId())) {
            throw new BusinessException("Ban khong phai nguoi lap phieu nay.");
        }
        if (!r.getStatus().isEditable()) {
            throw new BusinessException("Phieu da duoc gui, khong the gui lai.");
        }

        validate(r, emergencyRequestId);
        resolveOwnership(r);

        RequestStatus from = r.getStatus();
        workflowService.start(r);
        requestRepository.save(r);

        // Lien ket 05B voi 05A -> giai phong "no" phieu.
        if (r.getRequestType() == RequestType.HTKC_05B && emergencyRequestId != null) {
            EmergencyCompletionLink link = new EmergencyCompletionLink();
            link.setEmergencyRequestId(emergencyRequestId);
            link.setCompletionRequestId(r.getId());
            linkRepository.save(link);
        }

        historyService.record(r.getId(), r.getCurrentStepCode(), from, r.getStatus(),
                session.getUserId(),
                session.getActiveRole() == null ? RoleCode.REQUESTER.name() : session.getActiveRole().name(),
                WorkflowAction.SUBMIT, "Gui phe duyet");

        notifyNextActor(r, session.getUsername());

        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "SUBMIT", "access_request", r.getId(), "Gui phe duyet " + r.getRequestCode());
        return r;
    }

    @Transactional
    public AccessRequest resend(Long requestId, UserSession session) {
        AccessRequest r = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));
        if (r.getStatus() != RequestStatus.SEND_FAILED) {
            throw new BusinessException("Chi gui lai duoc khi phieu o trang thai gui loi.");
        }
        return submit(requestId, null, session);
    }

    private void validate(AccessRequest r, Long emergencyRequestId) {
        RequestType type = r.getRequestType();
        List<RequestDetail> details = detailRepository.findByRequestId(r.getId());

        // Toi thieu 1 dong chi tiet voi 01-YCTC, 04A-YCTK.
        if (type.requiresDetailLines() && details.isEmpty()) {
            throw new BusinessException("Phieu " + type.getFormCode()
                    + " phai co toi thieu mot dong chi tiet.");
        }

        // Nguoi yeu cau phai ky thanh cong.
        if (!signatureRepository.existsByRequestIdAndSignerUserIdAndResult(
                r.getId(), r.getRequesterUserId(), "SUCCESS")) {
            throw new BusinessException("Nguoi lap phai ky xac nhan truoc khi gui.");
        }

        // Nguoi dung chung tren phieu (target user khac nguoi lap) phai ky.
        if (type == RequestType.YCTC_01 || type == RequestType.YCTK_04A) {
            Set<Long> sharedUsers = new HashSet<>();
            for (RequestDetail d : details) {
                if (d.getTargetUserId() != null && !d.getTargetUserId().equals(r.getRequesterUserId())) {
                    sharedUsers.add(d.getTargetUserId());
                }
            }
            for (Long uid : sharedUsers) {
                if (!signatureRepository.existsByRequestIdAndSignerUserIdAndResult(
                        r.getId(), uid, "SUCCESS")) {
                    throw new BusinessException(
                            "Con nguoi dung chung phieu chua ky xac nhan (user id " + uid + ").");
                }
            }
        }

        // 05B phai lien ket toi mot phieu 05A chua hoan thien.
        if (type == RequestType.HTKC_05B) {
            if (emergencyRequestId == null) {
                throw new BusinessException("Phieu 05B phai chon mot phieu 05A-YCKC tuong ung.");
            }
            if (linkRepository.findByEmergencyRequestId(emergencyRequestId).isPresent()) {
                throw new BusinessException("Phieu 05A da co 05B lien ket.");
            }
        }
    }

    /** Xac dinh don vi chu quan ung dung va CSDL tu he thong/CSDL tren phieu hoac dong chi tiet. */
    private void resolveOwnership(AccessRequest r) {
        Long systemId = r.getSystemId();
        Long databaseId = r.getDatabaseId();
        if (systemId == null || databaseId == null) {
            List<RequestDetail> details = detailRepository.findByRequestId(r.getId());
            for (RequestDetail d : details) {
                if (systemId == null && d.getSystemId() != null) {
                    systemId = d.getSystemId();
                }
                if (databaseId == null && d.getDatabaseId() != null) {
                    databaseId = d.getDatabaseId();
                }
            }
        }
        if (systemId != null) {
            systemRepository.findById(systemId)
                    .ifPresent(s -> r.setOwnerUnitId(s.getOwnerUnitId()));
        }
        if (databaseId != null) {
            databaseRepository.findById(databaseId)
                    .ifPresent(d -> r.setOwnerDbUnitId(d.getOwnerUnitId()));
        }
        // Mac dinh: neu khong xac dinh duoc chu quan ung dung, coi don vi yeu cau la chu quan.
        if (r.getOwnerUnitId() == null) {
            r.setOwnerUnitId(r.getRequesterUnitId());
        }
        if (r.getOwnerDbUnitId() == null) {
            r.setOwnerDbUnitId(r.getOwnerUnitId());
        }
    }

    private void notifyNextActor(AccessRequest r, String fromUser) {
        RoleCode role = RoleCode.valueOf(r.getCurrentActorRole());
        NotificationEvent event = (role == RoleCode.ACCESS_TEAM
                || role == RoleCode.DBA || role == RoleCode.EXECUTOR)
                ? NotificationEvent.SENT_TO_PROCESSING
                : NotificationEvent.NEW_REQUEST_PENDING;
        notificationService.notifyActorRole(r, event, role, r.getCurrentUnitId(), fromUser);
    }
}
