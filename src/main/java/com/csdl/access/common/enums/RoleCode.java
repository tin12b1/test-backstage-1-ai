package com.csdl.access.common.enums;

/**
 * Ma vai tro (database-schema.md, role code de xuat).
 */
public enum RoleCode {

    REQUESTER("Nguoi lap yeu cau"),
    DEPT_MANAGER("Truong phong hoac tuong duong"),
    AUTHORITY("Nguoi co tham quyen"),
    CHECKER("Bo phan kiem tra"),
    ACCESS_TEAM("Bo phan mo truy cap"),
    DBA("Quan tri CSDL/DBA"),
    EXECUTOR("Nguoi thuc hien"),
    ADMIN("Quan tri he thong");

    private final String displayName;

    RoleCode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Spring Security authority dang ROLE_*. */
    public String authority() {
        return "ROLE_" + name();
    }
}
