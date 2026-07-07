package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.PreRegistrationRequest;
import com.csdl.access.domain.repo.PreRegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quan ly vong doi dang ky truoc yeu cau chi tiet (01-YCTC).
 * Bao gom CRUD, nap tu dong vao form, chuyen trang thai, va het han tu dong.
 */
@Service
public class PreRegistrationService {

    private static final String STATUS_UNUSED = "UNUSED";
    private static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String STATUS_USED = "USED";

    private final PreRegistrationRepository preRegistrationRepository;
    private final RequestValidationService requestValidationService;

    public PreRegistrationService(PreRegistrationRepository preRegistrationRepository,
                                  RequestValidationService requestValidationService) {
        this.preRegistrationRepository = preRegistrationRepository;
        this.requestValidationService = requestValidationService;
    }

    // ============================
    // CRUD Operations
    // ============================

    /**
     * Danh sach dang ky truoc cua user hien tai, phan trang.
     */
    public Page<PreRegistrationRequest> listByUser(Long userId, Pageable pageable) {
        return preRegistrationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Danh sach dang ky truoc cua user hien tai, loai tru ban ghi EXPIRED, phan trang.
     */
    public Page<PreRegistrationRequest> listByUserExcludingExpired(Long userId, Pageable pageable) {
        return preRegistrationRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, "EXPIRED", pageable);
    }

    /**
     * Tao moi dang ky truoc. Kiem tra trung lap va validate thoi gian.
     * requestType duoc tu dong xac dinh tu accessRights:
     * - Chi SELECT -> "Truy vấn"
     * - Co INSERT/UPDATE/DELETE -> "Chỉnh sửa"
     */
    @Transactional
    public PreRegistrationRequest create(PreRegistrationForm form, UserSession session) {
        validateTimeNotPast(form.getRegisterDate(), form.getShift());
        validateNoDuplicate(session.getUserId(), form);

        // Tu dong xac dinh requestType tu accessRights neu chua co
        String requestType = form.getRequestType();
        if (requestType == null || requestType.isBlank()) {
            requestType = deriveRequestType(form.getAccessRights());
        }

        PreRegistrationRequest entity = new PreRegistrationRequest();
        entity.setUserId(session.getUserId());
        entity.setUnitCode(session.getUnitId() != null ? session.getUnitId().toString() : null);
        entity.setRegisterDate(form.getRegisterDate());
        entity.setShift(form.getShift());
        entity.setRequestType(requestType);
        entity.setSystemId(form.getSystemId());
        entity.setDatabaseId(form.getDatabaseId());
        entity.setObjectName(form.getObjectName());
        entity.setAccessRights(form.getAccessRights());
        entity.setStatus(STATUS_UNUSED);

        return preRegistrationRepository.save(entity);
    }

    /**
     * Cap nhat dang ky truoc. Chi cho phep khi status = UNUSED, yeu cau ky lai.
     */
    @Transactional
    public PreRegistrationRequest update(Long id, PreRegistrationForm form, UserSession session) {
        PreRegistrationRequest entity = findOwnedRecord(id, session);

        if (!STATUS_UNUSED.equals(entity.getStatus())) {
            throw new BusinessException("Chi duoc sua dang ky truoc khi trang thai la 'Chua dung'");
        }

        validateTimeNotPast(form.getRegisterDate(), form.getShift());

        // Kiem tra trung lap chi khi thay doi cac truong identity
        if (hasIdentityChanged(entity, form)) {
            validateNoDuplicateExcluding(session.getUserId(), form, id);
        }

        entity.setRegisterDate(form.getRegisterDate());
        entity.setShift(form.getShift());
        // Tu dong xac dinh requestType tu accessRights neu chua co
        String requestType = form.getRequestType();
        if (requestType == null || requestType.isBlank()) {
            requestType = deriveRequestType(form.getAccessRights());
        }
        entity.setRequestType(requestType);
        entity.setSystemId(form.getSystemId());
        entity.setDatabaseId(form.getDatabaseId());
        entity.setObjectName(form.getObjectName());
        entity.setAccessRights(form.getAccessRights());
        // Xoa chu ky cu — bat buoc ky lai
        entity.setSignedAt(null);
        entity.setSignatureImageId(null);

        return preRegistrationRepository.save(entity);
    }

