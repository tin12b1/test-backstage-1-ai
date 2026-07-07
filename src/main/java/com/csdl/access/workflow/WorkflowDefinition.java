package com.csdl.access.workflow;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.workflow.WorkflowStep.UnitScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Dinh nghia chuoi buoc xu ly cho tung loai phieu (workflow-step-codes.md).
 *
 * Step code format: {MA_MAU}_{VARIANT}_{SO_THU_TU} hoac {MA_MAU}_{SO_THU_TU} (neu khong co variant).
 * crossUnit = nguoi yeu cau KHONG thuoc don vi chu quan ung dung (variant External).
 */
@Component
public class WorkflowDefinition {

    /**
     * Lay ma type code cho step code.
     * VD: YCTC_01 -> "01", YCCS_02 -> "02", YCCT_03 -> "03", YCTK_04A -> "04A", etc.
     */
    public String typeCode(RequestType type) {
        switch (type) {
            case YCTC_01:  return "01";
            case YCCS_02:  return "02";
            case YCCT_03:  return "03";
            case YCTK_04A: return "04A";
            case BGTK_04B: return "04B";
            case YCKC_05A: return "05A";
            case HTKC_05B: return "05B";
            default:
                throw new IllegalArgumentException("Khong ho tro loai yeu cau: " + type);
        }
    }

    /**
     * Xac dinh xem loai phieu co variant (I/E) hay khong.
     * 03-YCCT, 04B-BGTK va 05A-YCKC khong co variant.
     */
    public boolean hasVariant(RequestType type) {
        return type != RequestType.YCCT_03
                && type != RequestType.BGTK_04B
                && type != RequestType.YCKC_05A;
    }

    /**
     * Xac dinh variant ky tu: "I" (Internal) hoac "E" (External).
     * Tra ve null neu loai phieu khong co variant.
     */
    public String variantCode(RequestType type, boolean crossUnit) {
        if (!hasVariant(type)) {
            return null;
        }
        return crossUnit ? "E" : "I";
    }

