package com.csdl.access.auth;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Sinh ma captcha va ve thanh anh PNG cho man hinh dang nhap (features/login.md).
 *
 * <p>Tu sinh anh bang java.awt (chay headless), khong them dependency ngoai.</p>
 */
@Service
public class CaptchaService {

    /** Khoa luu ma captcha trong session. */
    public static final String SESSION_KEY = "LOGIN_CAPTCHA";

    private static final int LENGTH = 5;
    private static final int WIDTH = 140;
    private static final int HEIGHT = 46;
    // Bo ky tu tranh nham lan (khong co 0/O, 1/I/L).
    private static final char[] ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789".toCharArray();
    private static final Color[] TEXT_COLORS = {
            new Color(0x6e, 0x15, 0x25),
            new Color(0x8b, 0x1a, 0x2f),
            new Color(0x33, 0x33, 0x33),
            new Color(0x1f, 0x5a, 0x8a)
    };

    private final SecureRandom random = new SecureRandom();

    /** Sinh chuoi ma captcha ngau nhien. */
    public String generateCode() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }

    /** Ve ma captcha thanh anh PNG (mang byte). */
    public byte[] renderPng(String code) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Nen
            g.setColor(new Color(0xf7, 0xf1, 0xf2));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // Nhieu: cac duong cong
            for (int i = 0; i < 5; i++) {
                g.setColor(new Color(random.nextInt(160) + 60, random.nextInt(160) + 60, random.nextInt(160) + 60));
                int x1 = random.nextInt(WIDTH);
                int y1 = random.nextInt(HEIGHT);
                int x2 = random.nextInt(WIDTH);
                int y2 = random.nextInt(HEIGHT);
                g.drawLine(x1, y1, x2, y2);
            }
            // Nhieu: cac diem
            for (int i = 0; i < 60; i++) {
                g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                int x = random.nextInt(WIDTH);
                int y = random.nextInt(HEIGHT);
                g.fillRect(x, y, 1, 1);
            }

            // Ky tu, moi ky tu xoay nghieng ngau nhien
            int x = 12;
            for (int i = 0; i < code.length(); i++) {
                g.setColor(TEXT_COLORS[random.nextInt(TEXT_COLORS.length)]);
                int fontSize = 26 + random.nextInt(6);
                g.setFont(new Font("Arial", Font.BOLD, fontSize));
                double angle = (random.nextDouble() - 0.5) * 0.6; // ~ +-17 do
                int y = 32 + random.nextInt(4);
                g.rotate(angle, x, y);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
                g.rotate(-angle, x, y);
                x += 24;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } finally {
            g.dispose();
        }
    }

    /** So sanh ma nhap voi ma luu, khong phan biet hoa thuong. */
    public boolean matches(String expected, String input) {
        return expected != null && input != null && expected.equalsIgnoreCase(input.trim());
    }
}
