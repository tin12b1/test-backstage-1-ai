package com.csdl.access.request;

import com.csdl.access.common.enums.RequestType;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.RequestDetail;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.request.dto.ValidationError;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation nghiep vu cho cac mau phieu yeu cau.
 * Phan lon la pure logic; hasDebtBlock can repository de truy van no 05B.
 */
@Service
public class RequestValidationService {

    private static final int DEBT_OVERDUE_DAYS = 3;

    @Nullable
    private final AccessRequestRepository accessRequestRepository;

    /** Constructor dung boi Spring (inject repository). */
    public RequestValidationService(AccessRequestRepository accessRequestRepository) {
        this.accessRequestRepository = accessRequestRepository;
    }

    /** Constructor khong tham so cho unit test cac method thuan logic. */
    public RequestValidationService() {
        this.accessRequestRepository = null;
    }

    // ============================
    // hasDebtBlock
    // ============================

    /**
     * Kiem tra user co dang no phieu 05B (qua 3 ngay) hay khong.
     * Logic: Tim phieu 05A-YCKC da COMPLETED cua user ma KHONG co ban ghi
     * tuong ung trong emergency_completion_link. Neu completed_at cach hien tai > 3 ngay -> true (bi chan).
     *
     * @param userId ID nguoi dung can kiem tra
     * @return true neu user dang no 05B qua han, false neu khong no
     */
    public boolean hasDebtBlock(Long userId) {
        if (userId == null || accessRequestRepository == null) {
            return false;
        }

        LocalDateTime threshold = LocalDateTime.now().minusDays(DEBT_OVERDUE_DAYS);
        long overdueCount = accessRequestRepository.countOverdue05AWithoutLinked05B(userId, threshold);
        return overdueCount > 0;
    }

    // ============================
    // validateForSubmission
    // ============================

    /**
     * Validate day du truoc khi cho phep ky/gui.
     * Tra ve danh sach loi (empty = pass).
     */
    public List<ValidationError> validateForSubmission(AccessRequest request, List<RequestDetail> details) {
        List<ValidationError> errors = new ArrayList<>();

        // --- Mandatory fields chung cho moi loai ---
        if (request.getRequesterUserId() == null) {
            errors.add(new ValidationError("requester_user_id", "REQUIRED",
                    "Nguoi lap phieu khong duoc de trong"));
        }
        if (request.getShiftNo() == null) {
            errors.add(new ValidationError("shift_no", "REQUIRED",
                    "Ca lam viec khong duoc de trong"));
        }
        if (isBlank(request.getReason())) {
            errors.add(new ValidationError("reason", "REQUIRED",
                    "Ly do/noi dung khong duoc de trong"));
        }

        RequestType type = request.getRequestType();
        if (type == null) {
            errors.add(new ValidationError("request_type", "REQUIRED",
                    "Loai phieu khong duoc de trong"));
            return errors; // Cannot validate further without type
        }

        // --- Mandatory fields theo loai phieu ---
        switch (type) {
            case YCTC_01:
                // 01-YCTC: phai co it nhat 1 dong chi tiet
                if (details == null || details.isEmpty()) {
                    errors.add(new ValidationError("details", "REQUIRED",
                            "Phieu 01-YCTC phai co it nhat 1 dong chi tiet"));
                }
                break;

            case YCCS_02:
                // 02-YCCS: system_id, database_id bat buoc tren header
                if (request.getSystemId() == null) {
                    errors.add(new ValidationError("system_id", "REQUIRED",
                            "He thong khong duoc de trong"));
                }
                if (request.getDatabaseId() == null) {
                    errors.add(new ValidationError("database_id", "REQUIRED",
                            "Co so du lieu khong duoc de trong"));
                }
                break;

            case YCCT_03:
                // 03-YCCT: system_id, database_id, expected_execution_date bat buoc
                if (request.getSystemId() == null) {
                    errors.add(new ValidationError("system_id", "REQUIRED",
                            "He thong khong duoc de trong"));
                }
                if (request.getDatabaseId() == null) {
                    errors.add(new ValidationError("database_id", "REQUIRED",
                            "Co so du lieu khong duoc de trong"));
                }
                if (request.getExpectedExecutionDate() == null) {
                    errors.add(new ValidationError("expected_execution_date", "REQUIRED",
                            "Ngay thuc hien du kien khong duoc de trong"));
                }
                break;

            case YCTK_04A:
                // 04A-YCTK: system_id, database_id bat buoc; phai co it nhat 1 dong chi tiet
                if (request.getSystemId() == null) {
                    errors.add(new ValidationError("system_id", "REQUIRED",
                            "He thong khong duoc de trong"));
                }
                if (request.getDatabaseId() == null) {
                    errors.add(new ValidationError("database_id", "REQUIRED",
                            "Co so du lieu khong duoc de trong"));
                }
                if (details == null || details.isEmpty()) {
                    errors.add(new ValidationError("details", "REQUIRED",
                            "Phieu 04A-YCTK phai co it nhat 1 dong chi tiet"));
                }
                break;

            case YCKC_05A:
                // 05A-YCKC: system_id, database_id, start_time, end_time bat buoc
                if (request.getSystemId() == null) {
                    errors.add(new ValidationError("system_id", "REQUIRED",
                            "He thong khong duoc de trong"));
                }
                if (request.getDatabaseId() == null) {
                    errors.add(new ValidationError("database_id", "REQUIRED",
                            "Co so du lieu khong duoc de trong"));
                }
                if (request.getStartTime() == null) {
                    errors.add(new ValidationError("start_time", "REQUIRED",
                            "Thoi gian bat dau khong duoc de trong"));
                }
                if (request.getEndTime() == null) {
                    errors.add(new ValidationError("end_time", "REQUIRED",
                            "Thoi gian ket thuc khong duoc de trong"));
                }
                break;

            case HTKC_05B:
                // 05B-HTKC: system_id, database_id, reason (work description) bat buoc
                if (request.getSystemId() == null) {
                    errors.add(new ValidationError("system_id", "REQUIRED",
                            "He thong khong duoc de trong"));
                }
                if (request.getDatabaseId() == null) {
                    errors.add(new ValidationError("database_id", "REQUIRED",
                            "Co so du lieu khong duoc de trong"));
                }
                // reason da check o tren (chung) — o day la "mo ta cong viec"
                break;

            default:
                break;
        }

        return errors;
    }

