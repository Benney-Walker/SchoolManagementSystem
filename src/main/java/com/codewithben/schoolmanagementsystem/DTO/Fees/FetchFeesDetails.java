package com.codewithben.schoolmanagementsystem.DTO.Fees;

public class FetchFeesDetails {
    private String amount;

    private String semesterId;

    private String levelId;

    public FetchFeesDetails(String amount, String semesterId, String levelId) {
        this.amount = amount;
        this.semesterId = semesterId;
        this.levelId = levelId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }
}
