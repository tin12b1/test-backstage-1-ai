package com.csdl.access.integration.ad;

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
 * AD client goi dich vu SOAP "VerifyUserAD" tren truc tich hop (ESB/SOA) cua Agribank.
 *
 * <p>Kich hoat khi {@code integration.ad.mode=esb}. Toan bo thong tin ket noi va credential
 * cap ung dung lay tu cau hinh, khong hard-code (features/integrations.md muc 8).</p>
 *
 * <p>Khong dung dependency SOAP rieng: build XML request bang chuoi va parse response bang JAXP
 * (da co san trong JDK) de giu so phu thuoc toi thieu, dong nhat voi {@link LdapAdClient}.</p>
 *
 * <p>Luu y nghiep vu:</p>
 * <ul>
 *   <li>Phai kiem tra ca hai muc: {@code ResponseStatus/Status} (muc truc) va
 *       {@code BodyRes/Result} (muc nghiep vu). Ca hai bang 0 moi coi la thanh cong.</li>
 *   <li>{@code UserName} can co domain prefix (vi du {@code CORP\\username}).</li>
 *   <li>{@code UserDetail/UserPassword} la chuoi ASCII ma hexa cua mat khau he thong;
 *       cau hinh nhan mat khau dang ro, lop nay tu ma hoa hexa khi dung bang tin.</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "integration.ad.mode", havingValue = "esb")
public class EsbAdClient implements AdClient {

    private static final Logger log = LoggerFactory.getLogger(EsbAdClient.class);

    private static final String NS_HEADER = "http://www.agribank.com.vn/common/envelope/commonheader/1";
    private static final String NS_AUTHEN = "http://www.agribank.com.vn/entity/vn/authen/authensvcs/1";

    @Value("${integration.ad.esb.endpoint}")
    private String endpoint;

    @Value("${integration.ad.esb.source-app-id:EBANK}")
    private String sourceAppId;

    @Value("${integration.ad.esb.service-user-id:EBANK}")
    private String serviceUserId;

    /** Mat khau he thong dang ro (vi du EBANKING@2020); se duoc ma hoa hexa khi gui. */
    @Value("${integration.ad.esb.service-password:}")
    private String servicePassword;

    @Value("${integration.ad.esb.function-code:AUTH-AUTHENADUSER-LDAP-AD}")
    private String functionCode;

    @Value("${integration.ad.esb.service-version:1}")
    private String serviceVersion;

    /** Domain AD ghep vao truoc username neu username chua co. De trong neu khong can. */
    @Value("${integration.ad.esb.domain:}")
    private String domain;

    /** SOAPAction header (tuy chon, mot so truc yeu cau). */
    @Value("${integration.ad.esb.soap-action:}")
    private String soapAction;

