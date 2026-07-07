package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.audit.AuditService;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.SigningScope;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.*;
import com.csdl.access.domain.repo.*;
import com.csdl.access.integration.otp.OtpVerifyResult;
import com.csdl.access.integration.otp.OtpService;
import com.csdl.access.request.dto.ValidationError;
import com.csdl.access.workflow.RequestSubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nghiep vu lap/sua/ky/huy/gui lai yeu cau (features/request-create.md).
 */
@Service
public class RequestService {

    private final AccessRequestRepository requestRepository;
    private final RequestDetailRepository detailRepository;
    private final RequestSignatureRepository signatureRepository;
    private final AppUserRepository userRepository;
    private final InformationSystemRepository systemRepository;
    private final DatabaseCatalogRepository databaseRepository;
    private final EmergencyCompletionLinkRepository emergencyCompletionLinkRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RequestCodeGenerator codeGenerator;
    private final EmergencyDebtService debtService;
    private final OtpService otpService;
    private final AuditService auditService;
    private final RequestValidationService validationService;
    private final RequestSubmissionService submissionService;
    private final PreRegistrationRepository preRegistrationRepository;

    public RequestService(AccessRequestRepository requestRepository,
                          RequestDetailRepository detailRepository,
                          RequestSignatureRepository signatureRepository,
                          AppUserRepository userRepository,
                          InformationSystemRepository systemRepository,
                          DatabaseCatalogRepository databaseRepository,
                          EmergencyCompletionLinkRepository emergencyCompletionLinkRepository,
                          RoleRepository roleRepository,
                          UserRoleRepository userRoleRepository,
                          RequestCodeGenerator codeGenerator,
                          EmergencyDebtService debtService,
                          OtpService otpService,
                          AuditService auditService,
                          RequestValidationService validationService,
                          RequestSubmissionService submissionService,
                          PreRegistrationRepository preRegistrationRepository) {
        this.requestRepository = requestRepository;
        this.detailRepository = detailRepository;
        this.signatureRepository = signatureRepository;
        this.userRepository = userRepository;
        this.systemRepository = systemRepository;
        this.databaseRepository = databaseRepository;
        this.emergencyCompletionLinkRepository = emergencyCompletionLinkRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.codeGenerator = codeGenerator;
        this.debtService = debtService;
        this.otpService = otpService;
        this.auditService = auditService;
        this.validationService = validationService;
        this.submissionService = submissionService;
        this.preRegistrationRepository = preRegistrationRepository;
    }

    public List<AccessRequest> myRequests(UserSession session) {
        return requestRepository.findByRequesterUserIdOrderByCreatedAtDesc(session.getUserId());
    }

    /**
     * Phieu 01-YCTC / 04A-YCTK dang PENDING_SIGN cung don vi (loai tru phieu cua chinh minh).
     * Dung cho muc "Phieu cho ky chung" tren trang danh sach.
     */
    public List<AccessRequest> sharedPendingSignRequests(UserSession session) {
        List<RequestType> types = List.of(RequestType.YCTC_01, RequestType.YCTK_04A);
        return requestRepository.findByRequestTypeInAndStatusAndRequesterUnitIdAndRequesterUserIdNot(
                types, RequestStatus.PENDING_SIGN, session.getUnitId(), session.getUserId());
    }

    public AccessRequest get(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay yeu cau"));
    }

    public List<RequestDetail> details(Long requestId) {
        return detailRepository.findByRequestId(requestId);
    }

    public List<RequestSignature> signatures(Long requestId) {
        return signatureRepository.findByRequestId(requestId);
    }

    @Transactional
    public AccessRequest createDraft(RequestForm form, UserSession session) {
        RequestType type = parseType(form.getRequestType());

        // Chan lap phieu moi neu dang no 05B (tru chinh phieu 05B).
        if (type != RequestType.HTKC_05B && debtService.hasOutstandingDebt(session.getUserId())) {
            throw new BusinessException(
                    "Ban dang no phieu 05B-HTKC chua hoan thien, khong the lap phieu moi.");
        }

        AppUser user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new BusinessException("Khong tim thay nguoi dung"));

