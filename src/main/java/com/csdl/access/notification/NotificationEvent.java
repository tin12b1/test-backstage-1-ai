package com.csdl.access.notification;

/** Cac su kien nghiep vu can gui email (architecture.md muc 7). */
public enum NotificationEvent {
    NEW_REQUEST_PENDING("Co yeu cau moi cho kiem tra/phe duyet"),
    REQUEST_RETURNED("Yeu cau bi chuyen tra"),
    DEPT_APPROVED("Yeu cau da duoc lanh dao phong/bo phan phe duyet"),
    AUTHORITY_APPROVED("Yeu cau da duoc Nguoi co tham quyen phe duyet"),
    SENT_TO_PROCESSING("Yeu cau da chuyen den bo phan Mo truy cap/DBA/Nguoi thuc hien"),
    COMPLETED("Yeu cau da hoan thanh/xac nhan mo truy cap");

    private final String description;

    NotificationEvent(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
