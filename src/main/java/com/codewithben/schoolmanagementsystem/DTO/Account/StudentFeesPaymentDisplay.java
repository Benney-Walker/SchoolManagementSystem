package com.codewithben.schoolmanagementsystem.DTO.Account;

import java.util.List;

public class StudentFeesPaymentDisplay {

    private String studentId;

    private String studentName;

    private String semesterName;

    private String levelName;

    private String totalFeesAmount;

    private String totalAmountPaid;

    private String feesBalance;

    private List<PaymentHistory> paymentHistory;

    public StudentFeesPaymentDisplay(String studentId, String studentName, String semesterName, String levelName, String totalFeesAmount,
                                     String totalAmountPaid, String feesBalance, List<PaymentHistory> paymentHistory) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.semesterName = semesterName;
        this.levelName = levelName;
        this.totalFeesAmount = totalFeesAmount;
        this.totalAmountPaid = totalAmountPaid;
        this.feesBalance = feesBalance;
        this.paymentHistory = paymentHistory;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getTotalFeesAmount() {
        return totalFeesAmount;
    }

    public void setTotalFeesAmount(String totalFeesAmount) {
        this.totalFeesAmount = totalFeesAmount;
    }

    public String getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(String totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }

    public String getFeesBalance() {
        return feesBalance;
    }

    public void setFeesBalance(String feesBalance) {
        this.feesBalance = feesBalance;
    }

    public List<PaymentHistory> getPaymentHistory() {
        return paymentHistory;
    }

    public void setPaymentHistory(List<PaymentHistory> paymentHistory) {
        this.paymentHistory = paymentHistory;
    }
}
