
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

## 3. Luồng 1: 02-YCCS, 03-YCCT (Gửi bộ phận kiểm tra)

1. Người lập yêu cầu đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", chọn mẫu phiếu (02-YCCS hoặc 03-YCCT).
3. Chọn Hệ thống (1 HT duy nhất) → Chọn CSDL (1 CSDL duy nhất).
4. Nhập nội dung chi tiết cho mẫu phiếu.
5. Ký xác nhận gửi kiểm tra.
6. Hệ thống kiểm tra trường bắt buộc + validation rules.
7. Hệ thống sinh mã yêu cầu (format: KýhiệuĐV_DDMMYYYYCa:Lần: — Lần tự tăng theo số phiếu trong ca của đơn vị).
8. Hệ thống lưu hồ sơ ở trạng thái `Chờ kiểm tra`.
9. Hệ thống gửi email notification đến Bộ phận kiểm tra.
10. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

## 4. Luồng 2: 01-YCTC, 04A-YCTK (Phiếu nhiều người ký)

1. Người lập đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", chọn mẫu phiếu (01-YCTC hoặc 04A-YCTK).
3. (Mẫu 01-YCTC): Chọn "Loại yêu cầu" (Truy vấn / Chỉnh sửa) — áp dụng toàn bộ phiếu.
4. (Mẫu 04A-YCTK): Chọn Hệ thống (1 HT) → Chọn CSDL (1 CSDL) — thông tin chung.
5. Nhập thông tin chung + danh sách chi tiết (tối thiểu 1 dòng).
6. Người lập chọn một trong hai hành động:

### Nhánh A: Lưu phiếu (chờ người khác ký)

7A. Hệ thống kiểm tra trường bắt buộc + validation rules.
8A. Hệ thống sinh mã yêu cầu.
9A. Hệ thống lưu hồ sơ ở trạng thái `Chờ ký xác nhận`.
10A. Người dùng cùng đơn vị có nhu cầu đăng nhập bằng tài khoản AD.
11A. Chọn phiếu đang "Chờ ký xác nhận".
12A. Người dùng chỉ được:
- Sửa dòng của mình (nếu đã có).
- Thêm dòng mới (nếu chưa có dòng thông tin của mình).
- Không giới hạn số lượng người dùng.
13A. Người dùng ký xác nhận dòng chi tiết của mình.
- Cơ chế: Row-level locking + Polling (cập nhật real-time mỗi X giây).
- Mỗi người chỉ ký 1 lần trên 1 phiếu.
14A. Người lập phiếu thực hiện 1 trong 2:

a) **Ký xác nhận & Gửi phê duyệt:**
- Các dòng chưa ký xác nhận sẽ tự động bị xóa.
- Hệ thống lưu trạng thái `Chờ phê duyệt`.
- Gửi email notification đến Trưởng phòng/tương đương.
- → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

b) **Hủy phiếu:**
- Không cần lý do.
- Trạng thái → `Đã hủy`.
- → Kết thúc luồng.

### Nhánh B: Ký xác nhận & Gửi (Người lập chính là người ký chi tiết)

7B. Hệ thống kiểm tra trường bắt buộc + validation rules.
8B. Hệ thống sinh mã yêu cầu.
9B. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
10B. Hệ thống gửi email notification đến Trưởng phòng/tương đương.
11B. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

### Timeout:

- **Mẫu 01-YCTC:** Hết thời gian ca đã chọn. Khi người lập ký gửi, dòng chưa ký tự động bị xóa.
- **Mẫu 04A-YCTK:** Trong ngày lập phiếu hoặc khi người lập ký gửi. Dòng chưa ký tự động bị xóa.

## 5. Luồng 3: 05A-YCKC (Truy cập khẩn cấp — Gửi thẳng BP Mở truy cập)

1. Người yêu cầu đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", mẫu 05A-YCKC.
3. Chọn Hệ thống (1 HT) → Chọn CSDL (1 CSDL).
4. Chọn Ca truy cập (Ca 1: 0h-8h / Ca 2: 8h-20h / Ca 3: 20h-24h).
5. Hệ thống tự fill "Thời gian yêu cầu" theo ca, không cho phép sửa.
6. Nhập nội dung chi tiết (Mục đích/Lý do, Quyền trên đối tượng dữ liệu).
7. Người lập ký xác nhận.
8. Hệ thống kiểm tra trường bắt buộc + validation rules.
9. Hệ thống sinh mã yêu cầu.
10. Hệ thống lưu hồ sơ ở trạng thái `Đã chuyển bộ phận Mở truy cập`.
11. Hệ thống gửi email notification đến Bộ phận mở truy cập.
12. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

## 6. Luồng 4: 04B-BGTK, 05B-HTKC (Phiếu bổ sung sau hoàn thành)

### 6.1 Luồng 04B-BGTK (DBA lập)

**Điều kiện hiển thị:** Chỉ hiển thị đối với DBA (kiểm tra quyền).
**Điều kiện lập:** Phiếu 04A-YCTK liên quan phải ở trạng thái "Hoàn thành".

