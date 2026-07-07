package com.csdl.access.dashboard;

import com.csdl.access.common.lookup.RequestRow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Du lieu hien thi dashboard theo vai tro (features/dashboard.md). */
public class DashboardView {

    private String groupTitle;
    private final Map<String, Long> counters = new LinkedHashMap<>();
    private String primaryTitle;
    private List<RequestRow> primaryList = new ArrayList<>();
    private String secondaryTitle;
    private List<RequestRow> secondaryList = new ArrayList<>();
    private long sharedPendingSignCount;

    public String getGroupTitle() { return groupTitle; }
    public void setGroupTitle(String groupTitle) { this.groupTitle = groupTitle; }
    public Map<String, Long> getCounters() { return counters; }
    public String getPrimaryTitle() { return primaryTitle; }
    public void setPrimaryTitle(String primaryTitle) { this.primaryTitle = primaryTitle; }
    public List<RequestRow> getPrimaryList() { return primaryList; }
    public void setPrimaryList(List<RequestRow> primaryList) { this.primaryList = primaryList; }
    public String getSecondaryTitle() { return secondaryTitle; }
    public void setSecondaryTitle(String secondaryTitle) { this.secondaryTitle = secondaryTitle; }
    public List<RequestRow> getSecondaryList() { return secondaryList; }
    public void setSecondaryList(List<RequestRow> secondaryList) { this.secondaryList = secondaryList; }
    public long getSharedPendingSignCount() { return sharedPendingSignCount; }
    public void setSharedPendingSignCount(long sharedPendingSignCount) { this.sharedPendingSignCount = sharedPendingSignCount; }
}