    /**
     * Xoa vinh vien dang ky truoc. Chi cho phep khi status = UNUSED.
     */
    @Transactional
    public void delete(Long id, UserSession session) {
        PreRegistrationRequest entity = findOwnedRecord(id, session);

        if (!STATUS_UNUSED.equals(entity.getStatus())) {
            throw new BusinessException("Chi duoc xoa dang ky truoc khi trang thai la 'Chua dung'");
        }

        preRegistrationRepository.delete(entity);
    }

    /**
     * Sao chep dang ky truoc sang ngay/ca moi.
     */
    @Transactional
    public PreRegistrationRequest clone(Long id, LocalDate targetDate, int targetShift, UserSession session) {
        PreRegistrationRequest source = findOwnedRecord(id, session);

        validateTimeNotPast(targetDate, targetShift);

        // Kiem tra trung lap o ngay/ca moi
        boolean exists = preRegistrationRepository
                .existsByUserIdAndRegisterDateAndShiftAndSystemIdAndDatabaseIdAndObjectNameAndAccessRights(
                        session.getUserId(), targetDate, targetShift,
                        source.getSystemId(), source.getDatabaseId(),
                        source.getObjectName(), source.getAccessRights());
        if (exists) {
            throw new BusinessException("Da ton tai dang ky truoc trung lap cho ngay va ca nay");
        }

        PreRegistrationRequest clone = new PreRegistrationRequest();
        clone.setUserId(session.getUserId());
        clone.setUnitCode(source.getUnitCode());
        clone.setRegisterDate(targetDate);
        clone.setShift(targetShift);
        clone.setRequestType(source.getRequestType());
        clone.setSystemId(source.getSystemId());
        clone.setDatabaseId(source.getDatabaseId());
        clone.setObjectName(source.getObjectName());
        clone.setAccessRights(source.getAccessRights());
        clone.setStatus(STATUS_UNUSED);

        return preRegistrationRepository.save(clone);
    }

    // ============================
    // Load for Form 01-YCTC
    // ============================

    /**
     * Nap dang ky truoc vao phieu 01-YCTC.
     * - Neu requestType = "Truy vấn": chi nap ban ghi co requestType = "Truy vấn" hoac NULL (va accessRights chi SELECT) va status = UNUSED
     * - Neu requestType = "Chỉnh sửa": nap TAT CA ban ghi voi status = UNUSED
     */
    public List<PreRegistrationRequest> loadForForm01(String unitCode, LocalDate date, int shift, String requestType) {
        if ("Truy vấn".equals(requestType)) {
            // Load all UNUSED records for the unit/date/shift, then filter in memory
            // to include records with requestType = "Truy vấn" OR requestType is NULL with SELECT-only rights
            List<PreRegistrationRequest> all = preRegistrationRepository
                    .findByUnitCodeAndRegisterDateAndShiftAndStatus(unitCode, date, shift, STATUS_UNUSED);
            List<PreRegistrationRequest> filtered = new java.util.ArrayList<>();
            for (PreRegistrationRequest r : all) {
                if ("Truy vấn".equals(r.getRequestType())) {
                    filtered.add(r);
                } else if (r.getRequestType() == null && isSelectOnly(r.getAccessRights())) {
                    filtered.add(r);
                }
            }
            return filtered;
        }
        // "Chỉnh sửa" -> load all matching (both types)
        return preRegistrationRepository
                .findByUnitCodeAndRegisterDateAndShiftAndStatus(unitCode, date, shift, STATUS_UNUSED);
    }

    // ============================
    // Status Lifecycle
    // ============================

