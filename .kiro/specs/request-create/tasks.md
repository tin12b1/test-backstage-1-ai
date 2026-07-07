# Implementation Plan: request-create

## Overview

Triển khai tính năng "Lập và gửi yêu cầu truy cập CSDL" — bao gồm 7 mẫu phiếu, draft/auto-save, ký OTP, khởi tạo workflow, xử lý đồng thời nhiều người ký, và đăng ký trước yêu cầu chi tiết. Sử dụng Java Spring Boot + Thymeleaf, mở rộng các controller/service hiện có và tạo mới các service DraftService, RequestValidationService, SigningService, ConcurrencyHandler, PreRegistrationService.

## Tasks

- [x] 1. Set up project infrastructure and core DTOs
  - [x] 1.1 Add jqwik dependency to pom.xml and create test package structure
    - Add `net.jqwik:jqwik:1.8.3` to `<dependencies>` with `<scope>test</scope>`
    - Create package directory `src/test/java/com/csdl/access/request/`
    - Verify build compiles with `mvn compile`
    - _Requirements: Testing infrastructure_

  - [x] 1.2 Create DTO records for request-create feature
    - Create `AutoSaveResponse`, `SigningStatusResponse`, `DetailSigningStatus`, `FileUploadResponse`, `SignResult`, `EmergencyGroupDto`, `ValidationError`, `DraftInfo`, `AutoSaveResult`, `PreRegistrationDto`, `RequestSummaryDto`, `DetailSummaryDto` records in `com.csdl.access.request.dto` package
    - _Requirements: Design - Data Models DTOs_

  - [x] 1.3 Create `pre_registration_request` table, entity, and repository (thay thế bảng `access_registration` cũ)
    - **⚠️ DB Schema Note:** Bảng `access_registration` hiện tại chưa có Java code nào sử dụng (chỉ có DDL + seed data). Quyết định: XÓA bảng cũ và tạo bảng mới `pre_registration_request` với schema đầy đủ hơn.
    - Tạo migration script `V4__replace_access_registration.sql`:
      - `DROP TABLE access_registration` (hoặc thêm vào `V0__drop.sql`)
      - `CREATE TABLE pre_registration_request` với các cột: id, user_id, unit_code, register_date (DATE), shift (NUMBER), request_type (VARCHAR2 — "Truy vấn"/"Chỉnh sửa"), system_id, database_id, object_name, access_rights (CSV), signed_at (TIMESTAMP), signature_image_id, status (VARCHAR2 — UNUSED/PENDING_APPROVAL/USED/EXPIRED), request_id (FK nullable → access_request.id), created_at, updated_at, version
    - Cập nhật `V0__drop.sql`: đổi `ACCESS_REGISTRATION` thành `PRE_REGISTRATION_REQUEST`
    - Cập nhật `V3__seed_catalog.sql`: đổi INSERT sang bảng `pre_registration_request` với cột mới
    - Cập nhật `docs/database-schema.md` và `docs/api-contract.md` để phản ánh bảng mới
    - Tạo entity `PreRegistrationRequest` trong `com.csdl.access.domain` với `@Table(name = "pre_registration_request")` và toàn bộ fields theo design
    - Tạo `PreRegistrationRepository` interface trong `com.csdl.access.domain.repo` với custom query methods (findByUnitCodeAndRegisterDateAndShiftAndStatus, expireOutdated, existsByUserIdAndRegisterDateAndShiftAndSystemIdAndDatabaseIdAndObjectNameAndAccessRights)
    - _Requirements: 16.1, 16.2, 18.1, 18.2_

  - [x] 1.4 Extend RequestDetailRepository with concurrency queries
    - Add `deleteUnsignedByRequestId` method using `@Modifying` + `@Query`
    - **⚠️ DB Schema Note:** `request_signature.detail_id` có thể NULL (ký phiếu chung không theo dòng). Query cần logic: xóa dòng detail mà KHÔNG có signature record nào với `result='SUCCESS'` AND `detail_id = d.id` (chỉ match non-null detail_id).
    - Add `countByRequestId` method
    - _Requirements: 6.5, 13.1, 13.2, 19.5_

