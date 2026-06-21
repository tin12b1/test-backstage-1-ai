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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String username;

    @Column(length = 60)
    private String purpose;

    @Column(name = "request_id")
    private Long requestId;

    /** SUCCESS/FAILED. */
    @Column(length = 20)
    private String result;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt = LocalDateTime.now();
}
