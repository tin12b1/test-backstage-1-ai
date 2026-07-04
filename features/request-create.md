# Feature: Chức năng lập và gửi yêu cầu

**Người phụ trách:** Tín  
**Mã hạng mục:** 2.3

## 1. Mục tiêu

Cho phép người lập yêu cầu đăng nhập, chọn mẫu phiếu, nhập thông tin, ký xác nhận, lưu nháp, gửi phê duyệt hoặc gửi bộ phận Mở truy cập đối với yêu cầu khẩn cấp.
Cho phép người yêu cầu có thể đăng ký trước phần chi tiết của mẫu 01-YCTC, thời gian tối đa cho phép đăng ký trước là 1 tuần, hàng ngày người lập yêu cầu có thể tạo phiếu và lấy thông tin chi tiết của danh sách người đăng ký trong cùng 1 phòng/bộ phận.
## 2. Mẫu phiếu hỗ trợ

- 01-YCCT/01-YCTC: Truy cập, truy xuất CSDL thông thường.
- 02-YCCS: Chỉnh sửa dữ liệu.
- 03-YCCT: Thay đổi cấu trúc CSDL.
- 04A-YCTK: Cấp mới/thay đổi thuộc tính tài khoản.
- 04B-BGTK: Bàn giao tài khoản
- 05A-YCKC: Truy cập khẩn cấp.
- 05B-HTKC: Hoàn thành truy cập khẩn cấp.


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
## 5. Luồng riêng cho 04B-BGTK
-	DBA tạo biên bản giàn giao tài khoản,  ghi thời gian thực hiện, ký xác nhận gửi lãnh đạo phòng ký xác nhận.
-	Chủ tài khoản(người yêu cầu cấp tài khoản) và người lập yêu cầu mẫu 04A-YCTK vào chức năng Bàn giao tài khoản truy cập ký xác nhận bàn giao tài khoản, gửi lãnh đạo phòng phụ trách ký xác nhận.


## 6. Quy tắc nghiệp vụ chung

- Khi lập yêu cầu, hệ thống ràng buộc danh mục CSDL, người dùng với đơn vị chủ quản ứng dụng; chỉ cho phép chọn danh mục hợp lệ.
- Chặn không cho phép người sử dụng đang nợ phiếu 05B-HTKC lập phiếu yêu cầu mới, kể cả trường hợp nhiều người sử dụng trên một phiếu.
- Cho phép lưu nháp và sửa lại phiếu nếu chưa gửi phê duyệt.
- Cho phép gửi lại yêu cầu nếu yêu cầu bị gửi lỗi.
- Cho phép hủy yêu cầu nếu chưa được phê duyệt.
- Sau khi gửi phê duyệt không được sửa nội dung.

## 7. Quy tắc riêng mẫu 01-YCTC và 04A-YCTK

- Mỗi người dùng chỉ cần ký xác nhận một lần trên phần thông tin chung hoặc phần thông tin chi tiết.
- Hệ thống tự động điền chữ ký cho phần thông tin chung và các dòng chi tiết liên quan.
- Người yêu cầu và những người sử dụng chung phiếu phải ký xác nhận trước khi gửi lãnh đạo phê duyệt.
- Phải có tối thiểu một dòng tại phần danh sách yêu cầu chi tiết.
- Người dùng có thể yêu cầu truy cập nhiều CSDL trên một phiếu và chỉ cần ký một lần.

## 8. Quy tắc riêng mẫu 03-YCCT

- Hệ thống cho phép chọn hoặc nhập thêm hệ thống liên quan.
- Có 3 tab chi tiết: Tạo mới, Thay đổi, Xóa.
- Có phần nội dung DBA ghi để đánh giá tác động ảnh hưởng và hệ thống liên quan.
- Cho phép tải SQL Script và nhập mã kiểm tra.

## 9. Giao diện mẫu 01-YCTC

### Thông tin chung

| Trường | Loại | Mô tả |
|---|---|---|
| Mã yêu cầu | Tự động | `MãĐơnVị_MãPhòng_yyyyMMddHHmmss` (code đơn vị + code phòng + ngày giờ hiện tại); tự điền sẵn trên form |
| Ca | Chọn | Lấy từ danh mục ca (`work_shift`); chọn ca sẽ tự điền Thời gian truy cập |
| Lần | Chọn | 1, 2, 3, 4, 5 |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại |
| Thời gian truy cập/truy xuất | Tự động (sửa được) | Theo ca: Ca 1 0-8h, Ca 2 8-20h, Ca 3 20-24h |
| Ký tên | Google Authenticator | Nhập mã 6 số từ Google Authenticator (thay SoftOTP); sau khi ký hiển thị ảnh chữ ký đã khai báo, hoặc chữ **"Đã ký"** (màu xanh than, in đậm) nếu người dùng chưa có ảnh chữ ký. Áp dụng cho tất cả các mẫu phiếu |
| Ký xác nhận | Nút lệnh | Đặt trên thanh hành động, **ngay cạnh** nút "Gửi phê duyệt"; mở nhập mã Google Authenticator |
| Danh sách Trưởng phòng/tương đương | Chọn | Theo phòng/đơn vị của người dùng |
| Gửi phê duyệt | Nút lệnh | Gửi luồng xử lý |

