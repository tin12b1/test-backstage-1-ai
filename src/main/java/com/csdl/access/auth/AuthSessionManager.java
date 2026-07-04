package com.csdl.access.auth;

import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.domain.AppUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

/**
 * Thiet lap SecurityContext theo active role va luu vao session.
 * Active role quyet dinh authority de phan quyen menu/du lieu (features/login.md).
 */
@Component
public class AuthSessionManager {

    // Khoa luu SecurityContext trong HTTP session
    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    private final UserSession userSession;

    public AuthSessionManager(UserSession userSession) {
        this.userSession = userSession;
    }

    /** Khoi tao session sau khi dang nhap AD thanh cong. */
    public void initSession(AppUser user, List<RoleCode> roles) {
        userSession.reset();
        userSession.setUserId(user.getId());
        userSession.setUsername(user.getUsername());
        userSession.setFullName(user.getFullName());
        userSession.setEmail(user.getEmail());
        userSession.setUnitId(user.getUnitId());
        userSession.setDepartmentId(user.getDepartmentId());
        userSession.getAvailableRoles().addAll(roles);
    }

    /** Chon vai tro lam viec va dat authority tuong ung. */
    public void activateRole(RoleCode role, HttpServletRequest request) {
        userSession.setActiveRole(role);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userSession.getUsername(),
                "N/A",
                Collections.singletonList(new SimpleGrantedAuthority(role.authority()))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Luu context vao session de cac request sau giu trang thai dang nhap.
        HttpSession session = request.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);
    }

    /** Kiem tra nguoi dung co duoc gan vai tro nay hay khong. */
    public boolean hasRole(RoleCode role) {
        return userSession.getAvailableRoles().contains(role);
    }
}
