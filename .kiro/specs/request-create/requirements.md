# Requirements Document

## Introduction

Tính năng cho phép người dùng đăng nhập, chọn mẫu phiếu yêu cầu (7 loại), nhập thông tin, ký xác nhận bằng OTP, lưu nháp, gửi phê duyệt hoặc gửi thẳng bộ phận thực hiện. Hệ thống hỗ trợ 4 luồng nghiệp vụ chính: gửi kiểm tra (02/03), phiếu nhiều người ký (01/04A), truy cập khẩn cấp (05A), và phiếu bổ sung sau hoàn thành (04B/05B). Bao gồm cả chức năng "Đăng ký trước Yêu cầu chi tiết" dành cho mẫu 01-YCTC.

## Glossary

- **Request_System**: Hệ thống quản lý truy cập CSDL (ứng dụng Spring Boot + Thymeleaf)
- **Requester**: Người lập yêu cầu — cán bộ đăng nhập bằng tài khoản AD
- **Form_Selector**: Màn hình chọn mẫu phiếu yêu cầu
- **Request_Form**: Form nhập liệu tương ứng với từng mẫu phiếu
- **Request_Code_Generator**: Module sinh mã yêu cầu tự động theo format quy định
- **Validation_Engine**: Module kiểm tra nghiệp vụ và dữ liệu đầu vào
- **Signing_Module**: Module ký xác nhận bằng OTP (Google Authenticator / ESB)
- **Submission_Service**: Module gửi phiếu vào luồng workflow (xác định variant, step code, actor)
- **Notification_Service**: Module gửi email thông báo tự động khi chuyển trạng thái
- **Draft_Service**: Module lưu nháp (auto-save 30s, local draft sessionStorage)
- **Pre_Registration_Module**: Module đăng ký trước yêu cầu chi tiết cho mẫu 01-YCTC
- **Concurrency_Handler**: Module xử lý đồng thời nhiều người ký trên cùng phiếu (row-level locking + polling)
- **Checksum_Validator**: Module kiểm tra tính toàn vẹn file SQL (MD5/SHA-256)
- **Debt_Checker**: Module kiểm tra nợ phiếu 05B quá hạn

## Requirements

### Requirement 1: Chọn mẫu phiếu yêu cầu

**User Story:** As a Requester, I want to choose the appropriate request form type, so that I can create a request matching my access need.

#### Acceptance Criteria

1. WHEN Requester navigates to "Lập yêu cầu", THE Form_Selector SHALL display all 7 form types (01-YCTC, 02-YCCS, 03-YCCT, 04A-YCTK, 04B-BGTK, 05A-YCKC, 05B-HTKC) based on user permissions
2. WHILE Requester does not have DBA role, THE Form_Selector SHALL hide option 04B-BGTK
3. WHEN Requester selects a form type, THE Request_System SHALL navigate to the corresponding Request_Form with pre-populated user information (name, unit, department, phone)
4. WHEN Requester has outstanding 05B debt exceeding 3 days, THE Debt_Checker SHALL block all form types except 05B-HTKC and display message with link to 05B form

### Requirement 2: Lưu nháp và Auto-save

**User Story:** As a Requester, I want to save my work in progress, so that I do not lose data if interrupted.

#### Acceptance Criteria

1. WHEN Requester clicks "Lưu nháp", THE Draft_Service SHALL save form data to server with status DRAFT
2. WHILE Request_Form is open and has unsaved changes, THE Draft_Service SHALL auto-save to server every 30 seconds (silent, dirty check)
3. WHILE Request_Form is open, THE Draft_Service SHALL save form content to sessionStorage on every field change (excluding signatures and files)
4. WHEN Requester logs in and a local draft exists for the same form, THE Draft_Service SHALL prompt Requester to restore the draft and fill the form upon confirmation
5. WHILE status is DRAFT, THE Request_System SHALL allow Requester to edit all fields of the request
6. WHILE status is not DRAFT and not RETURNED, THE Request_System SHALL prevent editing of request content

### Requirement 3: Sinh mã yêu cầu

**User Story:** As a Requester, I want a unique request code generated automatically, so that each request is identifiable.

#### Acceptance Criteria