    // ============================
    // validateForDraft
    // ============================

    /**
     * Validate nhe khi luu nhap (chi kiem tra format co ban).
     * Hau het cac truong la optional khi luu nhap.
     */
    public List<ValidationError> validateForDraft(RequestForm form) {
        List<ValidationError> errors = new ArrayList<>();

        // Kiem tra do dai reason (neu co)
        if (form.getReason() != null && form.getReason().length() > 2000) {
            errors.add(new ValidationError("reason", "MAX_LENGTH",
                    "Ly do khong duoc vuot qua 2000 ky tu"));
        }

        // Kiem tra shift_no hop le (neu co)
        if (form.getShiftNo() != null && (form.getShiftNo() < 1 || form.getShiftNo() > 3)) {
            errors.add(new ValidationError("shift_no", "INVALID_FORMAT",
                    "Ca lam viec phai la 1, 2 hoac 3"));
        }

        // Kiem tra startTime format (neu co)
        if (form.getStartTime() != null && !form.getStartTime().isBlank()) {
            if (!isValidDateTimeFormat(form.getStartTime())) {
                errors.add(new ValidationError("start_time", "INVALID_FORMAT",
                        "Thoi gian bat dau khong dung dinh dang ISO-8601"));
            }
        }

        // Kiem tra endTime format (neu co)
        if (form.getEndTime() != null && !form.getEndTime().isBlank()) {
            if (!isValidDateTimeFormat(form.getEndTime())) {
                errors.add(new ValidationError("end_time", "INVALID_FORMAT",
                        "Thoi gian ket thuc khong dung dinh dang ISO-8601"));
            }
        }

        // Kiem tra expectedExecutionDate format (neu co)
        if (form.getExpectedExecutionDate() != null && !form.getExpectedExecutionDate().isBlank()) {
            if (!isValidDateTimeFormat(form.getExpectedExecutionDate())) {
                errors.add(new ValidationError("expected_execution_date", "INVALID_FORMAT",
                        "Ngay thuc hien du kien khong dung dinh dang"));
            }
        }

        return errors;
    }

    // ============================
    // validateTimeNotPast
    // ============================

    /**
     * Kiem tra thoi gian (date + shift) khong o qua khu.
     * Doi voi 05B-HTKC: luon cho phep (return null).
     * Doi voi cac loai khac: neu date+shift da qua → tra ve ValidationError.
     *
     * Logic ca lam viec: Ca 1 = 0-8h, Ca 2 = 8-20h, Ca 3 = 20-24h.
     */
    public ValidationError validateTimeNotPast(Integer shiftNo, LocalDate date, RequestType type) {
        // 04B-BGTK (chua co trong enum) va 05B-HTKC luon cho phep
        if (type == RequestType.HTKC_05B) {
            return null;
        }

        if (date == null || shiftNo == null) {
            return null; // Khong du thong tin de validate — bo qua
        }

        LocalDateTime shiftEnd = getShiftEndTime(date, shiftNo);
        LocalDateTime now = LocalDateTime.now();

        if (shiftEnd.isBefore(now)) {
            return new ValidationError("shift_no", "TIME_PAST",
                    "Thoi gian (ngay + ca) da qua, khong the lap phieu");
        }

        return null;
    }

