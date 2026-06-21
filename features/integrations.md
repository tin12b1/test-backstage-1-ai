# Feature: Service tích hợp OTP, Email, AD

**Người phụ trách:** Cường  
**Mã hạng mục:** 2.7

## 1. Mục tiêu

Xây dựng các service tích hợp phục vụ đăng nhập, ký xác nhận và thông báo nghiệp vụ.

## 2. Phạm vi

- AD: kết nối AD để dùng cho đăng nhập và lấy thông tin người dùng.
- SoftOTP: xác nhận khi người dùng ký xác nhận.
- Email: tổng hợp danh sách và gửi email thông báo nghiệp vụ.

## 3. AD Integration

### Chức năng

- Xác thực user/password AD.
- Lấy thông tin user: họ tên, email, điện thoại, đơn vị/phòng nếu có.
- Trả kết quả rõ ràng cho các tình huống: thành công, sai mật khẩu, user bị khóa/không tồn tại, lỗi kết nối.

### Interface đề xuất

```java
public interface AdClient {
    AdAuthResult authenticate(String username, String password);
    AdUserProfile getUserProfile(String username);
}
```

### Các chế độ triển khai (`integration.ad.mode`)

| Mode   | Lớp cài đặt     | Dùng cho                                            |
|--------|-----------------|-----------------------------------------------------|
| `mock` | `MockAdClient`  | Dev/test, danh bạ giả lập, mật khẩu `password`.     |
| `ldap` | `LdapAdClient`  | Bind LDAP trực tiếp qua JNDI.                        |
| `esb`  | `EsbAdClient`   | Gọi dịch vụ SOAP `VerifyUserAD` trên trục tích hợp. |

### 3.1. Dịch vụ ESB `VerifyUserAD`

Môi trường thật xác thực qua **dịch vụ SOAP trên trục tích hợp (ESB/SOA)** chứ không bind AD
trực tiếp. App gửi `VerifyUserADReq`, trục chuyển tới AD và trả `VerifyUserADRes`.

**Định tuyến / context:** header `Common/AdditionalInformation` mang `TARGETID=AD`,
`TARGETSERVICE=VerifyUserAD`.

**Cấu trúc Request (`VerifyUserADReq`)**

- `Header/Common`: `ServiceVersion`, `MessageId` (UUID), `TransactionId`, `MessageTimestamp` (ISO-8601).
- `Header/Client`: `SourceAppID` (vd `EBANK`), `UserDetail/UserId` + `UserDetail/UserPassword`.
  Đây là **credential cấp ứng dụng** để gọi trục, khác tài khoản người dùng cuối.
- `BodyReq`:
  - `FunctionCode` = `AUTH-AUTHENADUSER-LDAP-AD`
  - `UserName` = tài khoản người dùng, **kèm domain prefix** (vd `CORP\TRUNGLENGUYENTHANH`)
  - `Password` = mật khẩu người dùng (không bao giờ log dạng rõ)

> `UserDetail/UserPassword` trên bản tin là **chuỗi ASCII đã mã hóa hexa** của mật khẩu hệ thống
> (vd `EBANKING@2020` → `4542414e4b494e474032303230`). Cấu hình lưu mật khẩu dạng rõ trong
> secret store; `EsbAdClient` tự mã hóa hexa khi dựng bản tin.

**Cấu trúc Response (`VerifyUserADRes`)** — phải kiểm tra **cả hai mức**:

- `ResponseStatus` (mức trục): `Status`, `GlobalErrorCode`, `GlobalErrorDescription`.
- `BodyRes` (mức nghiệp vụ): `Result`, `Description`.

**Quy tắc map kết quả → `AdAuthResult`:**