1. WHEN Requester saves or submits a request, THE Request_Code_Generator SHALL generate a unique code in format: KýhiệuĐV_DDMMYYYYCa:Lần (where Lần auto-increments per unit per shift per day)
2. THE Request_Code_Generator SHALL use the unit code from the logged-in user information for 01-YCTC and 04A-YCTK
3. WHEN request type is 02-YCCS, 03-YCCT, 05A-YCKC, or 05B-HTKC, THE Request_Code_Generator SHALL use the information system code in the request code
4. WHEN request type is 05B-HTKC, THE Request_Code_Generator SHALL display the "Lần" field as a consolidated list (e.g., Lan01-02-03) from linked 05A requests

### Requirement 4: Ký xác nhận bằng OTP

**User Story:** As a Requester, I want to digitally sign my request using OTP, so that the request is authenticated.

#### Acceptance Criteria

1. WHEN Requester clicks "Ký xác nhận", THE Signing_Module SHALL prompt for OTP input and verify against Google Authenticator or ESB OTP service
2. WHEN OTP verification succeeds, THE Signing_Module SHALL record signature with timestamp and display the signature image on the form
3. IF OTP verification fails, THEN THE Signing_Module SHALL display error message and allow retry without losing form data
4. WHEN a detail row has been signed by the target user, THE Request_System SHALL lock that row content as read-only
5. THE Signing_Module SHALL enforce one signature per user per request (one detail row only)

### Requirement 5: Gửi phiếu 02-YCCS và 03-YCCT (Luồng gửi kiểm tra)

**User Story:** As a Requester, I want to submit forms 02-YCCS and 03-YCCT for inspection, so that the checking team can review my data change request.

#### Acceptance Criteria

1. WHEN Requester signs and submits form 02-YCCS or 03-YCCT, THE Validation_Engine SHALL verify all mandatory fields and validation rules before proceeding
2. WHEN validation passes, THE Submission_Service SHALL set status to PENDING_CHECK and determine variant (Internal/External) based on requester unit vs system owner unit
3. WHEN submission completes, THE Submission_Service SHALL set current_step_code to the first step (e.g., 02_I_01 or 02_E_01) and resolve the next actor
4. WHEN submission completes, THE Notification_Service SHALL send email notification to the Checking team of the application owner unit
5. WHEN form is 02-YCCS, THE Validation_Engine SHALL verify file name matches format YYYYMMDD_BS_XXX.sql
6. WHEN form is 02-YCCS, THE Checksum_Validator SHALL compute file hash (MD5 or SHA-256) and compare with user-provided checksum value
7. IF checksum does not match, THEN THE Checksum_Validator SHALL block submission with error "Mã kiểm tra không khớp"
8. WHEN form is 03-YCCT with SQL Script file and matching checksum, THE Validation_Engine SHALL not require detail content in the tabs (Tạo mới/Thay đổi/Xóa)
9. WHEN form is 03-YCCT without SQL Script file, THE Validation_Engine SHALL require data in all selected tabs (at least 1 entry per tab)

### Requirement 6: Gửi phiếu 01-YCTC và 04A-YCTK (Luồng nhiều người ký)

**User Story:** As a Requester, I want to create a multi-signer request (01/04A) and either send immediately or save for others to co-sign, so that team members can share one request form.

#### Acceptance Criteria

1. WHEN Requester chooses "Lưu phiếu" (Branch A) on form 01-YCTC or 04A-YCTK, THE Request_System SHALL set status to PENDING_SIGN and generate request code
2. WHILE status is PENDING_SIGN, THE Request_System SHALL allow users from the same unit to add new detail rows or edit their own existing row
3. WHILE status is PENDING_SIGN, THE Concurrency_Handler SHALL use row-level locking and polling to prevent edit conflicts between concurrent users
4. WHEN a co-signer signs their detail row, THE Signing_Module SHALL lock that row and update the form view for all other users via polling
5. WHEN Requester chooses "Ký xác nhận & Gửi phê duyệt", THE Request_System SHALL remove all unsigned detail rows, set status to PENDING_APPROVAL, and send notification to the department manager
6. WHEN Requester chooses "Hủy phiếu", THE Request_System SHALL set status to CANCELLED without requiring a reason
7. WHEN Requester chooses "Ký xác nhận & Gửi" (Branch B - sole signer), THE Request_System SHALL set status to PENDING_APPROVAL directly and send notification to the department manager
8. WHEN form 01-YCTC has no detail rows with valid signatures at submission time, THE Validation_Engine SHALL block submission

