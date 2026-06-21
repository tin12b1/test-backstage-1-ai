# Feature: Chức năng lập và gửi yêu cầu

**Người phụ trách:** Tín  
**Mã hạng mục:** 2.3

## 1. Mục tiêu

Cho phép người lập yêu cầu đăng nhập, chọn mẫu phiếu, nhập thông tin, ký xác nhận, lưu nháp, gửi phê duyệt hoặc gửi bộ phận Mở truy cập đối với yêu cầu khẩn cấp.

## 2. Mẫu phiếu hỗ trợ

- 01-YCCT/01-YCTC: Truy cập, truy xuất CSDL thông thường.
- 02-YCCS: Chỉnh sửa dữ liệu.
- 03-YCCT: Thay đổi cấu trúc CSDL.
- 04A-YCTK: Cấp mới/thay đổi thuộc tính tài khoản.
- 05A-YCKC: Truy cập khẩn cấp.
- 05B-HTKC: Hoàn thành truy cập khẩn cấp.

> Cần thống nhất lại mã `01-YCCT` hay `01-YCTC` trước khi chốt enum.

## 3. Luồng chung cho 01, 02, 03, 04A, 05B

1. Người lập yêu cầu đăng nhập bằng tài khoản AD.
2. Chọn chức năng “Lập yêu cầu”, chọn mẫu phiếu.
3. Nhập nội dung chi tiết cho từng mẫu phiếu.
4. Ký xác nhận gửi phê duyệt.
5. Hệ thống kiểm tra trường bắt buộc, sinh mã yêu cầu.
6. Hệ thống lưu hồ sơ ở trạng thái:
   - `Chờ phê duyệt`; hoặc
   - `Chờ kiểm tra` với mẫu có yêu cầu kiểm tra.
7. Hệ thống gửi email đến Trưởng phòng/tương đương hoặc Bộ phận kiểm tra.

## 4. Luồng riêng cho 05A-YCKC

1. Người yêu cầu đăng nhập bằng tài khoản AD.
2. Chọn chức năng “Lập yêu cầu”, mẫu 05A-YCKC.
3. Nhập nội dung chi tiết.
4. Người lập ký xác nhận.
5. Hệ thống kiểm tra trường bắt buộc, sinh mã yêu cầu.
6. Hệ thống lưu hồ sơ ở trạng thái “Đã chuyển bộ phận Mở truy cập”.

## 5. Quy tắc nghiệp vụ chung

- Khi lập yêu cầu, hệ thống ràng buộc danh mục CSDL, người dùng với đơn vị chủ quản ứng dụng; chỉ cho phép chọn danh mục hợp lệ.
- Chặn không cho phép người sử dụng đang nợ phiếu 05B-HTKC lập phiếu yêu cầu mới, kể cả trường hợp nhiều người sử dụng trên một phiếu.
- Cho phép lưu nháp và sửa lại phiếu nếu chưa gửi phê duyệt.
- Cho phép gửi lại yêu cầu nếu yêu cầu bị gửi lỗi.
- Cho phép hủy yêu cầu nếu chưa được phê duyệt.
- Sau khi gửi phê duyệt không được sửa nội dung.

## 6. Quy tắc riêng mẫu 01-YCTC và 04A-YCTK

- Mỗi người dùng chỉ cần ký xác nhận một lần trên phần thông tin chung hoặc phần thông tin chi tiết.
- Hệ thống tự động điền chữ ký cho phần thông tin chung và các dòng chi tiết liên quan.
- Người yêu cầu và những người sử dụng chung phiếu phải ký xác nhận trước khi gửi lãnh đạo phê duyệt.
- Phải có tối thiểu một dòng tại phần danh sách yêu cầu chi tiết.
- Người dùng có thể yêu cầu truy cập nhiều CSDL trên một phiếu và chỉ cần ký một lần.

## 7. Quy tắc riêng mẫu 03-YCCT

- Hệ thống cho phép chọn hoặc nhập thêm hệ thống liên quan.
- Có 3 tab chi tiết: Tạo mới, Thay đổi, Xóa.
- Có phần nội dung DBA ghi để đánh giá tác động ảnh hưởng và hệ thống liên quan.
- Cho phép tải SQL Script và nhập mã kiểm tra.