- [x] 2. Implement RequestValidationService
  - [x] 2.1 Create RequestValidationService with mandatory field and format validations
    - Create `com.csdl.access.request.RequestValidationService` class
    - Implement `validateForSubmission(AccessRequest, List<RequestDetail>)` — check all mandatory fields per form type
    - Implement `validateForDraft(RequestForm)` — lightweight format-only checks
    - Implement `validateTimeNotPast(Integer shiftNo, LocalDate date, RequestType type)` — block past date+shift except 04B/05B
    - _Requirements: 5.1, 10.1, 10.6, 16.3_

  - [ ]* 2.2 Write property test: Mandatory field validation completeness (Property 8)
    - **Property 8: Mandatory field validation completeness**
    - For any form submission where at least one mandatory field is empty/null, validateForSubmission SHALL return at least one ValidationError referencing that field
    - **Validates: Requirements 5.1, 10.1**

  - [ ]* 2.3 Write property test: Date+shift past-time blocking (Property 15)
    - **Property 15: Date+shift past-time blocking**
    - For any request type other than 04B/05B, a past date+shift SHALL be rejected. For 04B/05B, past date+shift SHALL be allowed.
    - **Validates: Requirements 10.6, 16.3**

  - [x] 2.4 Implement duplicate detection and single system enforcement validations
    - Implement `checkDuplicateDetails(RequestType, List<RequestDetail>)` — detect duplicates per type rules
    - Implement `validateSingleSystemDatabase(RequestType, AccessRequest)` — enforce exactly 1 system+database for 02/03/04A/05A
    - _Requirements: 10.2, 10.3, 10.4, 10.5_

  - [ ]* 2.5 Write property test: Duplicate detail detection (Property 14)
    - **Property 14: Duplicate detail detection**
    - For 01-YCTC: duplicate = same (systemId, databaseId, objectName, targetUserId). For 04A-YCTK: duplicate = same targetUserId.
    - **Validates: Requirements 10.4, 10.5**

  - [ ]* 2.6 Write property test: Single system+database enforcement (Property 16)
    - **Property 16: Single system+database enforcement**
    - For form types 02/03/04A/05A, validation SHALL require exactly one systemId and one databaseId.
    - **Validates: Requirements 10.2**

  - [x] 2.7 Implement file validation (SQL script + checksum)
    - Implement `validateScriptFile(MultipartFile, String checksumType, String checksumValue)` — check file name format, size ≤ 10MB, compute and compare checksum
    - Implement `validate03TabContent(AccessRequest, boolean hasScript, boolean checksumMatch)` — tab content rules for form 03
    - _Requirements: 5.5, 5.6, 5.7, 5.8, 5.9, 10.7, 10.8, 12.3, 12.4_

  - [ ]* 2.8 Write property test: File name format validation (Property 11)
    - **Property 11: File name format validation**
    - Validate file name accepted IFF matching pattern `YYYYMMDD_BS_XXX.sql`
    - **Validates: Requirements 5.5**

  - [ ]* 2.9 Write property test: Checksum verification (Property 12)
    - **Property 12: Checksum verification**
    - Computed hash matches user-provided string IFF correct hex-encoded hash. MD5 = 32 hex chars, SHA-256 = 64 hex chars.
    - **Validates: Requirements 5.6, 5.7, 10.8**

  - [x] 2.10 Implement debt check validation
    - Implement `hasDebtBlock(Long userId)` — query outstanding 05B debt > 3 days
    - **⚠️ DB Schema Note:** Logic nợ 05B cần query: tìm `access_request` với `request_type='05A'` + `status=COMPLETED` mà KHÔNG có record tương ứng trong `emergency_completion_link.emergency_request_id`. Nếu `completed_at` của 05A cách hiện tại > 3 ngày → user đang nợ. Kiểm tra qua bảng `emergency_completion_link` (FK: `emergency_request_id` → 05A, `completion_request_id` → 05B).
    - _Requirements: 1.4_

  - [ ]* 2.11 Write property test: Debt blocking (Property 1)
    - **Property 1: Chặn nợ 05B — Debt blocking**
    - User with 05B debt > 3 days SHALL be blocked from creating any request except 05B-HTKC.
    - **Validates: Requirements 1.4**

