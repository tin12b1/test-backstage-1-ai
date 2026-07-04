package com.csdl.access.configmaster;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.audit.AuditService;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.AppUser;
import com.csdl.access.domain.UserTotp;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.UserTotpRepository;
import com.csdl.access.integration.otp.TotpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Quan tri he thong (ADMIN) dang ky / reset Google Authenticator cho moi nguoi dung.
 * Duong dan /config/** da gioi han ADMIN trong SecurityConfig.
 */
@Controller
public class AdminGaController {

    /** Truy van thong tin nguoi dung ung dung. */
    private final AppUserRepository appUserRepository;
    /** Luu bi mat/trang thai Google Authenticator theo user. */
    private final UserTotpRepository userTotpRepository;
    /** Sinh bi mat, tao otpauth URI va anh QR TOTP. */
    private final TotpService totpService;
    /** Ghi nhat ky thao tac (audit). */
    private final AuditService auditService;
    /** Phien lam viec hien tai (lay username ADMIN dang thao tac). */
    private final UserSession userSession;

    /** Ten don vi phat hanh hien thi tren ung dung Authenticator. */
    @Value("${integration.otp.ga.issuer:Agribank CSDL}")
    private String issuer;

    public AdminGaController(AppUserRepository appUserRepository,
                             UserTotpRepository userTotpRepository,
                             TotpService totpService,
                             AuditService auditService,
                             UserSession userSession) {
        this.appUserRepository = appUserRepository;
        this.userTotpRepository = userTotpRepository;
        this.totpService = totpService;
        this.auditService = auditService;
        this.userSession = userSession;
    }

    /** Man hinh quan ly GA cua mot nguoi dung: hien trang thai va bi mat de ban giao. */
    @GetMapping("/config/users/{id}/ga")
    public String page(@PathVariable Long id, Model model) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));
        UserTotp totp = userTotpRepository.findByUserId(id).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("enabled", totp != null && totp.isEnabled());
        model.addAttribute("hasSecret", totp != null);
        model.addAttribute("issuer", issuer);
        // Chi hien bi mat/otpauth URI khi user da co dang ky GA.
        if (totp != null) {
            model.addAttribute("secretDisplay", totpService.formatForDisplay(totp.getSecret()));
            model.addAttribute("otpAuthUri",
                    totpService.otpAuthUri(issuer, user.getUsername(), totp.getSecret()));
        }
        return "config/user-ga";
    }

    /** ADMIN cap dang ky GA cho nguoi dung: sinh bi mat moi va kich hoat luon. */
    @PostMapping("/config/users/{id}/ga/enroll")
    public String enroll(@PathVariable Long id, RedirectAttributes ra) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));
        UserTotp totp = userTotpRepository.findByUserId(id).orElseGet(UserTotp::new);
        totp.setUserId(id);
        totp.setSecret(totpService.generateSecret());
        totp.setEnabled(true);
        totp.setConfirmedAt(LocalDateTime.now());
        userTotpRepository.save(totp);
        auditService.record(userSession.getUsername(), "ADMIN",
                "GA_ADMIN_ENROLL", "user_totp", totp.getId(),
                "Cap Google Authenticator cho user " + user.getUsername());
        ra.addFlashAttribute("infoMessage",
                "Đã cấp Google Authenticator cho " + user.getFullName()
                        + ". Cung cấp khóa bí mật bên dưới để người dùng thêm vào ứng dụng.");
        return "redirect:/config/users/" + id + "/ga";
    }

    /** Reset GA cua nguoi dung (xoa dang ky). */
    @PostMapping("/config/users/{id}/ga/reset")
    public String reset(@PathVariable Long id, RedirectAttributes ra) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));
        Optional<UserTotp> totp = userTotpRepository.findByUserId(id);
        totp.ifPresent(t -> {
            userTotpRepository.delete(t);
            auditService.record(userSession.getUsername(), "ADMIN",
                    "GA_ADMIN_RESET", "user_totp", t.getId(),
                    "Reset Google Authenticator cho user " + user.getUsername());
        });
        ra.addFlashAttribute("infoMessage", "Đã reset Google Authenticator cho " + user.getFullName() + ".");
        return "redirect:/config/users/" + id + "/ga";
    }

    /** Anh QR otpauth cho bi mat cua nguoi dung (ADMIN xem de ban giao). */
    @GetMapping(value = "/config/users/{id}/ga/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable Long id) {
        AppUser user = appUserRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserTotp totp = userTotpRepository.findByUserId(id).orElse(null);
        // Chua co dang ky GA thi khong co bi mat de tao QR.
        if (totp == null) {
            return ResponseEntity.notFound().build();
        }
        String uri = totpService.otpAuthUri(issuer, user.getUsername(), totp.getSecret());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(totpService.qrPng(uri, 240));
    }
}
