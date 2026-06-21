package com.csdl.access.integration.ad;

/** Ket qua xac thuc AD. */
public class AdAuthResult {

    public enum Status {
        SUCCESS,
        BAD_CREDENTIALS,
        USER_LOCKED,
        USER_NOT_FOUND,
        CONNECTION_ERROR
    }

    private final Status status;
    private final String message;

    private AdAuthResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static AdAuthResult success() {
        return new AdAuthResult(Status.SUCCESS, "OK");
    }

    public static AdAuthResult of(Status status, String message) {
        return new AdAuthResult(status, message);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
