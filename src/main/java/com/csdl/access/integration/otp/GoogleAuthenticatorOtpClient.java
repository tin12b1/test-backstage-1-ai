package com.csdl.access.integration.otp;

import com.csdl.access.domain.AppUser;
import com.csdl.access.domain.UserTotp;
import com.csdl.access.domain.repo.AppUserRepository;
import com.csdl.access.domain.repo.UserTotpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * OTP client dung Google Authenticator (TOTP). Kich hoat khi integration.otp.mode=ga.
 * Thay cho SoftOTP: nguoi dung phai dang ky Google Authenticator truoc khi ky xac nhan.
 */
@Component
@ConditionalOnProperty(name = "integration.otp.mode", havingValue = "ga")
public class GoogleAuthenticatorOtpClient implements OtpClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthenticatorOtpClient.class);

    private final AppUserRepository appUserRepository;
    private final UserTotpRepository userTotpRepository;
    private final TotpService totpService;

    public GoogleAuthenticatorOtpClient(AppUserRepository appUserRepository,
                                        UserTotpRepository userTotpRepository,
                                        TotpService totpService) {
        this.appUserRepository = appUserRepository;
        this.userTotpRepository = userTotpRepository;
        this.totpService = totpService;
    }

    @Override
    public OtpVerifyResult verify(String username, String otp, String purpose, Long requestId) {
        // Khong log gia tri OTP.
        log.debug("[GA-OTP] verify username={} purpose={} requestId={}", username, purpose, requestId);
        // Tim nguoi dung theo username (khong phan biet hoa thuong).
        AppUser user = appUserRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user == null) {
            return OtpVerifyResult.failure("Không tìm thấy người dùng");
        }
        // Nguoi dung phai da dang ky va bat Google Authenticator (co secret).
        Optional<UserTotp> totp = userTotpRepository.findByUserId(user.getId());
        if (!totp.isPresent() || !totp.get().isEnabled()) {
            return OtpVerifyResult.failure("Chưa đăng ký Google Authenticator");
        }
        // Kiem tra ma TOTP dua tren secret da luu.
        if (totpService.verify(totp.get().getSecret(), otp)) {
            return OtpVerifyResult.success();
        }
        return OtpVerifyResult.failure("Mã Google Authenticator không đúng");
    }
}
