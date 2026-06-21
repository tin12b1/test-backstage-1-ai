package com.csdl.access.auth;

import com.csdl.access.common.enums.RoleCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Controller dang nhap va chon vai tro (features/login.md, api-contract.md muc 2).
 */
@Controller
public class LoginController {

    private final AuthService authService;
    private final AuthSessionManager sessionManager;
    private final UserSession userSession;
    private final CaptchaService captchaService;

    public LoginController(AuthService authService,
                           AuthSessionManager sessionManager,
                           UserSession userSession,
                           CaptchaService captchaService) {
        this.authService = authService;
        this.sessionManager = sessionManager;
        this.userSession = userSession;
        this.captchaService = captchaService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String denied,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Ban da dang xuat.");
        }
        if (denied != null) {
            model.addAttribute("errorMessage", "Ban khong co quyen truy cap chuc nang nay.");
        }
        return "login/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam(required = false) String captcha,
                          HttpServletRequest request,
                          Model model) {
        // Kiem tra captcha truoc khi goi xac thuc AD. Ma captcha dung mot lan.
        HttpSession session = request.getSession();
        String expectedCaptcha = (String) session.getAttribute(CaptchaService.SESSION_KEY);
        session.removeAttribute(CaptchaService.SESSION_KEY);
        if (!captchaService.matches(expectedCaptcha, captcha)) {
            model.addAttribute("errorMessage", "Ma xac nhan khong dung. Vui long thu lai.");
            return "login/login";
        }

        LoginResult result = authService.login(username, password, request.getRemoteAddr());
        if (!result.isSuccess()) {
            model.addAttribute("errorMessage", friendly(result));
            return "login/login";
        }

        sessionManager.initSession(result.getUser(), result.getRoles());

        if (result.getRoles().size() == 1) {
            sessionManager.activateRole(result.getRoles().get(0), request);
            return "redirect:/dashboard";
        }
        // Nhieu vai tro: hien thi man hinh chon vai tro.
        model.addAttribute("roles", result.getRoles());
        return "auth/select-role";
    }

    @PostMapping("/session/role")
    public String selectRole(@RequestParam String roleCode,
                             HttpServletRequest request,
                             Model model) {
        if (!userSession.isAuthenticated()) {
            return "redirect:/login";
        }
        RoleCode role;
        try {
            role = RoleCode.valueOf(roleCode);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Vai tro khong hop le.");
            model.addAttribute("roles", userSession.getAvailableRoles());
            return "auth/select-role";
        }
        if (!sessionManager.hasRole(role)) {
            model.addAttribute("errorMessage", "Ban khong duoc gan vai tro nay.");
            model.addAttribute("roles", userSession.getAvailableRoles());
            return "auth/select-role";
        }
        sessionManager.activateRole(role, request);
        return "redirect:/dashboard";
    }

    private String friendly(LoginResult result) {
        switch (result.getStatus()) {
            case USER_LOCKED:
                return "Tai khoan da bi khoa. Vui long lien he quan tri.";
            case NOT_REGISTERED:
                return result.getMessage();
            case CONNECTION_ERROR:
                return "He thong xac thuc dang ban. Vui long thu lai sau.";
            default:
                return "Sai tai khoan hoac mat khau.";
        }
    }
}
