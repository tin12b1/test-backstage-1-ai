
# Workflow Step Codes - Danh mục mã bước xử lý

> **Phiên bản:** 1.1  
> **Ngày tạo:** 06/07/2026  
> **Người tạo:** Quang (Approval Processing)  
> **Liên quan:** ADR-0002, approval-processing.md, database-schema.md, features/request-create.md

### Changelog

| Thời điểm | Nội dung thay đổi |
|---|---|
| 06/07/2026 | v1.0 — Bản khởi tạo (Quang) |
| 06/07/2026 11:30 | v1.1 — Bổ sung luồng 04B-BGTK (mục 4.8, 5.9); Thêm 04B vào StepCode enum; Cập nhật numbering mục 4.9→4.11, 5.10→5.12. Đồng bộ với features/request-create.md |

## 1. Mục đích

Tài liệu này định nghĩa danh mục giá trị chuẩn cho cột `current_step_code` (bảng `access_request`) và `step_code` (bảng `workflow_history`). Đây là yếu tố cốt lõi để WorkflowService xác định chính xác bước hiện tại và bước tiếp theo, đặc biệt khi cùng một trạng thái/vai trò xuất hiện nhiều lần trong một luồng.

## 2. Quy ước đặt tên

### Format

| Thành phần | Ý nghĩa | Giá trị |
|---|---|---|
| MÃ_MẪU | Loại phiếu | `01`, `02`, `03`, `04A`, `05A`, `05B` |
| BIẾN_THỂ | Xác định luồng phê duyệt | `I` = Internal (thuộc đơn vị chủ quản ứng dụng), `E` = External (không thuộc). Bỏ qua nếu chỉ có 1 variant |
| SỐ_THỨ_TỰ | Bước tuần tự | `01`, `02`, `03`... (zero-padded 2 chữ số) |

### Ví dụ

- `01_I_01` → Mẫu 01-YCTC, Internal, bước 1 (TP đơn vị chủ quản duyệt)
- `04A_E_03` → Mẫu 04A-YCTK, External, bước 3 (TP đơn vị chủ quản ứng dụng ký)
- `03_04` → Mẫu 03-YCCT, bước 4 (TP phụ trách DBA ký)

### Trạng thái đặc biệt (không phải step_code)

Các giá trị sau nằm ở cột `status`, KHÔNG phải `current_step_code`:

| Status | Mô tả |
|---|---|
| `DRAFT` | Phiếu nháp, chưa gửi |
| `RETURNED` | Bị chuyển trả, chờ requester sửa lại |
| `CANCELLED` | Đã hủy |
| `COMPLETED` | Hoàn thành |

Khi `status = COMPLETED/RETURNED/CANCELLED`, cột `current_step_code` giữ nguyên giá trị bước cuối cùng trước khi chuyển sang trạng thái đặc biệt (phục vụ tra cứu lịch sử).

## 3. Quy tắc xác định Variant

### 3.1. Thời điểm xác định

Variant được xác định **một lần duy nhất** tại thời điểm người lập ấn "Gửi phê duyệt" (SUBMIT) và không thay đổi trong suốt vòng đời của phiếu.

### 3.2. Logic xác định


// Xác định đơn vị chủ quản ứng dụng
owner_unit_id = NULL
if request.system_id IS NOT NULL:
    owner_unit_id = informationSystem.findById(request.system_id).owner_unit_id
else if request has detail lines:
    // 01-YCTC: lấy từ system đầu tiên trên chi tiết
    owner_unit_id = firstDetail.system.owner_unit_id

// So sánh
if request.requester_unit_id == owner_unit_id:
    return 'I'  // Internal
else:
    return 'E'  // External


### 3.3. Xác định step_code ban đầu khi SUBMIT


if variant IS NULL:
    return "{request_type}_01"
else:
    return "{request_type}_{variant}_01"


### 3.4. Quy tắc gộp (Merge Rule)

