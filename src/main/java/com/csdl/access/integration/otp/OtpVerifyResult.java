package com.csdl.access.integration.otp;

/** Ket qua xac thuc OTP. */
public class OtpVerifyResult {

    /** True neu OTP hop le. */
    private final boolean success;
    /** Thong diep mo ta (dung de hien thi/ghi log). */
    private final String message;
    /** Id giao dich OTP da luu (gan sau khi audit). */
    private Long transactionId;

    private OtpVerifyResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /** Tao ket qua thanh cong. */
    public static OtpVerifyResult success() {
        return new OtpVerifyResult(true, "OK");
    }

    /** Tao ket qua that bai voi thong diep loi. */
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
