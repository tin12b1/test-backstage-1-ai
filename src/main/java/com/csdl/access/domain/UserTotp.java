package com.csdl.access.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * user_totp - bi mat Google Authenticator (TOTP) cua nguoi dung.
 * Thay cho SoftOTP: ky xac nhan bang ma 6 so tu ung dung Google Authenticator.
 */
@Entity
@Table(name = "user_totp")
@Getter
@Setter
public class UserTotp {

    /** Khoa chinh, tu tang. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nguoi dung so huu bi mat TOTP (duy nhat moi user). */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** Bi mat dang Base32 (chia se voi ung dung Authenticator). */
    @Column(nullable = false, length = 64)
    private String secret;

    /** Da xac nhan (nhap dung ma lan dau) va dang hieu luc. */
    @Column(nullable = false)
    private boolean enabled = false;

    /** Thoi diem tao bi mat TOTP. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Thoi diem xac nhan kich hoat TOTP. */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /** Gan thoi diem tao mac dinh truoc khi luu ban ghi. */
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
