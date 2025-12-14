package com.codewithben.schoolmanagementsystem.DTO.Institution;

import java.time.LocalDate;

public class FindStaffDTO {

    private String staffId;

    private String firstName;

    private String surname;

    private String gender;

    private LocalDate dateOfBirth;

    private String email;

    private String phoneNumber;

    private String status;

    private String dateOfRegistration;

    public FindStaffDTO(String staffId, String firstName, String surname,
                        String gender, LocalDate dateOfBirth, String email, String phoneNumber, String status,
                        String dateOfRegistration) {
        this.staffId = staffId;
        this.firstName = firstName;
        this.surname = surname;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }
}