1. DBA đăng nhập bằng tài khoản AD.
2. Chọn chức năng "Lập yêu cầu", mẫu 04B-BGTK.
3. Hệ thống hiển thị danh sách 04A-YCTK đã hoàn thành nhưng chưa có 04B-BGTK tương ứng.
4. DBA chọn phiếu đang nợ.
5. Hệ thống tự động fill nội dung từ 04A-YCTK:
   - Tên hệ thống, Tên CSDL.
   - Mã yêu cầu 04A liên quan.
   - Thời gian bàn giao (ngày hiện tại).
   - Đại diện BP quản trị CSDL (Cấp QL) — từ cấu hình.
   - Người bàn giao (DBA) — user đăng nhập.
   - Đại diện BP nhận bàn giao (Cấp QL) — lãnh đạo phòng người yêu cầu.
   - Người nhận bàn giao — danh sách người dùng từ 04A.
   - Chi tiết: Loại tài khoản, Phạm vi, Nội dung, Chủ tài khoản.
   - **KHÔNG tự fill:** Tài khoản được cấp (UserID) — DBA nhập tay.
6. DBA nhập thông tin tài khoản đã cấp + Địa điểm bàn giao.
7. DBA ký xác nhận.
8. Hệ thống kiểm tra trường bắt buộc + validation rules.
9. Hệ thống sinh mã yêu cầu.
10. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
11. Hệ thống gửi email notification đến Lãnh đạo phòng quản trị CSDL.
12. → Chuyển scope phê duyệt (Lãnh đạo phòng DBA duyệt).
13. Sau khi duyệt → Trạng thái chuyển `Chờ ký nhận`.
14. Hệ thống gửi email notification cho người dùng trong danh sách.
15. Người dùng đăng nhập → Ký nhận dòng của mình (row-level locking, polling).
16. Khi tất cả người dùng đã ký → Hệ thống tự động chuyển `Chờ phê duyệt` (Lãnh đạo phòng người dùng).
17. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt lần 2.

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
   - Mã yêu cầu: Lấy định dạng từ 05A đang nợ, trường "Lần" hiển thị tổng hợp (VD: Lan01-02-03). CSDL lưu thêm trường phân biệt loại phiếu (05A/05B).
   - Danh sách bảng = union tất cả bảng từ các phiếu 05A trong ca.
   - Thông tin chung: Hệ thống, CSDL, Ngày, Ca, Thời gian.
6. Người lập nhập nội dung công việc đã thực hiện (mô tả chi tiết, câu lệnh) — bắt buộc.
7. Người lập ký xác nhận.
8. Hệ thống kiểm tra trường bắt buộc + validation rules.
9. Hệ thống lưu hồ sơ ở trạng thái `Chờ phê duyệt`.
10. Hệ thống gửi email notification đến Lãnh đạo phòng của người lập.
11. → Kết thúc scope lập yêu cầu, chuyển scope phê duyệt.

## 7. Hệ thống trạng thái phiếu (Scope lập yêu cầu)

### 7.1 Bảng mapping Status Code ↔ Tên hiển thị

> Tham chiếu: `docs/workflow-step-codes.md` mục 2 "Trạng thái đặc biệt".

| Status Code | Tên hiển thị | Áp dụng cho | Ghi chú |
|---|---|---|---|
| `DRAFT` | Nháp | Tất cả mẫu | Phiếu đã lưu, chưa ký, chưa gửi |
| `PENDING_SIGN` | Chờ ký xác nhận | 01-YCTC, 04A-YCTK | Đã sinh mã, chờ người dùng chung ký dòng chi tiết |
| `PENDING_RECEIPT` | Chờ ký nhận | 04B-BGTK | Đã được lãnh đạo DBA duyệt, chờ người dùng ký nhận tài khoản |
| `PENDING_CHECK` | Chờ kiểm tra | 02-YCCS, 03-YCCT | Đã ký, gửi bộ phận kiểm tra. `current_step_code` = step đầu tiên (VD: `02_I_01` hoặc `02_E_01`) |
| `PENDING_APPROVAL` | Chờ phê duyệt | 01, 04A, 04B, 05B | Đã ký đầy đủ, chờ lãnh đạo phê duyệt. `current_step_code` = step đầu tiên |
| `PENDING_ACCESS_TEAM` | Đã chuyển BP Mở truy cập | 05A-YCKC | Gửi thẳng bộ phận mở truy cập. `current_step_code` = `05A_01` |
| `RETURNED` | Chuyển trả | Tất cả | Bị chuyển trả, chờ requester sửa lại |
| `CANCELLED` | Đã hủy | Tất cả | Người lập hủy phiếu (không cần lý do) |
| `COMPLETED` | Hoàn thành | Tất cả | Đã hoàn thành toàn bộ luồng |

### 7.2 Logic khởi tạo Workflow khi SUBMIT

> Tham chiếu: `docs/workflow-step-codes.md` mục 3 "Quy tắc xác định Variant" và mục 9.1 "Module Request — Khi SUBMIT".

Khi người lập ấn "Gửi phê duyệt" / "Gửi kiểm tra" / "Gửi BP Mở truy cập", module Request thực hiện tuần tự:

1. **Xác định variant (I/E):**
   - Lấy `owner_unit_id` từ `information_system` (đơn vị chủ quản ứng dụng).
   - Nếu `requester_unit_id == owner_unit_id` → variant = `I` (Internal).
   - Nếu `requester_unit_id != owner_unit_id` → variant = `E` (External).
   - Luồng 03-YCCT, 05A-YCKC: không có variant (chỉ 1 luồng duy nhất).

