package com.csdl.access.domain;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.WorkflowAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** workflow_history - toan bo tien trinh xu ly. */
@Entity
@Table(name = "workflow_history")
@Getter
@Setter
public class WorkflowHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "step_code", length = 60)
    private String stepCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 40)
    private RequestStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 40)
    private RequestStatus toStatus;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_role_code", length = 50)
    private String actorRoleCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WorkflowAction action;

    @Column(length = 2000)
    private String comment;

    @Column(name = "processed_at")
    private LocalDateTime processedAt = LocalDateTime.now();
}
