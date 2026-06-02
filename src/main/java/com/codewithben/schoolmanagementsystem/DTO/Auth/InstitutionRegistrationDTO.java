package com.codewithben.schoolmanagementsystem.DTO.Auth;

public class InstitutionRegistrationDTO {

    private String institutionName;

    private String subscriptionCode;

    public InstitutionRegistrationDTO(String institutionName, String subscriptionCode) {
        this.institutionName = institutionName;
        this.subscriptionCode = subscriptionCode;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getSubscriptionCode() {
        return subscriptionCode;
    }
}
