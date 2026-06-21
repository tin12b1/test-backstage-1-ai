package com.csdl.access.request;

import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.domain.repo.EmergencyCompletionLinkRepository;
import com.csdl.access.domain.repo.RequestDetailRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kiem tra "no" phieu 05B-HTKC: nguoi dung co phieu 05A da hoan thanh xu ly
 * nhung chua lap/hoan thien phieu 05B tuong ung (business-rules.md, database-schema.md).
 */
@Service
public class EmergencyDebtService {

    private final AccessRequestRepository accessRequestRepository;
    private final EmergencyCompletionLinkRepository linkRepository;
    private final RequestDetailRepository detailRepository;

    public EmergencyDebtService(AccessRequestRepository accessRequestRepository,
                                EmergencyCompletionLinkRepository linkRepository,
                                RequestDetailRepository detailRepository) {
        this.accessRequestRepository = accessRequestRepository;
        this.linkRepository = linkRepository;
        this.detailRepository = detailRepository;
    }

    /** Danh sach phieu 05A cua user chua co 05B lien ket (dang no). */
    public List<AccessRequest> outstandingEmergencyRequests(Long userId) {
        List<AccessRequest> result = new ArrayList<>();
        for (AccessRequest r : accessRequestRepository
                .findByRequestTypeAndRequesterUserId(RequestType.YCKC_05A, userId)) {
            // Chi tinh la "no" khi phieu 05A da duoc xu ly (khong con draft/cancelled).
            if (r.getStatus() == RequestStatus.CANCELLED || r.getStatus() == RequestStatus.DRAFT) {
                continue;
            }
            if (linkRepository.findByEmergencyRequestId(r.getId()).isEmpty()) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * User co dang no phieu 05B khong. Tinh ca truong hop user la nguoi dung chung
     * tren cac dong chi tiet cua phieu 05A.
     */
    public boolean hasOutstandingDebt(Long userId) {
        if (!outstandingEmergencyRequests(userId).isEmpty()) {
            return true;
        }
        // Nguoi dung chung tren dong chi tiet cua phieu 05A chua hoan thien.
        Set<Long> emergencyIds = new HashSet<>();
        detailRepository.findByTargetUserId(userId)
                .forEach(d -> emergencyIds.add(d.getRequestId()));
        for (Long reqId : emergencyIds) {
            AccessRequest r = accessRequestRepository.findById(reqId).orElse(null);
            if (r == null || r.getRequestType() != RequestType.YCKC_05A) {
                continue;
            }
            if (r.getStatus() == RequestStatus.CANCELLED || r.getStatus() == RequestStatus.DRAFT) {
                continue;
            }
            if (linkRepository.findByEmergencyRequestId(reqId).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
