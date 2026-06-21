package com.csdl.access.domain.repo;

import com.csdl.access.domain.WorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, Long> {
    List<WorkflowHistory> findByRequestIdOrderByProcessedAtAsc(Long requestId);
    List<WorkflowHistory> findByActorUserId(Long actorUserId);
}
