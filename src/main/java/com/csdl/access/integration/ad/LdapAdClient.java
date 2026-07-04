package com.csdl.access.integration.ad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * AD client that su dung JNDI LDAP bind (khong can them dependency).
 *
 * Kich hoat khi integration.ad.mode=ldap. Thong tin ket noi lay tu cau hinh,
 * khong hard-code (features/integrations.md muc 8).
 */
@Component
@ConditionalOnProperty(name = "integration.ad.mode", havingValue = "ldap")
public class LdapAdClient implements AdClient {

    private static final Logger log = LoggerFactory.getLogger(LdapAdClient.class);

    /** URL server LDAP (vi du ldap://host:389). */
    @Value("${integration.ad.url}")
    private String url;

    /** Domain AD ghep vao truoc username khi bind. De trong neu khong can. */
    @Value("${integration.ad.domain:}")
    private String domain;

    /** Thoi gian cho ket noi/doc LDAP (ms). */
    @Value("${integration.ad.timeout-ms:5000}")
    private String timeoutMs;

    @Override
    public AdAuthResult authenticate(String username, String password) {
        // Kiem tra dau vao truoc khi bind.
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return AdAuthResult.of(AdAuthResult.Status.BAD_CREDENTIALS, "Thieu thong tin dang nhap");
        }
        // Ghep domain neu co cau hinh (dinh dang DOMAIN\\username).
        String principal = domain == null || domain.isBlank() ? username : domain + "\\" + username;
        // Chuan bi moi truong JNDI cho bind LDAP "simple" voi credential nguoi dung.
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put("com.sun.jndi.ldap.connect.timeout", timeoutMs);
        env.put("com.sun.jndi.ldap.read.timeout", timeoutMs);

        InitialDirContext ctx = null;
        try {
            // Bind thanh cong = tai khoan/mat khau hop le.
            ctx = new InitialDirContext(env);
            return AdAuthResult.success();
        } catch (javax.naming.AuthenticationException e) {
            // Khong log password.
            log.warn("[LDAP] Xac thuc that bai cho user={}", username);
            return AdAuthResult.of(AdAuthResult.Status.BAD_CREDENTIALS, "Sai tai khoan hoac mat khau");
        } catch (Exception e) {
            log.error("[LDAP] Loi ket noi AD: {}", e.getMessage());
            return AdAuthResult.of(AdAuthResult.Status.CONNECTION_ERROR, "Khong ket noi duoc AD");
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception ignore) {
                    // bo qua
                }
            }
        }
    }

    @Override
    public AdUserProfile getUserProfile(String username) {
        // Trien khai tra cuu thuoc tinh chi tiet khi co schema AD cu the.
        AdUserProfile profile = new AdUserProfile();
        profile.setUsername(username);
        return profile;
    }
}