### Requirement 7: Gửi phiếu 05A-YCKC (Truy cập khẩn cấp)

**User Story:** As a Requester, I want to submit an emergency access request (05A) directly to the Access Team, so that urgent access is granted without approval delay.

#### Acceptance Criteria

1. WHEN Requester submits form 05A-YCKC, THE Submission_Service SHALL set status to PENDING_ACCESS_TEAM and current_step_code to 05A_01
2. WHEN submission completes, THE Notification_Service SHALL send email notification directly to the Access Team (Bộ phận Mở truy cập)
3. WHEN Requester selects Ca, THE Request_Form SHALL auto-fill time range (Từ/Đến) based on the shift and prevent manual editing
4. THE Request_Form SHALL display "Thực hiện mở truy cập" section as empty read-only fields (belongs to execution scope)

### Requirement 8: Lập phiếu 04B-BGTK (Biên bản bàn giao tài khoản)

**User Story:** As a DBA, I want to create a handover record (04B) after completing account provisioning (04A), so that the account handover is formally documented.

#### Acceptance Criteria

1. WHILE user does not have DBA role, THE Form_Selector SHALL not display form type 04B-BGTK
2. WHEN DBA selects form 04B-BGTK, THE Request_System SHALL display list of completed 04A-YCTK requests that do not yet have a corresponding 04B
3. WHEN DBA selects a pending 04A request, THE Request_Form SHALL auto-fill: system name, database name, related 04A code, handover date (today), DBA representative (from config), DBA name (logged-in user), receiver manager (04A requester manager), receiver list (users from 04A details)
4. THE Request_Form SHALL leave "Tài khoản (UserID)" field empty for DBA to input manually for each detail row
5. WHEN DBA signs and submits form 04B, THE Submission_Service SHALL set status to PENDING_APPROVAL and send notification to DBA department manager
6. WHEN DBA department manager approves 04B (step 04B_01), THE Request_System SHALL transition status to PENDING_RECEIPT and notify all receiver users
7. WHILE status is PENDING_RECEIPT, THE Request_System SHALL allow each receiver user to sign their own detail row (row-level locking + polling)
8. WHEN all receiver users have signed, THE Request_System SHALL automatically transition to PENDING_APPROVAL (phase 2) for requester department manager approval
9. IF 3 days pass since PENDING_RECEIPT without all signatures, THEN THE Notification_Service SHALL send reminder emails to DBA, DBA manager, and unsigned user managers without cancelling the request

### Requirement 9: Lập phiếu 05B-HTKC (Hoàn thành truy cập khẩn cấp)

**User Story:** As a Requester who used emergency access (05A), I want to submit a completion report (05B), so that my emergency access usage is documented.

#### Acceptance Criteria

1. WHEN Requester selects form 05B-HTKC, THE Request_System SHALL display consolidated list of outstanding 05A items grouped by System + Database + Date + Shift
2. WHEN Requester selects a consolidated item, THE Request_Form SHALL auto-fill: system, database, shift, time range, and union of all tables from related 05A requests
3. THE Request_Form SHALL require Requester to input work description (detailed content and SQL statements executed) as mandatory field
4. WHEN Requester signs and submits form 05B, THE Submission_Service SHALL set status to PENDING_APPROVAL and send notification to Requester department manager
5. WHEN request type is 05B, THE Request_Code_Generator SHALL use 05A code format with consolidated "Lần" values (e.g., Lan01-02-03)

### Requirement 10: Validation nghiệp vụ chung

**User Story:** As a Requester, I want the system to enforce business rules, so that invalid requests are caught before submission.

#### Acceptance Criteria

