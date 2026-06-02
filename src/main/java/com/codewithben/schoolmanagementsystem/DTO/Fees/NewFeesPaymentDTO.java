package com.codewithben.schoolmanagementsystem.DTO.Fees;

public class NewFeesPaymentDTO {
    private String studentId;

    private Double amountPaid;

    private String payerName;

    private String payerPhone;

    private String levelId;

    private String semesterId;

    public NewFeesPaymentDTO(String studentId, Double amountPaid, String payerName, String payerPhone, String levelId,
                             String semesterId) {
        this.studentId = studentId;
        this.amountPaid = amountPaid;
        this.payerName = payerName;
        this.payerPhone = payerPhone;
        this.levelId = levelId;
        this.semesterId = semesterId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerPhone() {
        return payerPhone;
    }

    public void setPayerPhone(String payerPhone) {
        this.payerPhone = payerPhone;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(String semesterId) {
        this.semesterId = semesterId;
    }
}
