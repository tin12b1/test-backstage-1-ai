# Script DDL Oracle

Thư mục chứa script khởi tạo/nâng cấp schema Oracle cho hệ thống Quản lý truy cập CSDL.
Áp dụng khi chạy với profile `oracle` (`SPRING_PROFILES_ACTIVE=oracle`), trong đó
`ddl-auto=validate` nên schema phải được tạo trước bằng các script này.

## Kết nối mặc định (application-oracle.yml)

- URL: `jdbc:oracle:thin:@//10.0.83.36:1521/ebankpdb_pt`
- User/pass: `qltcdb` / `qltcdb` (override qua `ORACLE_USER` / `ORACLE_PASSWORD`)

## Thứ tự chạy

| Thứ tự | File | Mục đích |
|---|---|---|
| 0 | `V0__drop.sql` | (Tùy chọn) Drop an toàn toàn bộ bảng cũ trước khi tạo lại. ⚠️ Xóa dữ liệu |
| 1 | `V1__schema.sql` | Tạo mới toàn bộ 23 bảng (cài đặt sạch) |
| 2 | `V3__seed_catalog.sql` | Seed danh mục (vai trò, đơn vị, phòng, hệ thống, CSDL, ca, người dùng, phân quyền) |
| — | `V2__04b_bgtk_columns.sql` | Chỉ dùng khi schema cũ đã có sẵn nhưng thiếu/sai kiểu các cột 04B-BGTK |

> **Cài đặt mới trên Oracle (khuyến nghị):** chạy lần lượt `V0__drop.sql` → `V1__schema.sql`
> → `V3__seed_catalog.sql`, rồi khởi động app với `SPRING_PROFILES_ACTIVE=oracle`.
> `V2` chỉ dành cho môi trường đã tồn tại bảng cần vá kiểu cột (không cần nếu đã chạy V1 mới).

## Quy ước kiểu dữ liệu (khớp entity JPA + Oracle12cDialect)

| Java | Oracle |
|---|---|
| `Long` | `NUMBER(19,0)` |
| `Integer` | `NUMBER(10,0)` |
| `boolean` | `NUMBER(1,0)` |
| `String(n)` | `VARCHAR2(n CHAR)` |
| `LocalDateTime` | `TIMESTAMP` |
| `LocalDate` | `DATE` |
| `@Lob String` | `CLOB` |
| `@Lob byte[]` | `BLOB` |

## Cách chạy (sqlplus / SQLcl)

```
sqlplus qltcdb/qltcdb@//10.0.83.36:1521/ebankpdb_pt
@V0__drop.sql
@V1__schema.sql
@V3__seed_catalog.sql
```

Sau đó khởi động ứng dụng theo profile Oracle:

```
set SPRING_PROFILES_ACTIVE=oracle
set AD_MODE=mock        REM hoac esb neu co mang toi truc
mvn spring-boot:run
```

## Lưu ý

- Seed dùng `INSERT ALL ... SELECT 1 FROM dual` (Oracle không hỗ trợ INSERT nhiều dòng kiểu H2).
- Boolean H2 (`TRUE/FALSE`) đã đổi thành `1/0`; `CURRENT_DATE`→`TRUNC(SYSDATE)`, `CURRENT_TIMESTAMP`→`SYSTIMESTAMP`.
- Sau khi seed các bản ghi id cố định, IDENTITY được đặt lại (`START WITH 100/1000`) để tránh trùng khi tự sinh.