    @Value("${integration.ad.esb.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${integration.ad.esb.read-timeout-ms:10000}")
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
    public AdAuthResult authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return AdAuthResult.of(AdAuthResult.Status.BAD_CREDENTIALS, "Thieu thong tin dang nhap");
        }

        String principal = buildPrincipal(username);
        String requestXml = buildRequest(principal, password);

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
                log.error("[ESB-AD] HTTP loi status={} cho user={}", response.getStatusCode(), username);
                return AdAuthResult.of(AdAuthResult.Status.CONNECTION_ERROR, "Khong ket noi duoc dich vu xac thuc");
            }
            return parseResponse(response.getBody(), username);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Timeout / khong ket noi duoc. Khong log password.
            log.error("[ESB-AD] Loi ket noi truc cho user={}: {}", username, e.getMessage());
            return AdAuthResult.of(AdAuthResult.Status.CONNECTION_ERROR, "Khong ket noi duoc dich vu xac thuc");
        } catch (Exception e) {
            log.error("[ESB-AD] Loi goi dich vu VerifyUserAD cho user={}: {}", username, e.getMessage());
            return AdAuthResult.of(AdAuthResult.Status.CONNECTION_ERROR, "Loi he thong xac thuc");
        }
    }

    @Override
    public AdUserProfile getUserProfile(String username) {
        // Dich vu VerifyUserAD chi xac thuc, khong tra ho so. Neu truc co service rieng
        // (vi du lay thong tin can bo) thi bo sung sau. Tam tra ho so toi thieu.
        AdUserProfile profile = new AdUserProfile();
        profile.setUsername(username);
        return profile;
    }

    private String buildPrincipal(String username) {
        if (domain == null || domain.isBlank()) {
            return username;
        }
        if (username.contains("\\") || username.contains("@")) {
            return username; // da co domain
        }
        return domain + "\\" + username;
    }

    private String buildRequest(String principal, String password) {
        String messageId = UUID.randomUUID().toString();
        String transactionId = String.valueOf(System.currentTimeMillis());
        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String passwordHex = toHex(servicePassword);

        StringBuilder sb = new StringBuilder();
        sb.append("<ns3:VerifyUserADReq xmlns:ns2=\"").append(NS_HEADER)
                .append("\" xmlns:ns3=\"").append(NS_AUTHEN).append("\">");
        sb.append("<ns2:Header>");
        sb.append("<ns2:Common>");
        sb.append("<ns2:ServiceVersion>").append(xml(serviceVersion)).append("</ns2:ServiceVersion>");
        sb.append("<ns2:MessageId>").append(messageId).append("</ns2:MessageId>");
        sb.append("<ns2:TransactionId>").append(transactionId).append("</ns2:TransactionId>");
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
        sb.append("<UserName>").append(xml(principal)).append("</UserName>");
        sb.append("<Password>").append(xml(password)).append("</Password>");
        sb.append("</BodyReq>");
        sb.append("</ns3:VerifyUserADReq>");
        return sb.toString();
    }

    /**
     * Parse response, kiem tra ResponseStatus/Status (truc) va BodyRes/Result (nghiep vu).
     * Tim phan tu theo local name de khong phu thuoc prefix namespace.
     */
    private AdAuthResult parseResponse(String body, String username) {
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
            String globalErrorCode = textOf(doc, "GlobalErrorCode");
            String globalErrorDesc = textOf(doc, "GlobalErrorDescription");
            String result = textOf(doc, "Result");
            String description = textOf(doc, "Description");

            boolean envelopeOk = "0".equals(trim(envelopeStatus));
            if (!envelopeOk) {
                log.warn("[ESB-AD] Truc tu choi user={} GlobalErrorCode={} desc={}",
                        username, globalErrorCode, globalErrorDesc);
                return AdAuthResult.of(AdAuthResult.Status.CONNECTION_ERROR,
                        nonEmpty(globalErrorDesc, "Dich vu xac thuc tra ve loi"));
            }

            boolean businessOk = "0".equals(trim(result));
            if (businessOk) {
                return AdAuthResult.success();
            }
            return mapBusinessError(result, description, username);
        } catch (Exception e) {
            log.error("[ESB-AD] Loi parse response cho user={}: {}", username, e.getMessage());
            return AdAuthResult.of(AdAuthResult.Status.CONNECTION_ERROR, "Phan hoi xac thuc khong hop le");
        }
    }

    /**
     * Map ket qua nghiep vu khi Result != 0. Chua co bo ma loi day du tu truc nen mac dinh
     * coi la sai thong tin dang nhap; bo sung cac ma cu the khi co tai lieu chinh thuc.
     */
    private AdAuthResult mapBusinessError(String result, String description, String username) {
        String desc = description == null ? "" : description.toLowerCase();
        // Dung cum tu cu the de tranh nham (vi du "khoan" trong "tai khoan" chua "khoa").
        if (desc.contains("lock") || desc.contains("kh\u00f3a") || desc.contains("kho\u00e1")
                || desc.contains("bi khoa")) {
            return AdAuthResult.of(AdAuthResult.Status.USER_LOCKED,
                    nonEmpty(description, "Tai khoan da bi khoa tren AD"));
        }
        if (desc.contains("not found") || desc.contains("kh\u00f4ng t\u1ed3n t\u1ea1i")
                || desc.contains("khong ton tai")) {
            return AdAuthResult.of(AdAuthResult.Status.USER_NOT_FOUND,
                    nonEmpty(description, "Tai khoan khong ton tai tren AD"));
        }
        log.warn("[ESB-AD] Xac thuc that bai user={} Result={} desc={}", username, result, description);
        return AdAuthResult.of(AdAuthResult.Status.BAD_CREDENTIALS,
                nonEmpty(description, "Sai tai khoan hoac mat khau"));
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
