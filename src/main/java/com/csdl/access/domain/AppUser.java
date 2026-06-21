package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** app_user - nguoi dung dang ky tren he thong. */
@Entity
@Table(name = "app_user")
@Getter
@Setter
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(length = 30)
    private String mobile;

    @Column(length = 200)
    private String email;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "signature_image_id")
    private Long signatureImageId;

    @Column(length = 20)
    private String status = "ACTIVE";

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
