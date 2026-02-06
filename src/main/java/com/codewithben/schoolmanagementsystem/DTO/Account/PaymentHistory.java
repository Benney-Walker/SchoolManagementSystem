package com.codewithben.schoolmanagementsystem.DTO.Account;

public class PaymentHistory {

    private String dateOfPayment;

    private String amountPaid;

    private String feesBalance;

    private String personWhoPaid;

    private String contactInfo;

    public PaymentHistory(String dateOfPayment, String amountPaid, String feesBalance, String personWhoPaid, String contactInfo) {
        this.dateOfPayment = dateOfPayment;
        this.amountPaid = amountPaid;
        this.feesBalance = feesBalance;
        this.personWhoPaid = personWhoPaid;
        this.contactInfo = contactInfo;
    }

    public String getDateOfPayment() {
        return dateOfPayment;
    }

    public void setDateOfPayment(String dateOfPayment) {
        this.dateOfPayment = dateOfPayment;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getFeesBalance() {
        return feesBalance;
    }

    public void setFeesBalance(String feesBalance) {
        this.feesBalance = feesBalance;
    }

    public String getPersonWhoPaid() {
        return personWhoPaid;
    }

    public void setPersonWhoPaid(String personWhoPaid) {
        this.personWhoPaid = personWhoPaid;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