1. THE Validation_Engine SHALL verify all mandatory fields are filled before allowing sign or submit actions
2. WHEN form type is 02-YCCS, 03-YCCT, 04A-YCTK, or 05A-YCKC, THE Validation_Engine SHALL enforce selection of exactly 1 system and 1 database per request
3. WHEN form type is 01-YCTC, THE Validation_Engine SHALL allow different systems and databases per detail row
4. WHEN form 01-YCTC has duplicate combination of System + Database + Object + User within the same request, THE Validation_Engine SHALL block with duplication error
5. WHEN form 04A-YCTK has duplicate user in the detail table, THE Validation_Engine SHALL block with duplication error
6. THE Validation_Engine SHALL enforce that date + shift selection is current or future only (except for 04B and 05B supplementary forms)
7. WHEN file size exceeds 10MB for SQL file upload (forms 02/03), THE Validation_Engine SHALL reject the file with error message
8. WHEN checksum format is invalid (MD5 must be 32 hex chars, SHA-256 must be 64 hex chars), THE Validation_Engine SHALL display format validation error

### Requirement 11: Quy tắc riêng mẫu 01-YCTC (Loại yêu cầu và Quyền truy cập)

**User Story:** As a Requester, I want the access rights to automatically adjust based on request type, so that permissions are correctly scoped.

#### Acceptance Criteria

1. WHEN Requester selects "Loại yêu cầu" = "Truy vấn" on form 01-YCTC, THE Request_Form SHALL auto-check SELECT only and disable other access right checkboxes
2. WHEN Requester selects "Loại yêu cầu" = "Chỉnh sửa" on form 01-YCTC, THE Request_Form SHALL enable multi-select from SELECT, INSERT, UPDATE, DELETE (at least 1 must be selected)
3. WHEN Requester changes "Loại yêu cầu" after detail rows have been filled, THE Request_Form SHALL display confirmation warning "Thay đổi loại yêu cầu sẽ reset quyền truy cập đã chọn" and reset all access rights upon confirmation
4. THE Request_Form SHALL display access rights as a multi-select checkbox group with 4 visible options (not a dropdown)

### Requirement 12: Quy tắc riêng mẫu 03-YCCT (Loại yêu cầu thay đổi cấu trúc)

**User Story:** As a Requester, I want to specify the type of structural change, so that the appropriate detail tabs are shown.

#### Acceptance Criteria

1. WHEN Requester selects "Loại yêu cầu" checkboxes on form 03-YCCT, THE Request_Form SHALL display corresponding tabs: "Tạo mới" tab when "Tạo mới" is checked, "Thay đổi" tab when "Thay đổi" is checked, "Xóa" tab when "Xóa" is checked
2. THE Validation_Engine SHALL require at least 1 checkbox selected from Tạo mới / Thay đổi / Xóa
3. WHEN SQL Script file is uploaded with valid checksum, THE Validation_Engine SHALL treat detail tab content as optional
4. WHEN no SQL Script file is provided, THE Validation_Engine SHALL require content in all selected tabs

### Requirement 13: Timeout và xử lý hết hạn ký

**User Story:** As a Requester, I want unsigned rows to be automatically handled at deadline, so that the request can proceed without indefinite waiting.

#### Acceptance Criteria

1. WHEN Requester submits form 01-YCTC after the selected shift has ended, THE Request_System SHALL automatically remove unsigned detail rows before final submission
2. WHEN Requester submits form 04A-YCTK after the creation date has passed, THE Request_System SHALL automatically remove unsigned detail rows before final submission
3. WHEN 04B-BGTK remains in PENDING_RECEIPT status for 3 days, THE Notification_Service SHALL send reminder emails to DBA, DBA manager, and managers of unsigned users
4. WHEN 04B-BGTK timeout occurs, THE Request_System SHALL keep the request in PENDING_RECEIPT status without cancelling

### Requirement 14: Khởi tạo Workflow khi Submit

**User Story:** As a Requester, I want the system to correctly initialize the workflow upon submission, so that the request routes to the appropriate approver.

#### Acceptance Criteria

