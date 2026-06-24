```markdown
# Feature: Chức năng lập và gửi yêu cầu

**Người phụ trách:** Tin  
**Mã hạng mục:** 2.3

---

## 1. Mục tiêu

Cho phép người lập yêu cầu đăng nhập, chọn mẫu phiếu, nhập thông tin, ký xác nhận, lưu nháp, gửi phê duyệt hoặc gửi bộ phận Mở truy cập đối với yêu cầu khẩn cấp. Đối với mẫu 04B-BGTK, cho phép DBA lập biên bản bàn giao tài khoản và gửi cho chủ tài khoản ký nhận.

## 2. Mẫu phiếu hỗ trợ

| Mẫu | Tên | Người lập |
|---|---|---|
| 01-YCTC | Truy cập, truy xuất CSDL thông thường | Người yêu cầu |
| 02-YCCS | Chỉnh sửa dữ liệu | Người yêu cầu |
| 03-YCCT | Thay đổi cấu trúc CSDL | Người yêu cầu |
| 04A-YCTK | Cấp mới/thay đổi thuộc tính tài khoản | Người yêu cầu |
| 04B-BGTK | Biên bản bàn giao tài khoản | DBA (Người quản trị CSDL) |
| 05A-YCKC | Truy cập khẩn cấp | Người yêu cầu |
| 05B-HTKC | Hoàn thành truy cập khẩn cấp | Người yêu cầu |

## 3. Luồng chung cho 02, 03, 05B

1. Người lập yêu cầu đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", chọn mẫu phiếu.
3. Nhập nội dung chi tiết cho từng mẫu phiếu.
4. Ký xác nhận (SoftOTP) gửi phê duyệt.
5. Hệ thống kiểm tra trường bắt buộc, sinh mã yêu cầu.
6. Hệ thống lưu hồ sơ ở trạng thái:
   - `Chờ phê duyệt`; hoặc
   - `Chờ kiểm tra` với mẫu có yêu cầu kiểm tra (02-YCCS).
7. Hệ thống gửi email đến Trưởng phòng/tương đương hoặc Bộ phận kiểm tra.

## 4. Luồng chung cho 01-YCTC, 04A-YCTK

1. Người lập đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", chọn mẫu phiếu.
3. Nhập nội dung chi tiết.
4. Người lập chọn một trong hai hành động:

── Nhánh A: Lưu phiếu ──
5. Hệ thống kiểm tra trường bắt buộc, sinh mã yêu cầu.
6. Hệ thống lưu hồ sơ ở trạng thái `Chờ ký xác nhận`.
7. Hệ thống gửi email thông báo đến người dùng liên quan (người sử dụng chung phiếu) để ký xác nhận.
8. Người dùng có liên quan đăng nhập bằng tài khoản AD.
9. Chọn phiếu đang `Chờ ký xác nhận`.
10. Kiểm tra/chỉnh sửa thông tin (chỉ phần của mình).
11. Người dùng ký xác nhận (SoftOTP).
12. Người lập phiếu ký xác nhận (SoftOTP), gửi phê duyệt.
13. Hệ thống kiểm tra trường bắt buộc.
14. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
15. Hệ thống gửi email đến Trưởng phòng/tương đương.

── Nhánh B: Ký xác nhận & Gửi ──
5. Hệ thống kiểm tra trường bắt buộc, sinh mã yêu cầu.
6. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
7. Hệ thống gửi email đến Trưởng phòng/tương đương.

## 5. Luồng riêng cho 05A-YCKC

1. Người yêu cầu đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", mẫu 05A-YCKC.
3. Nhập nội dung chi tiết.
4. Người lập ký xác nhận (SoftOTP).
5. Hệ thống kiểm tra trường bắt buộc, sinh mã yêu cầu.
6. Hệ thống lưu hồ sơ ở trạng thái `Đã chuyển bộ phận Mở truy cập`.
7. Hệ thống gửi email đến Bộ phận Mở truy cập.

> **Ghi chú:** Bước xác nhận đồng ý của người có thẩm quyền (Đơn vị yêu cầu + Đơn vị chủ quản ứng dụng) được thực hiện **thủ công** ngoài hệ thống (qua điện thoại, tin nhắn). Trên giao diện web, 2 ô xác nhận này **để trống**, không yêu cầu nhập. Người lập yêu cầu và người mở truy cập tự liên hệ cấp có thẩm quyền để xác nhận.

## 6. Luồng riêng cho 04B-BGTK

1. DBA đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", mẫu 04B-BGTK.
3. Hệ thống hiển thị danh sách mẫu 04A-YCTK đã được phê duyệt và hoàn thành cấp tài khoản nhưng **chưa có 04B tương ứng**.
4. DBA chọn 1 phiếu 04A để liên kết.
5. Hệ thống tự nạp thông tin từ 04A (Tên CSDL, danh sách chủ tài khoản, Loại tài khoản, Hình thức).
6. DBA nhập thêm **UserID** cho từng chủ tài khoản.
7. DBA ký xác nhận (SoftOTP) → hệ thống tự fill ô "Người bàn giao" = thông tin DBA đang đăng nhập.
8. Hệ thống lưu hồ sơ ở trạng thái `Chờ ký nhận`.
9. Hệ thống gửi email thông báo đến **tất cả chủ tài khoản** (lấy từ 04A) để ký nhận.
10. Mỗi chủ tài khoản đăng nhập, ký nhận (SoftOTP) riêng **dòng của mình**.
11. Khi người ký cuối cùng ký xong → hệ thống tự động fill ô "Người nhận bàn giao" = thông tin người dùng ký cuối cùng.
12. Hệ thống chuyển trạng thái sang `Đã chuyển cấp quản lý` → tự động chuyển module phê duyệt (ngoài scope).

## 7. Trạng thái phiếu

### 7.1. Trạng thái chung (01, 02, 03, 04A, 05A, 05B)

| Trạng thái | Mô tả |
|---|---|
| `Nháp` | Phiếu đã lưu nhưng chưa gửi |
| `Chờ ký xác nhận` | Phiếu 01/04A chờ người dùng liên quan ký |
| `Chờ kiểm tra` | Phiếu 02 đã gửi Bộ phận kiểm tra |
| `Chờ phê duyệt` | Đã gửi lãnh đạo phê duyệt |
| `Đã chuyển bộ phận Mở truy cập` | Riêng 05A, đã gửi bộ phận Mở truy cập |
| `Bị từ chối` | Lãnh đạo/Bộ phận kiểm tra từ chối |
| `Đã hủy` | Người lập hủy phiếu |
| `Gửi lỗi` | Gửi thất bại do mạng truyền thông, cho phép gửi lại |

### 7.2. Trạng thái riêng mẫu 04B-BGTK

| Trạng thái | Mô tả |
|---|---|
| `Nháp` | DBA đã lưu nhưng chưa gửi |
| `Chờ ký nhận` | Đã gửi, chờ chủ tài khoản ký nhận |
| `Đã chuyển cấp quản lý` | Tất cả chủ TK đã ký, tự động chuyển module phê duyệt |
| `Gửi lỗi` | Gửi thất bại do mạng |

> **Ghi chú:** Trạng thái `Bị từ chối` được nhận từ module phê duyệt (module khác quản lý). Hệ thống nhận trạng thái từ module phê duyệt thông qua callback/event.

## 8. Quy tắc nghiệp vụ chung

- Khi lập yêu cầu, hệ thống ràng buộc danh mục CSDL, người dùng với đơn vị chủ quản ứng dụng; chỉ cho phép chọn danh mục hợp lệ.
- Cho phép lưu nháp và sửa lại phiếu nếu chưa gửi phê duyệt.
- Sau khi gửi phê duyệt không được sửa nội dung.
- Tất cả chữ ký trên hệ thống đều sử dụng **SoftOTP**.

### 8.1. Quy tắc chặn nợ phiếu 05B-HTKC

- Chặn không cho phép người sử dụng đang nợ phiếu 05B-HTKC lập phiếu yêu cầu mới (01-YCTC, 05A-YCKC).
- Chặn luôn trường hợp người khác lập phiếu 01-YCTC mà trong danh sách chi tiết có người dùng đang nợ phiếu 05B.
- Khi thêm người dùng đang nợ 05B vào danh sách chi tiết phiếu 01-YCTC → **chặn ngay** tại thời điểm thêm (inline validation), không cho thêm vào danh sách.
- **Deadline:** 3 ngày làm việc tính từ thời điểm Bộ phận Mở truy cập điền và ký xác nhận mở quyền (do module khác quản lý, hệ thống nhận event từ module đó).
- **Cảnh báo:** Hiển thị Toast notification khi đăng nhập + Banner trên trang chủ, **chỉ đối với user đang nợ phiếu**.
- **Nội dung cảnh báo:** *"Bạn đang nợ phiếu 05B-HTKC cho yêu cầu [Mã 05A]. Hạn hoàn thành: [dd/MM/yyyy]. Vui lòng hoàn thành để tiếp tục lập yêu cầu mới."*

### 8.2. Quy tắc hủy yêu cầu

- Chỉ được hủy khi phiếu đang ở trạng thái: `Nháp`, `Chờ ký xác nhận`, `Chờ kiểm tra`.
- **Không được hủy** khi phiếu ở trạng thái `Chờ phê duyệt`.
- Khi hủy, **bắt buộc nhập lý do hủy**.
- Phiếu bị từ chối: người lập không thể thao tác gì thêm trên phiếu đó (chỉ xem lại). Phải lập phiếu mới.

### 8.3. Quy tắc gửi lại

- Chỉ gửi lại trong trường hợp **gửi lỗi do mạng truyền thông** (trạng thái `Gửi lỗi`).
- Trường hợp bị từ chối duyệt → hủy phiếu luôn, phải lập phiếu mới.
- **Cơ chế retry tự động:** Khi gửi thất bại, hệ thống tự động retry **3 lần**, mỗi lần cách nhau **5 giây**. Sau 3 lần retry vẫn thất bại → chuyển trạng thái `Gửi lỗi`, hiển thị nút "Gửi lại" cho người dùng thao tác thủ công.

### 8.4. Quy tắc mã yêu cầu

- Format: `KýhiệuĐV_DDMMYYYY_Ca_Lần`
- **Ca** và **Lần** được sinh tự động nhưng **cho phép người dùng thay đổi**.
- Chỉ cho phép sửa phần **Ca** và **Lần**, không cho sửa ký hiệu đơn vị và ngày tháng năm.
- Sau khi người dùng sửa, hệ thống **validate tính duy nhất ngay lập tức** (inline validation). Nếu mã bị trùng → hiển thị lỗi ngay để người dùng sửa.
- Định dạng NgàyThángNăm: DDMMYYYY.
- Ca, Lần theo định dạng số: 1, 2, 3...
- Lần: Lần thứ mấy về việc lập yêu cầu truy cập trong ca.

### 8.5. Quy tắc Ca và thời gian

- Ca 1: 00:00 – 08:00
- Ca 2: 08:00 – 20:00
- Ca 3: 20:00 – 24:00
- Khi người dùng chọn Ca, hệ thống **tự động fill thời gian Từ-Đến** theo khung giờ của ca đã chọn.
- Người dùng có thể điều chỉnh thời gian trong phạm vi khung ca (không được vượt ngoài khung ca).

## 9. Quy tắc riêng mẫu 01-YCTC

- Người dùng có thể yêu cầu truy cập **nhiều CSDL** trên một phiếu và chỉ cần ký một lần.
- Mỗi yêu cầu có thể dùng cho một hoặc nhiều người.
- Phải chọn **Loại yêu cầu: Truy vấn hoặc Chỉnh sửa** (hiển thị trên tiêu đề phiếu: "Yêu cầu truy cập, truy xuất [truy vấn|chỉnh sửa] Cơ sở dữ liệu").
- Trường hợp yêu cầu quyền vấn tin trên các bảng hoặc đối tượng đặc biệt, hạn chế truy cập, cần ghi rõ: Thông tin bảng, lý do yêu cầu.
- Mỗi người dùng chỉ cần ký xác nhận một lần trên phần thông tin chung hoặc phần thông tin chi tiết.
- Hệ thống tự động điền chữ ký cho phần thông tin chung và các dòng chi tiết liên quan.
- Người yêu cầu và những người sử dụng chung phiếu phải ký xác nhận trước khi gửi lãnh đạo phê duyệt.
- Phải có tối thiểu một dòng tại phần danh sách yêu cầu chi tiết.
- Trường nội dung nào mà cán bộ đã ký xác nhận thì không được sửa lại nội dung.

## 10. Quy tắc riêng mẫu 02-YCCS

- Người kiểm tra thay đổi dữ liệu và người thực hiện thay đổi dữ liệu phải thuộc Đơn vị chủ quản ứng dụng.
- Tên tệp SQL phải đúng định dạng `YYYYMMDD_BS_XXX.sql`; hệ thống kiểm tra đúng định dạng mới cho tải.
- Nếu nhiều file cần gộp thành một.
- Mã kiểm tra tính toàn vẹn (checksum) bắt buộc.

## 11. Quy tắc riêng mẫu 03-YCCT

- Có 3 tab chi tiết: Tạo mới, Thay đổi, Xóa.
- Có phần nội dung DBA ghi để đánh giá tác động ảnh hưởng và hệ thống liên quan.
- Cho phép tải SQL Script và nhập mã kiểm tra.
- Đối với Yêu cầu có liên quan đến nhiều Hệ thống thông tin thì phải có đầy đủ phê duyệt của người có thẩm quyền tại Đơn vị chủ quản ứng dụng phát sinh yêu cầu, cũng như tại các Đơn vị chủ quản ứng dụng có liên quan bị ảnh hưởng.

## 12. Quy tắc riêng mẫu 04A-YCTK

- Mỗi phiếu yêu cầu chỉ sử dụng trên **01 CSDL duy nhất**.
- Có thể có **nhiều người dùng** (nhiều tài khoản) trên cùng 1 phiếu.
- Trường hợp Đơn vị yêu cầu là Đơn vị chủ quản ứng dụng thì chỉ cần xin xác nhận ở ô Đơn vị chủ quản ứng dụng.
- Mỗi người dùng chỉ cần ký xác nhận một lần.
- Hệ thống tự động điền chữ ký cho phần thông tin chung và các dòng chi tiết liên quan.
- Người yêu cầu và những người sử dụng chung phiếu phải ký xác nhận trước khi gửi lãnh đạo phê duyệt.
- Phải có tối thiểu một dòng tại phần danh sách yêu cầu chi tiết.
- Trường nội dung nào mà cán bộ đã ký xác nhận thì không được sửa lại nội dung.

## 13. Quy tắc riêng mẫu 04B-BGTK

- DBA lập phiếu 04B sau khi cấp tài khoản thành công.
- Phiếu 04B liên kết với phiếu 04A-YCTK tương ứng (chọn từ danh sách 04A chưa có 04B).
- Hệ thống tự nạp thông tin từ 04A: Tên CSDL, danh sách chủ tài khoản, Loại tài khoản, Hình thức (Cấp mới/Đổi thuộc tính).
- DBA chỉ cần nhập thêm **UserID** cho từng chủ tài khoản.
- Trường "Phạm vi" = Tên CSDL (tự động lấy từ 04A).
- Trường "Nội dung" = Cấp mới/Đổi thuộc tính (tự động lấy từ trường "Hình thức" trong 04A).
- Ô "Người bàn giao" tự fill = thông tin DBA khi DBA ký xác nhận.
- Ô "Người nhận bàn giao" tự fill = thông tin người dùng ký cuối cùng (khi tất cả chủ TK đã ký).
- Mỗi chủ tài khoản ký nhận riêng dòng của mình.
- Sau khi tất cả chủ TK ký xong → hệ thống tự chuyển lên cấp quản lý (ngoài scope).

## 14. Quy tắc riêng mẫu 05A-YCKC

- Mỗi phiếu yêu cầu truy cập, truy xuất CSDL khẩn cấp chỉ sử dụng trên **01 CSDL duy nhất**.
- Trong một ca yêu cầu truy cập CSDL khẩn cấp, người yêu cầu có thể tạo **nhiều Yêu cầu 05A-YCKC** để xử lý công việc, ghi rõ số lần yêu cầu trong ca.
- Trường hợp người yêu cầu chỉ cần quyền truy vấn toàn bộ dữ liệu được phép thì tích vào ô "Query all data only" → các quyền chi tiết (Select, Insert, Update, Delete) chuyển `enable=false`.
- Nếu người yêu cầu muốn truy vấn dữ liệu các bảng đặc biệt thì cần ghi rõ tên bảng trong yêu cầu.

## 15. Quy tắc riêng mẫu 05B-HTKC

- Mỗi phiếu hoàn thành chỉ sử dụng trên **01 CSDL duy nhất**.
- **1 file 05B-HTKC dùng để xác nhận cho nhiều yêu cầu 05A-YCKC** nhưng chỉ trong trường hợp các 05A này **chung 1 ca**.
- Khi người lập chọn mẫu 05B-HTKC, hệ thống hiển thị danh sách **Ngày + Ca** mà người dùng có phát sinh 05A chưa có 05B tương ứng.
- Người lập chọn 1 dòng (Ngày + Ca) → hệ thống **tự nạp tất cả nội dung** của các mẫu 05A trong ngày + ca đó vào phiếu 05B.
- Các giá trị "Lần" tự sinh từ các mẫu 05A-YCKC đã phát sinh trong ca đó.
- Dữ liệu từ 05A là **read-only**, người lập chỉ bổ sung phần "Mục đích/câu lệnh đã thực hiện".
- Trường "Mục đích truy cập, truy xuất (mô tả chi tiết, câu lệnh đã thực hiện)": **1 ô chung** cho tất cả các lần 05A.
- Với mỗi ca truy cập, truy xuất CSDL khẩn cấp phải hoàn thành một (01) Mẫu 05B-HTKC tương ứng.

## 16. Scope và ranh giới module

- **Trong scope:** Khởi tạo yêu cầu, lưu nháp, ký xác nhận (SoftOTP), gửi phê duyệt/gửi bộ phận Mở truy cập/gửi chủ TK ký nhận (04B). Dừng ở bước gửi thành công.
- **Ngoài scope:**
  - Luồng phê duyệt 3 bên (04A-YCTK): chỉ khởi tạo yêu cầu rồi chuyển sang module phê duyệt.
  - Bước kiểm tra script (02-YCCS): chỉ dừng ở bước gửi đến Bộ phận kiểm tra.
  - Xác nhận mở quyền (05A): do Bộ phận Mở truy cập quản lý ở module khác.
  - Phần ký đại diện cấp quản lý 2 bên (04B): chuyển module phê duyệt sau khi tất cả chủ TK ký xong.
  - Phần "Người kiểm tra" trên mẫu 05B: ngoài scope.
  - Phần "Người thực hiện thay đổi dữ liệu" trên mẫu 02: ngoài scope.
  - Nhận trạng thái `Bị từ chối` từ module phê duyệt (qua callback/event).
  - Nhận event "hoàn thành mở quyền" từ module Mở truy cập để tính deadline 3 ngày nợ 05B.
  - Nhận event "hoàn thành cấp tài khoản" từ module DBA để DBA lập 04B.
- **Hiển thị trên form nhưng không xử lý:**
  - Các ô ký phê duyệt trên form: **KHÔNG hiển thị** trên form lập yêu cầu. Giao toàn bộ cho module phê duyệt.

## 17. Giao diện mẫu 01-YCTC

### Thông tin chung

| Trường | Loại | Mô tả |
|---|---|---|
| Loại yêu cầu | Chọn | **Truy vấn** hoặc **Chỉnh sửa** (hiển thị trên tiêu đề phiếu) |
| Mã yêu cầu | Tự động | `KýhiệuĐV_DDMMYYYY` |
| Ca | Tự động + Cho phép sửa | 1, 2, 3 – Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Lần | Tự động + Cho phép sửa | 1, 2, 3... – Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại (dd/MM/yyyy) |
| Thời gian truy cập/truy xuất | Tự động fill theo Ca | Từ - Đến (dd/MM/yyyy HH:mm), tự fill khi chọn Ca, cho phép điều chỉnh trong phạm vi khung ca |
| Mục đích/Lý do | Nhập | Bắt buộc |
| Ký tên | SoftOTP | Sau khi ký thành công hiển thị ảnh chữ ký |
| Danh sách Trưởng phòng/tương đương | Tự động | Lấy theo người dùng |
| Lưu phiếu | Nút lệnh | Lưu nháp hoặc chuyển Chờ ký xác nhận |
| Ký xác nhận & Gửi | Nút lệnh | Ký SoftOTP + Gửi phê duyệt |

### Thông tin chi tiết (Danh sách yêu cầu)

| Trường | Loại | Mô tả |
|---|---|---|
| STT | Tự động | Số thứ tự |
| Hệ thống thông tin | Chọn | Theo danh mục hợp lệ |
| Đơn vị chủ quản ứng dụng | Tự động | Tự fill theo Hệ thống thông tin đã chọn |
| CSDL | Chọn | Theo hệ thống/đơn vị chủ quản (cho phép nhiều CSDL trên 1 phiếu) |
| Tên đối tượng | Nhập | Bảng/đối tượng dữ liệu |
| Quyền truy cập | Chọn | Theo danh mục quyền |
| Họ và tên | Chọn | Người sử dụng (**validate nợ 05B ngay khi thêm**) |
| Ký tại mục chi tiết | SoftOTP | Nếu người lập và người truy cập là một thì không cần ký tại mục chi tiết |

> **Lưu ý:** Người truy cập trong danh sách không có đủ và đúng thông tin thì không thực hiện cấp quyền truy cập.

## 18. Giao diện mẫu 02-YCCS

| Trường | Loại | Mô tả |
|---|---|---|
| Tên hệ thống | Chọn | Theo danh mục |
| Tên cơ sở dữ liệu | Chọn | Theo hệ thống |
| Mã yêu cầu | Tự động | `KýhiệuĐV_DDMMYYYY` |
| Ca | Tự động + Cho phép sửa | 1, 2, 3 – Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Lần | Tự động + Cho phép sửa | 1, 2, 3... – Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại (dd/MM/yyyy) |
| Thời gian cập nhật | Tự động fill theo Ca | Từ - Đến (dd/MM/yyyy HH24), tự fill khi chọn Ca |
| Nội dung chỉnh sửa dữ liệu | Nhập | Bắt buộc |
| Tên tệp cần chạy | Tải file | Quy tắc `YYYYMMDD_BS_XXX.sql`; hệ thống kiểm tra đúng định dạng mới cho tải; nếu nhiều file cần gộp thành một |
| Mã kiểm tra tính toàn vẹn | Nhập | Checksum – bắt buộc |
| Ký tên | SoftOTP | Hiển thị ảnh chữ ký sau khi ký |
| Danh sách Người kiểm tra | Tự động | Lấy theo tên hệ thống (thuộc Đơn vị chủ quản ứng dụng) |
| Gửi | Nút lệnh | Gửi Bộ phận kiểm tra (trạng thái → `Chờ kiểm tra`) |

> **Lưu ý:** Người kiểm tra thay đổi dữ liệu và người thực hiện thay đổi dữ liệu phải thuộc Đơn vị chủ quản ứng dụng.

## 19. Giao diện mẫu 03-YCCT

### Thông tin chung

| Trường | Loại | Mô tả |
|---|---|---|
| Tên hệ thống | Chọn | Theo danh mục |
| Tên cơ sở dữ liệu | Chọn | Theo hệ thống |
| Mã yêu cầu | Tự động | `KýhiệuĐV_DDMMYYYY` |
| Ca | Tự động + Cho phép sửa | Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Lần | Tự động + Cho phép sửa | Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại (dd/MM/yyyy) |
| Ngày thực hiện dự kiến | Nhập | dd/MM/yyyy |
| Đánh giá tác động ảnh hưởng | Hiển thị (read-only) | Phần nội dung DBA ghi: Hiệu năng, Các thành phần/hệ thống liên quan, Đánh giá khác |
| Loại yêu cầu | Tab | Tạo mới / Thay đổi / Xóa |
| Ký tên | SoftOTP | Hiển thị ảnh chữ ký sau khi ký |
| Danh sách Trưởng phòng/tương đương | Tự động | Lấy theo người dùng |
| Gửi phê duyệt | Nút lệnh | Gửi luồng xử lý |

### Tab Tạo mới / Xóa

**Table:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Sở hữu bảng (Table Owner) | Owner |
| Tên bảng (Table name) | Tên bảng |
| Dự kiến tăng trưởng | Dung lượng dự kiến |
| Vòng đời lưu trữ dữ liệu tại CSDL | Thời gian lưu trữ |
| Cột xác định vòng đời | Cột dùng xác định vòng đời |
| Đối tượng phụ thuộc table cần tạo | Các đối tượng phụ thuộc |

**Cấu trúc table:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Tên bảng | Tên bảng |
| Tên cột | Tên cột |
| Kiểu dữ liệu | Kiểu DL |
| Cho phép Null | Y/N |
| Giá trị mặc định | Default value |
| Mô tả | Mô tả cột |

**Index:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Sở hữu (Owner) | Owner |
| Tên Index | Tên index |
| Sở hữu bảng (Table owner) | Table owner |
| Tên bảng | Tên bảng |
| Danh sách cột được đánh chỉ mục | Các cột index |

**Synonym:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Tên Synonym | Tên |
| Kiểu | Public/Private |
| Sở hữu bảng (Table owner) | Table owner |
| Tên bảng | Tên bảng |
| Mô tả | Mô tả |

**Tạo mới/Xóa khác:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Sở hữu (Owner) | Owner |
| Tên | Tên đối tượng |
| Kiểu | Kiểu đối tượng |
| Mô tả | Mô tả |

**SQL Script:**

| Trường | Loại | Mô tả |
|---|---|---|
| File SQL Script | Tải file | File DDL đính kèm |
| Mã kiểm tra (checksum) | Nhập | Bắt buộc |
| Tên file | Tự động | Hiển thị tên file đã tải |

### Tab Thay đổi

**Thêm cột bảng:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Sở hữu (Owner) | Owner |
| Tên bảng | Tên bảng |
| Tên cột | Tên cột mới |
| Loại dữ liệu | Kiểu DL |
| Mô tả | Mô tả |

**Sửa cột bảng:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Sở hữu (Owner) | Owner |
| Tên bảng | Tên bảng |
| Tên cột | Tên cột cần sửa |
| Giá trị cũ cần thay đổi | Giá trị hiện tại |
| Giá trị mới | Giá trị mới |
| Mô tả | Mô tả |

**Tạo lại index:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Sở hữu (Owner) | Owner |
| Tên bảng | Tên bảng |
| Tên cũ Index | Index hiện tại |
| Cột trong index | Cột hiện tại |
| Index mới | Tên index mới |
| Cột được đánh index mới | Cột mới |

**Thay đổi khác:**

| Trường | Mô tả |
|---|---|
| STT | Số thứ tự |
| Sở hữu (Owner) | Owner |
| Tên | Tên đối tượng |
| Kiểu | Kiểu đối tượng |
| Mô tả | Mô tả |

**SQL Script:** (giống Tab Tạo mới)

## 20. Giao diện mẫu 04A-YCTK

### Thông tin chung

| Trường | Loại | Mô tả |
|---|---|---|
| Tên hệ thống | Chọn | Theo danh mục |
| Tên cơ sở dữ liệu | Chọn | Theo hệ thống (**chỉ 1 CSDL duy nhất trên 1 phiếu**) |
| Mã yêu cầu | Tự động | `KýhiệuĐV_DDMMYYYY` |
| Ca | Tự động + Cho phép sửa | Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Lần | Tự động + Cho phép sửa | Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại (dd/MM/yyyy) |
| Thời gian sử dụng | Nhập | Bắt đầu - Kết thúc (dd/MM/yyyy HH24) |
| Lý do yêu cầu [tạo mới\|thay đổi thuộc tính] tài khoản | Nhập | Bắt buộc |
| Ký tên | SoftOTP | Hiển thị ảnh chữ ký sau khi ký |
| Danh sách Trưởng phòng/tương đương | Tự động | Lấy theo người dùng |
| Lưu phiếu | Nút lệnh | Lưu nháp hoặc chuyển Chờ ký xác nhận |
| Ký xác nhận & Gửi | Nút lệnh | Ký SoftOTP + Gửi phê duyệt (chuyển module phê duyệt 3 bên) |

### Thông tin chi tiết về tài khoản

| Trường | Loại | Mô tả |
|---|---|---|
| STT | Tự động | Số thứ tự |
| Họ tên chủ tài khoản | Chọn/Nhập | Người sử dụng (có thể nhiều người trên 1 phiếu) |
| Loại tài khoản | Chọn | Truy vấn (Query) / Chỉnh sửa (Update) |
| Hình thức | Chọn | Cấp mới / Đổi thuộc tính |
| Ký tại mục chi tiết | SoftOTP | Nếu người lập và người truy cập không phải là một |

## 21. Giao diện mẫu 04B-BGTK

### Thông tin chung

| Trường | Loại | Mô tả |
|---|---|---|
| Mã yêu cầu 04A liên kết | Chọn | Danh sách 04A đã phê duyệt + hoàn thành cấp TK, chưa có 04B |
| Tên hệ thống | Tự động | Lấy từ 04A đã chọn |
| Tên cơ sở dữ liệu | Tự động | Lấy từ 04A đã chọn |
| Ngày bàn giao | Tự động | Ngày hiện tại (dd/MM/yyyy) |
| Người bàn giao | Tự động | Fill = DBA đang đăng nhập (sau khi ký SoftOTP) |
| Người nhận bàn giao | Tự động | Fill = thông tin người dùng ký cuối cùng |
| Ký tên (DBA) | SoftOTP | DBA ký xác nhận bàn giao | Hiển thị ảnh chữ ký sau khi ký |
| Gửi ký nhận | Nút lệnh | Gửi cho chủ tài khoản ký nhận |

### Thông tin chi tiết (tự nạp từ 04A)

| Trường | Loại | Mô tả |
|---|---|---|
| STT | Tự động | Số thứ tự |
| Tài khoản (UserID) | Nhập (DBA) | DBA nhập UserID đã cấp |
| Loại tài khoản | Tự động | QUERY/UPDATE – lấy từ 04A |
| Phạm vi | Tự động | Tên CSDL – lấy từ 04A |
| Nội dung | Tự động | Cấp mới/Đổi thuộc tính – lấy từ trường "Hình thức" trong 04A |
| Chủ tài khoản | Tự động | Họ tên – lấy từ 04A |
| Ký tên (Chủ TK) | SoftOTP | Mỗi chủ TK ký nhận riêng dòng của mình | Hiển thị ảnh chữ ký sau khi ký |

## 22. Giao diện mẫu 05A-YCKC

### Thông tin chung

| Trường | Loại | Mô tả |
|---|---|---|
| Tên hệ thống | Chọn | Theo danh mục |
| Tên cơ sở dữ liệu | Chọn | Theo hệ thống (**chỉ 1 CSDL duy nhất trên 1 phiếu**) |
| Xác nhận đồng ý cho phép | Hiển thị (để trống) | Đơn vị yêu cầu + Đơn vị chủ quản ứng dụng – **để trống**, xác nhận thủ công ngoài hệ thống |
| Mã yêu cầu | Tự động | `KýhiệuĐV_DDMMYYYY` |
| Ca | Tự động + Cho phép sửa | Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Lần | Tự động + Cho phép sửa | Sinh tự động, cho phép thay đổi, validate trùng ngay |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại (dd/MM/yyyy) |
| Thời gian yêu cầu | Tự động fill theo Ca | Từ - Đến (dd/MM/yyyy HH24:mm), tự fill khi chọn Ca |
| Mục đích/Lý do yêu cầu | Nhập | Bắt buộc |
| Ký tên | SoftOTP | Hiển thị ảnh chữ ký sau khi ký |
| Gửi chuyển bộ phận Mở truy cập | Nút lệnh | Gửi (trạng thái → `Đã chuyển bộ phận Mở truy cập`) |

### Quyền trên đối tượng dữ liệu

| Trường | Loại | Mô tả |
|---|---|---|
| Query all data only | Checkbox | Nếu tích chọn → các quyền chi tiết bên dưới chuyển `enable=false` |
| STT | Tự động | Số thứ tự |
| Sở hữu (Owner) | Nhập | Owner |
| Tên bảng (table name) | Nhập | Tên bảng |
| Select | Checkbox | Quyền Select |
| Insert | Checkbox | Quyền Insert |
| Update | Checkbox | Quyền Update |
| Delete | Checkbox | Quyền Delete |

## 23. Giao diện mẫu 05B-HTKC

### Bước chọn Ngày + Ca

| Trường | Loại | Mô tả |
|---|---|---|
| Danh sách Ngày + Ca | Chọn | Hệ thống hiển thị các Ngày + Ca mà người dùng có phát sinh 05A chưa có 05B. Người lập chọn 1 dòng (Ngày + Ca) |

> Sau khi chọn, hệ thống **tự nạp tất cả nội dung** của các mẫu 05A trong ngày + ca đó vào phiếu 05B.

### Thông tin chung (tự nạp từ 05A – read-only trừ khi ghi chú khác)

| Trường | Loại | Mô tả |
|---|---|---|
| Tên hệ thống | Tự động (read-only) | Lấy từ 05A |
| Tên cơ sở dữ liệu | Tự động (read-only) | Lấy từ 05A |
| Mã yêu cầu | Tự động | `KýhiệuĐV_DDMMYYYY` – sinh cho phiếu 05B |
| Ca | Tự động (read-only) | Lấy từ Ca đã chọn ở bước trên |
| Lần | Tự động (read-only, nhiều giá trị) | Tự sinh từ các mẫu 05A đã phát sinh trong ca. Hiển thị: Lần 1, Lần 2... |
| Tên đơn vị yêu cầu | Tự động | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | Ngày hiện tại (dd/MM/yyyy) |
| Thời gian yêu cầu | Tự động (read-only) | Từ - Đến, lấy từ các 05A liên quan |
| Mục đích truy cập, truy xuất | Nhập (textarea) | Bắt buộc – **mô tả chi tiết, bao gồm câu lệnh đã thực hiện**. 1 ô chung cho tất cả các lần 05A |
| Ký tên | SoftOTP | Hiển thị ảnh chữ ký sau khi ký |
| Gửi phê duyệt | Nút lệnh | Gửi luồng xử lý |

### Danh sách các bảng đã yêu cầu (tự nạp từ 05A – read-only)

| Trường | Loại | Mô tả |
|---|---|---|
| Lần | Tự động | Lần tương ứng với 05A nào |
| STT | Tự động | Số thứ tự |
| Sở hữu (Owner) | Tự động (read-only) | Lấy từ 05A |
| Tên bảng (table name) | Tự động (read-only) | Lấy từ 05A |
| Select | Tự động (read-only) | Lấy từ 05A |
| Insert | Tự động (read-only) | Lấy từ 05A |
| Update | Tự động (read-only) | Lấy từ 05A |
| Delete | Tự động (read-only) | Lấy từ 05A |

## 24. Email Notification

### 24.1. Email gửi Trưởng phòng/tương đương (phiếu 01, 03, 04A, 05B)

- **Trigger:** Khi phiếu được gửi phê duyệt thành công (trạng thái → `Chờ phê duyệt`).
- **Người nhận:** Trưởng phòng/tương đương của đơn vị yêu cầu.
- **Subject:** `[Agribank - Truy cập CSDL] Yêu cầu phê duyệt phiếu {MãMẫuPhiếu} - {MãYêuCầu}`
- **Body:**

Kính gửi Anh/Chị {Tên Trưởng phòng},

Hệ thống Truy cập CSDL thông báo có yêu cầu mới cần phê duyệt:

    Mã yêu cầu: {MãYêuCầu}
    Loại phiếu: {TênMẫuPhiếu}
    Người lập: {HọTênNgườiLập}
    Đơn vị: {TênĐơnVị}
    Phòng: {TênPhòng}
    Ngày lập: {dd/MM/yyyy}

Vui lòng đăng nhập hệ thống để xem chi tiết và phê duyệt.

Link: {URL_phiếu_yêu_cầu}

Trân trọng, Hệ thống Truy cập CSDL - Agribank


### 24.2. Email gửi Bộ phận kiểm tra (phiếu 02-YCCS)

- **Trigger:** Khi phiếu 02 được gửi thành công (trạng thái → `Chờ kiểm tra`).
- **Người nhận:** Người kiểm tra của đơn vị chủ quản ứng dụng (lấy theo tên hệ thống).
- **Subject:** `[Agribank - Truy cập CSDL] Yêu cầu kiểm tra phiếu 02-YCCS - {MãYêuCầu}`
- **Body:**

Kính gửi Anh/Chị {Tên Người kiểm tra},

Hệ thống Truy cập CSDL thông báo có yêu cầu chỉnh sửa dữ liệu cần kiểm tra:

    Mã yêu cầu: {MãYêuCầu}
    Người lập: {HọTênNgườiLập}
    Đơn vị: {TênĐơnVị}
    Hệ thống: {TênHệThống}
    CSDL: {TênCSDL}
    Ngày lập: {dd/MM/yyyy}
    Tên tệp SQL: {TênTệp}

Vui lòng đăng nhập hệ thống để kiểm tra SQL Script và xác nhận.

Link: {URL_phiếu_yêu_cầu}

Trân trọng, Hệ thống Truy cập CSDL - Agribank

### 24.3. Email gửi Bộ phận Mở truy cập (phiếu 05A-YCKC)

- **Trigger:** Khi phiếu 05A được gửi thành công (trạng thái → `Đã chuyển bộ phận Mở truy cập`).
- **Người nhận:** Bộ phận Mở truy cập.
- **Subject:** `[Agribank - Truy cập CSDL] Yêu cầu khẩn cấp mở truy cập - {MãYêuCầu}`
- **Body:**

Kính gửi Bộ phận Mở truy cập,

Hệ thống Truy cập CSDL thông báo có yêu cầu truy cập KHẨN CẤP cần xử lý:

    Mã yêu cầu: {MãYêuCầu}
    Người yêu cầu: {HọTênNgườiLập}
    Đơn vị: {TênĐơnVị}
    Hệ thống: {TênHệThống}
    CSDL: {TênCSDL}
    Thời gian sử dụng: {TừGiờ} - {ĐếnGiờ} ngày {dd/MM/yyyy}
    Lý do: {LýDo}

Vui lòng đăng nhập hệ thống để xử lý mở quyền truy cập.

Link: {URL_phiếu_yêu_cầu}

Trân trọng, Hệ thống Truy cập CSDL - Agribank


### 24.4. Email thông báo người dùng liên quan ký xác nhận (phiếu 01-YCTC, 04A-YCTK)

- **Trigger:** Khi phiếu 01/04A được lưu ở trạng thái `Chờ ký xác nhận` (Nhánh A).
- **Người nhận:** Người dùng liên quan (người sử dụng chung phiếu) cần ký xác nhận.
- **Subject:** `[Agribank - Truy cập CSDL] Yêu cầu ký xác nhận phiếu {MãMẫuPhiếu} - {MãYêuCầu}`
- **Body:**

Kính gửi Anh/Chị {Tên người dùng liên quan},

Anh/Chị {HọTênNgườiLập} đã lập phiếu yêu cầu truy cập CSDL có liên quan đến Anh/Chị. Vui lòng đăng nhập hệ thống để kiểm tra thông tin và ký xác nhận.

    Mã yêu cầu: {MãYêuCầu}
    Loại phiếu: {TênMẫuPhiếu}
    Người lập: {HọTênNgườiLập}
    Đơn vị: {TênĐơnVị}
    Ngày lập: {dd/MM/yyyy}

Link: {URL_phiếu_yêu_cầu}

Trân trọng, Hệ thống Truy cập CSDL - Agribank


### 24.5. Email gửi chủ tài khoản ký nhận (phiếu 04B-BGTK)

- **Trigger:** Khi DBA ký xác nhận và gửi phiếu 04B thành công (trạng thái → `Chờ ký nhận`).
- **Người nhận:** Tất cả chủ tài khoản (lấy từ 04A liên kết).
- **Subject:** `[Agribank - Truy cập CSDL] Bàn giao tài khoản - Yêu cầu ký nhận {MãYêuCầu}`
- **Body:**

Kính gửi Anh/Chị {Tên chủ tài khoản},

Bộ phận Quản trị CSDL đã hoàn thành cấp tài khoản theo yêu cầu và lập Biên bản bàn giao tài khoản. Vui lòng đăng nhập hệ thống để kiểm tra thông tin tài khoản và ký nhận.

    Mã biên bản: {MãYêuCầu_04B}
    Mã yêu cầu gốc (04A): {MãYêuCầu_04A}
    Người bàn giao (DBA): {HọTênDBA}
    CSDL: {TênCSDL}
    Ngày bàn giao: {dd/MM/yyyy}

Link: {URL_phiếu_04B}

Trân trọng, Hệ thống Truy cập CSDL - Agribank


## 25. Allowed Files

- `src/main/java/.../request/**`
- `src/main/java/.../workflow/RequestSubmissionService.java`
- `src/main/resources/templates/requests/**`
- `src/main/resources/static/js/requests/**`
- `src/main/resources/static/css/requests/**`
- `src/test/java/.../request/**`

## 26. Must Not Change

- Không sửa màn hình Dashboard ngoài link/nút cần thiết.
- Không sửa service AD/Email/OTP ngoài interface đã thống nhất.
- Không sửa xử lý phê duyệt sau khi yêu cầu đã gửi, trừ phần khởi tạo bước đầu.

## 27. Verification

- Lưu nháp từng mẫu phiếu (7 mẫu).
- Ký xác nhận thành công bằng SoftOTP.
- Gửi phiếu 01/04A với nhiều dòng chi tiết và nhiều người ký.
- Chặn gửi nếu thiếu chữ ký người dùng chung phiếu.
- Chặn gửi nếu không có dòng chi tiết với 01/04A.
- Chặn lập phiếu mới nếu người dùng nợ 05B.
- Chặn thêm người dùng nợ 05B vào danh sách chi tiết phiếu 01-YCTC (inline validation).
- Mẫu 05A sau gửi vào trạng thái "Đã chuyển bộ phận Mở truy cập".
- File SQL 02-YCCS kiểm tra đúng định dạng tên.
- Validate mã yêu cầu trùng ngay khi sửa Ca/Lần (inline validation).
- Retry tự động 3 lần (cách 5s) khi gửi lỗi mạng.
- Hủy yêu cầu bắt buộc nhập lý do.
- Email notification gửi đúng người nhận theo từng loại phiếu (5 loại).
- Cảnh báo nợ 05B hiển thị Toast + Banner cho user đang nợ.
- Mẫu 01-YCTC: phải chọn Loại yêu cầu (Truy vấn/Chỉnh sửa).
- Mẫu 01-YCTC: cột Đơn vị chủ quản ứng dụng tự fill theo Hệ thống.
- Mẫu 04A-YCTK: chỉ cho phép chọn 1 CSDL duy nhất.
- Mẫu 04B-BGTK: DBA lập, liên kết 04A, tự nạp thông tin, DBA nhập UserID.
- Mẫu 04B-BGTK: Mỗi chủ TK ký riêng dòng, người cuối fill ô Người nhận bàn giao.
- Mẫu 05B-HTKC: chọn Ngày + Ca, hệ thống tự nạp 05A, dữ liệu read-only.
- Mẫu 05B-HTKC: trường Mục đích phải mô tả chi tiết bao gồm câu lệnh đã thực hiện (1 ô chung).
- Mẫu 03-YCCT: có trường chọn Tên hệ thống + Tên CSDL.
- Chọn Ca → tự fill thời gian Từ-Đến theo khung ca.

## 28. Definition of Done

- Hoàn thành form cho **7 mẫu phiếu** (01, 02, 03, 04A, 04B, 05A, 05B).
- Có lưu nháp, sửa nháp, ký (SoftOTP), gửi, hủy (có lý do), gửi lại (chỉ khi gửi lỗi).
- Có validate nghiệp vụ và validate giao diện (inline validation cho mã yêu cầu, chặn nợ 05B).
- Có retry tự động 3 lần khi gửi lỗi mạng.
- Có **5 loại email notification** với template chi tiết.
- Có cảnh báo nợ 05B (Toast + Banner).
- Có test cho các luồng chính và lỗi nghiệp vụ quan trọng.
- Nhận trạng thái từ module phê duyệt (callback/event).
- Mẫu 04B: DBA lập, liên kết 04A, chủ TK ký nhận, tự chuyển cấp quản lý.
- Mẫu 05B: Chọn Ngày + Ca, tự nạp 05A (read-only), 1 ô Mục đích chung.

---

## Changelog
Nội dung thay đổi
**Sửa đổi mục 3 và 4 - Đưa luồng 01 sang chung với luồng 04A |
**Bổ sung mục 6 - Luồng 04B-BGTK:** DBA lập, liên kết 04A, nhập UserID, ký SoftOTP, gửi chủ TK ký nhận, tự chuyển cấp quản lý |
**Bổ sung mục 7.2 - Trạng thái riêng 04B:** Nháp, Chờ ký nhận, Đã chuyển cấp quản lý, Gửi lỗi |
**Bổ sung mục 8.5 - Quy tắc Ca và thời gian:** Chọn Ca → tự fill thời gian Từ-Đến |
**Bổ sung mục 13 - Quy tắc riêng 04B:** DBA lập, liên kết 04A, tự nạp thông tin, DBA nhập UserID, ô Người bàn giao/Người nhận tự fill |
**Cập nhật mục 15 - Quy tắc riêng 05B:** Chọn Ngày + Ca, tự nạp 05A (read-only), 1 ô Mục đích chung cho tất cả lần |
**Bổ sung mục 17 - Giao diện 01-YCTC:** Thêm cột Đơn vị chủ quản ứng dụng (tự fill theo Hệ thống) |
**Bổ sung mục 19 - Giao diện 03-YCCT:** Thêm trường Tên hệ thống + Tên CSDL |
**Bổ sung mục 21 - Giao diện 04B-BGTK:** Đầy đủ thông tin chung + chi tiết |
**Cập nhật mục 23 - Giao diện 05B:** Bước chọn Ngày + Ca, dữ liệu read-only từ 05A |
**Bổ sung mục 24.5 - Email 04B:** Template email gửi chủ TK ký nhận |
**Cập nhật toàn bộ:** Thống nhất SoftOTP cho tất cả chữ ký|
**Cập nhật mục 2:** Thêm mẫu 04B-BGTK vào danh sách, ghi rõ người lập |
**Cập nhật mục 16 - Scope:** Bổ sung ranh giới cho 04B (ký đại diện cấp quản lý ngoài scope)
