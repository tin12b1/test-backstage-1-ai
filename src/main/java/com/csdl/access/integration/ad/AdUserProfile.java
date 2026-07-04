package com.csdl.access.integration.ad;

/** Thong tin nguoi dung lay tu AD. */
public class AdUserProfile {

    /** Ten dang nhap tren AD. */
    private String username;
    /** Ho ten day du. */
    private String fullName;
    /** Dia chi email. */
    private String email;
    /** So dien thoai di dong. */
    private String mobile;
    /** Don vi (chi nhanh/trung tam) neu AD co tra ve. */
    private String unit;
    /** Phong/ban neu AD co tra ve. */
    private String department;

    public AdUserProfile() {
    }

    /** Khoi tao voi cac thong tin co ban thuong dung nhat. */
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
