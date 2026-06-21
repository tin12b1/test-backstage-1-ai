package com.csdl.access.configmaster;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.lookup.LookupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Cau hinh danh muc (api-contract.md muc 7). Chi ADMIN truy cap (SecurityConfig).
 */
@Controller
public class ConfigController {

    private final ConfigService configService;
    private final LookupService lookupService;
    private final UserSession userSession;

    public ConfigController(ConfigService configService,
                            LookupService lookupService,
                            UserSession userSession) {
        this.configService = configService;
        this.lookupService = lookupService;
        this.userSession = userSession;
    }

    private String admin() {
        return userSession.getUsername();
    }

    // ===== Users =====
    @GetMapping("/config/users")
    public String users(Model model) {
        model.addAttribute("users", configService.listUsers());
        model.addAttribute("units", lookupService.activeUnits());
        model.addAttribute("roles", RoleCode.values());
        model.addAttribute("rolesByUser", configService.roleCodesByUser());
        return "config/users";
    }

    @PostMapping("/config/users")
    public String registerUser(@RequestParam String username,
                               @RequestParam(required = false) Long unitId,
                               @RequestParam(required = false) Long departmentId,
                               RedirectAttributes ra) {
        configService.registerUser(username, unitId, departmentId, admin());
        ra.addFlashAttribute("infoMessage", "Da dang ky nguoi dung " + username);
        return "redirect:/config/users";
    }

    @PostMapping("/config/users/{id}/assign-role")
    public String assignRole(@PathVariable Long id,
                             @RequestParam String roleCode,
                             @RequestParam(required = false) Long unitId,
                             @RequestParam(required = false) Long systemId,
                             @RequestParam(required = false) Long databaseId,
                             RedirectAttributes ra) {
        configService.assignRole(id, roleCode, unitId, systemId, databaseId, admin());
        ra.addFlashAttribute("infoMessage", "Da gan vai tro.");
        return "redirect:/config/users";
    }

    @PostMapping("/config/users/{id}/disable")
    public String disableUser(@PathVariable Long id, RedirectAttributes ra) {
        configService.disableUser(id, admin());
        ra.addFlashAttribute("infoMessage", "Da khoa nguoi dung.");
        return "redirect:/config/users";
    }

    // ===== Units =====
    @GetMapping("/config/units")
    public String units(Model model) {
        model.addAttribute("units", configService.listUnits());
        return "config/units";
    }

    @PostMapping("/config/units")
    public String createUnit(@RequestParam String code, @RequestParam String name, RedirectAttributes ra) {
        configService.createUnit(code, name, admin());
        ra.addFlashAttribute("infoMessage", "Da them don vi.");
        return "redirect:/config/units";
    }

    // ===== Systems =====
    @GetMapping("/config/systems")
    public String systems(Model model) {
        model.addAttribute("systems", configService.listSystems());
        model.addAttribute("units", lookupService.activeUnits());
        model.addAttribute("lookup", lookupService);
        return "config/systems";
    }

    @PostMapping("/config/systems")
    public String createSystem(@RequestParam String code, @RequestParam String name,
                               @RequestParam(required = false) Long ownerUnitId, RedirectAttributes ra) {
        configService.createSystem(code, name, ownerUnitId, admin());
        ra.addFlashAttribute("infoMessage", "Da them he thong.");
        return "redirect:/config/systems";
    }

    // ===== Databases =====
    @GetMapping("/config/databases")
    public String databases(Model model) {
        model.addAttribute("databases", configService.listDatabases());
        model.addAttribute("systems", configService.listSystems());
        model.addAttribute("units", lookupService.activeUnits());
        model.addAttribute("lookup", lookupService);
        return "config/databases";
    }

    @PostMapping("/config/databases")
    public String createDatabase(@RequestParam Long systemId, @RequestParam String code,
                                 @RequestParam String name,
                                 @RequestParam(required = false) Long ownerUnitId, RedirectAttributes ra) {
        configService.createDatabase(systemId, code, name, ownerUnitId, admin());
        ra.addFlashAttribute("infoMessage", "Da them CSDL.");
        return "redirect:/config/databases";
    }

    // ===== Roles / Statuses =====
    @GetMapping("/config/roles")
    public String roles(Model model) {
        model.addAttribute("roles", configService.listRoles());
        model.addAttribute("rights", configService.listRights());
        model.addAttribute("statuses", RequestStatus.values());
        return "config/roles";
    }

    @GetMapping("/config/statuses")
    public String statuses(Model model) {
        return roles(model);
    }
}
