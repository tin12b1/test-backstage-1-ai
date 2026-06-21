# Business Rules - Quy tắc nghiệp vụ trọng yếu

File này tóm tắt các quy tắc nghiệp vụ cần được tuân thủ xuyên suốt khi phát triển.

## 1. Lập yêu cầu

- Chỉ cho chọn CSDL, hệ thống, quyền truy cập, người dùng theo danh mục hợp lệ.
- 01-YCTC và 04A-YCTK phải có tối thiểu một dòng chi tiết.
- Người yêu cầu và người dùng chung phiếu phải ký xác nhận trước khi gửi.
- Mỗi người dùng chỉ cần ký một lần, hệ thống tự động điền chữ ký vào các vị trí liên quan.
- Người dùng đang nợ phiếu 05B-HTKC không được lập phiếu mới.
- Cho phép lưu nháp/sửa nháp trước khi gửi.
- Cho phép hủy nếu chưa phê duyệt.
- Cho phép gửi lại nếu gửi lỗi.

## 2. Phê duyệt/xử lý

- Danh sách chờ xử lý hiển thị theo đúng vai trò.
- Yêu cầu hợp lệ phải ký xác nhận để chuyển bước.
- Yêu cầu không hợp lệ chuyển trả và bắt buộc nhập lý do.
- Chuyển trả không cần ký xác nhận.
- Hệ thống tự xác định người/bộ phận tiếp theo theo workflow.

## 3. Nhật ký/thông báo

- Mọi bước xử lý phải ghi workflow history.
- Mọi thao tác quan trọng phải ghi audit log.
- Tự động ghi nhật ký 07-NKCV theo vai trò thực hiện.
- Tự động gửi email khi có sự kiện nghiệp vụ.
