package com.csdl.access.auth;

import com.csdl.access.common.enums.RoleCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/** Test dang nhap AD va truy van vai tro (features/login.md muc 9). */
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void login_singleRole_success() {
        LoginResult result = authService.login("requester1", "password", "127.0.0.1");
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(RoleCode.REQUESTER));
    }

    @Test
    void login_multipleRoles_success() {
        LoginResult result = authService.login("admin", "password", "127.0.0.1");
        assertTrue(result.isSuccess());
        assertTrue(result.getRoles().size() > 1);
        assertTrue(result.getRoles().contains(RoleCode.ADMIN));
    }

    @Test
    void login_badPassword_fails() {
        LoginResult result = authService.login("requester1", "wrong", "127.0.0.1");
        assertFalse(result.isSuccess());
        assertEquals(LoginResult.Status.BAD_CREDENTIALS, result.getStatus());
    }

    @Test
    void login_lockedUser_fails() {
        LoginResult result = authService.login("locked.user", "password", "127.0.0.1");
        assertFalse(result.isSuccess());
        assertEquals(LoginResult.Status.USER_LOCKED, result.getStatus());
    }

    @Test
    void login_unknownUser_fails() {
        LoginResult result = authService.login("nobody", "password", "127.0.0.1");
        assertFalse(result.isSuccess());
    }
}
