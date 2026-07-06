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

    /** Bat/tat gui email thuc te; khi tat chi luu hang doi. */
    @Value("${integration.email.enabled:true}")
    private boolean enabled;

    /** Dia chi nguoi gui (From). */
    @Value("${integration.email.sender:ebanking@agribank.com.vn}")
    private String sender;

    /** URL goc ung dung, dung de sinh link truy cap phieu trong noi dung email. */
    @Value("${integration.email.base-url:http://localhost:8080/app}")
    private String baseUrl;

    /** So lan gui lai toi da cho email bi loi. */
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
        // Tao ban ghi hang doi (PENDING) truoc de khong mat du lieu neu gui loi.
        EmailQueue queue = new EmailQueue();
        queue.setToAddress(n.getToAddress());
        queue.setSubject(buildSubject(n));
        queue.setBody(buildBody(n));
        queue.setRequestId(n.getRequestId());
        queue.setEventType(n.getEventType());
        queue.setStatus("PENDING");
        queue = emailQueueRepository.save(queue);

        // Thu gui ngay; neu that bai se danh dau FAILED de retry sau.
        attemptSend(queue);
    }

    @Override
    @Transactional
    public void sendBatch(List<WorkflowNotification> notifications) {
        if (notifications == null) {
            return;
        }
        // Gui lan luot tung thong bao.
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

    /** Thuc hien gui email cho mot ban ghi hang doi va cap nhat trang thai (SENT/FAILED/PENDING). */
    private void attemptSend(EmailQueue queue) {
        if (!enabled) {
            log.info("[EMAIL] Tat gui email; chi luu hang doi cho yeu cau requestId={}", queue.getRequestId());
            return;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            // Chua cau hinh mail sender: giu PENDING de gui lai khi da cau hinh.
            queue.setStatus("PENDING");
            log.warn("[EMAIL] Chua cau hinh JavaMailSender; giu trang thai PENDING");
            return;
        }
        try {
            // Dung noi dung email don gian tu ban ghi hang doi va gui di.
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(this.sender);
            msg.setTo(queue.getToAddress());
            msg.setSubject(queue.getSubject());
            msg.setText(queue.getBody());
            sender.send(msg);

            queue.setStatus("SENT");
            queue.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            // Loi gui: danh dau FAILED, tang so lan thu va luu thong bao loi de retry.
            queue.setStatus("FAILED");
            queue.setRetryCount(queue.getRetryCount() + 1);
            queue.setLastError(e.getMessage());
            log.error("[EMAIL] Gui that bai cho {}: {}", queue.getToAddress(), e.getMessage());
        }
        emailQueueRepository.save(queue);
    }

    /** Dung tieu de email: [CSDL-Access] <ma yeu cau> - <su kien>. */
    private String buildSubject(WorkflowNotification n) {
        return String.format("[CSDL-Access] %s - %s", safe(n.getRequestCode()), safe(eventText(n)));
    }

    /** Lay nhan su kien de hien thi (uu tien label, neu khong co dung eventType). */
    private String eventText(WorkflowNotification n) {
        return n.getEventLabel() != null ? n.getEventLabel() : n.getEventType();
    }

    /** Dung noi dung email dang text gom thong tin phieu va link truy cap. */
    private String buildBody(WorkflowNotification n) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mã yêu cầu: ").append(safe(n.getRequestCode())).append("\n");
        sb.append("Loại yêu cầu: ").append(safe(n.getRequestType())).append("\n");
        sb.append("Trạng thái: ").append(safe(n.getStatus())).append("\n");
        sb.append("Người gửi: ").append(safe(n.getFromUser())).append("\n");
        sb.append("Người/bộ phận cần xử lý: ").append(safe(n.getTargetActor())).append("\n");
        String link = n.getLink() != null ? n.getLink()
                : baseUrl + "/requests/" + (n.getRequestId() == null ? "" : n.getRequestId());
        sb.append("Link truy cập: ").append(link).append("\n");
        return sb.toString();
    }

    /** Tra ve chuoi rong thay vi null de tranh in "null" trong email. */
    private String safe(String s) {
        return s == null ? "" : s;
    }
}
