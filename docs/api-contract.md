# API Contract - Hợp đồng giao tiếp nội bộ

> Dự án dùng Spring Boot + Thymeleaf nên phần lớn thao tác là form submit/server-side rendering. File này mô tả chuẩn route/controller, request/response và service contract nội bộ để các nhóm phát triển thống nhất.

## 1. Quy ước chung

- Base web path: `/app`.
- API nội bộ phục vụ AJAX: `/api`.
- Date/time dùng ISO-8601 khi trao đổi qua API.
- Mọi API phải kiểm tra session và role hiện hành.
- Mọi API thay đổi dữ liệu phải ghi audit log.

## 2. Authentication

### GET `/login`

Hiển thị màn hình đăng nhập.

### POST `/login`

Đăng nhập bằng user AD.

Request form:

| Field | Required | Mô tả |
|---|---:|---|
| username | Yes | User AD |
| password | Yes | Mật khẩu AD |

Kết quả:

- Nếu user có một vai trò: chuyển Dashboard.
- Nếu user có nhiều vai trò: chuyển màn hình chọn vai trò.
- Nếu lỗi: hiển thị thông báo lỗi đăng nhập.

### POST `/session/role`

Chọn/đổi vai trò làm việc sau đăng nhập. Gọi từ màn hình chọn vai trò hoặc từ dropdown đổi vai trò trên thanh tiêu đề (không cần đăng xuất). Sau khi đổi, chuyển về Dashboard.

| Field | Required | Mô tả |
|---|---:|---|
| roleCode | Yes | Mã vai trò |

### GET `/profile`

Màn hình thông tin người dùng (mở từ tên người dùng cạnh biểu tượng người dùng). Hiển thị: thông tin cá nhân, danh sách vai trò và vai trò đang dùng, ảnh chữ ký, hệ thống thông tin và CSDL liên quan (theo phạm vi vai trò của người dùng).

### GET `/profile/signature`

Trả ảnh chữ ký đã khai báo của người đăng nhập (`app_user.signature_image_id` → `signature_image`). Trả 404 nếu chưa khai báo chữ ký.

### Google Authenticator (ký xác nhận thay SoftOTP)

- `GET /profile/ga` — màn hình đăng ký/quản lý Google Authenticator (trạng thái: chưa đăng ký / chờ xác nhận / đã kích hoạt).
- `GET /profile/ga/qr` — ảnh QR (PNG) otpauth của người đăng nhập (vẽ bằng zxing).
- `POST /profile/ga/enroll` — sinh bí mật TOTP mới (trạng thái chờ xác nhận).
- `POST /profile/ga/confirm` — nhập mã 6 số để kích hoạt. Field: `code`.
- `POST /profile/ga/reset` — xóa đăng ký hiện tại để đăng ký lại (khi đổi/mất thiết bị).

Khi `integration.otp.mode=ga`, các API ký (`/requests/{id}/sign`, `/requests/{id}/approve`, `/requests/{id}/execute`, đăng ký 01YCTC) xác thực mã 6 số qua Google Authenticator của người dùng.

## 3. Dashboard

### GET `/dashboard`

Hiển thị dashboard theo role hiện hành.

Service contract:

```java
DashboardView getDashboard(String username, String activeRole);
```

Dashboard gồm:

- Summary counters.
- Danh sách yêu cầu theo vai trò.
- Nút truy cập nhanh.

## 4. Request Management

### GET `/requests/new`

Hiển thị màn hình chọn mẫu phiếu.

### GET `/requests/new/{requestType}`

Hiển thị form lập yêu cầu theo mẫu.

`requestType` gồm:

- `YCTC_01`.
- `YCCS_02`.
- `YCCT_03`.
- `YCTK_04A`.
- `BGTK_04B` (mở màn hình riêng "Biên bản bàn giao tài khoản" `requests/handover`; các mẫu khác dùng `requests/form`).
- `YCKC_05A`.
- `HTKC_05B`.

### GET `/requests/new/{requestType}?loadRegistered=1`

Riêng `YCTC_01`: nạp các bản ghi trong bảng `access_registration` của người đăng nhập thành các dòng chi tiết sẵn (nút "Lấy dữ liệu đã đăng ký"). Nếu chưa có bản ghi thì hiển thị form trống mặc định.

