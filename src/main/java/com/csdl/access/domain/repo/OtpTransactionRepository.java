package com.csdl.access.domain.repo;

import com.csdl.access.domain.OtpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repository quan ly entity OtpTransaction (giao dich OTP). */
public interface OtpTransactionRepository extends JpaRepository<OtpTransaction, Long> {

    // Lay 500 giao dich OTP moi nhat, sap xep theo id giam dan
    List<OtpTransaction> findTop500ByOrderByIdDesc();
}
