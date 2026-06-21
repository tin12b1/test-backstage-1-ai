package com.csdl.access.domain.repo;

import com.csdl.access.domain.OtpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpTransactionRepository extends JpaRepository<OtpTransaction, Long> {
}
