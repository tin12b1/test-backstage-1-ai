package com.csdl.access.request.dto;

import java.time.LocalDateTime;

/** Tóm tắt phiếu yêu cầu. */
public record RequestSummaryDto(
    Long id,
    String requestCode,
    String requestType,
    String systemName,
    String databaseName,
    String status,
    LocalDateTime createdAt
) {}
