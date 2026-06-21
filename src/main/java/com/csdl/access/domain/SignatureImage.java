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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Lob
    @Column(name = "data")
    private byte[] data;
}
