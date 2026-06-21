package com.csdl.access.workflow;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RoleCode;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.execution.ExecutionService;
import com.csdl.access.approval.ApprovalService;
import com.csdl.access.request.DetailForm;
import com.csdl.access.request.RequestForm;
import com.csdl.access.request.RequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test luong 01-YCTC nguoi yeu cau thuoc don vi chu quan ung dung
 * (features/approval-processing.md muc 3, request-create.md muc 16).
 */
@SpringBootTest
class WorkflowIntegrationTest {

    @Autowired private RequestService requestService;
    @Autowired private RequestSubmissionService submissionService;
    @Autowired private ApprovalService approvalService;
    @Autowired private ExecutionService executionService;
    @Autowired private AccessRequestRepository requestRepository;

    private static final String OTP = "123456";

    private UserSession session(Long userId, String username, Long unitId, RoleCode role) {
        UserSession s = new UserSession();
        s.setUserId(userId);
        s.setUsername(username);
        s.setUnitId(unitId);
        s.setActiveRole(role);
        return s;
    }

    private RequestForm form01() {
        RequestForm form = new RequestForm();
        form.setRequestType("YCTC_01");
        form.setShiftNo(2);
        form.setAccessNo(1);
        form.setSystemId(1L);     // SYS01, owner unit = 2 (DV-KD) => trung don vi nguoi lap
        form.setDatabaseId(1L);
        form.setReason("Truy xuat du lieu phuc vu doi soat");
        DetailForm d = new DetailForm();
        d.setDatabaseId(1L);
        d.setObjectName("KH_KHACHHANG");
        d.setTargetUserId(2L);    // chinh nguoi lap
        d.setAccessRights("SELECT");
        d.setPurpose("Doi soat");
        form.getDetails().add(d);
        return form;
    }

    @Test
    void fullFlow_01_inOwnerUnit_completes() {
        UserSession requester = session(2L, "requester1", 2L, RoleCode.REQUESTER);

        AccessRequest draft = requestService.createDraft(form01(), requester);
        assertEquals(RequestStatus.DRAFT, draft.getStatus());
        assertNotNull(draft.getRequestCode());

        // Ky xac nhan cua nguoi lap.
        requestService.sign(draft.getId(), OTP,
                com.csdl.access.common.enums.SigningScope.GENERAL, null, requester);

        // Gui phe duyet.
        AccessRequest submitted = submissionService.submit(draft.getId(), null, requester);
        assertEquals(RequestStatus.PENDING_DEPT_APPROVAL, submitted.getStatus());
        assertEquals(RoleCode.DEPT_MANAGER.name(), submitted.getCurrentActorRole());

        // Truong phong phe duyet.
        approvalService.approve(submitted.getId(), OTP, "Dong y",
                session(3L, "manager1", 2L, RoleCode.DEPT_MANAGER));
        AccessRequest afterDept = requestRepository.findById(submitted.getId()).orElseThrow();
        assertEquals(RequestStatus.PENDING_AUTHORITY_APPROVAL, afterDept.getStatus());

        // Nguoi co tham quyen phe duyet -> chuyen bo phan mo truy cap.
        approvalService.approve(submitted.getId(), OTP, "Phe duyet",
                session(4L, "authority1", 2L, RoleCode.AUTHORITY));
        AccessRequest afterAuth = requestRepository.findById(submitted.getId()).orElseThrow();
        assertEquals(RequestStatus.SENT_TO_ACCESS_TEAM, afterAuth.getStatus());
        assertEquals(RoleCode.ACCESS_TEAM.name(), afterAuth.getCurrentActorRole());
        assertNotNull(afterAuth.getApprovedAt());

        // Bo phan mo truy cap xac nhan thuc hien -> hoan thanh.
        executionService.execute(submitted.getId(), OTP,
                "2026-06-17T08:00", "2026-06-17T08:30", "Da mo truy cap",
                session(6L, "access1", 1L, RoleCode.ACCESS_TEAM));
        AccessRequest done = requestRepository.findById(submitted.getId()).orElseThrow();
        assertEquals(RequestStatus.COMPLETED, done.getStatus());
        assertNotNull(done.getCompletedAt());
    }

    @Test
    void submit_01_withoutDetail_isRejected() {
        UserSession requester = session(2L, "requester1", 2L, RoleCode.REQUESTER);
        RequestForm form = form01();
        form.getDetails().clear();
        AccessRequest draft = requestService.createDraft(form, requester);
        requestService.sign(draft.getId(), OTP,
                com.csdl.access.common.enums.SigningScope.GENERAL, null, requester);
        assertThrows(BusinessException.class,
                () -> submissionService.submit(draft.getId(), null, requester));
    }

    @Test
    void return_requiresReason() {
        UserSession requester = session(2L, "requester1", 2L, RoleCode.REQUESTER);
        AccessRequest draft = requestService.createDraft(form01(), requester);
        requestService.sign(draft.getId(), OTP,
                com.csdl.access.common.enums.SigningScope.GENERAL, null, requester);
        AccessRequest submitted = submissionService.submit(draft.getId(), null, requester);

        UserSession manager = session(3L, "manager1", 2L, RoleCode.DEPT_MANAGER);
        assertThrows(BusinessException.class,
                () -> approvalService.returnRequest(submitted.getId(), "  ", manager));

        // Co ly do thi thanh cong.
        approvalService.returnRequest(submitted.getId(), "Thieu thong tin", manager);
        AccessRequest returned = requestRepository.findById(submitted.getId()).orElseThrow();
        assertEquals(RequestStatus.RETURNED, returned.getStatus());
    }
}
