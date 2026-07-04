-- =====================================================================
-- Seed danh muc cho Oracle (tuong duong data.sql cua H2).
-- Chay SAU V1__schema.sql. Cu phap Oracle:
--   - boolean TRUE/FALSE -> 1/0 (NUMBER(1,0))
--   - nhieu dong -> dung INSERT ALL ... SELECT 1 FROM dual
--   - CURRENT_DATE -> TRUNC(SYSDATE), CURRENT_TIMESTAMP -> SYSTIMESTAMP
-- Mat khau dang nhap demo: "password" (khi AD_MODE=mock).
-- =====================================================================

-- ===== Vai tro =====
INSERT ALL
  INTO role (id, code, name, description) VALUES (1, 'REQUESTER', 'Người lập yêu cầu', 'Tạo, ký, gửi yêu cầu')
  INTO role (id, code, name, description) VALUES (2, 'DEPT_MANAGER', 'Trưởng phòng/tương đương', 'Kiểm tra, ký, chuyển tiếp')
  INTO role (id, code, name, description) VALUES (3, 'AUTHORITY', 'Người có thẩm quyền', 'Phê duyệt')
  INTO role (id, code, name, description) VALUES (4, 'CHECKER', 'Bộ phận kiểm tra', 'Kiểm tra nội dung/script')
  INTO role (id, code, name, description) VALUES (5, 'ACCESS_TEAM', 'Bộ phận mở truy cập', 'Mở truy cập, ghi thời gian')
  INTO role (id, code, name, description) VALUES (6, 'DBA', 'Quản trị CSDL/DBA', 'Xử lý cấu trúc/tài khoản')
  INTO role (id, code, name, description) VALUES (7, 'EXECUTOR', 'Người thực hiện', 'Chạy script/chỉnh sửa dữ liệu')
  INTO role (id, code, name, description) VALUES (8, 'ADMIN', 'Quản trị hệ thống', 'Cấu hình hệ thống')
SELECT 1 FROM dual;

-- ===== Don vi =====
INSERT ALL
  INTO unit (id, code, name, active) VALUES (1, 'TTCNTT', 'Trung tâm Công nghệ thông tin', 1)
  INTO unit (id, code, name, active) VALUES (2, 'NHS', 'Ngân hàng số', 1)
  INTO unit (id, code, name, active) VALUES (3, 'QLDL', 'Trung tâm quản lý dữ liệu', 1)
SELECT 1 FROM dual;

-- ===== Phong/bo phan =====
INSERT ALL
  INTO department (id, code, name, unit_id, active) VALUES (1, 'NHDT', 'Ngân hàng điện tử', 1, 1)
  INTO department (id, code, name, unit_id, active) VALUES (2, 'CORE', 'Corebanking', 1, 1)
  INTO department (id, code, name, unit_id, active) VALUES (3, 'KT', 'SmartForm', 2, 1)
  INTO department (id, code, name, unit_id, active) VALUES (4, 'KT', 'MIS', 3, 1)
SELECT 1 FROM dual;

