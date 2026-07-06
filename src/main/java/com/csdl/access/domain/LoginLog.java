package com.csdl.access.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** login_log - ghi log dang nhap. */
@Entity
@Table(name = "login_log")
@Getter
@Setter
public class LoginLog {

    /** Khoa chinh, tu tang. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ten dang nhap cua lan dang nhap. */
    @Column(length = 100)
    private String username;

    /** Ket qua dang nhap thanh cong hay that bai. */
    @Column(nullable = false)
    private boolean success;

    /** Thong bao ket qua/nguyen nhan that bai. */
    @Column(length = 300)
    private String message;

    /** Dia chi IP thuc hien dang nhap. */
    @Column(name = "ip_address", length = 60)
    private String ipAddress;

    /** Thoi diem ghi log dang nhap. */
    @Column(name = "logged_at")
    private LocalDateTime loggedAt = LocalDateTime.now();
}
