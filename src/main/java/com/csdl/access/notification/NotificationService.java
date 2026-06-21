package com.csdl.access.notification;

import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.Role;
import com.csdl.access.domain.UserRole;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.RoleRepository;
import com.csdl.access.domain.repo.UserRoleRepository;
import com.csdl.access.integration.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Xay dung va gui thong bao email khi co su kien workflow (architecture.md muc 7).
 */
@Service
public class NotificationService {

    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AppUserRepository userRepository;

    @Value("${integration.email.base-url:http://localhost:8080/app}")
    private String baseUrl;

    public NotificationService(EmailService emailService,
                               RoleRepository roleRepository,
                               UserRoleRepository userRoleRepository,
                               AppUserRepository userRepository) {
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
    }

    /** Gui thong bao cho tat ca nguoi giu vai tro dang xu ly (trong pham vi don vi neu co). */
    public void notifyActorRole(AccessRequest request, NotificationEvent event,
                                RoleCode targetRole, Long unitId, String fromUser) {
        List<WorkflowNotification> batch = new ArrayList<>();
        for (String email : recipientEmails(targetRole, unitId)) {
            batch.add(build(request, event, email, targetRole, fromUser));
        }
        emailService.sendBatch(batch);
    }

    /** Gui thong bao cho nguoi lap phieu (vd: khi bi chuyen tra hoac hoan thanh). */
    public void notifyRequester(AccessRequest request, NotificationEvent event, String fromUser) {
        userRepository.findById(request.getRequesterUserId()).ifPresent(u -> {
            if (u.getEmail() != null) {
                emailService.sendWorkflowNotification(
                        build(request, event, u.getEmail(), RoleCode.REQUESTER, fromUser));
            }
        });
    }

    private List<String> recipientEmails(RoleCode role, Long unitId) {
        List<String> emails = new ArrayList<>();
        Role roleEntity = roleRepository.findByCode(role.name()).orElse(null);
        if (roleEntity == null) {
            return emails;
        }
        for (UserRole ur : userRoleRepository.findByRoleIdAndActiveTrue(roleEntity.getId())) {
            // Loc theo pham vi don vi neu user_role co rang buoc don vi.
            if (unitId != null && ur.getUnitId() != null && !unitId.equals(ur.getUnitId())) {
                continue;
            }
            userRepository.findById(ur.getUserId()).ifPresent(u -> {
                if (u.getEmail() != null && !emails.contains(u.getEmail())) {
                    emails.add(u.getEmail());
                }
            });
        }
        return emails;
    }

    private WorkflowNotification build(AccessRequest r, NotificationEvent event,
                                       String toEmail, RoleCode targetRole, String fromUser) {
        WorkflowNotification n = new WorkflowNotification();
        n.setToAddress(toEmail);
        n.setRequestId(r.getId());
        n.setRequestCode(r.getRequestCode());
        n.setRequestType(r.getRequestType() != null ? r.getRequestType().getDisplayName() : "");
        n.setStatus(r.getStatus() != null ? r.getStatus().getDisplayName() : "");
        n.setEventType(event.name());
        n.setEventLabel(event.getDescription());
        n.setFromUser(fromUser);
        n.setTargetActor(targetRole != null ? targetRole.getDisplayName() : "");
        n.setLink(baseUrl + "/requests/" + r.getId());
        return n;
    }
}
