package com.csdl.access.request;

import java.util.ArrayList;
import java.util.List;

/** Du lieu form lap/sua yeu cau (chung cho cac mau phieu). */
public class RequestForm {

    private String requestType;
    private Integer shiftNo;
    private Integer accessNo;
    private Long systemId;
    private Long databaseId;
    private String startTime;            // ISO-8601 local datetime
    private String endTime;
    private String expectedExecutionDate; // 03-YCCT
    private String reason;
    private Long emergencyRequestId;      // 05B: lien ket toi 05A

    private List<DetailForm> details = new ArrayList<>();

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public Integer getShiftNo() { return shiftNo; }
    public void setShiftNo(Integer shiftNo) { this.shiftNo = shiftNo; }
    public Integer getAccessNo() { return accessNo; }
    public void setAccessNo(Integer accessNo) { this.accessNo = accessNo; }
    public Long getSystemId() { return systemId; }
    public void setSystemId(Long systemId) { this.systemId = systemId; }
    public Long getDatabaseId() { return databaseId; }
    public void setDatabaseId(Long databaseId) { this.databaseId = databaseId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getExpectedExecutionDate() { return expectedExecutionDate; }
    public void setExpectedExecutionDate(String expectedExecutionDate) { this.expectedExecutionDate = expectedExecutionDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getEmergencyRequestId() { return emergencyRequestId; }
    public void setEmergencyRequestId(Long emergencyRequestId) { this.emergencyRequestId = emergencyRequestId; }
    public List<DetailForm> getDetails() { return details; }
    public void setDetails(List<DetailForm> details) { this.details = details; }
}