2. **Set `current_step_code`:** Theo format `{MÃ_MẪU}_{VARIANT}_{01}` hoặc `{MÃ_MẪU}_{01}` (nếu không có variant).
   - Ví dụ: 01-YCTC Internal → `01_I_01`; 02-YCCS External → `02_E_01`; 05A → `05A_01`.

3. **Set `at_requester_phase`:** Theo bảng mapping tại `workflow-step-codes.md` mục 7.
   - Variant `I` → luôn `false`.
   - Variant `E`, step 01/02 → `true` (đang ở đơn vị yêu cầu).

4. **Set `owner_unit_id`:** Đơn vị chủ quản ứng dụng (từ `information_system`).

5. **Set `owner_db_unit_id`:** Đơn vị chủ quản CSDL (từ `database_catalog`) — chỉ áp dụng cho 03, 04A.

6. **Gọi `resolveNextActor()`:** Xác định actor xử lý bước đầu tiên → set `current_actor_type`, `current_actor_id`, `current_actor_role`, `current_unit_id`.

7. **Set `status`:** Theo bảng mapping mục 7.1:
   - 01, 04A (Nhánh B), 04B, 05B → `PENDING_APPROVAL`
   - 01, 04A (Nhánh A lưu chờ ký) → `PENDING_SIGN`
   - 02, 03 → `PENDING_CHECK`
   - 05A → `PENDING_ACCESS_TEAM`

8. **Ghi `workflow_history`:** action = `SUBMIT`, step_code = step đầu tiên.

### 7.3 Bảng trạng thái (tham chiếu nhanh)

| Trạng thái | Áp dụng cho | Mô tả | Chuyển tiếp |
|---|---|---|---|
| Nháp | Tất cả mẫu | Phiếu đã lưu, chưa ký, chưa gửi | → Chờ ký xác nhận / Chờ phê duyệt / Chờ kiểm tra / Đã chuyển BP Mở truy cập |
| Chờ ký xác nhận | 01-YCTC, 04A-YCTK | Đã sinh mã, chờ người dùng chung ký dòng chi tiết | → Chờ phê duyệt / Đã hủy |
| Chờ ký nhận | 04B-BGTK | Đã được lãnh đạo DBA duyệt, chờ người dùng ký nhận tài khoản | → Chờ phê duyệt (lần 2) |
| Chờ kiểm tra | 02-YCCS, 03-YCCT | Đã ký, gửi bộ phận kiểm tra | → Scope phê duyệt |
| Chờ phê duyệt | 01, 04A, 04B, 05B | Đã ký đầy đủ, chờ lãnh đạo phê duyệt | → Scope phê duyệt |
| Đã chuyển BP Mở truy cập | 05A-YCKC | Gửi thẳng bộ phận mở truy cập | → Scope phê duyệt |
| Đã hủy | Tất cả | Người lập hủy phiếu (không cần lý do) | Kết thúc |
| Hoàn thành | 04B-BGTK | Tất cả người dùng đã ký nhận | Kết thúc |

## 8. Validation Rules

