package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.domain.PreRegistrationRequest;
import com.csdl.access.domain.repo.PreRegistrationRepository;
import com.csdl.access.integration.otp.OtpService;
import com.csdl.access.integration.otp.OtpVerifyResult;
import com.csdl.access.request.dto.PreRegistrationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller CRUD va AJAX cho dang ky truoc yeu cau chi tiet (01-YCTC).
 * Validates: Requirements 16.1, 16.2, 16.5, 16.6, 17.1
 */
@Controller
@RequestMapping("/pre-registrations")
public class PreRegistrationController {

    private static final int PAGE_SIZE = 20;

    private final PreRegistrationService preRegistrationService;
    private final PreRegistrationRepository preRegistrationRepository;
    private final OtpService otpService;
    private final LookupService lookupService;
    private final UserSession userSession;

    public PreRegistrationController(PreRegistrationService preRegistrationService,
                                     PreRegistrationRepository preRegistrationRepository,
                                     OtpService otpService,
                                     LookupService lookupService,
                                     UserSession userSession) {
        this.preRegistrationService = preRegistrationService;
        this.preRegistrationRepository = preRegistrationRepository;
        this.otpService = otpService;
        this.lookupService = lookupService;
        this.userSession = userSession;
    }

    /**
     * GET /pre-registrations — Danh sach dang ky truoc cua user hien tai (phan trang 20/page).
     * Expire cac ban ghi het han truoc khi hien thi, va chi hien thi cac ban ghi chua het han.
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        // Expire outdated registrations before querying list
        preRegistrationService.expireOutdatedRegistrations();

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<PreRegistrationRequest> pageResult = preRegistrationService.listByUserExcludingExpired(
                userSession.getUserId(), pageable);
        model.addAttribute("page", pageResult);

        // Build lookup maps for system and database names
        java.util.Map<Long, String> systemMap = new java.util.HashMap<>();
        java.util.Map<Long, String> databaseMap = new java.util.HashMap<>();
        for (PreRegistrationRequest item : pageResult.getContent()) {
            if (item.getSystemId() != null && !systemMap.containsKey(item.getSystemId())) {
                systemMap.put(item.getSystemId(), lookupService.systemName(item.getSystemId()));
            }
            if (item.getDatabaseId() != null && !databaseMap.containsKey(item.getDatabaseId())) {
                databaseMap.put(item.getDatabaseId(), lookupService.databaseName(item.getDatabaseId()));
            }
        }
        model.addAttribute("systemMap", systemMap);
        model.addAttribute("databaseMap", databaseMap);

        return "requests/pre-registration/list";
    }

    /**
     * GET /pre-registrations/new — Form tao moi dang ky truoc.
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new PreRegistrationForm());
        model.addAttribute("systems", lookupService.activeSystems());
        model.addAttribute("databases", lookupService.activeDatabases());
        return "requests/pre-registration/form";
    }

    /**
     * POST /pre-registrations — Tao moi dang ky truoc voi ky OTP.
     * Xac thuc OTP truoc, neu thanh cong thi tao ban ghi va ghi nhan signedAt.
     */
    @PostMapping
    public String create(@ModelAttribute PreRegistrationForm form,
                         @RequestParam String otp,
                         RedirectAttributes ra) {
        // Xac thuc OTP truoc khi tao
        OtpVerifyResult otpResult = otpService.verifyOtp(
                userSession.getUsername(), otp, "PRE_REGISTRATION", null);
        if (!otpResult.isSuccess()) {
            ra.addFlashAttribute("errorMessage", "OTP khong hop le. Vui long thu lai.");
            ra.addFlashAttribute("form", form);
            return "redirect:/pre-registrations/new";
        }

        PreRegistrationRequest entity = preRegistrationService.create(form, userSession);
        // Ghi nhan chu ky sau khi OTP thanh cong
        entity.setSignedAt(LocalDateTime.now());
        preRegistrationRepository.save(entity);

        ra.addFlashAttribute("infoMessage", "Dang ky truoc thanh cong.");
        return "redirect:/pre-registrations";
    }

