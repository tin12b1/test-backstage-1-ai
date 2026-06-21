package com.csdl.access.common.audit;

import com.csdl.access.domain.AuditLog;
import com.csdl.access.domain.repo.AuditLogRepository;
import org.springframework.stereotype.Service;

/**
 * Ghi audit log thao tac nghiep vu (architecture.md hard rules).
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String username, String roleCode, String action,
                       String entityType, Long entityId, String detail) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setRoleCode(roleCode);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetail(detail);
        auditLogRepository.save(log);
    }
}
