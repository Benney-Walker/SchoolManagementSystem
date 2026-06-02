package com.codewithben.schoolmanagementsystem.DTO.Students;

import java.time.LocalDate;

public class AddNewStudent {
    private String firstName;

    private String lastName;

    private String gender;

    private LocalDate dateOfBirth;

    private String hometown;

    private String parentName;

    private String guardianContact;

    private String levelId;

    public AddNewStudent(String firstName, String lastName, String gender, LocalDate dateOfBirth, String hometown, String parentName,
                         String guardianContact, String levelId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.hometown = hometown;
        this.parentName = parentName;
        this.guardianContact = guardianContact;
        this.levelId = levelId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getHomeTown() {
        return hometown;
    }

    public void setHomeTown(String homeTown) {
        this.hometown = homeTown;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getGuardianContact() {
        return guardianContact;
    }

    public void setGuardianContact(String guardianContact) {
        this.guardianContact = guardianContact;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String level) {
        this.levelId = level;
    }
}
