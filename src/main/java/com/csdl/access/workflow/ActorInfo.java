package com.csdl.access.workflow;

import com.csdl.access.common.enums.ActorType;

/**
 * Ket qua tra ve cua resolveNextActor: xac dinh nguoi xu ly buoc tiep theo.
 *
 * actorType — ROLE (ca nhan theo vai tro) hoac TEAM (nhom xu ly chung).
 * actorId   — user_id cua nguoi xu ly, null neu TEAM.
 * actorRole — ma vai tro (DEPT_MANAGER, AUTHORITY, CHECKER, ACCESS_TEAM, DBA, EXECUTOR).
 * unitId    — don vi chiu trach nhiem xu ly.
 */
public record ActorInfo(
        ActorType actorType,
        Long actorId,
        String actorRole,
        Long unitId
) {
    /** Tao ActorInfo cho role-based actor (ca nhan). */
    public static ActorInfo ofRole(Long actorId, String actorRole, Long unitId) {
        return new ActorInfo(ActorType.ROLE, actorId, actorRole, unitId);
    }

    /** Tao ActorInfo cho team-based actor (nhom xu ly). */
    public static ActorInfo ofTeam(String actorRole, Long unitId) {
        return new ActorInfo(ActorType.TEAM, null, actorRole, unitId);
    }
}
