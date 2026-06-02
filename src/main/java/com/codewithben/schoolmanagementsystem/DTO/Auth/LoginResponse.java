package com.codewithben.schoolmanagementsystem.DTO.Auth;

import java.util.List;

public class LoginResponse {
    private String staffId;

    private String staffName;

    private List<String> roles;

    private String institutionName;

    private String authToken;

    public LoginResponse(String staffId, String staffName, List<String> roles,
                         String institutionName, String authToken) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.roles = roles;
        this.institutionName = institutionName;
        this.authToken = authToken;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