> **Quy tắc:** Khi đơn vị yêu cầu trùng với đơn vị chủ quản ứng dụng → sử dụng variant Internal (bỏ qua các bước tại đơn vị yêu cầu, chỉ thực hiện bước tại đơn vị chủ quản).

Trường hợp đặc biệt cho 04A-YCTK:
- Nếu `requester_unit = owner_app_unit` → variant = `I` (bỏ 2 bước requester)
- Nếu `owner_app_unit = owner_db_unit` → NTQ ở bước app_owner và db_owner là **cùng một người** → vẫn giữ nguyên step nhưng WorkflowService kiểm tra nếu `next_actor_id == current_actor_id` thì **tự động skip** sang bước tiếp theo (không yêu cầu ký 2 lần cùng 1 người).

## 4. Danh mục Step Code

### 4.1. Luồng 01-YCTC Internal

> Người yêu cầu thuộc đơn vị chủ quản ứng dụng.

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `01_I_01` | 1 | DEPT_MANAGER | App Owner | APPROVE | Trưởng phòng/tương đương kiểm tra, ký xác nhận |
| `01_I_02` | 2 | AUTHORITY | App Owner | APPROVE | Người có thẩm quyền kiểm tra, ký xác nhận → "Đã phê duyệt" |
| `01_I_03` | 3 | ACCESS_TEAM | App Owner | EXECUTE | Bộ phận Mở truy cập kiểm tra, ghi thời gian, ký xác nhận cấp quyền |

### 4.2. Luồng 01-YCTC External

> Người yêu cầu KHÔNG thuộc đơn vị chủ quản ứng dụng.

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `01_E_01` | 1 | DEPT_MANAGER | Requester Unit | APPROVE | TP đơn vị yêu cầu kiểm tra, ký xác nhận |
| `01_E_02` | 2 | AUTHORITY | Requester Unit | APPROVE | NTQ đơn vị yêu cầu kiểm tra, ký xác nhận |
| `01_E_03` | 3 | DEPT_MANAGER | App Owner | APPROVE | TP đơn vị chủ quản ứng dụng kiểm tra, ký xác nhận |
| `01_E_04` | 4 | AUTHORITY | App Owner | APPROVE | NTQ đơn vị chủ quản kiểm tra, ký xác nhận |
| `01_E_05` | 5 | ACCESS_TEAM | App Owner | EXECUTE | Bộ phận Mở truy cập kiểm tra, ghi thời gian, ký xác nhận cấp quyền |

### 4.3. Luồng 02-YCCS Internal

> Người yêu cầu thuộc đơn vị chủ quản ứng dụng.

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `02_I_01` | 1 | CHECKER | App Owner | APPROVE | Bộ phận Kiểm tra/thực hiện kiểm tra nội dung, ghi thời gian, ký xác nhận |
| `02_I_02` | 2 | DEPT_MANAGER | App Owner | APPROVE | Trưởng phòng/tương đương kiểm tra, ký xác nhận |
| `02_I_03` | 3 | AUTHORITY | App Owner | APPROVE | Người có thẩm quyền kiểm tra, ký xác nhận → "Đã phê duyệt" |
| `02_I_04` | 4 | EXECUTOR | App Owner | EXECUTE | Người thực hiện chạy script, ghi thời gian, ký xác nhận đã thực hiện |

### 4.4. Luồng 02-YCCS External

> Người yêu cầu KHÔNG thuộc đơn vị chủ quản ứng dụng.

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `02_E_01` | 1 | DEPT_MANAGER | Requester Unit | APPROVE | TP đơn vị yêu cầu kiểm tra, ký xác nhận |
| `02_E_02` | 2 | AUTHORITY | Requester Unit | APPROVE | NTQ đơn vị yêu cầu kiểm tra, ký xác nhận |
| `02_E_03` | 3 | CHECKER | App Owner | APPROVE | Bộ phận Kiểm tra đơn vị chủ quản kiểm tra nội dung, ghi thời gian, ký |
| `02_E_04` | 4 | DEPT_MANAGER | App Owner | APPROVE | TP đơn vị chủ quản ứng dụng kiểm tra, ký xác nhận |
| `02_E_05` | 5 | AUTHORITY | App Owner | APPROVE | NTQ kiểm tra, ký xác nhận → "Đã phê duyệt" |
| `02_E_06` | 6 | EXECUTOR | App Owner | EXECUTE | Người thực hiện chạy script, ghi thời gian, ký xác nhận đã thực hiện |

