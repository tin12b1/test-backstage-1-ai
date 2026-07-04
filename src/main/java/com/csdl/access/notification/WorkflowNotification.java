package com.csdl.access.notification;

/**
 * Thong tin thong bao nghiep vu gui qua email (features/integrations.md muc 5).
 */
public class WorkflowNotification {

    private String toAddress;    // Email nguoi nhan
    private Long requestId;      // ID phieu yeu cau
    private String requestCode;  // Ma phieu yeu cau
    private String requestType;  // Ten loai yeu cau (hien thi)
    private String status;       // Ten trang thai phieu (hien thi)
    private String eventType;    // Ma su kien (NotificationEvent.name())
    private String eventLabel;   // Mo ta su kien (hien thi)
    private String fromUser;     // Nguoi gay ra su kien
    private String targetActor;  // Vai tro nguoi nhan (hien thi)
    private String link;         // Duong dan mo chi tiet phieu

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public void setEventLabel(String eventLabel) {
        this.eventLabel = eventLabel;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getTargetActor() {
        return targetActor;
    }

    public void setTargetActor(String targetActor) {
        this.targetActor = targetActor;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
