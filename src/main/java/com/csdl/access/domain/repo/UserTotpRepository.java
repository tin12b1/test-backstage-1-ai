package com.csdl.access.domain.repo;

import com.csdl.access.domain.UserTotp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository quan ly entity UserTotp (cau hinh TOTP cua nguoi dung). */
public interface UserTotpRepository extends JpaRepository<UserTotp, Long> {
    // Tim cau hinh TOTP theo nguoi dung
    Optional<UserTotp> findByUserId(Long userId);
}
