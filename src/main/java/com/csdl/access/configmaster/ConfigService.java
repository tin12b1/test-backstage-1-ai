package com.csdl.access.configmaster;

import com.csdl.access.common.audit.AuditService;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.*;
import com.csdl.access.domain.repo.*;
import com.csdl.access.integration.ad.AdClient;
import com.csdl.access.integration.ad.AdUserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cau hinh danh muc nen tang: nguoi dung, don vi, he thong, CSDL, vai tro
 * (features/configuration.md). Khong xoa cung danh muc da phat sinh giao dich,
 * chi khoa/ngung hieu luc.
 */
@Service
public class ConfigService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UnitRepository unitRepository;
    private final InformationSystemRepository systemRepository;
    private final DatabaseCatalogRepository databaseRepository;
    private final AccessRightCatalogRepository rightRepository;
    private final AdClient adClient;
    private final AuditService auditService;

    public ConfigService(AppUserRepository userRepository,
                         RoleRepository roleRepository,
                         UserRoleRepository userRoleRepository,
                         UnitRepository unitRepository,
                         InformationSystemRepository systemRepository,
                         DatabaseCatalogRepository databaseRepository,
                         AccessRightCatalogRepository rightRepository,
                         AdClient adClient,
                         AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.unitRepository = unitRepository;
        this.systemRepository = systemRepository;
        this.databaseRepository = databaseRepository;
        this.rightRepository = rightRepository;
        this.adClient = adClient;
        this.auditService = auditService;
    }

    // ===== Users =====
    public List<AppUser> listUsers() {
        return userRepository.findAll();
    }

    public List<UserRole> rolesOf(Long userId) {
        return userRoleRepository.findByUserId(userId);
    }

    /** Map userId -> danh sach ma vai tro (phuc vu hien thi). */
    public java.util.Map<Long, java.util.List<String>> roleCodesByUser() {
        java.util.Map<Long, java.util.List<String>> map = new java.util.HashMap<>();
        for (UserRole ur : userRoleRepository.findAll()) {
            if (!ur.isActive()) {
                continue;
            }
            String code = roleRepository.findById(ur.getRoleId()).map(Role::getCode).orElse("?");
            map.computeIfAbsent(ur.getUserId(), k -> new java.util.ArrayList<>()).add(code);
        }
        return map;
    }

    @Transactional
    public AppUser registerUser(String username, Long unitId, Long departmentId, String adminUser) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(u -> {
            throw new BusinessException("Tai khoan da ton tai: " + username);
        });
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setUnitId(unitId);
        user.setDepartmentId(departmentId);
        user.setStatus("ACTIVE");
        // Lay thong tin tu AD neu co.
        AdUserProfile profile = adClient.getUserProfile(username);
        if (profile != null) {
            user.setFullName(profile.getFullName());
            user.setEmail(profile.getEmail());
            user.setMobile(profile.getMobile());
        }
        user = userRepository.save(user);
        auditService.record(adminUser, "ADMIN", "REGISTER_USER", "app_user", user.getId(), username);
        return user;
    }

    @Transactional
    public void assignRole(Long userId, String roleCode, Long unitId, Long systemId, Long databaseId, String adminUser) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException("Vai tro khong hop le: " + roleCode));
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Khong tim thay nguoi dung"));
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(role.getId());
        ur.setUnitId(unitId);
        ur.setSystemId(systemId);
        ur.setDatabaseId(databaseId);
        ur.setActive(true);
        userRoleRepository.save(ur);
        auditService.record(adminUser, "ADMIN", "ASSIGN_ROLE", "user_role", userId,
                "Gan vai tro " + roleCode);
    }

    @Transactional
    public void disableUser(Long userId, String adminUser) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Khong tim thay nguoi dung"));
        user.setStatus("INACTIVE");
        userRepository.save(user);
        auditService.record(adminUser, "ADMIN", "DISABLE_USER", "app_user", userId, "Khoa nguoi dung");
    }

    // ===== Units =====
    public List<Unit> listUnits() {
        return unitRepository.findAll();
    }

    @Transactional
    public Unit createUnit(String code, String name, String adminUser) {
        unitRepository.findByCode(code).ifPresent(u -> {
            throw new BusinessException("Ma don vi da ton tai: " + code);
        });
        Unit unit = new Unit();
        unit.setCode(code);
        unit.setName(name);
        unit.setActive(true);
        unit = unitRepository.save(unit);
        auditService.record(adminUser, "ADMIN", "CREATE_UNIT", "unit", unit.getId(), code);
        return unit;
    }

    // ===== Systems =====
    public List<InformationSystem> listSystems() {
        return systemRepository.findAll();
    }

    @Transactional
    public InformationSystem createSystem(String code, String name, Long ownerUnitId, String adminUser) {
        systemRepository.findByCode(code).ifPresent(s -> {
            throw new BusinessException("Ma he thong da ton tai: " + code);
        });
        InformationSystem s = new InformationSystem();
        s.setCode(code);
        s.setName(name);
        s.setOwnerUnitId(ownerUnitId);
        s.setActive(true);
        s = systemRepository.save(s);
        auditService.record(adminUser, "ADMIN", "CREATE_SYSTEM", "information_system", s.getId(), code);
        return s;
    }

    // ===== Databases =====
    public List<DatabaseCatalog> listDatabases() {
        return databaseRepository.findAll();
    }

    @Transactional
    public DatabaseCatalog createDatabase(Long systemId, String code, String name, Long ownerUnitId, String adminUser) {
        DatabaseCatalog db = new DatabaseCatalog();
        db.setSystemId(systemId);
        db.setCode(code);
        db.setName(name);
        db.setOwnerUnitId(ownerUnitId);
        db.setActive(true);
        db = databaseRepository.save(db);
        auditService.record(adminUser, "ADMIN", "CREATE_DATABASE", "database_catalog", db.getId(), code);
        return db;
    }

    // ===== Roles / Rights (read-only) =====
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    public List<AccessRightCatalog> listRights() {
        return rightRepository.findAll();
    }
}
