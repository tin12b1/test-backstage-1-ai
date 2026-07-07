-- =====================================================================
-- Drop an toan cac bang cua ung dung (chay TRUOC V1__schema.sql khi can lam lai schema).
-- Chi drop cac bang thuoc danh sach da biet, neu bang khong ton tai thi bo qua (khong loi).
-- CANH BAO: Thao tac nay XOA du lieu trong cac bang lien quan tren schema hien tai (qltcdb).
-- Chi chay tren schema danh rieng cho ung dung.
-- =====================================================================

BEGIN
  FOR t IN (
    SELECT column_value AS tname FROM TABLE(sys.odcivarchar2list(
      'PRE_REGISTRATION_REQUEST',
      'WORK_LOG_07',
      'EMERGENCY_COMPLETION_LINK',
      'EMAIL_QUEUE',
      'OTP_TRANSACTION',
      'LOGIN_LOG',
      'AUDIT_LOG',
      'WORKFLOW_HISTORY',
      'REQUEST_SCRIPT_FILE',
      'REQUEST_SIGNATURE',
      'REQUEST_DETAIL',
      'ACCESS_REQUEST',
      'USER_TOTP',
      'SIGNATURE_IMAGE',
      'USER_ROLE',
      'APP_USER',
      'WORK_SHIFT',
      'ROLE',
      'ACCESS_RIGHT_CATALOG',
      'DATABASE_CATALOG',
      'INFORMATION_SYSTEM',
      'DEPARTMENT',
      'UNIT'
    ))
  ) LOOP
    BEGIN
      EXECUTE IMMEDIATE 'DROP TABLE ' || t.tname || ' CASCADE CONSTRAINTS PURGE';
    EXCEPTION
      WHEN OTHERS THEN
        IF SQLCODE != -942 THEN  -- -942: table or view does not exist -> bo qua
          RAISE;
        END IF;
    END;
  END LOOP;
END;
/
