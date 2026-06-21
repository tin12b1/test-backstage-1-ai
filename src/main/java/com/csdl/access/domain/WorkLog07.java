package com.csdl.access.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** work_log_07 - nhat ky cong viec theo mau 07-NKCV. */
@Entity
@Table(name = "work_log_07")
@Getter
@Setter
public class WorkLog07 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_role_code", length = 50)
    private String actorRoleCode;

    @Column(name = "work_content", length = 2000)
    private String workContent;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