- [x] 3. Implement DraftService
  - [x] 3.1 Create DraftService with saveDraft and autoSave methods
    - Create `com.csdl.access.request.DraftService` class
    - Implement `saveDraft(RequestForm, UserSession)` — create or update entity with status DRAFT, generate request code on first save
    - Implement `autoSave(Long requestId, RequestForm, UserSession)` — dirty check via content hash, update only when changed, return silently
    - Implement `findDraftsForUser(Long userId)` — list existing drafts for recovery
    - _Requirements: 2.1, 2.2, 2.4, 2.5_

  - [ ]* 3.2 Write property test: Draft round-trip persistence (Property 2)
    - **Property 2: Draft round-trip persistence**
    - Save as draft then read back SHALL produce equivalent field values (excluding auto-generated fields).
    - **Validates: Requirements 2.1**

  - [ ]* 3.3 Write property test: Dirty check detection (Property 3)
    - **Property 3: Dirty check detection**
    - Dirty check returns true IFF at least one non-excluded field differs between current and previous form state.
    - **Validates: Requirements 2.2**

  - [ ]* 3.4 Write property test: Status-based action guard (Property 4)
    - **Property 4: Status-based action guard**
    - Edit/cancel allowed only when status ∈ {DRAFT, PENDING_SIGN, RETURNED}. Blocked for all other statuses.
    - **Validates: Requirements 2.5, 2.6, 20.1, 20.2, 20.3**

- [x] 4. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement RequestCodeGenerator extensions
  - [x] 5.1 Extend RequestCodeGenerator with generateByUnit, generateBySystem, and generate05B methods
    - Extend existing `com.csdl.access.request.RequestCodeGenerator`
    - Implement `generateByUnit(Long unitId, Long departmentId)` — format: MãĐơnVị_MãPhòng_yyyyMMddHHmmss
    - Implement `generateBySystem(Long systemId)` — format: KýhiệuHệThống_yyyyMMddHHmmss
    - Implement `generate05B(Long systemId, List<AccessRequest> linked05As)` — format with consolidated "Lần"
    - **⚠️ DB Schema Note:** Cột `access_request.access_no` lưu giá trị "Lần" (1/2/3/4/5). Cần xác định: phần "Lần" auto-increment nằm trong `request_code` string hay lưu riêng ở `access_no`. Với 05B, `access_no` có thể lưu dạng "01-02-03" (consolidated) hoặc null và chỉ hiển thị trong code string. Đề xuất: lưu `access_no` = giá trị auto-increment đơn lẻ cho các phiếu thường, và ghi request_code đầy đủ bao gồm "Lần" cho 05B.
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [ ]* 5.2 Write property test: Request code format correctness (Property 5)
    - **Property 5: Request code format correctness**
    - Generated code SHALL match expected regex pattern per request type (unit-based for 01/04A, system-based for 02/03/05A/05B).
    - **Validates: Requirements 3.1, 3.2, 3.3**

- [x] 6. Implement SigningService
  - [x] 6.1 Create SigningService with signRequest and signReceipt methods
    - Create `com.csdl.access.request.SigningService` class
    - Implement `signRequest(Long requestId, String otp, SigningScope scope, Long detailId, UserSession session)` — verify OTP, record signature, return signature image URL
    - Implement `signReceipt(Long requestId, Long detailId, String otp, UserSession session)` — 04B receiver signs their row, check if all signed → trigger transition
    - Implement `hasAlreadySigned(Long requestId, Long userId)` — enforce 1 signature per user per request
    - Implement `allReceiversSigned(Long requestId)` — check all 04B receivers signed
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 8.7, 8.8_

  - [ ]* 6.2 Write property test: One signature per user per request (Property 6)
    - **Property 6: One signature per user per request**
    - Second signing attempt by the same user on the same request SHALL be rejected.
    - **Validates: Requirements 4.5**

  - [ ]* 6.3 Write property test: Signed row immutability (Property 7)
    - **Property 7: Signed row immutability**
    - Any attempt to modify or delete a signed detail row SHALL be rejected.
    - **Validates: Requirements 4.4, 6.4, 19.4**

  - [ ]* 6.4 Write property test: Receiver signing completion triggers transition (Property 19)
    - **Property 19: Receiver signing completion triggers transition**
    - When last unsigned receiver signs on 04B (PENDING_RECEIPT), system SHALL auto-transition to PENDING_APPROVAL phase 2.
    - **Validates: Requirements 8.7, 8.8**

