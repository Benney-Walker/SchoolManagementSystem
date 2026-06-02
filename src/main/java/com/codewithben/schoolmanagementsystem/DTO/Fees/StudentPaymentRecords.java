package com.codewithben.schoolmanagementsystem.DTO.Fees;

public class StudentPaymentRecords {
    private String paymentId;

    private String dateOfPayment;

    private String studentId;

    private String studentName;

    private String amount;

    private String semesterId;

    private String payerName;

    private String payerContact;

    public StudentPaymentRecords() {}

    public StudentPaymentRecords(String paymentId, String dateOfPayment, String studentId, String studentName, String amount,
                                 String semesterId, String payerName, String payerContact) {
        this.paymentId = paymentId;
        this.dateOfPayment = dateOfPayment;
        this.studentId = studentId;
        this.studentName = studentName;
        this.amount = amount;
        this.semesterId = semesterId;
        this.payerName = payerName;
        this.payerContact = payerContact;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getDateOfPayment() {
        return dateOfPayment;
    }

    public void setDateOfPayment(String dateOfPayment) {
        this.dateOfPayment = dateOfPayment;
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

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerContact() {
        return payerContact;
    }

    public void setPayerContact(String payerContact) {
        this.payerContact = payerContact;
    }
}
