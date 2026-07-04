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

    /** Hien thi trang dang nhap; hien thong bao loi/dang xuat/tu choi truy cap neu co. */
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
            model.addAttribute("errorMessage", "Bạn không có quyền truy cập chức năng này.");
        }
        return "login/login";
    }

    /**
     * Xu ly submit dang nhap: kiem tra captcha, xac thuc, roi dieu huong.
     * Neu co 1 vai tro thi vao dashboard luon; neu nhieu vai tro thi cho chon.
     */
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
            model.addAttribute("errorMessage", "Mã xác nhận không đúng. Vui lòng thử lại.");
            return "login/login";
        }

        LoginResult result = authService.login(username, password, request.getRemoteAddr());
        if (!result.isSuccess()) {
            model.addAttribute("errorMessage", friendly(result));
            return "login/login";
        }

        sessionManager.initSession(result.getUser(), result.getRoles());

        // Chi co 1 vai tro: kich hoat luon va vao dashboard
        if (result.getRoles().size() == 1) {
            sessionManager.activateRole(result.getRoles().get(0), request);
            return "redirect:/dashboard";
        }
        // Nhieu vai tro: hien thi man hinh chon vai tro.
        model.addAttribute("roles", result.getRoles());
        return "auth/select-role";
    }

    /** Kich hoat vai tro nguoi dung da chon (khi co nhieu vai tro), roi vao dashboard. */
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
            // Ma vai tro gui len khong ton tai
            model.addAttribute("errorMessage", "Vai trò không hợp lệ.");
            model.addAttribute("roles", userSession.getAvailableRoles());
            return "auth/select-role";
        }
        if (!sessionManager.hasRole(role)) {
            // Vai tro hop le nhung khong duoc gan cho nguoi dung nay
            model.addAttribute("errorMessage", "Bạn không được gán vai trò này.");
            model.addAttribute("roles", userSession.getAvailableRoles());
            return "auth/select-role";
        }
        sessionManager.activateRole(role, request);
        return "redirect:/dashboard";
    }

    /** Doi trang thai ket qua dang nhap sang thong bao than thien cho nguoi dung. */
    private String friendly(LoginResult result) {
        switch (result.getStatus()) {
            case USER_LOCKED:
                return "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị.";
            case NOT_REGISTERED:
                return result.getMessage();
            case CONNECTION_ERROR:
                return "Hệ thống xác thực đang bận. Vui lòng thử lại sau.";
            default:
                return "Sai tài khoản hoặc mật khẩu.";
        }
    }
}
