package com.csdl.access.workflow;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.WorkflowAction;
import com.csdl.access.domain.WorkflowHistory;
import com.csdl.access.domain.repo.WorkflowHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Ghi va doc lich su xu ly workflow_history (ADR 0002). */
@Service
public class WorkflowHistoryService {

    private final WorkflowHistoryRepository repository;

    public WorkflowHistoryService(WorkflowHistoryRepository repository) {
        this.repository = repository;
    }

    public void record(Long requestId, String stepCode,
                       RequestStatus from, RequestStatus to,
                       Long actorUserId, String actorRoleCode,
                       WorkflowAction action, String comment) {
        WorkflowHistory h = new WorkflowHistory();
        h.setRequestId(requestId);
        h.setStepCode(stepCode);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setActorUserId(actorUserId);
        h.setActorRoleCode(actorRoleCode);
        h.setAction(action);
        h.setCommentText(comment);
        repository.save(h);
    }

    public List<WorkflowHistory> history(Long requestId) {
        return repository.findByRequestIdOrderByProcessedAtAsc(requestId);
    }
}
