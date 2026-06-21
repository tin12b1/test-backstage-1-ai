# Database Schema - Mô hình dữ liệu đề xuất

> Đây là mô hình dữ liệu logic phục vụ phát triển ban đầu. Khi thiết kế vật lý cần bổ sung kiểu dữ liệu cụ thể, index, sequence, constraint và quy ước đặt tên theo chuẩn dự án.

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
| code | Mã hệ thống |
| name | Tên hệ thống |
| owner_unit_id | Đơn vị chủ quản ứng dụng |
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

## 2. Bảng yêu cầu

### `access_request`

Bảng header của phiếu yêu cầu.

| Cột | Mô tả |
|---|---|
| id | Khóa chính |
| request_code | Mã yêu cầu, ví dụ `KýhiệuĐV_NgàyThángNăm_SốTT` |
| request_type | Loại phiếu: 01/02/03/04A/05A/05B |
| status | Trạng thái hiện tại |
| requester_user_id | Người lập |
| requester_unit_id | Đơn vị yêu cầu |
| requester_department_id | Phòng/bộ phận |
| shift_no | Ca 1/2/3 |
| access_no | Lần 1/2/3/4/5 |
| start_time | Thời gian bắt đầu |
| end_time | Thời gian kết thúc |
| expected_execution_date | Ngày thực hiện dự kiến, dùng cho 03-YCCT |
| reason | Lý do/mục đích |
| current_actor_type | USER/ROLE/UNIT/TEAM |
| current_actor_id | Người/bộ phận đang xử lý |
| owner_unit_id | Đơn vị chủ quản ứng dụng nếu xác định được |
| owner_db_unit_id | Đơn vị chủ quản CSDL nếu xác định được |
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

## 5. Ràng buộc nghiệp vụ cần enforce ở DB/service

- Một phiếu 01-YCTC/04A-YCTK phải có tối thiểu một dòng chi tiết trước khi gửi.
- Phiếu 05B phải liên kết với một phiếu 05A chưa hoàn thiện.
- Người dùng đang nợ phiếu 05B-HTKC không được lập phiếu mới, kể cả khi là người dùng chung trên phiếu nhiều người.
- Sau khi gửi phê duyệt, không cho sửa nội dung phiếu.
- Trạng thái chỉ được chuyển theo workflow hợp lệ.
- Chuyển trả bắt buộc có lý do.
