package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** request_detail - dong chi tiet cho 01-YCTC, 04A-YCTK, 05A, 05B. */
@Entity
@Table(name = "request_detail")
@Getter
@Setter
public class RequestDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "system_id")
    private Long systemId;

    @Column(name = "database_id")
    private Long databaseId;

    @Column(name = "object_owner", length = 100)
    private String objectOwner;

    @Column(name = "object_name", length = 200)
    private String objectName;

    /** TABLE/INDEX/SYNONYM/OTHER. */
    @Column(name = "object_type", length = 30)
    private String objectType;

    /** Nguoi su dung tren dong chi tiet. */
    @Column(name = "target_user_id")
    private Long targetUserId;

    /** Chu tai khoan, dung cho 04A. */
    @Column(name = "account_owner_name", length = 200)
    private String accountOwnerName;

    /** Truy cap/Chinh sua. */
    @Column(name = "account_type", length = 30)
    private String accountType;

    /** Cap moi/Doi thuoc tinh. */
    @Column(name = "account_action", length = 30)
    private String accountAction;

    /** 04B-BGTK: pham vi ban giao (Toan bo/Theo he thong/Theo CSDL/Theo doi tuong). */
    @Column(name = "scope", length = 200)
    private String scope;

    /** Quyen truy cap dang ma hoac JSON. */
    @Column(name = "access_rights", length = 500)
    private String accessRights;

    @Column(name = "query_all")
    private boolean queryAll;

    @Column(length = 1000)
    private String purpose;

    /** JSON luu field dac thu tung mau. */
    @Lob
    @Column(name = "detail_data")
    private String detailData;
}
