
# Feature: Chức năng lập và gửi yêu cầu

**Người phụ trách:** Tin  
**Mã hạng mục:** 2.3

---

## Changelog

| Thời điểm | Nội dung thay đổi |
|---|---|
| 05/07/2026 16:57 | Cập nhật toàn bộ theo bản đề xuất FINAL v1.0: Bổ sung mẫu 04B-BGTK; Tách luồng 05B ra khỏi luồng 02,03; Cập nhật luồng 01/04A/05A; Thêm luồng 04B+05B; Thêm hệ thống trạng thái; Thêm Validation Rules; Cập nhật giao diện tất cả mẫu; Cập nhật quy tắc nghiệp vụ; Cập nhật Verification + DoD |
| 05/07/2026 20:25 | Bổ sung chức năng con "Đăng ký trước Yêu cầu chi tiết" cho mẫu 01-YCTC: Giao diện đăng ký trước, bảng CSDL pre_registration_request, logic nạp tự động, hệ thống trạng thái bản ghi, validation rules #16-#22, test cases bổ sung |
| 06/07/2026 11:30 | Đồng bộ với docs/workflow-step-codes.md: Thêm bảng mapping Status Code ↔ Tên hiển thị (mục 7.1); Thêm mục 7.2 mô tả logic khởi tạo workflow khi SUBMIT (set variant, current_step_code, resolveNextActor) |
| 08/07/2026 14:10 | Cập nhật: Format tên file SQL áp dụng cả 02+03 (chi tiết regex YYYYMMDD_BS_XXX.sql); Xóa giới hạn dung lượng file; Xóa trạng thái RETURNED (phiếu bị từ chối → CANCELLED, lập mới); Bổ sung bước khởi tạo workflow vào các luồng; Upload chỉ 1 file; Cập nhật logic bắt buộc Tab 03 (ít nhất 1 mục con); Bổ sung format tên file SQL vào mục 11+14; Bổ sung test cases |
| 08/07/2026 16:10 | Cập nhật: Bổ sung bước chọn Ngày → Ca vào Luồng 1, 2 (01, 02, 03, 04A); Cập nhật Rule #2 chặn nợ 05B (logic mới: validate lúc ký số + validate lúc gửi phê duyệt + cho phép xóa dòng đã ký); Cập nhật Mục 10 quyền xóa/sửa dòng; Polling interval = 10 giây + push sau ký; Variant 04B luôn Internal, 05B không variant; Variant 01 lấy theo dòng đầu tiên; Cập nhật 04B (Phạm vi DBA nhập, Nội dung = Hình thức từ 04A); Cập nhật 05A (Query all data only); Cập nhật 05B (logic gửi email); Validate Nhánh A chỉ tối thiểu khi lưu |
| 08/07/2026 17:06 | Bổ sung Mục 2.1 Role người dùng: Danh sách 8 role hệ thống; Quy tắc gán role (ADMIN gán, AD chỉ xác thực); Ràng buộc loại trừ DBA/ACCESS_TEAM không gán REQUESTER; Ma trận quyền scope lập yêu cầu; Ghi chú hành động REQUESTER (lập phiếu + ký chung) |
| 09/07/2026 10:44 | Cập nhật: Logic nạp đăng ký trước khi đổi Ngày/Ca (xóa toàn bộ + nạp lại); Cho phép lưu phiếu 0 dòng Nhánh A; Bổ sung trường Ngày + bước chọn Ngày vào 05A (mặc định fill hiện tại); 04B chỉ phát sinh khi 04A có dòng "Cấp mới" + chỉ nạp dòng "Cấp mới"; Rule #2 chỉ áp dụng phiếu 01 (không áp dụng 04A); Bổ sung ghi chú scope bước 14-18 luồng 04B; Trường "Họ và tên" mẫu 01 = dropdown REQUESTER cùng phòng (người lập) / tự fill (người khác); Rule #15 cập nhật: 1 người nhiều dòng ký 1 lần tất cả dòng tự "Đã ký"; Mục 7.2 ghi rõ Nhánh A không khởi tạo workflow; Format mã yêu cầu: KýhiệuĐV_ddmmyyyy_Ca_Lần (unique toàn hệ thống, không ký hiệu mẫu, phân biệt qua form_type); Mẫu 05B sinh mã riêng + bảng tham chiếu request_05b_05a_mapping (1-N); Trường "Lần" tự tăng theo Ngày+Ca toàn hệ thống, format 2 chữ số (mở rộng 3 nếu vượt 99); Cập nhật mục 19.2 vị trí giao diện đăng ký trước |
| 10/07/2026 10:00 | Đồng bộ với đặc tả v2.0: (1) Sinh mã chỉ khi gửi phê duyệt — DRAFT và PENDING_SIGN không sinh mã, cột Mã YC hiển thị "—", tìm kiếm qua metadata; (2) Timeout PENDING_SIGN hết ca → tự động CANCELLED + email; (3) Tách biệt logic xóa dòng chưa ký khi gửi duyệt (không liên quan timeout); (4) Cập nhật format mã 04B = [Mã 04A]_04B_V[xx], mã 05B = [Mã ĐV]_[ddmmyyyy]_[Ca]_[Lần ghép]_V[xx]; (5) Bổ sung trạng thái REJECTED, APPROVED, IN_PROGRESS (scope phê duyệt); (6) Giao diện 05A: bỏ trường "Ngày" riêng, "Ngày lập yêu cầu" = auto fill ngày hiện tại hoặc tương lai (date picker, không cho nhập); (7) Bổ sung Rule #16 Timeout PENDING_SIGN → CANCELLED; (8) Bổ sung mục Email Templates; (9) Bổ sung mục Phạm vi (In-scope/Out-of-scope), Thuật ngữ, Variant theo mẫu, Giao diện chọn mẫu, Filter ĐV chủ quản chi tiết, Logic Người lập = Người truy cập, Bảng mapping 04B/05B có version, Giả định bổ sung, Rủi ro bổ sung, DoD + Verification cập nhật |
| 10/07/2026 10:30 | Bổ sung mục 32: Schema CSDL chính — 9 bảng (request, request_detail, request_04b_detail, request_sql_file, request_03_structure, request_sequence, workflow_history, email_notification_log) + ERD tóm tắt + ghi chú thiết kế |

---

## 1. Mục tiêu

Cho phép người lập yêu cầu đăng nhập, chọn mẫu phiếu, nhập thông tin, ký xác nhận, lưu nháp, gửi phê duyệt hoặc gửi bộ phận Mở truy cập đối với yêu cầu khẩn cấp.

## 2. Mẫu phiếu hỗ trợ

- 01-YCTC: Truy cập, truy xuất CSDL thông thường.
- 02-YCCS: Chỉnh sửa dữ liệu.
- 03-YCCT: Thay đổi cấu trúc CSDL.
- 04A-YCTK: Cấp mới/thay đổi thuộc tính tài khoản.
- 04B-BGTK: Biên bản bàn giao tài khoản (DBA lập sau khi cấp thành công tài khoản theo 04A-YCTK).
- 05A-YCKC: Truy cập khẩn cấp.
- 05B-HTKC: Hoàn thành truy cập khẩn cấp.

## 2.1 Role người dùng

### Danh sách Role hệ thống

| # | Code | Tên hiển thị | Mô tả | Scope |
|---|---|---|---|---|
| 1 | REQUESTER | Người lập yêu cầu | Tạo, ký, gửi yêu cầu | Lập yêu cầu |
| 2 | DEPT_MANAGER | Trưởng phòng hoặc tương đương | Kiểm tra, ký, chuyển tiếp | Phê duyệt |
| 3 | AUTHORITY | Người có thẩm quyền | Phê duyệt | Phê duyệt |
| 4 | CHECKER | Bộ phận kiểm tra | Kiểm tra nội dung/script | Phê duyệt |
| 5 | ACCESS_TEAM | Bộ phận mở truy cập | Mở truy cập, ghi thời gian | Phê duyệt |
| 6 | DBA | Quản trị CSDL/DBA | Xử lý cấu trúc/tài khoản | Lập yêu cầu + Phê duyệt |
| 7 | EXECUTOR | Người thực hiện | Chạy script/chỉnh sửa dữ liệu | Phê duyệt |
| 8 | ADMIN | Quản trị hệ thống | Cấu hình hệ thống | Quản trị |

### Quy tắc gán Role

- Tất cả role do **ADMIN gán** trong hệ thống (không tự động từ AD).
- AD chỉ dùng để xác thực đăng nhập (authentication), không dùng để phân quyền (authorization).
- 1 người dùng có thể có **nhiều role** đồng thời.
- **Ràng buộc loại trừ:** DBA và ACCESS_TEAM **KHÔNG được gán** role REQUESTER.
  - Lý do: DBA đã có toàn quyền trên CSDL, không có nhu cầu lập phiếu yêu cầu truy cập.
  - ACCESS_TEAM không lập phiếu nào trong scope lập yêu cầu.

### Ma trận quyền — Scope lập yêu cầu

| Hành động | REQUESTER | DBA | ADMIN |
|---|---|---|---|
| Lập mẫu 01-YCTC | ✅ | — | — |
| Lập mẫu 02-YCCS | ✅ | — | — |
| Lập mẫu 03-YCCT | ✅ | — | — |
| Lập mẫu 04A-YCTK | ✅ | — | — |
| Lập mẫu 04B-BGTK | — | ✅ | — |
| Lập mẫu 05A-YCKC | ✅ | — | — |
| Lập mẫu 05B-HTKC | ✅ (*) | — | — |
| Đăng ký trước (01) | ✅ | — | — |
| Ký chung phiếu 01/04A | ✅ | — | — |
| Ký nhận 04B | ✅ (**) | — | — |
| Xóa dòng trên phiếu (người lập) | ✅ | ✅ (04B) | — |
| Hủy phiếu (người lập) | ✅ | ✅ (04B) | — |
| Gán role cho người dùng | — | — | ✅ |
| Cấu hình danh mục HT/CSDL | — | — | ✅ |

(*) 05B-HTKC: Chỉ REQUESTER đã lập 05A tương ứng mới được lập 05B.
(**) Ký nhận 04B: REQUESTER đã lập 04A trước đó, có tên trong danh sách người nhận bàn giao.

### Ghi chú

- **REQUESTER** có 2 hành động chính:
  - **(a) Lập phiếu:** Tạo mới, nhập thông tin, ký gửi phê duyệt/kiểm tra.
  - **(b) Ký chung:** Thêm dòng + ký xác nhận trên phiếu 01/04A do người khác lập (cùng đơn vị, trạng thái "Chờ ký xác nhận"). Bất kỳ REQUESTER nào cùng đơn vị đều có thể ký chung.
- **DBA:** Mặc định chỉ hiển thị mẫu 04B-BGTK. REQUESTER mặc định KHÔNG hiển thị 04B.
- **EXECUTOR:** Hoàn toàn thuộc scope phê duyệt — không có hành động nào trong scope lập yêu cầu.
- Các role DEPT_MANAGER, AUTHORITY, CHECKER, ACCESS_TEAM, EXECUTOR thuộc **scope phê duyệt** — không mô tả chi tiết trong file này.

## 2.2 Format Mã yêu cầu

**Format:** `KýhiệuĐV_ddmmyyyy_Ca_Lần`

| Thành phần | Mô tả | Ví dụ |
|---|---|---|
| Ký hiệu ĐV | Từ bảng cấu hình đơn vị (đã có sẵn). Format: ĐơnVịCha-PhòngBan | CNTT-NHDT, NHS-KT |
| ddmmyyyy | Ngày lập phiếu | 09072026 |
| Ca | Số, format 2 chữ số | 01, 02, 03 |
| Lần | Số, format 2 chữ số (mở rộng 3 chữ số nếu vượt 99). Tự tăng theo cùng Ngày + Ca toàn hệ thống (không phân biệt đơn vị, không phân biệt mẫu phiếu) | 01, 02, 03... |

**Ví dụ:** `CNTT-NHDT_09072026_02_03` = Phòng NHDT thuộc TT CNTT, ngày 09/07/2026, Ca 2, phiếu thứ 3 trong ca đó (toàn hệ thống).

**Quy tắc:**
- Unique toàn hệ thống (không trùng giữa các mẫu phiếu).
- Không có ký hiệu mẫu phiếu trong mã. Hệ thống phân biệt loại phiếu qua trường `form_type` trong CSDL.
- Separator: gạch dưới `_` giữa các thành phần.
- Sequence "Lần": 1 sequence duy nhất cho toàn hệ thống, tăng theo Ngày + Ca.

**Thời điểm sinh mã:**
- DRAFT → không sinh mã.
- PENDING_SIGN → không sinh mã. Cột "Mã yêu cầu" hiển thị "—".
- Gửi phê duyệt (chuyển sang PENDING_APPROVAL / PENDING_CHECK / PENDING_ACCESS_TEAM) → **SINH MÃ**. Ngày trong mã = ngày gửi phê duyệt.

**Hiển thị phiếu chưa sinh mã:**
- Phiếu ở trạng thái DRAFT / PENDING_SIGN được tìm kiếm/hiển thị qua metadata: Người lập + Ngày tạo + Loại mẫu + Trạng thái.
- Danh sách phiếu chờ ký: hiển thị cột "Loại mẫu", không hiển thị mã.
- Người cùng phòng tìm phiếu chờ ký qua: Filter trạng thái = PENDING_SIGN + Phòng ban mình.

### 2.2.1 Format mã 04B-BGTK

**Format:** `[Mã 04A]_04B_V[xx]`

| Thành phần | Mô tả | Ví dụ |
|---|---|---|
| Mã 04A | Mã yêu cầu của phiếu 04A tham chiếu | CNTT-NHDT_09072026_02_03 |
| 04B | Ký hiệu mẫu phiếu bổ sung | 04B |
| V[xx] | Version, 2 chữ số, tăng khi hủy + lập lại | V01, V02 |

**Ví dụ:** `CNTT-NHDT_09072026_02_03_04B_V01`

**Quy tắc:**
- Nhiều lần phát sinh (lần đầu bị hủy, lập lại) → tăng version: _V02, _V03...
- Lưu bảng `request_04b_04a_mapping` (có version, status).

### 2.2.2 Format mã 05B-HTKC

**Format:** `[Mã ĐV]_[ddmmyyyy]_[Ca]_[Lần ghép]_V[xx]`

