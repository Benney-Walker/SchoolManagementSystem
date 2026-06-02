package com.codewithben.schoolmanagementsystem.DTO.Report;

public class GradeFeesReport {
    private String studentId;

    private String studentName;

    private String totalAmountToPay;

    private String amountPayed;

    private String outstanding;

    public GradeFeesReport() {}

    public GradeFeesReport(String studentId, String studentName,
                           String totalAmountToPay, String amountPayed, String outstanding) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalAmountToPay = totalAmountToPay;
        this.amountPayed = amountPayed;
        this.outstanding = outstanding;
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

    public String getTotalAmountToPay() {
        return totalAmountToPay;
    }

    public void setTotalAmountToPay(String totalAmountToPay) {
        this.totalAmountToPay = totalAmountToPay;
    }

    public String getAmountPayed() {
        return amountPayed;
    }

    public void setAmountPayed(String amountPayed) {
        this.amountPayed = amountPayed;
    }

    public String getOutstanding() {
        return outstanding;
    }

    public void setOutstanding(String outstanding) {
        this.outstanding = outstanding;
    }
}