| Điều kiện                                   | Kết quả                          |
|---------------------------------------------|----------------------------------|
| `ResponseStatus/Status` = 0 và `Result` = 0 | `SUCCESS`                        |
| `ResponseStatus/Status` ≠ 0                 | `CONNECTION_ERROR` (lỗi trục)    |
| `Result` ≠ 0, mô tả chứa "lock/khóa"        | `USER_LOCKED`                    |
| `Result` ≠ 0, mô tả chứa "không tồn tại"    | `USER_NOT_FOUND`                 |
| `Result` ≠ 0 (mặc định)                     | `BAD_CREDENTIALS`                |
| Timeout / không kết nối / parse lỗi         | `CONNECTION_ERROR`               |

> Bộ mã lỗi nghiệp vụ chi tiết (`Result`/`GlobalErrorCode`) cần tài liệu chính thức từ đội trục;
> hiện `EsbAdClient` map theo mô tả văn bản và mặc định an toàn về `BAD_CREDENTIALS`. Khi có
> danh mục mã lỗi, cập nhật `mapBusinessError(...)`.

**Ghi chú kỹ thuật:**

- `EsbAdClient` không thêm dependency SOAP riêng: dựng XML bằng chuỗi, gọi `RestTemplate`
  (content-type `text/xml`), parse bằng JAXP (đã tắt DTD/external entity chống XXE).
- `VerifyUserAD` chỉ xác thực, không trả hồ sơ người dùng; `getUserProfile` hiện trả hồ sơ tối
  thiểu. Nếu trục có service riêng để lấy thông tin cán bộ thì bổ sung sau.

### Quy tắc

- Không log mật khẩu.
- Timeout kết nối phải có cấu hình.
- Lỗi AD phải được xử lý thân thiện trên màn hình đăng nhập.

## 4. SoftOTP Integration

### Chức năng

- Xác thực OTP khi người dùng ký xác nhận.
- Dùng cho ký tại thông tin chung, ký chi tiết, ký phê duyệt, ký xác nhận thực hiện.
- Lưu kết quả giao dịch OTP để audit.

### Interface đề xuất

```java
public interface OtpClient {
    OtpVerifyResult verify(String username, String otp, String purpose, Long requestId);
}
```

### Các chế độ triển khai (`integration.otp.mode`)

| Mode   | Lớp cài đặt     | Dùng cho                                          |
|--------|-----------------|---------------------------------------------------|
| `mock` | `MockOtpClient` | Dev/test, OTP đúng = giá trị cấu hình (123456).   |
| `esb`  | `EsbOtpClient`  | Gọi dịch vụ SOAP `VerifyOTP` trên trục tích hợp.  |

### 4.1. Dịch vụ ESB `VerifyOTP`

SoftOTP được xác thực qua dịch vụ SOAP trên trục (header định tuyến `TARGETID=OTP`,
`TARGETSERVICE=verifyOTPType`). Dùng chung envelope `commonheader/1` và credential cấp ứng dụng
như `VerifyUserAD`.

**Cấu trúc `BodyReq`**

- `FunctionCode` = `AUTH-VERIFYOTP-WS-OTP`
- `TransType` = loại giao dịch (cấu hình, vd `5`)
- `TransactionId` = UUID sinh cho từng lần gọi
- `DeviceTypeId` = loại thiết bị (cấu hình, vd `1`)
- `VerifyOTPType` = loại OTP (cấu hình, vd `26`)
- `OTP` = mã OTP người dùng nhập (không log dạng rõ)

> Bản tin `VerifyOTP` không mang username; username chỉ dùng để log/audit ở tầng `OtpService`.

**Response (`VerifyOTPRes`)** — kiểm tra **cả hai mức**:

- `ResponseStatus/Status` (mức trục)
- `BodyRes/ResponseCode` + `BodyRes/Message` (mức nghiệp vụ)

**Quy tắc map kết quả → `OtpVerifyResult`:**

