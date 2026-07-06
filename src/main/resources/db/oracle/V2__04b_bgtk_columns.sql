-- =====================================================================
-- Migration Oracle: chuan hoa cot phuc vu mau 04B-BGTK (Bien ban ban giao tai khoan)
-- Cac cot khoa ngoai (kieu Long trong entity) phai la NUMBER(19,0), khong phai VARCHAR2.
-- Chay bang user so huu schema (vd: qltcdb) tren PDB ebankpdb_pt.
-- Neu cot chua ton tai -> dung ADD; neu da ton tai sai kieu (VARCHAR2) -> dung MODIFY.
-- Yeu cau: cot dang rong (khong co du lieu) hoac du lieu chuyen doi duoc sang so.
-- =====================================================================

-- Truong hop cot da ton tai voi kieu sai (VARCHAR2) -> doi sang NUMBER(19,0):
ALTER TABLE access_request MODIFY (handover_manager_id NUMBER(19,0));
ALTER TABLE access_request MODIFY (receiver_user_id    NUMBER(19,0));
ALTER TABLE access_request MODIFY (receiver_manager_id NUMBER(19,0));
ALTER TABLE access_request MODIFY (source_request_id   NUMBER(19,0));

-- Cot scope tren request_detail la chuoi (VARCHAR2) - giu/nhan dung do dai:
ALTER TABLE request_detail MODIFY (scope VARCHAR2(200));

-- ---------------------------------------------------------------------
-- Neu cac cot CHUA ton tai, dung cac lenh ADD sau thay cho MODIFY o tren:
-- ALTER TABLE access_request ADD (handover_manager_id NUMBER(19,0));
-- ALTER TABLE access_request ADD (receiver_user_id    NUMBER(19,0));
-- ALTER TABLE access_request ADD (receiver_manager_id NUMBER(19,0));
-- ALTER TABLE access_request ADD (source_request_id   NUMBER(19,0));
-- ALTER TABLE request_detail ADD (scope VARCHAR2(200));
-- ---------------------------------------------------------------------

COMMIT;
