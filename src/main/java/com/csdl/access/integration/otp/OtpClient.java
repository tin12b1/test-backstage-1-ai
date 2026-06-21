package com.csdl.access.integration.otp;

/**
 * Interface tich hop SoftOTP (features/integrations.md muc 4).
 */
public interface OtpClient {

    /** Xac thuc OTP khi nguoi dung ky xac nhan. Khong duoc log OTP dang ro. */
    OtpVerifyResult verify(String username, String otp, String purpose, Long requestId);
}
