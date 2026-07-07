package com.csdl.access.domain.repo;

import com.csdl.access.domain.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {

    List<WorkShift> findByActiveTrueOrderByShiftNo();
}
