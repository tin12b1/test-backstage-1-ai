# Feature: Chức năng lập và gửi yêu cầu

**Người phụ trách:** Tin  
**Mã hạng mục:** 2.3

---

## 1. Mục tiêu

Cho phép người lập yêu cầu đăng nhập, chọn mẫu phiếu, nhập thông tin, ký xác nhận, lưu nháp, gửi phê duyệt hoặc gửi bộ phận Mở truy cập đối với yêu cầu khẩn cấp.

## 2. Mẫu phiếu hỗ trợ

- 01-YCTC: Truy cập, truy xuất CSDL thông thường.
- 02-YCCS: Chỉnh sửa dữ liệu.
- 03-YCCT: Thay đổi cấu trúc CSDL.
- 04A-YCTK: Cấp mới/thay đổi thuộc tính tài khoản.
- 04B-BGTK: Biên bản bàn giao tài khoản (Người quản trị CSDL lập sau khi cấp thành công tài khoản theo 04A-YCTK). **Không sinh mã riêng — dùng chung mã phiếu 04A liên kết.** Lưu bảng riêng `handover_record`.
- 05A-YCKC: Truy cập khẩn cấp.
- 05B-HTKC: Hoàn thành truy cập khẩn cấp.

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
10. Hệ thống khởi tạo workflow: Xác định variant (I/E), set `current_step_code`, gọi `resolveNextActor()` — chi tiết xem `docs/workflow-step-codes.md` mục 9.1.
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
- Hệ thống khởi tạo workflow: Xác định variant (I/E), set `current_step_code`, gọi `resolveNextActor()` — chi tiết xem `docs/workflow-step-codes.md` mục 9.1.
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
12B. Hệ thống khởi tạo workflow: Xác định variant (I/E), set `current_step_code`, gọi `resolveNextActor()` — chi tiết xem `docs/workflow-step-codes.md` mục 9.1.
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
7. Chọn Người có thẩm quyền đã liên hệ (bắt buộc):
   - **Trường hợp 1** (Người lập thuộc ĐV chủ quản ứng dụng): Hiển thị 1 dropdown — chọn Người có thẩm quyền của ĐV chủ quản ứng dụng.
   - **Trường hợp 2** (Người lập KHÔNG thuộc ĐV chủ quản ứng dụng): Hiển thị 2 dropdown — chọn Người có thẩm quyền của ĐV yêu cầu VÀ Người có thẩm quyền của ĐV chủ quản ứng dụng.
   - Mục đích: Lưu thông tin NTQ mà người lập đã liên hệ qua kênh liên lạc nhanh, phục vụ đối chiếu. Chỉ chọn, không yêu cầu NTQ ký số tại bước này.
8. Người lập ký xác nhận.
9. Hệ thống kiểm tra trường bắt buộc + validation rules.
10. Hệ thống sinh mã yêu cầu.
11. Hệ thống lưu hồ sơ ở trạng thái `Đã chuyển bộ phận Mở truy cập`.
12. Hệ thống khởi tạo workflow: Set `current_step_code` = `05A_01`, gọi `resolveNextActor()` — chi tiết xem `docs/workflow-step-codes.md` mục 9.1.
13. Hệ thống gửi email notification đến Bộ phận mở truy cập.
14. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

## 6. Luồng 4: 04B-BGTK, 05B-HTKC (Phiếu bổ sung sau hoàn thành)

### 6.1 Luồng 04B-BGTK (Người quản trị CSDL lập)

**Điều kiện hiển thị:** Chỉ hiển thị đối với người quản trị CSDL (kiểm tra quyền).
**Điều kiện lập:** Phiếu 04A-YCTK liên quan phải ở trạng thái "Hoàn thành" VÀ phải có ít nhất 1 dòng chi tiết có Hình thức = "Cấp mới".