-- ===== He thong thong tin =====
INSERT ALL
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (1,  'TTCNTT-NHDT-ARS',        'ARS-Kiều hối tập trung',              1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (2,  'TTCNTT-NHDT-CBPS',       'CBPS-Thanh toán biên mậu',            1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (3,  'TTCNTT-NHDT-GSKQ',       'AMS-Quản lý tài sản trong kho tiền',  1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (4,  'TTCNTT-NHDT-GSKQMB',     'Giám sát kho quỹ miền bắc',           1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (5,  'TTCNTT-NHDT-PaymentHub', 'Thanh toán tập trung',                1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (6,  'TTCNTT-NHDT-TTSPKB',     'Thanh toán song phương Kho bạc',      1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (7,  'TTCNTT-NHDT-TTSPBH',     'Thanh toán song phương Bảo hiểm',     1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (8,  'TTCNTT-NHDT-OSB',        'Open Smartbank',                      1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (9,  'TTCNTT-NHDT-CBPS2',      'CBPS',                                1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (10, 'TTCNTT-NHDT-DHTM',       'Điều hòa tiền mặt',                   1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (11, 'TTCNTT-NHDT-Agritax',    'Thu ngân sách Nhà nước',              1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (12, 'TTCNTT-NHDT-eInvoice',   'eInvoice-Hóa đơn điện tử',            1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (13, 'TTCNTT-NHDT-BAS',        'Quản lý kinh phí công đoàn',          1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (14, 'TTCNTT-NHDT-eBanking',   'Hệ thống eBanking',                   1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (15, 'TTCNTT-NHDT-SPRT',       'Song Phương Realtime',                1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (16, 'TTCNTT-NHDT-ACH',        'ACH',                                 1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (17, 'TTCNTT-NHDT-SMSGateway', 'SMS Gateway',                         1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (18, 'TTCNTT-NHDT-SWIFTHUB',   'SWIFTHUB',                            1, 1, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (19, 'TTCNTT-CORE-IPCAS',      'IPCAS',                               1, 2, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (20, 'NHS-KT-SMARTFORM',       'Hệ thống biểu mẫu thông minh',        2, 3, 1)
  INTO information_system (id, code, name, owner_unit_id, owner_department_id, active) VALUES (21, 'QLDL-KT-MIS',            'Báo cáo',                             3, 4, 1)
SELECT 1 FROM dual;

-- ===== CSDL =====
INSERT ALL
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (1,  1,  'ARS',        'ARS',        1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (2,  2,  'CBPS',       'CBPS',       1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (3,  3,  'GSKQ',       'GSKQ',       1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (4,  4,  'GSKQMB',     'GSKQMB',     1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (5,  5,  'PaymentHub', 'PaymentHub', 1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (6,  6,  'SPKB',       'SPKB',       1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (7,  7,  'SPBH',       'SPBH',       1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (8,  8,  'OSB',        'OSB',        1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (9,  9,  'CBPS',       'CBPS',       1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (10, 10, 'DHTM',       'DHTM',       1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (11, 11, 'Agritax',    'Agritax',    1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (12, 12, 'HDDT',       'HDDT',       1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (13, 13, 'AGRIBAS',    'AGRIBAS',    1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (14, 14, 'EBANK',      'EBANK',      1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (15, 15, 'SPRT',       'SPRT',       1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (16, 16, 'ACH',        'ACH',        1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (17, 17, 'SMSGateway', 'SMSGateway', 1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (18, 18, 'SWIFTHUB',   'SWIFTHUB',   1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (19, 19, 'IPCAS',      'IPCAS',      1, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (20, 20, 'SMARTFORM',  'SMARTFORM',  2, 1)
  INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES (21, 21, 'MIS',        'MIS',        3, 1)
SELECT 1 FROM dual;

-- ===== Danh muc quyen truy cap =====
INSERT ALL
  INTO access_right_catalog (id, code, name, active) VALUES (1, 'SELECT', 'Truy vấn dữ liệu', 1)
  INTO access_right_catalog (id, code, name, active) VALUES (2, 'INSERT', 'Thêm dữ liệu', 1)
  INTO access_right_catalog (id, code, name, active) VALUES (3, 'UPDATE', 'Cập nhật dữ liệu', 1)
  INTO access_right_catalog (id, code, name, active) VALUES (4, 'DELETE', 'Xóa dữ liệu', 1)
  INTO access_right_catalog (id, code, name, active) VALUES (5, 'QUERY_ALL', 'Truy vấn toàn bộ', 1)
SELECT 1 FROM dual;

-- ===== Danh muc ca lam viec =====
INSERT ALL
  INTO work_shift (id, shift_no, name, start_hour, end_hour, label, active) VALUES (1, 1, 'Ca 1', 0,  8,  '0-8h',     1)
  INTO work_shift (id, shift_no, name, start_hour, end_hour, label, active) VALUES (2, 2, 'Ca 2', 8,  20, '8h - 20h', 1)
  INTO work_shift (id, shift_no, name, start_hour, end_hour, label, active) VALUES (3, 3, 'Ca 3', 20, 24, '20-24h',   1)
SELECT 1 FROM dual;

-- ===== Nguoi dung =====
INSERT ALL
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (1,  'admin',              'Quản trị hệ thống',      '0901000001', 'admin@agribank.com.vn',              1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (2,  'longngothanh',       'Ngô Thành Long',         '0901000002', 'longngothanh@agribank.com.vn',       1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (3,  'namleviet',          'Lê Việt Nam',            '0901000003', 'namleviet@agribank.com.vn',          1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (4,  'hieuvutrung',        'Vũ Trung Hiếu',          '0901000004', 'hieuvutrung@agribank.com.vn',        1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (5,  'sondangthe',         'Đặng Thế Sơn',           '0901000005', 'sondangthe@agribank.com.vn',         1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (6,  'hiennguyenthiminh',  'Nguyễn Thị Minh Hiền',   '0901000006', 'hiennguyenthiminh@agribank.com.vn',  1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (7,  'hoanguyenduc',       'Nguyễn Đức Hòa',         '0901000007', 'hoanguyenduc@agribank.com.vn',       1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (8,  'anhhoangtuan',       'Hoàng Tuấn Anh',         '0901000008', 'anhhoangtuan@agribank.com.vn',       1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (9,  'thuchoangphuong',    'Hoàng Phương Thức',      '0901000009', 'thuchoangphuong@agribank.com.vn',    1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (10, 'maidoanthithanh',    'Đoàn Thị Thanh Mai',     '0901000010', 'maidoanthithanh@agribank.com.vn',    1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (11, 'huydangquang',       'Đặng Quang Huy',         '0901000011', 'huydangquang@agribank.com.vn',       1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (12, 'nhahotuan',          'Hồ Tuấn Nhạ',            '0901000012', 'nhahotuan@agribank.com.vn',          1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (13, 'lytranthi',          'Trần Thị Lý',            '0901000013', 'lytranthi@agribank.com.vn',          1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (14, 'tieptranvanviet',    'Trần Văn Việt Tiệp',     '0901000014', 'tieptranvanviet@agribank.com.vn',    1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (15, 'dungtohoang',        'Tô Hoàng Dũng',          '0901000015', 'dungtohoang@agribank.com.vn',        1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (16, 'manhnguyenduc',      'Nguyễn Đức Mạnh',        '0901000016', 'manhnguyenduc@agribank.com.vn',      1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (17, 'cuonghamanh',        'Hà Mạnh Cường',          '0901000017', 'cuonghamanh@agribank.com.vn',        1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (18, 'hoangnguyenanh',     'Nguyễn Anh Hoàng',       '0901000018', 'hoangnguyenanh@agribank.com.vn',     1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (19, 'maingothituyet',     'Ngô Thị Tuyết Mai',      '0901000019', 'maingothituyet@agribank.com.vn',     1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (20, 'ngochokim',          'Hồ Kim Ngọc',            '0901000020', 'ngochokim@agribank.com.vn',          1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (21, 'phuongnguyenthithu', 'Nguyễn Thị Thu Phương',  '0901000021', 'phuongnguyenthithu@agribank.com.vn', 1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (22, 'anhtranduc',         'Trần Đức Anh',           '0901000022', 'anhtranduc@agribank.com.vn',         1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (23, 'taingoduy',          'Ngô Duy Tài',            '0901000023', 'taingoduy@agribank.com.vn',          1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (24, 'quynhnguyenngoc',    'Nguyễn Ngọc Quỳnh',      '0901000024', 'quynhnguyenngoc@agribank.com.vn',    1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (25, 'phophamvan',         'Phạm Văn Phố',           '0901000025', 'phophamvan@agribank.com.vn',         1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (26, 'nguyenlethao',       'Lê Thảo Nguyên',         '0901000026', 'nguyenlethao@agribank.com.vn',       1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (27, 'tuyngothanh',        'Ngô Thanh Tùy',          '0901000027', 'tuyngothanh@agribank.com.vn',        1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (28, 'tungdoanhuu',        'Đoàn Hữu Tùng',          '0901000028', 'tungdoanhuu@agribank.com.vn',        1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (29, 'thanga',             'A Thắng',                '0901000029', 'thanga@agribank.com.vn',             1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (30, 'tuana',              'A Tuấn',                 '0901000030', 'tuana@agribank.com.vn',              1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (31, 'trungthanh',         'Thành Trung',            '0901000031', 'trungthanh@agribank.com.vn',         1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (32, 'myha',               'Hà Mỹ',                  '0901000032', 'myha@agribank.com.vn',               1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (33, 'anhduy',             'Duy Anh',                '0901000033', 'anhduy@agribank.com.vn',             1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (34, 'trungdinh',          'Đình Trung',             '0901000034', 'trungdinh@agribank.com.vn',          1, 1, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (35, 'haoluongcong',       'Lương Công Hảo',         '0901000035', 'haoluongcong@agribank.com.vn',       1, 2, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (36, 'dongnguyenduy',      'Nguyễn Duy Đông',        '0901000036', 'dongnguyenduy@agribank.com.vn',      1, 2, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (37, 'linhnguyenthuy',     'Nguyễn Thùy Linh',       '0901000037', 'linhnguyenthuy@agribank.com.vn',     1, 2, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (38, 'dongngohai',         'Ngô Hải Đông',           '0901000038', 'dongngohai@agribank.com.vn',         1, 2, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (39, 'thailuonghong',      'Lương Hồng Thái',        '0901000039', 'thailuonghong@agribank.com.vn',      1, 2, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (40, 'vinhdaoquang',       'Đào Quang Vinh',         '0901000040', 'vinhdaoquang@agribank.com.vn',       2, 3, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (41, 'thanhnguyenha',      'Nguyễn Hà Thanh',        '0901000041', 'thanhnguyenha@agribank.com.vn',      2, 3, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (42, 'anhhoangvan',        'Hoàng Văn Anh',          '0901000042', 'anhhoangvan@agribank.com.vn',        2, 3, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (43, 'minhnguyen',         'Nguyễn Minh',            '0901000043', 'minhnguyen@agribank.com.vn',         2, 3, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (44, 'mauleduc',           'Lê Đức Mậu',             '0901000044', 'mauleduc@agribank.com.vn',           2, 3, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (45, 'thuca',              'A Thực',                 '0901000045', 'thuca@agribank.com.vn',              2, 3, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (46, 'thanhnguyenvan',     'Nguyễn Văn Thành',       '0901000046', 'thanhnguyenvan@agribank.com.vn',     3, 4, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (47, 'tuanlevan',          'Lê Văn Tuấn',            '0901000047', 'tuanlevan@agribank.com.vn',          3, 4, 'ACTIVE')
  INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES (48, 'dana',               'A Dân',                  '0901000048', 'dana@agribank.com.vn',               3, 4, 'ACTIVE')
SELECT 1 FROM dual;

-- ===== Phan vai tro: ADMIN + REQUESTER =====
INSERT ALL
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (1, 8, NULL, NULL, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (1, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (2, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (3, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (4, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (5, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (6, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (7, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (8, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (9, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (10, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (11, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (12, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (13, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (14, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (15, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (16, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (17, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (18, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (19, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (20, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (21, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (22, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (23, 1, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (35, 1, 1, 2, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (36, 1, 1, 2, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (40, 1, 2, 3, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (41, 1, 2, 3, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (46, 1, 3, 4, NULL, 1)
SELECT 1 FROM dual;

-- ===== CHECKER (Nguoi kiem tra) theo he thong =====
INSERT ALL
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (2, 4, 1, 1, 1, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (3, 4, 1, 1, 1, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (4, 4, 1, 1, 2, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (3, 4, 1, 1, 2, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (5, 4, 1, 1, 3, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (5, 4, 1, 1, 4, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (6, 4, 1, 1, 5, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (7, 4, 1, 1, 5, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (8, 4, 1, 1, 5, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (9, 4, 1, 1, 6, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (10, 4, 1, 1, 6, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (9, 4, 1, 1, 7, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (10, 4, 1, 1, 7, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (11, 4, 1, 1, 8, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (12, 4, 1, 1, 9, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (12, 4, 1, 1, 10, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (13, 4, 1, 1, 11, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (14, 4, 1, 1, 11, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (15, 4, 1, 1, 11, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (16, 4, 1, 1, 11, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (13, 4, 1, 1, 12, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (15, 4, 1, 1, 12, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (16, 4, 1, 1, 12, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (16, 4, 1, 1, 13, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (17, 4, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (18, 4, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (19, 4, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (20, 4, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (21, 4, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (22, 4, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (7, 4, 1, 1, 15, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (8, 4, 1, 1, 15, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (8, 4, 1, 1, 16, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (23, 4, 1, 1, 16, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (23, 4, 1, 1, 17, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (23, 4, 1, 1, 18, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (35, 4, 1, 2, 19, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (36, 4, 1, 2, 19, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (40, 4, 2, 3, 20, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (41, 4, 2, 3, 20, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (46, 4, 3, 4, 21, 1)
SELECT 1 FROM dual;

-- ===== EXECUTOR (Nguoi thuc hien) theo he thong =====
INSERT ALL
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (2, 7, 1, 1, 1, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (3, 7, 1, 1, 1, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (4, 7, 1, 1, 2, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (3, 7, 1, 1, 2, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (5, 7, 1, 1, 3, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (5, 7, 1, 1, 4, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (6, 7, 1, 1, 5, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (7, 7, 1, 1, 5, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (8, 7, 1, 1, 5, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (9, 7, 1, 1, 6, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (10, 7, 1, 1, 6, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (9, 7, 1, 1, 7, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (10, 7, 1, 1, 7, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (11, 7, 1, 1, 8, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (12, 7, 1, 1, 9, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (12, 7, 1, 1, 10, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (13, 7, 1, 1, 11, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (14, 7, 1, 1, 11, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (15, 7, 1, 1, 11, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (16, 7, 1, 1, 11, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (13, 7, 1, 1, 12, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (15, 7, 1, 1, 12, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (16, 7, 1, 1, 12, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (16, 7, 1, 1, 13, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (17, 7, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (18, 7, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (19, 7, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (20, 7, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (21, 7, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (22, 7, 1, 1, 14, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (7, 7, 1, 1, 15, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (8, 7, 1, 1, 15, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (8, 7, 1, 1, 16, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (23, 7, 1, 1, 16, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (23, 7, 1, 1, 17, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (23, 7, 1, 1, 18, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (35, 7, 1, 2, 19, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (36, 7, 1, 2, 19, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (40, 7, 2, 3, 20, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (41, 7, 2, 3, 20, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (46, 7, 3, 4, 21, 1)
SELECT 1 FROM dual;

-- ===== DEPT_MANAGER / AUTHORITY / ACCESS_TEAM / DBA =====
INSERT ALL
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (24, 2, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (25, 2, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (26, 2, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (27, 2, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (8, 2, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (28, 2, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (37, 2, 1, 2, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (38, 2, 1, 2, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (39, 2, 1, 2, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (42, 2, 2, 3, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (43, 2, 2, 3, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (44, 2, 2, 3, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (47, 2, 3, 4, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (29, 3, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (30, 3, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (45, 3, 2, 3, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (48, 3, 3, 4, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (31, 5, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (32, 5, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (33, 6, 1, 1, NULL, 1)
  INTO user_role (user_id, role_id, unit_id, department_id, system_id, active) VALUES (34, 6, 1, 1, NULL, 1)
SELECT 1 FROM dual;

-- ===== Du lieu dang ky mau (man hinh 01YCTC_Dangky) =====
INSERT ALL
  INTO access_registration (requester_user_id, system_id, database_id, object_name, access_rights, shift_no, from_date, to_date, signed, created_at)
    VALUES (2, 1, 1, 'All Schema', 'SELECT,INSERT,UPDATE,DELETE', 2, TRUNC(SYSDATE), TRUNC(SYSDATE), 1, SYSTIMESTAMP)
  INTO access_registration (requester_user_id, system_id, database_id, object_name, access_rights, shift_no, from_date, to_date, signed, created_at)
    VALUES (3, 1, 1, 'All Schema', 'SELECT',                      2, TRUNC(SYSDATE), TRUNC(SYSDATE), 1, SYSTIMESTAMP)
  INTO access_registration (requester_user_id, system_id, database_id, object_name, access_rights, shift_no, from_date, to_date, signed, created_at)
    VALUES (3, 2, 2, 'All Schema', 'SELECT,INSERT,UPDATE,DELETE', 2, TRUNC(SYSDATE), TRUNC(SYSDATE), 1, SYSTIMESTAMP)
SELECT 1 FROM dual;

COMMIT;

-- ===== Dong bo lai IDENTITY sau khi chen ban ghi co id co dinh =====
-- START WITH LIMIT VALUE: Oracle tu dat gia tri tiep theo = max(id)+1 cho moi bang.
ALTER TABLE role                 MODIFY id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE unit                 MODIFY id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE department           MODIFY id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE app_user             MODIFY id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE information_system   MODIFY id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE database_catalog     MODIFY id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE access_right_catalog MODIFY id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE work_shift           MODIFY id GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