## 8. Giao diện mẫu 01-YCTC

### Thông tin chung

| Trường | Loại | Mô tả |
|---|---|---|
| Mã yêu cầu | Tự động | `KýhiệuĐV_NgàyThángNăm` |
| Ca | Chọn | 1, 2, 3 |
| Lần | Chọn | 1, 2, 3, 4, 5 |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại |
| Thời gian truy cập/truy xuất | Tự động | Theo ca: Ca 1 0-8h, Ca 2 8-20h, Ca 3 20-24h |
| Ký tên | Nhập OTP | Sau khi ký thành công hiển thị ảnh chữ ký |
| Ký xác nhận | Nút lệnh | Gọi OTP |
| Danh sách Trưởng phòng/tương đương | Tự động | Lấy theo người dùng |
| Gửi phê duyệt | Nút lệnh | Gửi luồng xử lý |

### Thông tin chi tiết

| Trường | Loại | Mô tả |
|---|---|---|
| Hệ thống thông tin | Chọn | Theo danh mục hợp lệ |
| CSDL | Chọn | Theo hệ thống/đơn vị chủ quản |
| Tên đối tượng | Nhập | Bảng/đối tượng dữ liệu |
| Quyền truy cập | Chọn | Theo danh mục quyền |
| Họ và tên | Tự động | Người sử dụng |
| Ký tại mục chi tiết | Nhập OTP | Nếu người lập và người truy cập là một thì không cần ký tại mục chi tiết |
| Ký xác nhận | Nút lệnh | Ký dòng chi tiết |
| Mục đích/Lý do | Nhập | Bắt buộc |

## 9. Giao diện mẫu 02-YCCS

| Trường | Loại | Mô tả |
|---|---|---|
| Tên hệ thống | Chọn | Theo danh mục |
| Tên cơ sở dữ liệu | Chọn | Theo hệ thống |
| Mã yêu cầu | Tự động | `KýhiệuĐV_NgàyThángNăm` |
| Ca | Chọn | 1, 2, 3 |
| Lần | Chọn | 1, 2, 3, 4, 5 |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại |
| Thời gian cập nhật | Tự động | Theo ca: Ca 1 0-8h, Ca 2 8-20h, Ca 3 20-24h |
| Tên tệp cần chạy | Tải file | Quy tắc `YYYYMMDD_BS_XXX.sql`; hệ thống kiểm tra đúng định dạng mới cho tải; nếu nhiều file cần gộp thành một |
| Mã kiểm tra tính toàn vẹn | Nhập | Checksum/mã kiểm tra |
| Nội dung chỉnh sửa | Nhập | Bắt buộc |
| Ký tên | Nhập OTP | Hiển thị ảnh chữ ký sau khi ký |
| Ký xác nhận | Nút lệnh | Gọi OTP |
| Danh sách Người kiểm tra của đơn vị chủ quản ứng dụng | Tự động | Lấy theo tên hệ thống |
| Gửi phê duyệt | Nút lệnh | Gửi Bộ phận kiểm tra/luồng xử lý |

## 10. Giao diện mẫu 03-YCCT

### Thông tin chung

- Mã yêu cầu, ca, lần, đơn vị, phòng, người yêu cầu, ĐTDĐ, ngày lập.
- Ngày thực hiện dự kiến.
- Phần nội dung DBA ghi: đánh giá tác động ảnh hưởng và hệ thống liên quan.
- Loại yêu cầu dạng tab: Tạo mới, Thay đổi, Xóa.
- Ký tên bằng OTP, ký xác nhận, danh sách Trưởng phòng/tương đương, gửi phê duyệt.

### Tab tạo mới/xóa

- Table: Owner, Table name, dự kiến tăng trưởng, vòng đời lưu trữ, cột xác định vòng đời, đối tượng phụ thuộc.
- Cấu trúc table: tên bảng, tên cột, kiểu dữ liệu, cho phép Null Y/N, giá trị mặc định, mô tả.
- Index: Owner, tên index, table owner, tên bảng, danh sách cột đánh chỉ mục.
- Synonym: tên synonym, kiểu Public/Private, table owner, tên bảng, mô tả.
- Tạo mới/xóa khác: owner, tên, kiểu, mô tả.
- SQL Script: file, mã kiểm tra, tên file.

