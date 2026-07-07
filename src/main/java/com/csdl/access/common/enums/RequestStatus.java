package com.csdl.access.common.enums;

/**
 * Trang thai phieu yeu cau (architecture.md muc 5).
 */
public enum RequestStatus {

    DRAFT("Luu nhap"),
    PENDING_SIGN("Cho ky xac nhan"),
    PENDING_RECEIPT("Cho nguoi nhan ky ban giao"),
    PENDING_APPROVAL("Cho phe duyet"),
    PENDING_CHECK("Cho bo phan kiem tra"),
    PENDING_DEPT_APPROVAL("Cho Truong phong/tuong duong"),
    PENDING_AUTHORITY_APPROVAL("Cho Nguoi co tham quyen"),
    PENDING_OWNER_UNIT("Cho don vi chu quan ung dung"),
    APPROVED("Da phe duyet"),
    SENT_TO_ACCESS_TEAM("Da chuyen bo phan Mo truy cap"),
    PENDING_DBA("Cho DBA/quan tri CSDL"),
    PENDING_EXECUTION("Cho nguoi thuc hien"),
    IN_PROGRESS("Dang thuc hien/mo truy cap"),
    COMPLETED("Hoan thanh"),
    RETURNED("Chuyen tra"),
    CANCELLED("Da huy"),
    SEND_FAILED("Gui loi, cho phep gui lai");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Trang thai con cho phep sua/huy (chua gui phe duyet). */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING_SIGN || this == RETURNED || this == SEND_FAILED;
    }

    /** Chi cho huy khi chua duoc phe duyet. */
    public boolean isCancellable() {
        return this == DRAFT || this == PENDING_SIGN || this == RETURNED || this == SEND_FAILED
                || this == PENDING_CHECK || this == PENDING_DEPT_APPROVAL
                || this == PENDING_AUTHORITY_APPROVAL || this == PENDING_OWNER_UNIT;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
