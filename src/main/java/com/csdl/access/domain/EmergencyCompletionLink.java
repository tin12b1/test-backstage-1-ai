package com.csdl.access.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** emergency_completion_link - lien ket 05B-HTKC voi 05A-YCKC. */
@Entity
@Table(name = "emergency_completion_link")
@Getter
@Setter
public class EmergencyCompletionLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emergency_request_id", nullable = false)
    private Long emergencyRequestId;

    @Column(name = "completion_request_id", nullable = false)
    private Long completionRequestId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