### 4.5. Luồng 03-YCCT

> Chỉ áp dụng cho đơn vị chủ quản ứng dụng (không có variant External).  
> Luồng này liên quan đến 2 đơn vị: đơn vị chủ quản ứng dụng (App Owner) và đơn vị chủ quản CSDL (DB Owner).

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `03_01` | 1 | DEPT_MANAGER | App Owner | APPROVE | Trưởng phòng/tương đương kiểm tra, ký xác nhận |
| `03_02` | 2 | AUTHORITY | App Owner | APPROVE | Người có thẩm quyền kiểm tra, ký xác nhận, chuyển DBA |
| `03_03` | 3 | DBA | DB Owner | APPROVE | DBA ký xác nhận (review kỹ thuật) |
| `03_04` | 4 | DEPT_MANAGER | DB Owner | APPROVE | TP phụ trách DBA ký xác nhận |
| `03_05` | 5 | AUTHORITY | DB Owner | APPROVE | NTQ đơn vị chủ quản CSDL ký số (phê duyệt cuối) |
| `03_06` | 6 | DBA | DB Owner | EXECUTE | DBA thực hiện, ghi thời gian, tên người thực hiện, ký |

### 4.6. Luồng 04A-YCTK Internal

> Người yêu cầu thuộc đơn vị chủ quản ứng dụng.  
> Luồng liên quan đến 2 đơn vị: đơn vị chủ quản ứng dụng (App Owner) và đơn vị chủ quản CSDL (DB Owner).

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `04A_I_01` | 1 | DEPT_MANAGER | App Owner | APPROVE | Trưởng phòng/tương đương kiểm tra, ký xác nhận |
| `04A_I_02` | 2 | AUTHORITY | App Owner | APPROVE | NTQ kiểm tra, ký xác nhận, chuyển bộ phận quản trị CSDL |
| `04A_I_03` | 3 | DEPT_MANAGER | DB Owner | APPROVE | Lãnh đạo phòng đơn vị chủ quản CSDL ký xác nhận |
| `04A_I_04` | 4 | AUTHORITY | DB Owner | APPROVE | NTQ ký xác nhận, chuyển DBA thực hiện |
| `04A_I_05` | 5 | DBA | DB Owner | EXECUTE | DBA thực hiện, ghi thời gian, tên người thực hiện, ký |

### 4.7. Luồng 04A-YCTK External

> Người yêu cầu KHÔNG thuộc đơn vị chủ quản ứng dụng.  
> Luồng liên quan đến 3 đơn vị: đơn vị yêu cầu (Requester), đơn vị chủ quản ứng dụng (App Owner), đơn vị chủ quản CSDL (DB Owner).

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `04A_E_01` | 1 | DEPT_MANAGER | Requester Unit | APPROVE | TP đơn vị yêu cầu kiểm tra, ký xác nhận |
| `04A_E_02` | 2 | AUTHORITY | Requester Unit | APPROVE | NTQ đơn vị yêu cầu kiểm tra, ký xác nhận |
| `04A_E_03` | 3 | DEPT_MANAGER | App Owner | APPROVE | TP đơn vị chủ quản ứng dụng ký xác nhận |
| `04A_E_04` | 4 | AUTHORITY | App Owner | APPROVE | NTQ ký xác nhận, chuyển đơn vị chủ quản CSDL |
| `04A_E_05` | 5 | DEPT_MANAGER | DB Owner | APPROVE | Lãnh đạo phòng đơn vị chủ quản CSDL ký xác nhận |
| `04A_E_06` | 6 | AUTHORITY | DB Owner | APPROVE | NTQ ký xác nhận, chuyển DBA thực hiện |
| `04A_E_07` | 7 | DBA | DB Owner | EXECUTE | DBA thực hiện, ghi thời gian, tên người thực hiện, ký |

