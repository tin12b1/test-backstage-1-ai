package com.csdl.access.approval;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.execution.ExecutionService;
import com.csdl.access.request.RequestService;
import com.csdl.access.workflow.WorkflowHistoryService;
import com.csdl.access.workflow.WorkflowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Man hinh cong viec va xu ly phe duyet/chuyen tra/thuc hien
 * (features/approval-processing.md, api-contract.md muc 5).
 */
@Controller
public class WorkItemController {

    private final WorkItemService workItemService;
    private final ApprovalService approvalService;
    private final ExecutionService executionService;
    private final RequestService requestService;
    private final WorkflowService workflowService;
    private final WorkflowHistoryService historyService;
    private final LookupService lookupService;
    private final UserSession userSession;

    public WorkItemController(WorkItemService workItemService,
                              ApprovalService approvalService,
                              ExecutionService executionService,
                              RequestService requestService,
                              WorkflowService workflowService,
                              WorkflowHistoryService historyService,
                              LookupService lookupService,
                              UserSession userSession) {
        this.workItemService = workItemService;
        this.approvalService = approvalService;
        this.executionService = executionService;
        this.requestService = requestService;
        this.workflowService = workflowService;
        this.historyService = historyService;
        this.lookupService = lookupService;
        this.userSession = userSession;
    }

    @GetMapping("/work-items")
    public String workItems(Model model) {
        model.addAttribute("rows", workItemService.pendingRows(userSession));
        model.addAttribute("activeRole", userSession.getActiveRole());
        return "work-items/list";
    }

    @GetMapping("/work-items/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AccessRequest r = requestService.get(id);
        RoleCode role = userSession.getActiveRole();
        model.addAttribute("request", r);
        model.addAttribute("row", lookupService.toRow(r));
        model.addAttribute("details", requestService.details(id));
        model.addAttribute("signatures", requestService.signatures(id));
        model.addAttribute("history", historyService.history(id));
        model.addAttribute("lookup", lookupService);
        model.addAttribute("canAct", workflowService.isCurrentActor(r, role));
        model.addAttribute("isExecutionStep", workflowService.isExecutionStep(r));
        return "approval/detail";
    }

    @PostMapping("/requests/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam String otp,
                          @RequestParam(required = false) String comment,
                          RedirectAttributes ra) {
        approvalService.approve(id, otp, comment, userSession);
        ra.addFlashAttribute("infoMessage", "Da phe duyet va chuyen buoc tiep theo.");
        return "redirect:/work-items";
    }

    @PostMapping("/requests/{id}/return")
    public String returnRequest(@PathVariable Long id,
                                @RequestParam String reason,
                                RedirectAttributes ra) {
        approvalService.returnRequest(id, reason, userSession);
        ra.addFlashAttribute("infoMessage", "Da chuyen tra yeu cau.");
        return "redirect:/work-items";
    }

    @PostMapping("/requests/{id}/execute")
    public String execute(@PathVariable Long id,
                          @RequestParam String otp,
                          @RequestParam(required = false) String executionStartTime,
                          @RequestParam(required = false) String executionEndTime,
                          @RequestParam(required = false) String executionNote,
                          RedirectAttributes ra) {
        executionService.execute(id, otp, executionStartTime, executionEndTime, executionNote, userSession);
        ra.addFlashAttribute("infoMessage", "Da xac nhan thuc hien, phieu hoan thanh.");
        return "redirect:/work-items";
    }
}
