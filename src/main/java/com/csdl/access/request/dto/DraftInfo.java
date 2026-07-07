package com.csdl.access.request.dto;

import java.time.LocalDateTime;

/** Thông tin bản nháp phiếu. */
public record DraftInfo(
    Long requestId,
    String requestType,
    String requestCode,
    LocalDateTime lastSavedAt
) {}