| Thành phần | Mô tả | Ví dụ |
|---|---|---|
| Mã ĐV | Ký hiệu đơn vị người lập (lấy từ 05A) | CNTT-NHDT |
| ddmmyyyy | Ngày (lấy từ 05A) | 09072026 |
| Ca | Ca (lấy từ 05A) | 02 |
| Lần ghép | Ghép số thứ tự các lần 05A thuộc nhóm gộp | 0103 |
| V[xx] | Version, 2 chữ số | V01 |

**Ví dụ:** Ca 02 có 3 phiếu 05A (lần 01, 02, 03), gộp lần 01 và 03 → `CNTT-NHDT_09072026_02_0103_V01`

**Quy tắc:**
- Chỉ ghép lần thuộc nhóm gộp (không phải toàn bộ lần trong ca).
- Hủy + lập lại → tăng version.
- Lưu bảng `request_05b_05a_mapping` (có version, status).

## 3. Luồng 1: 02-YCCS, 03-YCCT (Gửi bộ phận kiểm tra)

1. Người lập yêu cầu đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", chọn mẫu phiếu (02-YCCS hoặc 03-YCCT).
3. Chọn Hệ thống (1 HT duy nhất) → Chọn CSDL (1 CSDL duy nhất).
4. Chọn Ngày (date picker, chỉ cho chọn — không cho nhập, chỉ hiện tại hoặc tương lai) → Chọn Ca (dropdown). Validate: không cho phép chọn ngày + ca quá khứ.
5. Nhập nội dung chi tiết cho mẫu phiếu.
6. Ký xác nhận gửi kiểm tra.
7. Hệ thống kiểm tra trường bắt buộc + validation rules.
8. Hệ thống sinh mã yêu cầu (format: KýhiệuĐV_ddmmyyyy_Ca_Lần).
9. Hệ thống lưu hồ sơ ở trạng thái `Chờ kiểm tra`.
10. Hệ thống khởi tạo workflow: Xác định variant (I/E), set `current_step_code`, gọi `resolveNextActor()` — chi tiết xem mục 7.2.
11. Hệ thống gửi email notification đến Bộ phận kiểm tra.
12. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

## 4. Luồng 2: 01-YCTC, 04A-YCTK (Phiếu nhiều người ký)

1. Người lập đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", chọn mẫu phiếu (01-YCTC hoặc 04A-YCTK).
3. (Mẫu 01-YCTC): Chọn "Loại yêu cầu" (Truy vấn / Chỉnh sửa) — áp dụng toàn bộ phiếu.
4. (Mẫu 04A-YCTK): Chọn Hệ thống (1 HT) → Chọn CSDL (1 CSDL) — thông tin chung.
5. Chọn Ngày (date picker, chỉ cho chọn — không cho nhập, chỉ hiện tại hoặc tương lai) → Chọn Ca (dropdown). Validate: không cho phép chọn ngày + ca quá khứ.
6. Nhập thông tin chung + danh sách chi tiết (tối thiểu 1 dòng khi gửi).
7. Người lập chọn một trong hai hành động:

### Nhánh A: Lưu phiếu (chờ người khác ký)

8A. Hệ thống cho phép lưu phiếu trống (0 dòng chi tiết). Validate đầy đủ trường bắt buộc + validation rules (bao gồm ≥1 dòng chi tiết) chỉ thực hiện khi người lập ký gửi phê duyệt (bước 15A-a).
9A. Hệ thống lưu hồ sơ ở trạng thái `Chờ ký xác nhận` (PENDING_SIGN). **KHÔNG sinh mã yêu cầu.** Cột "Mã yêu cầu" hiển thị "—".
10A. Người dùng cùng đơn vị có nhu cầu đăng nhập bằng tài khoản AD.
11A. Chọn phiếu đang "Chờ ký xác nhận" (tìm qua: Filter trạng thái PENDING_SIGN + Phòng ban).
12A. Người dùng chỉ được:
- Thêm dòng mới cho bản thân (tự fill tên từ AD, không cho chọn người khác).
- Sửa dòng của mình (nếu đã có và chưa ký).
- Không giới hạn số lượng người dùng.
13A. Người dùng ký xác nhận dòng chi tiết của mình.
- Cơ chế: Row-level locking + Polling (cập nhật mỗi 10 giây) + Push ngay sau khi ký (không chờ polling cycle).
- 1 người có thể có nhiều dòng (mỗi dòng 1 HT/CSDL khác nhau). Ký 1 lần → tất cả dòng của người đó tự động chuyển trạng thái "Đã ký".
- **Validate lúc ký (chỉ mẫu 01-YCTC):** Kiểm tra người dùng có nợ phiếu 05B quá hạn không → nếu nợ → thông báo + không cho ký số.
14A. Người lập phiếu thực hiện 1 trong 2:

a) **Ký xác nhận & Gửi phê duyệt:**
- Hệ thống kiểm tra trường bắt buộc + validation rules đầy đủ (bao gồm ≥1 dòng chi tiết).
- **Validate nợ 05B (chỉ mẫu 01-YCTC):** Kiểm tra toàn bộ người dùng trong phiếu (bao gồm dòng nạp từ đăng ký trước đã ký từ trước) → nếu có người nợ 05B quá hạn → thông báo danh sách người nợ + không cho gửi. Người lập có thể xóa dòng của người nợ (dù đã ký) rồi gửi lại.
- Các dòng chưa ký xác nhận mà người dùng tại dòng chi tiết **khác người lập yêu cầu** sẽ tự động bị xóa.
- **Hệ thống sinh mã yêu cầu** (format: KýhiệuĐV_ddmmyyyy_Ca_Lần). Đây là thời điểm chính thức sinh mã.
- Hệ thống lưu trạng thái `Chờ phê duyệt`.
- Hệ thống khởi tạo workflow: Xác định variant (I/E), set `current_step_code`, gọi `resolveNextActor()` — chi tiết xem mục 7.2.
- Gửi email notification đến Trưởng phòng/tương đương.
- → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

b) **Hủy phiếu:**
- Không cần lý do.
- Trạng thái → `Đã hủy`.
- → Kết thúc luồng.

### Nhánh B: Ký xác nhận & Gửi (Người lập chính là người ký chi tiết)

8B. Hệ thống kiểm tra trường bắt buộc + validation rules.
9B. **Validate nợ 05B (chỉ mẫu 01-YCTC):** Kiểm tra người lập có nợ 05B quá hạn không → nếu nợ → thông báo + không cho gửi.
10B. Hệ thống sinh mã yêu cầu.
11B. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
12B. Hệ thống khởi tạo workflow: Xác định variant (I/E), set `current_step_code`, gọi `resolveNextActor()` — chi tiết xem mục 7.2.
13B. Hệ thống gửi email notification đến Trưởng phòng/tương đương.
14B. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

### Timeout PENDING_SIGN:

- **Mẫu 01-YCTC:** Hết thời gian ca đã chọn → Hệ thống **tự động chuyển trạng thái CANCELLED** + Gửi email notification đến người lập thông báo phiếu đã hết hạn.
- **Mẫu 04A-YCTK:** Hết ngày lập phiếu → Hệ thống **tự động chuyển trạng thái CANCELLED** + Gửi email notification đến người lập.

### Xóa dòng chưa ký khi gửi phê duyệt (logic riêng, không liên quan timeout):

- Khi người lập ký xác nhận và gửi phê duyệt (bước 14A-a), các dòng chưa được ký xác nhận mà **người dùng tại dòng chi tiết khác người lập yêu cầu** sẽ tự động bị xóa.
- Dòng của chính người lập (nếu chưa ký) vẫn được giữ lại (áp dụng logic "Người lập = Người truy cập" → auto "Đã ký").

## 5. Luồng 3: 05A-YCKC (Truy cập khẩn cấp — Gửi thẳng BP Mở truy cập)

1. Người yêu cầu đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", mẫu 05A-YCKC.
3. Chọn Hệ thống (1 HT) → Chọn CSDL (1 CSDL).
4. Chọn Ngày lập yêu cầu (date picker, auto fill ngày hiện tại, chỉ cho pick ngày hiện tại hoặc tương lai, không cho nhập tay) → Chọn Ca (dropdown, mặc định fill ca hiện tại). Validate: không cho phép chọn ngày + ca quá khứ.
5. Hệ thống tự fill "Thời gian yêu cầu" theo ca, không cho phép sửa.
6. Nhập nội dung chi tiết (Mục đích/Lý do, Quyền trên đối tượng dữ liệu).
7. Người lập ký xác nhận.
8. Hệ thống kiểm tra trường bắt buộc + validation rules.
9. Hệ thống sinh mã yêu cầu.
10. Hệ thống lưu hồ sơ ở trạng thái `Đã chuyển bộ phận Mở truy cập`.
11. Hệ thống khởi tạo workflow: Set `current_step_code` = `05A_01`, gọi `resolveNextActor()` — chi tiết xem mục 7.2.
12. Hệ thống gửi email notification đến Bộ phận mở truy cập.
13. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

## 6. Luồng 4: 04B-BGTK, 05B-HTKC (Phiếu bổ sung sau hoàn thành)

### 6.1 Luồng 04B-BGTK (DBA lập)

**Điều kiện hiển thị:** Chỉ hiển thị đối với DBA (kiểm tra quyền).
**Điều kiện lập:** Phiếu 04A-YCTK liên quan phải ở trạng thái "Hoàn thành" VÀ phải có ít nhất 1 dòng chi tiết có Hình thức = "Cấp mới".

1. DBA đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", mẫu 04B-BGTK.
3. Hệ thống hiển thị danh sách 04A-YCTK đã hoàn thành, có ít nhất 1 dòng "Cấp mới", nhưng chưa có 04B-BGTK tương ứng.
4. DBA chọn phiếu đang nợ.
5. Hệ thống tự động fill nội dung từ 04A-YCTK:
   - Tên hệ thống, Tên CSDL.
   - Mã yêu cầu 04A liên quan.
   - Thời gian bàn giao (ngày hiện tại).
   - Đại diện BP quản trị CSDL (Cấp QL) — từ cấu hình.
   - Người bàn giao (DBA) — user đăng nhập.
   - Đại diện BP nhận bàn giao (Cấp QL) — lãnh đạo phòng người yêu cầu.
   - Người nhận bàn giao — danh sách người dùng từ 04A (chỉ những người có Hình thức = "Cấp mới").
   - Chi tiết: Chỉ nạp các dòng có Hình thức = "Cấp mới" từ 04A (KHÔNG nạp dòng "Đổi thuộc tính"). Gồm: Loại tài khoản, Nội dung (= "Cấp mới"), Chủ tài khoản.
   - **KHÔNG tự fill:** Tài khoản được cấp (UserID) — DBA nhập tay. Phạm vi — DBA tự nhập.
6. DBA nhập thông tin tài khoản đã cấp + Phạm vi + Địa điểm bàn giao.
7. DBA ký xác nhận.
8. Hệ thống kiểm tra trường bắt buộc + validation rules.
9. Hệ thống sinh mã yêu cầu.
10. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
11. Hệ thống khởi tạo workflow: Variant luôn = `I` (Internal — DBA thuộc đơn vị chủ quản CSDL), set `current_step_code`, gọi `resolveNextActor()` — chi tiết xem mục 7.2.
12. Hệ thống gửi email notification đến Lãnh đạo phòng quản trị CSDL.
13. → Chuyển scope phê duyệt (Lãnh đạo phòng DBA duyệt).

> **Ghi chú:** Bước 14-18 mô tả tổng quan luồng ký nhận để developer hiểu context. Chi tiết xử lý phê duyệt + ký nhận sẽ được mô tả đầy đủ trong scope phê duyệt.

14. Sau khi duyệt → Trạng thái chuyển `Chờ ký nhận`.
15. Hệ thống gửi email notification cho người dùng trong danh sách.
16. Người dùng đăng nhập → Ký nhận dòng của mình (row-level locking, polling 10 giây + push sau ký).
17. Khi tất cả người dùng đã ký → Hệ thống tự động chuyển `Chờ phê duyệt` (Lãnh đạo phòng người dùng).
18. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt lần 2.

**Timeout ký nhận:** 3 ngày kể từ khi chuyển "Chờ ký nhận".
- Hành động: Gửi email cho DBA, lãnh đạo phòng DBA, lãnh đạo phòng cán bộ cần ký.
- KHÔNG hủy phiếu, giữ nguyên trạng thái "Chờ ký nhận".

### 6.2 Luồng 05B-HTKC (Người lập 05A lập)

**Điều kiện lập:** Phiếu 05A-YCKC liên quan phải ở trạng thái "Hoàn thành" (đã mở truy cập thành công).

1. Người lập đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", mẫu 05B-HTKC.
3. Hệ thống hiển thị các trường hợp cần bổ sung 05B:
   - Gộp tự động các mẫu 05A chung Hệ thống + CSDL + Ngày + Ca thành 1 mục.
   - Người dùng không cần chọn từng phiếu riêng lẻ.
4. Người dùng chọn mục cần bổ sung.
5. Hệ thống tự động fill thông tin:
   - Mã yêu cầu: Sinh mã riêng theo format `KýhiệuĐV_ddmmyyyy_Ca_Lần` (mã mới, không dùng mã 05A).
   - Danh sách bảng = union tất cả bảng từ các phiếu 05A trong ca.
   - Thông tin chung: Hệ thống, CSDL, Ngày, Ca, Thời gian.
   - Trường "Lần" hiển thị tổng hợp (VD: "Lần: 01, 02, 03") — chỉ là hiển thị, lấy từ trường "Lần" của các phiếu 05A liên quan.
   - Lưu bảng tham chiếu `request_05b_05a_mapping` (1 phiếu 05B → nhiều phiếu 05A).
6. Người lập nhập nội dung công việc đã thực hiện (mô tả chi tiết, câu lệnh) — bắt buộc.
7. Người lập ký xác nhận.
8. Hệ thống kiểm tra trường bắt buộc + validation rules.
9. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
10. Hệ thống khởi tạo workflow: Không có variant (giống 05A, chỉ 1 luồng duy nhất), set `current_step_code` = `05B_01`, gọi `resolveNextActor()` — chi tiết xem mục 7.2.
11. Hệ thống xác định: Nếu người lập thuộc ĐV chủ quản ứng dụng → gửi email đến lãnh đạo phòng ĐV chủ quản ứng dụng. Nếu không → gửi email đến lãnh đạo phòng ĐV yêu cầu.
12. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

## 7. Hệ thống trạng thái phiếu (Scope lập yêu cầu)

