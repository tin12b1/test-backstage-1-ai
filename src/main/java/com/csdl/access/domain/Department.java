package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** department - danh muc phong/bo phan thuoc don vi. */
@Entity
@Table(name = "department")
@Getter
@Setter
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(nullable = false)
    private boolean active = true;
}
