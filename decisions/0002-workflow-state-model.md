# ADR 0002 - Quản lý workflow bằng trạng thái và lịch sử xử lý

## Status

Accepted

## Context

Các mẫu phiếu có nhiều luồng xử lý khác nhau theo loại yêu cầu, đơn vị yêu cầu, đơn vị chủ quản ứng dụng, đơn vị chủ quản CSDL và vai trò người xử lý.

## Decision

Sử dụng mô hình:

- `access_request.status` lưu trạng thái hiện tại.
- `access_request.current_actor_*` lưu người/bộ phận đang xử lý.
- `workflow_history` lưu toàn bộ tiến trình xử lý.
- `request_signature` lưu thông tin ký xác nhận.

Workflow service chịu trách nhiệm xác định bước tiếp theo.

## Consequences

- Dễ hiển thị trạng thái hiện tại và lịch sử xử lý.
- Dễ kiểm tra phân quyền theo current actor.
- Cần test kỹ các luồng 01, 02, 03, 04A, 05A, 05B.
- Không được chuyển trạng thái trực tiếp ở controller; phải đi qua workflow service.
