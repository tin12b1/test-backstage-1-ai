package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** database_catalog - danh muc CSDL. */
@Entity
@Table(name = "database_catalog")
@Getter
@Setter
public class DatabaseCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "system_id", nullable = false)
    private Long systemId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    /** Don vi chu quan CSDL. */
    @Column(name = "owner_unit_id")
    private Long ownerUnitId;

    @Column(nullable = false)
    private boolean active = true;
}
