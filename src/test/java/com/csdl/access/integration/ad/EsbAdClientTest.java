package com.csdl.access.integration.ad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test phan parse response va map ket qua cua {@link EsbAdClient} bang cach mock RestTemplate,
 * khong goi truc that.
 */
class EsbAdClientTest {

    private EsbAdClient client;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        client = new EsbAdClient();
        restTemplate = mock(RestTemplate.class);
        // Inject cau hinh va RestTemplate gia.
        ReflectionTestUtils.setField(client, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(client, "endpoint", "http://esb.local/VerifyUserAD");
        ReflectionTestUtils.setField(client, "sourceAppId", "EBANK");
        ReflectionTestUtils.setField(client, "serviceUserId", "EBANK");
        ReflectionTestUtils.setField(client, "servicePassword", "EBANKING@2020");
        ReflectionTestUtils.setField(client, "functionCode", "AUTH-AUTHENADUSER-LDAP-AD");
        ReflectionTestUtils.setField(client, "serviceVersion", "1");
        ReflectionTestUtils.setField(client, "domain", "CORP");
        ReflectionTestUtils.setField(client, "soapAction", "");
    }

    private void stubResponse(String body) {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    private String response(String envelopeStatus, String globalErrorCode,
                            String result, String description) {
        return "<io3:VerifyUserADRes"
                + " xmlns:io2=\"http://www.agribank.com.vn/common/envelope/commonheader/1\""
                + " xmlns:io3=\"http://www.agribank.com.vn/entity/vn/authen/authensvcs/1\">"
                + "<io2:ResponseStatus>"
                + "<io2:Status>" + envelopeStatus + "</io2:Status>"
                + "<io2:GlobalErrorCode>" + globalErrorCode + "</io2:GlobalErrorCode>"
                + "<io2:GlobalErrorDescription>desc</io2:GlobalErrorDescription>"
                + "</io2:ResponseStatus>"
                + "<BodyRes>"
                + "<Result>" + result + "</Result>"
                + "<Description>" + description + "</Description>"
                + "</BodyRes>"
                + "</io3:VerifyUserADRes>";
    }

    @Test
    void authenticate_success_whenStatusAndResultAreZero() {
        stubResponse(response("0", "000", "0", "Xac thuc thanh cong"));

        AdAuthResult result = client.authenticate("TRUNGLENGUYENTHANH", "secret");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(AdAuthResult.Status.SUCCESS);
    }

    @Test
    void authenticate_badCredentials_whenBusinessResultNonZero() {
        stubResponse(response("0", "000", "1", "Sai tai khoan hoac mat khau"));

        AdAuthResult result = client.authenticate("user1", "wrong");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatus()).isEqualTo(AdAuthResult.Status.BAD_CREDENTIALS);
    }

    @Test
    void authenticate_userLocked_whenDescriptionMentionsLock() {
        stubResponse(response("0", "000", "2", "Account is locked"));

        AdAuthResult result = client.authenticate("user1", "secret");

        assertThat(result.getStatus()).isEqualTo(AdAuthResult.Status.USER_LOCKED);
    }

    @Test
    void authenticate_userNotFound_whenDescriptionMentionsNotFound() {
        stubResponse(response("0", "000", "3", "User not found"));

        AdAuthResult result = client.authenticate("user1", "secret");

        assertThat(result.getStatus()).isEqualTo(AdAuthResult.Status.USER_NOT_FOUND);
    }

    @Test
    void authenticate_connectionError_whenEnvelopeStatusNonZero() {
        stubResponse(response("1", "999", "0", "irrelevant"));

        AdAuthResult result = client.authenticate("user1", "secret");

        assertThat(result.getStatus()).isEqualTo(AdAuthResult.Status.CONNECTION_ERROR);
    }

    @Test
    void authenticate_badCredentials_whenMissingPassword() {
        // Khong goi HTTP khi thieu thong tin dang nhap.
        AdAuthResult result = client.authenticate("user1", "");

        assertThat(result.getStatus()).isEqualTo(AdAuthResult.Status.BAD_CREDENTIALS);
    }

    @Test
    void authenticate_connectionError_whenResourceAccessException() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        AdAuthResult result = client.authenticate("user1", "secret");

        assertThat(result.getStatus()).isEqualTo(AdAuthResult.Status.CONNECTION_ERROR);
    }

    @Test
    void request_includesDomainPrefixAndHexEncodedSystemPassword() {
        stubResponse(response("0", "000", "0", "OK"));

        client.authenticate("TRUNGLENGUYENTHANH", "secret");

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        org.mockito.Mockito.verify(restTemplate)
                .postForEntity(anyString(), captor.capture(), eq(String.class));
        String body = String.valueOf(captor.getValue().getBody());

        // Domain ghep vao username.
        assertThat(body).contains("<UserName>CORP\\TRUNGLENGUYENTHANH</UserName>");
        // EBANKING@2020 -> hex.
        assertThat(body).contains("<ns2:UserPassword>4542414e4b494e474032303230</ns2:UserPassword>");
        assertThat(body).contains("<FunctionCode>AUTH-AUTHENADUSER-LDAP-AD</FunctionCode>");
    }
}
