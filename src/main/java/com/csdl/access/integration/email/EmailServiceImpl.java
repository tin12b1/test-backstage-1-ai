package com.csdl.access.integration.email;

import com.csdl.access.domain.EmailQueue;
import com.csdl.access.domain.repo.EmailQueueRepository;
import com.csdl.access.notification.WorkflowNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gui email qua hang doi email_queue. Loi gui khong lam mat du lieu yeu cau,
 * ban ghi duoc danh dau FAILED va cho phep retry (features/integrations.md muc 5).
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailQueueRepository emailQueueRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${integration.email.enabled:true}")
    private boolean enabled;

    @Value("${integration.email.sender:no-reply@csdl.local}")
    private String sender;

    @Value("${integration.email.base-url:http://localhost:8080/app}")
    private String baseUrl;

    @Value("${integration.email.max-retry:3}")
    private int maxRetry;

    public EmailServiceImpl(EmailQueueRepository emailQueueRepository,
                            ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.emailQueueRepository = emailQueueRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    @Transactional
    public void sendWorkflowNotification(WorkflowNotification n) {
        EmailQueue queue = new EmailQueue();
        queue.setToAddress(n.getToAddress());
        queue.setSubject(buildSubject(n));
        queue.setBody(buildBody(n));
        queue.setRequestId(n.getRequestId());
        queue.setEventType(n.getEventType());
        queue.setStatus("PENDING");
        queue = emailQueueRepository.save(queue);

        attemptSend(queue);
    }

    @Override
    @Transactional
    public void sendBatch(List<WorkflowNotification> notifications) {
        if (notifications == null) {
            return;
        }
        for (WorkflowNotification n : notifications) {
            sendWorkflowNotification(n);
        }
    }

    /** Gui lai cac email loi (co the goi tu scheduler hoac man hinh quan tri). */
    @Transactional
    public void retryFailed() {
        for (EmailQueue queue : emailQueueRepository.findByStatus("FAILED")) {
            if (queue.getRetryCount() < maxRetry) {
                attemptSend(queue);
            }
        }
    }

    private void attemptSend(EmailQueue queue) {
        if (!enabled) {
            log.info("[EMAIL] Tat gui email; chi luu hang doi cho yeu cau requestId={}", queue.getRequestId());
            return;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            queue.setStatus("PENDING");
            log.warn("[EMAIL] Chua cau hinh JavaMailSender; giu trang thai PENDING");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(this.sender);
            msg.setTo(queue.getToAddress());
            msg.setSubject(queue.getSubject());
            msg.setText(queue.getBody());
            sender.send(msg);

            queue.setStatus("SENT");
            queue.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            queue.setStatus("FAILED");
            queue.setRetryCount(queue.getRetryCount() + 1);
            queue.setLastError(e.getMessage());
            log.error("[EMAIL] Gui that bai cho {}: {}", queue.getToAddress(), e.getMessage());
        }
        emailQueueRepository.save(queue);
    }

    private String buildSubject(WorkflowNotification n) {
        return String.format("[CSDL-Access] %s - %s", safe(n.getRequestCode()), safe(eventText(n)));
    }

    private String eventText(WorkflowNotification n) {
        return n.getEventLabel() != null ? n.getEventLabel() : n.getEventType();
    }

    private String buildBody(WorkflowNotification n) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ma yeu cau: ").append(safe(n.getRequestCode())).append("\n");
        sb.append("Loai yeu cau: ").append(safe(n.getRequestType())).append("\n");
        sb.append("Trang thai: ").append(safe(n.getStatus())).append("\n");
        sb.append("Nguoi gui: ").append(safe(n.getFromUser())).append("\n");
        sb.append("Nguoi/bo phan can xu ly: ").append(safe(n.getTargetActor())).append("\n");
        String link = n.getLink() != null ? n.getLink()
                : baseUrl + "/requests/" + (n.getRequestId() == null ? "" : n.getRequestId());
        sb.append("Link truy cap: ").append(link).append("\n");
        return sb.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
