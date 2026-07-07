package com.csdl.access.request.dto;

import java.time.LocalDateTime;

/** Response cho auto-save AJAX. */
public record AutoSaveResponse(boolean success, String message, LocalDateTime savedAt) {}
