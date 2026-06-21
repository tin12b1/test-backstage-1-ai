package com.csdl.access.integration.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * OTP client goi dich vu SOAP "VerifyOTP" tren truc tich hop (ESB/SOA) cua Agribank.
 *
 * <p>Kich hoat khi {@code integration.otp.mode=esb}. Toan bo thong tin ket noi va credential
 * cap ung dung lay tu cau hinh, khong hard-code (features/integrations.md muc 8).</p>
 *
 * <p>Khong dung dependency SOAP rieng: build XML request bang chuoi va parse response bang JAXP,
 * dong nhat voi {@link com.csdl.access.integration.ad.EsbAdClient}.</p>
 *
 * <p>Luu y nghiep vu:</p>
 * <ul>
 *   <li>Phai kiem tra ca hai muc: {@code ResponseStatus/Status} (muc truc) va
 *       {@code BodyRes/ResponseCode} (muc nghiep vu). Ca hai bang 0 moi coi la thanh cong.</li>
 *   <li>Khong log gia tri OTP dang ro.</li>
 *   <li>{@code UserDetail/UserPassword} la chuoi ASCII ma hexa cua mat khau he thong; cau hinh
 *       nhan mat khau dang ro, lop nay tu ma hoa hexa khi dung bang tin.</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "integration.otp.mode", havingValue = "esb")
public class EsbOtpClient implements OtpClient {

    private static final Logger log = LoggerFactory.getLogger(EsbOtpClient.class);

    private static final String NS_HEADER = "http://www.agribank.com.vn/common/envelope/commonheader/1";
    private static final String NS_AUTHEN = "http://www.agribank.com.vn/entity/vn/authen/authensvcs/1";

    @Value("${integration.otp.esb.endpoint}")
    private String endpoint;

    @Value("${integration.otp.esb.source-app-id:EBANK}")
    private String sourceAppId;

    @Value("${integration.otp.esb.service-user-id:EBANK}")
    private String serviceUserId;

    /** Mat khau he thong dang ro; se duoc ma hoa hexa khi gui. */
    @Value("${integration.otp.esb.service-password:}")
    private String servicePassword;

    @Value("${integration.otp.esb.function-code:AUTH-VERIFYOTP-WS-OTP}")
    private String functionCode;

    @Value("${integration.otp.esb.service-version:1}")
    private String serviceVersion;

    @Value("${integration.otp.esb.trans-type:5}")
    private String transType;

    @Value("${integration.otp.esb.device-type-id:1}")
    private String deviceTypeId;

    @Value("${integration.otp.esb.verify-otp-type:26}")
    private String verifyOtpType;

    /** SOAPAction header (tuy chon). */
    @Value("${integration.otp.esb.soap-action:}")
    private String soapAction;

    @Value("${integration.otp.esb.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${integration.otp.esb.read-timeout-ms:10000}")
    private int readTimeoutMs;

    private RestTemplate restTemplate;

    private RestTemplate restTemplate() {
        if (restTemplate == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(connectTimeoutMs);
            factory.setReadTimeout(readTimeoutMs);
            restTemplate = new RestTemplate(factory);
        }
        return restTemplate;
    }

