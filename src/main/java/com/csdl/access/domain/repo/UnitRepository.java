package com.csdl.access.domain.repo;

import com.csdl.access.domain.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findByCode(String code);
    List<Unit> findByActiveTrue();
}
