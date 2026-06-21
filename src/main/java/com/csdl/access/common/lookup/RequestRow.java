package com.csdl.access.common.lookup;

import com.csdl.access.common.enums.RequestStatus;

import java.time.LocalDateTime;

/** Dong hien thi tom tat phieu yeu cau cho danh sach/dashboard/tra cuu. */
public class RequestRow {

    private Long id;
    private String requestCode;
    private String requestType;
    private String requesterName;
    private String requesterUnit;
    private String systemName;
    private String databaseName;
    private RequestStatus status;
    private String statusLabel;
    private String currentActorRole;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getRequesterUnit() { return requesterUnit; }
    public void setRequesterUnit(String requesterUnit) { this.requesterUnit = requesterUnit; }
    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
    public String getCurrentActorRole() { return currentActorRole; }
    public void setCurrentActorRole(String currentActorRole) { this.currentActorRole = currentActorRole; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