1. Người quản trị CSDL đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", mẫu 04B-BGTK.
3. Hệ thống hiển thị danh sách 04A-YCTK đã hoàn thành, có ít nhất 1 dòng "Cấp mới", nhưng chưa có 04B-BGTK tương ứng.
4. Người quản trị CSDL chọn phiếu đang nợ.
5. Hệ thống tự động fill nội dung từ 04A-YCTK:
   - Tên hệ thống, Tên CSDL.
   - Mã yêu cầu 04A liên quan.
   - Thời gian bàn giao (ngày hiện tại).
   - Đại diện BP quản trị CSDL (Cấp QL) — từ cấu hình.
   - Người bàn giao (Người quản trị CSDL) — user đăng nhập.
   - Đại diện BP nhận bàn giao (Cấp QL) — lãnh đạo phòng người yêu cầu.
   - Người nhận bàn giao — danh sách người dùng từ 04A (chỉ những người có Hình thức = "Cấp mới").
   - Chi tiết: Chỉ nạp các dòng có Hình thức = "Cấp mới" từ 04A (KHÔNG nạp dòng "Đổi thuộc tính"). Gồm: Loại tài khoản, Nội dung (= "Cấp mới"), Chủ tài khoản.
   - **KHÔNG tự fill:** Tài khoản được cấp (UserID) — Người quản trị CSDL nhập tay. Phạm vi — Người quản trị CSDL tự nhập.
6. Người quản trị CSDL nhập thông tin tài khoản đã cấp + Phạm vi + Địa điểm bàn giao.
7. Người quản trị CSDL ký xác nhận.
8. Hệ thống kiểm tra trường bắt buộc + validation rules.
9. Hệ thống tạo bản ghi bàn giao (`handover_record`) liên kết phiếu 04A. Không sinh mã riêng — dùng chung mã 04A.
10. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
11. Hệ thống khởi tạo workflow: Variant luôn = `I` (Internal — Người quản trị CSDL thuộc đơn vị chủ quản CSDL), set `current_step_code`, gọi `resolveNextActor()` — chi tiết xem `docs/workflow-step-codes.md` mục 9.1.
12. Hệ thống gửi email notification đến Lãnh đạo phòng quản trị CSDL.
13. → Chuyển scope phê duyệt (Lãnh đạo phòng quản trị CSDL duyệt).

> **Ghi chú:** Bước 14-18 mô tả tổng quan luồng ký nhận để developer hiểu context. Chi tiết xử lý phê duyệt + ký nhận sẽ được mô tả đầy đủ trong scope phê duyệt.

14. Sau khi duyệt → Trạng thái chuyển `Chờ ký nhận`.
15. Hệ thống gửi email notification cho người dùng trong danh sách.
16. Người dùng đăng nhập → Ký nhận dòng của mình (row-level locking, polling 10 giây + push sau ký).
17. Khi tất cả người dùng đã ký → Hệ thống tự động chuyển `Chờ phê duyệt` (Lãnh đạo phòng người dùng).
18. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt lần 2.

**Timeout ký nhận:** 3 ngày kể từ khi chuyển "Chờ ký nhận".
- Hành động: Gửi email cho người quản trị CSDL đã lập phiếu, lãnh đạo phòng quản trị CSDL, lãnh đạo phòng cán bộ cần ký.
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
   - Mã yêu cầu: Chưa sinh mã khi lập. Mã chỉ được gán khi phiếu COMPLETED. Format: MãĐV_ddmmyyyy_Ca_LầnGhép. Trước đó hiển thị "—".
   - Danh sách bảng = union tất cả bảng từ các phiếu 05A trong ca.
   - Thông tin chung: Hệ thống, CSDL, Ngày, Ca, Thời gian.
   - Trường "Lần" hiển thị tổng hợp (VD: "Lần: 01, 02, 03") — chỉ là hiển thị, lấy từ trường "Lần" của các phiếu 05A liên quan.
   - Lưu bảng tham chiếu `emergency_completion_link` (1 phiếu 05B → nhiều phiếu 05A).
