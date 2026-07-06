package com.csdl.access.integration.ad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AD client gia lap dung cho moi truong phat trien/test.
 *
 * Quy tac: moi user trong directory gia lap xac thuc thanh cong voi mat khau dung cau hinh
 * (mac dinh "password"). User "locked.user" mo phong tinh huong bi khoa.
 */
@Component
@ConditionalOnProperty(name = "integration.ad.mode", havingValue = "mock", matchIfMissing = true)
public class MockAdClient implements AdClient {

    private static final Logger log = LoggerFactory.getLogger(MockAdClient.class);
    /** Mat khau mac dinh cho moi tai khoan trong moi truong gia lap. */
    private static final String DEFAULT_PASSWORD = "password";

    /** Directory gia lap: key la username viet thuong -> ho so nguoi dung. */
    private final Map<String, AdUserProfile> directory = new HashMap<>();

    /** Nap san mot so tai khoan mau tuong ung cac vai tro trong quy trinh. */
    public MockAdClient() {
        add("admin", "Quan tri he thong", "admin@csdl.local", "0900000000", "DV-CNTT", "Phong Quan tri");
        add("requester1", "Nguyen Van A", "requester1@csdl.local", "0900000001", "DV-KD", "Phong Kinh doanh");
        add("manager1", "Tran Thi B", "manager1@csdl.local", "0900000002", "DV-KD", "Phong Kinh doanh");
        add("authority1", "Le Van C", "authority1@csdl.local", "0900000003", "DV-KD", "Ban Lanh dao");
        add("checker1", "Pham Thi D", "checker1@csdl.local", "0900000004", "DV-CNTT", "Phong Kiem tra");
        add("access1", "Hoang Van E", "access1@csdl.local", "0900000005", "DV-CNTT", "Phong Van hanh");
        add("dba1", "Vo Thi F", "dba1@csdl.local", "0900000006", "DV-CNTT", "Phong CSDL");
        add("executor1", "Dang Van G", "executor1@csdl.local", "0900000007", "DV-CNTT", "Phong Van hanh");
        add("locked.user", "User Bi Khoa", "locked@csdl.local", "0900000099", "DV-KD", "Phong Kinh doanh");
    }

    /** Them mot ho so nguoi dung vao directory gia lap (key theo username viet thuong). */
    private void add(String username, String fullName, String email, String mobile, String unit, String dept) {
        AdUserProfile p = new AdUserProfile(username, fullName, email, mobile);
        p.setUnit(unit);
        p.setDepartment(dept);
        directory.put(username.toLowerCase(), p);
    }

    @Override
    public AdAuthResult authenticate(String username, String password) {
        // Khong bao gio log gia tri password.
        log.debug("[MOCK-AD] authenticate username={}", username);
        if (username == null || username.isBlank()) {
            return AdAuthResult.of(AdAuthResult.Status.USER_NOT_FOUND, "Tài khoản không hợp lệ");
        }
        String key = username.toLowerCase();
        if ("locked.user".equals(key)) {
            return AdAuthResult.of(AdAuthResult.Status.USER_LOCKED, "Tài khoản đã bị khóa trên AD");
        }
        // Moi truong gia lap: chap nhan moi tai khoan AD voi mat khau dung cau hinh.
        // Phan quyen thuc su do bang app_user/user_role quyet dinh (AuthService).
        if (!DEFAULT_PASSWORD.equals(password)) {
            return AdAuthResult.of(AdAuthResult.Status.BAD_CREDENTIALS, "Sai mật khẩu");
        }
        return AdAuthResult.success();
    }

    @Override
    public AdUserProfile getUserProfile(String username) {
        // Tra ho so tu directory gia lap (null neu khong tim thay).
        if (username == null) {
            return null;
        }
        return directory.get(username.toLowerCase());
    }
}
