package com.codewithben.schoolmanagementsystem.DTO.Academics;

import com.codewithben.schoolmanagementsystem.Entity.Level;

import java.time.LocalDate;

public class FindStudentDTO {
    
    private String studentId;

    private String firstName;

    private String lastName;

    private String gender;

    private String dateOfBirth;

    private String parentName;

    private String parentPhoneNumber;
    
    private String gradeName;

    public FindStudentDTO(String studentId, String firstName, String lastName,
                          String gender, String dateOfBirth,
                          String parentName, String parentPhoneNumber, String gradeName) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.parentName = parentName;
        this.parentPhoneNumber = parentPhoneNumber;
        this.gradeName = gradeName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentPhoneNumber() {
        return parentPhoneNumber;
    }

    public void setParentPhoneNumber(String parentPhoneNumber) {
        this.parentPhoneNumber = parentPhoneNumber;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }
}