6. Người lập nhập nội dung công việc đã thực hiện (mô tả chi tiết, câu lệnh) — bắt buộc.
7. Người lập ký xác nhận.
8. Hệ thống kiểm tra trường bắt buộc + validation rules.
9. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
10. Hệ thống khởi tạo workflow: Xác định variant (I/E), set `current_step_code` = `05B_I_01` hoặc `05B_E_01`, gọi `resolveNextActor()` — chi tiết xem `docs/workflow-step-codes.md` mục 9.1.
11. Hệ thống xác định: Nếu người lập thuộc ĐV chủ quản ứng dụng → gửi email đến lãnh đạo phòng ĐV chủ quản ứng dụng. Nếu không → gửi email đến lãnh đạo phòng ĐV yêu cầu.
12. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

## 7. Hệ thống trạng thái và khởi tạo Workflow

> Chi tiết đầy đủ: xem `docs/workflow-step-codes.md` — mục 2 (Trạng thái đặc biệt), mục 3 (Quy tắc xác định Variant), mục 7 (at_requester_phase), mục 9.1 (Module Request — Khi SUBMIT).

### Trạng thái áp dụng cho scope lập yêu cầu

> Danh sách trạng thái đầy đủ: xem `docs/architecture.md` mục 5.

### Quy tắc nghiệp vụ trạng thái (scope lập yêu cầu)

- Phiếu DRAFT / PENDING_SIGN: chưa sinh mã, cột "Mã YC" hiển thị "—".
- Phiếu bị chuyển trả → RETURNED: không cho phép chỉnh sửa, phải lập mẫu mới.
- Phiếu PENDING_SIGN hết ca → tự động CANCELLED + email notification đến người lập.
- Nhánh A lưu phiếu: chỉ set status = PENDING_SIGN, KHÔNG khởi tạo workflow. Workflow chỉ khởi tạo khi SUBMIT.
- Người lập chỉ được hủy phiếu khi status = DRAFT hoặc PENDING_SIGN. Sau khi đã gửi, chỉ hệ thống mới chuyển CANCELLED (timeout).

### Mapping status khi SUBMIT

| Trường hợp | Status sau SUBMIT |
|---|---|
| 01, 04A (Nhánh B), 05B | `PENDING_DEPT_APPROVAL` |
| 02, 03 | `PENDING_CHECK` |
| 05A | `SENT_TO_ACCESS_TEAM` |
| 01, 04A (Nhánh A — chỉ lưu chờ ký) | `PENDING_SIGN` (không khởi tạo workflow) |

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
| 9c | Timeout 04B ký nhận | 3 ngày kể từ khi chuyển "Chờ ký nhận" → Email cho người lập 04B, lãnh đạo phòng quản trị CSDL, lãnh đạo phòng cán bộ. KHÔNG hủy phiếu, giữ nguyên. | 04B-BGTK |
| 10a | Trùng lặp 01-YCTC | Chặn khi trùng cả 4 nội dung: Hệ thống + CSDL + Đối tượng + Người dùng (trong cùng phiếu) | 01-YCTC |
| 10b | Trùng lặp 04A-YCTK | Chặn trùng người dùng ở bảng chi tiết (trong cùng phiếu) | 04A-YCTK |
| 11 | Thời gian tạo phiếu | Ngày + Ca: chỉ cho phép chọn (không cho nhập), chỉ hiện tại hoặc tương lai, không được chọn quá khứ (trừ 04B, 05B — phiếu bổ sung) | Tất cả (trừ 04B, 05B) |
| 12 | Concurrency | Row-level locking + Polling 10 giây + Push ngay sau khi ký (không chờ polling cycle). Mỗi người thao tác dòng riêng, không conflict. Khi 1 người ký xong → push cập nhật ngay cho các người khác đang mở phiếu. Người lập có quyền xóa bất kỳ dòng nào (kể cả đã ký). | 01, 04A, 04B |
| 13 | Kiểm tra quyền | Kiểm tra quyền người dùng trước khi cho phép chọn mẫu. VD: 04B chỉ hiển thị cho người quản trị CSDL. | Tất cả |
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
- Cho phép hủy yêu cầu chỉ khi phiếu ở trạng thái DRAFT hoặc PENDING_SIGN (không cần lý do). Sau khi đã gửi phê duyệt/gửi kiểm tra/gửi BP Mở truy cập, không cho phép hủy.
- Sau khi gửi phê duyệt không được sửa nội dung.
- Mẫu 06-ĐKNS: không thuộc hệ thống.
- Mẫu 07-NKCV: tự sinh sau khi hoàn thành, không thuộc scope lập yêu cầu.
- Tất cả phần thuộc scope phê duyệt/thực hiện: hiển thị để trống, read-only trên form lập yêu cầu (bao gồm: ô ký phê duyệt, kết quả thực hiện, phần Người quản trị CSDL ghi, phần thực hiện mở truy cập).
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
  + Người lập phiếu: dropdown chọn từ danh sách người dùng cùng phòng ban. Có thể thêm nhiều dòng cho nhiều người khác nhau.
  + Người dùng khác (vào phiếu đang "Chờ ký xác nhận"): chỉ được thêm dòng cho bản thân (tự fill tên từ AD, không cho chọn người khác).

