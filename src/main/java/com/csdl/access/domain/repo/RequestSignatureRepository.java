package com.csdl.access.domain.repo;

import com.csdl.access.common.enums.SigningScope;
import com.csdl.access.domain.RequestSignature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestSignatureRepository extends JpaRepository<RequestSignature, Long> {
    List<RequestSignature> findByRequestId(Long requestId);
    List<RequestSignature> findByRequestIdAndSignerUserId(Long requestId, Long signerUserId);
    boolean existsByRequestIdAndSignerUserIdAndResult(Long requestId, Long signerUserId, String result);
    boolean existsByDetailIdAndResult(Long detailId, String result);
    List<RequestSignature> findByRequestIdAndDetailIdNotNullAndResult(Long requestId, String result);
    void deleteByRequestIdAndSigningScopeAndResult(Long requestId, SigningScope signingScope, String result);
}
