package com.csdl.access.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Du lieu form dang ky truoc yeu cau chi tiet (01-YCTC). */
public class PreRegistrationForm {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate registerDate;
    private Integer shift;
    private String requestType;
    private Long systemId;
    private Long databaseId;
    private String objectName;
    private String accessRights;

    public LocalDate getRegisterDate() { return registerDate; }
    public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }
    public Integer getShift() { return shift; }
    public void setShift(Integer shift) { this.shift = shift; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public Long getSystemId() { return systemId; }
    public void setSystemId(Long systemId) { this.systemId = systemId; }
    public Long getDatabaseId() { return databaseId; }
    public void setDatabaseId(Long databaseId) { this.databaseId = databaseId; }
    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }
    public String getAccessRights() { return accessRights; }
    public void setAccessRights(String accessRights) { this.accessRights = accessRights; }
}