| # | Rule | Chi tiết | Áp dụng |
|---|---|---|---|
| 1 | Trường bắt buộc | Kiểm tra tất cả trường bắt buộc trước khi cho phép ký/gửi | Tất cả |
| 2 | Chặn nợ 05B | Người dùng có phiếu 05A đã "Hoàn thành" quá 03 ngày mà chưa lập 05B → Chặn tất cả chức năng lập yêu cầu (trừ 05B). Chặn luôn trường hợp người khác thêm dòng cho người bị chặn vào phiếu 01 đang "Chờ ký xác nhận". Thông báo: "Bạn đang nợ phiếu 05B-HTKC quá hạn. Vui lòng hoàn thành trước khi lập yêu cầu mới." (kèm link đến mẫu 05B). Gỡ chặn khi hoàn thành tất cả 05B đang nợ. | Tất cả |
| 3 | 1 HT + 1 CSDL | Mỗi phiếu chỉ được chọn 1 Hệ thống và 1 CSDL | 02, 03, 04A, 05A |
| 4 | Phiếu gốc hoàn thành | Chỉ lập 04B/05B khi phiếu 04A/05A tương ứng ở trạng thái "Hoàn thành" | 04B, 05B |
| 5 | Checksum file SQL | Hỗ trợ MD5 + SHA-256 (người dùng chọn loại). Luồng: Upload file → Hệ thống tự tính hash → Người dùng nhập checksum gốc → So sánh. Match = OK, cho tiếp. Không match = Báo lỗi "Mã kiểm tra không khớp", chặn gửi. Format validation: MD5=32 ký tự hex, SHA-256=64 ký tự hex. | 02, 03 |
| 6 | Giới hạn file SQL | Tối đa 10MB | 02, 03 |
| 7 | Format tên file SQL | Phải đúng định dạng `YYYYMMDD_BS_XXX.sql` | 02-YCCS |
| 8 | Mẫu 03 - Nội dung/Script | 1 file SQL Script chung cho toàn bộ phiếu (có thể bao gồm cả Tạo mới/Thay đổi/Xóa hoặc chỉ 1 phần). Nếu có file SQL Script + checksum khớp → nội dung chi tiết các tab KHÔNG bắt buộc. Nếu KHÔNG có file SQL Script → nội dung chi tiết của tất cả tab đã chọn PHẢI có dữ liệu. | 03-YCCT |
| 9a | Timeout 01-YCTC | Hết thời gian ca đã chọn. Khi người lập ký gửi, dòng chưa ký tự động bị xóa. | 01-YCTC |
| 9b | Timeout 04A-YCTK | Trong ngày lập phiếu hoặc khi người lập ký gửi. Dòng chưa ký tự động bị xóa. | 04A-YCTK |
| 9c | Timeout 04B ký nhận | 3 ngày kể từ khi chuyển "Chờ ký nhận" → Email cho DBA, lãnh đạo phòng DBA, lãnh đạo phòng cán bộ. KHÔNG hủy phiếu, giữ nguyên. | 04B-BGTK |
| 10a | Trùng lặp 01-YCTC | Chặn khi trùng cả 4 nội dung: Hệ thống + CSDL + Đối tượng + Người dùng (trong cùng phiếu) | 01-YCTC |
| 10b | Trùng lặp 04A-YCTK | Chặn trùng người dùng ở bảng chi tiết (trong cùng phiếu) | 04A-YCTK |
| 11 | Thời gian tạo phiếu | Ngày + Ca: chỉ cho phép hiện tại hoặc tương lai, không được chọn quá khứ (trừ 04B, 05B — phiếu bổ sung) | Tất cả (trừ 04B, 05B) |
| 12 | Concurrency | Row-level locking + Polling. Mỗi người thao tác dòng riêng, không conflict. Khi 1 người ký xong → các người khác đang mở phiếu thấy cập nhật. Người lập có quyền xóa dòng chưa ký. | 01, 04A, 04B |
| 13 | Kiểm tra quyền | Kiểm tra quyền người dùng trước khi cho phép chọn mẫu. VD: 04B chỉ hiển thị cho DBA. | Tất cả |
| 14 | Mẫu 01 - Quyền truy cập | Loại "Truy vấn" → chỉ SELECT (auto-checked, không cho bỏ). Loại "Chỉnh sửa" → multi-select từ SELECT/INSERT/UPDATE/DELETE (chọn ≥1). Khi đổi loại → cảnh báo reset quyền đã chọn. | 01-YCTC |
| 15 | Không ký trùng | 1 người chỉ ký 1 lần trên 1 phiếu (1 dòng duy nhất) | 01, 04A, 04B |

## 9. Quy tắc nghiệp vụ chung

- Ngày lập yêu cầu = ngày hiện tại (dd/MM/yyyy), KHÔNG cho phép sửa (tất cả mẫu).
- Thời gian truy cập/truy xuất tự fill theo Ca, KHÔNG cho phép sửa (01, 05A, 05B).
- Thời gian cập nhật (02-YCCS): tự fill theo Ca, CHO PHÉP chỉnh sửa, KHÔNG được để trống.
- Ca truy cập: Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h).
- Khi lập yêu cầu, hệ thống ràng buộc danh mục CSDL, người dùng với đơn vị chủ quản ứng dụng; chỉ cho phép chọn danh mục hợp lệ.
- Cho phép lưu nháp và sửa lại phiếu nếu chưa gửi phê duyệt.
Xử lý gửi thất bại:
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
- Mẫu 01-YCTC cho phép yêu cầu nhiều HT + CSDL trên 1 phiếu (mỗi dòng chi tiết có thể chọn HT/CSDL khác nhau).
- Quyền truy cập: Multi-select Checkbox Group (không phải dropdown). Hiển thị trực quan 4 options.
- Mỗi người dùng chỉ cần ký xác nhận một lần trên phần thông tin chi tiết.
- Trường nội dung nào mà cán bộ đã ký xác nhận thì không được sửa lại nội dung.

## 11. Quy tắc riêng mẫu 03-YCCT

- Loại yêu cầu: Checkbox — chọn ít nhất 1, có thể chọn nhiều hoặc cả 3 (Tạo mới / Thay đổi / Xóa). Khi chọn mục nào → hiển thị phần nội dung tương ứng.
- SQL Script là mục chung cho toàn bộ phiếu (1 file duy nhất, có thể bao gồm cả Tạo mới/Thay đổi/Xóa hoặc chỉ 1 phần trong 3).
- Nếu có file SQL Script + checksum khớp → nội dung chi tiết các tab KHÔNG bắt buộc (có thể để trống).
- Nếu KHÔNG có file SQL Script → nội dung chi tiết của tất cả tab đã chọn PHẢI có dữ liệu (ít nhất 1 dòng/trường mỗi tab).
- Có phần nội dung DBA ghi để đánh giá tác động ảnh hưởng và hệ thống liên quan (hiển thị để trống, read-only — thuộc scope phê duyệt).
- Tối đa 10MB cho file SQL Script.

## 12. Giao diện mẫu 01-YCTC

