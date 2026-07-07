package com.csdl.access.domain.repo;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.domain.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessRequestRepository
        extends JpaRepository<AccessRequest, Long>, JpaSpecificationExecutor<AccessRequest> {

    List<AccessRequest> findByRequesterUserIdOrderByCreatedAtDesc(Long requesterUserId);

    List<AccessRequest> findByRequesterUserIdAndStatus(Long requesterUserId, RequestStatus status);

    List<AccessRequest> findByRequesterUserIdAndStatusOrderByCreatedAtDesc(Long requesterUserId, RequestStatus status);

    long countByRequesterUserIdAndStatus(Long requesterUserId, RequestStatus status);

    long countByRequesterUserId(Long requesterUserId);

    List<AccessRequest> findByStatusIn(List<RequestStatus> statuses);

    List<AccessRequest> findByCurrentActorRoleAndStatusIn(String currentActorRole, List<RequestStatus> statuses);

    List<AccessRequest> findByCurrentActorRole(String currentActorRole);

    long countByRequestCodeStartingWith(String prefix);

    /** Phieu 05A chua duoc lien ket voi 05B (phuc vu chon trong 05B-HTKC). */
    List<AccessRequest> findByRequestTypeAndRequesterUserId(RequestType requestType, Long requesterUserId);

    /** Kiem tra co phieu 04B nao da lien ket voi phieu 04A nay chua. */
    List<AccessRequest> findBySourceRequestId(Long sourceRequestId);

    /** Tim phieu theo loai, trang thai va ngay gui truoc threshold (phuc vu nhac nho 04B qua han). */
    List<AccessRequest> findByRequestTypeAndStatusAndSubmittedAtBefore(
            RequestType requestType, RequestStatus status, LocalDateTime threshold);

    /** Tim phieu theo trang thai va ngay gui truoc threshold (PENDING_RECEIPT chi dung cho 04B). */
    List<AccessRequest> findByStatusAndSubmittedAtBefore(RequestStatus status, LocalDateTime threshold);

    /**
     * Dem so phieu 05A-YCKC da hoan thanh cua user ma:
     * - Khong co ban ghi lien ket trong emergency_completion_link
     * - completed_at truoc threshold (qua han > 3 ngay)
     */
    @Query("SELECT COUNT(ar) FROM AccessRequest ar " +
           "WHERE ar.requesterUserId = :userId " +
           "AND ar.requestType = com.csdl.access.common.enums.RequestType.YCKC_05A " +
           "AND ar.status = com.csdl.access.common.enums.RequestStatus.COMPLETED " +
           "AND ar.completedAt < :threshold " +
           "AND ar.id NOT IN (SELECT ecl.emergencyRequestId FROM EmergencyCompletionLink ecl)")
    long countOverdue05AWithoutLinked05B(@Param("userId") Long userId,
                                         @Param("threshold") LocalDateTime threshold);

    /**
     * Tim phieu 04A-YCTK da COMPLETED ma chua co 04B lien ket (source_request_id tro ve).
     * Dung cho endpoint GET /requests/pending-04a.
     */
    @Query("SELECT ar FROM AccessRequest ar " +
           "WHERE ar.requestType = com.csdl.access.common.enums.RequestType.YCTK_04A " +
           "AND ar.status = com.csdl.access.common.enums.RequestStatus.COMPLETED " +
           "AND ar.id NOT IN (SELECT ar2.sourceRequestId FROM AccessRequest ar2 " +
           "WHERE ar2.requestType = com.csdl.access.common.enums.RequestType.BGTK_04B " +
           "AND ar2.sourceRequestId IS NOT NULL)")
    List<AccessRequest> findCompletedYCTK04AWithout04B();

    /**
     * Tim phieu 01-YCTC / 04A-YCTK dang PENDING_SIGN cung don vi, loai tru nguoi lap.
     * Dung cho muc "Phieu cho ky chung" tren danh sach yeu cau.
     */
    List<AccessRequest> findByRequestTypeInAndStatusAndRequesterUnitIdAndRequesterUserIdNot(
            List<RequestType> types, RequestStatus status, Long unitId, Long excludeUserId);

    /**
     * Tim phieu 05A-YCKC dang no (da xu ly nhung chua co 05B lien ket) cua user.
     * Dung cho endpoint GET /requests/pending-05a-groups.
     */
    @Query("SELECT ar FROM AccessRequest ar " +
           "WHERE ar.requestType = com.csdl.access.common.enums.RequestType.YCKC_05A " +
           "AND ar.requesterUserId = :userId " +
           "AND ar.status <> com.csdl.access.common.enums.RequestStatus.DRAFT " +
           "AND ar.status <> com.csdl.access.common.enums.RequestStatus.CANCELLED " +
           "AND ar.id NOT IN (SELECT ecl.emergencyRequestId FROM EmergencyCompletionLink ecl)")
    List<AccessRequest> findOutstanding05AForUser(@Param("userId") Long userId);
}