### 7.1 Bảng mapping Status Code ↔ Tên hiển thị

> Tham chiếu: `docs/workflow-step-codes.md` mục 2 "Trạng thái đặc biệt".

| Status Code | Tên hiển thị | Áp dụng cho | Có mã YC? | Ghi chú |
|---|---|---|---|---|
| `DRAFT` | Nháp | Tất cả mẫu | ❌ Không | Phiếu đã lưu, chưa ký, chưa gửi |
| `PENDING_SIGN` | Chờ ký xác nhận | 01-YCTC, 04A-YCTK | ❌ Không | Chưa sinh mã, chờ người dùng chung ký dòng chi tiết. Cột "Mã YC" hiển thị "—" |
| `PENDING_RECEIPT` | Chờ ký nhận | 04B-BGTK | ✅ Có | Đã được lãnh đạo DBA duyệt, chờ người dùng ký nhận tài khoản |
| `PENDING_CHECK` | Chờ kiểm tra | 02-YCCS, 03-YCCT | ✅ Có | Đã ký, gửi bộ phận kiểm tra. `current_step_code` = step đầu tiên (VD: `02_I_01` hoặc `02_E_01`) |
| `PENDING_APPROVAL` | Chờ phê duyệt | 01, 04A, 04B, 05B | ✅ Có | Đã ký đầy đủ, chờ lãnh đạo phê duyệt. `current_step_code` = step đầu tiên |
| `PENDING_ACCESS_TEAM` | Đã chuyển BP Mở truy cập | 05A-YCKC | ✅ Có | Gửi thẳng bộ phận mở truy cập. `current_step_code` = `05A_01` |
| `REJECTED` | Bị từ chối | Tất cả | ✅ Có | Bị từ chối bởi lãnh đạo hoặc người kiểm tra. Có lý do từ chối. Không cho phép chỉnh sửa, phải lập mẫu mới. *Thuộc scope phê duyệt — chi tiết xem module Phê duyệt.* |
| `APPROVED` | Đã phê duyệt | Tất cả | ✅ Có | Đã được phê duyệt thành công. *Thuộc scope phê duyệt — chi tiết xem module Phê duyệt.* |
| `IN_PROGRESS` | Đang thực hiện | 02, 03, 04A, 05A | ✅ Có | Đang được thực hiện (chạy script, mở truy cập...). *Thuộc scope phê duyệt — chi tiết xem module Phê duyệt.* |
| `CANCELLED` | Đã hủy | Tất cả | Tùy thời điểm | Người lập hủy phiếu (không cần lý do) HOẶC phiếu PENDING_SIGN hết ca → tự động hủy. Không cho phép chỉnh sửa, phải lập mẫu mới. |
| `COMPLETED` | Hoàn thành | Tất cả | ✅ Có | Đã hoàn thành toàn bộ luồng |

**Lưu ý:**
- Phiếu bị từ chối bởi lãnh đạo hoặc người kiểm tra → chuyển trạng thái `REJECTED`. Không cho phép chỉnh sửa, phải lập mẫu mới.
- Phiếu PENDING_SIGN hết thời gian ca → tự động chuyển `CANCELLED` + gửi email notification đến người lập.

### 7.2 Logic khởi tạo Workflow khi SUBMIT

> Tham chiếu: `docs/workflow-step-codes.md` mục 3 "Quy tắc xác định Variant" và mục 9.1 "Module Request — Khi SUBMIT".

**Lưu ý:** Khi Nhánh A lưu phiếu (bước 8A-10A), hệ thống chỉ set status = `PENDING_SIGN`, KHÔNG khởi tạo workflow (không set step_code, không gọi resolveNextActor). Workflow chỉ khởi tạo khi người lập SUBMIT (bước 15A-a).

Khi người lập ấn "Gửi phê duyệt" / "Gửi kiểm tra" / "Gửi BP Mở truy cập", module Request thực hiện tuần tự:

1. **Xác định variant (I/E):**
   - Lấy `owner_unit_id` từ `information_system` (đơn vị chủ quản ứng dụng).
   - Nếu `requester_unit_id == owner_unit_id` → variant = `I` (Internal).
   - Nếu `requester_unit_id != owner_unit_id` → variant = `E` (External).
   - **04B-BGTK:** Luôn variant = `I` (Internal). DBA thuộc đơn vị chủ quản CSDL.
   - **03-YCCT, 05A-YCKC, 05B-HTKC:** Không có variant (chỉ 1 luồng duy nhất).
   - **01-YCTC:** Nếu phiếu có nhiều HT khác nhau (chung 1 đơn vị chủ quản), lấy `owner_unit_id` theo dòng chi tiết đầu tiên.

2. **Set `current_step_code`:** Theo format `{MÃ_MẪU}_{VARIANT}_{01}` hoặc `{MÃ_MẪU}_{01}` (nếu không có variant).
   - Ví dụ: 01-YCTC Internal → `01_I_01`; 02-YCCS External → `02_E_01`; 05A → `05A_01`; 05B → `05B_01`.

3. **Set `at_requester_phase`:** Theo bảng mapping tại `workflow-step-codes.md` mục 7.
   - Variant `I` → luôn `false`.
   - Variant `E`, step 01/02 → `true` (đang ở đơn vị yêu cầu).

4. **Set `owner_unit_id`:** Đơn vị chủ quản ứng dụng (từ `information_system`).

5. **Set `owner_db_unit_id`:** Đơn vị chủ quản CSDL (từ `database_catalog`) — chỉ áp dụng cho 03, 04A.

6. **Gọi `resolveNextActor()`:** Xác định actor xử lý bước đầu tiên → set `current_actor_type`, `current_actor_id`, `current_actor_role`, `current_unit_id`.

7. **Set `status`:** Theo bảng mapping mục 7.1:
   - 01, 04A (Nhánh B), 04B, 05B → `PENDING_APPROVAL`
   - 01, 04A (Nhánh A lưu chờ ký) → `PENDING_SIGN` (chỉ set status, KHÔNG khởi tạo workflow, KHÔNG sinh mã)
   - 02, 03 → `PENDING_CHECK`
   - 05A → `PENDING_ACCESS_TEAM`

8. **Ghi `workflow_history`:** action = `SUBMIT`, step_code = step đầu tiên.

### 7.3 Bảng trạng thái (tham chiếu nhanh)

| Trạng thái | Áp dụng cho | Mô tả | Chuyển tiếp |
|---|---|---|---|
| Nháp | Tất cả mẫu | Phiếu đã lưu, chưa ký, chưa gửi | → Chờ ký xác nhận / Chờ phê duyệt / Chờ kiểm tra / Đã chuyển BP Mở truy cập |
| Chờ ký xác nhận | 01-YCTC, 04A-YCTK | Chưa sinh mã, chờ người dùng chung ký dòng chi tiết | → Chờ phê duyệt / Đã hủy (timeout hoặc người lập hủy) |
| Chờ ký nhận | 04B-BGTK | Đã được lãnh đạo DBA duyệt, chờ người dùng ký nhận tài khoản | → Chờ phê duyệt (lần 2) |
| Chờ kiểm tra | 02-YCCS, 03-YCCT | Đã ký, gửi bộ phận kiểm tra | → Scope phê duyệt |
| Chờ phê duyệt | 01, 04A, 04B, 05B | Đã ký đầy đủ, chờ lãnh đạo phê duyệt | → Scope phê duyệt |
| Đã chuyển BP Mở truy cập | 05A-YCKC | Gửi thẳng bộ phận mở truy cập | → Scope phê duyệt |
| Bị từ chối | Tất cả | Bị từ chối bởi lãnh đạo/người kiểm tra (có lý do) | Kết thúc — phải lập mới |
| Đã phê duyệt | Tất cả | Đã được phê duyệt (scope phê duyệt) | → Đang thực hiện / Hoàn thành |
| Đang thực hiện | 02, 03, 04A, 05A | Đang xử lý (scope phê duyệt) | → Hoàn thành |
| Đã hủy | Tất cả | Người lập hủy hoặc timeout PENDING_SIGN | Kết thúc |
| Hoàn thành | Tất cả | Đã hoàn thành toàn bộ luồng | Kết thúc |

## 8. Validation Rules