### Thông tin chung

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Loại yêu cầu | Dropdown | ✅ | Truy vấn / Chỉnh sửa — áp dụng toàn bộ phiếu, ở đầu phiếu |
| Mã yêu cầu | Tự động | ✅ | Sinh khi lưu/gửi. Format: KýhiệuĐV_DDMMYYYYCa:Lần: |
| Ca | Dropdown | ✅ | Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h) |
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
| Hệ thống thông tin | Dropdown | ✅ | Cho phép chọn nhiều HT khác nhau trên 1 phiếu |
| CSDL | Dropdown | ✅ | Theo HT đã chọn |
| Tên đối tượng | Nhập text | ✅ | Bảng/đối tượng dữ liệu |
| Quyền truy cập | Multi-select Checkbox | ✅ | SELECT, INSERT, UPDATE, DELETE. Logic: Loại "Truy vấn" → chỉ SELECT (auto-checked); Loại "Chỉnh sửa" → chọn ≥1 |
| Họ và tên | Nhập/Tự động | ✅ | Nếu người dùng tự thêm dòng → tự fill tên |
| Ký tên | Ký điện tử (OTP) | ✅ | Mỗi người ký dòng của mình. Nếu người lập và người truy cập là một thì không cần ký tại mục chi tiết |

## 13. Giao diện mẫu 02-YCCS

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Dropdown | ✅ | Chọn 1 HT duy nhất, ở đầu phiếu |
| Tên CSDL | Dropdown | ✅ | Chọn 1 CSDL duy nhất, theo HT đã chọn |
| Mã yêu cầu | Tự động | ✅ | Sinh khi gửi. Format: KýhiệuĐV_DDMMYYYYCa:Lần: |
| Ca | Dropdown | ✅ | Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h) |
| Tên đơn vị yêu cầu | Tự động | ✅ | Lấy từ thông tin đăng nhập |
| Tên phòng hoặc tương đương | Tự động | ✅ | Lấy từ thông tin đăng nhập/cấu hình |
| Người yêu cầu | Tự động | ✅ | Lấy từ user đăng nhập |
| ĐTDĐ | Tự động | ✅ | Lấy từ hồ sơ người dùng |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại (dd/MM/yyyy), KHÔNG cho phép sửa |
| Thời gian cập nhật (Bắt đầu/Kết thúc) | Tự động + Cho sửa | ✅ | Tự fill theo ca, CHO PHÉP chỉnh sửa, KHÔNG được để trống |
| Tên tệp cần chạy | Upload file | ✅ | Định dạng: YYYYMMDD_BS_XXX.sql. Tối đa 10MB. Nếu nhiều file cần gộp thành một |
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
| Mã yêu cầu | Tự động | ✅ | Sinh khi gửi |
| Ca | Dropdown | ✅ | Ca 1 / Ca 2 / Ca 3 |
| Tên đơn vị yêu cầu | Tự động | ✅ | |
| Tên phòng hoặc tương đương | Tự động | ✅ | |
| Người yêu cầu | Tự động | ✅ | |
| ĐTDĐ | Nhập/Tự động | ✅ | Số điện thoại liên hệ |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Ngày thực hiện dự kiến | Nhập (dd/MM/yyyy) | ❌ | Không bắt buộc |
| Loại yêu cầu | Checkbox/Radio | ✅ | Tạo mới / Thay đổi / Xóa (chọn ít nhất 1) — quyết định tab nào hiển thị |
| Đơn vị chủ quản ứng dụng | Nhập/Chọn | ✅ | |
| Đơn vị chủ quản quản trị CSDL | Nhập/Chọn | ✅ | |
| Ký tên (người lập) | Ký điện tử (OTP) | ✅ | |
| Danh sách Trưởng phòng/tương đương | Tự động | ✅ | |
| Phần DBA ghi (Đánh giá tác động) | Hiển thị | — | Để trống, read-only (thuộc scope phê duyệt, DBA nhập sau khi người lập gửi kiểm tra) |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |
| Kết quả thực hiện | Hiển thị | — | Để trống, read-only (thuộc scope phê duyệt) |

### SQL Script (Mục chung — áp dụng cho toàn bộ phiếu)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên tệp SQL Script | Upload file | Có điều kiện | Tối đa 10MB. File script có thể bao gồm toàn bộ nội dung Tạo mới/Thay đổi/Xóa hoặc chỉ 1 phần trong 3 |
| Loại checksum | Dropdown | Có điều kiện | MD5 / SHA-256. Bắt buộc nếu có file |
| Mã kiểm tra (Checksum) | Nhập text | Có điều kiện | Bắt buộc nếu có file. Hệ thống tự tính hash → so sánh |

**Ràng buộc:**
- Nếu **có file SQL Script** → checksum phải khớp. Nội dung chi tiết các tab bên dưới **KHÔNG bắt buộc**.
- Nếu **KHÔNG có file SQL Script** → nội dung chi tiết của **tất cả tab đã chọn** PHẢI có dữ liệu.

### Tab Tạo mới (hiển thị khi chọn "Tạo mới")

| Mục | Trường | Bắt buộc | Ghi chú |
|---|---|---|---|
| Table | Owner, Table name, dự kiến tăng trưởng, vòng đời lưu trữ, cột xác định vòng đời, đối tượng phụ thuộc | Có điều kiện | Bắt buộc nếu không có SQL Script |
| Cấu trúc table | Tên bảng, tên cột, kiểu dữ liệu, cho phép Null (Y/N), giá trị mặc định, mô tả | Có điều kiện | Bắt buộc nếu không có SQL Script |
| Index | Owner, tên index, table owner, tên bảng, danh sách cột đánh chỉ mục | Có điều kiện | |
| Synonym | Tên synonym, kiểu Public/Private, table owner, tên bảng, mô tả | Có điều kiện | |
| Tạo mới khác | Owner, tên, kiểu, mô tả | Có điều kiện | |

