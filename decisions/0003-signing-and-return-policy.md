# ADR 0003 - Chính sách ký xác nhận và chuyển trả

## Status

Accepted

## Context

Tài liệu nghiệp vụ yêu cầu tích hợp SoftOTP để xác nhận khi người dùng ký. Tuy nhiên thao tác chuyển trả yêu cầu không cần ký xác nhận, chỉ cần ghi nội dung chuyển trả.

## Decision

- Các thao tác submit, approve, execute cần ký xác nhận bằng OTP.
- Phương thức OTP triển khai mặc định là **Google Authenticator (TOTP RFC 6238)** thay cho SoftOTP;
  client OTP cấu hình được qua `integration.otp.mode` (`ga` | `mock` | `esb`).
- Người dùng phải đăng ký và kích hoạt Google Authenticator (`/profile/ga`) trước khi ký; ADMIN có
  thể cấp/reset GA cho mọi người dùng (`/config/users/{id}/ga`).
- Chỉ khi xác thực OTP thành công mới được chuyển bước.
- Chuyển trả không yêu cầu OTP nhưng bắt buộc nhập lý do.
- Mọi thao tác đều ghi workflow history và audit log.

## Consequences

- Giảm thao tác ký không cần thiết khi chuyển trả.
- Vẫn đảm bảo truy vết đầy đủ lý do chuyển trả.
- Controller/service phải tách rõ action cần ký và action không cần ký.
- Phụ thuộc việc người dùng đã đăng ký Google Authenticator; cần quy trình cấp/reset GA cho người
  dùng mới hoặc khi đổi/mất thiết bị.
