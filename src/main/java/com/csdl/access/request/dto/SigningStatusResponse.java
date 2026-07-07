package com.csdl.access.request.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Response cho polling trạng thái ký. */
public record SigningStatusResponse(
    Long requestId,
    List<DetailSigningStatus> details,
    LocalDateTime lastUpdated
) {}
