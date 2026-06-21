package com.csdl.access.domain.repo;

import com.csdl.access.domain.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {
    List<EmailQueue> findByStatus(String status);
    List<EmailQueue> findByRequestId(Long requestId);
}
