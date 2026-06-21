package com.csdl.access.workflow;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.workflow.WorkflowStep.UnitScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Dinh nghia chuoi buoc xu ly cho tung loai phieu (features/approval-processing.md).
 *
 * crossUnit = nguoi yeu cau KHONG thuoc don vi chu quan ung dung.
 */
@Component
public class WorkflowDefinition {

    public List<WorkflowStep> buildChain(RequestType type, boolean crossUnit) {
        switch (type) {
            case YCTC_01:
                return chain01(crossUnit);
            case YCCS_02:
                return chain02(crossUnit);
            case YCCT_03:
                return chain03();
            case YCTK_04A:
                return chain04A(crossUnit);
            case YCKC_05A:
                return chain05A();
            case HTKC_05B:
                return chain05B(crossUnit);
            default:
                throw new IllegalArgumentException("Khong ho tro loai yeu cau: " + type);
        }
    }

    private void requesterApproval(List<WorkflowStep> steps) {
        steps.add(new WorkflowStep("REQ_DEPT", RequestStatus.PENDING_DEPT_APPROVAL,
                RoleCode.DEPT_MANAGER, true, UnitScope.REQUESTER_UNIT));
        steps.add(new WorkflowStep("REQ_AUTH", RequestStatus.PENDING_AUTHORITY_APPROVAL,
                RoleCode.AUTHORITY, true, UnitScope.REQUESTER_UNIT));
    }

    private void ownerAppApproval(List<WorkflowStep> steps) {
        steps.add(new WorkflowStep("OWNER_DEPT", RequestStatus.PENDING_DEPT_APPROVAL,
                RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
        steps.add(new WorkflowStep("OWNER_AUTH", RequestStatus.PENDING_AUTHORITY_APPROVAL,
                RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
    }

    private void dbUnitApproval(List<WorkflowStep> steps) {
        steps.add(new WorkflowStep("DB_DEPT", RequestStatus.PENDING_DEPT_APPROVAL,
                RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_DB_UNIT));
        steps.add(new WorkflowStep("DB_AUTH", RequestStatus.PENDING_AUTHORITY_APPROVAL,
                RoleCode.AUTHORITY, false, UnitScope.OWNER_DB_UNIT));
    }

    // 01-YCTC
    private List<WorkflowStep> chain01(boolean crossUnit) {
        List<WorkflowStep> s = new ArrayList<>();
        if (crossUnit) {
            requesterApproval(s);
        }
        ownerAppApproval(s);
        s.add(new WorkflowStep("ACCESS", RequestStatus.SENT_TO_ACCESS_TEAM,
                RoleCode.ACCESS_TEAM, false, UnitScope.ACCESS_TEAM));
        return s;
    }

    // 02-YCCS
    private List<WorkflowStep> chain02(boolean crossUnit) {
        List<WorkflowStep> s = new ArrayList<>();
        if (crossUnit) {
            requesterApproval(s);
        }
        s.add(new WorkflowStep("CHECK", RequestStatus.PENDING_CHECK,
                RoleCode.CHECKER, false, UnitScope.OWNER_APP_UNIT));
        ownerAppApproval(s);
        s.add(new WorkflowStep("EXEC", RequestStatus.PENDING_EXECUTION,
                RoleCode.EXECUTOR, false, UnitScope.OWNER_APP_UNIT));
        return s;
    }

    // 03-YCCT (chi don vi chu quan ung dung)
    private List<WorkflowStep> chain03() {
        List<WorkflowStep> s = new ArrayList<>();
        ownerAppApproval(s);
        s.add(new WorkflowStep("DBA_SIGN", RequestStatus.PENDING_DBA,
                RoleCode.DBA, false, UnitScope.OWNER_DB_UNIT));
        dbUnitApproval(s);
        s.add(new WorkflowStep("DBA_EXEC", RequestStatus.PENDING_DBA,
                RoleCode.DBA, false, UnitScope.OWNER_DB_UNIT));
        return s;
    }

    // 04A-YCTK
    private List<WorkflowStep> chain04A(boolean crossUnit) {
        List<WorkflowStep> s = new ArrayList<>();
        if (crossUnit) {
            requesterApproval(s);
        }
        ownerAppApproval(s);
        dbUnitApproval(s);
        s.add(new WorkflowStep("DBA_EXEC", RequestStatus.PENDING_DBA,
                RoleCode.DBA, false, UnitScope.OWNER_DB_UNIT));
        return s;
    }

    // 05A-YCKC (chuyen thang bo phan mo truy cap)
    private List<WorkflowStep> chain05A() {
        List<WorkflowStep> s = new ArrayList<>();
        s.add(new WorkflowStep("ACCESS", RequestStatus.SENT_TO_ACCESS_TEAM,
                RoleCode.ACCESS_TEAM, false, UnitScope.ACCESS_TEAM));
        return s;
    }

    // 05B-HTKC
    private List<WorkflowStep> chain05B(boolean crossUnit) {
        List<WorkflowStep> s = new ArrayList<>();
        if (crossUnit) {
            requesterApproval(s);
        }
        s.add(new WorkflowStep("CHECK", RequestStatus.PENDING_CHECK,
                RoleCode.CHECKER, false, UnitScope.OWNER_APP_UNIT));
        ownerAppApproval(s);
        s.add(new WorkflowStep("ACCESS", RequestStatus.SENT_TO_ACCESS_TEAM,
                RoleCode.ACCESS_TEAM, false, UnitScope.ACCESS_TEAM));
        return s;
    }
}
