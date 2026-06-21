# Feature: Màn hình Dashboard

**Người phụ trách:** Khương  
**Mã hạng mục:** 2.2

## 1. Mục tiêu

Thiết kế Dashboard theo vai trò để mỗi người dùng chỉ nhìn thấy chỉ số, cảnh báo, danh sách công việc và nút thao tác phù hợp với nhiệm vụ được phân công.

## 2. Vai trò Dashboard

| Vai trò | Trọng tâm hiển thị |
|---|---|
| Người lập yêu cầu | Tạo mới, theo dõi trạng thái, xử lý yêu cầu bị chuyển trả, hoàn thiện 05B |
| Người phê duyệt | Yêu cầu chờ phê duyệt, yêu cầu khẩn cấp, thống kê đã xử lý |
| Người mở truy cập/Quản trị CSDL/Người kiểm tra | Tiếp nhận yêu cầu đã phê duyệt, xác nhận thực hiện, theo dõi yêu cầu sắp hết thời gian |

## 3. Dashboard người lập yêu cầu

### Chỉ số tổng quan

- Tổng số yêu cầu đã lập.
- Số yêu cầu đang chờ phê duyệt.
- Số yêu cầu đã được phê duyệt.
- Số yêu cầu đang chờ mở truy cập.
- Số yêu cầu đã hoàn thành.
- Số yêu cầu bị chuyển trả.
- Số yêu cầu khẩn cấp chưa hoàn thiện hồ sơ sau xử lý, mẫu 05B-HTKC.

### Danh sách chi tiết

- Danh sách yêu cầu đang chờ phê duyệt.
- Danh sách yêu cầu chờ thực hiện bởi người mở truy cập/người quản trị CSDL/người kiểm tra, có hiển thị đang ở bước nào.
- Danh sách yêu cầu bị chuyển trả.
- Danh sách yêu cầu khẩn cấp chưa hoàn thiện hồ sơ sau xử lý.
- Danh sách yêu cầu đã được phê duyệt.

### Nút truy cập nhanh

- Xem chi tiết.
- In danh sách Excel/PDF.
- Tạo yêu cầu theo mẫu phiếu.
- Tra cứu lịch sử.

## 4. Dashboard người phê duyệt

### Chỉ số

- Số yêu cầu chờ tôi phê duyệt.
- Số yêu cầu khẩn cấp chờ phê duyệt.
- Số yêu cầu đã phê duyệt trong ngày.
- Số yêu cầu đã chuyển trả.
- Số yêu cầu theo từng loại nghiệp vụ.
- Số yêu cầu theo từng đơn vị gửi yêu cầu.

### Danh sách chi tiết

- Danh sách yêu cầu chờ phê duyệt thông thường và khẩn cấp theo từng đơn vị, nhóm hệ thống.

### Nút truy cập nhanh

- Phê duyệt.
- In danh sách.
- Tra cứu.

## 5. Dashboard người mở truy cập/DBA/người kiểm tra

### Chỉ số

- Số yêu cầu đã phê duyệt chờ bộ phận mở truy cập/quản trị CSDL.
- Số yêu cầu đang thực hiện mở truy cập.
- Số yêu cầu bị chuyển trả do không hợp lệ.
- Số yêu cầu sắp hết thời gian truy cập.

### Danh sách chi tiết

- Danh sách yêu cầu đã phê duyệt chờ cấp quyền/xác nhận.
- Danh sách yêu cầu đang thực hiện mở truy cập, đã mở và đang hiệu lực.
- Danh sách yêu cầu bị chuyển trả do không hợp lệ.
- Danh sách yêu cầu sắp hết thời gian truy cập, trước 1 giờ khi thu hồi quyền.
- Danh sách yêu cầu cần đóng quyền hoặc thu hồi quyền. Nội dung này cần làm rõ thêm với DBA.

### Nút truy cập nhanh

- Cấp quyền.
- Xác nhận.
- In danh sách.
- Tra cứu.

## 6. Quy tắc giao diện

- Trạng thái yêu cầu phải hiển thị rõ bằng nhãn, màu sắc hoặc biểu tượng.
- Dữ liệu dashboard phải theo active role trong session.
- Không hiển thị dữ liệu ngoài phạm vi phân quyền.
- Bấm vào dòng dữ liệu phải xem được chi tiết yêu cầu nếu user có quyền.

## 7. Allowed Files

- `src/main/java/.../dashboard/**`
- `src/main/resources/templates/dashboard/**`
- `src/main/resources/static/js/dashboard/**`
- `src/main/resources/static/css/dashboard/**`
- `src/test/java/.../dashboard/**`

## 8. Must Not Change

- Không sửa logic workflow chuyển trạng thái.
- Không sửa form lập yêu cầu.
- Không sửa API tích hợp OTP/AD/Email.

## 9. Verification

- Login với từng vai trò và kiểm tra dashboard hiển thị đúng.
- User người lập chỉ thấy yêu cầu mình lập/liên quan.
- Người phê duyệt chỉ thấy yêu cầu chờ mình xử lý.
- Bộ phận mở truy cập/DBA/người kiểm tra chỉ thấy yêu cầu thuộc phạm vi xử lý.
- Các chỉ số khớp với danh sách chi tiết.

## 10. Definition of Done

- Có dashboard cho 3 nhóm vai trò chính.
- Có danh sách chi tiết và nút truy cập nhanh.
- Có kiểm tra phân quyền dữ liệu.
- Có test/service test cho thống kê theo vai trò.
