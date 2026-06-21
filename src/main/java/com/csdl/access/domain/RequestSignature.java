package com.csdl.access.domain;

import com.csdl.access.common.enums.SigningScope;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** request_signature - thong tin ky xac nhan. */
@Entity
@Table(name = "request_signature")
@Getter
@Setter
public class RequestSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "detail_id")
    private Long detailId;

    @Column(name = "signer_user_id", nullable = false)
    private Long signerUserId;

    @Column(name = "signer_role_code", length = 50)
    private String signerRoleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "signing_scope", length = 20)
    private SigningScope signingScope;

    @Column(name = "otp_transaction_id")
    private Long otpTransactionId;

    @Column(name = "signed_at")
    private LocalDateTime signedAt = LocalDateTime.now();

    @Column(name = "signature_image_id")
    private Long signatureImageId;

    /** SUCCESS/FAILED. */
    @Column(length = 20)
    private String result;
}