> 01-YCTC **không** chọn Hệ thống/CSDL ở phần thông tin chung; hệ thống và CSDL được chọn theo
> từng dòng chi tiết. Mã yêu cầu vì vậy sinh theo đơn vị + phòng (không theo ký hiệu hệ thống).

### Thông tin chi tiết

| Trường | Loại | Mô tả |
|---|---|---|
| Hệ thống thông tin | Chọn | Theo danh mục hợp lệ; ràng buộc với CSDL và Họ và tên |
| CSDL | Chọn | Lọc theo hệ thống; chọn CSDL sẽ tự suy ra hệ thống |
| Tên đối tượng | Nhập | Mặc định `All Schema` |
| Quyền truy cập | Chọn (list box, đa chọn) | Mặc định `SELECT, INSERT, UPDATE, DELETE` |
| Họ và tên | Chọn | Người sử dụng; lọc theo hệ thống đã chọn |
| Ký tại mục chi tiết | Google Authenticator | Nếu người lập và người truy cập là một thì không cần ký tại mục chi tiết |
| Mục đích/Lý do | Nhập | **Một dòng lý do chung** ở cuối danh sách chi tiết (không nhập theo từng dòng) |

> Ràng buộc dữ liệu hai chiều trong từng dòng: chọn một trong ba cột (Hệ thống / CSDL / Họ và tên)
> sẽ tự lọc hai cột còn lại theo danh mục và phạm vi phân quyền của người dùng.

### 9.1. Đăng ký nhanh và lấy dữ liệu đã đăng ký (01YCTC_Dangky)

- **Màn đăng ký** (`/requests/register/YCTC_01`): đăng ký một dòng chi tiết gồm Từ ngày/Đến ngày,
  Ca, Người yêu cầu (tự động), Hệ thống, CSDL, Tên đối tượng, Quyền truy cập và ký bằng
  Google Authenticator. Khi gửi: tạo phiếu nháp 01-YCTC, ký mục chi tiết và lưu một bản ghi vào
  bảng `access_registration`.
- **Ràng buộc thời gian đăng ký:**
  - Từ ngày **≥** ngày hiện tại.
  - Đến ngày **≤** ngày hiện tại **+ 7** ngày (đăng ký trước tối đa 1 tuần).
  - Đến ngày ≥ Từ ngày.
  - Ràng buộc được áp cả ở giao diện (thuộc tính `min`/`max` của ô ngày) và kiểm tra lại phía máy chủ
    (báo lỗi nghiệp vụ nếu vượt phạm vi).
- **Hệ thống thông tin & CSDL:** lấy theo **dữ liệu người dùng đã đăng ký** (bảng
  `access_registration`); hai ô **liên kết** với nhau (chọn Hệ thống thì CSDL lọc theo và ngược lại).
  Lần đầu chưa có dữ liệu đăng ký thì lấy theo phạm vi hệ thống/CSDL người dùng được phân quyền.
- **Bố cục màn hình:** toàn bộ nằm trong **một khung (frame/card)** duy nhất. Tên đối tượng và Quyền
  truy cập xếp dọc (Quyền **ngay dưới** Tên đối tượng) ở cột trái; label **"Ký xác nhận
  (Google Authenticator)"** cùng ô nhập mã 6 số nằm ở **cột phải, ngay trên** nút lệnh. Các ô Tên
  đối tượng, Quyền truy cập, Ký xác nhận có **độ rộng bằng** ô Hệ thống thông tin.
- **Nút lệnh "Ký và lưu lại"**: đặt ở cột phải (cùng bên với ô Ký xác nhận); ký xác nhận bằng OTP và
  lưu dữ liệu đăng ký trong một thao tác.
- **Nút "Lấy dữ liệu đã đăng ký"** trên form lập 01-YCTC: quét bảng `access_registration` của
  người dùng và nạp các bản ghi thành các dòng chi tiết.

## 10. Giao diện mẫu 02-YCCS

