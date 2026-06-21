package com.csdl.access.domain.repo;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.domain.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AccessRequestRepository
        extends JpaRepository<AccessRequest, Long>, JpaSpecificationExecutor<AccessRequest> {

    List<AccessRequest> findByRequesterUserIdOrderByCreatedAtDesc(Long requesterUserId);

    List<AccessRequest> findByRequesterUserIdAndStatus(Long requesterUserId, RequestStatus status);

    long countByRequesterUserIdAndStatus(Long requesterUserId, RequestStatus status);

    long countByRequesterUserId(Long requesterUserId);

    List<AccessRequest> findByStatusIn(List<RequestStatus> statuses);

    List<AccessRequest> findByCurrentActorRoleAndStatusIn(String currentActorRole, List<RequestStatus> statuses);

    List<AccessRequest> findByCurrentActorRole(String currentActorRole);

    long countByRequestCodeStartingWith(String prefix);

    /** Phieu 05A chua duoc lien ket voi 05B (phuc vu chon trong 05B-HTKC). */
    List<AccessRequest> findByRequestTypeAndRequesterUserId(RequestType requestType, Long requesterUserId);
}
