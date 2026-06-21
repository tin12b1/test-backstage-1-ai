# Feature: Phê duyệt và xác nhận xử lý

**Người phụ trách:** Quang  
**Mã hạng mục:** 2.4

## 1. Mục tiêu

Cung cấp chức năng hiển thị danh sách yêu cầu chờ xử lý theo vai trò, xem chi tiết, kiểm tra, ký xác nhận, phê duyệt, chuyển trả, tiếp nhận và xác nhận thực hiện bởi Bộ phận kiểm tra/Mở truy cập/Quản trị CSDL/Người thực hiện.

## 2. Quy tắc chung

- Danh sách chờ xử lý phải theo đúng vai trò được phân quyền:
  - Bộ phận kiểm tra/Trưởng phòng hoặc tương đương: hiển thị các yêu cầu đang chờ kiểm tra và phê duyệt cấp phòng.
  - Người có thẩm quyền: hiển thị các yêu cầu đã được Trưởng phòng/tương đương ký xác nhận và chuyển tiếp, đang chờ phê duyệt.
  - Bộ phận Mở truy cập: hiển thị các yêu cầu đã được phê duyệt, đang chờ tiếp nhận và xác nhận thực hiện.
- Người xử lý được xem đầy đủ thông tin chi tiết trước khi xử lý.
- Yêu cầu hợp lệ: ký xác nhận và chuyển bước tiếp theo.
- Yêu cầu không hợp lệ: chuyển trả người lập và bắt buộc nhập lý do.
- Chuyển trả không cần ký xác nhận.
- Chỉ chuyển bước tiếp theo khi ký xác nhận thành công.
- Hệ thống tự động xác định người/bộ phận tiếp theo dựa trên loại yêu cầu, đơn vị lập, đơn vị chủ quản ứng dụng, CSDL và vai trò.
- Hệ thống tự động ghi nhật ký 07-NKCV và gửi email khi có sự kiện.

## 3. Luồng 01-YCTC - người yêu cầu thuộc đơn vị chủ quản ứng dụng

1. Trưởng phòng/tương đương kiểm tra yêu cầu.
2. Nếu hợp lệ, Trưởng phòng ký xác nhận và chuyển Người có thẩm quyền.
3. Người có thẩm quyền kiểm tra.
4. Nếu không hợp lệ, chuyển trả người lập.
5. Nếu hợp lệ, ký xác nhận, chuyển bộ phận Mở truy cập, trạng thái “Đã phê duyệt”.
6. Bộ phận Mở truy cập kiểm tra.
7. Nếu hợp lệ, ghi nhận thời gian và ký xác nhận cấp quyền.
8. Nếu không hợp lệ, ghi rõ nội dung và chuyển trả người lập.
9. Hệ thống Mở truy cập tự động quét yêu cầu đã xác nhận để cấp quyền.

## 4. Luồng 01-YCTC - người yêu cầu không thuộc đơn vị chủ quản ứng dụng

1. Trưởng phòng/tương đương đơn vị yêu cầu kiểm tra, ký xác nhận và gửi Người có thẩm quyền đơn vị yêu cầu.
2. Người có thẩm quyền đơn vị yêu cầu kiểm tra.
3. Nếu không hợp lệ, chuyển trả người lập.
4. Nếu hợp lệ, ký xác nhận và chuyển đơn vị chủ quản ứng dụng.
5. Trưởng phòng/tương đương đơn vị chủ quản ứng dụng kiểm tra.
6. Nếu không hợp lệ, chuyển trả đơn vị lập yêu cầu.
7. Nếu hợp lệ, ký xác nhận và chuyển Người có thẩm quyền đơn vị chủ quản.
8. Người có thẩm quyền đơn vị chủ quản kiểm tra.
9. Nếu hợp lệ, ký xác nhận và chuyển bộ phận Mở truy cập.
10. Bộ phận Mở truy cập kiểm tra, ghi nhận thời gian, ký xác nhận hoặc chuyển trả.
11. Hệ thống Mở truy cập tự động quét yêu cầu đã xác nhận để cấp quyền.

## 5. Luồng 02-YCCS - người yêu cầu thuộc đơn vị chủ quản ứng dụng

1. Bộ phận Kiểm tra/thực hiện kiểm tra nội dung.
2. Nếu hợp lệ, ghi nhận thời gian, ký xác nhận, chuyển Trưởng phòng/tương đương.
3. Nếu không hợp lệ, ghi rõ nội dung và chuyển trả người lập.
4. Trưởng phòng/tương đương kiểm tra, ký xác nhận và gửi Người có thẩm quyền.
5. Người có thẩm quyền kiểm tra, ký xác nhận, chuyển Người thực hiện; trạng thái “Đã phê duyệt”.
6. Người thực hiện chạy script, ghi thời gian chạy, ký xác nhận đã thực hiện.