1. WHEN Requester submits a request, THE Submission_Service SHALL determine variant (Internal/External) by comparing requester_unit_id with owner_unit_id from information_system
2. WHEN requester_unit_id equals owner_unit_id, THE Submission_Service SHALL set variant to Internal (I)
3. WHEN requester_unit_id differs from owner_unit_id, THE Submission_Service SHALL set variant to External (E)
4. WHEN variant is determined, THE Submission_Service SHALL set current_step_code in format {REQUEST_TYPE}_{VARIANT}_{01} or {REQUEST_TYPE}_{01} if no variant applies
5. WHEN submission completes, THE Submission_Service SHALL call resolveNextActor to determine current_actor_type, current_actor_id, current_actor_role, and current_unit_id
6. WHEN submission completes, THE Submission_Service SHALL record workflow_history entry with action = SUBMIT and the initial step_code
7. WHEN submission completes, THE Submission_Service SHALL set at_requester_phase according to the variant/step mapping (Internal always false, External steps 01-02 true)

### Requirement 15: Xử lý gửi thất bại và phục hồi

**User Story:** As a Requester, I want the system to handle network failures gracefully, so that my data is not lost.

#### Acceptance Criteria

1. IF network error occurs during submission, THEN THE Request_System SHALL retry up to 3 times with 5-second intervals (only for network errors)
2. IF session has expired during submission, THEN THE Request_System SHALL stop retry, display "Phiên đăng nhập hết hạn" message, and prompt re-login
3. WHILE network is completely unavailable, THE Request_System SHALL display connection error banner and preserve form state until network recovery
4. WHEN Requester logs in after interrupted session and local draft exists, THE Draft_Service SHALL detect and offer to restore the draft for review and resubmission

### Requirement 16: Đăng ký trước Yêu cầu chi tiết (Mẫu 01-YCTC)

**User Story:** As a Requester, I want to pre-register my access details before a request is created, so that the form is auto-populated when someone creates a 01-YCTC form for my shift.

#### Acceptance Criteria

1. WHEN Requester navigates to "Đăng ký trước Yêu cầu chi tiết", THE Pre_Registration_Module SHALL display a list of the current user's pre-registrations with pagination (20 records/page)
2. WHEN Requester fills and signs the pre-registration form, THE Pre_Registration_Module SHALL save records with status "Chưa dùng" and OTP signature timestamp
3. WHEN Requester attempts to register for a past date+shift, THE Validation_Engine SHALL block with error
4. WHEN Requester creates a duplicate registration (same user + date + shift + system + database + object + rights), THE Validation_Engine SHALL block with duplication error
5. WHILE pre-registration status is "Chưa dùng", THE Pre_Registration_Module SHALL allow editing (requires re-signing OTP) and permanent deletion
6. WHILE pre-registration status is not "Chưa dùng", THE Pre_Registration_Module SHALL prevent editing and deletion

### Requirement 17: Nạp tự động đăng ký trước vào phiếu 01-YCTC

**User Story:** As a Requester creating a 01-YCTC form, I want pre-registered details to auto-load, so that I save time and reduce input errors.

#### Acceptance Criteria

1. WHEN form creator selects Ca on form 01-YCTC (after selecting Loại yêu cầu), THE Pre_Registration_Module SHALL query pre_registration_request table for matching unit + current date + selected shift + status "Chưa dùng"
2. WHEN Loại yêu cầu is "Truy vấn", THE Pre_Registration_Module SHALL only load pre-registrations with request_type = "Truy vấn" (SELECT only)
3. WHEN Loại yêu cầu is "Chỉnh sửa", THE Pre_Registration_Module SHALL load all matching pre-registrations (both Truy vấn and Chỉnh sửa)
4. WHEN pre-registration rows are loaded into the form, THE Request_Form SHALL display them as "Đã ký" with locked content (no edit/delete allowed on form)
5. WHEN form creator changes Loại yêu cầu from "Chỉnh sửa" to "Truy vấn", THE Request_Form SHALL remove loaded rows that have non-SELECT permissions, display warning, and revert those pre-registration records to "Chưa dùng"
6. WHEN form 01-YCTC is submitted for approval, THE Pre_Registration_Module SHALL update linked pre-registration records to status "Chờ duyệt"
7. WHEN form 01-YCTC completes access grant, THE Pre_Registration_Module SHALL update linked records to status "Đã dùng"
8. WHEN form 01-YCTC is cancelled, THE Pre_Registration_Module SHALL revert linked records to status "Chưa dùng"

