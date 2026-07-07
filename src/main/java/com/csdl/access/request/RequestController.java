package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.SigningScope;
import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.RequestDetail;
import com.csdl.access.domain.RequestScriptFile;
import com.csdl.access.domain.RequestSignature;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.domain.repo.RequestDetailRepository;
import com.csdl.access.domain.repo.RequestScriptFileRepository;
import com.csdl.access.request.dto.AutoSaveResponse;
import com.csdl.access.request.dto.AutoSaveResult;
import com.csdl.access.request.dto.DetailSummaryDto;
import com.csdl.access.request.dto.EmergencyGroupDto;
import com.csdl.access.request.dto.FileUploadResponse;
import com.csdl.access.request.dto.RequestSummaryDto;
import com.csdl.access.request.dto.SigningStatusResponse;
import com.csdl.access.request.dto.ValidationError;
import com.csdl.access.workflow.RequestSubmissionService;
import com.csdl.access.workflow.WorkflowHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller lap/sua/ky/gui/huy yeu cau (api-contract.md muc 4).
 */
@Controller
@RequestMapping("/requests")
public class RequestController {

    private static final DateTimeFormatter HTML_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final RequestService requestService;
    private final RequestSubmissionService submissionService;
    private final EmergencyDebtService debtService;
    private final WorkflowHistoryService historyService;
    private final LookupService lookupService;
    private final UserSession userSession;
    private final DraftService draftService;
    private final ConcurrencyHandler concurrencyHandler;
    private final RequestValidationService validationService;
    private final AccessRequestRepository accessRequestRepository;
    private final RequestDetailRepository requestDetailRepository;
    private final RequestScriptFileRepository scriptFileRepository;
    private final com.csdl.access.domain.repo.PreRegistrationRepository preRegistrationRepository;

    public RequestController(RequestService requestService,
                             RequestSubmissionService submissionService,
                             EmergencyDebtService debtService,
                             WorkflowHistoryService historyService,
                             LookupService lookupService,
                             UserSession userSession,
                             DraftService draftService,
                             ConcurrencyHandler concurrencyHandler,
                             RequestValidationService validationService,
                             AccessRequestRepository accessRequestRepository,
                             RequestDetailRepository requestDetailRepository,
                             RequestScriptFileRepository scriptFileRepository,
                             com.csdl.access.domain.repo.PreRegistrationRepository preRegistrationRepository) {
        this.requestService = requestService;
        this.submissionService = submissionService;
        this.debtService = debtService;
        this.historyService = historyService;
        this.lookupService = lookupService;
        this.userSession = userSession;
        this.draftService = draftService;
        this.concurrencyHandler = concurrencyHandler;
        this.validationService = validationService;
        this.accessRequestRepository = accessRequestRepository;
        this.requestDetailRepository = requestDetailRepository;
        this.scriptFileRepository = scriptFileRepository;
        this.preRegistrationRepository = preRegistrationRepository;
    }

    @GetMapping
    public String myRequests(Model model) {
        List<com.csdl.access.common.lookup.RequestRow> rows = new ArrayList<>();
        for (AccessRequest r : requestService.myRequests(userSession)) {
            rows.add(lookupService.toRow(r));
        }
        model.addAttribute("rows", rows);

        // Shared requests from same unit (PENDING_SIGN for 01/04A)
        List<com.csdl.access.common.lookup.RequestRow> sharedRows = new ArrayList<>();
        for (AccessRequest r : requestService.sharedPendingSignRequests(userSession)) {
            sharedRows.add(lookupService.toRow(r));
        }
        model.addAttribute("sharedRows", sharedRows);

        return "requests/list";
    }

    @GetMapping("/new")
    public String chooseType(Model model) {
        model.addAttribute("types", RequestType.values());
        return "requests/new";
    }

    @GetMapping("/new/{type}")
    public String newForm(@PathVariable String type, Model model) {
        RequestType requestType = RequestType.valueOf(type);
        RequestForm form = new RequestForm();
        form.setRequestType(requestType.name());
        // chuan bi cac dong chi tiet trong de nhap
        for (int i = 0; i < 5; i++) {
            form.getDetails().add(new DetailForm());
        }
        prepareFormModel(model, requestType, form, null);
        return "requests/form";
    }

    @PostMapping("/draft")
    public String createDraft(@ModelAttribute RequestForm form,
                              @RequestParam(defaultValue = "draft") String action,
                              RedirectAttributes ra) {
        AccessRequest r = requestService.createDraft(form, userSession);

        // If action is "save-for-sign", immediately transition to PENDING_SIGN
        if ("save-for-sign".equals(action)) {
            requestService.saveForSign(r.getId(), userSession);
            ra.addFlashAttribute("infoMessage", "Đã lưu phiếu. Chờ người dùng chung ký xác nhận.");
        } else {
            ra.addFlashAttribute("infoMessage", "Đã lưu nháp. Mã yêu cầu: " + r.getRequestCode());
        }
        return "redirect:/requests/" + r.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        AccessRequest r = requestService.get(id);
        RequestForm form = toForm(r);
        prepareFormModel(model, r.getRequestType(), form, r);
        return "requests/form";
    }

