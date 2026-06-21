package com.csdl.access.domain.repo;

import com.csdl.access.domain.DatabaseCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatabaseCatalogRepository extends JpaRepository<DatabaseCatalog, Long> {
    List<DatabaseCatalog> findBySystemId(Long systemId);
    List<DatabaseCatalog> findBySystemIdAndActiveTrue(Long systemId);
    List<DatabaseCatalog> findByActiveTrue();
}