> **Lưu ý:** Nếu Người có thẩm quyền (NTQ) phụ trách chung đơn vị chủ quản ứng dụng và đơn vị chủ quản CSDL (cùng 1 người), thì tại bước `04A_E_06` hệ thống phát hiện `next_actor_id == previous_signer_id` → tự động chuyển sang bước `04A_E_07` mà không yêu cầu ký lại.

### 4.8. Luồng 04B-BGTK

> Biên bản bàn giao tài khoản — DBA lập sau khi 04A-YCTK hoàn thành.  
> Luồng có 2 giai đoạn phê duyệt: (1) Lãnh đạo DBA duyệt → Chờ ký nhận → (2) Lãnh đạo phòng người dùng duyệt.

**Giai đoạn 1:** DBA gửi → Lãnh đạo phòng DBA duyệt → chuyển trạng thái "Chờ ký nhận".

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `04B_01` | 1 | DEPT_MANAGER | DB Owner | APPROVE | Lãnh đạo phòng quản trị CSDL kiểm tra, ký xác nhận biên bản bàn giao |

**Giai đoạn "Chờ ký nhận":** Sau bước `04B_01` duyệt → status = `PENDING_RECEIPT`. Người dùng ký nhận dòng của mình (row-level). Khi tất cả ký xong → tự động chuyển giai đoạn 2.

**Giai đoạn 2:** Lãnh đạo phòng người dùng (đơn vị yêu cầu ban đầu từ 04A) duyệt hoàn tất.

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `04B_02` | 2 | DEPT_MANAGER | Requester Unit | APPROVE | Lãnh đạo phòng đơn vị yêu cầu (từ 04A) ký xác nhận hoàn tất bàn giao |

> **Lưu ý:** Luồng 04B không có variant (I/E) vì DBA luôn thuộc đơn vị chủ quản CSDL. Transition: `04B_01` → PENDING_RECEIPT → (tất cả ký nhận) → `04B_02` → COMPLETED.

### 4.9. Luồng 05A-YCKC

> Yêu cầu khẩn cấp — chỉ có 1 bước thực hiện sau khi requester gửi.

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `05A_01` | 1 | ACCESS_TEAM | App Owner | EXECUTE | Bộ phận Mở truy cập thực hiện, ghi thời gian, họ tên, ký xác nhận |

### 4.10. Luồng 05B-HTKC Internal

> Người yêu cầu thuộc đơn vị chủ quản ứng dụng.

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `05B_I_01` | 1 | CHECKER | App Owner | APPROVE | Người kiểm tra kiểm tra nội dung, ký xác nhận |
| `05B_I_02` | 2 | DEPT_MANAGER | App Owner | APPROVE | TP đơn vị chủ quản ứng dụng ký xác nhận |
| `05B_I_03` | 3 | AUTHORITY | App Owner | APPROVE | NTQ ký xác nhận |
| `05B_I_04` | 4 | ACCESS_TEAM | App Owner | EXECUTE | Bộ phận Mở truy cập ký xác nhận, ghi họ tên, thời gian, hoàn thành |

### 4.11. Luồng 05B-HTKC External

> Người yêu cầu KHÔNG thuộc đơn vị chủ quản ứng dụng.