| # | Rule | Chi tiết | Áp dụng |
|---|---|---|---|
| 1 | Trường bắt buộc | Kiểm tra tất cả trường bắt buộc trước khi cho phép ký/gửi | Tất cả |
| 2 | Chặn nợ 05B | Người dùng có phiếu 05A đã "Hoàn thành" (đã mở truy cập) quá 03 ngày mà chưa lập 05B: **(a)** Chặn người dùng đó lập tất cả phiếu (trừ 05B). Thông báo: "Bạn đang nợ phiếu 05B-HTKC quá hạn. Vui lòng hoàn thành trước khi lập yêu cầu mới." (kèm link đến mẫu 05B). **(b)** Khi người dùng nợ 05B được thêm vào phiếu 01 đang "Chờ ký xác nhận" → validate lúc ký số: check nợ → nếu nợ → thông báo + không cho ký số. **(c)** Khi người lập phiếu 01 ký số gửi phê duyệt → validate toàn bộ người dùng trong phiếu (bao gồm dòng nạp từ đăng ký trước đã ký từ trước) → nếu có người nợ 05B → thông báo danh sách người nợ + không cho gửi. Người lập có thể xóa dòng của người nợ (dù đã ký) rồi gửi lại. **(d)** Gỡ chặn khi hoàn thành tất cả 05B đang nợ. **Lưu ý:** Rule #2(b)(c) chỉ áp dụng cho phiếu 01-YCTC. Không áp dụng cho 04A-YCTK. | 01-YCTC (ký chung + gửi), Tất cả (lập mới) |
| 3 | 1 HT + 1 CSDL | Mỗi phiếu chỉ được chọn 1 Hệ thống và 1 CSDL | 02, 03, 04A, 05A |
| 4 | Phiếu gốc hoàn thành | Chỉ lập 04B/05B khi phiếu 04A/05A tương ứng ở trạng thái "Hoàn thành". Riêng 04B: phiếu 04A phải có ít nhất 1 dòng Hình thức = "Cấp mới" (04B chỉ nạp dòng "Cấp mới", không nạp "Đổi thuộc tính"). | 04B, 05B |
| 5 | Checksum file SQL | Hỗ trợ MD5 + SHA-256 (người dùng chọn loại). Luồng: Upload file → Hệ thống tự tính hash → Người dùng nhập checksum gốc → So sánh. Match = OK, cho tiếp. Không match = Báo lỗi "Mã kiểm tra không khớp", chặn gửi. Format validation: MD5=32 ký tự hex, SHA-256=64 ký tự hex. | 02, 03 |
| 7 | Format tên file SQL | Phải đúng định dạng `YYYYMMDD_BS_XXX.sql`. Trong đó: YYYYMMDD = ngày tháng năm (8 chữ số); BS = 2 ký tự số hoặc chữ bất kỳ (không chứa ký tự đặc biệt, regex: `[A-Za-z0-9]{2}`); XXX = chỉ được là số (regex: `[0-9]+`). | 02-YCCS, 03-YCCT |
| 8 | Mẫu 03 - Nội dung/Script | 1 file SQL Script chung cho toàn bộ phiếu (có thể bao gồm cả Tạo mới/Thay đổi/Xóa hoặc chỉ 1 phần). Nếu có file SQL Script + checksum khớp → nội dung chi tiết các tab KHÔNG bắt buộc. Nếu KHÔNG có file SQL Script → nội dung chi tiết của tất cả tab đã chọn PHẢI có dữ liệu: Tab Tạo mới và Tab Thay đổi chỉ cần ít nhất 1 mục con có dữ liệu; Tab Xóa phải có dữ liệu (chỉ có 1 trường "Nội dung lệnh xóa"). | 03-YCCT |
| 9a | Timeout 01-YCTC (PENDING_SIGN) | Phiếu PENDING_SIGN hết thời gian ca đã chọn → Hệ thống tự động chuyển CANCELLED + email (xem Rule #16). | 01-YCTC |
| 9b | Timeout 04A-YCTK (PENDING_SIGN) | Phiếu PENDING_SIGN hết ngày lập phiếu → Hệ thống tự động chuyển CANCELLED + email (xem Rule #16). | 04A-YCTK |
| 9c | Timeout 04B ký nhận | 3 ngày kể từ khi chuyển "Chờ ký nhận" → Email cho DBA, lãnh đạo phòng DBA, lãnh đạo phòng cán bộ. KHÔNG hủy phiếu, giữ nguyên. | 04B-BGTK |
| 10a | Trùng lặp 01-YCTC | Chặn khi trùng cả 4 nội dung: Hệ thống + CSDL + Đối tượng + Người dùng (trong cùng phiếu) | 01-YCTC |
| 10b | Trùng lặp 04A-YCTK | Chặn trùng người dùng ở bảng chi tiết (trong cùng phiếu) | 04A-YCTK |
| 11 | Thời gian tạo phiếu | Ngày + Ca: chỉ cho phép chọn (không cho nhập), chỉ hiện tại hoặc tương lai, không được chọn quá khứ (trừ 04B, 05B — phiếu bổ sung) | Tất cả (trừ 04B, 05B) |
| 12 | Concurrency | Row-level locking + Polling 10 giây + Push ngay sau khi ký (không chờ polling cycle). Mỗi người thao tác dòng riêng, không conflict. Khi 1 người ký xong → push cập nhật ngay cho các người khác đang mở phiếu. Người lập có quyền xóa bất kỳ dòng nào (kể cả đã ký). | 01, 04A, 04B |
| 13 | Kiểm tra quyền | Kiểm tra quyền người dùng trước khi cho phép chọn mẫu. VD: 04B chỉ hiển thị cho DBA. | Tất cả |
| 14 | Mẫu 01 - Quyền truy cập | Loại "Truy vấn" → chỉ SELECT (auto-checked, không cho bỏ). Loại "Chỉnh sửa" → multi-select từ SELECT/INSERT/UPDATE/DELETE (chọn ≥1). Khi đổi loại → cảnh báo reset quyền đã chọn. | 01-YCTC |
| 15 | Ký 1 lần cho tất cả dòng | 1 người có thể có nhiều dòng trên 1 phiếu (mỗi dòng 1 HT/CSDL khác nhau). Ký 1 lần → tất cả dòng của người đó tự động chuyển trạng thái "Đã ký". Không cho phép ký nhiều lần. | 01, 04A, 04B |
| 16 | Timeout PENDING_SIGN | Phiếu ở trạng thái PENDING_SIGN hết thời gian ca đã chọn (01-YCTC) hoặc hết ngày lập phiếu (04A-YCTK) → Hệ thống tự động chuyển trạng thái `CANCELLED` + Gửi email notification đến người lập thông báo phiếu đã hết hạn. Scheduler kiểm tra mỗi 5 phút. | 01-YCTC, 04A-YCTK |

## 9. Quy tắc nghiệp vụ chung

- Thời gian truy cập/truy xuất tự fill theo Ca, KHÔNG cho phép sửa (01, 05A, 05B).
- Thời gian cập nhật (02-YCCS): tự fill theo Ca, CHO PHÉP chỉnh sửa, KHÔNG được để trống.
- Ca truy cập: Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h).
- Ngày + Ca: Chỉ cho phép chọn (date picker / dropdown), KHÔNG cho nhập tay. Chỉ hiện tại hoặc tương lai, không cho phép quá khứ (trừ 04B, 05B).
- Cho phép lưu nháp và sửa lại phiếu nếu chưa gửi phê duyệt.
- Xử lý gửi thất bại:
  + Auto-save định kỳ 30 giây lên server (silent, dirty check).
  + Local draft (sessionStorage): lưu nội dung form mỗi khi thay đổi (không lưu chữ ký/file).
  + Khi gửi: Kiểm tra mạng → Kiểm tra session → Gửi. Nếu lỗi mạng: retry 3 lần × 5 giây (chỉ với network error). Nếu session hết hạn: dừng retry, thông báo đăng nhập lại.
  + Khi mất mạng hoàn toàn: thông báo lỗi, giữ form, chờ mạng phục hồi.
  + Khi đăng nhập lại: phát hiện local draft → hỏi khôi phục → fill form → người dùng review + gửi lại.
- Cho phép hủy yêu cầu nếu chưa được phê duyệt (không cần lý do).
- Sau khi gửi phê duyệt không được sửa nội dung.
- Mẫu 06-ĐKNS: không thuộc hệ thống.
- Mẫu 07-NKCV: tự sinh sau khi hoàn thành, không thuộc scope lập yêu cầu.
- Tất cả phần thuộc scope phê duyệt/thực hiện: hiển thị để trống, read-only trên form lập yêu cầu (bao gồm: ô ký phê duyệt, kết quả thực hiện, phần DBA ghi, phần thực hiện mở truy cập).
- Email notification: gửi tự động khi chuyển trạng thái.

## 10. Quy tắc riêng mẫu 01-YCTC

- Trường "Loại yêu cầu" (Truy vấn/Chỉnh sửa) áp dụng cho toàn bộ phiếu (tất cả dòng chi tiết).
- Khi đổi loại yêu cầu → cảnh báo "Thay đổi loại yêu cầu sẽ reset quyền truy cập đã chọn" → Confirm → Reset.
- Mục đích/Lý do nằm ở thông tin chung (1 trường duy nhất cho toàn bộ phiếu).
- Mẫu 01-YCTC cho phép yêu cầu nhiều HT + CSDL trên 1 phiếu (mỗi dòng chi tiết có thể chọn HT/CSDL khác nhau, nhưng chung 1 đơn vị chủ quản).
- Quyền truy cập: Multi-select Checkbox Group (không phải dropdown). Hiển thị trực quan 4 options.
- 1 người có thể có nhiều dòng (mỗi dòng 1 HT/CSDL khác nhau). Ký 1 lần → tất cả dòng của người đó tự động chuyển trạng thái "Đã ký".
- **Quyền xóa dòng:** Người lập phiếu được phép **xóa** bất kỳ dòng đăng ký chi tiết nào (kể cả dòng đã ký số).
- **Không cho sửa dòng đã ký:** Người lập phiếu **KHÔNG được chỉnh sửa** nội dung dòng dữ liệu đã ký số.
- Quy tắc xóa/sửa tương tự áp dụng cho mẫu 04A-YCTK.
- **Trường "Họ và tên" ở bảng chi tiết:**
  + Người lập phiếu: dropdown chọn từ danh sách toàn bộ REQUESTER cùng phòng. Có thể thêm nhiều dòng cho nhiều người khác nhau.
  + Người dùng khác (vào phiếu đang "Chờ ký xác nhận"): chỉ được thêm dòng cho bản thân (tự fill tên từ AD, không cho chọn người khác).

## 11. Quy tắc riêng mẫu 03-YCCT

- Loại yêu cầu: Checkbox — chọn ít nhất 1, có thể chọn nhiều hoặc cả 3 (Tạo mới / Thay đổi / Xóa). Khi chọn mục nào → hiển thị phần nội dung tương ứng.
- SQL Script là mục chung cho toàn bộ phiếu (1 file duy nhất, có thể bao gồm cả Tạo mới/Thay đổi/Xóa hoặc chỉ 1 phần trong 3). Định dạng tên file: `YYYYMMDD_BS_XXX.sql`. Chỉ cho phép upload 1 file duy nhất.
- Nếu có file SQL Script + checksum khớp → nội dung chi tiết các tab KHÔNG bắt buộc (có thể để trống).
- Nếu KHÔNG có file SQL Script → nội dung chi tiết của tất cả tab đã chọn PHẢI có dữ liệu:
  + Tab Tạo mới: chỉ cần ít nhất 1 mục con có dữ liệu (Table, Cấu trúc table, Index, Synonym, Tạo mới khác).
  + Tab Thay đổi: chỉ cần ít nhất 1 mục con có dữ liệu (Thêm cột, Sửa cột, Tạo lại index, Thay đổi khác).
  + Tab Xóa: trường "Nội dung lệnh xóa" phải có dữ liệu.
- Có phần nội dung DBA ghi để đánh giá tác động ảnh hưởng và hệ thống liên quan (hiển thị để trống, read-only — thuộc scope phê duyệt).

## 12. Giao diện mẫu 01-YCTC

### Thông tin chung

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Loại yêu cầu | Dropdown | ✅ | Truy vấn / Chỉnh sửa — áp dụng toàn bộ phiếu, ở đầu phiếu |
| Mã yêu cầu | Tự động | ✅ | Sinh khi gửi phê duyệt (không sinh khi lưu/chờ ký). Format: KýhiệuĐV_ddmmyyyy_Ca_Lần. Hiển thị "—" khi PENDING_SIGN |
| Ngày | Date picker | ✅ | Chỉ cho chọn (không cho nhập), chỉ hiện tại hoặc tương lai |
| Ca | Dropdown | ✅ | Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h). Validate: không cho chọn ngày + ca quá khứ |
| Tên đơn vị yêu cầu | Tự động | ✅ | Lấy từ thông tin user đăng nhập |
| Tên phòng hoặc tương đương | Tự động | ✅ | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu (Họ và tên) | Tự động | ✅ | Lấy từ thông tin user đăng nhập |
| ĐTDĐ | Tự động | ✅ | Lấy từ hồ sơ người dùng |
| Thời gian truy cập/truy xuất (Từ/Đến) | Tự động | ✅ | Fill theo ca đã chọn, KHÔNG cho phép sửa |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại (dd/MM/yyyy), KHÔNG cho phép sửa |
| Mục đích/Lý do yêu cầu truy cập | Nhập text | ✅ | 1 trường duy nhất cho toàn bộ phiếu (thông tin chung) |
| Ký tên (người lập) | Ký điện tử (OTP) | ✅ | Khi ký gửi phê duyệt. Sau khi ký hiển thị ảnh chữ ký |
| Danh sách Trưởng phòng/tương đương | Tự động | ✅ | Lấy theo người dùng |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only, chờ module phê duyệt |

### Thông tin chi tiết (bảng — mỗi dòng 1 người dùng)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Hệ thống thông tin | Dropdown | ✅ | Cho phép chọn nhiều HT khác nhau trên 1 phiếu (chung 1 đơn vị chủ quản) |
| CSDL | Dropdown | ✅ | Theo HT đã chọn |
| Tên đối tượng | Nhập text | ✅ | Bảng/đối tượng dữ liệu |
| Quyền truy cập | Multi-select Checkbox | ✅ | SELECT, INSERT, UPDATE, DELETE. Logic: Loại "Truy vấn" → chỉ SELECT (auto-checked); Loại "Chỉnh sửa" → chọn ≥1 |
| Họ và tên | Dropdown/Tự động | ✅ | Người lập phiếu: dropdown chọn từ danh sách REQUESTER cùng phòng (có thể thêm nhiều dòng cho nhiều người). Người dùng khác (vào phiếu "Chờ ký xác nhận"): chỉ được thêm dòng cho bản thân (tự fill tên, không cho chọn người khác) |
| Ký tên | Ký điện tử (OTP) | ✅ | 1 người có thể có nhiều dòng. Ký 1 lần → tất cả dòng của người đó tự động "Đã ký". Nếu người lập và người truy cập là một thì không cần ký tại mục chi tiết |

## 13. Giao diện mẫu 02-YCCS

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Dropdown | ✅ | Chọn 1 HT duy nhất, ở đầu phiếu |
| Tên CSDL | Dropdown | ✅ | Chọn 1 CSDL duy nhất, theo HT đã chọn |
| Mã yêu cầu | Tự động | ✅ | Sinh khi gửi phê duyệt. Format: KýhiệuĐV_ddmmyyyy_Ca_Lần |
| Ngày | Date picker | ✅ | Chỉ cho chọn (không cho nhập), chỉ hiện tại hoặc tương lai |
| Ca | Dropdown | ✅ | Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h). Validate: không cho chọn ngày + ca quá khứ |
| Tên đơn vị yêu cầu | Tự động | ✅ | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | ✅ | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | ✅ | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | ✅ | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại (dd/MM/yyyy), KHÔNG cho phép sửa |
| Thời gian cập nhật (Bắt đầu/Kết thúc) | Tự động + Cho sửa | ✅ | Tự fill theo ca, CHO PHÉP chỉnh sửa, KHÔNG được để trống |
| Tên tệp cần chạy | Upload file | ✅ | Định dạng: YYYYMMDD_BS_XXX.sql. Chỉ cho phép upload 1 file duy nhất (nếu nhiều file, người dùng phải tự gộp trước khi upload) |
| Loại checksum | Dropdown | ✅ | MD5 / SHA-256 |
| Mã kiểm tra tính toàn vẹn | Nhập text | ✅ | Hệ thống tự tính hash file → so sánh với mã người dùng nhập. MD5=32 hex, SHA-256=64 hex |
| Nội dung chỉnh sửa dữ liệu | Nhập text | ✅ | Mô tả chi tiết |
| Ký tên (người lập) | Ký điện tử (OTP) | ✅ | Sau khi ký hiển thị ảnh chữ ký |
| Danh sách Người kiểm tra của đơn vị chủ quản ứng dụng | Tự động | ✅ | Lấy theo tên hệ thống |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only, chờ module phê duyệt |
| Kết quả thực hiện | Hiển thị | — | Để trống, read-only (thuộc scope phê duyệt) |

## 14. Giao diện mẫu 03-YCCT

### Thông tin chung

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Dropdown | ✅ | Chọn 1 HT duy nhất, ở đầu phiếu |
| Tên CSDL | Dropdown | ✅ | Chọn 1 CSDL duy nhất |
| Mã yêu cầu | Tự động | ✅ | Sinh khi gửi phê duyệt. Format: KýhiệuĐV_ddmmyyyy_Ca_Lần |
| Ngày | Date picker | ✅ | Chỉ cho chọn (không cho nhập), chỉ hiện tại hoặc tương lai |
| Ca | Dropdown | ✅ | Ca 1 / Ca 2 / Ca 3. Validate: không cho chọn ngày + ca quá khứ |
| Tên đơn vị yêu cầu | Tự động | ✅ | |
| Tên phòng hoặc tương đương | Tự động | ✅ | |
| Người yêu cầu | Tự động | ✅ | |
| ĐTDĐ | Nhập/Tự động | ✅ | Số điện thoại liên hệ |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Ngày thực hiện dự kiến | Nhập (dd/MM/yyyy) | ❌ | Không bắt buộc |
| Loại yêu cầu | Checkbox | ✅ | Tạo mới / Thay đổi / Xóa (chọn ít nhất 1, có thể chọn nhiều hoặc cả 3) — quyết định tab/phần nào hiển thị |
| Đơn vị chủ quản ứng dụng | Nhập/Chọn | ✅ | |
| Đơn vị chủ quản quản trị CSDL | Nhập/Chọn | ✅ | |
| Ký tên (người lập) | Ký điện tử (OTP) | ✅ | |
| Danh sách Trưởng phòng/tương đương | Tự động | ✅ | |
| Phần DBA ghi (Đánh giá tác động) | Hiển thị | — | Để trống, read-only (thuộc scope phê duyệt) |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |
| Kết quả thực hiện | Hiển thị | — | Để trống, read-only (thuộc scope phê duyệt) |

### SQL Script (Mục chung — áp dụng cho toàn bộ phiếu)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên tệp SQL Script | Upload file | Có điều kiện | Định dạng: YYYYMMDD_BS_XXX.sql. Chỉ cho phép upload 1 file duy nhất. File script có thể bao gồm toàn bộ nội dung Tạo mới/Thay đổi/Xóa hoặc chỉ 1 phần trong 3 |
| Loại checksum | Dropdown | Có điều kiện | MD5 / SHA-256. Bắt buộc nếu có file |
| Mã kiểm tra (Checksum) | Nhập text | Có điều kiện | Bắt buộc nếu có file. Hệ thống tự tính hash → so sánh |

**Ràng buộc:**
- Nếu **có file SQL Script** → checksum phải khớp. Nội dung chi tiết các tab bên dưới **KHÔNG bắt buộc**.
- Nếu **KHÔNG có file SQL Script** → nội dung chi tiết của **tất cả tab đã chọn** PHẢI có dữ liệu.

### Tab Tạo mới (hiển thị khi chọn "Tạo mới")

| Mục | Trường | Bắt buộc | Ghi chú |
|---|---|---|---|
| Table | Owner, Table name, dự kiến tăng trưởng, vòng đời lưu trữ, cột xác định vòng đời, đối tượng phụ thuộc | Có điều kiện | Ít nhất 1 mục con trong tab phải có dữ liệu (nếu không có SQL Script) |
| Cấu trúc table | Tên bảng, tên cột, kiểu dữ liệu, cho phép Null (Y/N), giá trị mặc định, mô tả | Có điều kiện | |
| Index | Owner, tên index, table owner, tên bảng, danh sách cột đánh chỉ mục | Có điều kiện | |
| Synonym | Tên synonym, kiểu Public/Private, table owner, tên bảng, mô tả | Có điều kiện | |
| Tạo mới khác | Owner, tên, kiểu, mô tả | Có điều kiện | |