### GET `/requests/register/{requestType}`

Hiển thị màn hình đăng ký thông tin chi tiết (01YCTC_Dangky) cho `YCTC_01`: Từ ngày/Đến ngày, Ca, Người yêu cầu (tự động), Hệ thống, CSDL, Tên đối tượng, Quyền truy cập (ngay dưới Tên đối tượng), ô nhập OTP với label "Ký xác nhận (Google Authenticator)" đặt ngay trên nút "Ký và lưu lại". Model cấp thêm `today` và `maxToDate` (= hôm nay + 7) cho ràng buộc `min`/`max` của ô ngày.

### POST `/requests/register/{requestType}`

Đăng ký một dòng chi tiết 01-YCTC. Quy tắc:

- Kiểm tra ràng buộc ngày: `fromDate` ≥ hôm nay; `toDate` ≤ hôm nay + 7 ngày; `toDate` ≥ `fromDate` (báo lỗi nghiệp vụ nếu vi phạm).
- Tạo phiếu nháp `YCTC_01` với một dòng chi tiết.
- Ký xác nhận tại mục chi tiết bằng OTP (`signingScope=DETAIL`) — thao tác "Ký và lưu lại".
- Lưu một bản ghi vào bảng `access_registration` (phục vụ nút "Lấy dữ liệu đã đăng ký").
- Chuyển sang trang chi tiết phiếu nháp vừa tạo.

Request form: `fromDate`, `toDate`, `shiftNo`, `details[0].systemId`, `details[0].databaseId`, `details[0].objectName`, `details[0].accessRightCodes`, `otp`.

### POST `/requests/draft`

Lưu nháp yêu cầu.

Request body chính:

```json
{
  "requestType": "YCTC_01",
  "shiftNo": 1,
  "accessNo": 1,
  "systemId": 10,
  "databaseId": 20,
  "reason": "Lý do nghiệp vụ",
  "details": []
}
```

Kết quả:

```json
{
  "requestId": 123,
  "requestCode": "DV_20260616_001",
  "status": "DRAFT"
}
```

### POST `/requests/{id}/sign`

Ký xác nhận bằng Google Authenticator (TOTP) / OTP.

Request:

```json
{
  "otp": "123456",
  "signingScope": "GENERAL|DETAIL",
  "detailId": 456
}
```

Kết quả:

```json
{
  "success": true,
  "signedAt": "2026-06-16T10:00:00",
  "signatureImageUrl": "/app/signatures/preview/789"
}
```

### POST `/requests/{id}/submit`

Gửi phê duyệt sau khi ký.

Quy tắc:

- Kiểm tra trường bắt buộc.
- Kiểm tra tối thiểu 1 dòng chi tiết với mẫu 01-YCTC, 04A-YCTK.
- Kiểm tra các người dùng chung phiếu đã ký.
- Kiểm tra nợ phiếu 05B-HTKC.
- Sinh mã yêu cầu nếu chưa có.
- Cập nhật trạng thái: `PENDING_DEPT_APPROVAL`, `PENDING_CHECK` hoặc `SENT_TO_ACCESS_TEAM` tùy loại yêu cầu.
- Gửi email cho người/bộ phận tiếp theo.

### POST `/requests/{id}/cancel`

Hủy yêu cầu khi chưa được phê duyệt.

### POST `/requests/{id}/resend`

Gửi lại yêu cầu nếu trạng thái `SEND_FAILED`.

## 5. Approval Processing

### GET `/work-items`

Danh sách yêu cầu đang chờ xử lý theo vai trò hiện hành.

Query:

| Field | Required | Mô tả |
|---|---:|---|
| status | No | Trạng thái |
| requestType | No | Loại yêu cầu |
| unitId | No | Đơn vị |
| databaseId | No | CSDL |
| fromDate | No | Từ ngày |
| toDate | No | Đến ngày |

### GET `/requests/{id}`

Xem chi tiết yêu cầu.

### POST `/requests/{id}/approve`

Ký xác nhận và chuyển bước tiếp theo.

Request:

```json
{
  "otp": "123456",
  "comment": "Đồng ý"
}
```

Quy tắc:

