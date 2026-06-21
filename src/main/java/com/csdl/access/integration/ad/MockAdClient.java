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
    private static final String DEFAULT_PASSWORD = "password";

    private final Map<String, AdUserProfile> directory = new HashMap<>();

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
            return AdAuthResult.of(AdAuthResult.Status.USER_NOT_FOUND, "Tai khoan khong hop le");
        }
        String key = username.toLowerCase();
        if ("locked.user".equals(key)) {
            return AdAuthResult.of(AdAuthResult.Status.USER_LOCKED, "Tai khoan da bi khoa tren AD");
        }
        if (!directory.containsKey(key)) {
            return AdAuthResult.of(AdAuthResult.Status.USER_NOT_FOUND, "Tai khoan khong ton tai tren AD");
        }
        if (!DEFAULT_PASSWORD.equals(password)) {
            return AdAuthResult.of(AdAuthResult.Status.BAD_CREDENTIALS, "Sai mat khau");
        }
        return AdAuthResult.success();
    }

    @Override
    public AdUserProfile getUserProfile(String username) {
        if (username == null) {
            return null;
        }
        return directory.get(username.toLowerCase());
    }
}
