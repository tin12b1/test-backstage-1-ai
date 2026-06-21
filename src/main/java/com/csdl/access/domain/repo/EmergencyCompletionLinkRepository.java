package com.csdl.access.domain.repo;

import com.csdl.access.domain.EmergencyCompletionLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmergencyCompletionLinkRepository extends JpaRepository<EmergencyCompletionLink, Long> {
    Optional<EmergencyCompletionLink> findByEmergencyRequestId(Long emergencyRequestId);
    Optional<EmergencyCompletionLink> findByCompletionRequestId(Long completionRequestId);
}
