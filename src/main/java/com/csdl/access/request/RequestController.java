package com.csdl.access.request;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.enums.SigningScope;
import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.RequestDetail;
import com.csdl.access.workflow.RequestSubmissionService;
import com.csdl.access.workflow.WorkflowHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public RequestController(RequestService requestService,
                             RequestSubmissionService submissionService,
                             EmergencyDebtService debtService,
                             WorkflowHistoryService historyService,
                             LookupService lookupService,
                             UserSession userSession) {
        this.requestService = requestService;
        this.submissionService = submissionService;
        this.debtService = debtService;
        this.historyService = historyService;
        this.lookupService = lookupService;
        this.userSession = userSession;
    }

    @GetMapping
    public String myRequests(Model model) {
        List<com.csdl.access.common.lookup.RequestRow> rows = new ArrayList<>();
        for (AccessRequest r : requestService.myRequests(userSession)) {
            rows.add(lookupService.toRow(r));
        }
        model.addAttribute("rows", rows);
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
    public String createDraft(@ModelAttribute RequestForm form, RedirectAttributes ra) {
        AccessRequest r = requestService.createDraft(form, userSession);
        ra.addFlashAttribute("infoMessage", "Da luu nhap. Ma yeu cau: " + r.getRequestCode());
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
    public String updateDraft(@PathVariable Long id, @ModelAttribute RequestForm form, RedirectAttributes ra) {
        requestService.updateDraft(id, form, userSession);
        ra.addFlashAttribute("infoMessage", "Da cap nhat nhap.");
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
        model.addAttribute("signatures", requestService.signatures(id));
        model.addAttribute("history", historyService.history(id));
        model.addAttribute("lookup", lookupService);
        boolean isOwner = r.getRequesterUserId().equals(userSession.getUserId());
        model.addAttribute("isOwner", isOwner);
        return "requests/view";
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
    }

    private RequestForm toForm(AccessRequest r) {
        RequestForm form = new RequestForm();
        form.setRequestType(r.getRequestType().name());
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
        List<RequestDetail> details = requestService.details(r.getId());
        for (RequestDetail d : details) {
            DetailForm df = new DetailForm();
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
            form.getDetails().add(df);
        }
        // them dong trong de bo sung
        for (int i = 0; i < 3; i++) {
            form.getDetails().add(new DetailForm());
        }
        return form;
    }
}
