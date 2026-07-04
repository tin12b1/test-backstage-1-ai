# Feature: Cấu hình hệ thống

**Người phụ trách:** Khương  
**Mã hạng mục:** 2.6

## 1. Mục tiêu

Cung cấp chức năng cấu hình danh mục nền tảng để phục vụ lập yêu cầu, phân quyền, phê duyệt và xử lý nghiệp vụ.

## 2. Danh mục cấu hình

- Người dùng.
- Đăng ký người dùng.
- Cập nhật người dùng: hủy, thay đổi vai trò.
- Đơn vị/phòng.
- Hệ thống thông tin.
- CSDL.
- Quan hệ đơn vị chủ quản ứng dụng, đơn vị chủ quản CSDL.
- Vai trò người dùng.
- Danh mục trạng thái.
- Danh mục quyền truy cập.
- Danh sách Trưởng phòng/tương đương, Người có thẩm quyền, Người kiểm tra theo đơn vị/hệ thống/CSDL.

## 3. Chức năng người dùng

- Danh sách người dùng.
- Tìm kiếm người dùng theo tài khoản.
- Hiển thị trạng thái kích hoạt Google Authenticator của từng người dùng.
- Thêm/đăng ký người dùng từ thông tin AD.
- Cập nhật thông tin liên hệ nếu được phép.
- Gán vai trò.
- Hủy/khóa người dùng.
- Khai báo ảnh chữ ký.
- Cấp/đăng ký và reset Google Authenticator cho người dùng (ADMIN), tại `/config/users/{id}/ga`.

## 4. Chức năng đơn vị/phòng

- Danh sách đơn vị.
- Danh sách phòng/bộ phận thuộc đơn vị.
- Cấu hình Trưởng phòng/tương đương.
- Cấu hình Người có thẩm quyền.

## 5. Chức năng hệ thống/CSDL

- Danh mục hệ thống thông tin.
- Danh mục CSDL thuộc hệ thống.
- Cấu hình đơn vị chủ quản ứng dụng.
- Cấu hình đơn vị chủ quản CSDL.
- Cấu hình Bộ phận kiểm tra/DBA/Mở truy cập theo hệ thống/CSDL.
- Ràng buộc người dùng với đơn vị chủ quản ứng dụng và CSDL hợp lệ.

## 6. Chức năng vai trò/trạng thái

- Xem danh mục vai trò.
- Xem danh mục trạng thái.
- Không tự ý xóa vai trò/trạng thái hệ thống nếu đã phát sinh dữ liệu.

## 6.1. Log giao dịch (kiểm tra lỗi)

- Màn hình `/config/logs` (ADMIN) tổng hợp log để quản trị/dev kiểm tra lỗi, gồm 5 tab:
  - **Nhật ký thao tác** (`audit_log`): các thao tác nghiệp vụ (tạo nháp, ký, gán vai trò...).
  - **Đăng nhập** (`login_log`): kết quả đăng nhập, tô đỏ dòng thất bại.
  - **Ký/OTP** (`otp_transaction`): kết quả ký/xác thực OTP, tô đỏ dòng khác `SUCCESS`.
  - **Hàng đợi email** (`email_queue`): trạng thái gửi email, tô đỏ dòng `FAILED`, kèm lỗi gần nhất.
  - **Debug ứng dụng**: đọc trực tiếp các dòng cuối của file log ứng dụng (`logging.file.name`,
    mặc định `logs/csdl-access.log`) cho lập trình viên; đặt `APP_LOG_LEVEL=DEBUG` để ghi chi tiết hơn.
- Bộ lọc: từ khóa (`q`) và "Chỉ hiển thị lỗi" (`onlyErrors`). Mỗi nguồn hiển thị tối đa 500 dòng gần
  nhất (Debug: 800 dòng cuối file).

## 7. Quy tắc nghiệp vụ

- Cấu hình phải kiểm tra trùng mã.
- Không cho xóa cứng danh mục đã phát sinh giao dịch; chỉ khóa/ngừng hiệu lực.
- Thay đổi vai trò phải ghi audit log.
- Thay đổi cấu hình người xử lý có thể ảnh hưởng workflow; cần ghi nhận thời gian hiệu lực nếu cần.

## 8. Allowed Files

- `src/main/java/.../configmaster/**`
- `src/main/java/.../admin/**`
- `src/main/resources/templates/config/**`
- `src/main/resources/templates/admin/**`
- `src/main/resources/static/js/config/**`
- `src/test/java/.../configmaster/**`
- `src/test/java/.../admin/**`

## 9. Must Not Change

- Không sửa workflow xử lý yêu cầu ngoài việc cung cấp dữ liệu cấu hình.
- Không sửa form nghiệp vụ nếu không cần thiết.
- Không sửa service tích hợp AD/OTP/Email.

## 10. Verification

- Thêm/sửa/khóa người dùng.
- Gán nhiều vai trò cho người dùng.
- Cấu hình đơn vị, phòng, hệ thống, CSDL.
- Cấu hình đúng danh sách lãnh đạo/người kiểm tra/DBA theo hệ thống.
- Danh mục bị khóa không còn được chọn khi lập yêu cầu.
- Thay đổi cấu hình được ghi audit log.

## 11. Definition of Done

- Có màn hình cấu hình các danh mục chính.
- Có validate trùng mã và dữ liệu bắt buộc.
- Có phân quyền quản trị cấu hình.
- Có test cho CRUD và khóa/ngừng hiệu lực.
