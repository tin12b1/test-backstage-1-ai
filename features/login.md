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
- Đổi vai trò làm việc ngay trên thanh tiêu đề (cạnh biểu tượng người dùng), không cần đăng xuất.
- Màn hình thông tin người dùng (cạnh biểu tượng người dùng): thông tin cá nhân, vai trò, chữ ký, hệ thống thông tin và CSDL liên quan.

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

### 5.1. Đổi vai trò trên thanh tiêu đề

- Cạnh biểu tượng/tên người dùng có dropdown đổi vai trò, chỉ hiển thị khi tài khoản có nhiều vai trò.
- Chọn vai trò khác sẽ kích hoạt active role mới (gọi `POST /session/role`) và về Dashboard, không cần đăng xuất.
- Tài khoản chỉ có một vai trò: hiển thị tên vai trò, không có dropdown.

### 5.2. Màn hình thông tin người dùng (`/profile`)

- Mở bằng cách bấm vào tên người dùng cạnh biểu tượng người dùng.
- Hiển thị: thông tin cá nhân (tài khoản, họ tên, email, ĐTDĐ, đơn vị, phòng, trạng thái); danh sách vai trò và vai trò đang dùng; ảnh chữ ký đã khai báo (`GET /profile/signature`, nếu chưa có thì báo "Chưa khai báo chữ ký"); hệ thống thông tin liên quan và CSDL liên quan (theo phạm vi vai trò của người dùng).

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
- Đổi vai trò trên thanh tiêu đề cập nhật đúng active role mà không phải đăng nhập lại.
- Màn hình `/profile` hiển thị đúng thông tin cá nhân, vai trò, chữ ký, hệ thống/CSDL liên quan.

## 10. Definition of Done

- Hoàn thành màn hình đăng nhập và chọn vai trò.
- Tích hợp được service AD mock/real theo cấu hình.
- Có test cho các case thành công/thất bại/nhiều vai trò.
- Không có lỗi phân quyền cơ bản khi truy cập Dashboard.
