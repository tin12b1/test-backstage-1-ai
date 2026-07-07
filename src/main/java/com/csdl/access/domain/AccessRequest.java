package com.csdl.access.domain;

import com.csdl.access.common.enums.ActorType;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** access_request - header phieu yeu cau. */
@Entity
@Table(name = "access_request")
@Getter
@Setter
public class AccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ma yeu cau: KyhieuDV_NgayThangNam_SoTT. */
    @Column(name = "request_code", unique = true, length = 80)
    private String requestCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(name = "requester_user_id", nullable = false)
    private Long requesterUserId;

    @Column(name = "requester_unit_id")
    private Long requesterUnitId;

    @Column(name = "requester_department_id")
    private Long requesterDepartmentId;

    @Column(name = "shift_no")
    private Integer shiftNo;

    @Column(name = "access_no")
    private Integer accessNo;

    @Column(name = "system_id")
    private Long systemId;

    @Column(name = "database_id")
    private Long databaseId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** Ngay thuc hien du kien, dung cho 03-YCCT. */
    @Column(name = "expected_execution_date")
    private LocalDateTime expectedExecutionDate;

    @Column(length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_actor_type", length = 20)
    private ActorType currentActorType;

    @Column(name = "current_actor_id")
    private Long currentActorId;

    /** Vai tro dang xu ly (tham chieu nhanh khi actor la ROLE/TEAM). */
    @Column(name = "current_actor_role", length = 50)
    private String currentActorRole;

    /** Ma buoc workflow hien tai (xac dinh vi tri trong chuoi xu ly). */
    @Column(name = "current_step_code", length = 60)
    private String currentStepCode;

    /** Don vi dang chiu trach nhiem xu ly o buoc hien tai. */
    @Column(name = "current_unit_id")
    private Long currentUnitId;

    @Column(name = "owner_unit_id")
    private Long ownerUnitId;

    @Column(name = "owner_db_unit_id")
    private Long ownerDbUnitId;

    /** 04B-BGTK: phieu 04A-YCTK duoc lien ket ban giao. */
    @Column(name = "source_request_id")
    private Long sourceRequestId;

    /** 04B-BGTK: nguoi nhan ban giao tai khoan (deprecated, dung detail rows). */
    @Column(name = "receiver_user_id")
    private Long receiverUserId;

    /** 04B-BGTK: lanh dao phong phu trach nguoi ban giao (DBA). */
    @Column(name = "handover_manager_id")
    private Long handoverManagerId;

    /** 04B-BGTK: lanh dao phong phu trach nguoi nhan ban giao. */
    @Column(name = "receiver_manager_id")
    private Long receiverManagerId;

    /** Danh dau giai doan trong luong cua don vi yeu cau (true) hay chu quan (false). */
    @Column(name = "at_requester_phase")
    private boolean atRequesterPhase = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /** 01-YCTC: Loai yeu cau phu (Truy van / Chinh sua). */
    @Column(name = "sub_type", length = 30)
    private String subType;

    /** Hash noi dung form (MD5) dung cho dirty check khi auto-save. */
    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
