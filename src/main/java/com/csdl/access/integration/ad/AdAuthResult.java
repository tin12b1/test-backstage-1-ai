package com.csdl.access.integration.ad;

/** Ket qua xac thuc AD. */
public class AdAuthResult {

    /** Cac trang thai co the co sau khi xac thuc voi AD. */
    public enum Status {
        SUCCESS,          // xac thuc thanh cong
        BAD_CREDENTIALS,  // sai tai khoan hoac mat khau
        USER_LOCKED,      // tai khoan bi khoa tren AD
        USER_NOT_FOUND,   // tai khoan khong ton tai
        CONNECTION_ERROR  // loi ket noi/he thong xac thuc
    }

    /** Trang thai ket qua xac thuc. */
    private final Status status;
    /** Thong diep mo ta (dung de hien thi/ghi log). */
    private final String message;

    private AdAuthResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    /** Tao ket qua thanh cong. */
    public static AdAuthResult success() {
        return new AdAuthResult(Status.SUCCESS, "OK");
    }

    /** Tao ket qua voi trang thai va thong diep tuy y. */
    public static AdAuthResult of(Status status, String message) {
        return new AdAuthResult(status, message);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    /** True neu trang thai la SUCCESS. */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