## 11. Quy tắc riêng mẫu 03-YCCT

- Loại yêu cầu: Checkbox — chọn ít nhất 1, có thể chọn nhiều hoặc cả 3 (Tạo mới / Thay đổi / Xóa). Khi chọn mục nào → hiển thị phần nội dung tương ứng.
- SQL Script là mục chung cho toàn bộ phiếu (1 file duy nhất, có thể bao gồm cả Tạo mới/Thay đổi/Xóa hoặc chỉ 1 phần trong 3). Định dạng tên file: `YYYYMMDD_BS_XXX.sql`. Chỉ cho phép upload 1 file duy nhất.
- Nếu có file SQL Script + checksum khớp → nội dung chi tiết các tab KHÔNG bắt buộc (có thể để trống).
- Nếu KHÔNG có file SQL Script → nội dung chi tiết của tất cả tab đã chọn PHẢI có dữ liệu:
  + Tab Tạo mới: chỉ cần ít nhất 1 mục con có dữ liệu (Table, Cấu trúc table, Index, Synonym, Tạo mới khác).
  + Tab Thay đổi: chỉ cần ít nhất 1 mục con có dữ liệu (Thêm cột, Sửa cột, Tạo lại index, Thay đổi khác).
  + Tab Xóa: trường "Nội dung lệnh xóa" phải có dữ liệu.
- Có phần nội dung Người quản trị CSDL ghi để đánh giá tác động ảnh hưởng và hệ thống liên quan (hiển thị để trống, read-only — thuộc scope phê duyệt).

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
| Họ và tên | Dropdown/Tự động | ✅ | Người lập phiếu: dropdown chọn từ danh sách người dùng cùng phòng ban (có thể thêm nhiều dòng cho nhiều người). Người dùng khác (vào phiếu "Chờ ký xác nhận"): chỉ được thêm dòng cho bản thân (tự fill tên, không cho chọn người khác) |
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
| Phần Người quản trị CSDL ghi (Đánh giá tác động) | Hiển thị | — | Để trống, read-only (thuộc scope phê duyệt) |
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
| Họ tên chủ tài khoản | Dropdown/Tự động | ✅ | Người lập: dropdown chọn người dùng cùng phòng ban. Người khác: tự fill tên bản thân |
| Loại tài khoản | Dropdown | ✅ | Truy vấn / Chỉnh sửa — mỗi dòng tự chọn riêng |
| Hình thức | Dropdown | ✅ | Cấp mới / Đổi thuộc tính — mỗi dòng tự chọn riêng |
| Ký tên | Ký điện tử (OTP) | ✅ | 1 người có thể có nhiều dòng. Ký 1 lần → tất cả dòng tự "Đã ký". Nếu người lập và người truy cập là một thì không cần ký tại mục chi tiết |