### Tab Thay đổi (hiển thị khi chọn "Thay đổi")

| Mục | Trường | Bắt buộc | Ghi chú |
|---|---|---|---|
| Thêm cột bảng | Owner, tên bảng, tên cột, loại dữ liệu, mô tả | Có điều kiện | Ít nhất 1 mục con trong tab phải có dữ liệu (nếu không có SQL Script) |
| Sửa cột bảng | Owner, tên bảng, tên cột, giá trị cũ, giá trị mới, mô tả | Có điều kiện | |
| Tạo lại index | Owner, tên bảng, tên index cũ, cột trong index, index mới, cột đánh index mới | Có điều kiện | |
| Thay đổi khác | Owner, tên, kiểu, mô tả | Có điều kiện | |

### Tab Xóa (hiển thị khi chọn "Xóa")

| Mục | Trường | Bắt buộc | Ghi chú |
|---|---|---|---|
| Nội dung lệnh xóa | Nhập text (textarea) | Có điều kiện | Bắt buộc nếu không có SQL Script. Mô tả chi tiết đối tượng cần xóa và lệnh xóa |

## 15. Giao diện mẫu 04A-YCTK

### Thông tin chung

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Dropdown | ✅ | Chọn 1 HT duy nhất, ở đầu phiếu (thông tin chung) |
| Tên CSDL | Dropdown | ✅ | Chọn 1 CSDL duy nhất (thông tin chung) |
| Mã yêu cầu | Tự động | ✅ | Sinh khi gửi phê duyệt. Format: KýhiệuĐV_ddmmyyyy_Ca_Lần. Hiển thị "—" khi PENDING_SIGN |
| Ngày | Date picker | ✅ | Chỉ cho chọn (không cho nhập), chỉ hiện tại hoặc tương lai |
| Ca | Dropdown | ✅ | Ca 1 / Ca 2 / Ca 3. Validate: không cho chọn ngày + ca quá khứ |
| Tên đơn vị yêu cầu | Tự động | ✅ | |
| Tên phòng hoặc tương đương | Tự động | ✅ | |
| Người yêu cầu | Tự động | ✅ | |
| ĐTDĐ | Tự động | ✅ | |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Thời gian sử dụng (Bắt đầu/Kết thúc) | Nhập | ❌ | Không bắt buộc, cho phép để trống |
| Lý do yêu cầu tạo mới/thay đổi thuộc tính | Nhập text | ✅ | |
| Nội dung yêu cầu | Nhập text | ❌ | Mô tả chi tiết (không bắt buộc) |
| Ký tên (người lập) | Ký điện tử (OTP) | ✅ | |
| Danh sách Trưởng phòng/tương đương | Tự động | ✅ | |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |

### Thông tin chi tiết tài khoản (bảng — mỗi dòng 1 người dùng)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Họ tên chủ tài khoản | Dropdown/Tự động | ✅ | Người lập: dropdown chọn REQUESTER cùng phòng. Người khác: tự fill tên bản thân |
| Loại tài khoản | Dropdown | ✅ | Truy vấn / Chỉnh sửa — mỗi dòng tự chọn riêng |
| Hình thức | Dropdown | ✅ | Cấp mới / Đổi thuộc tính — mỗi dòng tự chọn riêng |
| Ký tên | Ký điện tử (OTP) | ✅ | 1 người có thể có nhiều dòng. Ký 1 lần → tất cả dòng tự "Đã ký". Nếu người lập và người truy cập là một thì không cần ký tại mục chi tiết |

**Lưu ý:** Logic ký tương tự mẫu 01-YCTC (Nhánh A/B, row-level locking, polling 10 giây + push sau ký). Người lập được xóa bất kỳ dòng nào (kể cả đã ký), không được sửa dòng đã ký.

## 16. Giao diện mẫu 04B-BGTK

**Điều kiện hiển thị:** Chỉ hiển thị đối với DBA. REQUESTER mặc định KHÔNG hiển thị 04B.

### Màn hình chọn phiếu nợ

| Trường | Loại | Ghi chú |
|---|---|---|
| Danh sách 04A đang nợ | Danh sách chọn | Hiển thị các phiếu 04A hoàn thành, có ít nhất 1 dòng "Cấp mới", chưa có 04B |

### Form nhập liệu (sau khi chọn phiếu)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Tự động | ✅ | Fill từ 04A, read-only |
| Tên CSDL | Tự động | ✅ | Fill từ 04A, read-only |
| Mã yêu cầu 04B | Tự động | ✅ | Sinh khi gửi. Format: [Mã 04A]_04B_V[xx]. VD: CNTT-NHDT_09072026_02_03_04B_V01 |
| Mã yêu cầu 04A liên quan | Tự động | ✅ | Fill từ 04A, read-only |
| Thời gian bàn giao | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Địa điểm | Nhập text | ✅ | DBA nhập |
| Đại diện BP quản trị CSDL (Cấp QL) | Tự động | ✅ | Lấy từ cấu hình |
| Người bàn giao (DBA) | Tự động | ✅ | User đăng nhập |
| Đại diện BP nhận bàn giao (Cấp QL) | Tự động | ✅ | Lãnh đạo phòng người yêu cầu (từ 04A) |
| Người nhận bàn giao | Tự động | ✅ | Danh sách người dùng từ 04A (chỉ những người có Hình thức = "Cấp mới") |
| Ký tên DBA (Người bàn giao) | Ký điện tử (OTP) | ✅ | DBA ký |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |

### Chi tiết bàn giao (bảng)

**Lưu ý:** Chỉ hiển thị các dòng có Hình thức = "Cấp mới" từ 04A. Các dòng "Đổi thuộc tính" KHÔNG hiển thị trong 04B.

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tài khoản (UserID) | Nhập text | ✅ | DBA nhập tay — KHÔNG tự fill |
| Loại tài khoản (QUERY/UPDATE) | Tự động | ✅ | Fill từ 04A |
| Phạm vi | Nhập text | ✅ | DBA tự nhập (KHÔNG fill từ 04A) |
| Nội dung | Tự động | ✅ | Fill từ trường "Hình thức" của 04A (= "Cấp mới") |
| Chủ tài khoản | Tự động | ✅ | Fill từ 04A |
| Ký nhận (Người dùng) | Ký điện tử (OTP) | ✅ | Mỗi người ký dòng mình (sau khi lãnh đạo DBA duyệt, trạng thái "Chờ ký nhận") |

## 17. Giao diện mẫu 05A-YCKC

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Dropdown | ✅ | Chọn 1 HT duy nhất |
| Tên CSDL | Dropdown | ✅ | Chọn 1 CSDL duy nhất |
| Mã yêu cầu | Tự động | ✅ | Sinh khi gửi phê duyệt. Format: KýhiệuĐV_ddmmyyyy_Ca_Lần |
| Ngày lập yêu cầu | Date picker | ✅ | Auto fill ngày hiện tại; chỉ cho pick ngày hiện tại hoặc tương lai; không cho phép nhập tay. Mã yêu cầu sử dụng trường này |
| Ca | Dropdown | ✅ | Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h). Mặc định fill ca hiện tại. Validate: không cho chọn ngày + ca quá khứ |
| Tên đơn vị yêu cầu | Tự động | ✅ | |
| Tên phòng hoặc tương đương | Tự động | ✅ | |
| Người yêu cầu | Tự động | ✅ | |
| ĐTDĐ | Tự động | ✅ | |
| Thời gian yêu cầu (Từ/Đến) | Tự động | ✅ | Fill theo ca, KHÔNG cho phép sửa |
| Mục đích/Lý do yêu cầu truy cập, truy xuất | Nhập text | ✅ | Bắt buộc |
| Quyền trên đối tượng dữ liệu | Chọn/Nhập | ✅ | Checkbox "Query all data only": Nếu tích → lưu giá trị = "Query all data only", danh sách bảng KHÔNG bắt buộc. Nếu không tích → phải nhập ít nhất 1 dòng chi tiết (Owner, Table name, Select/Insert/Update/Delete) |
| Ký tên (người lập) | Ký điện tử (OTP) | ✅ | |
| Phần "Thực hiện mở truy cập" | Hiển thị | — | Để trống, read-only. Gồm: Thời gian mở TC, Họ và tên, Ký tên, Lý do không thực hiện. Thuộc scope phê duyệt/thực hiện |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |

## 18. Giao diện mẫu 05B-HTKC

### Màn hình chọn trường hợp cần bổ sung

| Trường | Loại | Ghi chú |
|---|---|---|
| Danh sách cần bổ sung 05B | Danh sách | Hệ thống gộp tự động các 05A chung HT + CSDL + Ngày + Ca thành 1 mục. Người dùng chọn mục, không cần chọn từng phiếu |

### Form nhập liệu (sau khi chọn)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Tự động | ✅ | Fill từ 05A, read-only |
| Tên CSDL | Tự động | ✅ | Fill từ 05A, read-only |
| Mã yêu cầu 05B | Tự động | ✅ | Sinh khi gửi. Format: [Mã ĐV]_[ddmmyyyy]_[Ca]_[Lần ghép]_V[xx]. VD: CNTT-NHDT_09072026_02_0103_V01 |
| Phiếu 05A liên quan | Tự động | ✅ | Hiển thị danh sách mã 05A liên quan (từ bảng mapping) |
| Ca | Tự động | ✅ | Fill từ 05A, read-only |
| Lần (hiển thị) | Tự động | — | Hiển thị tổng hợp: "Lần: 01, 02, 03" — chỉ là hiển thị, lấy từ trường "Lần" của các phiếu 05A liên quan |
| Tên đơn vị yêu cầu | Tự động | ✅ | |
| Tên phòng hoặc tương đương | Tự động | ✅ | |
| Người yêu cầu | Tự động | ✅ | |
| ĐTDĐ | Tự động | ✅ | |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Thời gian yêu cầu (Từ/Đến) | Tự động | ✅ | Fill từ 05A, read-only |
| Danh sách bảng đã yêu cầu | Tự động | ✅ | Union tất cả bảng từ các phiếu 05A trong ca. Cột: Owner, Table name, Select/Insert/Update/Delete |
| Mục đích truy cập, truy xuất (mô tả chi tiết, câu lệnh thực hiện) | Nhập text | ✅ | Bắt buộc — người lập nhập nội dung công việc đã thực hiện |
| Ký tên (người lập) | Ký điện tử (OTP) | ✅ | |
| Xác nhận (ĐV chủ quản ứng dụng + ĐV yêu cầu) | Hiển thị | — | Để trống, read-only (thuộc scope phê duyệt). Hệ thống tự xác định: người lập thuộc ĐV chủ quản → gửi lãnh đạo ĐV chủ quản; không thuộc → gửi lãnh đạo ĐV yêu cầu |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |

## 19. Chức năng con: Đăng ký trước Yêu cầu chi tiết (Mẫu 01-YCTC)

### 19.1 Mô tả chức năng

Cho phép người dùng có quyền lập yêu cầu (REQUESTER) đăng ký trước thông tin chi tiết truy cập CSDL cho ngày + ca hiện tại hoặc tương lai. Khi người lập phiếu 01-YCTC chọn ca, hệ thống tự động nạp toàn bộ đăng ký trước phù hợp của tất cả người dùng cùng đơn vị vào bảng chi tiết phiếu.

**Lợi ích:**
- Tiết kiệm thời gian lập phiếu.
- Người dùng chủ động đăng ký trước khi cần truy cập.
- Giảm sai sót do nhập liệu thủ công.

### 19.2 Vị trí giao diện

- Button **"Đăng ký trước chi tiết"** nằm bên trong Card "Lập yêu cầu mẫu 01-YCTC" tại màn hình chọn mẫu.
- Đây là chức năng con thuộc "Lập yêu cầu mẫu 01-YCTC", không phải chức năng riêng biệt.
- Card "Lập yêu cầu mẫu 01-YCTC" chứa:
  + Button chính: **"Lập yêu cầu"** (vào form lập phiếu 01).
  + Button phụ: **"Đăng ký trước chi tiết"** (vào trang đăng ký trước).
- Khi click "Đăng ký trước chi tiết" → điều hướng sang trang đăng ký trước.

### 19.3 Quyền truy cập

- Bất kỳ người dùng nào có role REQUESTER đều có chức năng này.

### 19.4 Giao diện trang "Đăng ký trước Yêu cầu chi tiết"

#### 19.4.1 Danh sách đăng ký đã lưu

Hiển thị danh sách các đăng ký đã lưu **của chính người dùng đang truy cập**. Phân trang 20 bản ghi/trang.

| Cột hiển thị | Ghi chú |
|---|---|
| Ngày đăng ký | dd/MM/yyyy |
| Ca | Ca 1 / Ca 2 / Ca 3 |
| Loại yêu cầu | Truy vấn / Chỉnh sửa |
| Hệ thống thông tin | |
| CSDL | |
| Bảng/Đối tượng | |
| Quyền | SELECT, INSERT, UPDATE, DELETE |
| Trạng thái | Chưa dùng / Chờ duyệt / Đã dùng / Hết hạn |
| Thời điểm ký | dd/MM/yyyy HH:mm |
| Hành động | Sửa / Xóa (chỉ khi trạng thái "Chưa dùng") |

#### 19.4.2 Form đăng ký mới

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Loại yêu cầu | Dropdown | ✅ | Truy vấn / Chỉnh sửa — logic tương tự mẫu 01 |
| Ngày đăng ký | Date picker | ✅ | Chỉ cho phép hiện tại hoặc tương lai (dd/MM/yyyy), chỉ cho chọn không cho nhập |
| Ca | Dropdown | ✅ | Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h) |
| Hệ thống thông tin | Dropdown | ✅ | Chọn từ danh mục |
| CSDL | Dropdown | ✅ | Theo HT đã chọn |
| Bảng/Đối tượng | Nhập text | ✅ | |
| Quyền | Multi-select Checkbox | ✅ | Logic: Loại "Truy vấn" → chỉ SELECT (auto-checked); Loại "Chỉnh sửa" → chọn ≥1 từ SELECT/INSERT/UPDATE/DELETE |

