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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String username;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 300)
    private String message;

    @Column(name = "ip_address", length = 60)
    private String ipAddress;

    @Column(name = "logged_at")
    private LocalDateTime loggedAt = LocalDateTime.now();
}