### Tab thay đổi

- Thêm cột bảng: owner, tên bảng, tên cột, loại dữ liệu, mô tả.
- Sửa cột bảng: owner, tên bảng, tên cột, giá trị cũ, giá trị mới, mô tả.
- Tạo lại index: owner, tên bảng, tên index cũ, cột trong index, index mới, cột đánh index mới.
- Thay đổi khác: owner, tên, kiểu, mô tả.
- SQL Script: file, mã kiểm tra, tên file.

## 11. Giao diện mẫu 04A-YCTK

- Mã yêu cầu, ca, lần, đơn vị, phòng, người yêu cầu, ĐTDĐ, ngày lập.
- Thời gian sử dụng từ bắt đầu đến kết thúc.
- Lý do yêu cầu.
- Ký tên bằng OTP, ký xác nhận, danh sách Trưởng phòng/tương đương, gửi phê duyệt.
- Chi tiết tài khoản:
  - Họ tên chủ tài khoản.
  - Loại tài khoản: Truy cập/Chỉnh sửa.
  - Hình thức: Cấp mới/Đổi thuộc tính.
  - Ký tại mục chi tiết nếu người lập và người truy cập không phải là một.

## 12. Giao diện mẫu 05A-YCKC

- Tên hệ thống, tên CSDL.
- Mã yêu cầu, ca, lần, đơn vị, phòng, người yêu cầu, ĐTDĐ, ngày lập.
- Thời gian sử dụng từ/đến, ràng buộc theo ca.
- Lý do yêu cầu.
- Ký tên bằng OTP, ký xác nhận, gửi phê duyệt/chuyển bộ phận mở.
- Quyền trên đối tượng dữ liệu:
  - QueryAll: nếu tích chọn thì các quyền sau chuyển `enable=false`.
  - Owner, tên bảng.
  - Select, Insert, Update, Delete.

## 13. Giao diện mẫu 05B-HTKC

- Tên hệ thống, tên CSDL.
- Mã yêu cầu, ca, lần, đơn vị, phòng, người yêu cầu, ĐTDĐ, ngày lập.
- Thời gian yêu cầu từ/đến, ràng buộc theo ca.
- Mục đích.
- Ký tên bằng OTP, ký xác nhận, gửi phê duyệt.
- Nội dung công việc đã thực hiện:
  - Chọn mã yêu cầu 05A-YCKC từ danh sách 05A chưa có 05B.
  - Owner, tên bảng.
  - Select, Insert, Update, Delete.

## 14. Allowed Files

- `src/main/java/.../request/**`
- `src/main/java/.../workflow/RequestSubmissionService.java`
- `src/main/resources/templates/requests/**`
- `src/main/resources/static/js/requests/**`
- `src/main/resources/static/css/requests/**`
- `src/test/java/.../request/**`

## 15. Must Not Change

- Không sửa màn hình Dashboard ngoài link/nút cần thiết.
- Không sửa service AD/Email/OTP ngoài interface đã thống nhất.
- Không sửa xử lý phê duyệt sau khi yêu cầu đã gửi, trừ phần khởi tạo bước đầu.

## 16. Verification

- Lưu nháp từng mẫu phiếu.
- Ký xác nhận thành công bằng OTP.
- Gửi phiếu 01/04A với nhiều dòng chi tiết và nhiều người ký.
- Chặn gửi nếu thiếu chữ ký người dùng chung phiếu.
- Chặn gửi nếu không có dòng chi tiết với 01/04A.
- Chặn lập phiếu mới nếu người dùng nợ 05B.
- Mẫu 05A sau gửi vào trạng thái “Đã chuyển bộ phận Mở truy cập”.
- File SQL 02-YCCS kiểm tra đúng định dạng tên.

## 17. Definition of Done

- Hoàn thành form cho 6 mẫu phiếu.
- Có lưu nháp, sửa nháp, ký, gửi, hủy, gửi lại.
- Có validate nghiệp vụ và validate giao diện.
- Có test cho các luồng chính và lỗi nghiệp vụ quan trọng.