## 6. Luồng 02-YCCS - người yêu cầu không thuộc đơn vị chủ quản ứng dụng

1. Trưởng phòng/tương đương đơn vị yêu cầu kiểm tra, ký xác nhận và gửi Người có thẩm quyền đơn vị yêu cầu.
2. Người có thẩm quyền đơn vị yêu cầu kiểm tra, ký xác nhận, chuyển Bộ phận kiểm tra đơn vị chủ quản ứng dụng.
3. Bộ phận Kiểm tra/thực hiện kiểm tra nội dung.
4. Nếu hợp lệ, ghi nhận thời gian, ký xác nhận, chuyển Trưởng phòng/tương đương đơn vị chủ quản ứng dụng.
5. Nếu không hợp lệ, ghi rõ nội dung và chuyển trả người lập.
6. Trưởng phòng/tương đương đơn vị chủ quản ứng dụng kiểm tra, ký xác nhận và gửi Người có thẩm quyền.
7. Người có thẩm quyền kiểm tra, ký xác nhận, chuyển Người thực hiện; trạng thái “Đã phê duyệt”.
8. Người thực hiện chạy script, ghi thời gian chạy, ký xác nhận đã thực hiện.

> Ghi chú: Tài liệu có đánh số trùng “Bước 3” ở luồng này. Khi triển khai code cần theo trình tự nghiệp vụ ở trên.

## 7. Luồng 03-YCCT

Áp dụng cho yêu cầu thêm mới/thay đổi cấu trúc, chỉ dành cho đơn vị chủ quản ứng dụng.

1. Trưởng phòng/tương đương kiểm tra, nếu hợp lệ chuyển bước 2.
2. Ký xác nhận và gửi Người có thẩm quyền.
3. Người có thẩm quyền kiểm tra, chuyển trả nếu không hợp lệ hoặc chuyển bước 4 nếu hợp lệ.
4. Ký xác nhận, chuyển DBA.
5. DBA ký xác nhận và chuyển Trưởng phòng/tương đương phụ trách DBA.
6. Trưởng phòng/tương đương phụ trách DBA ký xác nhận chuyển Người có thẩm quyền.
7. Người có thẩm quyền ký số, chuyển DBA thực hiện.
8. DBA ghi thời gian thực hiện, tên người ký sau khi thực hiện.

## 8. Luồng 04A-YCTK - người yêu cầu thuộc đơn vị chủ quản ứng dụng

1. Trưởng phòng/tương đương kiểm tra, ký xác nhận và gửi Người có thẩm quyền.
2. Người có thẩm quyền kiểm tra, chuyển trả nếu không hợp lệ hoặc ký xác nhận nếu hợp lệ.
3. Chuyển bộ phận quản trị CSDL/DBA.
4. Lãnh đạo phòng đơn vị chủ quản CSDL ký xác nhận và chuyển Người có thẩm quyền.
5. Người có thẩm quyền ký xác nhận, chuyển DBA thực hiện.
6. DBA ghi thời gian thực hiện, tên người ký sau khi thực hiện.

## 9. Luồng 04A-YCTK - người yêu cầu không thuộc đơn vị chủ quản ứng dụng

1. Trưởng phòng/tương đương đơn vị yêu cầu kiểm tra, ký xác nhận và gửi Người có thẩm quyền đơn vị yêu cầu.
2. Người có thẩm quyền đơn vị yêu cầu kiểm tra, ký xác nhận và chuyển đơn vị chủ quản ứng dụng.
3. Trưởng phòng/tương đương đơn vị chủ quản ứng dụng ký xác nhận và gửi Người có thẩm quyền.
4. Người có thẩm quyền ký xác nhận, gửi đơn vị chủ quản CSDL.
5. Lãnh đạo phòng đơn vị chủ quản CSDL ký xác nhận và chuyển Người có thẩm quyền. Nếu Người có thẩm quyền phụ trách chung các đơn vị thì chỉ cần ký một nơi.
6. Người có thẩm quyền ký xác nhận, chuyển DBA thực hiện.
7. DBA ghi thời gian thực hiện, tên người ký sau khi thực hiện.

## 10. Luồng 05A-YCKC

- Người yêu cầu ký xác nhận và chuyển tới Bộ phận mở.
- Bộ phận mở thực hiện, ghi thời gian, họ tên và ký xác nhận.