| Điều kiện                                          | Kết quả                       |
|----------------------------------------------------|-------------------------------|
| `ResponseStatus/Status` = 0 và `ResponseCode` = 0  | `success()`                   |
| `ResponseStatus/Status` ≠ 0                        | `failure()` (lỗi trục)        |
| `ResponseCode` ≠ 0                                 | `failure(Message)`            |
| Thiếu OTP / timeout / parse lỗi                    | `failure(...)`                |

Khi có bộ mã `ResponseCode` chính thức (OTP sai, hết hạn, khóa thiết bị...), cập nhật mapping
trong `parseResponse(...)` để phân loại thông báo chi tiết hơn.

### Quy tắc

- OTP đúng mới cho ký thành công.
- Sau ký thành công, lưu thông tin chữ ký và hiển thị ảnh chữ ký được khai báo trên hệ thống.
- Chuyển trả không cần ký xác nhận/OTP.
- Không log giá trị OTP dạng rõ.

## 5. Email Integration

### Sự kiện cần gửi email

- Có yêu cầu mới chờ kiểm tra/phê duyệt.
- Yêu cầu bị chuyển trả.
- Yêu cầu đã được lãnh đạo phòng/bộ phận phê duyệt.
- Yêu cầu đã được Người có thẩm quyền phê duyệt.
- Yêu cầu đã chuyển đến bộ phận Mở truy cập/DBA/Người thực hiện.
- Yêu cầu đã hoàn thành hoặc đã được xác nhận mở truy cập.

### Interface đề xuất

```java
public interface EmailService {
    void sendWorkflowNotification(WorkflowNotification notification);
    void sendBatch(List<WorkflowNotification> notifications);
}
```

### Quy tắc

- Gửi email không được làm mất dữ liệu yêu cầu nếu mail lỗi.
- Nên có bảng `email_queue` để lưu trạng thái gửi.
- Cho phép retry khi gửi lỗi.
- Nội dung email phải có mã yêu cầu, loại yêu cầu, trạng thái, người gửi, người/bộ phận cần xử lý và link truy cập.

## 6. Cấu hình cần có

- AD URL/domain/base DN hoặc thông số LDAP tương ứng.
- Timeout AD.
- Khi `mode=esb`: endpoint dịch vụ `VerifyUserAD`, `SourceAppID`, `UserId`/mật khẩu hệ thống
  (secret, dạng rõ), `FunctionCode`, domain prefix, timeout kết nối/đọc, `SOAPAction` (tùy chọn).
- Endpoint/credential SoftOTP.
- Khi OTP `mode=esb`: endpoint dịch vụ `VerifyOTP`, credential hệ thống, `FunctionCode`,
  `TransType`/`DeviceTypeId`/`VerifyOTPType`, timeout, `SOAPAction` (tùy chọn).
- SMTP host/port/user hoặc relay nội bộ.
- Email sender mặc định.
- Số lần retry email.

## 7. Allowed Files

- `src/main/java/.../integration/ad/**`
- `src/main/java/.../integration/otp/**`
- `src/main/java/.../integration/email/**`
- `src/main/java/.../notification/**`
- `src/main/resources/templates/email/**`
- `src/test/java/.../integration/**`
- `src/test/java/.../notification/**`

## 8. Must Not Change

- Không sửa màn hình nghiệp vụ ngoài việc cung cấp interface/service.
- Không sửa workflow nếu chưa thống nhất contract gọi service.
- Không hard-code thông tin kết nối trong code.

## 9. Verification

- Mock AD đăng nhập thành công/thất bại.
- Mock OTP ký thành công/thất bại.
- OTP thất bại không tạo chữ ký và không chuyển bước.
- Email được tạo đúng nội dung khi có sự kiện workflow.
- Email lỗi được ghi trạng thái lỗi và cho phép retry.
- Không log mật khẩu hoặc OTP rõ.

## 10. Definition of Done

- Có interface và implementation/mock cho AD, OTP, Email.
- Có cấu hình externalized trong application properties/yaml.
- Có test cho các tình huống tích hợp chính.
- Có audit/log an toàn cho giao dịch tích hợp.
