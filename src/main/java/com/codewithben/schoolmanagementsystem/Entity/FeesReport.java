package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class FeesReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long feesReportId;

    private Double amountPaid;

    private double feesBalance;

    private String personWhoPaid;

    private String phoneNumber;

    private LocalDate dateOfPayment;

    @ManyToOne
    @JoinColumn(nullable = false, name = "student_studentId")
    private Students student;

    @ManyToOne
    @JoinColumn(name = "fees_feesId")
    private Fees fees;

    @ManyToOne
    private Institution institution;

    public long getFeesReportId() {
        return feesReportId;
    }

    public void setFeesReportId(long feesReportId) {
        this.feesReportId = feesReportId;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getFeesBalance() {
        return feesBalance;
    }

    public void setFeesBalance(double feesBalance) {
        this.feesBalance = feesBalance;
    }

    public String getPersonWhoPaid() {
        return personWhoPaid;
    }

    public void setPersonWhoPaid(String personWhoPaid) {
        this.personWhoPaid = personWhoPaid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfPayment() {
        return dateOfPayment;
    }

    public void setDateOfPayment(LocalDate dateOfPayment) {
        this.dateOfPayment = dateOfPayment;
    }

    public Students getStudent() {
        return student;
    }

    public void setStudent(Students student) {
        this.student = student;
    }

    public Fees getFees() {
        return fees;
    }

    public void setFees(Fees fees) {
        this.fees = fees;
    }
}
