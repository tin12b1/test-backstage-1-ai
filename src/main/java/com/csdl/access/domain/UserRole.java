package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** user_role - gan nhieu vai tro cho mot user, co the gioi han pham vi. */
@Entity
@Table(name = "user_role")
@Getter
@Setter
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /** Pham vi don vi neu co. */
    @Column(name = "unit_id")
    private Long unitId;

    /** Pham vi he thong neu co. */
    @Column(name = "system_id")
    private Long systemId;

    /** Pham vi CSDL neu co. */
    @Column(name = "database_id")
    private Long databaseId;

    @Column(nullable = false)
    private boolean active = true;
}
