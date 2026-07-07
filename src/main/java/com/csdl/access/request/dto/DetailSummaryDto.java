package com.csdl.access.request.dto;

/** Tóm tắt dòng chi tiết. */
public record DetailSummaryDto(
    Long detailId,
    String objectName,
    String accessRights,
    String targetUserName
) {}