- [x] 7. Implement ConcurrencyHandler
  - [x] 7.1 Create ConcurrencyHandler with row-level locking and polling support
    - Create `com.csdl.access.request.ConcurrencyHandler` class
    - Implement `getSigningStatus(Long requestId)` — return current signing status of all detail rows
    - Implement `addDetailRow(Long requestId, DetailForm, UserSession)` — optimistic locking on request entity
    - Implement `updateOwnDetail(Long requestId, Long detailId, DetailForm, UserSession)` — validate ownership, check not signed
    - Implement `deleteUnsignedDetail(Long requestId, Long detailId, UserSession)` — only form creator can delete
    - Implement `removeAllUnsignedDetails(Long requestId)` — remove unsigned rows at submission time
    - _Requirements: 6.2, 6.3, 6.4, 6.5, 19.1, 19.2, 19.3, 19.4, 19.5_

  - [ ]* 7.2 Write property test: Submission removes unsigned detail rows (Property 13)
    - **Property 13: Submission removes unsigned detail rows**
    - At submission time for 01-YCTC/04A-YCTK, all unsigned rows SHALL be removed, only signed rows remain.
    - **Validates: Requirements 6.5, 13.1, 13.2**

  - [ ]* 7.3 Write property test: Only form creator can delete unsigned rows (Property 27)
    - **Property 27: Only form creator can delete unsigned rows**
    - Delete unsigned row succeeds only if user is the form creator. Non-creators SHALL be rejected.
    - **Validates: Requirements 19.5**

- [x] 8. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Implement Submission and Workflow initialization
  - [x] 9.1 Extend RequestService with variant determination and submission logic
    - Extend existing `com.csdl.access.request.RequestService`
    - Implement `prepareSubmission(Long id, UserSession session)` — validate, determine variant (I/E), call submission service
    - Implement `determineVariant(AccessRequest)` — compare requester_unit_id with owner_unit_id from InformationSystem
    - **⚠️ DB Schema Note:** Schema có 2 nguồn owner_unit_id: `information_system.owner_unit_id` (chủ quản ứng dụng) và `database_catalog.owner_unit_id` (chủ quản CSDL). Quy tắc: dùng `information_system.owner_unit_id` làm nguồn chính để xác định variant. Cột `access_request.owner_unit_id` lưu kết quả xác định (cache). Nếu form chỉ chọn database mà không chọn system trực tiếp, tra ngược qua `database_catalog.system_id → information_system.owner_unit_id`.
    - _Requirements: 5.2, 14.1, 14.2, 14.3_

  - [ ]* 9.2 Write property test: Variant determination correctness (Property 9)
    - **Property 9: Variant determination correctness**
    - Requester unit == owner unit → Internal (I). Different → External (E). Types without variant (03, 05A) → null.
    - **Validates: Requirements 5.2, 14.1, 14.2, 14.3**

  - [x] 9.3 Extend RequestSubmissionService with workflow initialization
    - Extend existing `com.csdl.access.workflow.RequestSubmissionService`
    - Implement `submit(Long requestId, Long emergencyRequestId, UserSession session)` — set step_code, resolve actor, create workflow_history, send notification
    - Set `current_step_code` format: `{TYPE}_{VARIANT}_{01}` or `{TYPE}_{01}` for no-variant types
    - Set `at_requester_phase` based on variant/step mapping
    - _Requirements: 5.3, 14.4, 14.5, 14.6, 14.7_

  - [ ]* 9.4 Write property test: Workflow initialization correctness (Property 10)
    - **Property 10: Workflow initialization correctness**
    - Submitted request SHALL have correct step_code format, non-null actor role, workflow_history record with action=SUBMIT.
    - **Validates: Requirements 5.3, 14.4, 14.5, 14.6, 14.7**

  - [ ]* 9.5 Write property test: Resubmission restarts workflow (Property 28)
    - **Property 28: Resubmission restarts workflow**
    - RETURNED request resubmitted SHALL transition to appropriate pending status and re-initialize workflow from beginning step.
    - **Validates: Requirements 21.2**

