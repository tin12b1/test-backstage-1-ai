package com.csdl.access.domain;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** work_shift - danh muc ca lam viec. */
@Entity
@Table(name = "work_shift")
@Getter
@Setter
public class WorkShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_no", nullable = false)
    private Integer shiftNo;

    @Column(nullable = false, length = 100)
    private String name;

    /** Gio bat dau (0-23). */
    @Column(name = "start_hour", nullable = false)
    private Integer startHour;

    /** Gio ket thuc (0-24). */
    @Column(name = "end_hour", nullable = false)
    private Integer endHour;

    @Column(nullable = false)
    private boolean active = true;
}
