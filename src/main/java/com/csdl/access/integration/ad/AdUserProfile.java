package com.csdl.access.integration.ad;

/** Thong tin nguoi dung lay tu AD. */
public class AdUserProfile {

    private String username;
    private String fullName;
    private String email;
    private String mobile;
    private String unit;
    private String department;

    public AdUserProfile() {
    }

    public AdUserProfile(String username, String fullName, String email, String mobile) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
