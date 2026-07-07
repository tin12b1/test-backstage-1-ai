package com.csdl.access.request.dto;

import java.time.LocalDate;
import java.util.List;

/** Thông tin nhóm 05A cho 05B. */
public record EmergencyGroupDto(
    Long systemId,
    String systemName,
    Long databaseId,
    String databaseName,
    LocalDate date,
    Integer shift,
    List<Long> requestIds,
    List<String> accessNos,
    List<DetailSummaryDto> unionDetails
) {}