### Tab Thay đổi (hiển thị khi chọn "Thay đổi")

| Mục | Trường | Bắt buộc | Ghi chú |
|---|---|---|---|
| Thêm cột bảng | Owner, tên bảng, tên cột, loại dữ liệu, mô tả | Có điều kiện | Bắt buộc nếu không có SQL Script |
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
| Mã yêu cầu | Tự động | ✅ | Sinh khi lưu/gửi |
| Ca | Dropdown | ✅ | Ca 1 / Ca 2 / Ca 3 |
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
| Họ tên chủ tài khoản | Nhập/Tự động | ✅ | |
| Loại tài khoản | Dropdown | ✅ | Truy vấn / Chỉnh sửa — mỗi dòng tự chọn riêng |
| Hình thức | Dropdown | ✅ | Cấp mới / Đổi thuộc tính — mỗi dòng tự chọn riêng |
| Ký tên | Ký điện tử (OTP) | ✅ | Mỗi người ký dòng của mình. Nếu người lập và người truy cập là một thì không cần ký tại mục chi tiết |

**Lưu ý:** Logic ký tương tự mẫu 01-YCTC (Nhánh A/B, row-level locking, polling).

## 16. Giao diện mẫu 04B-BGTK

**Điều kiện hiển thị:** Chỉ hiển thị đối với DBA.

### Màn hình chọn phiếu nợ

| Trường | Loại | Ghi chú |
|---|---|---|
| Danh sách 04A đang nợ | Danh sách chọn | Hiển thị các phiếu 04A hoàn thành chưa có 04B |

### Form nhập liệu (sau khi chọn phiếu)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Tự động | ✅ | Fill từ 04A, read-only |
| Tên CSDL | Tự động | ✅ | Fill từ 04A, read-only |
| Mã yêu cầu 04B | Tự động | ✅ | Sinh mã mới |
| Mã yêu cầu 04A liên quan | Tự động | ✅ | Fill từ 04A, read-only |
| Thời gian bàn giao | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Địa điểm | Nhập text | ✅ | DBA nhập |
| Đại diện BP quản trị CSDL (Cấp QL) | Tự động | ✅ | Lấy từ cấu hình |
| Người bàn giao (DBA) | Tự động | ✅ | User đăng nhập |
| Đại diện BP nhận bàn giao (Cấp QL) | Tự động | ✅ | Lãnh đạo phòng người yêu cầu (từ 04A) |
| Người nhận bàn giao | Tự động | ✅ | Danh sách người dùng từ 04A |
| Ký tên DBA (Người bàn giao) | Ký điện tử (OTP) | ✅ | DBA ký |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |

### Chi tiết bàn giao (bảng)

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tài khoản (UserID) | Nhập text | ✅ | DBA nhập tay — KHÔNG tự fill |
| Loại tài khoản (QUERY/UPDATE) | Tự động | ✅ | Fill từ 04A |
| Phạm vi | Tự động | ✅ | Fill từ 04A |
| Nội dung (cấp mới) | Tự động | ✅ | Fill từ 04A |
| Chủ tài khoản | Tự động | ✅ | Fill từ 04A |
| Ký nhận (Người dùng) | Ký điện tử (OTP) | ✅ | Mỗi người ký dòng mình (sau khi lãnh đạo DBA duyệt, trạng thái "Chờ ký nhận") |

## 17. Giao diện mẫu 05A-YCKC

| Trường | Loại | Bắt buộc | Ghi chú |
|---|---|---|---|
| Tên hệ thống | Dropdown | ✅ | Chọn 1 HT duy nhất |
| Tên CSDL | Dropdown | ✅ | Chọn 1 CSDL duy nhất |
| Mã yêu cầu | Tự động | ✅ | Sinh khi gửi |
| Ca | Dropdown | ✅ | Ca 1 (0h-8h) / Ca 2 (8h-20h) / Ca 3 (20h-24h) |
| Tên đơn vị yêu cầu | Tự động | ✅ | |
| Tên phòng hoặc tương đương | Tự động | ✅ | |
| Người yêu cầu | Tự động | ✅ | |
| ĐTDĐ | Tự động | ✅ | |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Thời gian yêu cầu (Từ/Đến) | Tự động | ✅ | Fill theo ca, KHÔNG cho phép sửa |
| Mục đích/Lý do yêu cầu truy cập, truy xuất | Nhập text | ✅ | Bắt buộc |
| Quyền trên đối tượng dữ liệu | Chọn/Nhập | ✅ | "Query all data only" (nếu tích chọn → các quyền chi tiết disable) HOẶC chi tiết bảng: Owner, Table name, Select/Insert/Update/Delete |
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
| Mã yêu cầu | Tự động | ✅ | Lấy định dạng từ 05A, trường "Lần" tổng hợp (VD: Lan01-02-03). CSDL lưu thêm trường phân biệt loại phiếu (05A/05B) |
| Ca | Tự động | ✅ | Fill từ 05A, read-only |
| Lần | Tự động | ✅ | Hiển thị tổng hợp: "Lần: 1, 2, 3" |
| Tên đơn vị yêu cầu | Tự động | ✅ | |
| Tên phòng hoặc tương đương | Tự động | ✅ | |
| Người yêu cầu | Tự động | ✅ | |
| ĐTDĐ | Tự động | ✅ | |
| Ngày lập yêu cầu | Tự động | ✅ | Ngày hiện tại, KHÔNG cho phép sửa |
| Thời gian yêu cầu (Từ/Đến) | Tự động | ✅ | Fill từ 05A, read-only |
| Danh sách bảng đã yêu cầu | Tự động | ✅ | Union tất cả bảng từ các phiếu 05A trong ca. Cột: Owner, Table name, Select/Insert/Update/Delete |
| Mục đích truy cập, truy xuất (mô tả chi tiết, câu lệnh thực hiện) | Nhập text | ✅ | Bắt buộc — người lập nhập nội dung công việc đã thực hiện |
| Ký tên (người lập) | Ký điện tử (OTP) | ✅ | |
| Xác nhận (ĐV chủ quản ứng dụng + ĐV yêu cầu) | Hiển thị | — | Để trống, read-only (thuộc scope phê duyệt) |
| Ô ký phê duyệt | Hiển thị | — | Để trống, read-only |

