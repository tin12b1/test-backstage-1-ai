package com.csdl.access.auth;

import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.domain.AppUser;
import com.csdl.access.domain.LoginLog;
import com.csdl.access.domain.Role;
import com.csdl.access.domain.UserRole;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.LoginLogRepository;
import com.csdl.access.domain.repo.RoleRepository;
import com.csdl.access.domain.repo.UserRoleRepository;
import com.csdl.access.integration.ad.AdAuthResult;
import com.csdl.access.integration.ad.AdClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Xac thuc AD, truy van vai tro va ghi log dang nhap (features/login.md).
 */
@Service
public class AuthService {

    private final AdClient adClient;
    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final LoginLogRepository loginLogRepository;

    public AuthService(AdClient adClient,
                       AppUserRepository appUserRepository,
                       UserRoleRepository userRoleRepository,
                       RoleRepository roleRepository,
                       LoginLogRepository loginLogRepository) {
        this.adClient = adClient;
        this.appUserRepository = appUserRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.loginLogRepository = loginLogRepository;
    }

    @Transactional
    public LoginResult login(String username, String password, String ipAddress) {
        AdAuthResult adResult = adClient.authenticate(username, password);
        if (!adResult.isSuccess()) {
            LoginResult.Status status;
            switch (adResult.getStatus()) {
                case USER_LOCKED:
                    status = LoginResult.Status.USER_LOCKED;
                    break;
                case CONNECTION_ERROR:
                    status = LoginResult.Status.CONNECTION_ERROR;
                    break;
                default:
                    status = LoginResult.Status.BAD_CREDENTIALS;
            }
            writeLog(username, false, adResult.getMessage(), ipAddress);
            return new LoginResult(status, adResult.getMessage());
        }

        Optional<AppUser> userOpt = appUserRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            writeLog(username, false, "Chua dang ky tren he thong hoac da bi khoa", ipAddress);
            return new LoginResult(LoginResult.Status.NOT_REGISTERED,
                    "Tai khoan chua duoc dang ky vai tro tren he thong");
        }

        AppUser user = userOpt.get();
        List<RoleCode> roles = resolveActiveRoles(user.getId());
        if (roles.isEmpty()) {
            writeLog(username, false, "Khong co vai tro active", ipAddress);
            return new LoginResult(LoginResult.Status.NOT_REGISTERED,
                    "Tai khoan chua duoc gan vai tro active");
        }

        writeLog(username, true, "Dang nhap thanh cong", ipAddress);
        LoginResult result = new LoginResult(LoginResult.Status.SUCCESS, "OK");
        result.setUser(user);
        result.setRoles(roles);
        return result;
    }

    /** Tra ve danh sach vai tro active cua user, khong trung. */
    public List<RoleCode> resolveActiveRoles(Long userId) {
        Set<RoleCode> roleCodes = new LinkedHashSet<>();
        for (UserRole ur : userRoleRepository.findByUserIdAndActiveTrue(userId)) {
            roleRepository.findById(ur.getRoleId())
                    .map(Role::getCode)
                    .ifPresent(code -> {
                        try {
                            roleCodes.add(RoleCode.valueOf(code));
                        } catch (IllegalArgumentException ignore) {
                            // bo qua ma vai tro khong hop le
                        }
                    });
        }
        return new ArrayList<>(roleCodes);
    }

    private void writeLog(String username, boolean success, String message, String ipAddress) {
        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setSuccess(success);
        log.setMessage(message);
        log.setIpAddress(ipAddress);
        loginLogRepository.save(log);
    }
}
