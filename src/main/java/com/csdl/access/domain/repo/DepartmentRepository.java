package com.csdl.access.domain.repo;

import com.csdl.access.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByUnitId(Long unitId);
    List<Department> findByActiveTrue();
}