| Step Code | Bước | Vai trò xử lý | Đơn vị | Hành động | Mô tả |
|---|---|---|---|---|---|
| `05B_E_01` | 1 | DEPT_MANAGER | Requester Unit | APPROVE | TP đơn vị yêu cầu kiểm tra, ký xác nhận |
| `05B_E_02` | 2 | AUTHORITY | Requester Unit | APPROVE | NTQ đơn vị yêu cầu kiểm tra, ký xác nhận |
| `05B_E_03` | 3 | CHECKER | App Owner | APPROVE | Người kiểm tra đơn vị chủ quản kiểm tra nội dung, ký xác nhận |
| `05B_E_04` | 4 | DEPT_MANAGER | App Owner | APPROVE | TP đơn vị chủ quản ứng dụng ký xác nhận |
| `05B_E_05` | 5 | AUTHORITY | App Owner | APPROVE | NTQ ký xác nhận |
| `05B_E_06` | 6 | ACCESS_TEAM | App Owner | EXECUTE | Bộ phận Mở truy cập ký xác nhận, ghi họ tên, thời gian, hoàn thành |

## 5. Bảng chuyển bước (Transition Logic)

### 5.1. Quy tắc chung

- **APPROVE:** Chuyển sang step_code tiếp theo trong cùng luồng. Step cuối cùng (EXECUTE) → `status = COMPLETED`.
- **RETURN:** Tại **bất kỳ bước nào** (trừ bước EXECUTE cuối cùng), actor có thể chuyển trả → `status = RETURNED`, `current_step_code` giữ nguyên giá trị bước chuyển trả. Phiếu quay về requester.
- **Bước EXECUTE:** Không cho phép RETURN. Chỉ có EXECUTE (hoàn thành).

### 5.2. Luồng 01-YCTC Internal

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `01_I_01` | `01_I_02` | Requester (RETURNED) |
| `01_I_02` | `01_I_03` | Requester (RETURNED) |
| `01_I_03` | COMPLETED | Requester (RETURNED) |

### 5.3. Luồng 01-YCTC External

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `01_E_01` | `01_E_02` | Requester (RETURNED) |
| `01_E_02` | `01_E_03` | Requester (RETURNED) |
| `01_E_03` | `01_E_04` | Requester (RETURNED) |
| `01_E_04` | `01_E_05` | Requester (RETURNED) |
| `01_E_05` | COMPLETED | Requester (RETURNED) |

### 5.4. Luồng 02-YCCS Internal

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `02_I_01` | `02_I_02` | Requester (RETURNED) |
| `02_I_02` | `02_I_03` | Requester (RETURNED) |
| `02_I_03` | `02_I_04` | Requester (RETURNED) |
| `02_I_04` | COMPLETED | N/A (không cho RETURN) |

### 5.5. Luồng 02-YCCS External

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `02_E_01` | `02_E_02` | Requester (RETURNED) |
| `02_E_02` | `02_E_03` | Requester (RETURNED) |
| `02_E_03` | `02_E_04` | Requester (RETURNED) |
| `02_E_04` | `02_E_05` | Requester (RETURNED) |
| `02_E_05` | `02_E_06` | Requester (RETURNED) |
| `02_E_06` | COMPLETED | N/A (không cho RETURN) |

### 5.6. Luồng 03-YCCT

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `03_01` | `03_02` | Requester (RETURNED) |
| `03_02` | `03_03` | Requester (RETURNED) |
| `03_03` | `03_04` | Requester (RETURNED) |
| `03_04` | `03_05` | Requester (RETURNED) |
| `03_05` | `03_06` | Requester (RETURNED) |
| `03_06` | COMPLETED | N/A (không cho RETURN) |

### 5.7. Luồng 04A-YCTK Internal

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `04A_I_01` | `04A_I_02` | Requester (RETURNED) |
| `04A_I_02` | `04A_I_03` | Requester (RETURNED) |
| `04A_I_03` | `04A_I_04` | Requester (RETURNED) |
| `04A_I_04` | `04A_I_05` | Requester (RETURNED) |
| `04A_I_05` | COMPLETED | N/A (không cho RETURN) |

