# Bộ tài liệu phát triển Hệ thống quản lý truy cập CSDL

Bộ tài liệu này được tạo theo cấu trúc làm việc trong `CLAUDE.md`. Khi phát triển một chức năng, AI agent/developer cần đọc tài liệu theo thứ tự:

1. `docs/vision.md`
2. `docs/architecture.md`
3. `docs/api-contract.md`
4. `docs/database-schema.md`
5. `features/<ten-chuc-nang>.md`
6. `decisions/`

## Phân công phát triển và self test

| STT | Hạng mục | Người phụ trách | Feature spec |
|---:|---|---|---|
| 2.1 | Chức năng đăng nhập | Khương | `features/login.md` |
| 2.2 | Màn hình Dashboard | Khương | `features/dashboard.md` |
| 2.3 | Chức năng Lập yêu cầu | Tín | `features/request-create.md` |
| 2.4 | Chức năng phê duyệt và xác nhận của bộ phận Kiểm tra/Mở truy cập/Quản trị CSDL | Quang | `features/approval-processing.md` |
| 2.5 | Màn hình tra cứu, báo cáo | Khương | `features/search-report.md` |
| 2.6 | Cấu hình | Khương | `features/configuration.md` |
| 2.7 | Service tích hợp OTP, Email, AD | Cường | `features/integrations.md` |

## Nguyên tắc sử dụng

- Mỗi người chỉ sửa các file nằm trong mục `Allowed Files` của feature mình phụ trách.
- Không sửa lan sang feature khác nếu chưa thống nhất.
- Mỗi feature phải tự self test theo `Verification` và `Definition of Done` trong file tương ứng.
- Nếu tài liệu nghiệp vụ, API, DB hoặc quyết định kiến trúc có mâu thuẫn, dừng lại và báo lại trước khi code.
