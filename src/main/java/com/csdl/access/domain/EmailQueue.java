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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "to_address", nullable = false, length = 300)
    private String toAddress;

    @Column(length = 300)
    private String subject;

    @Lob
    @Column(name = "body")
    private String body;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "event_type", length = 60)
    private String eventType;

    /** PENDING/SENT/FAILED. */
    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
