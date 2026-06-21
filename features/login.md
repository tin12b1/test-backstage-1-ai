# Feature: Chức năng đăng nhập

**Người phụ trách:** Khương  
**Mã hạng mục:** 2.1

## 1. Mục tiêu

Cho phép người sử dụng đăng nhập hệ thống bằng tài khoản AD, hệ thống truy vấn vai trò đã đăng ký để hiển thị Dashboard và chức năng phù hợp.

## 2. Phạm vi

- Màn hình đăng nhập.
- Xác thực user AD.
- Mã xác nhận (captcha) ảnh chống đăng nhập tự động.
- Ghi log đăng nhập.
- Quản lý phiên đăng nhập.
- Nếu user có nhiều vai trò, hiển thị màn hình chọn vai trò làm việc.
- Sau khi chọn vai trò, chuyển đến Dashboard theo vai trò.

## 3. Ngoài phạm vi

- Captcha: đã triển khai mã xác nhận dạng ảnh sinh phía server (bắt buộc khi đăng nhập). Google Authenticator vẫn là option, chưa bắt buộc.
- Không tự ý thay đổi cơ chế phân quyền của các module khác.

## 4. Luồng nghiệp vụ

1. Người dùng mở màn hình đăng nhập.
2. Nhập user/password AD và mã xác nhận (captcha).
3. Hệ thống kiểm tra captcha; sai thì báo lỗi và không gọi AD.
4. Hệ thống gọi service AD để xác thực.
5. Nếu không hợp lệ, hiển thị lỗi và ghi log thất bại.
6. Nếu hợp lệ, hệ thống truy vấn danh sách vai trò của user trong hệ thống.
7. Nếu có một vai trò, lưu session với vai trò đó và chuyển Dashboard.
8. Nếu có nhiều vai trò, hiển thị màn hình chọn vai trò.
9. Sau khi chọn vai trò, lưu active role vào session và chuyển Dashboard.

## 5. Yêu cầu giao diện

- Form đăng nhập gồm: user AD, mật khẩu, mã xác nhận (ảnh captcha + nút đổi mã), nút đăng nhập.
- Thông báo lỗi rõ ràng khi đăng nhập sai hoặc user chưa được đăng ký vai trò.
- Màn hình chọn vai trò nếu user có nhiều vai trò.
- Không hiển thị thông tin kỹ thuật/stacktrace cho người dùng.

## 6. Quy tắc nghiệp vụ

- Chỉ user AD xác thực thành công và có vai trò active mới được vào hệ thống.
- Mọi request sau đăng nhập phải kiểm tra session.
- Active role trong session quyết định dashboard và menu.
- Ghi log đăng nhập thành công/thất bại.
- Session timeout theo cấu hình hệ thống.

## 7. Allowed Files

- `src/main/java/.../auth/**`
- `src/main/java/.../config/SecurityConfig.java`
- `src/main/java/.../integration/ad/**`
- `src/main/resources/templates/login/**`
- `src/main/resources/templates/auth/**`
- `src/main/resources/static/js/auth/**`
- `src/main/resources/static/css/auth/**`
- `src/test/java/.../auth/**`

## 8. Must Not Change

- Không sửa workflow phê duyệt.
- Không sửa cấu trúc form lập yêu cầu.
- Không sửa module báo cáo/cấu hình nếu không cần thiết.

## 9. Verification

- Đăng nhập AD thành công với user có một vai trò.
- Nhập sai mã xác nhận bị từ chối, không gọi AD; mã captcha dùng một lần.
- Đăng nhập AD thành công với user có nhiều vai trò và chọn được vai trò.
- Đăng nhập thất bại hiển thị lỗi.
- User chưa có vai trò không được vào hệ thống.
- Log đăng nhập được ghi.
- Session lưu đúng username và active role.

## 10. Definition of Done

- Hoàn thành màn hình đăng nhập và chọn vai trò.
- Tích hợp được service AD mock/real theo cấu hình.
- Có test cho các case thành công/thất bại/nhiều vai trò.
- Không có lỗi phân quyền cơ bản khi truy cập Dashboard.
