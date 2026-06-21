package com.csdl.access.auth;

import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.domain.AppUser;

import java.util.List;

/** Ket qua dang nhap tra ve cho controller. */
public class LoginResult {

    public enum Status {
        SUCCESS,
        BAD_CREDENTIALS,
        USER_LOCKED,
        NOT_REGISTERED,   // AD ok nhung chua dang ky/khong co vai tro
        CONNECTION_ERROR
    }

    private final Status status;
    private final String message;
    private AppUser user;
    private List<RoleCode> roles;

    public LoginResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public List<RoleCode> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleCode> roles) {
        this.roles = roles;
    }
}
