package com.csdl.access.workflow;

import com.csdl.access.common.enums.ActorType;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.workflow.WorkflowStep.UnitScope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Engine xac dinh buoc xu ly tiep theo cua phieu (ADR 0002).
 * Khong chuyen trang thai truc tiep o controller; phai di qua service nay.
 */
@Service
public class WorkflowService {

    private final WorkflowDefinition definition;

    public WorkflowService(WorkflowDefinition definition) {
        this.definition = definition;
    }

    /** Nguoi yeu cau khong thuoc don vi chu quan ung dung. */
    public boolean isCrossUnit(AccessRequest r) {
        return r.getOwnerUnitId() != null
                && r.getRequesterUnitId() != null
                && !r.getOwnerUnitId().equals(r.getRequesterUnitId());
    }

    public List<WorkflowStep> chainFor(AccessRequest r) {
        return definition.buildChain(r.getRequestType(), isCrossUnit(r));
    }

    /** Bat dau workflow: dat phieu vao buoc dau tien. */
    public void start(AccessRequest r) {
        List<WorkflowStep> chain = chainFor(r);
        applyStep(r, chain.get(0));
        r.setSubmittedAt(LocalDateTime.now());
    }

    private int currentIndex(AccessRequest r, List<WorkflowStep> chain) {
        for (int i = 0; i < chain.size(); i++) {
            if (chain.get(i).getStepCode().equals(r.getCurrentStepCode())) {
                return i;
            }
        }
        return -1;
    }

    /** Buoc hien tai co phai buoc thuc hien cuoi cung (can execute) khong. */
    public boolean isExecutionStep(AccessRequest r) {
        List<WorkflowStep> chain = chainFor(r);
        int i = currentIndex(r, chain);
        return i >= 0 && i == chain.size() - 1;
    }

    /** Vai tro dang duoc giao xu ly buoc hien tai. */
    public boolean isCurrentActor(AccessRequest r, RoleCode role) {
        return role != null && role.name().equals(r.getCurrentActorRole());
    }

    /**
     * Ky duyet va chuyen buoc tiep theo. Chi dung cho cac buoc truoc buoc thuc hien.
     * Tra ve true neu chuyen thanh cong.
     */
    public boolean advance(AccessRequest r) {
        List<WorkflowStep> chain = chainFor(r);
        int i = currentIndex(r, chain);
        if (i < 0 || i >= chain.size() - 1) {
            return false;
        }
        WorkflowStep next = chain.get(i + 1);
        applyStep(r, next);
        // Danh dau da phe duyet khi buoc ke tiep la buoc thuc hien cuoi cung.
        if (i + 1 == chain.size() - 1) {
            r.setApprovedAt(LocalDateTime.now());
        }
        return true;
    }

    /** Hoan thanh phieu sau khi bo phan thuc hien xac nhan. */
    public void complete(AccessRequest r) {
        r.setStatus(RequestStatus.COMPLETED);
        r.setCompletedAt(LocalDateTime.now());
        r.setCurrentActorType(null);
        r.setCurrentActorRole(null);
        r.setCurrentActorId(null);
        r.setCurrentStepCode(null);
    }

    /** Chuyen trang thai khi dang thuc hien (mo truy cap/chay script). */
    public void markInProgress(AccessRequest r) {
        r.setStatus(RequestStatus.IN_PROGRESS);
    }

    private void applyStep(AccessRequest r, WorkflowStep step) {
        r.setStatus(step.getStatus());
        r.setCurrentActorRole(step.getActorRole().name());
        r.setCurrentActorType(ActorType.ROLE);
        r.setCurrentStepCode(step.getStepCode());
        r.setAtRequesterPhase(step.isRequesterPhase());
        r.setCurrentUnitId(resolveUnit(r, step.getUnitScope()));
    }

    private Long resolveUnit(AccessRequest r, UnitScope scope) {
        switch (scope) {
            case REQUESTER_UNIT:
                return r.getRequesterUnitId();
            case OWNER_APP_UNIT:
                return r.getOwnerUnitId();
            case OWNER_DB_UNIT:
                return r.getOwnerDbUnitId();
            default:
                return r.getOwnerUnitId();
        }
    }
}
