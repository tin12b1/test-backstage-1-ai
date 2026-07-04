package com.csdl.access.auth;

import com.csdl.access.common.enums.RoleCode;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Trang thai phien dang nhap: nguoi dung va vai tro dang lam viec (active role).
 * Active role quyet dinh dashboard va menu (features/login.md muc 6).
 */
@Component
@SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession implements Serializable {

    // Id nguoi dung
    private Long userId;
    // Ten dang nhap
    private String username;
    // Ho ten day du
    private String fullName;
    // Dia chi email
    private String email;
    // Id don vi
    private Long unitId;
    // Id phong ban
    private Long departmentId;
    // Danh sach vai tro nguoi dung duoc gan
    private final List<RoleCode> availableRoles = new ArrayList<>();
    // Vai tro dang lam viec (quyet dinh menu/du lieu)
    private RoleCode activeRole;

    /** Da dang nhap khi da co ten dang nhap trong phien. */
    public boolean isAuthenticated() {
        return username != null;
    }

    /** Co nhieu hon mot vai tro (can chon vai tro lam viec). */
    public boolean hasMultipleRoles() {
        return availableRoles.size() > 1;
    }

    /** Xoa toan bo trang thai phien (dung khi dang nhap lai/dang xuat). */
    public void reset() {
        userId = null;
        username = null;
        fullName = null;
        email = null;
        unitId = null;
        departmentId = null;
        availableRoles.clear();
        activeRole = null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public List<RoleCode> getAvailableRoles() {
        return availableRoles;
    }

    public RoleCode getActiveRole() {
        return activeRole;
    }

    public void setActiveRole(RoleCode activeRole) {
        this.activeRole = activeRole;
    }
}
