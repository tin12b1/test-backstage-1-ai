package com.csdl.access.request.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTO cho đăng ký trước. */
public record PreRegistrationDto(
    Long id,
    Long userId,
    String userName,
    String unitCode,
    LocalDate registerDate,
    Integer shift,
    String requestType,
    Long systemId,
    String systemName,
    Long databaseId,
    String databaseName,
    String objectName,
    String accessRights,
    LocalDateTime signedAt,
    String status
) {}
