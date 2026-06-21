# Feature: Màn hình tra cứu, báo cáo

**Người phụ trách:** Khương  
**Mã hạng mục:** 2.5

## 1. Mục tiêu

Cung cấp màn hình tra cứu yêu cầu và xuất danh sách theo phân quyền. Báo cáo chi tiết sẽ bổ sung khi có mẫu yêu cầu cụ thể.

## 2. Phạm vi tra cứu

Người dùng chỉ tìm kiếm được các yêu cầu mình có liên quan theo vai trò:

- Người lập: yêu cầu do mình lập hoặc mình là người dùng chung trên phiếu.
- Người phê duyệt: yêu cầu mình đã/đang phê duyệt trong phạm vi phân quyền.
- Người kiểm tra: yêu cầu mình đã/đang kiểm tra.
- Người thực hiện/Mở truy cập/DBA: yêu cầu mình đã/đang thực hiện.
- Quản trị hệ thống: theo phạm vi được cấp quyền.

## 3. Điều kiện tìm kiếm

- Ngày lập/gửi/xử lý.
- Trạng thái.
- Người thực hiện/người xử lý.
- Đơn vị.
- Phòng/bộ phận.
- Hệ thống.
- CSDL.
- Loại yêu cầu.
- Mã yêu cầu.

## 4. Danh sách kết quả

Các cột tối thiểu:

- Mã yêu cầu.
- Loại yêu cầu.
- Người lập yêu cầu.
- Đơn vị yêu cầu.
- Hệ thống/CSDL.
- Thời gian truy cập/truy xuất/thực hiện.
- Quyền truy cập.
- Trạng thái xử lý.
- Thời gian gửi yêu cầu.
- Người/bộ phận đang xử lý.

## 5. Chức năng

- Tìm kiếm/lọc dữ liệu.
- Xem chi tiết yêu cầu.
- Xem luồng xử lý.
- In/xuất danh sách Excel hoặc PDF.
- Báo cáo: chờ mẫu yêu cầu cụ thể.

## 6. Quy tắc phân quyền

- Query dữ liệu phải áp dụng điều kiện phân quyền ở service/repository, không chỉ ẩn trên giao diện.
- Không cho xem chi tiết nếu user không có quan hệ với phiếu hoặc không có quyền quản trị.
- Xuất file phải áp dụng cùng điều kiện tìm kiếm và phân quyền như màn hình.

## 7. Allowed Files

- `src/main/java/.../search/**`
- `src/main/java/.../report/**`
- `src/main/resources/templates/search/**`
- `src/main/resources/templates/report/**`
- `src/main/resources/static/js/search/**`
- `src/test/java/.../search/**`
- `src/test/java/.../report/**`

## 8. Must Not Change

- Không sửa workflow phê duyệt.
- Không sửa logic lập yêu cầu.
- Không sửa tích hợp OTP/AD/Email.

## 9. Verification

- Tìm kiếm theo từng tiêu chí chính.
- Kiểm tra người lập chỉ thấy dữ liệu của mình/liên quan.
- Kiểm tra người phê duyệt chỉ thấy dữ liệu trong phạm vi.
- Xuất Excel/PDF đúng kết quả đang lọc.
- Không truy cập được chi tiết phiếu ngoài quyền.

## 10. Definition of Done

- Có màn hình tra cứu với bộ lọc chính.
- Có danh sách kết quả và xem chi tiết.
- Có xuất danh sách Excel/PDF.
- Có test phân quyền tra cứu.
