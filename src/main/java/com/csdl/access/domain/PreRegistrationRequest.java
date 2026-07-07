package com.csdl.access.domain;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** pre_registration_request - dang ky truoc yeu cau chi tiet (01-YCTC). */
@Entity
@Table(name = "pre_registration_request")
@Getter
@Setter
public class PreRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "unit_code", length = 50)
    private String unitCode;

    @Column(name = "register_date", nullable = false)
    private LocalDate registerDate;

    @Column(name = "shift", nullable = false)
    private Integer shift;

    @Column(name = "request_type", length = 20)
    private String requestType;

    @Column(name = "system_id")
    private Long systemId;

    @Column(name = "database_id")
    private Long databaseId;

    @Column(name = "object_name", length = 200)
    private String objectName;

    @Column(name = "access_rights", length = 200)
    private String accessRights;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "signature_image_id")
    private Long signatureImageId;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "UNUSED";

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
