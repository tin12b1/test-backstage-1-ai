package com.csdl.access.workflow;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.audit.AuditService;
import com.csdl.access.common.enums.ActorType;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.enums.WorkflowAction;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.EmergencyCompletionLink;
import com.csdl.access.domain.RequestDetail;
import com.csdl.access.domain.Role;
import com.csdl.access.domain.UserRole;
import com.csdl.access.domain.repo.*;
import com.csdl.access.notification.NotificationEvent;
import com.csdl.access.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Khoi tao luong xu ly khi nguoi lap gui phe duyet (features/request-create.md muc 14).
 * Kiem tra rang buoc nghiep vu, xac dinh don vi chu quan, bat dau workflow va gui email.
 *
 * Responsibilities:
 * - Set current_step_code format: {TYPE}_{VARIANT}_{01} or {TYPE}_{01}
 * - Set at_requester_phase based on variant/step mapping
 * - Resolve next actor (role, unit, actor_id)
 * - Set request status (PENDING_APPROVAL, PENDING_CHECK, SENT_TO_ACCESS_TEAM, etc.)
 * - Record workflow_history entry with action = SUBMIT
 */
@Service
public class RequestSubmissionService {

    private final AccessRequestRepository requestRepository;
    private final RequestDetailRepository detailRepository;
    private final RequestSignatureRepository signatureRepository;
    private final InformationSystemRepository systemRepository;
    private final DatabaseCatalogRepository databaseRepository;
    private final EmergencyCompletionLinkRepository linkRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
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
                                    UserRoleRepository userRoleRepository,
                                    RoleRepository roleRepository,
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
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
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

        // Khoi tao workflow: set step_code, status, at_requester_phase, current_actor_role, current_unit_id
        workflowService.start(r);

        // Resolve current_actor_id dua tren role + unit da duoc set boi workflowService.start()
        resolveNextActor(r, r.getCurrentStepCode());

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

    /**
     * Xac dinh actor (nguoi xu ly) tiep theo dua tren step code hien tai.
     * Tra ve ActorInfo chua thong tin actor va dong thoi cap nhat vao request.
     *
     * Logic:
     * - Lay role code va unit_id tu access_request (da duoc set boi WorkflowService.start/advance)
     * - Tra cuu user_role de tim user co vai tro tuong ung tai don vi do
     * - Set current_actor_type, current_actor_id
     * - Voi ACCESS_TEAM: actor_type = TEAM, actor_id = null (xu ly theo nhom)
     * - Voi cac role khac: actor_type = ROLE, actor_id = user_id cua nguoi duoc phan cong
     *
     * @param request  phieu yeu cau da co current_actor_role va current_unit_id
     * @param stepCode step code hien tai (de xac dinh pham vi)
     * @return ActorInfo voi actorType, actorId, actorRole, unitId; null neu khong xac dinh duoc
     */
    public ActorInfo resolveNextActor(AccessRequest request, String stepCode) {
        String actorRoleStr = request.getCurrentActorRole();
        if (actorRoleStr == null) {
            return null;
        }

        RoleCode roleCode;
        try {
            roleCode = RoleCode.valueOf(actorRoleStr);
        } catch (IllegalArgumentException e) {
            return null;
        }

        Long unitId = request.getCurrentUnitId();

        // ACCESS_TEAM xu ly theo nhom, khong can xac dinh ca nhan cu the
        if (roleCode == RoleCode.ACCESS_TEAM) {
            request.setCurrentActorType(ActorType.TEAM);
            request.setCurrentActorId(null);
            return ActorInfo.ofTeam(actorRoleStr, unitId);
        }

        request.setCurrentActorType(ActorType.ROLE);

        // Tra cuu role entity de lay role_id
        Optional<Role> roleEntity = roleRepository.findByCode(roleCode.name());
        if (roleEntity.isEmpty()) {
            request.setCurrentActorId(null);
            return ActorInfo.ofRole(null, actorRoleStr, unitId);
        }

        Long roleId = roleEntity.get().getId();
        List<UserRole> candidates = userRoleRepository.findByRoleIdAndActiveTrue(roleId);

        // Loc theo don vi
        Long resolvedActorId = null;
        for (UserRole ur : candidates) {
            // Match theo unit_id
            if (unitId != null && ur.getUnitId() != null && !unitId.equals(ur.getUnitId())) {
                continue;
            }

            // Doi voi CHECKER va EXECUTOR, can match them system_id
            if ((roleCode == RoleCode.CHECKER || roleCode == RoleCode.EXECUTOR)
                    && request.getSystemId() != null
                    && ur.getSystemId() != null
                    && !request.getSystemId().equals(ur.getSystemId())) {
                continue;
            }

            // Doi voi DBA, can match them database_id
            if (roleCode == RoleCode.DBA
                    && request.getDatabaseId() != null
                    && ur.getDatabaseId() != null
                    && !request.getDatabaseId().equals(ur.getDatabaseId())) {
                continue;
            }

            resolvedActorId = ur.getUserId();
            break;
        }

        request.setCurrentActorId(resolvedActorId);
        return ActorInfo.ofRole(resolvedActorId, actorRoleStr, unitId);
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

        // 01-YCTC, 04A-YCTK: Xoa cac dong chi tiet chua ky truoc khi gui.
        // Sau khi xoa, kiem tra con it nhat 1 dong da ky.
        if (type == RequestType.YCTC_01 || type == RequestType.YCTK_04A) {
            // Xoa tat ca dong chua co chu ky thanh cong
            detailRepository.deleteUnsignedByRequestId(r.getId());

            // Kiem tra con it nhat 1 dong chi tiet sau khi xoa
            long remainingCount = detailRepository.countByRequestId(r.getId());
            if (remainingCount == 0) {
                throw new BusinessException(
                        "Phieu phai co it nhat 1 dong chi tiet da ky xac nhan truoc khi gui.");
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
