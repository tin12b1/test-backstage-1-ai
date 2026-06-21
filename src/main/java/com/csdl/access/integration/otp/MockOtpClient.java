package com.csdl.access.integration.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * OTP client gia lap: OTP dung khi bang gia tri cau hinh (mac dinh 123456).
 */
@Component
@ConditionalOnProperty(name = "integration.otp.mode", havingValue = "mock", matchIfMissing = true)
public class MockOtpClient implements OtpClient {

    private static final Logger log = LoggerFactory.getLogger(MockOtpClient.class);

    @Value("${integration.otp.mock-value:123456}")
    private String mockValue;

    @Override
    public OtpVerifyResult verify(String username, String otp, String purpose, Long requestId) {
        // Khong log gia tri OTP.
        log.debug("[MOCK-OTP] verify username={} purpose={} requestId={}", username, purpose, requestId);
        if (otp != null && otp.equals(mockValue)) {
            return OtpVerifyResult.success();
        }
        return OtpVerifyResult.failure("Ma OTP khong dung");
    }
}
