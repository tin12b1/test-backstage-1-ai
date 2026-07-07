package com.csdl.access.request.dto;

/** Response cho upload file. */
public record FileUploadResponse(
    boolean success,
    String fileName,
    long fileSize,
    String computedChecksum,
    String checksumType
) {}
