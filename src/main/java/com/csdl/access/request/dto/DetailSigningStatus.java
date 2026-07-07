package com.csdl.access.request.dto;

import java.time.LocalDateTime;

/** Trạng thái ký của một dòng chi tiết. */
public record DetailSigningStatus(
    Long detailId,
    Long targetUserId,
    String targetUserName,
    boolean signed,
    LocalDateTime signedAt,
    String signatureImageUrl
) {}