**Chức năng bổ sung:**
- Nút **"Thêm dòng"**: Thêm dòng đăng ký mới (cùng form, nhiều dòng).
- Nút **"Nhân bản"**: Copy dòng hiện tại sang ngày/ca khác (người dùng chọn ngày + ca đích).
- Nút **"Lưu"**: Lưu tất cả dòng + Ký số OTP xác nhận → Lưu vào CSDL.

#### 19.4.3 Form sửa đăng ký

- Chỉ cho phép sửa khi trạng thái = "Chưa dùng".
- Sau khi sửa → **yêu cầu ký số OTP lại** (vì nội dung đã thay đổi).
- Cập nhật trường `signed_at` và `signature` mới.

#### 19.4.4 Xóa đăng ký

- Chỉ cho phép xóa khi trạng thái = "Chưa dùng".
- Xóa vĩnh viễn (hard delete) khỏi CSDL.
- Không cho phép xóa ở các trạng thái khác.

### 19.5 Cấu trúc bảng CSDL

**Tên bảng:** `pre_registration_request`

| Cột | Kiểu | Mô tả |
|---|---|---|
| id | BIGINT (PK) | Auto-increment |
| user_id | VARCHAR | Mã người dùng (từ AD) |
| user_name | VARCHAR | Họ tên |
| unit_code | VARCHAR | Mã đơn vị |
| department | VARCHAR | Phòng/ban |
| register_date | DATE | Ngày đăng ký (ngày truy cập) |
| shift | INT | Ca (1/2/3) |
| request_type | VARCHAR | Loại yêu cầu (Truy vấn/Chỉnh sửa) |
| system_name | VARCHAR | Tên hệ thống |
| database_name | VARCHAR | Tên CSDL |
| object_name | VARCHAR | Bảng/Đối tượng |
| access_rights | VARCHAR | Quyền (SELECT, INSERT, UPDATE, DELETE) |
| signature | TEXT | Chữ ký số |
| signed_at | TIMESTAMP | Thời điểm ký |
| status | VARCHAR | Chưa dùng / Chờ duyệt / Đã dùng / Hết hạn |
| request_id | VARCHAR (nullable) | Mã phiếu 01 đã nạp (nếu có) |
| created_at | TIMESTAMP | Thời điểm tạo |
| updated_at | TIMESTAMP | Thời điểm cập nhật |

**Index:**
- `idx_pre_reg_unit_date_shift` ON (unit_code, register_date, shift, status) — phục vụ truy vấn nạp tự động.
- `idx_pre_reg_user` ON (user_id, status) — phục vụ hiển thị danh sách cá nhân.

### 19.6 Bảng tham chiếu 05B → 05A

**Tên bảng:** `request_05b_05a_mapping`

| Cột | Kiểu | Mô tả |
|---|---|---|
| id | BIGINT (PK) | Auto-increment |
| request_05b_id | VARCHAR | Mã phiếu 05B |
| request_05a_id | VARCHAR | Mã phiếu 05A liên quan |
| created_at | TIMESTAMP | Thời điểm tạo |

**Quan hệ:** 1 phiếu 05B → nhiều phiếu 05A (1-N).

### 19.7 Hệ thống trạng thái bản ghi đăng ký trước

| Trạng thái | Mô tả | Chuyển tiếp |
|---|---|---|
| Chưa dùng | Đăng ký mới, chưa được nạp vào phiếu nào | → Chờ duyệt / Hết hạn |
| Chờ duyệt | Đã nạp vào phiếu 01, người lập đã ký gửi phê duyệt | → Đã dùng / Chưa dùng (nếu phiếu bị hủy) |
| Đã dùng | Phiếu 01 hoàn thành mở truy cập thành công | Kết thúc |
| Hết hạn | Ngày + ca đã qua mà chưa được nạp | Kết thúc |

**Logic chuyển trạng thái:**
- Chưa dùng → **Chờ duyệt**: Khi phiếu 01 được người lập ký số gửi phê duyệt.
- Chờ duyệt → **Đã dùng**: Khi phiếu 01 hoàn thành mở truy cập.
- Chờ duyệt → **Chưa dùng**: Khi phiếu 01 bị hủy.
- Chưa dùng → **Hết hạn**: Khi ngày + ca đã qua (cron job tự động kiểm tra).

### 19.8 Logic nạp tự động vào phiếu 01-YCTC

**Trigger:** Khi người lập phiếu 01-YCTC chọn "Ca" (sau khi đã chọn "Loại yêu cầu" và "Ngày").

**Quy trình:**
1. Hệ thống truy vấn bảng `pre_registration_request` với điều kiện:
   - `unit_code` = đơn vị của người lập.
   - `register_date` = ngày đã chọn.
   - `shift` = ca đã chọn.
   - `status` = "Chưa dùng".
   - Logic loại yêu cầu:
     - Nếu người lập chọn **"Truy vấn"** → chỉ nạp bản ghi có `request_type` = "Truy vấn".
     - Nếu người lập chọn **"Chỉnh sửa"** → nạp toàn bộ bản ghi phù hợp (cả Truy vấn lẫn Chỉnh sửa).
2. Nạp tất cả bản ghi phù hợp vào bảng chi tiết phiếu 01.
3. Các dòng đã nạp hiển thị:
   - Trạng thái **"Đã ký"** (tự fill chữ ký từ bản ghi đăng ký trước).
   - **Không cho phép sửa** trên phiếu (vì đã ký số từ trước).
   - Người lập **được phép xóa** dòng đã nạp (quyền xóa bất kỳ dòng nào).
4. Người lập vẫn có thể thêm dòng mới (nhập thủ công) bên cạnh các dòng đã nạp.

**Xử lý khi đổi "Loại yêu cầu":**
- Nếu đổi từ "Chỉnh sửa" → "Truy vấn": Các dòng đăng ký trước có quyền INSERT/UPDATE/DELETE tự động bị xóa khỏi phiếu. Bản ghi đăng ký trước giữ nguyên trạng thái "Chưa dùng".
- Cảnh báo: "Thay đổi loại yêu cầu sẽ xóa các dòng đăng ký trước không phù hợp khỏi phiếu" → Confirm → Thực hiện.

**Xử lý khi đổi Ngày hoặc Ca (sau khi đã nạp):**
- Hệ thống xóa toàn bộ các dòng đã nạp từ đăng ký trước.
- Nạp lại theo ngày/ca mới (cùng logic nạp ban đầu).
- Các dòng nhập thủ công (không phải từ đăng ký trước) giữ nguyên.
- Cảnh báo: "Thay đổi Ngày/Ca sẽ xóa các dòng đăng ký trước đã nạp và nạp lại theo Ngày/Ca mới" → Confirm → Thực hiện.

### 19.9 Validation Rules bổ sung

| # | Rule | Chi tiết | Áp dụng |
|---|---|---|---|
| 16 | Không đăng ký quá khứ | Ngày + Ca: chỉ cho phép hiện tại hoặc tương lai, chỉ cho chọn không cho nhập | Đăng ký trước |
| 17 | Kiểm tra trùng lặp đăng ký | Chặn khi trùng: user_id + register_date + shift + system_name + database_name + object_name + access_rights | Đăng ký trước |
| 18 | Chỉ sửa/xóa khi "Chưa dùng" | Không cho phép sửa/xóa bản ghi ở trạng thái khác "Chưa dùng" | Đăng ký trước |
| 19 | Ký số lại khi sửa | Sau khi sửa nội dung đăng ký → yêu cầu ký OTP lại | Đăng ký trước |
| 20 | Xóa vĩnh viễn | Xóa bản ghi khỏi CSDL (hard delete), chỉ khi status = "Chưa dùng" | Đăng ký trước |
| 21 | Hết hạn tự động | Cron job kiểm tra: register_date + shift đã qua → chuyển status = "Hết hạn" | Đăng ký trước |
| 22 | Nạp tự động | Chỉ nạp bản ghi status = "Chưa dùng". Không nạp "Chờ duyệt", "Đã dùng", "Hết hạn" | Phiếu 01 |

### 19.10 Verification bổ sung

- [ ] Đăng ký trước thành công + ký OTP.
- [ ] Đăng ký cho ngày/ca tương lai.
- [ ] Chặn đăng ký ngày/ca quá khứ.
- [ ] Chặn đăng ký trùng lặp.
- [ ] Nhân bản dòng sang ngày/ca khác.
- [ ] Sửa đăng ký "Chưa dùng" → yêu cầu ký lại.
- [ ] Xóa đăng ký "Chưa dùng" → xóa vĩnh viễn.
- [ ] Không cho sửa/xóa khi trạng thái khác "Chưa dùng".
- [ ] Nạp tự động khi lập phiếu 01 chọn ca: đúng đơn vị + ngày + ca + loại yêu cầu.
- [ ] Dòng nạp hiển thị "Đã ký", không cho sửa trên phiếu, cho phép xóa.
- [ ] Đổi loại yêu cầu "Chỉnh sửa" → "Truy vấn": xóa dòng không phù hợp, bản ghi giữ "Chưa dùng".
- [ ] Đổi Ngày/Ca sau khi nạp: xóa toàn bộ dòng đăng ký trước + nạp lại, dòng thủ công giữ nguyên.
- [ ] Phiếu 01 gửi phê duyệt → bản ghi chuyển "Chờ duyệt".
- [ ] Phiếu 01 hoàn thành mở truy cập → bản ghi chuyển "Đã dùng".
- [ ] Phiếu 01 bị hủy → bản ghi quay lại "Chưa dùng".
- [ ] Cron job: bản ghi hết hạn tự động chuyển "Hết hạn".
- [ ] Danh sách đăng ký hiển thị đúng (chỉ của người dùng đang truy cập).

---

## 20. Allowed Files

- `src/main/java/.../request/**`
- `src/main/java/.../workflow/RequestSubmissionService.java`
- `src/main/resources/templates/requests/**`
- `src/main/resources/static/js/requests/**`
- `src/main/resources/static/css/requests/**`
- `src/test/java/.../request/**`

## 21. Must Not Change

- Không sửa màn hình Dashboard ngoài link/nút cần thiết.
- Không sửa service AD/Email/OTP ngoài interface đã thống nhất.
- Không sửa xử lý phê duyệt sau khi yêu cầu đã gửi, trừ phần khởi tạo bước đầu.

## 22. Verification

- [ ] Lưu nháp từng mẫu phiếu.
- [ ] Ký xác nhận thành công bằng OTP.
- [ ] Gửi phiếu 01/04A với nhiều dòng chi tiết và nhiều người ký.
- [ ] Chặn gửi nếu thiếu chữ ký người dùng chung phiếu.
- [ ] Chặn gửi nếu không có dòng chi tiết với 01/04A.
- [ ] Chặn nợ 05B: (a) quá 3 ngày → chặn lập yêu cầu; (b) validate lúc ký số trên phiếu 01 chung; (c) validate toàn bộ người dùng lúc gửi phê duyệt (bao gồm đăng ký trước). Thông báo đúng + link đến 05B.
- [ ] Người lập xóa dòng đã ký của người nợ 05B → gửi phiếu thành công.
- [ ] Mẫu 05A sau gửi vào trạng thái "Đã chuyển bộ phận Mở truy cập".
- [ ] File SQL 02-YCCS kiểm tra đúng định dạng tên (YYYYMMDD_BS_XXX.sql).
- [ ] File SQL 03-YCCT kiểm tra đúng định dạng tên (YYYYMMDD_BS_XXX.sql).
- [ ] Upload file SQL: chỉ cho phép 1 file duy nhất (cả 02 và 03).
- [ ] Lập 04B-BGTK từ 04A đã hoàn thành: chỉ nạp dòng "Cấp mới", kiểm tra auto-fill đúng (Nội dung = "Cấp mới", Phạm vi = DBA nhập).
- [ ] 04B không hiển thị dòng "Đổi thuộc tính" từ 04A.
- [ ] Lập 05B-HTKC, kiểm tra gộp tự động các 05A chung HT+CSDL+Ngày+Ca.
- [ ] Mã 05B sinh riêng, hiển thị "Lần" tổng hợp từ 05A liên quan.
- [ ] Bảng mapping request_05b_05a_mapping lưu đúng quan hệ 1-N.
- [ ] Timeout 04B-BGTK: 3 ngày → email notification gửi đúng người.
- [ ] Checksum file SQL: match → OK; không match → chặn gửi.
- [ ] Trùng lặp 01 (4 trường): chặn đúng.
- [ ] Trùng lặp 04A (người dùng): chặn đúng.
- [ ] Concurrency: nhiều người ký đồng thời trên 01/04A/04B → không conflict. Polling 10 giây + push sau ký.
- [ ] Kiểm tra quyền: 04B chỉ hiển thị cho DBA, REQUESTER không thấy 04B.
- [ ] Chặn chọn ngày + ca quá khứ (trừ 04B, 05B). Chỉ cho chọn, không cho nhập.
- [ ] Mẫu 01: logic Truy vấn → chỉ SELECT; Chỉnh sửa → multi-select.
- [ ] Đổi loại yêu cầu 01 → cảnh báo reset → confirm → reset đúng.
- [ ] 04B: tất cả người dùng ký nhận → tự động chuyển "Chờ phê duyệt" lần 2.
- [ ] Mẫu 02: Thời gian cập nhật tự fill theo ca, cho phép sửa, không được để trống.
- [ ] Tất cả phần read-only hiển thị đúng, không cho nhập.
- [ ] Phiếu bị từ chối → trạng thái REJECTED (có lý do), không cho sửa, phải lập mới.
- [ ] Timeout PENDING_SIGN (01-YCTC): phiếu hết ca → tự động CANCELLED + email đến người lập.
- [ ] Timeout PENDING_SIGN (04A-YCTK): phiếu hết ngày → tự động CANCELLED + email đến người lập.
- [ ] Phiếu PENDING_SIGN/DRAFT: cột "Mã yêu cầu" hiển thị "—", tìm kiếm qua metadata.
- [ ] Sinh mã chỉ khi gửi phê duyệt, mã format đúng, unique toàn hệ thống.
- [ ] Mã 04B format đúng: [Mã 04A]_04B_V[xx]. Hủy + lập lại → tăng version.
- [ ] Mã 05B format đúng: [Mã ĐV]_[ddmmyyyy]_[Ca]_[Lần ghép]_V[xx]. Hủy + lập lại → tăng version.
- [ ] Mẫu 03 Tab Tạo mới: không có SQL Script → ít nhất 1 mục con có dữ liệu → OK.
- [ ] Mẫu 03 Tab Thay đổi: không có SQL Script → ít nhất 1 mục con có dữ liệu → OK.
- [ ] Mẫu 03 Tab Xóa: không có SQL Script → "Nội dung lệnh xóa" phải có dữ liệu.
- [ ] Auto-save định kỳ 30 giây lên server (silent, dirty check) hoạt động đúng.
- [ ] Local draft (sessionStorage): lưu nội dung form mỗi khi thay đổi.
- [ ] Mất mạng hoàn toàn: thông báo lỗi, giữ form, local draft còn nguyên.
- [ ] Session hết hạn: dừng retry, thông báo đăng nhập lại, dữ liệu đã lưu.
- [ ] Đăng nhập lại sau mất mạng: phát hiện local draft → hỏi khôi phục → fill form đúng.
- [ ] Retry chỉ với lỗi mạng (network error), KHÔNG retry khi 401/403/400/422.
- [ ] Khởi tạo workflow đúng khi gửi phê duyệt/kiểm tra (variant I/E, step code, actor).
- [ ] Mẫu 05A: tích "Query all data only" → danh sách bảng không bắt buộc.
- [ ] Mẫu 05A: không tích → phải nhập ít nhất 1 dòng chi tiết.
- [ ] Mẫu 05B: gửi email đúng (thuộc ĐV chủ quản → lãnh đạo ĐV chủ quản; không thuộc → lãnh đạo ĐV yêu cầu).
- [ ] Người lập được xóa bất kỳ dòng nào (kể cả đã ký) trên phiếu 01/04A.
- [ ] Người lập KHÔNG được sửa dòng đã ký trên phiếu 01/04A.
- [ ] Validate Nhánh A: cho phép lưu 0 dòng, validate đầy đủ chỉ khi gửi phê duyệt.
- [ ] Rule #15: 1 người nhiều dòng, ký 1 lần → tất cả dòng tự "Đã ký".
- [ ] Mã yêu cầu format đúng: KýhiệuĐV_ddmmyyyy_Ca_Lần. Unique toàn hệ thống.
- [ ] Trường "Lần" tự tăng theo Ngày + Ca toàn hệ thống.
- [ ] Mẫu 05A: trường "Ngày lập yêu cầu" auto fill ngày hiện tại, cho pick ngày hiện tại/tương lai, không cho nhập tay.
- [ ] Trường "Họ và tên" mẫu 01: người lập = dropdown REQUESTER cùng phòng; người khác = tự fill bản thân.
- [ ] Khi gửi phê duyệt phiếu 01/04A: dòng chưa ký (người dùng khác người lập) tự động bị xóa.

