package com.csdl.access.search;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.common.lookup.RequestRow;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.WorkflowHistory;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.domain.repo.RequestDetailRepository;
import com.csdl.access.domain.repo.WorkflowHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tra cuu yeu cau theo phan quyen (features/search-report.md).
 * Phan quyen ap dung o service, khong chi an tren giao dien.
 */
@Service
public class SearchService {

    private final AccessRequestRepository requestRepository;
    private final RequestDetailRepository detailRepository;
    private final WorkflowHistoryRepository historyRepository;
    private final LookupService lookupService;

    public SearchService(AccessRequestRepository requestRepository,
                         RequestDetailRepository detailRepository,
                         WorkflowHistoryRepository historyRepository,
                         LookupService lookupService) {
        this.requestRepository = requestRepository;
        this.detailRepository = detailRepository;
        this.historyRepository = historyRepository;
        this.lookupService = lookupService;
    }

    public List<RequestRow> search(SearchCriteria c, UserSession session) {
        List<AccessRequest> candidates = permissionScope(session);
        List<RequestRow> rows = new ArrayList<>();
        for (AccessRequest r : candidates) {
            if (matches(r, c)) {
                rows.add(lookupService.toRow(r));
            }
        }
        return rows;
    }

    /** Pham vi du lieu user duoc xem theo vai tro hien hanh. */
    private List<AccessRequest> permissionScope(UserSession session) {
        RoleCode role = session.getActiveRole();
        if (role == RoleCode.ADMIN) {
            return requestRepository.findAll();
        }

        Map<Long, AccessRequest> map = new LinkedHashMap<>();

        if (role == RoleCode.REQUESTER) {
            for (AccessRequest r : requestRepository
                    .findByRequesterUserIdOrderByCreatedAtDesc(session.getUserId())) {
                map.put(r.getId(), r);
            }
            // Phieu ma user la nguoi dung chung tren dong chi tiet.
            detailRepository.findByTargetUserId(session.getUserId()).forEach(d ->
                    requestRepository.findById(d.getRequestId())
                            .ifPresent(r -> map.put(r.getId(), r)));
            return new ArrayList<>(map.values());
        }

        // Vai tro xu ly: phieu dang o buoc cua minh + phieu da tung xu ly.
        if (role != null) {
            for (AccessRequest r : requestRepository.findByCurrentActorRole(role.name())) {
                map.put(r.getId(), r);
            }
        }
        for (WorkflowHistory h : historyRepository.findByActorUserId(session.getUserId())) {
            requestRepository.findById(h.getRequestId())
                    .ifPresent(r -> map.put(r.getId(), r));
        }
        return new ArrayList<>(map.values());
    }

    /** User co quyen xem chi tiet phieu nay khong (dung khi mo chi tiet truc tiep). */
    public boolean canView(AccessRequest r, UserSession session) {
        if (session.getActiveRole() == RoleCode.ADMIN) {
            return true;
        }
        return permissionScope(session).stream().anyMatch(x -> x.getId().equals(r.getId()));
    }

    private boolean matches(AccessRequest r, SearchCriteria c) {
        if (c == null) {
            return true;
        }
        if (notBlank(c.getStatus())) {
            if (r.getStatus() == null || r.getStatus() != RequestStatus.valueOf(c.getStatus())) {
                return false;
            }
        }
        if (notBlank(c.getRequestType())) {
            if (r.getRequestType() == null || r.getRequestType() != RequestType.valueOf(c.getRequestType())) {
                return false;
            }
        }
        if (c.getUnitId() != null && !c.getUnitId().equals(r.getRequesterUnitId())) {
            return false;
        }
        if (c.getSystemId() != null && !c.getSystemId().equals(r.getSystemId())) {
            return false;
        }
        if (c.getDatabaseId() != null && !c.getDatabaseId().equals(r.getDatabaseId())) {
            return false;
        }
        if (notBlank(c.getRequestCode())) {
            if (r.getRequestCode() == null
                    || !r.getRequestCode().toLowerCase().contains(c.getRequestCode().toLowerCase())) {
                return false;
            }
        }
        if (notBlank(c.getFromDate()) && r.getCreatedAt() != null) {
            LocalDate from = LocalDate.parse(c.getFromDate());
            if (r.getCreatedAt().toLocalDate().isBefore(from)) {
                return false;
            }
        }
        if (notBlank(c.getToDate()) && r.getCreatedAt() != null) {
            LocalDate to = LocalDate.parse(c.getToDate());
            if (r.getCreatedAt().toLocalDate().isAfter(to)) {
                return false;
            }
        }
        return true;
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