    @PostMapping("/{id}/draft")
    public String updateDraft(@PathVariable Long id,
                              @ModelAttribute RequestForm form,
                              @RequestParam(defaultValue = "draft") String action,
                              RedirectAttributes ra) {
        requestService.updateDraft(id, form, userSession);

        // If action is "save-for-sign", transition to PENDING_SIGN
        if ("save-for-sign".equals(action)) {
            requestService.saveForSign(id, userSession);
            ra.addFlashAttribute("infoMessage", "Đã lưu phiếu. Chờ người dùng chung ký xác nhận.");
        } else {
            ra.addFlashAttribute("infoMessage", "Đã cập nhật nháp.");
        }
        return "redirect:/requests/" + id + "/edit";
    }

    @PostMapping("/{id}/sign")
    public String sign(@PathVariable Long id,
                       @RequestParam String otp,
                       @RequestParam(defaultValue = "GENERAL") String signingScope,
                       @RequestParam(required = false) Long detailId,
                       RedirectAttributes ra) {
        requestService.sign(id, otp, SigningScope.valueOf(signingScope), detailId, userSession);
        ra.addFlashAttribute("infoMessage", "Ky xac nhan thanh cong.");
        return "redirect:/requests/" + id + "/edit";
    }

    @PostMapping("/{id}/save-for-sign")
    public String saveForSign(@PathVariable Long id, RedirectAttributes ra) {
        requestService.saveForSign(id, userSession);
        ra.addFlashAttribute("infoMessage", "Đã lưu phiếu. Chờ người dùng chung ký xác nhận.");
        return "redirect:/requests/" + id + "/edit";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @RequestParam(required = false) Long emergencyRequestId,
                         RedirectAttributes ra) {
        submissionService.submit(id, emergencyRequestId, userSession);
        ra.addFlashAttribute("infoMessage", "Da gui yeu cau vao luong xu ly.");
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        requestService.cancel(id, userSession);
        ra.addFlashAttribute("infoMessage", "Da huy yeu cau.");
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/resend")
    public String resend(@PathVariable Long id, RedirectAttributes ra) {
        submissionService.resend(id, userSession);
        ra.addFlashAttribute("infoMessage", "Da gui lai yeu cau.");
        return "redirect:/requests/" + id;
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        AccessRequest r = requestService.get(id);
        model.addAttribute("request", r);
        model.addAttribute("row", lookupService.toRow(r));
        model.addAttribute("details", requestService.details(id));
        List<RequestSignature> sigs = requestService.signatures(id);
        model.addAttribute("signatures", sigs);
        // Set of detail IDs that have been successfully signed
        Set<Long> signedDetailIds = sigs.stream()
                .filter(s -> "SUCCESS".equals(s.getResult()) && s.getDetailId() != null)
                .map(RequestSignature::getDetailId)
                .collect(Collectors.toSet());
        model.addAttribute("signedDetailIds", signedDetailIds);
        model.addAttribute("history", historyService.history(id));
        model.addAttribute("lookup", lookupService);
        boolean isOwner = r.getRequesterUserId().equals(userSession.getUserId());
        model.addAttribute("isOwner", isOwner);
        return "requests/view";
    }

    // === AJAX Endpoints (new) ===

    /**
     * Auto-save (AJAX, khong reload trang).
     * Goi tu client moi 30s khi form co thay doi.
     */
    @PostMapping("/{id}/auto-save")
    @ResponseBody
    public ResponseEntity<AutoSaveResponse> autoSave(@PathVariable Long id,
                                                     @RequestBody RequestForm form) {
        AutoSaveResult result = draftService.autoSave(id, form, userSession);
        AutoSaveResponse response = new AutoSaveResponse(
                result.saved(),
                result.reason(),
                result.saved() ? LocalDateTime.now() : null
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Polling: lay trang thai ky moi nhat (cho phieu chung nhieu nguoi).
     * Client goi dinh ky de cap nhat giao dien.
     */
    @GetMapping("/{id}/signing-status")
    @ResponseBody
    public ResponseEntity<SigningStatusResponse> signingStatus(@PathVariable Long id) {
        SigningStatusResponse response = concurrencyHandler.getSigningStatus(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload file SQL (02-YCCS, 03-YCCT).
     * Validates file name format, size, checksum. Stores to DB.
     */
    @PostMapping("/{id}/upload-script")
    @ResponseBody
    public ResponseEntity<FileUploadResponse> uploadScript(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "checksumType", required = false) String checksumType,
            @RequestParam(value = "checksumValue", required = false) String checksumValue) {

        // Validate file
        ValidationError error = validationService.validateScriptFile(file, checksumType, checksumValue);
        if (error != null) {
            return ResponseEntity.badRequest().body(new FileUploadResponse(
                    false, file.getOriginalFilename(), file.getSize(), null, null));
        }

        // Store file to DB
        try {
            RequestScriptFile scriptFile = new RequestScriptFile();
            scriptFile.setRequestId(id);
            scriptFile.setFileName(file.getOriginalFilename());
            scriptFile.setFileContent(file.getBytes());
            scriptFile.setChecksum(checksumValue);
            scriptFile.setUploadedBy(userSession.getUserId());
            scriptFile.setUploadedAt(LocalDateTime.now());
            scriptFileRepository.save(scriptFile);

            return ResponseEntity.ok(new FileUploadResponse(
                    true,
                    file.getOriginalFilename(),
                    file.getSize(),
                    checksumValue,
                    checksumType
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new FileUploadResponse(
                    false, file.getOriginalFilename(), file.getSize(), null, null));
        }
    }

    /**
     * Xoa dong chi tiet chua ky (nguoi lap xoa).
     */
    @DeleteMapping("/{id}/details/{detailId}")
    @ResponseBody
    public ResponseEntity<Void> deleteUnsignedDetail(@PathVariable Long id,
                                                     @PathVariable Long detailId) {
        concurrencyHandler.deleteUnsignedDetail(id, detailId, userSession);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lay danh sach phieu 04A-YCTK da hoan thanh nhung chua co 04B lien ket.
     * Dung khi DBA lap phieu 04B-BGTK.
     */
    @GetMapping("/pending-04a")
    @ResponseBody
    public ResponseEntity<List<RequestSummaryDto>> pending04A() {
        List<AccessRequest> requests = accessRequestRepository.findCompletedYCTK04AWithout04B();
        List<RequestSummaryDto> result = requests.stream()
                .map(r -> new RequestSummaryDto(
                        r.getId(),
                        r.getRequestCode(),
                        r.getRequestType() != null ? r.getRequestType().getFormCode() : null,
                        lookupService.systemName(r.getSystemId()),
                        lookupService.databaseName(r.getDatabaseId()),
                        r.getStatus() != null ? r.getStatus().name() : null,
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Lay danh sach phieu 05A dang no, gop theo (systemId, databaseId, date, shift).
     * Dung khi nguoi dung lap phieu 05B-HTKC.
     */
    @GetMapping("/pending-05a-groups")
    @ResponseBody
    public ResponseEntity<List<EmergencyGroupDto>> pending05AGroups() {
        List<AccessRequest> outstanding = accessRequestRepository
                .findOutstanding05AForUser(userSession.getUserId());

        // Group by (systemId, databaseId, date from startTime, shift)
        Map<String, List<AccessRequest>> grouped = outstanding.stream()
                .collect(Collectors.groupingBy(r -> {
                    Long sysId = r.getSystemId() != null ? r.getSystemId() : 0L;
                    Long dbId = r.getDatabaseId() != null ? r.getDatabaseId() : 0L;
                    LocalDate date = r.getStartTime() != null ? r.getStartTime().toLocalDate() : LocalDate.MIN;
                    Integer shift = r.getShiftNo() != null ? r.getShiftNo() : 0;
                    return sysId + "|" + dbId + "|" + date + "|" + shift;
                }));

        List<EmergencyGroupDto> result = new ArrayList<>();
        for (Map.Entry<String, List<AccessRequest>> entry : grouped.entrySet()) {
            List<AccessRequest> group = entry.getValue();
            AccessRequest first = group.get(0);

            List<Long> requestIds = group.stream()
                    .map(AccessRequest::getId)
                    .collect(Collectors.toList());

            List<String> accessNos = group.stream()
                    .map(r -> r.getAccessNo() != null ? String.valueOf(r.getAccessNo()) : "")
                    .collect(Collectors.toList());

            // Collect union of all details across grouped requests
            List<DetailSummaryDto> unionDetails = new ArrayList<>();
            for (AccessRequest r : group) {
                List<RequestDetail> details = requestDetailRepository.findByRequestId(r.getId());
                for (RequestDetail d : details) {
                    unionDetails.add(new DetailSummaryDto(
                            d.getId(),
                            d.getObjectName(),
                            d.getAccessRights(),
                            lookupService.userName(d.getTargetUserId())
                    ));
                }
            }

            LocalDate date = first.getStartTime() != null ? first.getStartTime().toLocalDate() : null;

            result.add(new EmergencyGroupDto(
                    first.getSystemId(),
                    lookupService.systemName(first.getSystemId()),
                    first.getDatabaseId(),
                    lookupService.databaseName(first.getDatabaseId()),
                    date,
                    first.getShiftNo(),
                    requestIds,
                    accessNos,
                    unionDetails
            ));
        }
        return ResponseEntity.ok(result);
    }

    private void prepareFormModel(Model model, RequestType type, RequestForm form, AccessRequest entity) {
        model.addAttribute("form", form);
        model.addAttribute("requestType", type);
        model.addAttribute("entity", entity);
        model.addAttribute("systems", lookupService.activeSystems());
        model.addAttribute("databases", lookupService.activeDatabases());
        model.addAttribute("rights", lookupService.activeRights());
        model.addAttribute("users", lookupService.allUsers());
        // 05B: danh sach phieu 05A dang no de lien ket
        if (type == RequestType.HTKC_05B) {
            model.addAttribute("emergencyOptions",
                    debtService.outstandingEmergencyRequests(userSession.getUserId()));
        }
        // Per-row signing status for detail rows
        if (entity != null) {
            Set<Long> signedDetailIds = requestService.signatures(entity.getId()).stream()
                    .filter(s -> "SUCCESS".equals(s.getResult()) && s.getDetailId() != null)
                    .map(RequestSignature::getDetailId)
                    .collect(Collectors.toSet());
            model.addAttribute("signedDetailIds", signedDetailIds);
        } else {
            model.addAttribute("signedDetailIds", java.util.Collections.emptySet());
        }
        // Current user ID for per-row signing visibility
        model.addAttribute("currentUserId", userSession.getUserId());
        // Current unit ID for pre-registration loading (used by JS)
        model.addAttribute("currentUnitId", userSession.getUnitId());
    }

    private RequestForm toForm(AccessRequest r) {
        RequestForm form = new RequestForm();
        form.setRequestType(r.getRequestType().name());
        form.setRequestSubType(r.getSubType());
        form.setShiftNo(r.getShiftNo());
        form.setAccessNo(r.getAccessNo());
        form.setSystemId(r.getSystemId());
        form.setDatabaseId(r.getDatabaseId());
        form.setReason(r.getReason());
        if (r.getStartTime() != null) {
            form.setStartTime(r.getStartTime().format(HTML_DT));
        }
        if (r.getEndTime() != null) {
            form.setEndTime(r.getEndTime().format(HTML_DT));
        }
        if (r.getExpectedExecutionDate() != null) {
            form.setExpectedExecutionDate(r.getExpectedExecutionDate().format(HTML_DT));
        }

        // Build a map from (targetUserId, systemId, databaseId, objectName) -> preRegistrationId
        // for pre-registration records linked to this request
        List<com.csdl.access.domain.PreRegistrationRequest> linkedPreRegs =
                preRegistrationRepository.findByRequestId(r.getId());
        Map<String, Long> preRegMap = new java.util.HashMap<>();
        for (com.csdl.access.domain.PreRegistrationRequest preReg : linkedPreRegs) {
            String key = preReg.getUserId() + "|" + preReg.getSystemId() + "|"
                    + preReg.getDatabaseId() + "|" + (preReg.getObjectName() != null ? preReg.getObjectName() : "");
            preRegMap.put(key, preReg.getId());
        }

        List<RequestDetail> details = requestService.details(r.getId());
        for (RequestDetail d : details) {
            DetailForm df = new DetailForm();
            df.setId(d.getId());
            df.setSystemId(d.getSystemId());
            df.setDatabaseId(d.getDatabaseId());
            df.setObjectOwner(d.getObjectOwner());
            df.setObjectName(d.getObjectName());
            df.setObjectType(d.getObjectType());
            df.setTargetUserId(d.getTargetUserId());
            df.setAccountOwnerName(d.getAccountOwnerName());
            df.setAccountType(d.getAccountType());
            df.setAccountAction(d.getAccountAction());
            df.setAccessRights(d.getAccessRights());
            df.setQueryAll(d.isQueryAll());
            df.setPurpose(d.getPurpose());

            // Look up linked pre-registration ID
            String key = d.getTargetUserId() + "|" + d.getSystemId() + "|"
                    + d.getDatabaseId() + "|" + (d.getObjectName() != null ? d.getObjectName() : "");
            df.setPreRegistrationId(preRegMap.get(key));

            form.getDetails().add(df);
        }
        // them dong trong de bo sung
        for (int i = 0; i < 3; i++) {
            form.getDetails().add(new DetailForm());
        }
        return form;
    }
}
