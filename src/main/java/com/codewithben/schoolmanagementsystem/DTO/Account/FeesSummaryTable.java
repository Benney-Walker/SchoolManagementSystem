package com.codewithben.schoolmanagementsystem.DTO.Account;

public class FeesSummaryTable {
    private String studentId;

    private String studentName;

    private String gradeName;

    private String totalFees;

    private String amountPaid;

    private String outStandingBalance;

    public FeesSummaryTable(String studentId, String studentName, String gradeName, String totalFees, String amountPaid, String outStandingBalance) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.gradeName = gradeName;
        this.totalFees = totalFees;
        this.amountPaid = amountPaid;
        this.outStandingBalance = outStandingBalance;
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

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(String totalFees) {
        this.totalFees = totalFees;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getOutStandingBalance() {
        return outStandingBalance;
    }

    public void setOutStandingBalance(String outStandingBalance) {
        this.outStandingBalance = outStandingBalance;
    }
}