    /**
     * GET /pre-registrations/{id}/edit — Form sua dang ky truoc (chi khi UNUSED).
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PreRegistrationRequest entity = findOwned(id);
        if (!"UNUSED".equals(entity.getStatus())) {
            throw new BusinessException("Chi duoc sua dang ky truoc khi trang thai la 'Chua dung'");
        }
        model.addAttribute("entity", entity);
        model.addAttribute("form", toForm(entity));
        model.addAttribute("systems", lookupService.activeSystems());
        model.addAttribute("databases", lookupService.activeDatabases());
        return "requests/pre-registration/form";
    }

    /**
     * POST /pre-registrations/{id} — Cap nhat dang ky truoc voi ky lai OTP.
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute PreRegistrationForm form,
                         @RequestParam String otp,
                         RedirectAttributes ra) {
        // Xac thuc OTP truoc khi cap nhat
        OtpVerifyResult otpResult = otpService.verifyOtp(
                userSession.getUsername(), otp, "PRE_REGISTRATION_UPDATE", null);
        if (!otpResult.isSuccess()) {
            ra.addFlashAttribute("errorMessage", "OTP khong hop le. Vui long thu lai.");
            ra.addFlashAttribute("form", form);
            return "redirect:/pre-registrations/" + id + "/edit";
        }

        PreRegistrationRequest entity = preRegistrationService.update(id, form, userSession);
        // Ghi nhan chu ky moi sau khi update thanh cong (update da xoa signedAt)
        entity.setSignedAt(LocalDateTime.now());
        preRegistrationRepository.save(entity);

        ra.addFlashAttribute("infoMessage", "Cap nhat dang ky truoc thanh cong.");
        return "redirect:/pre-registrations";
    }

    /**
     * DELETE /pre-registrations/{id} — Xoa vinh vien (chi khi UNUSED).
     * Tra ve 204 No Content.
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        preRegistrationService.delete(id, userSession);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /pre-registrations/clone — Sao chep dang ky truoc sang ngay/ca khac.
     */
    @PostMapping("/clone")
    public String clone(@RequestParam Long sourceId,
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate,
                        @RequestParam int targetShift,
                        RedirectAttributes ra) {
        preRegistrationService.clone(sourceId, targetDate, targetShift, userSession);
        ra.addFlashAttribute("infoMessage", "Sao chep dang ky truoc thanh cong.");
        return "redirect:/pre-registrations";
    }

    /**
     * GET /pre-registrations/load — AJAX endpoint nap dang ky truoc vao form 01-YCTC.
     * Tra ve List PreRegistrationDto (JSON).
     */
    @GetMapping("/load")
    @ResponseBody
    public List<PreRegistrationDto> loadForForm(
            @RequestParam String unitCode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam int shift,
            @RequestParam String requestType) {
        List<PreRegistrationRequest> records = preRegistrationService.loadForForm01(
                unitCode, date, shift, requestType);
        return records.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ============================
    // Private helpers
    // ============================

    private PreRegistrationRequest findOwned(Long id) {
        PreRegistrationRequest entity = preRegistrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay ban ghi dang ky truoc"));
        if (!entity.getUserId().equals(userSession.getUserId())) {
            throw new BusinessException("Ban khong co quyen thao tac tren ban ghi nay");
        }
        return entity;
    }

    private PreRegistrationForm toForm(PreRegistrationRequest entity) {
        PreRegistrationForm form = new PreRegistrationForm();
        form.setRegisterDate(entity.getRegisterDate());
        form.setShift(entity.getShift());
        form.setRequestType(entity.getRequestType());
        form.setSystemId(entity.getSystemId());
        form.setDatabaseId(entity.getDatabaseId());
        form.setObjectName(entity.getObjectName());
        form.setAccessRights(entity.getAccessRights());
        return form;
    }

    private PreRegistrationDto toDto(PreRegistrationRequest entity) {
        return new PreRegistrationDto(
                entity.getId(),
                entity.getUserId(),
                lookupService.userName(entity.getUserId()),
                entity.getUnitCode(),
                entity.getRegisterDate(),
                entity.getShift(),
                entity.getRequestType(),
                entity.getSystemId(),
                lookupService.systemName(entity.getSystemId()),
                entity.getDatabaseId(),
                lookupService.databaseName(entity.getDatabaseId()),
                entity.getObjectName(),
                entity.getAccessRights(),
                entity.getSignedAt(),
                entity.getStatus()
        );
    }
}