        AccessRequest r = new AccessRequest();
        r.setRequestType(type);
        r.setStatus(RequestStatus.DRAFT);
        r.setRequesterUserId(user.getId());
        r.setRequesterUnitId(user.getUnitId());
        r.setRequesterDepartmentId(user.getDepartmentId());
        applyForm(r, form);
        r.setRequestCode(codeGenerator.generate(user.getUnitId()));
        r = requestRepository.save(r);

        saveDetails(r.getId(), form, type);

        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "CREATE_DRAFT", "access_request", r.getId(), "Tao nhap " + type.getFormCode());
        return r;
    }

    @Transactional
    public AccessRequest updateDraft(Long id, RequestForm form, UserSession session) {
        AccessRequest r = get(id);
        // Allow co-workers from same unit to edit PENDING_SIGN forms for 01/04A
        if (!r.getRequesterUserId().equals(session.getUserId())) {
            boolean isSharedEditable = r.getStatus() == RequestStatus.PENDING_SIGN
                    && (r.getRequestType() == RequestType.YCTC_01 || r.getRequestType() == RequestType.YCTK_04A)
                    && r.getRequesterUnitId() != null
                    && r.getRequesterUnitId().equals(session.getUnitId());
            if (!isSharedEditable) {
                throw new BusinessException("Ban khong phai nguoi lap phieu nay.");
            }
        }
        if (!r.getStatus().isEditable()) {
            throw new BusinessException("Phieu da gui phe duyet, khong the sua.");
        }
        applyForm(r, form);
        r = requestRepository.save(r);

        // For PENDING_SIGN co-worker: only ADD their own new rows, don't delete others
        if (r.getStatus() == RequestStatus.PENDING_SIGN && !r.getRequesterUserId().equals(session.getUserId())) {
            // Co-worker: only save NEW rows where targetUserId matches the current user
            for (DetailForm d : form.getDetails()) {
                if (isBlankDetail(d)) continue;
                if (d.getId() == null && session.getUserId().equals(d.getTargetUserId())) {
                    RequestDetail detail = new RequestDetail();
                    detail.setRequestId(id);
                    detail.setSystemId(d.getSystemId());
                    detail.setDatabaseId(d.getDatabaseId());
                    detail.setObjectOwner(d.getObjectOwner());
                    detail.setObjectName(d.getObjectName());
                    detail.setTargetUserId(d.getTargetUserId());
                    detail.setAccessRights(d.getAccessRights());
                    detail.setPurpose(d.getPurpose());
                    detailRepository.save(detail);
                }
            }
        } else {
            // Owner or DRAFT: full reset + re-save all details (existing behavior)
            List<PreRegistrationRequest> linkedPreRegs = preRegistrationRepository.findByRequestId(id);
            for (PreRegistrationRequest preReg : linkedPreRegs) {
                preReg.setRequestId(null);
                preReg.setStatus("UNUSED");
                preRegistrationRepository.save(preReg);
            }
            List<RequestSignature> existingDetailSigs = signatureRepository
                    .findByRequestIdAndDetailIdNotNullAndResult(id, "SUCCESS");
            for (RequestSignature sig : existingDetailSigs) {
                if (sig.getOtpTransactionId() == null && sig.getSigningScope() == SigningScope.DETAIL) {
                    signatureRepository.delete(sig);
                }
            }
            detailRepository.deleteByRequestId(id);
            saveDetails(id, form, r.getRequestType());
        }

        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "UPDATE_DRAFT", "access_request", id, "Cap nhat nhap");
        return r;
    }

    @Transactional
    public OtpVerifyResult sign(Long id, String otp, SigningScope scope, Long detailId, UserSession session) {
        AccessRequest r = get(id);
        // Cho phep ky khi con o giai doan lap (DRAFT/RETURNED/SEND_FAILED).
        if (!r.getStatus().isEditable()) {
            throw new BusinessException("Phieu khong o trang thai cho ky.");
        }
        OtpVerifyResult result = otpService.verifyOtp(
                session.getUsername(), otp, "SIGN_REQUEST", id);

        // Neu ky GENERAL va nguoi ky co ten trong dong chi tiet → tu dong ky dong do luon
        Long resolvedDetailId = detailId;
        if (scope == SigningScope.GENERAL && detailId == null) {
            // Tim dong chi tiet cua chinh nguoi ky (requester cung la target user)
            List<RequestDetail> details = detailRepository.findByRequestId(id);
            for (RequestDetail d : details) {
                if (session.getUserId().equals(d.getTargetUserId())) {
                    resolvedDetailId = d.getId();
                    break;
                }
            }
        }

        RequestSignature sig = new RequestSignature();
        sig.setRequestId(id);
        sig.setDetailId(resolvedDetailId);
        sig.setSignerUserId(session.getUserId());
        sig.setSignerRoleCode(session.getActiveRole() == null ? null : session.getActiveRole().name());
        sig.setSigningScope(scope);
        sig.setOtpTransactionId(result.getTransactionId());
        sig.setSignedAt(LocalDateTime.now());
        sig.setResult(result.isSuccess() ? "SUCCESS" : "FAILED");
        userRepository.findById(session.getUserId())
                .ifPresent(u -> sig.setSignatureImageId(u.getSignatureImageId()));
        signatureRepository.save(sig);

        auditService.record(session.getUsername(),
                sig.getSignerRoleCode(), "SIGN_REQUEST", "access_request", id,
                "Ky " + scope + " ket qua " + sig.getResult());

        if (!result.isSuccess()) {
            throw new BusinessException("Ky xac nhan that bai: " + result.getMessage());
        }
        return result;
    }

    @Transactional
    public void cancel(Long id, UserSession session) {
        AccessRequest r = get(id);
        assertOwner(r, session);
        if (!r.getStatus().isCancellable()) {
            throw new BusinessException("Chi duoc huy khi phieu chua duoc phe duyet.");
        }
        r.setStatus(RequestStatus.CANCELLED);
        r.setCancelledAt(LocalDateTime.now());
        requestRepository.save(r);
        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "CANCEL", "access_request", id, "Huy yeu cau");
    }

    // ============================
    // Luu phieu — Branch A (cho ky chung)
    // ============================

    /**
     * Chuyen phieu tu DRAFT sang PENDING_SIGN de dong nghiep cung don vi co the them dong va ky.
     * Chi ap dung cho mau 01-YCTC va 04A-YCTK.
     */
    @Transactional
    public void saveForSign(Long id, UserSession session) {
        AccessRequest r = get(id);
        assertOwner(r, session);

        if (r.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessException("Chỉ có thể lưu phiếu từ trạng thái Nháp.");
        }

        RequestType type = r.getRequestType();
        if (type != RequestType.YCTC_01 && type != RequestType.YCTK_04A) {
            throw new BusinessException("Chức năng lưu phiếu chỉ dành cho mẫu 01-YCTC và 04A-YCTK.");
        }

        // Generate request code if not yet generated
        if (r.getRequestCode() == null || r.getRequestCode().isBlank()) {
            r.setRequestCode(codeGenerator.generateByUnit(session.getUnitId(), session.getDepartmentId()));
        }

        r.setStatus(RequestStatus.PENDING_SIGN);
        requestRepository.save(r);

        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "SAVE_FOR_SIGN", "access_request", id, "Luu phieu cho ky chung");
    }

    // ============================
    // 05B Consolidation from 05A group
    // ============================

    /**
     * Tao phieu 05B-HTKC tu nhom cac phieu 05A-YCKC da hoan thanh.
     *
     * <p>Logic:
     * <ol>
     *   <li>Load tat ca 05A theo danh sach ID</li>
     *   <li>Xac minh tung phieu: status=COMPLETED, type=YCKC_05A, thuoc user hien tai</li>
     *   <li>Kiem tra cung (system_id, database_id, shift_no)</li>
     *   <li>Tao AccessRequest 05B-HTKC voi thong tin gop</li>
     *   <li>Sinh request_code bang RequestCodeGenerator.generate05B</li>
     *   <li>Tao union detail rows (deduplicate theo objectName)</li>
     *   <li>Tao N ban ghi emergency_completion_link (1 per 05A)</li>
     * </ol>
     *
     * @param source05AIds Danh sach ID cua cac phieu 05A can gop
     * @param session      Phien nguoi dung hien tai
     * @return Phieu 05B-HTKC moi
     */
    @Transactional
    public AccessRequest create05BFromGroup(List<Long> source05AIds, UserSession session) {
        if (source05AIds == null || source05AIds.isEmpty()) {
            throw new BusinessException("Danh sach phieu 05A khong duoc de trong.");
        }

        // 1. Load all source 05A requests
        List<AccessRequest> source05As = requestRepository.findAllById(source05AIds);
        if (source05As.size() != source05AIds.size()) {
            throw new BusinessException("Mot so phieu 05A khong ton tai.");
        }

        // 2. Verify each: status=COMPLETED, type=YCKC_05A, belongs to current user
        for (AccessRequest src : source05As) {
            if (src.getRequestType() != RequestType.YCKC_05A) {
                throw new BusinessException(
                        "Phieu ID=" + src.getId() + " khong phai loai 05A-YCKC.");
            }
            if (src.getStatus() != RequestStatus.COMPLETED) {
                throw new BusinessException(
                        "Phieu 05A ID=" + src.getId() + " chua hoan thanh (status="
                                + src.getStatus().getDisplayName() + ").");
            }
            if (!src.getRequesterUserId().equals(session.getUserId())) {
                throw new BusinessException(
                        "Phieu 05A ID=" + src.getId() + " khong thuoc nguoi dung hien tai.");
            }
        }

        // 3. Verify they share the same (system_id, database_id, shift_no)
        AccessRequest first = source05As.get(0);
        Long commonSystemId = first.getSystemId();
        Long commonDatabaseId = first.getDatabaseId();
        Integer commonShiftNo = first.getShiftNo();

        for (AccessRequest src : source05As) {
            if (!Objects.equals(src.getSystemId(), commonSystemId)) {
                throw new BusinessException(
                        "Cac phieu 05A khong cung he thong (system_id khac nhau).");
            }
            if (!Objects.equals(src.getDatabaseId(), commonDatabaseId)) {
                throw new BusinessException(
                        "Cac phieu 05A khong cung co so du lieu (database_id khac nhau).");
            }
            if (!Objects.equals(src.getShiftNo(), commonShiftNo)) {
                throw new BusinessException(
                        "Cac phieu 05A khong cung ca lam viec (shift_no khac nhau).");
            }
        }

        // 4. Create new 05B-HTKC request
        // Determine time range: earliest start_time and latest end_time from 05A group
        LocalDateTime earliestStart = source05As.stream()
                .map(AccessRequest::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestEnd = source05As.stream()
                .map(AccessRequest::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        AccessRequest request05B = new AccessRequest();
        request05B.setRequestType(RequestType.HTKC_05B);
        request05B.setStatus(RequestStatus.DRAFT);
        request05B.setSystemId(commonSystemId);
        request05B.setDatabaseId(commonDatabaseId);
        request05B.setShiftNo(commonShiftNo);
        request05B.setStartTime(earliestStart);
        request05B.setEndTime(latestEnd);
        request05B.setRequesterUserId(session.getUserId());
        request05B.setRequesterUnitId(session.getUnitId());

        // 5. Generate request code
        String requestCode = codeGenerator.generate05B(commonSystemId, source05As);
        request05B.setRequestCode(requestCode);

        request05B = requestRepository.save(request05B);

        // 6. Create union of detail rows (deduplicate by objectName)
        Set<String> seenObjectNames = new HashSet<>();
        for (AccessRequest src : source05As) {
            List<RequestDetail> srcDetails = detailRepository.findByRequestId(src.getId());
            for (RequestDetail srcDetail : srcDetails) {
                String objectName = srcDetail.getObjectName();
                // Deduplicate by objectName (null objectName treated as unique)
                if (objectName != null && seenObjectNames.contains(objectName)) {
                    continue;
                }
                if (objectName != null) {
                    seenObjectNames.add(objectName);
                }

                RequestDetail newDetail = new RequestDetail();
                newDetail.setRequestId(request05B.getId());
                newDetail.setSystemId(srcDetail.getSystemId());
                newDetail.setDatabaseId(srcDetail.getDatabaseId());
                newDetail.setObjectOwner(srcDetail.getObjectOwner());
                newDetail.setObjectName(srcDetail.getObjectName());
                newDetail.setObjectType(srcDetail.getObjectType());
                newDetail.setTargetUserId(srcDetail.getTargetUserId());
                newDetail.setAccountOwnerName(srcDetail.getAccountOwnerName());
                newDetail.setAccountType(srcDetail.getAccountType());
                newDetail.setAccountAction(srcDetail.getAccountAction());
                newDetail.setAccessRights(srcDetail.getAccessRights());
                newDetail.setQueryAll(srcDetail.isQueryAll());
                newDetail.setPurpose(srcDetail.getPurpose());
                detailRepository.save(newDetail);
            }
        }

        // 7. Create N records in emergency_completion_link (one per source 05A)
        for (AccessRequest src : source05As) {
            EmergencyCompletionLink link = new EmergencyCompletionLink();
            link.setEmergencyRequestId(src.getId());
            link.setCompletionRequestId(request05B.getId());
            link.setCreatedAt(LocalDateTime.now());
            emergencyCompletionLinkRepository.save(link);
        }

        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "CREATE_05B", "access_request", request05B.getId(),
                "Tao phieu 05B-HTKC gop tu " + source05AIds.size() + " phieu 05A");

        return request05B;
    }

    private void assertOwner(AccessRequest r, UserSession session) {
        if (!r.getRequesterUserId().equals(session.getUserId())) {
            throw new BusinessException("Ban khong phai nguoi lap phieu nay.");
        }
    }

    // ============================
    // Variant determination & Submission
    // ============================

    /**
     * Xac dinh variant (Internal / External) cho yeu cau.
     * So sanh requester_unit_id voi owner_unit_id tu information_system.
     *
     * - Neu requester_unit_id == information_system.owner_unit_id → "I" (Internal)
     * - Neu khac nhau → "E" (External)
     * - Cac loai khong dung variant (03-YCCT, 05A-YCKC, 04B): return null
     *
     * Fallback: Neu system_id null nhung database_id co → tra nguoc qua database_catalog.system_id
     *           → information_system.owner_unit_id.
     *
     * @param request Yeu cau can xac dinh variant
     * @return "I", "E", hoac null (khong ap dung variant)
     */
    public String determineVariant(AccessRequest request) {
        RequestType type = request.getRequestType();

        // Types that don't use variant: 03-YCCT and 05A-YCKC always return null
        if (type == RequestType.YCCT_03 || type == RequestType.YCKC_05A) {
            return null;
        }

        // Resolve owner_unit_id from information_system
        Long ownerUnitId = resolveOwnerUnitId(request);

        if (ownerUnitId == null) {
            // Khong xac dinh duoc don vi chu quan → mac dinh null (no variant)
            return null;
        }

        Long requesterUnitId = request.getRequesterUnitId();
        if (requesterUnitId == null) {
            return null;
        }

        if (requesterUnitId.equals(ownerUnitId)) {
            return "I"; // Internal
        } else {
            return "E"; // External
        }
    }

    /**
     * Chuan bi va gui phieu phe duyet.
     * 1. Load request
     * 2. Validate (validateForSubmission)
     * 3. Xac dinh variant (determineVariant)
     * 4. Cache owner_unit_id len request
     * 5. Goi RequestSubmissionService.submit(...)
     *
     * @param requestId ID phieu yeu cau
     * @param session   Phien nguoi dung hien tai
     */
    @Transactional
    public void prepareSubmission(Long requestId, UserSession session) {
        AccessRequest request = get(requestId);
        assertOwner(request, session);

        // Kiem tra trang thai cho phep gui (DRAFT, PENDING_SIGN, RETURNED)
        if (!request.getStatus().isEditable()) {
            throw new BusinessException("Phieu khong o trang thai cho phep gui phe duyet.");
        }

        // Validate truoc khi gui
        List<RequestDetail> details = detailRepository.findByRequestId(requestId);
        List<ValidationError> errors = validationService.validateForSubmission(request, details);
        if (!errors.isEmpty()) {
            String messages = errors.stream()
                    .map(ValidationError::message)
                    .collect(Collectors.joining("; "));
            throw new BusinessException(messages);
        }

        // Xac dinh variant va cache owner_unit_id
        String variant = determineVariant(request);
        Long ownerUnitId = resolveOwnerUnitId(request);
        if (ownerUnitId != null) {
            request.setOwnerUnitId(ownerUnitId);
            requestRepository.save(request);
        }

        // Goi submission service (task 9.3 se hoan thien logic ben trong)
        submissionService.submit(requestId, null, session);
    }

    /**
     * Resolve owner_unit_id tu information_system.
     * Primary: dung request.systemId → information_system.owner_unit_id
     * Fallback: neu systemId null nhung databaseId co → database_catalog.system_id → information_system.owner_unit_id
     */
    private Long resolveOwnerUnitId(AccessRequest request) {
        Long systemId = request.getSystemId();

        // Primary path: system_id on request
        if (systemId != null) {
            return systemRepository.findById(systemId)
                    .map(InformationSystem::getOwnerUnitId)
                    .orElse(null);
        }

        // Fallback: database_id → database_catalog.system_id → information_system.owner_unit_id
        Long databaseId = request.getDatabaseId();
        if (databaseId != null) {
            return databaseRepository.findById(databaseId)
                    .map(DatabaseCatalog::getSystemId)
                    .flatMap(sysId -> systemRepository.findById(sysId))
                    .map(InformationSystem::getOwnerUnitId)
                    .orElse(null);
        }

        return null;
    }

    private void applyForm(AccessRequest r, RequestForm form) {
        r.setShiftNo(form.getShiftNo());
        r.setAccessNo(form.getAccessNo());
        r.setSystemId(form.getSystemId());
        r.setDatabaseId(form.getDatabaseId());
        r.setReason(form.getReason());
        r.setSubType(form.getRequestSubType());
        r.setExpectedExecutionDate(parseDateTime(form.getExpectedExecutionDate()));

        LocalDateTime start = parseDateTime(form.getStartTime());
        LocalDateTime end = parseDateTime(form.getEndTime());
        // Neu khong nhap thoi gian, suy ra theo ca lam viec.
        if ((start == null || end == null) && form.getShiftNo() != null) {
            LocalDateTime[] shift = shiftTimes(form.getShiftNo());
            if (start == null) {
                start = shift[0];
            }
            if (end == null) {
                end = shift[1];
            }
        }
        r.setStartTime(start);
        r.setEndTime(end);
    }

    /** Ca 1: 0-8h, Ca 2: 8-20h, Ca 3: 20-24h (request-create.md). */
    private LocalDateTime[] shiftTimes(int shift) {
        java.time.LocalDate today = java.time.LocalDate.now();
        switch (shift) {
            case 1:
                return new LocalDateTime[]{today.atTime(LocalTime.MIDNIGHT), today.atTime(8, 0)};
            case 2:
                return new LocalDateTime[]{today.atTime(8, 0), today.atTime(20, 0)};
            case 3:
                return new LocalDateTime[]{today.atTime(20, 0), today.atTime(23, 59)};
            default:
                return new LocalDateTime[]{today.atStartOfDay(), today.atTime(23, 59)};
        }
    }

    private void saveDetails(Long requestId, RequestForm form, RequestType type) {
        if (form.getDetails() == null) {
            return;
        }
        for (DetailForm d : form.getDetails()) {
            if (isBlankDetail(d)) {
                continue;
            }
            RequestDetail detail = new RequestDetail();
            detail.setRequestId(requestId);
            detail.setSystemId(d.getSystemId());
            detail.setDatabaseId(d.getDatabaseId());
            detail.setObjectOwner(d.getObjectOwner());
            detail.setObjectName(d.getObjectName());
            detail.setObjectType(d.getObjectType());
            detail.setTargetUserId(d.getTargetUserId());
            detail.setAccountOwnerName(d.getAccountOwnerName());
            detail.setAccountType(d.getAccountType());
            detail.setAccountAction(d.getAccountAction());
            detail.setQueryAll(d.isQueryAll());
            // QueryAll bat thi cac quyen chi tiet khong ap dung (05A-YCKC).
            detail.setAccessRights(d.isQueryAll() ? "QUERY_ALL" : d.getAccessRights());
            detail.setPurpose(d.getPurpose());
            detail = detailRepository.save(detail);

            // If this detail row came from a pre-registration that was already signed,
            // create a corresponding request_signature record so the signing status
            // displays correctly ("Đã ký") on the edit/view pages.
            if (d.getPreRegistrationId() != null) {
                final RequestDetail savedDetail = detail;
                preRegistrationRepository.findById(d.getPreRegistrationId()).ifPresent(preReg -> {
                    if (preReg.getSignedAt() != null) {
                        RequestSignature sig = new RequestSignature();
                        sig.setRequestId(requestId);
                        sig.setDetailId(savedDetail.getId());
                        sig.setSignerUserId(preReg.getUserId());
                        sig.setSigningScope(SigningScope.DETAIL);
                        sig.setSignedAt(preReg.getSignedAt());
                        sig.setSignatureImageId(preReg.getSignatureImageId());
                        sig.setResult("SUCCESS");
                        signatureRepository.save(sig);
                    }
                    // Mark pre-registration as linked to this request
                    preReg.setRequestId(requestId);
                    preReg.setStatus("PENDING_APPROVAL");
                    preRegistrationRepository.save(preReg);
                });
            }
        }
    }

    private boolean isBlankDetail(DetailForm d) {
        return (d.getDatabaseId() == null && d.getSystemId() == null)
                && isBlank(d.getObjectName())
                && isBlank(d.getAccountOwnerName())
                && d.getTargetUserId() == null
                && !d.isQueryAll();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private RequestType parseType(String code) {
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

    // ============================
    // 04B-BGTK creation from completed 04A
    // ============================

    /**
     * Tao phieu 04B-BGTK tu phieu 04A-YCTK da hoan thanh.
     *
     * Logic:
     * 1. Load source 04A request
     * 2. Verify status=COMPLETED va type=YCTK_04A
     * 3. Verify chua co 04B nao lien ket voi 04A nay (check source_request_id)
     * 4. Tao header 04B voi thong tin tu 04A + session DBA
     * 5. Tao N detail rows tuong ung N target_user tu 04A detail
     * 6. Save va return phieu 04B moi
     *
     * @param source04AId ID cua phieu 04A nguon
     * @param session     Phien nguoi dung DBA hien tai
     * @return Phieu 04B-BGTK moi
     */
    @Transactional
    public AccessRequest create04BFromSource(Long source04AId, UserSession session) {
        // 1. Load source 04A
        AccessRequest source04A = requestRepository.findById(source04AId)
                .orElseThrow(() -> new BusinessException("Khong tim thay phieu 04A nguon."));

        // 2. Verify status and type
        if (source04A.getRequestType() != RequestType.YCTK_04A) {
            throw new BusinessException("Phieu nguon khong phai loai 04A-YCTK.");
        }
        if (source04A.getStatus() != RequestStatus.COMPLETED) {
            throw new BusinessException("Phieu 04A chua hoan thanh, khong the tao 04B.");
        }

        // 3. Verify no existing 04B links to this 04A
        List<AccessRequest> existing04B = requestRepository.findBySourceRequestId(source04AId);
        if (!existing04B.isEmpty()) {
            throw new BusinessException("Phieu 04A nay da co phieu 04B lien ket.");
        }

        // 4. Create new 04B header
        AccessRequest request04B = new AccessRequest();
        request04B.setRequestType(RequestType.BGTK_04B);
        request04B.setStatus(RequestStatus.DRAFT);
        request04B.setSystemId(source04A.getSystemId());
        request04B.setDatabaseId(source04A.getDatabaseId());
        request04B.setSourceRequestId(source04A.getId());
        request04B.setRequesterUserId(session.getUserId());
        request04B.setRequesterUnitId(session.getUnitId());
        request04B.setRequesterDepartmentId(session.getDepartmentId());

        // Resolve handover manager (DBA's department manager)
        Long handoverManagerId = resolveDepartmentManager(session.getDepartmentId());
        request04B.setHandoverManagerId(handoverManagerId);

        // Resolve receiver manager (04A requester's department manager)
        AppUser requester04A = userRepository.findById(source04A.getRequesterUserId())
                .orElse(null);
        if (requester04A != null) {
            Long receiverManagerId = resolveDepartmentManager(requester04A.getDepartmentId());
            request04B.setReceiverManagerId(receiverManagerId);
        }

        // Generate request code for 04B
        request04B.setRequestCode(codeGenerator.generate(session.getUnitId()));

        request04B = requestRepository.save(request04B);

        // 5. Create N detail rows from source 04A's detail rows
        List<RequestDetail> source04ADetails = detailRepository.findByRequestId(source04AId);
        for (RequestDetail sourceDetail : source04ADetails) {
            RequestDetail detail04B = new RequestDetail();
            detail04B.setRequestId(request04B.getId());
            detail04B.setSystemId(sourceDetail.getSystemId());
            detail04B.setDatabaseId(sourceDetail.getDatabaseId());
            detail04B.setTargetUserId(sourceDetail.getTargetUserId());
            detail04B.setAccountType(sourceDetail.getAccountType());
            detail04B.setAccountAction(sourceDetail.getAccountAction());
            detail04B.setScope(sourceDetail.getScope());
            detail04B.setAccessRights(sourceDetail.getAccessRights());

            // Resolve target user's full name as account_owner_name
            if (sourceDetail.getTargetUserId() != null) {
                userRepository.findById(sourceDetail.getTargetUserId())
                        .ifPresent(u -> detail04B.setAccountOwnerName(u.getFullName()));
            }

            // Leave "Tai khoan (UserID)" field empty — DBA fills manually
            // (account_owner_name is set above as receiver's full name)

            detailRepository.save(detail04B);
        }

        // 6. Audit and return
        auditService.record(session.getUsername(),
                session.getActiveRole() == null ? null : session.getActiveRole().name(),
                "CREATE_04B", "access_request", request04B.getId(),
                "Tao phieu 04B-BGTK tu 04A #" + source04AId);

        return request04B;
    }

    /**
     * Tim lanh dao phong (DEPT_MANAGER) theo department_id.
     * Tra cuu qua user_role voi role code = DEPT_MANAGER va department_id khop.
     *
     * @param departmentId ID phong ban can tim lanh dao
     * @return userId cua lanh dao phong, hoac null neu khong tim thay
     */
    private Long resolveDepartmentManager(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        Optional<Role> deptManagerRole = roleRepository.findByCode("DEPT_MANAGER");
        if (deptManagerRole.isEmpty()) {
            return null;
        }
        List<UserRole> managers = userRoleRepository
                .findByRoleIdAndDepartmentIdAndActiveTrue(deptManagerRole.get().getId(), departmentId);
        if (managers.isEmpty()) {
            return null;
        }
        // Return first active department manager found
        return managers.get(0).getUserId();
    }
}
