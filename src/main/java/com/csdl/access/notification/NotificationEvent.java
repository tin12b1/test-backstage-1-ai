package com.csdl.access.notification;

/** Cac su kien nghiep vu can gui email (architecture.md muc 7). */
public enum NotificationEvent {
    /** Co yeu cau moi den buoc kiem tra/phe duyet. */
    NEW_REQUEST_PENDING("Co yeu cau moi cho kiem tra/phe duyet"),
    /** Yeu cau bi chuyen tra ve buoc truoc. */
    REQUEST_RETURNED("Yeu cau bi chuyen tra"),
    /** Lanh dao phong/bo phan da phe duyet. */
    DEPT_APPROVED("Yeu cau da duoc lanh dao phong/bo phan phe duyet"),
    /** Nguoi co tham quyen da phe duyet. */
    AUTHORITY_APPROVED("Yeu cau da duoc Nguoi co tham quyen phe duyet"),
    /** Da chuyen den bo phan Mo truy cap/DBA/Nguoi thuc hien. */
    SENT_TO_PROCESSING("Yeu cau da chuyen den bo phan Mo truy cap/DBA/Nguoi thuc hien"),
    /** Yeu cau da hoan thanh/xac nhan mo truy cap. */
    COMPLETED("Yeu cau da hoan thanh/xac nhan mo truy cap");

    /** Mo ta hien thi cua su kien (dung trong noi dung email). */
    private final String description;

    NotificationEvent(String description) {
        this.description = description;
    }

    /** Mo ta hien thi cua su kien. */
    public String getDescription() {
        return description;
    }
}
