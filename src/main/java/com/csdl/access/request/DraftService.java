package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.request.dto.AutoSaveResult;
import com.csdl.access.request.dto.DraftInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quan ly luu nhap va auto-save cho phieu yeu cau (Requirement 2: Luu nhap va Auto-save).
 */
@Service
public class DraftService {

    private final AccessRequestRepository requestRepository;
    private final RequestCodeGenerator codeGenerator;

    public DraftService(AccessRequestRepository requestRepository,
                        RequestCodeGenerator codeGenerator) {
        this.requestRepository = requestRepository;
        this.codeGenerator = codeGenerator;
    }

    /**
     * Luu nhap phieu yeu cau.
     * - Neu form khong co id: tao moi AccessRequest voi status DRAFT.
     * - Neu form co id: load phieu hien tai, kiem tra trang thai, cap nhat fields.
     */
    @Transactional
    public AccessRequest saveDraft(RequestForm form, UserSession session) {
        if (form.getId() == null) {
            return createNewDraft(form, session);
        }
        return updateExistingDraft(form, session);
    }

    /**
     * Auto-save: luu im lang, khong throw exception.
     * Tra ve ket qua cho biet co luu hay khong.
     */
    @Transactional
    public AutoSaveResult autoSave(Long requestId, RequestForm form, UserSession session) {
        try {
            AccessRequest request = requestRepository.findById(requestId).orElse(null);
            if (request == null) {
                return new AutoSaveResult(false, "Khong tim thay yeu cau");
            }
            if (request.getStatus() != RequestStatus.DRAFT) {
                return new AutoSaveResult(false, "Phieu khong o trang thai nhap");
            }

            // Dirty check: so sanh content hash
            String newHash = computeContentHash(form);
            if (newHash.equals(request.getContentHash())) {
                return new AutoSaveResult(false, "Khong co thay doi");
            }

            // Co thay doi -> cap nhat
            applyFormFields(request, form);
            request.setContentHash(newHash);
            requestRepository.save(request);
            return new AutoSaveResult(true, null);
        } catch (Exception e) {
            return new AutoSaveResult(false, "Loi khi auto-save: " + e.getMessage());
        }
    }

    /**
     * Tra ve danh sach phieu nhap cua nguoi dung, sap xep theo ngay tao giam dan.
     */
    public List<DraftInfo> findDraftsForUser(Long userId) {
        List<AccessRequest> drafts = requestRepository
                .findByRequesterUserIdAndStatusOrderByCreatedAtDesc(userId, RequestStatus.DRAFT);
        return drafts.stream()
                .map(r -> new DraftInfo(
                        r.getId(),
                        r.getRequestType() != null ? r.getRequestType().getFormCode() : null,
                        r.getRequestCode(),
                        r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // ─── Private helpers ──────────────────────────────────────────────

    private AccessRequest createNewDraft(RequestForm form, UserSession session) {
        RequestType type = parseType(form.getRequestType());

        AccessRequest request = new AccessRequest();
        request.setRequestType(type);
        request.setStatus(RequestStatus.DRAFT);
        request.setRequesterUserId(session.getUserId());
        request.setRequesterUnitId(session.getUnitId());
        request.setRequesterDepartmentId(session.getDepartmentId());
        applyFormFields(request, form);
        request.setContentHash(computeContentHash(form));
        request.setRequestCode(codeGenerator.generate(session.getUnitId()));
        return requestRepository.save(request);
    }

    private AccessRequest updateExistingDraft(RequestForm form, UserSession session) {
        AccessRequest request = requestRepository.findById(form.getId())
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessException("Chi cho phep chinh sua khi phieu o trang thai Luu nhap");
        }

        applyFormFields(request, form);
        request.setContentHash(computeContentHash(form));
        return requestRepository.save(request);
    }

    private void applyFormFields(AccessRequest request, RequestForm form) {
        if (form.getRequestType() != null) {
            request.setRequestType(parseType(form.getRequestType()));
        }
        request.setShiftNo(form.getShiftNo());
        request.setAccessNo(form.getAccessNo());
        request.setSystemId(form.getSystemId());
        request.setDatabaseId(form.getDatabaseId());
        request.setReason(form.getReason());
        request.setStartTime(parseDateTime(form.getStartTime()));
        request.setEndTime(parseDateTime(form.getEndTime()));
        request.setExpectedExecutionDate(parseDateTime(form.getExpectedExecutionDate()));
    }

    /**
     * Tinh MD5 hash cua noi dung form de phuc vu dirty check.
     * Gom cac truong: requestType, shiftNo, systemId, databaseId, reason, accessNo,
     * startTime, endTime, expectedExecutionDate.
     */
    private String computeContentHash(RequestForm form) {
        StringBuilder sb = new StringBuilder();
        sb.append(nullSafe(form.getRequestType()));
        sb.append("|").append(nullSafe(form.getShiftNo()));
        sb.append("|").append(nullSafe(form.getSystemId()));
        sb.append("|").append(nullSafe(form.getDatabaseId()));
        sb.append("|").append(nullSafe(form.getReason()));
        sb.append("|").append(nullSafe(form.getAccessNo()));
        sb.append("|").append(nullSafe(form.getStartTime()));
        sb.append("|").append(nullSafe(form.getEndTime()));
        sb.append("|").append(nullSafe(form.getExpectedExecutionDate()));

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 luon co san trong moi JVM
            throw new IllegalStateException("MD5 not available", e);
        }
    }

    private String nullSafe(Object value) {
        return value == null ? "" : value.toString();
    }

    private RequestType parseType(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("Loai phieu khong duoc de trong");
        }
        try {
            return RequestType.valueOf(code);
        } catch (Exception e) {
            throw new BusinessException("Loai phieu khong hop le: " + code);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
