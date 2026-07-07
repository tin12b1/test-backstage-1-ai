# Database Schema - Mô hình dữ liệu đề xuất

> Đây là mô hình dữ liệu logic phục vụ phát triển ban đầu. Khi thiết kế vật lý cần bổ sung kiểu dữ liệu cụ thể, index, sequence, constraint và quy ước đặt tên theo chuẩn dự án.

> **DDL Oracle vật lý:** thư mục `src/main/resources/db/oracle/` gồm `V0__drop.sql` (drop an toàn),
> `V1__schema.sql` (tạo mới 23 bảng), `V3__seed_catalog.sql` (seed danh mục/người dùng/phân quyền),
> và `V2__04b_bgtk_columns.sql` (vá cột 04B-BGTK cho schema cũ). Cài mới chạy V0 → V1 → V3.
> Kiểu cột đã khớp entity JPA (`Long`→`NUMBER(19,0)`, `Integer`→`NUMBER(10,0)`, `boolean`→`NUMBER(1,0)`,
> `String(n)`→`VARCHAR2(n CHAR)`, `LocalDateTime`→`TIMESTAMP`, `LocalDate`→`DATE`, `@Lob`→`CLOB`/`BLOB`).

## 1. Danh mục chính

### `app_user`

Lưu người dùng được đăng ký trên hệ thống.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| username | User AD |
| full_name | Họ tên |
| mobile | Số điện thoại |
| email | Email |
| unit_id | Đơn vị |
| department_id | Phòng/bộ phận |
| signature_image_id | Ảnh chữ ký khai báo |
| status | ACTIVE/INACTIVE |

### `role`

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| code | Mã vai trò |
| name | Tên vai trò |
| description | Mô tả |

Role code đề xuất:

- `REQUESTER`
- `DEPT_MANAGER`
- `AUTHORITY`
- `CHECKER`
- `ACCESS_TEAM`
- `DBA`
- `EXECUTOR`
- `ADMIN`

### `user_role`

Gán nhiều vai trò cho một user.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| user_id | Người dùng |
| role_id | Vai trò |
| unit_id | Phạm vi đơn vị nếu có |
| department_id | Phạm vi phòng/bộ phận nếu có (gán Trưởng phòng/Người có thẩm quyền theo phòng) |
| system_id | Phạm vi hệ thống nếu có |
| database_id | Phạm vi CSDL nếu có |
| active | Hiệu lực |

### `unit`, `department`

Danh mục đơn vị/phòng ban.

### `information_system`

Danh mục hệ thống thông tin.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| code | Ký hiệu đơn vị/hệ thống (dạng mã ASCII, ví dụ `TTCNTT-NHDT-ARS`) |
| name | Tên hệ thống |
| owner_unit_id | Đơn vị chủ quản ứng dụng |
| owner_department_id | Phòng/bộ phận chủ quản ứng dụng |
| active | Hiệu lực |

### `database_catalog`

Danh mục CSDL.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| system_id | Hệ thống thông tin |
| code | Mã CSDL |
| name | Tên CSDL |
| owner_unit_id | Đơn vị chủ quản CSDL |
| active | Hiệu lực |

### `access_right_catalog`

Danh mục quyền truy cập/truy xuất.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| code | SELECT/INSERT/UPDATE/DELETE/QUERY_ALL/... |
| name | Tên quyền |
| active | Hiệu lực |

### `work_shift`

Danh mục ca làm việc/truy cập (Ca 1: 0-8h, Ca 2: 8-20h, Ca 3: 20-24h).

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| shift_no | Số thứ tự ca (1, 2, 3), duy nhất |
| name | Tên hiển thị, ví dụ "Ca 1" |
| start_hour | Giờ bắt đầu (0-23) |
| end_hour | Giờ kết thúc (1-24) |
| label | Nhãn khung giờ, ví dụ "0-8h" |
| active | Hiệu lực |

## 2. Bảng yêu cầu

### `access_request`

