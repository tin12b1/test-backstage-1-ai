package com.csdl.access.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** otp_transaction - luu giao dich xac thuc OTP/SoftOTP. Khong luu OTP dang ro. */
@Entity
@Table(name = "otp_transaction")
@Getter
@Setter
public class OtpTransaction {

    /** Khoa chinh, tu tang. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ten dang nhap thuc hien xac thuc OTP. */
    @Column(length = 100)
    private String username;

    /** Muc dich xac thuc OTP. */
    @Column(length = 60)
    private String purpose;

    /** Phieu yeu cau lien quan (neu co). */
    @Column(name = "request_id")
    private Long requestId;

    /** SUCCESS/FAILED. */
    @Column(length = 20)
    private String result;

    /** Thoi diem xac thuc OTP. */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt = LocalDateTime.now();
}