| Trường | Loại | Mô tả |
|---|---|---|
| Tên hệ thống | Chọn | Lấy theo dữ liệu người dùng đã đăng ký; liên kết với ô CSDL (chọn hệ thống thì CSDL lọc theo và ngược lại) |
| Tên cơ sở dữ liệu | Chọn | Lấy theo dữ liệu người dùng đã đăng ký; liên kết với ô Tên hệ thống |
| Mã yêu cầu | Tự động | Cùng cấu trúc 01-YCTC: `MãĐơnVị_MãPhòng_yyyyMMddHHmmss` |
| Ca | Chọn | 1, 2, 3 — **mặc định 2** |
| Lần | Chọn | 1, 2, 3, 4, 5 — **mặc định 1** |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại |
| Thời gian cập nhật | Tự động | Theo ca: Ca 1 0-8h, Ca 2 8-20h, Ca 3 20-24h |
| Tệp SQL/Script | Nút tải file | Nút chọn tệp; ô hiển thị tên tệp đã tải (quy tắc `YYYYMMDD_BS_XXX.sql`); lưu vào `request_script_file` |
| Mã kiểm tra tính toàn vẹn | Nhập | Người dùng nhập checksum/mã kiểm tra của tệp |
| Nội dung chỉnh sửa | Nhập | Bắt buộc |
| Ký tên | Google Authenticator | Sau khi ký hiển thị ảnh chữ ký đã khai báo, hoặc chữ **"Đã ký"** (màu xanh than, in đậm) nếu chưa có ảnh |
| Ký xác nhận | Nút lệnh | Đặt trên thanh hành động, **ngay cạnh** nút "Gửi phê duyệt"; mở nhập mã Google Authenticator |
| Danh sách Người kiểm tra của đơn vị chủ quản ứng dụng | Tự động | Lấy theo tên hệ thống |
| Gửi phê duyệt | Nút lệnh | Gửi Bộ phận kiểm tra/luồng xử lý |

## 11. Giao diện mẫu 03-YCCT

### Thông tin chung

- Mã yêu cầu tự sinh cùng cấu trúc 01-YCTC: `MãĐơnVị_MãPhòng_yyyyMMddHHmmss`.
- Ca (**mặc định 2**), Lần (**mặc định 1**), đơn vị, phòng, người yêu cầu, ĐTDĐ, ngày lập.
- Ngày thực hiện dự kiến: **mặc định ngày hiện tại**, cho phép sửa.
- Phần nội dung DBA ghi: đánh giá tác động ảnh hưởng và hệ thống liên quan.
- Loại yêu cầu dạng tab: Tạo mới, Thay đổi, Xóa.
- Ký tên bằng Google Authenticator (ô chữ ký hiển thị ảnh chữ ký đã khai báo, hoặc chữ **"Đã ký"**
  màu xanh than in đậm nếu chưa có ảnh — tương tự 01-YCTC); nút **Ký xác nhận** đặt **ngay cạnh** nút
  **Gửi phê duyệt**; danh sách Trưởng phòng/tương đương.

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

## 12. Giao diện mẫu 04A-YCTK

- Mã yêu cầu, ca, lần, đơn vị, phòng, người yêu cầu, ĐTDĐ, ngày lập.
- Thời gian sử dụng từ bắt đầu đến kết thúc.
- Lý do yêu cầu.
- Ký tên bằng OTP, ký xác nhận, danh sách Trưởng phòng/tương đương, gửi phê duyệt.
- Chi tiết tài khoản:
  - Họ tên chủ tài khoản.
  - Loại tài khoản: Truy cập/Chỉnh sửa.
  - Hình thức: Cấp mới/Đổi thuộc tính.
  - Ký tại mục chi tiết nếu người lập và người truy cập không phải là một.

## 13. Giao diện mẫu 05A-YCKC

- Tên hệ thống, tên CSDL.
- Mã yêu cầu, ca, lần, đơn vị, phòng, người yêu cầu, ĐTDĐ, ngày lập.
- Thời gian sử dụng từ/đến, ràng buộc theo ca.
- Lý do yêu cầu.
- Ký tên bằng OTP, ký xác nhận, gửi phê duyệt/chuyển bộ phận mở.
- Quyền trên đối tượng dữ liệu:
  - QueryAll: nếu tích chọn thì các quyền sau chuyển `enable=false`.
  - Owner, tên bảng.
  - Select, Insert, Update, Delete.

## 14. Giao diện mẫu 05B-HTKC

- Tên hệ thống, tên CSDL.
- Mã yêu cầu, ca, lần, đơn vị, phòng, người yêu cầu, ĐTDĐ, ngày lập.
- Thời gian yêu cầu từ/đến, ràng buộc theo ca.
- Mục đích.
- Ký tên bằng OTP, ký xác nhận, gửi phê duyệt.
- Nội dung công việc đã thực hiện:
  - Chọn mã yêu cầu 05A-YCKC từ danh sách 05A chưa có 05B.
  - Owner, tên bảng.
  - Select, Insert, Update, Delete.
