package com.csdl.access.domain.repo;

import com.csdl.access.domain.AccessRightCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessRightCatalogRepository extends JpaRepository<AccessRightCatalog, Long> {
    List<AccessRightCatalog> findByActiveTrue();
}
