package com.csdl.access.domain.repo;

import com.csdl.access.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserIdAndActiveTrue(Long userId);
    List<UserRole> findByUserId(Long userId);
    List<UserRole> findByRoleIdAndActiveTrue(Long roleId);
}