### 5.8. Luồng 04A-YCTK External

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `04A_E_01` | `04A_E_02` | Requester (RETURNED) |
| `04A_E_02` | `04A_E_03` | Requester (RETURNED) |
| `04A_E_03` | `04A_E_04` | Requester (RETURNED) |
| `04A_E_04` | `04A_E_05` | Requester (RETURNED) |
| `04A_E_05` | `04A_E_06` | Requester (RETURNED) |
| `04A_E_06` | `04A_E_07` | Requester (RETURNED) |
| `04A_E_07` | COMPLETED | N/A (không cho RETURN) |

### 5.9. Luồng 04B-BGTK

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `04B_01` | PENDING_RECEIPT (chờ ký nhận) | Requester (RETURNED) |
| `04B_02` | COMPLETED | Requester (RETURNED) |

> **Lưu ý:** Giữa `04B_01` và `04B_02` có giai đoạn "Chờ ký nhận" (row-level signing). Khi tất cả người dùng ký nhận xong, hệ thống tự động chuyển sang bước `04B_02`.

### 5.10. Luồng 05A-YCKC

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `05A_01` | COMPLETED | Requester (RETURNED) |

### 5.11. Luồng 05B-HTKC Internal

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `05B_I_01` | `05B_I_02` | Requester (RETURNED) |
| `05B_I_02` | `05B_I_03` | Requester (RETURNED) |
| `05B_I_03` | `05B_I_04` | Requester (RETURNED) |
| `05B_I_04` | COMPLETED | N/A (không cho RETURN) |

### 5.12. Luồng 05B-HTKC External

| Bước hiện tại | APPROVE → Next | RETURN → Target |
|---|---|---|
| `05B_E_01` | `05B_E_02` | Requester (RETURNED) |
| `05B_E_02` | `05B_E_03` | Requester (RETURNED) |
| `05B_E_03` | `05B_E_04` | Requester (RETURNED) |
| `05B_E_04` | `05B_E_05` | Requester (RETURNED) |
| `05B_E_05` | `05B_E_06` | Requester (RETURNED) |
| `05B_E_06` | COMPLETED | N/A (không cho RETURN) |

## 6. Logic xác định Actor tiếp theo

### 6.1. Quy tắc tra cứu Actor

Khi chuyển sang step tiếp theo, WorkflowService cần xác định `current_actor_*` mới:


// Xác định đơn vị chịu trách nhiệm tại bước tiếp theo
targetUnit = resolveUnit(request, stepDef.unit_type)

// Tra cứu user/role tại đơn vị đó
switch stepDef.actor_role:
    case DEPT_MANAGER:
        actor = userRoleRepository.findByRoleAndUnit('DEPT_MANAGER', targetUnit.id)
        return ActorInfo(type=ROLE, id=actor.id, role='DEPT_MANAGER', unit=targetUnit.id)
    
    case AUTHORITY:
        actor = userRoleRepository.findByRoleAndUnit('AUTHORITY', targetUnit.id)
        return ActorInfo(type=ROLE, id=actor.id, role='AUTHORITY', unit=targetUnit.id)
    
    case CHECKER:
        actor = userRoleRepository.findByRoleAndUnitAndSystem('CHECKER', targetUnit.id, request.system_id)
        return ActorInfo(type=ROLE, id=actor.id, role='CHECKER', unit=targetUnit.id)
    
    case ACCESS_TEAM:
        return ActorInfo(type=TEAM, id=null, role='ACCESS_TEAM', unit=targetUnit.id)
    
    case DBA:
        actor = userRoleRepository.findByRoleAndDatabase('DBA', request.database_id)
        return ActorInfo(type=ROLE, id=actor.id, role='DBA', unit=targetUnit.id)
    
    case EXECUTOR:
        actor = userRoleRepository.findByRoleAndUnitAndSystem('EXECUTOR', targetUnit.id, request.system_id)
        return ActorInfo(type=ROLE, id=actor.id, role='EXECUTOR', unit=targetUnit.id)