## 23. Definition of Done

- Hoàn thành form cho **7 mẫu phiếu** (01-YCTC, 02-YCCS, 03-YCCT, 04A-YCTK, 04B-BGTK, 05A-YCKC, 05B-HTKC).
- Hoàn thành chức năng con "Đăng ký trước Yêu cầu chi tiết" cho mẫu 01-YCTC.
- Có lưu nháp, sửa nháp, ký, gửi, hủy, gửi lại.
- Có validate nghiệp vụ và validate giao diện.
- Có test cho các luồng chính và lỗi nghiệp vụ quan trọng.
- Khởi tạo workflow thành công khi gửi phê duyệt/kiểm tra.
- Mã yêu cầu sinh đúng format, không trùng lặp, chỉ sinh khi gửi phê duyệt.
- Mã 04B, 05B sinh đúng format với version.
- Timeout PENDING_SIGN tự động CANCELLED + email hoạt động.
- Email notification gửi đúng người, đúng thời điểm.

---

## 24. Email Templates

| # | Sự kiện | Subject | Body (tóm tắt) |
|---|---|---|---|
| 1 | Gửi phê duyệt | [DB Access] Yêu cầu chờ phê duyệt - [Mã YC] | Yêu cầu [Mã YC] loại [Tên mẫu] do [Tên người lập] - [Phòng ban] lập ngày [Ngày lập] đang chờ phê duyệt. |
| 2 | Bị từ chối | [DB Access] Yêu cầu bị từ chối - [Mã YC] | Yêu cầu [Mã YC] đã bị từ chối. Lý do: [Lý do]. |
| 3 | Chờ ký xác nhận | [DB Access] Phiếu chờ ký xác nhận - [Loại mẫu] | Có phiếu [Loại mẫu] do [Tên người lập] lập ngày [Ngày lập] đang chờ ký xác nhận của bạn. |
| 4 | Phiếu hết ca (CANCELLED) | [DB Access] Phiếu đã hết hạn - [Loại mẫu] | Phiếu [Loại mẫu] lập ngày [Ngày lập] ca [Ca] đã hết hạn và chuyển trạng thái HỦY. |
| 5 | Nhắc nhở 05B quá hạn | [DB Access] Nhắc nhở hoàn tất 05B - [Mã 05A] | Yêu cầu [Mã 05A] đã hoàn thành quá [Số ngày] ngày. Vui lòng hoàn tất 05B. |
| 6 | Timeout 04B ký nhận | [DB Access] Nhắc nhở ký nhận 04B - [Mã 04B] | Phiếu [Mã 04B] đã chờ ký nhận quá 3 ngày. Vui lòng đăng nhập ký nhận. |

---

## 25. Phạm vi

### 25.1 Trong scope

- Lập mới yêu cầu (7 loại mẫu: 01-YCTC, 02-YCCS, 03-YCCT, 04A-YCTK, 04B-BGTK, 05A-YCKC, 05B-HTKC)
- Lưu nháp (DRAFT)
- Lưu chờ ký chung (PENDING_SIGN) — áp dụng cho mẫu 01-YCTC, 04A-YCTK
- Gửi phê duyệt / Gửi kiểm tra / Gửi BP Mở truy cập
- Đăng ký trước yêu cầu chi tiết (Mẫu 01)
- Sinh mã yêu cầu tự động (khi gửi phê duyệt)
- Khởi tạo workflow (set variant, step_code, resolveNextActor)
- Email notification khi chuyển trạng thái
- Timeout PENDING_SIGN → CANCELLED

### 25.2 Ngoài scope

- Phê duyệt yêu cầu (module riêng)
- Ký xác nhận / chữ ký điện tử (module riêng)
- Quản lý danh mục HT/CSDL (module riêng)
- Mẫu 06-ĐKNS (đăng ký thành viên — ngoài hệ thống)
- Mẫu 07-NKCV (nhật ký công việc — tự sinh)
- Xử lý phê duyệt sau khi yêu cầu đã gửi (trạng thái APPROVED, IN_PROGRESS, COMPLETED)

---

## 26. Thuật ngữ

| Thuật ngữ | Mô tả |
|---|---|
| REQUESTER | Người lập yêu cầu |
| ĐV chủ quản ứng dụng | Đơn vị chủ quản hệ thống/ứng dụng liên quan |
| ĐV chủ quản CSDL | Đơn vị chủ quản cơ sở dữ liệu |
| ĐV yêu cầu | Đơn vị mà người lập yêu cầu thuộc |
| Variant I | Internal — Người lập thuộc ĐV chủ quản ứng dụng |
| Variant E | External — Người lập KHÔNG thuộc ĐV chủ quản ứng dụng |
| Ca | Ca truy cập (01, 02, 03) — format 2 chữ số zero-padded |
| Lần | Lần phát sinh trong ca — format 2 chữ số zero-padded |
| PENDING_SIGN | Trạng thái chờ ký xác nhận chung (chưa sinh mã) |
| DBA | Người quản trị CSDL — người lập phiếu 04B |

---

## 27. Variant theo mẫu

| Mẫu | Có variant? | Ghi chú |
|---|---|---|
| 01-YCTC | Có (I/E) | Xác định dựa trên ĐV chủ quản của HT/CSDL (dòng đầu tiên) |
| 02-YCCS | Có (I/E) | Xác định dựa trên ĐV chủ quản của HT đã chọn |
| 03-YCCT | Không | Chỉ 1 luồng duy nhất |
| 04A-YCTK | Có (I/E) | Xác định dựa trên ĐV chủ quản của HT đã chọn |
| 04B-BGTK | Luôn I | DBA thuộc đơn vị chủ quản CSDL |
| 05A-YCKC | Không | Chỉ 1 luồng duy nhất |
| 05B-HTKC | Không | Phiếu bổ sung, chỉ 1 luồng duy nhất |

---

## 28. Giao diện — Chọn loại mẫu yêu cầu

| # | Thành phần | Loại | Mô tả |
|---|---|---|---|
| 1 | Tiêu đề trang | Label | "Lập yêu cầu mới" |
| 2 | Danh sách loại mẫu | Card/Button | 7 loại mẫu hiển thị dạng card (lọc theo quyền người dùng) |
| 3 | Breadcrumb | Navigation | Trang chủ > Lập yêu cầu |

**Quy tắc hiển thị:**
- REQUESTER: hiển thị 01, 02, 03, 04A, 05A, 05B (ẩn 04B).
- DBA: hiển thị 04B (ẩn các mẫu còn lại).
- Card 01-YCTC chứa thêm button phụ "Đăng ký trước chi tiết".

---

## 29. Quy tắc filter HT/CSDL theo ĐV chủ quản (Mẫu 01)

- **Dòng đầu tiên:** Chọn HT và CSDL tự do (toàn bộ danh mục).
- **Các dòng tiếp theo:** Dropdown HT và CSDL chỉ hiển thị các item có chung ĐV chủ quản với dòng đầu tiên.
- **Khi xóa dòng đầu tiên:** Hệ thống lấy ĐV chủ quản theo dòng có thứ tự nhỏ nhất còn lại. Nếu không còn dòng nào → mở lại toàn bộ danh sách.
- **Thông báo:** Khi chọn xong dòng đầu, hiển thị info message: "Các dòng tiếp theo sẽ chỉ hiển thị HT/CSDL thuộc cùng đơn vị chủ quản [Tên ĐV]".

---

## 30. Logic "Người lập = Người truy cập" (Mẫu 01, 04A)

- Khi người lập thêm dòng chi tiết với "Người truy cập" / "Họ tên chủ tài khoản" = chính mình → Cột "Trạng thái ký" tự động hiển thị **"Đã ký"**.
- Chữ ký sử dụng: chữ ký của người lập tại thời điểm gửi phê duyệt.
- Không yêu cầu người lập ký riêng cho dòng của mình.
- Logic này áp dụng cho cả Nhánh A (lưu chờ ký) và Nhánh B (gửi trực tiếp).

---

## 31. Bảng CSDL mapping (04B, 05B)

### 31.1 Bảng `request_04b_04a_mapping`

| Cột | Kiểu | Mô tả |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| request_04b_code | VARCHAR | Mã 04B (format: [Mã 04A]_04B_V[xx]) |
| request_04a_id | BIGINT FK | Tham chiếu đến phiếu 04A |
| version | INT | Số thứ tự version (01, 02, ...) |
| status | ENUM | ACTIVE / CANCELLED |
| created_at | DATETIME | Ngày tạo |
| created_by | VARCHAR | Người tạo (DBA) |

### 31.2 Bảng `request_05b_05a_mapping`

| Cột | Kiểu | Mô tả |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| request_05b_code | VARCHAR | Mã 05B |
| request_05a_id | BIGINT FK | Tham chiếu đến phiếu 05A |
| version | INT | Số thứ tự version |
| status | ENUM | ACTIVE / CANCELLED |
| created_at | DATETIME | Ngày tạo |
| created_by | VARCHAR | Người tạo |

**Quan hệ:** 1 phiếu 05B → nhiều phiếu 05A (1-N).

---

## Giả định

1. Hệ thống đã có sẵn danh mục Hệ thống + CSDL để dropdown.
2. Hệ thống đã có email notification service.
3. Cơ chế ký điện tử đã được xác định (OTP).
4. Thông tin user (đơn vị, phòng ban, lãnh đạo) lấy từ AD/LDAP.
5. Bảng ký hiệu đơn vị đã có sẵn (do ADMIN cấu hình).
6. Mỗi phòng ban có ít nhất 1 Trưởng phòng/tương đương được cấu hình trong hệ thống.
7. Ca truy cập (01, 02, 03) có thời gian bắt đầu/kết thúc được cấu hình trong danh mục.

## Rủi ro

1. Logic gộp 05A cho 05B phức tạp — cần test kỹ edge cases (VD: 05A bị hủy giữa chừng).
2. Timeout 04B (3 ngày) — cần cron job/scheduler để kiểm tra và gửi email.
3. Cron job hết hạn đăng ký trước — cần scheduler chạy định kỳ (VD: mỗi giờ hoặc mỗi ca).
4. Local draft (sessionStorage) — cần xử lý edge case khi người dùng mở nhiều tab cùng lúc.
5. Validate nợ 05B lúc gửi phê duyệt phiếu 01 — cần query real-time, có thể ảnh hưởng performance nếu phiếu có nhiều người dùng.
6. Sequence "Lần" (toàn hệ thống theo Ngày + Ca) — cần đảm bảo thread-safe khi nhiều người lập phiếu đồng thời.
7. Timeout PENDING_SIGN — cần scheduler chạy mỗi 5 phút kiểm tra phiếu hết ca → chuyển CANCELLED.
8. Mã yêu cầu trùng lặp khi nhiều người gửi phê duyệt cùng lúc — cần DB sequence hoặc distributed lock.
9. Concurrency: Nhiều người cùng ký 1 phiếu PENDING_SIGN — cần optimistic/pessimistic locking trên dòng chi tiết.
10. Email notification thất bại — cần retry tự động 3 lần, mỗi lần cách nhau 5 giây.

---

## 32. Schema CSDL chính — Module Lập yêu cầu

> Mô tả cấu trúc các bảng chính phục vụ chức năng lập và gửi yêu cầu. Các bảng danh mục (information_system, database_catalog, unit...) được giả định đã tồn tại trong module khác.

### 32.1 Bảng `request` (Phiếu yêu cầu — bảng chính)

