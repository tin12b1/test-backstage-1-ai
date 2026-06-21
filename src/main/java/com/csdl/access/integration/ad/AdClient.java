package com.csdl.access.integration.ad;

/**
 * Interface tich hop AD (features/integrations.md muc 3).
 */
public interface AdClient {

    /** Xac thuc user/password AD. Khong duoc log mat khau. */
    AdAuthResult authenticate(String username, String password);

    /** Lay thong tin user: ho ten, email, dien thoai, don vi/phong neu co. */
    AdUserProfile getUserProfile(String username);
}