- [x] 10. Implement PreRegistrationService
  - [x] 10.1 Create PreRegistrationService with CRUD and lifecycle management
    - Create `com.csdl.access.request.PreRegistrationService` class
    - Implement `listByUser`, `create`, `update`, `delete`, `clone` operations
    - Implement `loadForForm01(String unitCode, LocalDate date, int shift, String requestType)` — filter and load matching pre-registrations
    - Implement `markAsPending`, `markAsUsed`, `revertToUnused` — status lifecycle transitions
    - Implement `expireOutdatedRegistrations()` — scheduled job for expiry
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 17.1, 17.2, 17.3, 17.6, 17.7, 17.8, 18.1, 18.2_

  - [ ]* 10.2 Write property test: Pre-registration edit/delete status guard (Property 21)
    - **Property 21: Pre-registration edit/delete status guard**
    - Edit/delete succeed only when status="UNUSED". All other statuses SHALL reject.
    - **Validates: Requirements 16.5, 16.6**

  - [ ]* 10.3 Write property test: Pre-registration duplicate blocking (Property 22)
    - **Property 22: Pre-registration duplicate blocking**
    - Creation rejected when record exists with same (userId, registerDate, shift, systemId, databaseId, objectName, accessRights).
    - **Validates: Requirements 16.4**

  - [ ]* 10.4 Write property test: Pre-registration loading filter correctness (Property 23)
    - **Property 23: Pre-registration loading filter correctness**
    - Load results match all parameters + status=UNUSED. "Truy vấn" loads only Truy vấn. "Chỉnh sửa" loads both.
    - **Validates: Requirements 17.1, 17.2, 17.3**

  - [ ]* 10.5 Write property test: Pre-registration status lifecycle (Property 24)
    - **Property 24: Pre-registration status lifecycle**
    - Submit → PENDING_APPROVAL; Cancel → UNUSED; Complete → USED.
    - **Validates: Requirements 17.6, 17.7, 17.8**

  - [ ]* 10.6 Write property test: Pre-registration type change removes incompatible rows (Property 25)
    - **Property 25: Pre-registration type change removes incompatible rows**
    - Change from "Chỉnh sửa" to "Truy vấn" SHALL remove rows with INSERT/UPDATE/DELETE rights and revert their status to UNUSED.
    - **Validates: Requirements 17.5**

  - [ ]* 10.7 Write property test: Pre-registration expiry (Property 26)
    - **Property 26: Pre-registration expiry**
    - UNUSED records with past date+shift SHALL be updated to EXPIRED.
    - **Validates: Requirements 18.2**

- [x] 11. Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 12. Implement RequestController extensions and PreRegistrationController
  - [x] 12.1 Extend RequestController with new AJAX endpoints
    - Add `autoSave` endpoint — `POST /{id}/auto-save` → delegates to DraftService.autoSave
    - Add `signingStatus` endpoint — `GET /{id}/signing-status` → delegates to ConcurrencyHandler.getSigningStatus
    - Add `uploadScript` endpoint — `POST /{id}/upload-script` → validates file + stores
    - Add `deleteUnsignedDetail` endpoint — `DELETE /{id}/details/{detailId}` → delegates to ConcurrencyHandler
    - Add `pending04A` endpoint — `GET /pending-04a` → query completed 04A without linked 04B
    - Add `pending05AGroups` endpoint — `GET /pending-05a-groups` → group outstanding 05A by system+db+date+shift
    - _Requirements: 2.2, 8.2, 9.1, 19.2, 19.5_

  - [ ]* 12.2 Write property test: 04A filtering for 04B creation (Property 17)
    - **Property 17: 04A filtering for 04B creation**
    - Only 04A requests with status=COMPLETED and no linked 04B SHALL be displayed.
    - **Validates: Requirements 8.2**

  - [ ]* 12.3 Write property test: 05A grouping for 05B (Property 20)
    - **Property 20: 05A grouping for 05B**
    - Outstanding 05A requests SHALL be grouped by (systemId, databaseId, date, shift) with union of detail tables.
    - **Validates: Requirements 9.1, 9.2**

  - [x] 12.4 Create PreRegistrationController
    - Create `com.csdl.access.request.PreRegistrationController`
    - Implement CRUD endpoints: list, create, edit, update, delete, clone
    - Implement `loadForForm` AJAX endpoint — `GET /pre-registrations/load`
    - _Requirements: 16.1, 16.2, 16.5, 16.6, 17.1_

