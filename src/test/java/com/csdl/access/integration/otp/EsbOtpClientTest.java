package com.csdl.access.integration.otp;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test phan parse response va dung request cua {@link EsbOtpClient} bang cach mock RestTemplate,
 * khong goi truc that.
 */
class EsbOtpClientTest {

    private EsbOtpClient client;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        client = new EsbOtpClient();
        restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(client, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(client, "endpoint", "http://esb.local/VerifyOTP");
        ReflectionTestUtils.setField(client, "sourceAppId", "EBANK");
        ReflectionTestUtils.setField(client, "serviceUserId", "EBANK");
        ReflectionTestUtils.setField(client, "servicePassword", "EBANKING@2020");
        ReflectionTestUtils.setField(client, "functionCode", "AUTH-VERIFYOTP-WS-OTP");
        ReflectionTestUtils.setField(client, "serviceVersion", "1");
        ReflectionTestUtils.setField(client, "transType", "5");
        ReflectionTestUtils.setField(client, "deviceTypeId", "1");
        ReflectionTestUtils.setField(client, "verifyOtpType", "26");
        ReflectionTestUtils.setField(client, "soapAction", "");
    }

    private void stubResponse(String body) {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    private String response(String envelopeStatus, String responseCode, String message) {
        return "<out2:VerifyOTPRes"
                + " xmlns:io2=\"http://www.agribank.com.vn/common/envelope/commonheader/1\""
                + " xmlns:out2=\"http://www.agribank.com.vn/entity/vn/authen/authensvcs/1\">"
                + "<io2:ResponseStatus>"
                + "<io2:Status>" + envelopeStatus + "</io2:Status>"
                + "<io2:GlobalErrorCode>000</io2:GlobalErrorCode>"
                + "<io2:GlobalErrorDescription>desc</io2:GlobalErrorDescription>"
                + "</io2:ResponseStatus>"
                + "<BodyRes>"
                + "<ResponseCode>" + responseCode + "</ResponseCode>"
                + "<Message>" + message + "</Message>"
                + "</BodyRes>"
                + "</out2:VerifyOTPRes>";
    }

    @Test
    void verify_success_whenStatusAndResponseCodeAreZero() {
        stubResponse(response("0", "0", "Success"));

        OtpVerifyResult result = client.verify("user1", "994214", "SIGN", 100L);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void verify_failure_whenResponseCodeNonZero() {
        stubResponse(response("0", "1", "OTP khong dung"));

        OtpVerifyResult result = client.verify("user1", "000000", "SIGN", 100L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("OTP khong dung");
    }

    @Test
    void verify_failure_whenEnvelopeStatusNonZero() {
        stubResponse(response("1", "0", "irrelevant"));

        OtpVerifyResult result = client.verify("user1", "994214", "SIGN", 100L);

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void verify_failure_whenOtpMissing() {
        // Khong goi HTTP khi thieu OTP.
        OtpVerifyResult result = client.verify("user1", "", "SIGN", 100L);

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void verify_failure_whenResourceAccessException() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        OtpVerifyResult result = client.verify("user1", "994214", "SIGN", 100L);

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void request_includesOtpFunctionCodeAndHexEncodedSystemPassword() {
        stubResponse(response("0", "0", "Success"));

        client.verify("user1", "994214", "SIGN", 100L);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));
        String body = String.valueOf(captor.getValue().getBody());

        assertThat(body).contains("<FunctionCode>AUTH-VERIFYOTP-WS-OTP</FunctionCode>");
        assertThat(body).contains("<OTP>994214</OTP>");
        assertThat(body).contains("<TransType>5</TransType>");
        assertThat(body).contains("<VerifyOTPType>26</VerifyOTPType>");
        assertThat(body).contains("<ns2:UserPassword>4542414e4b494e474032303230</ns2:UserPassword>");
    }
}
