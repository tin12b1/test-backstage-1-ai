# Architecture - Kiến trúc hệ thống

## 1. Nền tảng triển khai

Kiến trúc giai đoạn đầu thống nhất theo tài liệu nghiệp vụ:

- Monolith: Spring Boot + Thymeleaf.
- Máy chủ WebApp cài Nginx để reverse proxy vào ứng dụng Spring Boot.
- Ứng dụng kết nối AD, Email, Google Authenticator (TOTP)/OTP và Database nội bộ.

## 2. Mô hình lớp

```text
User Browser
    |
    v
Nginx Reverse Proxy
    |
    v
Spring Boot Monolith
    |-- Web MVC / Thymeleaf views
    |-- Controller layer
    |-- Service layer
    |-- Workflow engine/service
    |-- Repository layer
    |-- Integration adapters: AD, OTP, Email
    v
Application Database
```

## 3. Module trong ứng dụng

| Module | Mô tả |
|---|---|
| Authentication | Đăng nhập AD, quản lý phiên, chọn vai trò |
| Dashboard | Hiển thị chỉ số và danh sách công việc theo vai trò |
| Request Management | Lập, lưu nháp, ký, gửi, hủy, gửi lại yêu cầu |
| Workflow | Xác định bước xử lý tiếp theo, trạng thái, người/bộ phận nhận việc |
| Approval Processing | Kiểm tra, phê duyệt, chuyển trả, ký xác nhận |
| Execution | Mở truy cập, kiểm tra script, DBA/người thực hiện xác nhận hoàn thành |
| Search & Report | Tra cứu theo phân quyền, xuất Excel/PDF, báo cáo khi có mẫu |
| Configuration | Người dùng, đơn vị, phòng, hệ thống, CSDL, vai trò, trạng thái |
| Integration | AD, Email, OTP (Google Authenticator) |
| Audit | Log đăng nhập, log thao tác, nhật ký công việc 07-NKCV |

## 4. Hard rules

- Không bỏ qua phân quyền theo vai trò.
- Không cho người dùng xem/xử lý yêu cầu ngoài phạm vi được phân quyền.
- Không cho sửa nội dung sau khi đã gửi phê duyệt.
- Chỉ cho lưu nháp/sửa phiếu khi chưa gửi duyệt.
- Chỉ cho hủy yêu cầu khi chưa được phê duyệt.
- Chuyển trả phải bắt buộc nhập lý do.
- Chuyển trả không yêu cầu ký xác nhận.
- Chỉ chuyển bước tiếp theo khi ký xác nhận thành công.
- Mỗi thao tác quan trọng phải ghi audit log.
- Mỗi bước xử lý nghiệp vụ phải ghi workflow history.
- Các danh mục CSDL, hệ thống, đơn vị, người dùng phải lấy từ cấu hình hợp lệ.

## 5. Quy tắc trạng thái đề xuất

| Trạng thái | Ý nghĩa |
|---|---|
| DRAFT | Lưu nháp, chưa gửi |
| PENDING_CHECK | Chờ bộ phận kiểm tra |
| PENDING_DEPT_APPROVAL | Chờ Trưởng phòng/tương đương |
| PENDING_AUTHORITY_APPROVAL | Chờ Người có thẩm quyền |
| PENDING_OWNER_UNIT | Chờ đơn vị chủ quản ứng dụng |
| APPROVED | Đã phê duyệt |
| SENT_TO_ACCESS_TEAM | Đã chuyển bộ phận Mở truy cập |
| PENDING_DBA | Chờ DBA/quản trị CSDL |
| PENDING_EXECUTION | Chờ người thực hiện |
| IN_PROGRESS | Đang thực hiện/mở truy cập |
| COMPLETED | Hoàn thành |
| RETURNED | Chuyển trả |
| CANCELLED | Đã hủy |
| SEND_FAILED | Gửi lỗi, cho phép gửi lại |

## 6. Quy tắc ký xác nhận

