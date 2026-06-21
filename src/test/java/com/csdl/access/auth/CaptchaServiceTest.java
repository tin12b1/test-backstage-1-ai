package com.csdl.access.auth;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/** Test sinh ma va ve anh captcha (features/login.md). */
class CaptchaServiceTest {

    private final CaptchaService service = new CaptchaService();

    @Test
    void generateCode_hasExpectedLengthAndSafeAlphabet() {
        String code = service.generateCode();
        assertThat(code).hasSize(5);
        // Khong chua ky tu de nham lan: 0, O, 1, I, L.
        assertThat(code).doesNotContainPattern("[01OIL]");
    }

    @Test
    void matches_isCaseInsensitiveAndTrimmed() {
        assertThat(service.matches("ABCDE", "abcde")).isTrue();
        assertThat(service.matches("ABCDE", "  AbCdE  ")).isTrue();
        assertThat(service.matches("ABCDE", "ABCDX")).isFalse();
        assertThat(service.matches("ABCDE", null)).isFalse();
        assertThat(service.matches(null, "ABCDE")).isFalse();
    }

    @Test
    void renderPng_returnsValidPngImage() throws IOException {
        byte[] png = service.renderPng("ABCDE");
        assertThat(png).isNotEmpty();
        // Chu ky PNG: 89 50 4E 47
        assertThat(png[0] & 0xFF).isEqualTo(0x89);
        assertThat(png[1] & 0xFF).isEqualTo(0x50);
        assertThat(png[2] & 0xFF).isEqualTo(0x4E);
        assertThat(png[3] & 0xFF).isEqualTo(0x47);
    }
}
