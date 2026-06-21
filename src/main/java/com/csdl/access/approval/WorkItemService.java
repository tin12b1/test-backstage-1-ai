package com.csdl.access.approval;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.common.lookup.RequestRow;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.repo.AccessRequestRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Danh sach yeu cau cho xu ly theo vai tro hien hanh (features/approval-processing.md muc 2).
 */
@Service
public class WorkItemService {

    private final AccessRequestRepository requestRepository;
    private final LookupService lookupService;

    public WorkItemService(AccessRequestRepository requestRepository, LookupService lookupService) {
        this.requestRepository = requestRepository;
        this.lookupService = lookupService;
    }

    /** Cac yeu cau dang cho vai tro hien hanh xu ly. */
    public List<AccessRequest> pendingForRole(RoleCode role) {
        List<AccessRequest> result = new ArrayList<>();
        if (role == null) {
            return result;
        }
        for (AccessRequest r : requestRepository.findByCurrentActorRole(role.name())) {
            if (r.getStatus() != RequestStatus.COMPLETED
                    && r.getStatus() != RequestStatus.CANCELLED
                    && r.getStatus() != RequestStatus.RETURNED) {
                result.add(r);
            }
        }
        return result;
    }

    public List<RequestRow> pendingRows(UserSession session) {
        List<RequestRow> rows = new ArrayList<>();
        for (AccessRequest r : pendingForRole(session.getActiveRole())) {
            rows.add(lookupService.toRow(r));
        }
        return rows;
    }
}