Bảng header của phiếu yêu cầu.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| request_code | Mã yêu cầu. 01-YCTC: `MãĐơnVị_MãPhòng_yyyyMMddHHmmss`; mẫu có chọn hệ thống (02/05A/05B): `KýhiệuHệThống_yyyyMMddHHmmss` |
| request_type | Loại phiếu: 01/02/03/04A/04B/05A/05B |
| status | Trạng thái hiện tại |
| requester_user_id | Người lập |
| requester_unit_id | Đơn vị yêu cầu |
| requester_department_id | Phòng/bộ phận |
| shift_no | Ca 1/2/3 |
| access_no | Lần 1/2/3/4/5 |
| system_id | Hệ thống (dùng cho 02/05A/05B; 01-YCTC chọn theo từng dòng chi tiết) |
| database_id | CSDL (dùng cho 02/05A/05B) |
| start_time | Thời gian bắt đầu |
| end_time | Thời gian kết thúc |
| expected_execution_date | Ngày thực hiện dự kiến, dùng cho 03-YCCT |
| reason | Lý do/mục đích |
| receiver_user_id | 04B-BGTK: người nhận bàn giao tài khoản |
| handover_manager_id | 04B-BGTK: lãnh đạo phòng phụ trách người bàn giao |
| receiver_manager_id | 04B-BGTK: lãnh đạo phòng phụ trách người nhận bàn giao |
| source_request_id | 04B-BGTK: phiếu 04A-YCTK được liên kết bàn giao |
| current_actor_type | USER/ROLE/UNIT/TEAM |
| current_actor_id | Người/bộ phận đang xử lý |
| current_actor_role | Vai trò đang xử lý (tham chiếu nhanh khi actor là ROLE/TEAM) |
| current_step_code | Mã bước workflow hiện tại |
| current_unit_id | Đơn vị chịu trách nhiệm ở bước hiện tại |
| owner_unit_id | Đơn vị chủ quản ứng dụng nếu xác định được |
| owner_db_unit_id | Đơn vị chủ quản CSDL nếu xác định được |
| at_requester_phase | Đang ở giai đoạn của đơn vị yêu cầu (true) hay đơn vị chủ quản (false) |
| created_at | Ngày lập |
| submitted_at | Ngày gửi |
| approved_at | Ngày phê duyệt |
| completed_at | Ngày hoàn thành |
| cancelled_at | Ngày hủy |

### `request_detail`

Dòng chi tiết dùng cho các mẫu có danh sách chi tiết như 01-YCTC, 04A-YCTK, 05A, 05B.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| request_id | Phiếu cha |
| system_id | Hệ thống |
| database_id | CSDL |
| object_owner | Owner |
| object_name | Tên bảng/đối tượng |
| object_type | TABLE/INDEX/SYNONYM/OTHER |
| target_user_id | Người sử dụng trên dòng chi tiết |
| account_owner_name | Chủ tài khoản, dùng cho 04A |
| account_type | Truy cập/Chỉnh sửa |
| account_action | Cấp mới/Đổi thuộc tính |
| scope | 04B-BGTK: phạm vi bàn giao (Toàn bộ/Theo hệ thống/Theo CSDL/Theo đối tượng) |
| access_rights | Quyền truy cập dạng mã hoặc JSON |
| query_all | Có chọn QueryAll không |
| purpose | Mục đích/lý do dòng chi tiết |
| detail_data | JSON lưu field đặc thù từng mẫu |

### `request_script_file`

Lưu file SQL/script đính kèm cho 02-YCCS, 03-YCCT.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| request_id | Phiếu |
| detail_id | Dòng chi tiết nếu có |
| file_name | Tên file |
| file_path/blob_id | Vị trí lưu file |
| checksum | Mã kiểm tra tính toàn vẹn |
| uploaded_by | Người tải |
| uploaded_at | Thời gian tải |

Quy tắc tên file 02-YCCS:

```text
YYYYMMDD_BS_XXX.sql
```

Trong đó:

- `YYYYMMDD`: ngày tháng năm.
- `BS`: mã viết tắt nghiệp vụ.
- `XXX`: số thứ tự file SQL trong ngày.

### `emergency_completion_link`

Liên kết phiếu 05B-HTKC với phiếu 05A-YCKC.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| emergency_request_id | Phiếu 05A |
| completion_request_id | Phiếu 05B |
| created_at | Thời gian liên kết |

### `pre_registration_request`

Bảng đăng ký trước yêu cầu chi tiết (màn hình 01YCTC_Dangky). Thay thế bảng `access_registration` cũ với schema đầy đủ hỗ trợ lifecycle quản lý trạng thái, ký OTP, và liên kết tự động vào phiếu 01-YCTC.

