# Vision - Hệ thống quản lý truy cập CSDL

## 1. Mục tiêu

Xây dựng phần mềm quản lý yêu cầu truy cập, truy xuất, chỉnh sửa dữ liệu, thay đổi cấu trúc CSDL, cấp mới/thay đổi thuộc tính tài khoản và xử lý truy cập khẩn cấp đối với CSDL.

Hệ thống thay thế quy trình xử lý thủ công bằng quy trình điện tử có đăng nhập AD, lập phiếu, ký xác nhận bằng SoftOTP/OTP, phê duyệt theo vai trò, chuyển bộ phận xử lý, ghi nhận thời gian thực hiện, thông báo email và lưu nhật ký công việc.

## 2. Phạm vi nghiệp vụ

Hệ thống hỗ trợ các loại yêu cầu sau:

| STT | Loại yêu cầu | Mẫu phiếu |
|---:|---|---|
| 1 | Truy cập, truy xuất CSDL thông thường | 01-YCTC |
| 2 | Chỉnh sửa dữ liệu | 02-YCCS |
| 3 | Thay đổi cấu trúc CSDL | 03-YCCT |
| 4 | Cấp mới/thay đổi thuộc tính tài khoản | 04A-YCTK |
| 5 | Truy cập khẩn cấp | 05A-YCKC |
| 6 | Hoàn thành truy cập khẩn cấp | 05B-HTKC |
| 7 | Nhật ký công việc | 07-NKCV |

> Ghi chú: Trong tài liệu nghiệp vụ có xuất hiện cả `01-YCCT` và `01-YCTC`. Khi triển khai cần thống nhất lại mã chính thức trước khi chốt enum trong code/database.

## 3. Các chức năng chính

- Đăng nhập bằng tài khoản AD.
- Dashboard theo vai trò.
- Lập và gửi yêu cầu.
- Phê duyệt, kiểm tra, xác nhận của Trưởng phòng/tương đương, Người có thẩm quyền, Bộ phận kiểm tra, Bộ phận mở truy cập, Quản trị CSDL/DBA, Người thực hiện.
- Tra cứu, báo cáo.
- Cấu hình danh mục người dùng, CSDL, đơn vị, vai trò, trạng thái.
- Tích hợp AD, SoftOTP/OTP và Email.

## 4. Vai trò sử dụng

| Vai trò | Nhiệm vụ chính |
|---|---|
| Người lập yêu cầu | Tạo mới, lưu nháp, ký xác nhận, gửi phê duyệt, theo dõi trạng thái, xử lý yêu cầu bị chuyển trả |
| Người dùng chung phiếu | Ký xác nhận phần chi tiết nếu có tham gia yêu cầu trên cùng phiếu |
| Trưởng phòng hoặc tương đương | Kiểm tra, ký xác nhận, chuyển người có thẩm quyền hoặc chuyển trả |
| Người có thẩm quyền | Phê duyệt, ký xác nhận, chuyển bước tiếp theo hoặc chuyển trả |
| Bộ phận kiểm tra | Kiểm tra nội dung/script, ký xác nhận, chuyển lãnh đạo hoặc chuyển trả |
| Bộ phận mở truy cập | Tiếp nhận yêu cầu đã phê duyệt/khẩn cấp, xác nhận mở truy cập, ghi thời gian, ký xác nhận |
| Quản trị CSDL/DBA | Xử lý yêu cầu cấu trúc CSDL/tài khoản, ghi nhận thực hiện |
| Người thực hiện | Chạy script/chỉnh sửa dữ liệu, ghi thời gian, ký xác nhận |
| Quản trị hệ thống | Cấu hình người dùng, vai trò, đơn vị, CSDL, trạng thái |

## 5. Tiêu chí thành công

- Người dùng đăng nhập AD và hệ thống xác định đúng vai trò.
- Người lập chọn đúng mẫu phiếu, nhập đủ thông tin, ký xác nhận và gửi phê duyệt.
- Hệ thống tự động sinh mã yêu cầu, kiểm tra trường bắt buộc, ràng buộc danh mục hợp lệ và cập nhật trạng thái đúng.
- Luồng xử lý tự động xác định người/bộ phận tiếp theo theo loại yêu cầu, đơn vị yêu cầu, đơn vị chủ quản ứng dụng, đơn vị chủ quản CSDL và vai trò.
- Mỗi bước ký xác nhận thành công phải ghi người ký, thời gian ký và nội dung xử lý.
- Yêu cầu bị chuyển trả phải bắt buộc nhập lý do, không cần ký xác nhận.
- Dashboard, tra cứu và báo cáo chỉ hiển thị dữ liệu đúng phạm vi phân quyền.
- Hệ thống tự động gửi email khi phát sinh sự kiện nghiệp vụ.
- Hệ thống tự động ghi nhật ký công việc theo mẫu 07-NKCV.

## 6. Ngoài phạm vi giai đoạn đầu

- Tự động cấp quyền trực tiếp vào toàn bộ các loại CSDL nếu chưa có đặc tả kỹ thuật từ DBA.
- Báo cáo quản trị nâng cao khi chưa có mẫu báo cáo cụ thể.
- Tích hợp captcha/Google Authenticator là tùy chọn, chưa bắt buộc ở giai đoạn đầu.
