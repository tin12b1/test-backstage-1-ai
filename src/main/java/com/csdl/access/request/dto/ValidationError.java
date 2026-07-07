package com.csdl.access.request.dto;

/** Lỗi validation. */
public record ValidationError(String field, String code, String message) {}