| Cột | Mô tả |
|---|---|
| id | Khóa chính (IDENTITY) |
| user_id | Người đăng ký (FK → app_user.id) |
| unit_code | Mã đơn vị — dùng để lọc theo đơn vị khi nạp vào phiếu |
| register_date | Ngày đăng ký truy cập (DATE) |
| shift | Ca làm việc (1/2/3) |
| request_type | Loại yêu cầu: "Truy vấn" / "Chỉnh sửa" |
| system_id | Hệ thống thông tin |
| database_id | CSDL |
| object_name | Tên đối tượng (mặc định "All Schema") |
| access_rights | CSV mã quyền, ví dụ `SELECT,INSERT,UPDATE,DELETE` |
| signed_at | Thời điểm ký OTP xác nhận |
| signature_image_id | Ảnh chữ ký (FK → signature_image.id) |
| status | Trạng thái: UNUSED / PENDING_APPROVAL / USED / EXPIRED |
| request_id | FK nullable → access_request.id (phiếu 01-YCTC liên kết) |
| created_at | Thời điểm tạo |
| updated_at | Thời điểm cập nhật |
| version | Optimistic locking version |

**Indexes:**
- `ix_prereg_user_status` ON (user_id, status)
- `ix_prereg_unit_date_shift` ON (unit_code, register_date, shift, status)

**Lifecycle trạng thái:**
- `UNUSED` → đăng ký xong, chờ nạp vào phiếu
- `PENDING_APPROVAL` → đã nạp vào phiếu 01-YCTC, đang chờ duyệt
- `USED` → phiếu 01-YCTC đã hoàn thành cấp quyền
- `EXPIRED` → hết hạn (ngày+ca đã qua mà chưa được sử dụng)

## 3. Workflow và ký xác nhận

### `workflow_history`

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| request_id | Phiếu |
| step_code | Mã bước |
| from_status | Trạng thái trước |
| to_status | Trạng thái sau |
| actor_user_id | Người xử lý |
| actor_role_code | Vai trò xử lý |
| action | SUBMIT/APPROVE/RETURN/EXECUTE/CANCEL/... |
| comment | Nội dung xử lý/lý do chuyển trả |
| processed_at | Thời gian xử lý |

### `request_signature`

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| request_id | Phiếu |
| detail_id | Dòng chi tiết nếu ký theo dòng |
| signer_user_id | Người ký |
| signer_role_code | Vai trò khi ký |
| signing_scope | GENERAL/DETAIL/APPROVAL/EXECUTION |
| otp_transaction_id | Mã giao dịch OTP nếu có |
| signed_at | Thời gian ký |
| signature_image_id | Ảnh chữ ký hiển thị |
| result | SUCCESS/FAILED |

### `signature_image`

Ảnh chữ ký khai báo của người dùng (tham chiếu bởi `app_user.signature_image_id` và `request_signature.signature_image_id`).

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| user_id | Người dùng sở hữu chữ ký |
| content_type | Kiểu MIME của ảnh (ví dụ image/png) |
| data | Dữ liệu ảnh (BLOB) |

### `work_log_07`

Nhật ký công việc theo mẫu 07-NKCV.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| request_id | Phiếu liên quan |
| actor_user_id | Người thực hiện |
| actor_role_code | Vai trò |
| work_content | Nội dung công việc |
| start_time | Bắt đầu |
| end_time | Kết thúc |
| created_at | Thời gian ghi nhận |

## 4. Audit và tích hợp

### `login_log`

Ghi log đăng nhập.

### `audit_log`

Ghi log thao tác nghiệp vụ.

### `email_queue`

Hàng đợi gửi email.

### `otp_transaction`

Lưu giao dịch xác thực OTP/SoftOTP.

### `user_totp`

Bí mật Google Authenticator (TOTP) của người dùng — dùng để ký xác nhận thay SoftOTP.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| user_id | Người dùng (duy nhất) |
| secret | Bí mật dạng Base32 (chia sẻ với app Authenticator) |
| enabled | Đã xác nhận và đang hiệu lực |
| created_at | Thời điểm tạo/đăng ký |
| confirmed_at | Thời điểm xác nhận kích hoạt |

## 5. Ràng buộc nghiệp vụ cần enforce ở DB/service

- Một phiếu 01-YCTC/04A-YCTK phải có tối thiểu một dòng chi tiết trước khi gửi.
- Phiếu 05B phải liên kết với một phiếu 05A chưa hoàn thiện.
- Người dùng đang nợ phiếu 05B-HTKC không được lập phiếu mới, kể cả khi là người dùng chung trên phiếu nhiều người.
- Sau khi gửi phê duyệt, không cho sửa nội dung phiếu.
- Trạng thái chỉ được chuyển theo workflow hợp lệ.
- Chuyển trả bắt buộc có lý do.
