package com.codewithben.schoolmanagementsystem.DTO.Fees;

public class RecentPaymentTable {
    private String paymentDate;

    private String studentId;

    private String studentNameCol;

    private String amountCol;

    private String payerCol;

    private String levelCol;

    public RecentPaymentTable(String paymentDate, String studentId, String studentNameCol, String amountCol, String payerCol, String levelCol) {
        this.paymentDate = paymentDate;
        this.studentId = studentId;
        this.studentNameCol = studentNameCol;
        this.amountCol = amountCol;
        this.payerCol = payerCol;
        this.levelCol = levelCol;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentNameCol() {
        return studentNameCol;
    }

    public void setStudentNameCol(String studentNameCol) {
        this.studentNameCol = studentNameCol;
    }

    public String getAmountCol() {
        return amountCol;
    }

    public void setAmountCol(String amountCol) {
        this.amountCol = amountCol;
    }

    public String getPayerCol() {
        return payerCol;
    }

    public void setPayerCol(String payerCol) {
        this.payerCol = payerCol;
    }

    public String getLevelCol() {
        return levelCol;
    }

    public void setLevelCol(String levelCol) {
        this.levelCol = levelCol;
    }
}