## 15. Giao diện mẫu 04B-BGTK

Màn hình riêng (`requests/handover.html`), truy cập tại `/requests/new/BGTK_04B`.

### Thông tin chung

| Trường | Loại | Mô tả |
|---|---|---|
| Mã yêu cầu | Tự động | Sinh khi Lưu tạm (theo đơn vị + phòng của người lập) |
| Thời gian | Tự động | Ngày giờ hiện tại của hệ thống |
| Thông tin yêu cầu 04A-YCTK | Chọn (nút lệnh) | Liệt kê các phiếu 04A-YCTK **chưa có** 04B-BGTK để liên kết (`sourceRequestId`) |
| Người bàn giao | Tự động | Người lập biên bản (DBA đăng nhập) |
| Lãnh đạo phòng phụ trách người bàn giao | Chọn | Danh sách lãnh đạo phòng (`handoverManagerId`) |
| Người nhận bàn giao | Chọn | Người nhận tài khoản bàn giao (`receiverUserId`) |
| Lãnh đạo phòng phụ trách người nhận bàn giao | Chọn | Danh sách lãnh đạo phòng (`receiverManagerId`) |
| Nội dung bàn giao | Nhập | Nội dung biên bản (`reason`) |
| Ký xác nhận | Nút lệnh | Mở nhập mã Google Authenticator để ký |
| Gửi danh sách người nhận liên quan | Nút lệnh | Thông báo cho chủ tài khoản/người lập 04A vào ký bàn giao |
| Gửi lãnh đạo phụ trách | Nút lệnh | Gửi biên bản vào luồng xử lý phê duyệt |

### Thông tin chi tiết (người bàn giao nhập)

| Trường | Loại | Mô tả |
|---|---|---|
| Tài khoản | Nhập | Tên tài khoản bàn giao (`objectName`) |
| Loại tài khoản | Chọn | Truy cập / Chỉnh sửa (`accountType`) |
| Phạm vi | Chọn | Toàn bộ / Theo hệ thống / Theo CSDL / Theo đối tượng (`scope`) |
| Nội dung | Nhập | Nội dung bàn giao của dòng (`purpose`) |
| Chủ tài khoản | Nhập | Họ tên chủ tài khoản (`accountOwnerName`) |

> Luồng ký (mục 5): DBA lập biên bản → ký → gửi lãnh đạo phòng phụ trách người bàn giao; sau đó chủ
> tài khoản và người lập 04A-YCTK vào ký xác nhận bàn giao → gửi lãnh đạo phòng phụ trách người nhận.

**Ghi chú triển khai hiện tại (đơn giản hóa):**
- Luồng phê duyệt định tuyến theo nhóm vai trò DEPT_MANAGER tại đơn vị người lập (chưa định tuyến
  trực tiếp theo lãnh đạo được chọn ở `handoverManagerId`/`receiverManagerId`).
- "Người nhận bàn giao" hiện là danh sách chọn (spec mô tả tự động theo phiếu 04A).
- "Gửi danh sách người nhận liên quan" hiện là nút placeholder (thông báo phía JS), chưa gửi email.
## 16. Allowed Files

- `src/main/java/.../request/**`
- `src/main/java/.../workflow/RequestSubmissionService.java`
- `src/main/resources/templates/requests/**`
- `src/main/resources/static/js/requests/**`
- `src/main/resources/static/css/requests/**`
- `src/test/java/.../request/**`

## 17. Must Not Change

- Không sửa màn hình Dashboard ngoài link/nút cần thiết.
- Không sửa service AD/Email/OTP ngoài interface đã thống nhất.
- Không sửa xử lý phê duyệt sau khi yêu cầu đã gửi, trừ phần khởi tạo bước đầu.

## 18. Verification

- Lưu nháp từng mẫu phiếu.
- Ký xác nhận thành công bằng OTP.
- Gửi phiếu 01/04A với nhiều dòng chi tiết và nhiều người ký.
- Chặn gửi nếu thiếu chữ ký người dùng chung phiếu.
- Chặn gửi nếu không có dòng chi tiết với 01/04A.
- Chặn lập phiếu mới nếu người dùng nợ 05B.
- Mẫu 05A sau gửi vào trạng thái “Đã chuyển bộ phận Mở truy cập”.
- File SQL 02-YCCS kiểm tra đúng định dạng tên.

## 19. Definition of Done

- Hoàn thành form cho 6 mẫu phiếu.
- Có lưu nháp, sửa nháp, ký, gửi, hủy, gửi lại.
- Có validate nghiệp vụ và validate giao diện.
- Có test cho các luồng chính và lỗi nghiệp vụ quan trọng.