**Lưu ý:** Logic ký tương tự mẫu 01-YCTC (Nhánh A/B, row-level locking, polling 10 giây + push sau ký). Người lập được xóa bất kỳ dòng nào (kể cả đã ký), không được sửa dòng đã ký.

## 16. Giao diện mẫu 04B-BGTK

**Điều kiện hiển thị:** Chỉ hiển thị đối với người quản trị CSDL. Người dùng khác thông thường KHÔNG hiển thị 04B.

### Màn hình chọn phiếu nợ

| Trường | Loại | Ghi chú |
|---|---|---|
| Danh sách 04A đang nợ | Danh sách chọn | Hiển thị các phiếu 04A hoàn thành, có ít nhất 1 dòng "Cấp mới", chưa có 04B |

### Form nhập liệu (sau khi chọn phiếu)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Tự động | ✅ | Fill từ 04A, read-only |
| Tên CSDL | Tự động | ✅ | Fill từ 04A, read-only |
| Mã yêu cầu | Tự động | ✅ | Hiển thị mã 04A liên kết + "(Biên bản bàn giao)". Không sinh mã riêng |
| Mã yêu cầu 04A liên quan | Tự động | ✅ | Fill từ 04A, read-only |
| Thời gian bàn giao | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Địa điểm | Nhập text | ✅ | Người lập phiếu nhập |
| Đại diện BP quản trị CSDL (Cấp QL) | Tự động | ✅ | Lấy từ cấu hình |
| Người bàn giao (Người quản trị CSDL) | Tự động | ✅ | User đăng nhập |
| Đại diện BP nhận bàn giao (Cấp QL) | Tự động | ✅ | Lãnh đạo phòng người yêu cầu (từ 04A) |
| Người nhận bàn giao | Tự động | ✅ | Danh sách người dùng từ 04A (chỉ những người có Hình thức = "Cấp mới") |
| Ký tên (Người bàn giao) | Ký điện tử (OTP) | ✅ | Người quản trị CSDL ký |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |

### Chi tiết bàn giao (bảng)

**Lưu ý:** Chỉ hiển thị các dòng có Hình thức = "Cấp mới" từ 04A. Các dòng "Đổi thuộc tính" KHÔNG hiển thị trong 04B.

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tài khoản (UserID) | Nhập text | ✅ | Người lập phiếu nhập tay — KHÔNG tự fill |
| Loại tài khoản (QUERY/UPDATE) | Tự động | ✅ | Fill từ 04A |
| Phạm vi | Nhập text | ✅ | Người lập phiếu tự nhập (KHÔNG fill từ 04A) |
| Nội dung | Tự động | ✅ | Fill từ trường "Hình thức" của 04A (= "Cấp mới") |
| Chủ tài khoản | Tự động | ✅ | Fill từ 04A |
| Ký nhận (Người dùng) | Ký điện tử (OTP) | ✅ | Mỗi người ký dòng mình (sau khi lãnh đạo phòng quản trị CSDL duyệt, trạng thái "Chờ ký nhận") |

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
| Người có thẩm quyền ĐV chủ quản ứng dụng | Dropdown | ✅ | Luôn hiển thị. Danh sách NTQ của ĐV chủ quản ứng dụng (theo HT đã chọn). Lưu thông tin đã liên hệ, không yêu cầu ký |
| Người có thẩm quyền ĐV yêu cầu | Dropdown | Có điều kiện | Chỉ hiển thị khi người lập KHÔNG thuộc ĐV chủ quản ứng dụng. Danh sách NTQ của ĐV yêu cầu. Bắt buộc khi hiển thị |
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
| Mã yêu cầu 05B | Tự động | ✅ | Gán khi COMPLETED. Format: [Mã ĐV]_[ddmmyyyy]_[Ca]_[Lần ghép]. VD: CNTT-NHDT_09072026_02_0103. Hiển thị "—" khi chưa hoàn thành |
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