- [x] 13. Implement 04B auto-fill and 05B consolidation logic
  - [x] 13.1 Implement 04B creation from completed 04A
    - Add method in RequestService to create 04B from selected 04A: auto-fill system, database, 04A code, handover date, DBA info, receiver list
    - Leave UserID field empty for DBA manual input
    - **⚠️ DB Schema Note:** Receiver list (nhiều người nhận) lấy từ `request_detail.target_user_id` của phiếu 04A nguồn (N dòng detail = N receivers). Cột `access_request.receiver_user_id` trên header 04B chỉ lưu 1 người — dùng cho trường hợp đơn giản hoặc deprecated. Logic chính: tạo N dòng `request_detail` trên phiếu 04B tương ứng N users từ 04A, mỗi dòng có `target_user_id` = receiver. Cột `source_request_id` trên header 04B lưu FK về phiếu 04A.
    - _Requirements: 8.3, 8.4, 8.5_

  - [ ]* 13.2 Write property test: 04B auto-fill data mapping (Property 18)
    - **Property 18: 04B auto-fill data mapping**
    - Creating 04B from 04A SHALL correctly map systemId, databaseId, requester info, detail rows with UserID empty.
    - **Validates: Requirements 8.3, 8.4**

  - [x] 13.3 Implement 05B consolidation from grouped 05A requests
    - Add method in RequestService to create 05B from selected 05A group: auto-fill system, database, shift, time range, union of detail tables
    - Generate 05B code with consolidated "Lần" values
    - **⚠️ DB Schema Note:** Bảng `emergency_completion_link` hỗ trợ N:1 (nhiều 05A → 1 phiếu 05B): insert N rows với cùng `completion_request_id` và khác `emergency_request_id`. Khi tạo 05B, insert N records vào `emergency_completion_link` tương ứng N phiếu 05A được gộp.
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 14. Implement Thymeleaf views and JavaScript client logic
  - [-] 14.1 Create Thymeleaf fragments for shared form components
    - Create `templates/requests/fragments/header-info.html` — common header (unit, dept, requester, shift, date)
    - Create `templates/requests/fragments/signing-panel.html` — OTP input + signature display
    - Create `templates/requests/fragments/file-upload.html` — file upload + checksum input
    - Create `templates/requests/fragments/readonly-approval.html` — read-only approval section
    - Create `templates/requests/fragments/readonly-execution.html` — read-only execution section
    - Create `templates/requests/fragments/readonly-dba.html` — DBA notes section (form 03)
    - _Requirements: 22.1, 22.2, 22.3, 22.4_

  - [-] 14.2 Create form type-specific detail table fragments
    - Create `templates/requests/fragments/detail-table-01.html` — 01-YCTC multi-row detail table with access rights checkboxes
    - Create `templates/requests/fragments/detail-table-04a.html` — 04A-YCTK detail table
    - Create `templates/requests/fragments/detail-table-04b.html` — 04B-BGTK detail table with receiver signing
    - _Requirements: 6.1, 6.2, 8.3, 11.1, 11.2, 11.4_

  - [-] 14.3 Create main request form template with conditional rendering
    - Create/update `templates/requests/form.html` — single template with `th:switch` / `th:if` for each form type
    - Include type selector, header fragment, type-specific detail table fragment, signing panel, read-only sections
    - Wire form submission buttons: "Lưu nháp", "Ký xác nhận & Gửi", "Lưu phiếu" (branch A), "Hủy phiếu"
    - _Requirements: 1.1, 1.3, 6.1, 6.6, 6.7_

  - [x] 14.4 Implement JavaScript auto-save, polling, and client-side retry logic
    - Create `static/js/request-form.js` with:
      - Auto-save timer (30s interval with dirty check via content hash)
      - sessionStorage save on every field change
      - Draft recovery dialog on page load
      - Polling for signing-status endpoint (5s interval for multi-signer forms)
      - Submit with retry (3 attempts × 5s delay, session expiry detection)
      - Network offline/online banner
    - _Requirements: 2.2, 2.3, 2.4, 15.1, 15.2, 15.3, 15.4, 19.2_

  - [-] 14.5 Create pre-registration list and form templates
    - Create `templates/requests/pre-registration/list.html` — paginated list with status badges
    - Create `templates/requests/pre-registration/form.html` — registration form with OTP signing
    - _Requirements: 16.1, 16.2_