- Chỉ role hiện hành đúng người/bộ phận đang xử lý mới được approve.
- OTP hợp lệ mới được chuyển bước.
- Lưu workflow history, signature, audit log.
- Tự động xác định bước tiếp theo.

### POST `/requests/{id}/return`

Chuyển trả yêu cầu.

Request:

```json
{
  "reason": "Nội dung chưa hợp lệ/cần bổ sung"
}
```

Quy tắc:

- Bắt buộc nhập lý do.
- Không cần ký xác nhận.
- Cập nhật trạng thái `RETURNED`.
- Gửi email cho người lập.

### POST `/requests/{id}/execute`

Bộ phận mở truy cập/DBA/người thực hiện ghi nhận thực hiện.

Request:

```json
{
  "otp": "123456",
  "executionStartTime": "2026-06-16T08:00:00",
  "executionEndTime": "2026-06-16T08:30:00",
  "executionNote": "Đã thực hiện"
}
```

## 6. Search & Report

### GET `/search`

Tra cứu yêu cầu theo phân quyền.

Query:

- `fromDate`, `toDate`
- `status`
- `requestType`
- `unitId`
- `systemId`
- `databaseId`
- `actorUsername`

### GET `/reports/export`

Xuất danh sách ra Excel/PDF theo phân quyền.

## 7. Configuration

### Users

- `GET /config/users` — danh sách người dùng. Query `q` (tùy chọn): lọc theo tài khoản (chứa, không phân biệt hoa thường). Danh sách hiển thị thêm cột trạng thái kích hoạt Google Authenticator (Đã kích hoạt / Chờ kích hoạt / Chưa đăng ký).
- `GET /config/users/new`
- `POST /config/users`
- `POST /config/users/{id}/update`
- `POST /config/users/{id}/disable`
- `GET /config/users/{id}/ga` — ADMIN: màn cấp/quản lý Google Authenticator cho người dùng.
- `GET /config/users/{id}/ga/qr` — ADMIN: ảnh QR (PNG) otpauth của người dùng.
- `POST /config/users/{id}/ga/enroll` — ADMIN: cấp (sinh + kích hoạt) GA cho người dùng, hiển thị khóa bí mật để bàn giao.
- `POST /config/users/{id}/ga/reset` — ADMIN: reset (xóa) GA của người dùng.

### Units/Departments

- `GET /config/units`
- `POST /config/units`
- `POST /config/units/{id}/update`

### Systems/Databases

- `GET /config/systems`
- `POST /config/systems`
- `GET /config/databases`
- `POST /config/databases`

### Roles/Statuses

- `GET /config/roles`
- `GET /config/statuses`

### Logs (nhật ký giao dịch cho quản trị/dev)

- `GET /config/logs` — ADMIN: xem log giao dịch để kiểm tra lỗi. Tham số:
  - `type`: `audit` (mặc định) | `login` | `otp` | `email` | `debug`.
    - `audit`: nhật ký thao tác nghiệp vu (`audit_log`).
    - `login`: nhật ký đăng nhập (`login_log`), tô đỏ dòng thất bại.
    - `otp`: giao dịch ký/xác thực OTP (`otp_transaction`), tô đỏ dòng khác `SUCCESS`.
    - `email`: hàng đợi email (`email_queue`), tô đỏ dòng `FAILED`.
    - `debug`: đọc trực tiếp các dòng cuối của file log ứng dụng (`logging.file.name`) cho lập trình viên debug; tô màu dòng `ERROR`/`WARN`/`Exception`.
  - `q` (tùy chọn): lọc theo từ khóa (tài khoản, hành động, nội dung, thông báo lỗi...).
  - `onlyErrors` (mặc định false): chỉ hiển thị dòng lỗi/thất bại.
  - Mỗi nguồn hiển thị tối đa 500 dòng gần nhất (riêng `debug` là 800 dòng cuối file).

## 8. Integration Services

### OTP

```java
OtpVerifyResult verifyOtp(String username, String otp, String purpose, Long requestId);
```

### AD

```java
AdUser authenticate(String username, String password);
AdUserProfile getUserProfile(String username);
```

### Email

```java
void sendWorkflowNotification(WorkflowNotification notification);
```