### 6.2. Xử lý trường hợp NTQ phụ trách chung (04A)


    if previousAuthSigner.signer_user_id == resolvedActor.id:
        // Cùng 1 người → tự động skip sang bước tiếp theo
        return autoAdvance(request, '04A_E_07')

return resolvedActor


## 7. Cập nhật cột `at_requester_phase`

Cột `at_requester_phase` trên `access_request` được cập nhật tự động:

| Điều kiện | Giá trị |
|---|---|
| Step code chứa đơn vị = "Requester Unit" | `true` |
| Step code chứa đơn vị = "App Owner" hoặc "DB Owner" | `false` |
| Variant = Internal (I) | Luôn `false` (vì requester = app owner) |

Bảng mapping cụ thể:

| Step Codes | at_requester_phase |
|---|---|
| `01_E_01`, `01_E_02` | true |
| `01_E_03`, `01_E_04`, `01_E_05` | false |
| `02_E_01`, `02_E_02` | true |
| `02_E_03` → `02_E_06` | false |
| `04A_E_01`, `04A_E_02` | true |
| `04A_E_03` → `04A_E_07` | false |
| `05B_E_01`, `05B_E_02` | true |
| `05B_E_03` → `05B_E_06` | false |
| Tất cả variant Internal (`*_I_*`) | false |
| `03_*`, `05A_*` | false |
| `04B_*` | false |

## 8. Cập nhật trường `approved_at`

Trường `approved_at` được set **một lần duy nhất** tại bước **AUTHORITY cuối cùng trước khi chuyển sang EXECUTE/ACCESS_TEAM**:

| Luồng | Step code trigger `approved_at` |
|---|---|
| 01_I | `01_I_02` (NTQ ký → chuyển ACCESS_TEAM) |
| 01_E | `01_E_04` (NTQ chủ quản ký → chuyển ACCESS_TEAM) |
| 02_I | `02_I_03` (NTQ ký → chuyển EXECUTOR) |
| 02_E | `02_E_05` (NTQ ký → chuyển EXECUTOR) |
| 03 | `03_05` (NTQ DB Owner ký → chuyển DBA thực hiện) |
| 04A_I | `04A_I_04` (NTQ DB Owner ký → chuyển DBA) |
| 04A_E | `04A_E_06` (NTQ DB Owner ký → chuyển DBA) |
| 05A | Không có (không qua AUTHORITY) |
| 04B | Không có (không qua AUTHORITY — chỉ DEPT_MANAGER duyệt) |
| 05B_I | `05B_I_03` (NTQ ký → chuyển ACCESS_TEAM) |
| 05B_E | `05B_E_05` (NTQ ký → chuyển ACCESS_TEAM) |

## 9. Tích hợp với các module khác

### 9.1. Module Request — Khi SUBMIT

Trách nhiệm của module Request khi người lập gửi phiếu:

1. Xác định variant (gọi logic mục 3.2)
2. Set `current_step_code` = step code ban đầu (gọi logic mục 3.3)
3. Set `at_requester_phase` theo bảng mục 7
4. Set `owner_unit_id` = đơn vị chủ quản ứng dụng (từ `information_system`)
5. Set `owner_db_unit_id` = đơn vị chủ quản CSDL (từ `database_catalog`)
6. Gọi `resolveNextActor()` cho step đầu tiên để set `current_actor_*`
7. Set `status` = trạng thái phù hợp (PENDING_APPROVAL / PENDING_CHECK)
8. Ghi `workflow_history` với action = SUBMIT

### 9.2. Module Approval — Khi APPROVE/RETURN/EXECUTE

Trách nhiệm của module Approval:

1. Validate: `current_step_code` + `current_actor_*` khớp với user đang đăng nhập
2. Verify OTP (nếu APPROVE/EXECUTE)
3. Tra transition table → xác định next step code
4. Gọi `resolveNextActor()` cho next step → set `current_actor_*` mới
5. Cập nhật `current_step_code`, `at_requester_phase`, `current_unit_id`
6. Set `approved_at` nếu đúng step trigger (mục 8)
7. Ghi `workflow_history`, `request_signature`, `work_log_07`
8. Gửi email notification

