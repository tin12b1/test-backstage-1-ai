package com.csdl.access.common.exception;

/** Loi nghiep vu hien thi than thien cho nguoi dung. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
