package com.csdl.access.domain.repo;

import com.csdl.access.domain.InformationSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InformationSystemRepository extends JpaRepository<InformationSystem, Long> {
    Optional<InformationSystem> findByCode(String code);
    List<InformationSystem> findByActiveTrue();
}