    @Override
    public OtpVerifyResult verify(String username, String otp, String purpose, Long requestId) {
        // Khong log gia tri OTP.
        log.debug("[ESB-OTP] verify username={} purpose={} requestId={}", username, purpose, requestId);
        if (otp == null || otp.isBlank()) {
            return OtpVerifyResult.failure("Thieu ma OTP");
        }

        String txId = UUID.randomUUID().toString();
        String requestXml = buildRequest(txId, otp);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            if (soapAction != null && !soapAction.isBlank()) {
                headers.add("SOAPAction", soapAction);
            }
            HttpEntity<String> entity = new HttpEntity<>(requestXml, headers);

            ResponseEntity<String> response =
                    restTemplate().postForEntity(endpoint, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("[ESB-OTP] HTTP loi status={} requestId={}", response.getStatusCode(), requestId);
                return OtpVerifyResult.failure("Khong ket noi duoc dich vu OTP");
            }
            return parseResponse(response.getBody(), requestId);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("[ESB-OTP] Loi ket noi truc requestId={}: {}", requestId, e.getMessage());
            return OtpVerifyResult.failure("Khong ket noi duoc dich vu OTP");
        } catch (Exception e) {
            log.error("[ESB-OTP] Loi goi dich vu VerifyOTP requestId={}: {}", requestId, e.getMessage());
            return OtpVerifyResult.failure("Loi he thong xac thuc OTP");
        }
    }

    private String buildRequest(String txId, String otp) {
        String messageId = UUID.randomUUID().toString();
        String envTransactionId = String.valueOf(System.currentTimeMillis());
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String passwordHex = toHex(servicePassword);

        StringBuilder sb = new StringBuilder();
        sb.append("<ns3:VerifyOTPReq xmlns:ns2=\"").append(NS_HEADER)
                .append("\" xmlns:ns3=\"").append(NS_AUTHEN).append("\">");
        sb.append("<ns2:Header>");
        sb.append("<ns2:Common>");
        sb.append("<ns2:ServiceVersion>").append(xml(serviceVersion)).append("</ns2:ServiceVersion>");
        sb.append("<ns2:MessageId>").append(messageId).append("</ns2:MessageId>");
        sb.append("<ns2:TransactionId>").append(envTransactionId).append("</ns2:TransactionId>");
        sb.append("<ns2:MessageTimestamp>").append(timestamp).append("</ns2:MessageTimestamp>");
        sb.append("</ns2:Common>");
        sb.append("<ns2:Client>");
        sb.append("<ns2:SourceAppID>").append(xml(sourceAppId)).append("</ns2:SourceAppID>");
        sb.append("<ns2:TargetAppIDs/>");
        sb.append("<ns2:UserDetail>");
        sb.append("<ns2:UserId>").append(xml(serviceUserId)).append("</ns2:UserId>");
        sb.append("<ns2:UserPassword>").append(passwordHex).append("</ns2:UserPassword>");
        sb.append("</ns2:UserDetail>");
        sb.append("</ns2:Client>");
        sb.append("</ns2:Header>");
        sb.append("<BodyReq>");
        sb.append("<FunctionCode>").append(xml(functionCode)).append("</FunctionCode>");
        sb.append("<TransType>").append(xml(transType)).append("</TransType>");
        sb.append("<TransactionId>").append(xml(txId)).append("</TransactionId>");
        sb.append("<DeviceTypeId>").append(xml(deviceTypeId)).append("</DeviceTypeId>");
        sb.append("<VerifyOTPType>").append(xml(verifyOtpType)).append("</VerifyOTPType>");
        sb.append("<OTP>").append(xml(otp)).append("</OTP>");
        sb.append("</BodyReq>");
        sb.append("</ns3:VerifyOTPReq>");
        return sb.toString();
    }

    /**
     * Parse response, kiem tra ResponseStatus/Status (truc) va BodyRes/ResponseCode (nghiep vu).
     * Tim phan tu theo local name de khong phu thuoc prefix namespace.
     */
    private OtpVerifyResult parseResponse(String body, Long requestId) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            // Chong XXE: tat DTD va external entity tu nguon khong tin cay.
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setExpandEntityReferences(false);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

            String envelopeStatus = textOf(doc, "Status");
            String globalErrorDesc = textOf(doc, "GlobalErrorDescription");
            String responseCode = textOf(doc, "ResponseCode");
            String message = textOf(doc, "Message");

            if (!"0".equals(trim(envelopeStatus))) {
                log.warn("[ESB-OTP] Truc tu choi requestId={} desc={}", requestId, globalErrorDesc);
                return OtpVerifyResult.failure(nonEmpty(globalErrorDesc, "Dich vu OTP tra ve loi"));
            }
            if ("0".equals(trim(responseCode))) {
                return OtpVerifyResult.success();
            }
            log.warn("[ESB-OTP] OTP khong hop le requestId={} ResponseCode={}", requestId, responseCode);
            return OtpVerifyResult.failure(nonEmpty(message, "Ma OTP khong dung"));
        } catch (Exception e) {
            log.error("[ESB-OTP] Loi parse response requestId={}: {}", requestId, e.getMessage());
            return OtpVerifyResult.failure("Phan hoi OTP khong hop le");
        }
    }

    /** Lay text cua phan tu dau tien co local name tuong ung, bo qua prefix. */
    private static String textOf(Document doc, String localName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        Node node = nodes.item(0);
        return node.getTextContent();
    }

    /** Ma hoa ASCII -> chuoi hexa thuong (giong dinh dang UserPassword tren bang tin). */
    private static String toHex(String value) {
        if (value == null) {
            return "";
        }
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    /** Escape ky tu dac biet XML. */
    private static String xml(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String nonEmpty(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