    /**
     * Chuyen trang thai sang PENDING_APPROVAL va lien ket request_id.
     */
    @Transactional
    public void markAsPending(List<Long> preRegIds, Long requestId) {
        if (preRegIds == null || preRegIds.isEmpty()) {
            return;
        }
        List<PreRegistrationRequest> records = preRegistrationRepository.findAllById(preRegIds);
        for (PreRegistrationRequest record : records) {
            record.setStatus(STATUS_PENDING_APPROVAL);
            record.setRequestId(requestId);
        }
        preRegistrationRepository.saveAll(records);
    }

    /**
     * Chuyen tat ca ban ghi lien ket sang USED khi phieu 01-YCTC hoan thanh.
     */
    @Transactional
    public void markAsUsed(Long requestId) {
        preRegistrationRepository.updateStatusByRequestId(requestId, STATUS_USED);
    }

    /**
     * Hoan trang thai ve UNUSED va xoa lien ket request_id khi phieu 01-YCTC bi huy.
     */
    @Transactional
    public void revertToUnused(Long requestId) {
        List<PreRegistrationRequest> records = preRegistrationRepository.findByRequestId(requestId);
        for (PreRegistrationRequest record : records) {
            record.setStatus(STATUS_UNUSED);
            record.setRequestId(null);
        }
        preRegistrationRepository.saveAll(records);
    }

    /**
     * Khi loai yeu cau thay doi tu "Chỉnh sửa" sang "Truy vấn",
     * xoa cac dong co quyen khong phai SELECT (INSERT, UPDATE, DELETE, ...)
     * khoi phieu va hoan trang thai ve UNUSED.
     */
    @Transactional
    public void removeIncompatibleRows(Long requestId, String newRequestType) {
        if (!"Truy vấn".equals(newRequestType)) {
            return; // Chi xu ly khi chuyen sang "Truy vấn"
        }

        List<PreRegistrationRequest> records = preRegistrationRepository.findByRequestId(requestId);
        List<PreRegistrationRequest> incompatible = new ArrayList<>();

        for (PreRegistrationRequest record : records) {
            if (!isSelectOnly(record.getAccessRights())) {
                record.setStatus(STATUS_UNUSED);
                record.setRequestId(null);
                incompatible.add(record);
            }
        }

        if (!incompatible.isEmpty()) {
            preRegistrationRepository.saveAll(incompatible);
        }
    }

    // ============================
    // Scheduled Expiry
    // ============================

    /**
     * Danh dau cac dang ky truoc da het han (date+shift qua khu) thanh EXPIRED.
     * Ca 1 = 0-8h, Ca 2 = 8-20h, Ca 3 = 20-24h.
     *
     * @return so ban ghi da cap nhat thanh EXPIRED
     */
    @Transactional
    public int expireOutdatedRegistrations() {
        LocalDate today = LocalDate.now();
        int currentShift = determineCurrentShift(LocalTime.now());
        return preRegistrationRepository.expireOutdated(today, currentShift);
    }

    // ============================
    // Private Helpers
    // ============================

