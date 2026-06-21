package com.csdl.access.dashboard;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.common.lookup.RequestRow;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.request.EmergencyDebtService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tinh chi so va danh sach cong viec theo nhom vai tro (features/dashboard.md).
 * Du lieu chi trong pham vi phan quyen cua active role.
 */
@Service
public class DashboardService {

    private static final Set<RequestStatus> PENDING_APPROVAL = EnumSet.of(
            RequestStatus.PENDING_CHECK, RequestStatus.PENDING_DEPT_APPROVAL,
            RequestStatus.PENDING_AUTHORITY_APPROVAL, RequestStatus.PENDING_OWNER_UNIT);

    private static final Set<RequestStatus> IN_PROCESSING = EnumSet.of(
            RequestStatus.SENT_TO_ACCESS_TEAM, RequestStatus.PENDING_DBA,
            RequestStatus.PENDING_EXECUTION, RequestStatus.IN_PROGRESS);

    private final AccessRequestRepository requestRepository;
    private final EmergencyDebtService debtService;
    private final LookupService lookupService;

    public DashboardService(AccessRequestRepository requestRepository,
                            EmergencyDebtService debtService,
                            LookupService lookupService) {
        this.requestRepository = requestRepository;
        this.debtService = debtService;
        this.lookupService = lookupService;
    }

    public DashboardView build(UserSession session) {
        RoleCode role = session.getActiveRole();
        if (role == RoleCode.REQUESTER || role == RoleCode.ADMIN) {
            return requesterDashboard(session);
        }
        if (role == RoleCode.DEPT_MANAGER || role == RoleCode.AUTHORITY || role == RoleCode.CHECKER) {
            return approverDashboard(session, role);
        }
        return processingDashboard(session, role);
    }

    private DashboardView requesterDashboard(UserSession session) {
        DashboardView view = new DashboardView();
        view.setGroupTitle("Dashboard nguoi lap yeu cau");

        List<AccessRequest> mine = requestRepository
                .findByRequesterUserIdOrderByCreatedAtDesc(session.getUserId());

        long pending = mine.stream().filter(r -> PENDING_APPROVAL.contains(r.getStatus())).count();
        long processing = mine.stream().filter(r -> IN_PROCESSING.contains(r.getStatus())).count();
        long completed = mine.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count();
        long returned = mine.stream().filter(r -> r.getStatus() == RequestStatus.RETURNED).count();
        long approved = mine.stream().filter(r -> r.getApprovedAt() != null
                && r.getStatus() != RequestStatus.COMPLETED).count();

        view.getCounters().put("Tong so yeu cau", (long) mine.size());
        view.getCounters().put("Cho phe duyet", pending);
        view.getCounters().put("Da phe duyet", approved);
        view.getCounters().put("Cho mo truy cap/thuc hien", processing);
        view.getCounters().put("Da hoan thanh", completed);
        view.getCounters().put("Bi chuyen tra", returned);
        view.getCounters().put("No phieu 05B-HTKC",
                (long) debtService.outstandingEmergencyRequests(session.getUserId()).size());

        view.setPrimaryTitle("Yeu cau bi chuyen tra (can xu ly lai)");
        view.setPrimaryList(toRows(mine.stream()
                .filter(r -> r.getStatus() == RequestStatus.RETURNED)
                .collect(Collectors.toList())));

        view.setSecondaryTitle("Yeu cau gan day");
        view.setSecondaryList(toRows(mine.stream().limit(10).collect(Collectors.toList())));
        return view;
    }

    private DashboardView approverDashboard(UserSession session, RoleCode role) {
        DashboardView view = new DashboardView();
        view.setGroupTitle("Dashboard " + role.getDisplayName());

        List<AccessRequest> pending = pendingForRole(role);
        view.getCounters().put("Cho toi xu ly", (long) pending.size());
        view.getCounters().put("Khan cap (05A) cho xu ly", pending.stream()
                .filter(r -> r.getRequestType() != null
                        && r.getRequestType().name().equals("YCKC_05A")).count());

        view.setPrimaryTitle("Yeu cau cho xu ly");
        view.setPrimaryList(toRows(pending));
        return view;
    }

    private DashboardView processingDashboard(UserSession session, RoleCode role) {
        DashboardView view = new DashboardView();
        view.setGroupTitle("Dashboard " + role.getDisplayName());

        List<AccessRequest> pending = pendingForRole(role);
        long inProgress = pending.stream()
                .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS).count();

        view.getCounters().put("Cho cap quyen/xac nhan", (long) pending.size());
        view.getCounters().put("Dang thuc hien", inProgress);

        view.setPrimaryTitle("Yeu cau cho cap quyen/thuc hien");
        view.setPrimaryList(toRows(pending));
        return view;
    }

    private List<AccessRequest> pendingForRole(RoleCode role) {
        List<AccessRequest> result = new ArrayList<>();
        for (AccessRequest r : requestRepository.findByCurrentActorRole(role.name())) {
            if (r.getStatus() != RequestStatus.COMPLETED
                    && r.getStatus() != RequestStatus.CANCELLED
                    && r.getStatus() != RequestStatus.RETURNED) {
                result.add(r);
            }
        }
        return result;
    }

    private List<RequestRow> toRows(List<AccessRequest> list) {
        List<RequestRow> rows = new ArrayList<>();
        for (AccessRequest r : list) {
            rows.add(lookupService.toRow(r));
        }
        return rows;
    }
}