### 9.3. Module Configuration — Seed Data cần đảm bảo

Để workflow hoạt động chính xác, bảng `user_role` phải có dữ liệu:

| Yêu cầu | Mô tả |
|---|---|
| Mỗi đơn vị phải có ít nhất 1 DEPT_MANAGER | Để bước TP không bị treo |
| Mỗi đơn vị phải có ít nhất 1 AUTHORITY | Để bước NTQ không bị treo |
| Mỗi hệ thống phải có ít nhất 1 CHECKER | Cho luồng 02, 05B |
| Mỗi CSDL phải có ít nhất 1 DBA | Cho luồng 03, 04A |
| Phải có ACCESS_TEAM tại đơn vị chủ quản | Cho luồng 01, 05A, 05B |
| Mỗi hệ thống phải có ít nhất 1 EXECUTOR | Cho luồng 02 |

## 10. Tổng hợp Step Code Enum (cho Java)

```java
public enum StepCode {
    // 01-YCTC Internal
    STEP_01_I_01("01_I_01"), STEP_01_I_02("01_I_02"), STEP_01_I_03("01_I_03"),
    
    // 01-YCTC External
    STEP_01_E_01("01_E_01"), STEP_01_E_02("01_E_02"), STEP_01_E_03("01_E_03"),
    STEP_01_E_04("01_E_04"), STEP_01_E_05("01_E_05"),
    
    // 02-YCCS Internal
    STEP_02_I_01("02_I_01"), STEP_02_I_02("02_I_02"), STEP_02_I_03("02_I_03"),
    STEP_02_I_04("02_I_04"),
    
    // 02-YCCS External
    STEP_02_E_01("02_E_01"), STEP_02_E_02("02_E_02"), STEP_02_E_03("02_E_03"),
    STEP_02_E_04("02_E_04"), STEP_02_E_05("02_E_05"), STEP_02_E_06("02_E_06"),
    
    // 03-YCCT
    STEP_03_01("03_01"), STEP_03_02("03_02"), STEP_03_03("03_03"),
    STEP_03_04("03_04"), STEP_03_05("03_05"), STEP_03_06("03_06"),
    
    // 04A-YCTK Internal
    STEP_04A_I_01("04A_I_01"), STEP_04A_I_02("04A_I_02"), STEP_04A_I_03("04A_I_03"),
    STEP_04A_I_04("04A_I_04"), STEP_04A_I_05("04A_I_05"),
    
    // 04A-YCTK External
    STEP_04A_E_01("04A_E_01"), STEP_04A_E_02("04A_E_02"), STEP_04A_E_03("04A_E_03"),
    STEP_04A_E_04("04A_E_04"), STEP_04A_E_05("04A_E_05"), STEP_04A_E_06("04A_E_06"),
    STEP_04A_E_07("04A_E_07"),
    
    // 04B-BGTK
    STEP_04B_01("04B_01"), STEP_04B_02("04B_02"),
    
    // 05A-YCKC
    STEP_05A_01("05A_01"),
    
    // 05B-HTKC Internal
    STEP_05B_I_01("05B_I_01"), STEP_05B_I_02("05B_I_02"), STEP_05B_I_03("05B_I_03"),
    STEP_05B_I_04("05B_I_04"),
    
    // 05B-HTKC External
    STEP_05B_E_01("05B_E_01"), STEP_05B_E_02("05B_E_02"), STEP_05B_E_03("05B_E_03"),
    STEP_05B_E_04("05B_E_04"), STEP_05B_E_05("05B_E_05"), STEP_05B_E_06("05B_E_06");
    
    private final String code;
    
    StepCode(String code) { this.code = code; }
    public String getCode() { return code; }
}

