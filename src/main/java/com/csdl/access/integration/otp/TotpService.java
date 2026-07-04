package com.csdl.access.integration.otp;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * TOTP RFC 6238 (Google Authenticator): HMAC-SHA1, 6 chu so, chu ky 30s.
 * Tu cai dat bang JDK, khong them thu vien ngoai cho phan tinh toan ma.
 */
@Service
public class TotpService {

    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP = 30;
    private static final int DIGITS = 6;
    private static final int SECRET_BYTES = 20; // 160 bit
    /** So buoc thoi gian cho phep sai lech 2 ben (do lech dong ho). */
    private static final int WINDOW = 1;

    /** Nguon ngau nhien an toan de sinh bi mat. */
    private final SecureRandom random = new SecureRandom();

    /** Sinh bi mat moi dang Base32. */
    public String generateSecret() {
        byte[] buf = new byte[SECRET_BYTES];
        random.nextBytes(buf);
        return base32Encode(buf);
    }

    /** otpauth URI de tao QR cho Google Authenticator. */
    public String otpAuthUri(String issuer, String account, String secret) {
        String iss = urlEncode(issuer);
        String acc = urlEncode(account);
        return "otpauth://totp/" + iss + ":" + acc
                + "?secret=" + secret
                + "&issuer=" + iss
                + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + TIME_STEP;
    }

    /** Xac thuc ma 6 so (chap nhan +-1 buoc thoi gian). */
    public boolean verify(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }
        // Chuan hoa: bo khoang trang; chi chap nhan dung so chu so quy dinh.
        String normalized = code.trim().replaceAll("\\s", "");
        if (!normalized.matches("\\d{" + DIGITS + "}")) {
            return false;
        }
        byte[] key;
        try {
            // Giai ma bi mat Base32 thanh khoa HMAC.
            key = base32Decode(secret);
        } catch (RuntimeException e) {
            return false;
        }
        // Buoc thoi gian hien tai = so giay tu epoch chia cho chu ky 30s.
        long counter = Instant.now().getEpochSecond() / TIME_STEP;
        // Duyet cua so +-WINDOW de bu do lech dong ho giua server va thiet bi.
        for (int i = -WINDOW; i <= WINDOW; i++) {
            if (normalized.equals(generateCode(key, counter + i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tinh ma TOTP cho mot buoc thoi gian theo RFC 6238/4226.
     * Cac buoc: HMAC-SHA1(key, counter) -> dynamic truncation (lay 4 byte theo offset)
     * -> lay 31 bit duong -> modulo 10^6 -> bu 0 dau cho du DIGITS chu so.
     */
    private String generateCode(byte[] key, long counter) {
        try {
            // Bien counter (long) thanh 8 byte big-endian lam du lieu dau vao HMAC.
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            // Dynamic truncation: 4 bit cuoi cung cua hash lam offset bat dau.
            int offset = hash[hash.length - 1] & 0xF;
            // Ghep 4 byte tu offset thanh so 31 bit (mask 0x7f de bo dau).
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            // Lay DIGITS chu so cuoi va bu 0 dau neu thieu.
            int otp = binary % 1_000_000;
            return String.format("%0" + DIGITS + "d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("Loi tinh TOTP", e);
        }
    }

    /** Chia bi mat thanh nhom 4 ky tu cho de doc/nhap tay. */
    public String formatForDisplay(String secret) {
        if (secret == null) {
            return "";
        }
        return secret.replaceAll("(.{4})(?=.)", "$1 ");
    }

    /** Ve QR (PNG) cho noi dung otpauth bang zxing. */
    public byte[] qrPng(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Loi tao QR", e);
        }
    }

    // ===== Base32 (RFC 4648, khong padding) =====
    /** Ma hoa mang byte thanh chuoi Base32 (gom nhom 5 bit). */
    private static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                sb.append(BASE32.charAt((buffer >> bits) & 0x1F));
            }
        }
        if (bits > 0) {
            sb.append(BASE32.charAt((buffer << (5 - bits)) & 0x1F));
        }
        return sb.toString();
    }

    /** Giai ma chuoi Base32 (bo qua khoang trang/padding) thanh mang byte. */
    private static byte[] base32Decode(String s) {
        String clean = s.trim().replaceAll("[\\s=]", "").toUpperCase();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buffer = 0;
        int bits = 0;
        for (int i = 0; i < clean.length(); i++) {
            int val = BASE32.indexOf(clean.charAt(i));
            if (val < 0) {
                throw new IllegalArgumentException("Ky tu Base32 khong hop le");
            }
            buffer = (buffer << 5) | val;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                out.write((buffer >> bits) & 0xFF);
            }
        }
        return out.toByteArray();
    }

    /** URL-encode gia tri, thay '+' bang '%20' cho phu hop URI otpauth. */
    private static String urlEncode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }
}
