package com.csdl.access.domain.repo;

import com.csdl.access.domain.PreRegistrationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PreRegistrationRepository extends JpaRepository<PreRegistrationRequest, Long> {

    Page<PreRegistrationRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<PreRegistrationRequest> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    List<PreRegistrationRequest> findByUnitCodeAndRegisterDateAndShiftAndStatus(
            String unitCode, LocalDate date, int shift, String status);

    List<PreRegistrationRequest> findByUnitCodeAndRegisterDateAndShiftAndStatusAndRequestType(
            String unitCode, LocalDate date, int shift, String status, String requestType);

    List<PreRegistrationRequest> findByRequestId(Long requestId);

    @Modifying
    @Query("UPDATE PreRegistrationRequest p SET p.status = :status WHERE p.requestId = :requestId")
    int updateStatusByRequestId(@Param("requestId") Long requestId, @Param("status") String status);

    @Modifying
    @Query("UPDATE PreRegistrationRequest p SET p.status = 'EXPIRED' " +
           "WHERE p.status = 'UNUSED' AND (p.registerDate < :today " +
           "OR (p.registerDate = :today AND p.shift < :currentShift))")
    int expireOutdated(@Param("today") LocalDate today, @Param("currentShift") int currentShift);

    boolean existsByUserIdAndRegisterDateAndShiftAndSystemIdAndDatabaseIdAndObjectNameAndAccessRights(
            Long userId, LocalDate date, int shift, Long systemId, Long databaseId,
            String objectName, String accessRights);
}
