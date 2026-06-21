package com.csdl.access.integration.email;

import com.csdl.access.notification.WorkflowNotification;

import java.util.List;

/**
 * Interface gui email thong bao nghiep vu (features/integrations.md muc 5).
 */
public interface EmailService {

    void sendWorkflowNotification(WorkflowNotification notification);

    void sendBatch(List<WorkflowNotification> notifications);
}
