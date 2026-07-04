package com.csdl.access.auth;

import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.domain.AppUser;
import com.csdl.access.domain.DatabaseCatalog;
import com.csdl.access.domain.InformationSystem;
import com.csdl.access.domain.SignatureImage;
import com.csdl.access.domain.UserRole;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.DatabaseCatalogRepository;
import com.csdl.access.domain.repo.InformationSystemRepository;
import com.csdl.access.domain.repo.SignatureImageRepository;
import com.csdl.access.domain.repo.UserRoleRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Man hinh thong tin nguoi dung: thong tin ca nhan, vai tro, chu ky,
 * he thong thong tin va CSDL lien quan.
 */
@Controller
public class ProfileController {

    private final UserSession userSession;
    private final AppUserRepository appUserRepository;
    private final SignatureImageRepository signatureImageRepository;
    private final UserRoleRepository userRoleRepository;
    private final InformationSystemRepository systemRepository;
    private final DatabaseCatalogRepository databaseRepository;
    private final LookupService lookupService;

    public ProfileController(UserSession userSession,
                             AppUserRepository appUserRepository,
                             SignatureImageRepository signatureImageRepository,
                             UserRoleRepository userRoleRepository,
                             InformationSystemRepository systemRepository,
                             DatabaseCatalogRepository databaseRepository,
                             LookupService lookupService) {
        this.userSession = userSession;
        this.appUserRepository = appUserRepository;
        this.signatureImageRepository = signatureImageRepository;
        this.userRoleRepository = userRoleRepository;
        this.systemRepository = systemRepository;
        this.databaseRepository = databaseRepository;
        this.lookupService = lookupService;
    }

    /** Trang thong tin nguoi dung: ho so, vai tro, chu ky, he thong va CSDL lien quan. */
    @GetMapping("/profile")
    public String profile(Model model) {
        if (!userSession.isAuthenticated()) {
            return "redirect:/login";
        }
        AppUser user = appUserRepository.findById(userSession.getUserId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("unitName", lookupService.unitName(userSession.getUnitId()));
        model.addAttribute("departmentName", lookupService.departmentName(userSession.getDepartmentId()));
        model.addAttribute("roles", userSession.getAvailableRoles());
        model.addAttribute("activeRole", userSession.getActiveRole());
        model.addAttribute("hasSignature", user != null && user.getSignatureImageId() != null);

        // He thong lien quan: lay tu pham vi vai tro (user_role.system_id); neu khong co thi lay theo don vi.
        Set<Long> sysIds = userRoleRepository.findByUserIdAndActiveTrue(userSession.getUserId()).stream()
                .map(UserRole::getSystemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<InformationSystem> systems;
        if (!sysIds.isEmpty()) {
            systems = systemRepository.findByActiveTrue().stream()
                    .filter(s -> sysIds.contains(s.getId()))
                    .collect(Collectors.toList());
        } else {
            Long unitId = userSession.getUnitId();
            systems = systemRepository.findByActiveTrue().stream()
                    .filter(s -> unitId != null && unitId.equals(s.getOwnerUnitId()))
                    .collect(Collectors.toList());
        }
        // CSDL lien quan: cac CSDL thuoc cac he thong lien quan.
        List<DatabaseCatalog> databases = new ArrayList<>();
        for (InformationSystem s : systems) {
            databases.addAll(databaseRepository.findBySystemIdAndActiveTrue(s.getId()));
        }
        model.addAttribute("systems", systems);
        model.addAttribute("databases", databases);
        model.addAttribute("lookup", lookupService);
        return "profile/index";
    }

    /** Phuc vu anh chu ky cua nguoi dang nhap (neu da khai bao). */
    @GetMapping("/profile/signature")
    public ResponseEntity<byte[]> signature() {
        if (!userSession.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        AppUser user = appUserRepository.findById(userSession.getUserId()).orElse(null);
        if (user == null || user.getSignatureImageId() == null) {
            return ResponseEntity.notFound().build();
        }
        SignatureImage img = signatureImageRepository.findById(user.getSignatureImageId()).orElse(null);
        if (img == null || img.getData() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType type = img.getContentType() == null ? MediaType.IMAGE_PNG
                : MediaType.parseMediaType(img.getContentType());
        return ResponseEntity.ok().contentType(type).body(img.getData());
    }
}
