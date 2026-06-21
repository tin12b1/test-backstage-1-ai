# ADR 0003 - Chính sách ký xác nhận và chuyển trả

## Status

Accepted

## Context

Tài liệu nghiệp vụ yêu cầu tích hợp SoftOTP để xác nhận khi người dùng ký. Tuy nhiên thao tác chuyển trả yêu cầu không cần ký xác nhận, chỉ cần ghi nội dung chuyển trả.

## Decision

- Các thao tác submit, approve, execute cần ký xác nhận bằng SoftOTP/OTP.
- Chỉ khi xác thực OTP thành công mới được chuyển bước.
- Chuyển trả không yêu cầu OTP nhưng bắt buộc nhập lý do.
- Mọi thao tác đều ghi workflow history và audit log.

## Consequences

- Giảm thao tác ký không cần thiết khi chuyển trả.
- Vẫn đảm bảo truy vết đầy đủ lý do chuyển trả.
- Controller/service phải tách rõ action cần ký và action không cần ký.
