package com.csdl.access.request;

/** Mot dong chi tiet trong form yeu cau (01-YCTC, 04A-YCTK, 05A, 05B). */
public class DetailForm {

    private Long systemId;
    private Long databaseId;
    private String objectOwner;
    private String objectName;
    private String objectType;
    private Long targetUserId;
    private String accountOwnerName;
    private String accountType;
    private String accountAction;
    private String accessRights;   // CSV cac ma quyen
    private boolean queryAll;
    private String purpose;

    public Long getSystemId() { return systemId; }
    public void setSystemId(Long systemId) { this.systemId = systemId; }
    public Long getDatabaseId() { return databaseId; }
    public void setDatabaseId(Long databaseId) { this.databaseId = databaseId; }
    public String getObjectOwner() { return objectOwner; }
    public void setObjectOwner(String objectOwner) { this.objectOwner = objectOwner; }
    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getAccountOwnerName() { return accountOwnerName; }
    public void setAccountOwnerName(String accountOwnerName) { this.accountOwnerName = accountOwnerName; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getAccountAction() { return accountAction; }
    public void setAccountAction(String accountAction) { this.accountAction = accountAction; }
    public String getAccessRights() { return accessRights; }
    public void setAccessRights(String accessRights) { this.accessRights = accessRights; }
    public boolean isQueryAll() { return queryAll; }
    public void setQueryAll(boolean queryAll) { this.queryAll = queryAll; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}
