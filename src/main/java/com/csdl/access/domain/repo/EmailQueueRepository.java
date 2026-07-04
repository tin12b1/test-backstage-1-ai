package com.csdl.access.domain.repo;

import com.csdl.access.domain.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repository quan ly entity EmailQueue (hang doi gui email). */
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {
    // Tim email theo trang thai gui
    List<EmailQueue> findByStatus(String status);
    // Tim email theo yeu cau lien quan
    List<EmailQueue> findByRequestId(Long requestId);
    // Lay 500 email moi nhat, sap xep theo id giam dan
    List<EmailQueue> findTop500ByOrderByIdDesc();
}