## 19. Chức năng con: Đăng ký trước Yêu cầu chi tiết (Mẫu 01-YCTC)

### 19.1 Mô tả chức năng

Cho phép người dùng có quyền lập yêu cầu (requester) đăng ký trước thông tin chi tiết truy cập CSDL cho ngày + ca hiện tại hoặc tương lai. Khi người lập phiếu 01-YCTC chọn ca, hệ thống tự động nạp toàn bộ đăng ký trước phù hợp của tất cả người dùng cùng đơn vị vào bảng chi tiết phiếu.

**Lợi ích:**
- Tiết kiệm thời gian lập phiếu.
- Người dùng chủ động đăng ký trước khi cần truy cập.
- Giảm sai sót do nhập liệu thủ công.

### 19.2 Vị trí giao diện

- Nút/link phụ **"Đăng ký trước Yêu cầu chi tiết"** nằm bên trong card/button của mẫu 01-YCTC tại màn hình chọn mẫu.
- Khi click → điều hướng sang trang "Đăng ký trước Yêu cầu chi tiết".

### 19.3 Quyền truy cập

- Bất kỳ người dùng nào có quyền lập yêu cầu (requester) đều có chức năng này.

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
| Ngày đăng ký | Date picker | ✅ | Chỉ cho phép hiện tại hoặc tương lai (dd/MM/yyyy) |
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

### 19.6 Hệ thống trạng thái bản ghi đăng ký trước

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

### 19.7 Logic nạp tự động vào phiếu 01-YCTC

**Trigger:** Khi người lập phiếu 01-YCTC chọn "Ca" (sau khi đã chọn "Loại yêu cầu").

**Quy trình:**
1. Hệ thống truy vấn bảng `pre_registration_request` với điều kiện:
   - `unit_code` = đơn vị của người lập.
   - `register_date` = ngày hiện tại (ngày lập phiếu).
   - `shift` = ca đã chọn.
   - `status` = "Chưa dùng".
   - Logic loại yêu cầu:
     - Nếu người lập chọn **"Truy vấn"** → chỉ nạp bản ghi có `request_type` = "Truy vấn" (tức `access_rights` chỉ có SELECT).
     - Nếu người lập chọn **"Chỉnh sửa"** → nạp toàn bộ bản ghi phù hợp ca (cả Truy vấn lẫn Chỉnh sửa).
2. Nạp tất cả bản ghi phù hợp vào bảng chi tiết phiếu 01.
3. Các dòng đã nạp hiển thị:
   - Trạng thái **"Đã ký"** (tự fill chữ ký từ bản ghi đăng ký trước).
   - **Không cho phép sửa/xóa** trên phiếu (vì đã ký số từ trước).
4. Người lập vẫn có thể thêm dòng mới (nhập thủ công) bên cạnh các dòng đã nạp.

**Xử lý khi đổi "Loại yêu cầu":**
- Nếu đổi từ "Chỉnh sửa" → "Truy vấn": Các dòng đăng ký trước có quyền INSERT/UPDATE/DELETE tự động bị xóa khỏi phiếu. Bản ghi đăng ký trước giữ nguyên trạng thái "Chưa dùng".
- Cảnh báo: "Thay đổi loại yêu cầu sẽ xóa các dòng đăng ký trước không phù hợp khỏi phiếu" → Confirm → Thực hiện.

### 19.8 Validation Rules bổ sung

| # | Rule | Chi tiết | Áp dụng |
|---|---|---|---|
| 16 | Không đăng ký quá khứ | Ngày + Ca: chỉ cho phép hiện tại hoặc tương lai | Đăng ký trước |
| 17 | Kiểm tra trùng lặp đăng ký | Chặn khi trùng: user_id + register_date + shift + system_name + database_name + object_name + access_rights | Đăng ký trước |
| 18 | Chỉ sửa/xóa khi "Chưa dùng" | Không cho phép sửa/xóa bản ghi ở trạng thái khác "Chưa dùng" | Đăng ký trước |
| 19 | Ký số lại khi sửa | Sau khi sửa nội dung đăng ký → yêu cầu ký OTP lại | Đăng ký trước |
| 20 | Xóa vĩnh viễn | Xóa bản ghi khỏi CSDL (hard delete), chỉ khi status = "Chưa dùng" | Đăng ký trước |
| 21 | Hết hạn tự động | Cron job kiểm tra: register_date + shift đã qua → chuyển status = "Hết hạn" | Đăng ký trước |
| 22 | Nạp tự động | Chỉ nạp bản ghi status = "Chưa dùng". Không nạp "Chờ duyệt", "Đã dùng", "Hết hạn" | Phiếu 01 |

