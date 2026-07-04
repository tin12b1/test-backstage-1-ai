package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Anh chu ky khai bao tren he thong (app_user.signature_image_id). */
@Entity
@Table(name = "signature_image")
@Getter
@Setter
public class SignatureImage {

    /** Khoa chinh, tu tang. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nguoi dung so huu anh chu ky. */
    @Column(name = "user_id")
    private Long userId;

    /** Kieu noi dung (MIME) cua anh chu ky. */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /** Du lieu nhi phan cua anh chu ky. */
    @Lob
    @Column(name = "data")
    private byte[] data;
}
