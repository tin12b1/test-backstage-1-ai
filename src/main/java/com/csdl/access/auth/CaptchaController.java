package com.csdl.access.auth;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Cung cap anh captcha cho man hinh dang nhap. Moi lan goi sinh ma moi va luu vao session.
 */
@RestController
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping("/captcha")
    public ResponseEntity<byte[]> captcha(HttpSession session) throws IOException {
        String code = captchaService.generateCode();
        session.setAttribute(CaptchaService.SESSION_KEY, code);
        byte[] png = captchaService.renderPng(code);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore().mustRevalidate())
                .body(png);
    }
}
