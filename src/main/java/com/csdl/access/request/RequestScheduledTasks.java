package com.csdl.access.request;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.notification.NotificationEvent;
import com.csdl.access.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks lien quan den yeu cau truy cap CSDL:
 * - Het han dang ky truoc (moi gio)
 * - Nhac nho 04B cho nguoi nhan ky ban giao qua 3 ngay (moi ngay 8h sang)
 */
@Service
public class RequestScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(RequestScheduledTasks.class);

    private final PreRegistrationService preRegistrationService;
    private final AccessRequestRepository accessRequestRepository;
    private final NotificationService notificationService;

    public RequestScheduledTasks(PreRegistrationService preRegistrationService,
                                 AccessRequestRepository accessRequestRepository,
                                 NotificationService notificationService) {
        this.preRegistrationService = preRegistrationService;
        this.accessRequestRepository = accessRequestRepository;
        this.notificationService = notificationService;
    }

    /**
     * Het han dang ky truoc — chay moi gio.
     * Danh dau cac ban ghi pre-registration co date+shift da qua thanh EXPIRED.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void expirePreRegistrations() {
        int expired = preRegistrationService.expireOutdatedRegistrations();
        log.info("Expired {} pre-registration records", expired);
    }

    /**
     * Nhac nho 04B-BGTK cho nguoi nhan ky ban giao — chay moi ngay luc 8h sang.
     * Tim cac phieu co trang thai PENDING_RECEIPT va submitted_at truoc 3 ngay,
     * gui email nhac nho cho DBA, DBA manager va nguoi nhan chua ky.
     * Luu y: Trang thai PENDING_RECEIPT chi ap dung cho phieu 04B-BGTK.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void remind04BPendingReceipt() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);
        List<AccessRequest> overdue = accessRequestRepository
                .findByRequestTypeAndStatusAndSubmittedAtBefore(
                        RequestType.YCTK_04A, RequestStatus.PENDING_RECEIPT, threshold);
        for (AccessRequest request : overdue) {
            try {
                notificationService.notifyRequester(request,
                        NotificationEvent.REMINDER_04B_PENDING_RECEIPT, "System");
                log.debug("Sent 04B reminder for request id={}, code={}",
                        request.getId(), request.getRequestCode());
            } catch (Exception e) {
                log.warn("Failed to send 04B reminder for request id={}: {}",
                        request.getId(), e.getMessage());
            }
        }
        if (!overdue.isEmpty()) {
            log.info("Sent 04B pending receipt reminders for {} overdue requests", overdue.size());
        }
    }
}
