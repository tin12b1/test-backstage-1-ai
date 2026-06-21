package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** access_right_catalog - danh muc quyen truy cap/truy xuat. */
@Entity
@Table(name = "access_right_catalog")
@Getter
@Setter
public class AccessRightCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SELECT/INSERT/UPDATE/DELETE/QUERY_ALL/... */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}