Cho phép người dùng có quyền lập yêu cầu đăng ký trước thông tin chi tiết truy cập CSDL cho ngày + ca hiện tại hoặc tương lai. Khi người lập phiếu 01-YCTC chọn ca, hệ thống tự động nạp toàn bộ đăng ký trước phù hợp của tất cả người dùng cùng đơn vị vào bảng chi tiết phiếu.

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

- Bất kỳ người dùng nào có quyền lập yêu cầu đều có chức năng này.

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

> Chi tiết schema: xem `docs/database-schema.md` — bảng `access_registration`.

### 19.6 Bảng tham chiếu 05B → 05A

> Chi tiết schema: xem `docs/database-schema.md` — bảng `emergency_completion_link`.

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
1. Hệ thống truy vấn bảng `access_registration` với điều kiện:
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
- [ ] Lập 04B-BGTK từ 04A đã hoàn thành: chỉ nạp dòng "Cấp mới", kiểm tra auto-fill đúng (Nội dung = "Cấp mới", Phạm vi = Người lập phiếu nhập).
- [ ] 04B không hiển thị dòng "Đổi thuộc tính" từ 04A.
- [ ] Lập 05B-HTKC, kiểm tra gộp tự động các 05A chung HT+CSDL+Ngày+Ca.
- [ ] Mã 05B chỉ gán khi COMPLETED, hiển thị "Lần" tổng hợp từ 05A liên quan.
- [ ] Bảng mapping emergency_completion_link lưu đúng quan hệ 1-N.
- [ ] Timeout 04B-BGTK: 3 ngày → email notification gửi đúng người.
- [ ] Checksum file SQL: match → OK; không match → chặn gửi.
- [ ] Trùng lặp 01 (4 trường): chặn đúng.
- [ ] Trùng lặp 04A (người dùng): chặn đúng.
- [ ] Concurrency: nhiều người ký đồng thời trên 01/04A/04B → không conflict. Polling 10 giây + push sau ký.
- [ ] Kiểm tra quyền: 04B chỉ hiển thị cho người quản trị CSDL, người dùng khác không thấy 04B.
- [ ] Chặn chọn ngày + ca quá khứ (trừ 04B, 05B). Chỉ cho chọn, không cho nhập.
- [ ] Mẫu 01: logic Truy vấn → chỉ SELECT; Chỉnh sửa → multi-select.
- [ ] Đổi loại yêu cầu 01 → cảnh báo reset → confirm → reset đúng.
- [ ] 04B: tất cả người dùng ký nhận → tự động chuyển "Chờ phê duyệt" lần 2.
- [ ] Mẫu 02: Thời gian cập nhật tự fill theo ca, cho phép sửa, không được để trống.
- [ ] Tất cả phần read-only hiển thị đúng, không cho nhập.
- [ ] Phiếu bị chuyển trả → trạng thái RETURNED (có lý do), không cho sửa, phải lập mới.
- [ ] Timeout PENDING_SIGN (01-YCTC): phiếu hết ca → tự động CANCELLED + email đến người lập.
- [ ] Timeout PENDING_SIGN (04A-YCTK): phiếu hết ngày → tự động CANCELLED + email đến người lập.
- [ ] Phiếu PENDING_SIGN/DRAFT: cột "Mã yêu cầu" hiển thị "—", tìm kiếm qua metadata.
- [ ] Sinh mã chỉ khi gửi phê duyệt, mã format đúng, unique toàn hệ thống.
- [ ] Mã 04B: dùng chung mã 04A. Phiếu bị RETURNED → lập mới (bản ghi mới liên kết cùng 04A).
- [ ] Mã 05B: chỉ gán khi COMPLETED. Format: [Mã ĐV]_[ddmmyyyy]_[Ca]_[Lần ghép]. Trước khi COMPLETED hiển thị "—".
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
- [ ] Trường "Họ và tên" mẫu 01: người lập = dropdown người dùng cùng phòng ban; người khác = tự fill bản thân.
- [ ] Khi gửi phê duyệt phiếu 01/04A: dòng chưa ký (người dùng khác người lập) tự động bị xóa.