| Cột | Kiểu | Null? | Mô tả |
|---|---|---|---|
| id | BIGINT PK | — | Auto-increment |
| request_code | VARCHAR(50) | NULL | Mã yêu cầu (sinh khi gửi phê duyệt). NULL khi DRAFT/PENDING_SIGN |
| form_type | VARCHAR(10) | NOT NULL | Loại mẫu: `01`, `02`, `03`, `04A`, `04B`, `05A`, `05B` |
| status | VARCHAR(30) | NOT NULL | Trạng thái: DRAFT, PENDING_SIGN, PENDING_CHECK, PENDING_APPROVAL, PENDING_ACCESS_TEAM, PENDING_RECEIPT, REJECTED, APPROVED, IN_PROGRESS, CANCELLED, COMPLETED |
| variant | VARCHAR(1) | NULL | `I` / `E` / NULL (mẫu không có variant) |
| requester_id | VARCHAR(50) | NOT NULL | Mã người lập (từ AD) |
| requester_name | VARCHAR(100) | NOT NULL | Họ tên người lập |
| unit_code | VARCHAR(20) | NOT NULL | Mã đơn vị yêu cầu |
| unit_name | VARCHAR(100) | NOT NULL | Tên đơn vị yêu cầu |
| department_code | VARCHAR(20) | NOT NULL | Mã phòng ban |
| department_name | VARCHAR(100) | NOT NULL | Tên phòng ban |
| phone | VARCHAR(20) | NULL | ĐTDĐ |
| request_date | DATE | NOT NULL | Ngày lập yêu cầu (ngày tạo phiếu) |
| submit_date | DATE | NULL | Ngày gửi phê duyệt (ngày sinh mã). NULL khi chưa gửi |
| shift | VARCHAR(2) | NOT NULL | Ca: 01, 02, 03 |
| time_from | DATETIME | NULL | Thời gian bắt đầu (fill theo ca) |
| time_to | DATETIME | NULL | Thời gian kết thúc (fill theo ca) |
| system_id | BIGINT FK | NULL | HT chọn (NULL cho mẫu 01 — HT nằm ở detail) |
| system_name | VARCHAR(100) | NULL | Tên HT (denormalize) |
| database_id | BIGINT FK | NULL | CSDL chọn (NULL cho mẫu 01 — CSDL nằm ở detail) |
| database_name | VARCHAR(100) | NULL | Tên CSDL (denormalize) |
| owner_unit_id | VARCHAR(20) | NULL | ĐV chủ quản ứng dụng |
| owner_db_unit_id | VARCHAR(20) | NULL | ĐV chủ quản CSDL (chỉ 03, 04A) |
| request_type | VARCHAR(20) | NULL | Loại yêu cầu — Mẫu 01: Truy vấn/Chỉnh sửa; Mẫu 04A: Cấp mới/Đổi thuộc tính |
| reason | TEXT | NULL | Mục đích / Lý do yêu cầu |
| content | TEXT | NULL | Nội dung chi tiết (02, 03) |
| signature | TEXT | NULL | Chữ ký điện tử người lập |
| signed_at | DATETIME | NULL | Thời điểm ký |
| current_step_code | VARCHAR(20) | NULL | Step hiện tại trong workflow (VD: 01_I_01) |
| current_actor_id | VARCHAR(50) | NULL | Người xử lý hiện tại |
| current_actor_role | VARCHAR(20) | NULL | Role người xử lý hiện tại |
| at_requester_phase | BOOLEAN | NULL | Đang ở phase đơn vị yêu cầu? (Variant E) |
| reject_reason | TEXT | NULL | Lý do từ chối (khi REJECTED) |
| cancelled_reason | VARCHAR(50) | NULL | Lý do hủy: USER_CANCEL / TIMEOUT |
| version | INT | NOT NULL | Optimistic locking version |
| created_at | DATETIME | NOT NULL | Thời điểm tạo |
| updated_at | DATETIME | NOT NULL | Thời điểm cập nhật cuối |

**Index:**
- `idx_request_code` UNIQUE ON (request_code) WHERE request_code IS NOT NULL
- `idx_request_status_dept` ON (status, department_code) — phục vụ danh sách phiếu chờ ký
- `idx_request_requester` ON (requester_id, status) — phục vụ danh sách phiếu của người lập
- `idx_request_submit_date_shift` ON (submit_date, shift) — phục vụ sinh sequence "Lần"

### 32.2 Bảng `request_detail` (Dòng chi tiết — Mẫu 01, 04A)

| Cột | Kiểu | Null? | Mô tả |
|---|---|---|---|
| id | BIGINT PK | — | Auto-increment |
| request_id | BIGINT FK | NOT NULL | Tham chiếu bảng `request` |
| row_order | INT | NOT NULL | Số thứ tự dòng |
| system_id | BIGINT FK | NOT NULL | Hệ thống |
| system_name | VARCHAR(100) | NOT NULL | Tên HT (denormalize) |
| database_id | BIGINT FK | NOT NULL | CSDL |
| database_name | VARCHAR(100) | NOT NULL | Tên CSDL (denormalize) |
| object_name | VARCHAR(200) | NULL | Bảng/Đối tượng (Mẫu 01) |
| access_rights | VARCHAR(50) | NULL | Quyền truy cập: SELECT,INSERT,UPDATE,DELETE (Mẫu 01) |
| account_type | VARCHAR(20) | NULL | Loại tài khoản: QUERY/UPDATE (Mẫu 04A) |
| account_action | VARCHAR(20) | NULL | Hình thức: Cấp mới/Đổi thuộc tính (Mẫu 04A) |
| user_id | VARCHAR(50) | NOT NULL | Mã người truy cập/chủ tài khoản |
| user_name | VARCHAR(100) | NOT NULL | Họ tên người truy cập/chủ tài khoản |
| sign_status | VARCHAR(20) | NOT NULL | Trạng thái ký: PENDING / SIGNED / AUTO_SIGNED |
| signature | TEXT | NULL | Chữ ký điện tử |
| signed_at | DATETIME | NULL | Thời điểm ký |
| source | VARCHAR(20) | NOT NULL | Nguồn: MANUAL / PRE_REGISTER (nạp từ đăng ký trước) |
| pre_registration_id | BIGINT FK | NULL | Tham chiếu pre_registration_request (nếu source = PRE_REGISTER) |
| version | INT | NOT NULL | Optimistic locking version (row-level) |
| created_at | DATETIME | NOT NULL | Thời điểm tạo |
| updated_at | DATETIME | NOT NULL | Thời điểm cập nhật |

**Index:**
- `idx_detail_request` ON (request_id, row_order)
- `idx_detail_user` ON (request_id, user_id) — phục vụ validate trùng lặp
- `uk_detail_01_unique` UNIQUE ON (request_id, system_id, database_id, object_name, user_id) WHERE form_type = '01' — Rule #10a

### 32.3 Bảng `request_04b_detail` (Chi tiết bàn giao — Mẫu 04B)

| Cột | Kiểu | Null? | Mô tả |
|---|---|---|---|
| id | BIGINT PK | — | Auto-increment |
| request_id | BIGINT FK | NOT NULL | Tham chiếu bảng `request` (phiếu 04B) |
| row_order | INT | NOT NULL | Số thứ tự dòng |
| account_user_id | VARCHAR(50) | NOT NULL | Tài khoản được cấp (UserID) — DBA nhập |
| account_type | VARCHAR(20) | NOT NULL | Loại tài khoản (QUERY/UPDATE) — fill từ 04A |
| scope | VARCHAR(500) | NOT NULL | Phạm vi — DBA nhập |
| content | VARCHAR(100) | NOT NULL | Nội dung (= "Cấp mới") — fill từ 04A |
| owner_name | VARCHAR(100) | NOT NULL | Chủ tài khoản — fill từ 04A |
| receipt_status | VARCHAR(20) | NOT NULL | Trạng thái ký nhận: PENDING / SIGNED |
| receipt_signature | TEXT | NULL | Chữ ký ký nhận |
| receipt_signed_at | DATETIME | NULL | Thời điểm ký nhận |
| version | INT | NOT NULL | Optimistic locking |
| created_at | DATETIME | NOT NULL | Thời điểm tạo |

**Index:**
- `idx_04b_detail_request` ON (request_id)

### 32.4 Bảng `request_sql_file` (File SQL đính kèm — Mẫu 02, 03)

| Cột | Kiểu | Null? | Mô tả |
|---|---|---|---|
| id | BIGINT PK | — | Auto-increment |
| request_id | BIGINT FK | NOT NULL | Tham chiếu bảng `request` |
| file_name | VARCHAR(200) | NOT NULL | Tên file (format: YYYYMMDD_BS_XXX.sql) |
| file_path | VARCHAR(500) | NOT NULL | Đường dẫn lưu trữ |
| file_size | BIGINT | NOT NULL | Dung lượng (bytes) |
| checksum_type | VARCHAR(10) | NOT NULL | MD5 / SHA-256 |
| checksum_system | VARCHAR(64) | NOT NULL | Hash do hệ thống tính |
| checksum_user | VARCHAR(64) | NOT NULL | Hash do người dùng nhập |
| checksum_match | BOOLEAN | NOT NULL | TRUE nếu khớp |
| uploaded_at | DATETIME | NOT NULL | Thời điểm upload |

**Index:**
- `idx_sql_file_request` ON (request_id)

### 32.5 Bảng `request_03_structure` (Nội dung cấu trúc — Mẫu 03)

| Cột | Kiểu | Null? | Mô tả |
|---|---|---|---|
| id | BIGINT PK | — | Auto-increment |
| request_id | BIGINT FK | NOT NULL | Tham chiếu bảng `request` |
| category | VARCHAR(20) | NOT NULL | Tab: CREATE / ALTER / DROP |
| sub_category | VARCHAR(30) | NOT NULL | Mục con: TABLE, STRUCTURE, INDEX, SYNONYM, OTHER (Create); ADD_COL, MODIFY_COL, REBUILD_INDEX, OTHER (Alter); DROP_CONTENT (Drop) |
| content | TEXT | NOT NULL | Nội dung chi tiết (JSON hoặc text tùy mục) |
| row_order | INT | NOT NULL | Thứ tự hiển thị |
| created_at | DATETIME | NOT NULL | Thời điểm tạo |
| updated_at | DATETIME | NOT NULL | Thời điểm cập nhật |

**Index:**
- `idx_03_structure_request` ON (request_id, category)

### 32.6 Bảng `request_sequence` (Sequence sinh mã "Lần")

| Cột | Kiểu | Null? | Mô tả |
|---|---|---|---|
| id | BIGINT PK | — | Auto-increment |
| seq_date | DATE | NOT NULL | Ngày (ddmmyyyy) |
| seq_shift | VARCHAR(2) | NOT NULL | Ca (01, 02, 03) |
| last_value | INT | NOT NULL | Giá trị cuối cùng của "Lần" trong ngày + ca |
| updated_at | DATETIME | NOT NULL | Thời điểm cập nhật |

**Constraint:**
- `uk_sequence_date_shift` UNIQUE ON (seq_date, seq_shift)

**Logic sinh mã:**
1. SELECT FOR UPDATE trên dòng (seq_date, seq_shift).
2. Nếu chưa tồn tại → INSERT với last_value = 1.
3. Nếu đã tồn tại → UPDATE last_value = last_value + 1.
4. Format "Lần" = zero-padded 2 chữ số (mở rộng 3 chữ số nếu > 99).

### 32.7 Bảng `workflow_history` (Lịch sử workflow)

| Cột | Kiểu | Null? | Mô tả |
|---|---|---|---|
| id | BIGINT PK | — | Auto-increment |
| request_id | BIGINT FK | NOT NULL | Tham chiếu bảng `request` |
| action | VARCHAR(30) | NOT NULL | SUBMIT, APPROVE, REJECT, FORWARD, CANCEL... |
| step_code | VARCHAR(20) | NOT NULL | Step tại thời điểm action (VD: 01_I_01) |
| actor_id | VARCHAR(50) | NOT NULL | Người thực hiện action |
| actor_role | VARCHAR(20) | NOT NULL | Role người thực hiện |
| actor_unit_id | VARCHAR(20) | NULL | Đơn vị người thực hiện |
| comment | TEXT | NULL | Ghi chú / Lý do từ chối |
| created_at | DATETIME | NOT NULL | Thời điểm action |

**Index:**
- `idx_wf_history_request` ON (request_id, created_at)

### 32.8 Bảng `email_notification_log` (Log gửi email)

| Cột | Kiểu | Null? | Mô tả |
|---|---|---|---|
| id | BIGINT PK | — | Auto-increment |
| request_id | BIGINT FK | NOT NULL | Phiếu liên quan |
| template_code | VARCHAR(30) | NOT NULL | Mã template (SUBMIT, REJECT, PENDING_SIGN, TIMEOUT, REMIND_05B, TIMEOUT_04B) |
| recipient_email | VARCHAR(200) | NOT NULL | Email người nhận |
| recipient_name | VARCHAR(100) | NOT NULL | Tên người nhận |
| subject | VARCHAR(300) | NOT NULL | Subject email |
| status | VARCHAR(20) | NOT NULL | SENT / FAILED / RETRYING |
| retry_count | INT | NOT NULL | Số lần retry (max 3) |
| sent_at | DATETIME | NULL | Thời điểm gửi thành công |
| error_message | TEXT | NULL | Lỗi nếu FAILED |
| created_at | DATETIME | NOT NULL | Thời điểm tạo |

**Index:**
- `idx_email_log_request` ON (request_id)
- `idx_email_log_status` ON (status) WHERE status = 'RETRYING' — phục vụ job retry

### 32.9 Quan hệ giữa các bảng (ERD tóm tắt)

```
request (1) ──── (N) request_detail          [Mẫu 01, 04A]
request (1) ──── (N) request_04b_detail      [Mẫu 04B]
request (1) ──── (1) request_sql_file        [Mẫu 02, 03]
request (1) ──── (N) request_03_structure    [Mẫu 03]
request (1) ──── (N) workflow_history
request (1) ──── (N) email_notification_log
request (1) ──── (N) request_04b_04a_mapping [04B → 04A]
request (1) ──── (N) request_05b_05a_mapping [05B → 05A]
request_detail (N) ── (1) pre_registration_request [source = PRE_REGISTER]
```

**Ghi chú thiết kế:**
- Bảng `request` là bảng chung cho tất cả 7 loại mẫu. Phân biệt qua `form_type`.
- Các trường không áp dụng cho mẫu cụ thể sẽ = NULL.
- `request_detail` dùng cho mẫu 01 (nhiều HT/CSDL/người) và 04A (nhiều người/tài khoản).
- `request_04b_detail` riêng cho 04B vì có logic ký nhận khác biệt.
- Denormalize tên HT/CSDL/người vào bảng chi tiết để tránh JOIN khi hiển thị.
- Optimistic locking (`version`) trên `request` và `request_detail` để xử lý concurrency.
