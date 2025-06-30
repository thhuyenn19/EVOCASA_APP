package com.thanhhuyen.models;

import java.util.List;

public class Account {
    private String _id;
    private String employeeid;
    private String fullName;
    private String email;
    private String image;
    private String password;
    private String role;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String lastLogin;
    private List<String> permissions;

    // Constructors
    public Account() {}

    public Account(String _id, String employeeid, String fullName, String email, String image, String password) {
        this._id = _id;
        this.employeeid = employeeid;
        this.fullName = fullName;
        this.email = email;
        this.image = image;
        this.password = password;
    }

    public Account(String employeeid, String password, String status) {
        this.employeeid = employeeid;
        this.password = password;
        this.status = status;
    }

    public Account(String _id, String employeeid, String fullName, String email, String image, String password, String role, String status, String createdAt, String updatedAt, String lastLogin, List<String> permissions) {
        this._id = _id;
        this.employeeid = employeeid;
        this.fullName = fullName;
        this.email = email;
        this.image = image;
        this.password = password;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
        this.permissions = permissions;
    }

    // Getters and Setters
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getEmployeeid() { return employeeid; }
    public void setEmployeeid(String employeeid) { this.employeeid = employeeid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}