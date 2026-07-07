package com.csdl.access.request.dto;

import java.time.LocalDateTime;

/** Kết quả ký. */
public record SignResult(
    boolean success,
    String message,
    String signatureImageUrl,
    LocalDateTime signedAt
) {}
