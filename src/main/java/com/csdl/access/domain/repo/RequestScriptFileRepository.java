package com.csdl.access.domain.repo;

import com.csdl.access.domain.RequestScriptFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestScriptFileRepository extends JpaRepository<RequestScriptFile, Long> {
    List<RequestScriptFile> findByRequestId(Long requestId);
}