- Ký xác nhận bằng Google Authenticator (TOTP); SoftOTP/ESB là phương án thay thế qua cấu hình `integration.otp.mode`.
- Người dùng phải đăng ký và kích hoạt Google Authenticator trước khi ký; ADMIN có thể cấp/reset GA cho người dùng.
- Sau khi ký thành công, hệ thống hiển thị hình ảnh chữ ký đã khai báo trên hệ thống.
- Thông tin cần lưu: người ký, vai trò ký, thời gian ký, phương thức ký, kết quả xác thực, bước xử lý, nội dung ký.
- Với mẫu 01-YCTC và 04A-YCTK, mỗi người dùng chỉ cần ký một lần; hệ thống tự động điền chữ ký cho phần thông tin chung và các dòng chi tiết liên quan.

## 7. Quy tắc thông báo email

Gửi email khi có các sự kiện:

- Có yêu cầu mới chờ kiểm tra/phê duyệt.
- Yêu cầu bị chuyển trả.
- Yêu cầu đã được lãnh đạo phòng/bộ phận phê duyệt.
- Yêu cầu đã được Người có thẩm quyền phê duyệt.
- Yêu cầu đã chuyển đến bộ phận Mở truy cập/DBA/Người thực hiện.
- Yêu cầu đã hoàn thành/xác nhận mở truy cập.

## 8. Quy tắc phát triển

- Backend, frontend Thymeleaf và xử lý nghiệp vụ cùng trong một Spring Boot monolith.
- Mỗi feature chỉ sửa các file được liệt kê trong `Allowed Files` của feature spec.
- Không tự ý tạo abstraction hoặc microservice khi chưa có quyết định kiến trúc mới.
- Các enum về loại yêu cầu, trạng thái, vai trò phải thống nhất với `docs/database-schema.md` và `docs/api-contract.md`.

## 9. Cấu hình kết nối Database

Ứng dụng hỗ trợ hai cấu hình datasource qua Spring profile, thông tin kết nối externalized
qua biến môi trường, không hard-code.

| Profile           | Database         | Dùng cho        | ddl-auto      |
|-------------------|------------------|-----------------|---------------|
| (mặc định)        | H2 in-memory     | Dev/test        | `create`      |
| `oracle`          | Oracle           | Staging/Prod    | `validate`(*) |

(*) Trên Oracle mặc định `validate` để không tự sửa schema. Schema/seed do DDL hoặc công cụ
migration quản lý riêng (không chạy `data.sql`).

### Chạy với Oracle

```bash
# Kich hoat profile oracle + cung cap thong tin ket noi qua bien moi truong
set SPRING_PROFILES_ACTIVE=oracle
set ORACLE_URL=jdbc:oracle:thin:@//db-host:1521/PDBNAME
set ORACLE_USER=csdl
set ORACLE_PASSWORD=<secret>
mvn spring-boot:run
```

### Biến môi trường (profile `oracle`)

| Biến                  | Mặc định                                      | Ý nghĩa                          |
|-----------------------|-----------------------------------------------|----------------------------------|
| `ORACLE_URL`          | `jdbc:oracle:thin:@//localhost:1521/XEPDB1`   | JDBC URL                         |
| `ORACLE_USER`         | `csdl`                                         | User DB                          |
| `ORACLE_PASSWORD`     | _(trống)_                                      | Mật khẩu DB (secret)             |
| `ORACLE_DDL_AUTO`     | `validate`                                     | Chiến lược schema của Hibernate  |
| `ORACLE_DIALECT`      | `org.hibernate.dialect.Oracle12cDialect`       | Hibernate dialect                |
| `ORACLE_POOL_MAX`     | `10`                                           | HikariCP max pool size           |
| `ORACLE_POOL_MIN`     | `2`                                            | HikariCP min idle                |
| `ORACLE_CONN_TIMEOUT_MS` | `30000`                                     | Timeout lấy connection           |
| `ORACLE_TZ`           | `Asia/Ho_Chi_Minh`                             | Timezone JDBC                    |

### Ghi chú kỹ thuật

- Driver: `com.oracle.database.jdbc:ojdbc11` (scope `runtime`), tương thích Java 11.
- Connection pool dùng HikariCP (mặc định của Spring Boot).
- Cấu hình profile nằm ở `src/main/resources/application-oracle.yml`.
- Dialect: dùng `Oracle12cDialect` cho Oracle 12c+; chỉnh `ORACLE_DIALECT` nếu phiên bản khác.