    /**
     * Xac dinh ca hien tai tu thoi gian: 0-8h = Ca 1, 8-20h = Ca 2, 20-24h = Ca 3.
     */
    int determineCurrentShift(LocalTime time) {
        int hour = time.getHour();
        if (hour < 8) {
            return 1;
        } else if (hour < 20) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Validate ngay + ca khong o qua khu.
     */
    private void validateTimeNotPast(LocalDate date, Integer shift) {
        if (date == null || shift == null) {
            throw new BusinessException("Ngay va ca dang ky khong duoc de trong");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime shiftEnd = getShiftEndTime(date, shift);

        if (shiftEnd.isBefore(now)) {
            throw new BusinessException("Khong the dang ky cho ngay va ca da qua");
        }
    }

    /**
     * Tra ve thoi diem ket thuc cua ca. Ca 1: 08:00, Ca 2: 20:00, Ca 3: 23:59:59.
     */
    private LocalDateTime getShiftEndTime(LocalDate date, int shiftNo) {
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

    /**
     * Kiem tra trung lap truoc khi tao moi.
     */
    private void validateNoDuplicate(Long userId, PreRegistrationForm form) {
        boolean exists = preRegistrationRepository
                .existsByUserIdAndRegisterDateAndShiftAndSystemIdAndDatabaseIdAndObjectNameAndAccessRights(
                        userId, form.getRegisterDate(), form.getShift(),
                        form.getSystemId(), form.getDatabaseId(),
                        form.getObjectName(), form.getAccessRights());
        if (exists) {
            throw new BusinessException("Da ton tai dang ky truoc trung lap (cung ngay, ca, he thong, CSDL, doi tuong, quyen)");
        }
    }

    /**
     * Kiem tra trung lap khi cap nhat (loai tru ban ghi hien tai).
     */
    private void validateNoDuplicateExcluding(Long userId, PreRegistrationForm form, Long excludeId) {
        boolean exists = preRegistrationRepository
                .existsByUserIdAndRegisterDateAndShiftAndSystemIdAndDatabaseIdAndObjectNameAndAccessRights(
                        userId, form.getRegisterDate(), form.getShift(),
                        form.getSystemId(), form.getDatabaseId(),
                        form.getObjectName(), form.getAccessRights());
        if (exists) {
            // Luc nay co the trung chinh no — kiem tra lai
            PreRegistrationRequest existing = preRegistrationRepository
                    .findById(excludeId).orElse(null);
            if (existing != null && isSameIdentity(existing, form)) {
                return; // Trung chinh no, ok
            }
            throw new BusinessException("Da ton tai dang ky truoc trung lap (cung ngay, ca, he thong, CSDL, doi tuong, quyen)");
        }
    }

    /**
     * Tim ban ghi va dam bao thuoc quyen so huu cua user hien tai.
     */
    private PreRegistrationRequest findOwnedRecord(Long id, UserSession session) {
        PreRegistrationRequest entity = preRegistrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay ban ghi dang ky truoc"));
        if (!entity.getUserId().equals(session.getUserId())) {
            throw new BusinessException("Ban khong co quyen thao tac tren ban ghi nay");
        }
        return entity;
    }

    /**
     * Kiem tra identity (cac truong dung de kiem tra trung lap) co thay doi khong.
     */
    private boolean hasIdentityChanged(PreRegistrationRequest entity, PreRegistrationForm form) {
        return !equals(entity.getRegisterDate(), form.getRegisterDate())
                || !equals(entity.getShift(), form.getShift())
                || !equals(entity.getSystemId(), form.getSystemId())
                || !equals(entity.getDatabaseId(), form.getDatabaseId())
                || !equals(entity.getObjectName(), form.getObjectName())
                || !equals(entity.getAccessRights(), form.getAccessRights());
    }

    /**
     * So sanh identity cua entity voi form.
     */
    private boolean isSameIdentity(PreRegistrationRequest entity, PreRegistrationForm form) {
        return equals(entity.getRegisterDate(), form.getRegisterDate())
                && equals(entity.getShift(), form.getShift())
                && equals(entity.getSystemId(), form.getSystemId())
                && equals(entity.getDatabaseId(), form.getDatabaseId())
                && equals(entity.getObjectName(), form.getObjectName())
                && equals(entity.getAccessRights(), form.getAccessRights());
    }

    /**
     * Kiem tra quyen truy cap chi la SELECT.
     * accessRights co the la gia tri don ("SELECT") hoac danh sach phan cach boi dau phay ("SELECT,INSERT").
     */
    private boolean isSelectOnly(String accessRights) {
        if (accessRights == null || accessRights.isBlank()) {
            return false;
        }
        String[] rights = accessRights.split(",");
        for (String right : rights) {
            if (!"SELECT".equalsIgnoreCase(right.trim())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tu dong xac dinh requestType tu accessRights:
     * - Chi SELECT -> "Truy vấn"
     * - Co INSERT/UPDATE/DELETE -> "Chỉnh sửa"
     * - Null hoac rong -> null
     */
    private String deriveRequestType(String accessRights) {
        if (accessRights == null || accessRights.isBlank()) {
            return null;
        }
        return isSelectOnly(accessRights) ? "Truy vấn" : "Chỉnh sửa";
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