    /**
     * Tao step code tu cac thanh phan.
     * Format: {TYPE}_{VARIANT}_{SEQ} hoac {TYPE}_{SEQ}
     */
    public String buildStepCode(String typeCode, String variant, int sequence) {
        String seq = String.format("%02d", sequence);
        if (variant == null) {
            return typeCode + "_" + seq;
        }
        return typeCode + "_" + variant + "_" + seq;
    }

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
            case BGTK_04B:
                return chain04B();
            case YCKC_05A:
                return chain05A();
            case HTKC_05B:
                return chain05B(crossUnit);
            default:
                throw new IllegalArgumentException("Khong ho tro loai yeu cau: " + type);
        }
    }

    // 01-YCTC Internal: 01_I_01 (DEPT_MANAGER), 01_I_02 (AUTHORITY), 01_I_03 (ACCESS_TEAM)
    // 01-YCTC External: 01_E_01..02 (Requester), 01_E_03..04 (Owner), 01_E_05 (ACCESS_TEAM)
    private List<WorkflowStep> chain01(boolean crossUnit) {
        List<WorkflowStep> s = new ArrayList<>();
        String tc = "01";
        if (crossUnit) {
            String v = "E";
            s.add(new WorkflowStep(buildStepCode(tc, v, 1), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, true, UnitScope.REQUESTER_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 2), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, true, UnitScope.REQUESTER_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 3), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 4), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 5), RequestStatus.SENT_TO_ACCESS_TEAM,
                    RoleCode.ACCESS_TEAM, false, UnitScope.OWNER_APP_UNIT));
        } else {
            String v = "I";
            s.add(new WorkflowStep(buildStepCode(tc, v, 1), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 2), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 3), RequestStatus.SENT_TO_ACCESS_TEAM,
                    RoleCode.ACCESS_TEAM, false, UnitScope.OWNER_APP_UNIT));
        }
        return s;
    }

    // 02-YCCS Internal: 02_I_01 (CHECKER), 02_I_02 (DEPT_MANAGER), 02_I_03 (AUTHORITY), 02_I_04 (EXECUTOR)
    // 02-YCCS External: 02_E_01..02 (Requester), 02_E_03 (CHECKER), 02_E_04..05 (Owner), 02_E_06 (EXECUTOR)
    private List<WorkflowStep> chain02(boolean crossUnit) {
        List<WorkflowStep> s = new ArrayList<>();
        String tc = "02";
        if (crossUnit) {
            String v = "E";
            s.add(new WorkflowStep(buildStepCode(tc, v, 1), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, true, UnitScope.REQUESTER_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 2), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, true, UnitScope.REQUESTER_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 3), RequestStatus.PENDING_CHECK,
                    RoleCode.CHECKER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 4), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 5), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 6), RequestStatus.PENDING_EXECUTION,
                    RoleCode.EXECUTOR, false, UnitScope.OWNER_APP_UNIT));
        } else {
            String v = "I";
            s.add(new WorkflowStep(buildStepCode(tc, v, 1), RequestStatus.PENDING_CHECK,
                    RoleCode.CHECKER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 2), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 3), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 4), RequestStatus.PENDING_EXECUTION,
                    RoleCode.EXECUTOR, false, UnitScope.OWNER_APP_UNIT));
        }
        return s;
    }

    // 03-YCCT (khong co variant): 03_01..03_06
    private List<WorkflowStep> chain03() {
        List<WorkflowStep> s = new ArrayList<>();
        String tc = "03";
        s.add(new WorkflowStep(buildStepCode(tc, null, 1), RequestStatus.PENDING_DEPT_APPROVAL,
                RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
        s.add(new WorkflowStep(buildStepCode(tc, null, 2), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
        s.add(new WorkflowStep(buildStepCode(tc, null, 3), RequestStatus.PENDING_DBA,
                RoleCode.DBA, false, UnitScope.OWNER_DB_UNIT));
        s.add(new WorkflowStep(buildStepCode(tc, null, 4), RequestStatus.PENDING_DEPT_APPROVAL,
                RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_DB_UNIT));
        s.add(new WorkflowStep(buildStepCode(tc, null, 5), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                RoleCode.AUTHORITY, false, UnitScope.OWNER_DB_UNIT));
        s.add(new WorkflowStep(buildStepCode(tc, null, 6), RequestStatus.PENDING_DBA,
                RoleCode.DBA, false, UnitScope.OWNER_DB_UNIT));
        return s;
    }

    // 04A-YCTK Internal: 04A_I_01..05
    // 04A-YCTK External: 04A_E_01..07
    private List<WorkflowStep> chain04A(boolean crossUnit) {
        List<WorkflowStep> s = new ArrayList<>();
        String tc = "04A";
        if (crossUnit) {
            String v = "E";
            s.add(new WorkflowStep(buildStepCode(tc, v, 1), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, true, UnitScope.REQUESTER_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 2), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, true, UnitScope.REQUESTER_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 3), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 4), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 5), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_DB_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 6), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_DB_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 7), RequestStatus.PENDING_DBA,
                    RoleCode.DBA, false, UnitScope.OWNER_DB_UNIT));
        } else {
            String v = "I";
            s.add(new WorkflowStep(buildStepCode(tc, v, 1), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 2), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 3), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_DB_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 4), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_DB_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 5), RequestStatus.PENDING_DBA,
                    RoleCode.DBA, false, UnitScope.OWNER_DB_UNIT));
        }
        return s;
    }

    // 04B-BGTK (khong co variant): 04B_01, 04B_02
    // Giai doan 1: DBA gui -> Lanh dao phong DBA duyet
    // (Giai doan cho ky nhan PENDING_RECEIPT xu ly ngoai workflow chain)
    // Giai doan 2: Lanh dao phong nguoi nhan duyet
    private List<WorkflowStep> chain04B() {
        List<WorkflowStep> s = new ArrayList<>();
        String tc = "04B";
        s.add(new WorkflowStep(buildStepCode(tc, null, 1), RequestStatus.PENDING_DEPT_APPROVAL,
                RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_DB_UNIT));
        s.add(new WorkflowStep(buildStepCode(tc, null, 2), RequestStatus.PENDING_DEPT_APPROVAL,
                RoleCode.DEPT_MANAGER, false, UnitScope.REQUESTER_UNIT));
        return s;
    }

    // 05A-YCKC (khong co variant): 05A_01
    private List<WorkflowStep> chain05A() {
        List<WorkflowStep> s = new ArrayList<>();
        s.add(new WorkflowStep(buildStepCode("05A", null, 1), RequestStatus.SENT_TO_ACCESS_TEAM,
                RoleCode.ACCESS_TEAM, false, UnitScope.OWNER_APP_UNIT));
        return s;
    }

    // 05B-HTKC Internal: 05B_I_01..04
    // 05B-HTKC External: 05B_E_01..06
    private List<WorkflowStep> chain05B(boolean crossUnit) {
        List<WorkflowStep> s = new ArrayList<>();
        String tc = "05B";
        if (crossUnit) {
            String v = "E";
            s.add(new WorkflowStep(buildStepCode(tc, v, 1), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, true, UnitScope.REQUESTER_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 2), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, true, UnitScope.REQUESTER_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 3), RequestStatus.PENDING_CHECK,
                    RoleCode.CHECKER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 4), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 5), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 6), RequestStatus.SENT_TO_ACCESS_TEAM,
                    RoleCode.ACCESS_TEAM, false, UnitScope.OWNER_APP_UNIT));
        } else {
            String v = "I";
            s.add(new WorkflowStep(buildStepCode(tc, v, 1), RequestStatus.PENDING_CHECK,
                    RoleCode.CHECKER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 2), RequestStatus.PENDING_DEPT_APPROVAL,
                    RoleCode.DEPT_MANAGER, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 3), RequestStatus.PENDING_AUTHORITY_APPROVAL,
                    RoleCode.AUTHORITY, false, UnitScope.OWNER_APP_UNIT));
            s.add(new WorkflowStep(buildStepCode(tc, v, 4), RequestStatus.SENT_TO_ACCESS_TEAM,
                    RoleCode.ACCESS_TEAM, false, UnitScope.OWNER_APP_UNIT));
        }
        return s;
    }
}
