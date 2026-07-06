package com.csdl.access.auth;

import com.csdl.access.common.audit.AuditService;
import com.csdl.access.domain.UserTotp;
import com.csdl.access.domain.repo.UserTotpRepository;
import com.csdl.access.integration.otp.TotpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Dang ky / xac nhan / reset Google Authenticator (TOTP) - thay cho SoftOTP.
 */
@Controller
public class GoogleAuthController {

    private final UserSession userSession;
    private final UserTotpRepository userTotpRepository;
    private final TotpService totpService;
    private final AuditService auditService;

    @Value("${integration.otp.ga.issuer:Agribank CSDL}")
    private String issuer;

    public GoogleAuthController(UserSession userSession,
                                UserTotpRepository userTotpRepository,
                                TotpService totpService,
                                AuditService auditService) {
        this.userSession = userSession;
        this.userTotpRepository = userTotpRepository;
        this.totpService = totpService;
        this.auditService = auditService;
    }

    /** Trang cau hinh Google Authenticator: hien trang thai (da bat / cho xac nhan) va QR. */
    @GetMapping("/profile/ga")
    public String page(Model model) {
        if (!userSession.isAuthenticated()) {
            return "redirect:/login";
        }
        UserTotp totp = userTotpRepository.findByUserId(userSession.getUserId()).orElse(null);
        // enabled: da kich hoat; pending: da sinh bi mat nhung chua xac nhan
        boolean enabled = totp != null && totp.isEnabled();
        boolean pending = totp != null && !totp.isEnabled();
        model.addAttribute("enabled", enabled);
        model.addAttribute("pending", pending);
        model.addAttribute("issuer", issuer);
        model.addAttribute("account", userSession.getUsername());
        if (pending) {
            // Hien thi bi mat va URI otpauth de nguoi dung them vao ung dung
            model.addAttribute("secretDisplay", totpService.formatForDisplay(totp.getSecret()));
            model.addAttribute("otpAuthUri",
                    totpService.otpAuthUri(issuer, userSession.getUsername(), totp.getSecret()));
        }
        return "profile/ga";
    }

    /** Bat dau dang ky: sinh bi mat moi (trang thai cho xac nhan). */
    @PostMapping("/profile/ga/enroll")
    public String enroll(RedirectAttributes ra) {
        if (!userSession.isAuthenticated()) {
            return "redirect:/login";
        }
        UserTotp totp = userTotpRepository.findByUserId(userSession.getUserId())
                .orElseGet(UserTotp::new);
        totp.setUserId(userSession.getUserId());
        totp.setSecret(totpService.generateSecret());
        totp.setEnabled(false);
        totp.setConfirmedAt(null);
        userTotpRepository.save(totp);
        auditService.record(userSession.getUsername(),
                userSession.getActiveRole() == null ? null : userSession.getActiveRole().name(),
                "GA_ENROLL", "user_totp", totp.getId(), "Bat dau dang ky Google Authenticator");
        ra.addFlashAttribute("infoMessage",
                "Đã tạo mã bí mật mới. Thêm tài khoản vào Google Authenticator rồi nhập mã 6 số để xác nhận.");
        return "redirect:/profile/ga";
    }

    /** Xac nhan ma 6 so de kich hoat. */
    @PostMapping("/profile/ga/confirm")
    public String confirm(@RequestParam String code, RedirectAttributes ra) {
        if (!userSession.isAuthenticated()) {
            return "redirect:/login";
        }
        UserTotp totp = userTotpRepository.findByUserId(userSession.getUserId()).orElse(null);
        if (totp == null) {
            ra.addFlashAttribute("errorMessage", "Chưa có mã đăng ký. Hãy bấm Đăng ký trước.");
            return "redirect:/profile/ga";
        }
        if (totpService.verify(totp.getSecret(), code)) {
            totp.setEnabled(true);
            totp.setConfirmedAt(LocalDateTime.now());
            userTotpRepository.save(totp);
            auditService.record(userSession.getUsername(),
                    userSession.getActiveRole() == null ? null : userSession.getActiveRole().name(),
                    "GA_CONFIRM", "user_totp", totp.getId(), "Kich hoat Google Authenticator");
            ra.addFlashAttribute("infoMessage", "Kích hoạt Google Authenticator thành công.");
        } else {
            ra.addFlashAttribute("errorMessage", "Mã xác nhận không đúng. Vui lòng thử lại.");
        }
        return "redirect:/profile/ga";
    }

    /** Reset: xoa dang ky hien tai de dang ky lai. */
    @PostMapping("/profile/ga/reset")
    public String reset(RedirectAttributes ra) {
        if (!userSession.isAuthenticated()) {
            return "redirect:/login";
        }
        Optional<UserTotp> totp = userTotpRepository.findByUserId(userSession.getUserId());
        totp.ifPresent(t -> {
            userTotpRepository.delete(t);
            auditService.record(userSession.getUsername(),
                    userSession.getActiveRole() == null ? null : userSession.getActiveRole().name(),
                    "GA_RESET", "user_totp", t.getId(), "Reset Google Authenticator");
        });
        ra.addFlashAttribute("infoMessage",
                "Đã reset Google Authenticator. Hãy đăng ký lại để tiếp tục ký xác nhận.");
        return "redirect:/profile/ga";
    }

    /** Anh QR otpauth cho bi mat hien tai (pending hoac da kich hoat). */
    @GetMapping(value = "/profile/ga/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr() {
        if (!userSession.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        UserTotp totp = userTotpRepository.findByUserId(userSession.getUserId()).orElse(null);
        if (totp == null) {
            return ResponseEntity.notFound().build();
        }
        String uri = totpService.otpAuthUri(issuer, userSession.getUsername(), totp.getSecret());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(totpService.qrPng(uri, 240));
    }
}
