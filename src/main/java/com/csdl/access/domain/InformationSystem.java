package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** information_system - danh muc he thong thong tin. */
@Entity
@Table(name = "information_system")
@Getter
@Setter
public class InformationSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    /** Don vi chu quan ung dung. */
    @Column(name = "owner_unit_id")
    private Long ownerUnitId;

    @Column(nullable = false)
    private boolean active = true;
}
