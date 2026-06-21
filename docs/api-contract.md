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

Chọn vai trò làm việc sau đăng nhập.

| Field | Required | Mô tả |
|---|---:|---|
| roleCode | Yes | Mã vai trò |

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
- `YCKC_05A`.
- `HTKC_05B`.

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

Ký xác nhận bằng SoftOTP/OTP.

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

- `GET /config/users`
- `GET /config/users/new`
- `POST /config/users`
- `POST /config/users/{id}/update`
- `POST /config/users/{id}/disable`

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