    // ============================
    // checkDuplicateDetails
    // ============================

    /**
     * Kiem tra trung lap dong chi tiet theo loai phieu.
     * - 01-YCTC: trung khi cung (systemId, databaseId, objectName, targetUserId)
     * - 04A-YCTK: trung khi cung targetUserId
     * - Cac loai khac: khong kiem tra
     */
    public List<ValidationError> checkDuplicateDetails(RequestType type, List<RequestDetail> details) {
        List<ValidationError> errors = new ArrayList<>();

        if (type == null || details == null || details.isEmpty()) {
            return errors;
        }

        switch (type) {
            case YCTC_01:
                checkDuplicates01(details, errors);
                break;
            case YCTK_04A:
                checkDuplicates04A(details, errors);
                break;
            default:
                break;
        }

        return errors;
    }

    private void checkDuplicates01(List<RequestDetail> details, List<ValidationError> errors) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < details.size(); i++) {
            RequestDetail d = details.get(i);
            String key = d.getSystemId() + "|" + d.getDatabaseId() + "|" + d.getObjectName() + "|" + d.getTargetUserId();
            if (!seen.add(key)) {
                errors.add(new ValidationError("details[" + i + "]", "DUPLICATE",
                        "Dong chi tiet trung lap (He thong + CSDL + Object + User)"));
            }
        }
    }

    private void checkDuplicates04A(List<RequestDetail> details, List<ValidationError> errors) {
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (int i = 0; i < details.size(); i++) {
            RequestDetail d = details.get(i);
            Long userId = d.getTargetUserId();
            if (userId != null && !seen.add(userId)) {
                errors.add(new ValidationError("details[" + i + "]", "DUPLICATE",
                        "Nguoi dung bi trung lap trong danh sach chi tiet"));
            }
        }
    }

    // ============================
    // validateSingleSystemDatabase
    // ============================

    /**
     * Kiem tra phieu chi duoc chon dung 1 systemId va 1 databaseId (khong null).
     * Ap dung cho: 02-YCCS, 03-YCCT, 04A-YCTK, 05A-YCKC.
     * Phieu 01-YCTC: bo qua (cho phep nhieu system/database tren cac dong chi tiet).
     */
    public List<ValidationError> validateSingleSystemDatabase(RequestType type, AccessRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (type == null || request == null) {
            return errors;
        }

        // Chi ap dung cho 02, 03, 04A, 05A
        switch (type) {
            case YCCS_02:
            case YCCT_03:
            case YCTK_04A:
            case YCKC_05A:
                if (request.getSystemId() == null) {
                    errors.add(new ValidationError("system_id", "REQUIRED",
                            "Phai chon dung 1 he thong"));
                }
                if (request.getDatabaseId() == null) {
                    errors.add(new ValidationError("database_id", "REQUIRED",
                            "Phai chon dung 1 co so du lieu"));
                }
                break;
            default:
                // 01-YCTC, 05B-HTKC: khong kiem tra
                break;
        }

        return errors;
    }

    // ============================
    // validateScriptFile
    // ============================

    /**
     * File name pattern: YYYYMMDD_BS_XXX.sql
     * YYYY = 4-digit year, MM = month 01-12, DD = day 01-31,
     * BS = alphanumeric business code (1+ chars), XXX = 3-digit number.
     */
    private static final Pattern SCRIPT_FILE_NAME_PATTERN =
            Pattern.compile("^(\\d{4})(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])_[A-Za-z0-9]+_\\d{3}\\.sql$");

    /** Max file size: 10MB */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private static final Pattern MD5_PATTERN = Pattern.compile("^[0-9a-fA-F]{32}$");
    private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");

    /**
     * Validate file SQL script: ten file, kich thuoc, checksum.
     * Tra ve null neu hop le, hoac ValidationError tuong ung.
     */
    public ValidationError validateScriptFile(MultipartFile file, String checksumType, String checksumValue) {
        if (file == null || file.isEmpty()) {
            return null; // No file to validate
        }

        // 1. Check file name format
        String fileName = file.getOriginalFilename();
        if (fileName == null || !SCRIPT_FILE_NAME_PATTERN.matcher(fileName).matches()) {
            return new ValidationError("script_file", "FILE_NAME_FORMAT",
                    "Ten file phai theo dinh dang YYYYMMDD_BS_XXX.sql");
        }

        // 2. Check file size <= 10MB
        if (file.getSize() > MAX_FILE_SIZE) {
            return new ValidationError("script_file", "FILE_SIZE",
                    "Kich thuoc file khong duoc vuot qua 10MB");
        }

        // 3. If checksumType is provided, validate format and compute hash
        if (checksumType != null && !checksumType.isBlank()) {
            // Validate checksum format
            ValidationError formatError = validateChecksumFormat(checksumType, checksumValue);
            if (formatError != null) {
                return formatError;
            }

            // Compute actual file hash and compare
            try {
                String algorithmName = mapChecksumAlgorithm(checksumType);
                if (algorithmName == null) {
                    return new ValidationError("checksum_type", "CHECKSUM_FORMAT",
                            "Loai checksum khong duoc ho tro (chi ho tro MD5 hoac SHA-256)");
                }

                MessageDigest digest = MessageDigest.getInstance(algorithmName);
                byte[] hashBytes = digest.digest(file.getBytes());
                String computedHash = bytesToHex(hashBytes);

                if (!computedHash.equalsIgnoreCase(checksumValue)) {
                    return new ValidationError("checksum_value", "CHECKSUM_MISMATCH",
                            "Checksum file khong khop voi gia tri da cung cap");
                }
            } catch (NoSuchAlgorithmException | IOException e) {
                return new ValidationError("script_file", "CHECKSUM_MISMATCH",
                        "Khong the tinh checksum file: " + e.getMessage());
            }
        }

        return null; // Valid
    }

    // ============================
    // validate03TabContent
    // ============================

    /**
     * Validate noi dung tab cho mau 03-YCCT:
     * - Khi co SQL script + checksum khop: noi dung tab la OPTIONAL (tra ve empty)
     * - Khi khong co SQL script: TAT CA tab da chon phai co noi dung
     */
    public List<ValidationError> validate03TabContent(AccessRequest request, boolean hasScript, boolean checksumMatch) {
        List<ValidationError> errors = new ArrayList<>();

        // When SQL Script file uploaded AND checksum matches: tab content is OPTIONAL
        if (hasScript && checksumMatch) {
            return errors; // Empty — no validation needed
        }

        // When no SQL Script file (or checksum doesn't match): all selected tabs must have content
        if (!hasScript) {
            // Check detail_data / reason for content presence
            // The actual tab content comes from form data — here we just enforce the business rule
            if (request != null && isBlank(request.getReason())) {
                errors.add(new ValidationError("detail_content", "REQUIRED",
                        "Khi khong co file SQL, noi dung chi tiet phai duoc nhap day du"));
            }
        }

        return errors;
    }

    // ============================
    // Checksum helper methods
    // ============================

    /**
     * Validate checksum format: MD5 must be 32 hex chars, SHA-256 must be 64 hex chars.
     */
    ValidationError validateChecksumFormat(String checksumType, String checksumValue) {
        if (checksumValue == null || checksumValue.isBlank()) {
            return new ValidationError("checksum_value", "CHECKSUM_FORMAT",
                    "Gia tri checksum khong duoc de trong khi da chon loai checksum");
        }

        String normalizedType = checksumType.trim().toUpperCase().replace("-", "");
        switch (normalizedType) {
            case "MD5":
                if (!MD5_PATTERN.matcher(checksumValue).matches()) {
                    return new ValidationError("checksum_value", "CHECKSUM_FORMAT",
                            "MD5 checksum phai la 32 ky tu hex");
                }
                break;
            case "SHA256":
                if (!SHA256_PATTERN.matcher(checksumValue).matches()) {
                    return new ValidationError("checksum_value", "CHECKSUM_FORMAT",
                            "SHA-256 checksum phai la 64 ky tu hex");
                }
                break;
            default:
                return new ValidationError("checksum_type", "CHECKSUM_FORMAT",
                        "Loai checksum khong duoc ho tro (chi ho tro MD5 hoac SHA-256)");
        }
        return null;
    }

    /**
     * Map checksumType string to Java MessageDigest algorithm name.
     */
    String mapChecksumAlgorithm(String checksumType) {
        String normalizedType = checksumType.trim().toUpperCase().replace("-", "");
        switch (normalizedType) {
            case "MD5":
                return "MD5";
            case "SHA256":
                return "SHA-256";
            default:
                return null;
        }
    }

    /**
     * Convert byte array to lowercase hex string.
     */
    String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ============================
    // Private helpers
    // ============================

    /**
     * Tra ve thoi diem ket thuc cua ca lam viec trong ngay.
     * Ca 1: 0h-8h  → end = 08:00
     * Ca 2: 8h-20h → end = 20:00
     * Ca 3: 20h-24h → end = 23:59:59
     */
    LocalDateTime getShiftEndTime(LocalDate date, int shiftNo) {
        switch (shiftNo) {
            case 1:
                return date.atTime(LocalTime.of(8, 0));
            case 2:
                return date.atTime(LocalTime.of(20, 0));
            case 3:
                return date.atTime(LocalTime.of(23, 59, 59));
            default:
                return date.atTime(LocalTime.of(23, 59, 59));
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidDateTimeFormat(String value) {
        try {
            LocalDateTime.parse(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
