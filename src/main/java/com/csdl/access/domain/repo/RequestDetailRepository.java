package com.csdl.access.domain.repo;

import com.csdl.access.domain.RequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestDetailRepository extends JpaRepository<RequestDetail, Long> {
    List<RequestDetail> findByRequestId(Long requestId);
    long countByRequestId(Long requestId);
    void deleteByRequestId(Long requestId);
    List<RequestDetail> findByTargetUserId(Long targetUserId);

    @Modifying
    @Query("DELETE FROM RequestDetail d WHERE d.requestId = :requestId " +
           "AND d.id NOT IN (SELECT s.detailId FROM RequestSignature s " +
           "WHERE s.requestId = :requestId AND s.detailId IS NOT NULL AND s.result = 'SUCCESS')")
    int deleteUnsignedByRequestId(@Param("requestId") Long requestId);
}
