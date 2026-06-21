package com.csdl.access.common.lookup;

import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.AccessRightCatalog;
import com.csdl.access.domain.AppUser;
import com.csdl.access.domain.DatabaseCatalog;
import com.csdl.access.domain.InformationSystem;
import com.csdl.access.domain.Unit;
import com.csdl.access.domain.repo.AccessRightCatalogRepository;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.DatabaseCatalogRepository;
import com.csdl.access.domain.repo.InformationSystemRepository;
import com.csdl.access.domain.repo.UnitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Tra cuu ten hien thi tu id (nguoi dung, don vi, he thong, CSDL) phuc vu view.
 */
@Service
public class LookupService {

    private final AppUserRepository appUserRepository;
    private final UnitRepository unitRepository;
    private final InformationSystemRepository systemRepository;
    private final DatabaseCatalogRepository databaseRepository;
    private final AccessRightCatalogRepository rightRepository;

    public LookupService(AppUserRepository appUserRepository,
                         UnitRepository unitRepository,
                         InformationSystemRepository systemRepository,
                         DatabaseCatalogRepository databaseRepository,
                         AccessRightCatalogRepository rightRepository) {
        this.appUserRepository = appUserRepository;
        this.unitRepository = unitRepository;
        this.systemRepository = systemRepository;
        this.databaseRepository = databaseRepository;
        this.rightRepository = rightRepository;
    }

    // ===== Danh muc cho dropdown =====
    public List<InformationSystem> activeSystems() {
        return systemRepository.findByActiveTrue();
    }

    public List<DatabaseCatalog> activeDatabases() {
        return databaseRepository.findByActiveTrue();
    }

    public List<AccessRightCatalog> activeRights() {
        return rightRepository.findByActiveTrue();
    }

    public List<AppUser> allUsers() {
        return appUserRepository.findAll();
    }

    public List<Unit> activeUnits() {
        return unitRepository.findByActiveTrue();
    }

    public String userName(Long userId) {
        if (userId == null) {
            return "";
        }
        return appUserRepository.findById(userId).map(u -> u.getFullName()).orElse("");
    }

    public String unitName(Long unitId) {
        if (unitId == null) {
            return "";
        }
        return unitRepository.findById(unitId).map(u -> u.getName()).orElse("");
    }

    public String systemName(Long systemId) {
        if (systemId == null) {
            return "";
        }
        return systemRepository.findById(systemId).map(s -> s.getName()).orElse("");
    }

    public String databaseName(Long databaseId) {
        if (databaseId == null) {
            return "";
        }
        return databaseRepository.findById(databaseId).map(d -> d.getName()).orElse("");
    }

    /** Tao dong hien thi tom tat cho danh sach yeu cau. */
    public RequestRow toRow(AccessRequest r) {
        RequestRow row = new RequestRow();
        row.setId(r.getId());
        row.setRequestCode(r.getRequestCode());
        row.setRequestType(r.getRequestType() != null ? r.getRequestType().getDisplayName() : "");
        row.setRequesterName(userName(r.getRequesterUserId()));
        row.setRequesterUnit(unitName(r.getRequesterUnitId()));
        row.setSystemName(systemName(r.getSystemId()));
        row.setDatabaseName(databaseName(r.getDatabaseId()));
        row.setStatus(r.getStatus());
        row.setStatusLabel(r.getStatus() != null ? r.getStatus().getDisplayName() : "");
        row.setCurrentActorRole(r.getCurrentActorRole());
        row.setStartTime(r.getStartTime());
        row.setEndTime(r.getEndTime());
        row.setSubmittedAt(r.getSubmittedAt());
        row.setCreatedAt(r.getCreatedAt());
        return row;
    }
}
