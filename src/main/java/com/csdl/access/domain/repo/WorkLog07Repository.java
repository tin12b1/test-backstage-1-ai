package com.csdl.access.domain.repo;

import com.csdl.access.domain.WorkLog07;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkLog07Repository extends JpaRepository<WorkLog07, Long> {
    List<WorkLog07> findByRequestId(Long requestId);
}
