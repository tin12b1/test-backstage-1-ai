package com.csdl.access.workflow;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RoleCode;

/**
 * Mot buoc xu ly trong chuoi workflow.
 *
 * requesterPhase: true neu buoc thuoc giai doan don vi yeu cau,
 * false neu thuoc don vi chu quan ung dung/CSDL.
 */
public class WorkflowStep {

    private final String stepCode;
    private final RequestStatus status;
    private final RoleCode actorRole;
    private final boolean requesterPhase;
    private final UnitScope unitScope;

    /** Buoc xu ly gan voi don vi nao. */
    public enum UnitScope {
        REQUESTER_UNIT,
        OWNER_APP_UNIT,
        OWNER_DB_UNIT,
        ACCESS_TEAM
    }

    public WorkflowStep(String stepCode, RequestStatus status, RoleCode actorRole,
                        boolean requesterPhase, UnitScope unitScope) {
        this.stepCode = stepCode;
        this.status = status;
        this.actorRole = actorRole;
        this.requesterPhase = requesterPhase;
        this.unitScope = unitScope;
    }

    public String getStepCode() {
        return stepCode;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public RoleCode getActorRole() {
        return actorRole;
    }

    public boolean isRequesterPhase() {
        return requesterPhase;
    }

    public UnitScope getUnitScope() {
        return unitScope;
    }
}