## 11. Luồng 05B-HTKC - người yêu cầu không thuộc đơn vị chủ quản ứng dụng

1. Trưởng phòng/tương đương đơn vị yêu cầu kiểm tra, ký xác nhận và gửi Người có thẩm quyền.
2. Người có thẩm quyền đơn vị yêu cầu kiểm tra, ký xác nhận và chuyển Bộ phận kiểm tra của đơn vị chủ quản ứng dụng.
3. Người kiểm tra kiểm tra nội dung, ký xác nhận, gửi Trưởng phòng/tương đương.
4. Trưởng phòng/tương đương đơn vị chủ quản ứng dụng ký xác nhận gửi Người có thẩm quyền.
5. Người có thẩm quyền ký xác nhận gửi Bộ phận Mở truy cập.
6. Bộ phận Mở truy cập ký xác nhận, ghi họ tên, thời gian thực hiện để hoàn thành phiếu.

## 12. Luồng 05B-HTKC - người yêu cầu thuộc đơn vị chủ quản ứng dụng

1. Người kiểm tra kiểm tra nội dung, ký xác nhận, gửi Trưởng phòng/tương đương.
2. Trưởng phòng/tương đương đơn vị chủ quản ứng dụng ký xác nhận gửi Người có thẩm quyền.
3. Người có thẩm quyền ký xác nhận gửi Bộ phận Mở truy cập.
4. Bộ phận Mở truy cập ký xác nhận, ghi họ tên, thời gian thực hiện để hoàn thành phiếu.

## 13. Giao diện xử lý

### Thông tin yêu cầu

| Trường | Loại | Mô tả |
|---|---|---|
| Mã yêu cầu | Hệ thống | Hiển thị mã phiếu |
| Tên đơn vị yêu cầu | Hệ thống | Đơn vị lập |
| Tên phòng hoặc tương đương | Hệ thống | Phòng/bộ phận lập |
| Người yêu cầu | Hệ thống | Người lập |
| Loại yêu cầu | Hệ thống | Mẫu phiếu |
| Thời gian truy cập/truy xuất | Hệ thống | Theo phiếu |
| Chuyển trả | Nút lệnh | Bắt buộc nhập lý do |
| Xem chi tiết | Nút lệnh | Mở chi tiết phiếu |
| Ký xác nhận | Nút lệnh | Gọi OTP |
| Lý do từ chối | Nhập | Bắt buộc khi chuyển trả |
| Gửi phê duyệt | Nút lệnh | Chuyển bước tiếp theo |

### Thông tin chi tiết

- Danh sách chi tiết theo phiếu dạng Grid.

### Luồng xử lý

| Trường | Loại | Mô tả |
|---|---|---|
| Người xử lý | Hệ thống | Liệt kê toàn bộ tiến trình kiểm tra/phê duyệt |
| Thời gian xử lý | Hệ thống | Thời điểm xử lý |
| Nội dung xử lý | Hệ thống | Nội dung/lý do/kết quả |

## 14. Allowed Files

- `src/main/java/.../workflow/**`
- `src/main/java/.../approval/**`
- `src/main/java/.../execution/**`
- `src/main/resources/templates/approval/**`
- `src/main/resources/templates/work-items/**`
- `src/main/resources/static/js/approval/**`
- `src/test/java/.../workflow/**`
- `src/test/java/.../approval/**`

## 15. Must Not Change

- Không sửa form lập yêu cầu, trừ link điều hướng sang màn hình xử lý.
- Không sửa dashboard ngoài số lượng chờ xử lý nếu cần expose service.
- Không sửa cấu hình danh mục nếu không cần thiết.

## 16. Verification

- Danh sách chờ xử lý đúng theo từng vai trò.
- Xem chi tiết yêu cầu đầy đủ thông tin.
- Phê duyệt yêu cầu hợp lệ yêu cầu OTP và chuyển đúng bước.
- Chuyển trả bắt buộc nhập lý do và không yêu cầu OTP.
- Các luồng 01, 02, 03, 04A, 05A, 05B chuyển đúng người/bộ phận tiếp theo.
- Workflow history, signature và nhật ký 07-NKCV được ghi.
- Email thông báo được tạo/gửi khi phát sinh sự kiện.

## 17. Definition of Done

- Hoàn thành màn hình danh sách chờ xử lý và chi tiết xử lý.
- Hoàn thành approve/return/execute cho các vai trò.
- Có workflow service xác định bước tiếp theo.
- Có test cho từng luồng chính và chuyển trả.
