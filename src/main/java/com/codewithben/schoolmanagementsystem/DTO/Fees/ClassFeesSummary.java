package com.codewithben.schoolmanagementsystem.DTO.Fees;

public class ClassFeesSummary {
    private String className;

    private String totalStudents;

    private String expectedFees;

    private String amountPaid;

    private String outStandingFees;

    public ClassFeesSummary(String className, String totalStudents, String expectedFees, String amountPaid, String outStandingFees) {
        this.className = className;
        this.totalStudents = totalStudents;
        this.expectedFees = expectedFees;
        this.amountPaid = amountPaid;
        this.outStandingFees = outStandingFees;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(String totalStudents) {
        this.totalStudents = totalStudents;
    }

    public String getExpectedFees() {
        return expectedFees;
    }

    public void setExpectedFees(String expectedFees) {
        this.expectedFees = expectedFees;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getOutStandingFees() {
        return outStandingFees;
    }

    public void setOutStandingFees(String outStandingFees) {
        this.outStandingFees = outStandingFees;
    }
}
