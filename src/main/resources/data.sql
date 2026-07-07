-- =====================================================================
-- [DEV-OVERRIDE] Seed data for H2 in-memory database
-- Translated from V3__seed_catalog.sql (Oracle) to H2-compatible SQL.
-- Original Oracle seed: src/main/resources/db/oracle/V3__seed_catalog.sql
-- To restore: remove this file or set spring.sql.init.mode=never
-- =====================================================================

-- ===== Vai tro =====
INSERT INTO role (id, code, name, description) VALUES
 (1, 'REQUESTER', 'Nguoi lap yeu cau', 'Tao, ky, gui yeu cau'),
 (2, 'DEPT_MANAGER', 'Truong phong/tuong duong', 'Kiem tra, ky, chuyen tiep'),
 (3, 'AUTHORITY', 'Nguoi co tham quyen', 'Phe duyet'),
 (4, 'CHECKER', 'Bo phan kiem tra', 'Kiem tra noi dung/script'),
 (5, 'ACCESS_TEAM', 'Bo phan mo truy cap', 'Mo truy cap, ghi thoi gian'),
 (6, 'DBA', 'Quan tri CSDL/DBA', 'Xu ly cau truc/tai khoan'),
 (7, 'EXECUTOR', 'Nguoi thuc hien', 'Chay script/chinh sua du lieu'),
 (8, 'ADMIN', 'Quan tri he thong', 'Cau hinh he thong');

-- ===== Don vi =====
INSERT INTO unit (id, code, name, active) VALUES
 (1, 'DV-CNTT', 'Don vi Cong nghe thong tin', TRUE),
 (2, 'DV-KD', 'Don vi Kinh doanh', TRUE);

-- ===== Phong/bo phan =====
INSERT INTO department (id, code, name, unit_id, active) VALUES
 (1, 'P-QT', 'Phong Quan tri', 1, TRUE),
 (2, 'P-KD', 'Phong Kinh doanh', 2, TRUE),
 (3, 'BLD-KD', 'Ban Lanh dao KD', 2, TRUE),
 (4, 'P-KT', 'Phong Kiem tra', 1, TRUE),
 (5, 'P-VH', 'Phong Van hanh', 1, TRUE),
 (6, 'P-CSDL', 'Phong CSDL', 1, TRUE);

-- ===== Nguoi dung (dung de login dev mode - khong can password) =====
INSERT INTO app_user (id, username, full_name, mobile, email, unit_id, department_id, status) VALUES
 (1, 'admin', 'Quan tri he thong', '0900000000', 'admin@csdl.local', 1, 1, 'ACTIVE'),
 (2, 'requester1', 'Nguyen Van A', '0900000001', 'requester1@csdl.local', 2, 2, 'ACTIVE'),
 (3, 'manager1', 'Tran Thi B', '0900000002', 'manager1@csdl.local', 2, 2, 'ACTIVE'),
 (4, 'authority1', 'Le Van C', '0900000003', 'authority1@csdl.local', 2, 3, 'ACTIVE'),
 (5, 'checker1', 'Pham Thi D', '0900000004', 'checker1@csdl.local', 1, 4, 'ACTIVE'),
 (6, 'access1', 'Hoang Van E', '0900000005', 'access1@csdl.local', 1, 5, 'ACTIVE'),
 (7, 'dba1', 'Vo Thi F', '0900000006', 'dba1@csdl.local', 1, 6, 'ACTIVE'),
 (8, 'executor1', 'Dang Van G', '0900000007', 'executor1@csdl.local', 1, 5, 'ACTIVE');

-- ===== Gan vai tro =====
INSERT INTO user_role (id, user_id, role_id, active) VALUES
 (1, 1, 8, TRUE),
 (2, 1, 1, TRUE),
 (3, 2, 1, TRUE),
 (4, 3, 2, TRUE),
 (5, 3, 1, TRUE),
 (6, 4, 3, TRUE),
 (7, 5, 4, TRUE),
 (8, 6, 5, TRUE),
 (9, 7, 6, TRUE),
 (10, 8, 7, TRUE);

-- ===== He thong thong tin =====
INSERT INTO information_system (id, code, name, owner_unit_id, active) VALUES
 (1, 'SYS01', 'He thong Core Kinh doanh', 2, TRUE),
 (2, 'SYS02', 'He thong Quan tri noi bo', 1, TRUE);

-- ===== CSDL =====
INSERT INTO database_catalog (id, system_id, code, name, owner_unit_id, active) VALUES
 (1, 1, 'DB01', 'CSDL Core', 1, TRUE),
 (2, 1, 'DB02', 'CSDL Bao cao', 1, TRUE),
 (3, 2, 'DB03', 'CSDL Quan tri', 1, TRUE);

-- ===== Danh muc quyen truy cap =====
INSERT INTO access_right_catalog (id, code, name, active) VALUES
 (1, 'SELECT', 'Truy van du lieu', TRUE),
 (2, 'INSERT', 'Them du lieu', TRUE),
 (3, 'UPDATE', 'Cap nhat du lieu', TRUE),
 (4, 'DELETE', 'Xoa du lieu', TRUE),
 (5, 'QUERY_ALL', 'Truy van toan bo', TRUE);

-- ===== Ca lam viec =====
INSERT INTO work_shift (id, shift_no, name, start_hour, end_hour, active) VALUES
 (1, 1, 'Ca 1', 0, 8, TRUE),
 (2, 2, 'Ca 2', 8, 20, TRUE),
 (3, 3, 'Ca 3', 20, 24, TRUE);

-- ===== pre_registration_request (sample for testing) =====
-- unit_code stores the numeric unit ID as string (matching PreRegistrationService.create() logic)
-- requestType: use same values as PreRegistrationService.deriveRequestType()
INSERT INTO pre_registration_request (id, user_id, unit_code, register_date, shift, request_type, system_id, database_id, object_name, access_rights, status, created_at, signed_at, version) VALUES
 (1, 2, '2', CURRENT_DATE, 2, NULL, 1, 1, 'All Schema', 'SELECT', 'UNUSED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO pre_registration_request (id, user_id, unit_code, register_date, shift, request_type, system_id, database_id, object_name, access_rights, status, created_at, signed_at, version) VALUES
 (2, 3, '2', CURRENT_DATE, 2, NULL, 1, 1, 'CUSTOMER_TABLE', 'SELECT', 'UNUSED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- ===== Dong bo sequence/identity sau khi chen ban ghi co id co dinh =====
ALTER TABLE role ALTER COLUMN id RESTART WITH 100;
ALTER TABLE unit ALTER COLUMN id RESTART WITH 100;
ALTER TABLE department ALTER COLUMN id RESTART WITH 100;
ALTER TABLE app_user ALTER COLUMN id RESTART WITH 100;
ALTER TABLE user_role ALTER COLUMN id RESTART WITH 100;
ALTER TABLE information_system ALTER COLUMN id RESTART WITH 100;
ALTER TABLE database_catalog ALTER COLUMN id RESTART WITH 100;
ALTER TABLE access_right_catalog ALTER COLUMN id RESTART WITH 100;
ALTER TABLE work_shift ALTER COLUMN id RESTART WITH 100;
ALTER TABLE pre_registration_request ALTER COLUMN id RESTART WITH 100;