## 23. Definition of Done

- Hoàn thành form cho **7 mẫu phiếu** (01-YCTC, 02-YCCS, 03-YCCT, 04A-YCTK, 04B-BGTK, 05A-YCKC, 05B-HTKC).
- Hoàn thành chức năng con "Đăng ký trước Yêu cầu chi tiết" cho mẫu 01-YCTC.
- Có lưu nháp, sửa nháp, ký, gửi, hủy, gửi lại.
- Có validate nghiệp vụ và validate giao diện.
- Có test cho các luồng chính và lỗi nghiệp vụ quan trọng.
- Khởi tạo workflow thành công khi gửi phê duyệt/kiểm tra.
- Mã yêu cầu sinh đúng format, không trùng lặp, chỉ sinh khi gửi phê duyệt.
- Mã 04B dùng chung mã 04A. Mã 05B sinh đúng format khi COMPLETED (không version).
- Timeout PENDING_SIGN tự động CANCELLED + email hoạt động.
- Email notification gửi đúng người, đúng thời điểm.

---

## 24. Email Templates

| # | Sự kiện | Subject | Body (tóm tắt) |
|---|---|---|---|
| 1 | Gửi phê duyệt | [DB Access] Yêu cầu chờ phê duyệt - [Mã YC] | Yêu cầu [Mã YC] loại [Tên mẫu] do [Tên người lập] - [Phòng ban] lập ngày [Ngày lập] đang chờ phê duyệt. |
| 2 | Bị chuyển trả | [DB Access] Yêu cầu bị chuyển trả - [Mã YC] | Yêu cầu [Mã YC] đã bị chuyển trả. Lý do: [Lý do]. |
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
| Người quản trị CSDL | Người quản trị cơ sở dữ liệu — người lập phiếu 04B |

---

## 27. Variant theo mẫu

| Mẫu | Có variant? | Ghi chú |
|---|---|---|
| 01-YCTC | Có (I/E) | Xác định dựa trên ĐV chủ quản của HT/CSDL (dòng đầu tiên) |
| 02-YCCS | Có (I/E) | Xác định dựa trên ĐV chủ quản của HT đã chọn |
| 03-YCCT | Không | Chỉ 1 luồng duy nhất |
| 04A-YCTK | Có (I/E) | Xác định dựa trên ĐV chủ quản của HT đã chọn |
| 04B-BGTK | Luôn I | Người quản trị CSDL thuộc đơn vị chủ quản CSDL |
| 05A-YCKC | Không | Chỉ 1 luồng duy nhất |
| 05B-HTKC | Có (I/E) | Xác định dựa trên ĐV chủ quản của HT đã chọn (từ 05A) |

---

## 28. Giao diện — Chọn loại mẫu yêu cầu

| # | Thành phần | Loại | Mô tả |
|---|---|---|---|
| 1 | Tiêu đề trang | Label | "Lập yêu cầu mới" |
| 2 | Danh sách loại mẫu | Card/Button | 7 loại mẫu hiển thị dạng card (lọc theo quyền người dùng) |
| 3 | Breadcrumb | Navigation | Trang chủ > Lập yêu cầu |

**Quy tắc hiển thị:**
- Người lập yêu cầu thông thường: hiển thị 01, 02, 03, 04A, 05A, 05B (ẩn 04B).
- Người quản trị CSDL: hiển thị 04B (ẩn các mẫu còn lại).
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