- [ ] 15. Implement Scheduled Jobs and Notification integration
  - [x] 15.1 Create RequestScheduledTasks for pre-registration expiry and 04B reminders
    - Create `com.csdl.access.request.RequestScheduledTasks` class
    - Implement `expirePreRegistrations()` — cron every hour, call PreRegistrationService.expireOutdatedRegistrations
    - Implement `remind04BPendingReceipt()` — cron daily 8AM, find overdue 04B > 3 days, send reminders
    - _Requirements: 13.3, 13.4, 18.1, 18.2_

  - [-] 15.2 Wire NotificationService calls into submission and transition flows
    - Add notification calls in RequestSubmissionService for: submit → notify next actor, 04B approve → notify receivers, cancel/return → notify requester
    - _Requirements: 5.4, 7.2, 8.6, 8.9, 23.1, 23.2, 23.3, 23.4, 23.5_

- [x] 16. Implement Form 01-YCTC access rights rules and form 03 tab logic
  - [x] 16.1 Implement access rights toggle logic for form 01-YCTC
    - Add JavaScript logic: "Truy vấn" → auto-check SELECT only, disable others; "Chỉnh sửa" → enable multi-select
    - Add type change confirmation dialog — warn about access rights reset
    - Wire pre-registration loading on shift selection (AJAX call to `/pre-registrations/load`)
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 17.1, 17.4_

  - [x] 16.2 Implement form 03-YCCT tab display logic
    - Add JavaScript: show/hide tabs based on "Loại yêu cầu" checkboxes (Tạo mới / Thay đổi / Xóa)
    - Wire SQL script upload section with checksum validation feedback
    - _Requirements: 12.1, 12.2, 12.3, 12.4_

- [x] 17. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik (28 properties total)
- Unit tests validate specific examples and edge cases
- The project already has existing entities (AccessRequest, RequestDetail, RequestSignature, etc.) and basic controllers — this plan extends them
- All new services are created in `com.csdl.access.request` package except workflow extensions in `com.csdl.access.workflow`
- Thymeleaf templates use a single `form.html` with conditional rendering per form type to maximize fragment reuse

### ⚠️ Lưu ý quan trọng về CSDL

Các task có đánh dấu **⚠️ DB Schema Note** cần đặc biệt chú ý khi triển khai:

| Task | Vấn đề CSDL | Mức ảnh hưởng |
|---|---|---|
| **1.3** | Tạo bảng mới `pre_registration_request` thay thế `access_registration` cũ (xóa bảng cũ + cập nhật DDL/seed/docs) | Trung bình |
| **1.4** | `request_signature.detail_id` nullable — cần handle trong query join | Thấp |
| **2.10** | Logic nợ 05B query qua `emergency_completion_link` | Thấp |
| **5.1** | Format mã yêu cầu vs cột `access_no` — cần thống nhất cách lưu "Lần" | Trung bình |
| **9.1** | Chọn `owner_unit_id` từ `information_system` (primary) — có fallback qua `database_catalog` | Thấp |
| **13.1** | Receiver list lấy từ `request_detail` (N users), không chỉ 1 `receiver_user_id` trên header | Trung bình |
| **13.3** | Insert N rows vào `emergency_completion_link` cho 1 phiếu 05B gộp nhiều 05A | Thấp |

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["1.3", "1.4"] },
    { "id": 2, "tasks": ["2.1", "3.1", "5.1"] },
    { "id": 3, "tasks": ["2.2", "2.3", "2.4", "3.2", "3.3", "3.4", "5.2"] },
    { "id": 4, "tasks": ["2.5", "2.6", "2.7", "2.10"] },
    { "id": 5, "tasks": ["2.8", "2.9", "2.11", "6.1", "7.1"] },
    { "id": 6, "tasks": ["6.2", "6.3", "6.4", "7.2", "7.3"] },
    { "id": 7, "tasks": ["9.1", "9.3", "10.1"] },
    { "id": 8, "tasks": ["9.2", "9.4", "9.5", "10.2", "10.3", "10.4", "10.5", "10.6", "10.7"] },
    { "id": 9, "tasks": ["12.1", "12.4", "13.1", "13.3"] },
    { "id": 10, "tasks": ["12.2", "12.3", "13.2"] },
    { "id": 11, "tasks": ["14.1", "14.2", "15.1", "15.2"] },
    { "id": 12, "tasks": ["14.3", "14.5", "16.1", "16.2"] },
    { "id": 13, "tasks": ["14.4"] }
  ]
}
```
