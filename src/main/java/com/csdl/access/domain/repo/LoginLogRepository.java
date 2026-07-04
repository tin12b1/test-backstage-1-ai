package com.csdl.access.domain.repo;

import com.csdl.access.domain.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repository quan ly entity LoginLog (nhat ky dang nhap). */
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    // Lay 500 ban ghi dang nhap moi nhat, sap xep theo id giam dan
    List<LoginLog> findTop500ByOrderByIdDesc();
}
