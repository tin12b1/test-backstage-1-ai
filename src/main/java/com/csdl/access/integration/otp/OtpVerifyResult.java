package com.csdl.access.integration.otp;

/** Ket qua xac thuc OTP. */
public class OtpVerifyResult {

    private final boolean success;
    private final String message;
    private Long transactionId;

    private OtpVerifyResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static OtpVerifyResult success() {
        return new OtpVerifyResult(true, "OK");
    }

    public static OtpVerifyResult failure(String message) {
        return new OtpVerifyResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
}
