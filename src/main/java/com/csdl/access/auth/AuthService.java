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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // Client xac thuc voi Active Directory
    private final AdClient adClient;
    // Repository nguoi dung ung dung
    private final AppUserRepository appUserRepository;
    // Repository gan vai tro cho nguoi dung
    private final UserRoleRepository userRoleRepository;
    // Repository danh muc vai tro
    private final RoleRepository roleRepository;
    // Repository nhat ky dang nhap
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

    /**
     * Dang nhap: xac thuc AD, kiem tra dang ky va vai tro active, ghi log.
     *
     * @param username ten dang nhap
     * @param password mat khau
     * @param ipAddress dia chi IP nguoi goi (de ghi log)
     * @return ket qua dang nhap kem trang thai va (neu thanh cong) user + vai tro
     */
    @Transactional
    public LoginResult login(String username, String password, String ipAddress) {
        // Buoc 1: xac thuc voi Active Directory
        AdAuthResult adResult = adClient.authenticate(username, password);
        if (!adResult.isSuccess()) {
            // Chuyen doi trang thai loi AD sang trang thai ket qua dang nhap
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

        // Buoc 2: AD ok nhung phai da dang ky va con hoat dong tren he thong
        Optional<AppUser> userOpt = appUserRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            writeLog(username, false, "Chưa đăng ký trên hệ thống hoặc đã bị khóa", ipAddress);
            return new LoginResult(LoginResult.Status.NOT_REGISTERED,
                    "Tài khoản chưa được đăng ký vai trò trên hệ thống");
        }

        // Buoc 3: phai co it nhat mot vai tro active
        AppUser user = userOpt.get();
        List<RoleCode> roles = resolveActiveRoles(user.getId());
        if (roles.isEmpty()) {
            writeLog(username, false, "Không có vai trò active", ipAddress);
            return new LoginResult(LoginResult.Status.NOT_REGISTERED,
                    "Tài khoản chưa được gán vai trò active");
        }

        // Thanh cong: tra ve user va danh sach vai tro de controller khoi tao session
        writeLog(username, true, "Đăng nhập thành công", ipAddress);
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

    /** Ghi ban ghi nhat ky dang nhap vao DB va xuat log ung dung. */
    private void writeLog(String username, boolean success, String message, String ipAddress) {
        LoginLog entry = new LoginLog();
        entry.setUsername(username);
        entry.setSuccess(success);
        entry.setMessage(message);
        entry.setIpAddress(ipAddress);
        loginLogRepository.save(entry);
        // Ghi ra file log ung dung (man hinh Debug) - ca thanh cong lan that bai.
        if (success) {
            log.info("[DANG NHAP] user={} ip={} ket qua=THANH CONG ({})", username, ipAddress, message);
        } else {
            log.warn("[DANG NHAP] user={} ip={} ket qua=THAT BAI ({})", username, ipAddress, message);
        }
    }
}