### Requirement 18: Hết hạn tự động đăng ký trước

**User Story:** As a system administrator, I want expired pre-registrations to be marked automatically, so that the data remains clean.

#### Acceptance Criteria

1. THE Pre_Registration_Module SHALL run a scheduled job (cron) to check pre-registration records where register_date + shift has passed
2. WHEN a pre-registration record with status "Chưa dùng" has its date+shift in the past, THE Pre_Registration_Module SHALL update status to "Hết hạn"

### Requirement 19: Concurrency — Ký đồng thời trên phiếu chung

**User Story:** As a co-signer on a shared request, I want to sign my row without conflicting with other signers, so that multiple people can sign concurrently.

#### Acceptance Criteria

1. WHILE multiple users are editing form 01-YCTC, 04A-YCTK, or 04B-BGTK simultaneously, THE Concurrency_Handler SHALL use row-level locking so each user operates on their own row only
2. WHEN any user signs their row, THE Concurrency_Handler SHALL update the form view for all other active users via periodic polling
3. WHEN Requester (form creator) views the form, THE Request_Form SHALL show real-time status of all detail rows (signed/unsigned) via polling updates
4. WHILE a user is editing their row, THE Concurrency_Handler SHALL prevent other users from modifying that same row
5. WHEN Requester (form creator) wants to remove unsigned rows before timeout, THE Request_System SHALL allow deletion of unsigned detail rows only

### Requirement 20: Hủy yêu cầu

**User Story:** As a Requester, I want to cancel a request that has not been approved yet, so that I can withdraw it if needed.

#### Acceptance Criteria

1. WHILE request status is DRAFT, PENDING_SIGN, or RETURNED, THE Request_System SHALL allow Requester to cancel without requiring a reason
2. WHEN Requester cancels a request, THE Request_System SHALL set status to CANCELLED and record cancelled_at timestamp
3. WHILE request status is PENDING_APPROVAL, PENDING_CHECK, or PENDING_ACCESS_TEAM, THE Request_System SHALL not allow Requester to cancel (requires approval workflow to handle)

### Requirement 21: Gửi lại yêu cầu bị chuyển trả

**User Story:** As a Requester, I want to edit and resubmit a returned request, so that I can address the reviewer's feedback.

#### Acceptance Criteria

1. WHILE request status is RETURNED, THE Request_System SHALL allow Requester to edit form content and re-sign
2. WHEN Requester re-signs and resubmits a returned request, THE Submission_Service SHALL transition status back to the appropriate pending status and re-trigger workflow from the beginning step
3. WHEN resubmission completes, THE Notification_Service SHALL send email notification to the first approver in the workflow

### Requirement 22: Hiển thị phần read-only trên form lập yêu cầu

**User Story:** As a Requester, I want to see where approval and execution sections will appear, so that I understand the full form layout.

#### Acceptance Criteria

1. THE Request_Form SHALL display approval signature sections as empty read-only fields for all form types
2. THE Request_Form SHALL display "Kết quả thực hiện" section as empty read-only for forms 02-YCCS and 03-YCCT
3. THE Request_Form SHALL display "Phần DBA ghi" section as empty read-only for form 03-YCCT
4. THE Request_Form SHALL display "Thực hiện mở truy cập" section as empty read-only for form 05A-YCKC

### Requirement 23: Email Notification khi chuyển trạng thái

**User Story:** As a participant in the workflow, I want to receive email notifications when a request changes status, so that I know when action is needed.

#### Acceptance Criteria

1. WHEN request transitions from DRAFT to PENDING_APPROVAL, THE Notification_Service SHALL send email to the department manager of the requester unit (or app owner unit for internal variant)
2. WHEN request transitions from DRAFT to PENDING_CHECK, THE Notification_Service SHALL send email to the Checking team of the application owner unit
3. WHEN request transitions from DRAFT to PENDING_ACCESS_TEAM, THE Notification_Service SHALL send email to the Access Team
4. WHEN 04B transitions to PENDING_RECEIPT, THE Notification_Service SHALL send email to all receiver users in the detail list
5. WHEN request is cancelled or returned, THE Notification_Service SHALL send email to the requester