### 19.9 Verification bổ sung

- [ ] Đăng ký trước thành công + ký OTP.
- [ ] Đăng ký cho ngày/ca tương lai.
- [ ] Chặn đăng ký ngày/ca quá khứ.
- [ ] Chặn đăng ký trùng lặp.
- [ ] Nhân bản dòng sang ngày/ca khác.
- [ ] Sửa đăng ký "Chưa dùng" → yêu cầu ký lại.
- [ ] Xóa đăng ký "Chưa dùng" → xóa vĩnh viễn.
- [ ] Không cho sửa/xóa khi trạng thái khác "Chưa dùng".
- [ ] Nạp tự động khi lập phiếu 01 chọn ca: đúng đơn vị + ngày + ca + loại yêu cầu.
- [ ] Dòng nạp hiển thị "Đã ký", không cho sửa/xóa trên phiếu.
- [ ] Đổi loại yêu cầu "Chỉnh sửa" → "Truy vấn": xóa dòng không phù hợp, bản ghi giữ "Chưa dùng".
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
- [ ] Chặn nợ 05B: quá 3 ngày → chặn lập yêu cầu + chặn thêm vào phiếu 01 chung. Thông báo đúng + link đến 05B.
- [ ] Mẫu 05A sau gửi vào trạng thái "Đã chuyển bộ phận Mở truy cập".
- [ ] File SQL 02-YCCS kiểm tra đúng định dạng tên (YYYYMMDD_BS_XXX.sql).
- [ ] Lập 04B-BGTK từ 04A đã hoàn thành, kiểm tra auto-fill đúng.
- [ ] Lập 05B-HTKC, kiểm tra gộp tự động các 05A chung HT+CSDL+Ngày+Ca.
- [ ] Mã 05B hiển thị đúng format Lan01-02-03.
- [ ] Timeout 01-YCTC: hết ca → dòng chưa ký bị xóa khi gửi.
- [ ] Timeout 04A-YCTK: hết ngày → dòng chưa ký bị xóa khi gửi.
- [ ] Timeout 04B-BGTK: 3 ngày → email notification gửi đúng người.
- [ ] Checksum file SQL: match → OK; không match → chặn gửi.
- [ ] Giới hạn file 10MB: vượt → báo lỗi.
- [ ] Trùng lặp 01 (4 trường): chặn đúng.
- [ ] Trùng lặp 04A (người dùng): chặn đúng.
- [ ] Concurrency: nhiều người ký đồng thời trên 01/04A/04B → không conflict.
- [ ] Kiểm tra quyền: 04B chỉ hiển thị cho DBA.
- [ ] Chặn chọn ca/ngày quá khứ (trừ 04B, 05B).
- [ ] Mẫu 01: logic Truy vấn → chỉ SELECT; Chỉnh sửa → multi-select.
- [ ] Đổi loại yêu cầu 01 → cảnh báo reset → confirm → reset đúng.
- [ ] 04B: tất cả người dùng ký nhận → tự động chuyển "Chờ phê duyệt" lần 2.
- [ ] Mẫu 02: Thời gian cập nhật tự fill theo ca, cho phép sửa, không được để trống.
- [ ] Tất cả phần read-only (ô ký phê duyệt, kết quả thực hiện, phần DBA ghi, phần thực hiện mở truy cập) hiển thị đúng, không cho nhập.

## 23. Definition of Done

- Hoàn thành form cho **7 mẫu phiếu** (01-YCTC, 02-YCCS, 03-YCCT, 04A-YCTK, 04B-BGTK, 05A-YCKC, 05B-HTKC).
- Hoàn thành chức năng con "Đăng ký trước Yêu cầu chi tiết" cho mẫu 01-YCTC.
- Có lưu nháp, sửa nháp, ký, gửi, hủy, gửi lại.
- Có validate nghiệp vụ và validate giao diện.
- Có test cho các luồng chính và lỗi nghiệp vụ quan trọng.

---

## Giả định

1. Hệ thống đã có sẵn danh mục Hệ thống + CSDL để dropdown.
2. Hệ thống đã có email notification service.
3. Cơ chế ký điện tử đã được xác định (OTP).
4. Thông tin user (đơn vị, phòng ban, lãnh đạo) lấy từ AD/LDAP.

## Rủi ro

1. Concurrency polling interval chưa xác định cụ thể (cần xác định ở giai đoạn thiết kế kỹ thuật).
2. Logic gộp 05A cho 05B phức tạp — cần test kỹ edge cases (VD: 05A bị hủy giữa chừng).
3. Timeout 04B (3 ngày) — cần cron job/scheduler để kiểm tra và gửi email.
4. Cron job hết hạn đăng ký trước — cần scheduler chạy định kỳ (VD: mỗi giờ hoặc mỗi ca).