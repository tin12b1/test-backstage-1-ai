package com.csdl.access.domain.repo;

import com.csdl.access.domain.RequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestDetailRepository extends JpaRepository<RequestDetail, Long> {
    List<RequestDetail> findByRequestId(Long requestId);
    long countByRequestId(Long requestId);
    void deleteByRequestId(Long requestId);
    List<RequestDetail> findByTargetUserId(Long targetUserId);
}
