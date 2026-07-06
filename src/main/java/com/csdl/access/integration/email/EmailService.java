package com.csdl.access.integration.email;

import com.csdl.access.notification.WorkflowNotification;

import java.util.List;

/**
 * Interface gui email thong bao nghiep vu (features/integrations.md muc 5).
 */
public interface EmailService {

    /** Gui mot email thong bao theo su kien nghiep vu. */
    void sendWorkflowNotification(WorkflowNotification notification);

    /** Gui nhieu email thong bao mot lan (lap qua tung phan tu). */
    void sendBatch(List<WorkflowNotification> notifications);
}
