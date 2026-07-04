package com.csdl.access.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** email_queue - hang doi gui email, ho tro retry. */
@Entity
@Table(name = "email_queue")
@Getter
@Setter
public class EmailQueue {

    /** Khoa chinh, tu tang. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dia chi email nguoi nhan. */
    @Column(name = "to_address", nullable = false, length = 300)
    private String toAddress;

    /** Tieu de email. */
    @Column(length = 300)
    private String subject;

    /** Noi dung email. */
    @Lob
    @Column(name = "body")
    private String body;

    /** Phieu yeu cau lien quan (neu co). */
    @Column(name = "request_id")
    private Long requestId;

    /** Loai su kien phat sinh email. */
    @Column(name = "event_type", length = 60)
    private String eventType;

    /** PENDING/SENT/FAILED. */
    @Column(length = 20)
    private String status = "PENDING";

    /** So lan da thu gui lai. */
    @Column(name = "retry_count")
    private int retryCount = 0;

    /** Thong bao loi cua lan gui gan nhat. */
    @Column(name = "last_error", length = 1000)
    private String lastError;

    /** Thoi diem tao ban ghi hang doi. */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Thoi diem gui thanh cong. */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
