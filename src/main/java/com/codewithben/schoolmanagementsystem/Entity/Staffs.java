package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Contants.StaffRoles;
import com.codewithben.schoolmanagementsystem.Contants.StaffStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Staffs {

    @Id
    @Column(nullable = false, unique = true)
    private String staffId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @OneToMany(mappedBy = "staff")
    private List<StaffRolesEntity> staffRoles;

    @Column(nullable = false)
    private LocalDate dateOfRegistration;

    private String status;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Institution_institutionId")
    private Institution institution;

    @OneToMany(mappedBy = "staff")
    private List<Level> level;

    @Enumerated(EnumType.STRING)
    private StaffStatus staffStatus;

    public Staffs() {}

    public Staffs(String staffId, String firstName, String lastName, String gender,
                  LocalDate dateOfBirth, String password, String email, String phoneNumber,
                  List<StaffRolesEntity> staffRoles, LocalDate dateOfRegistration, String status, Institution institution, List<Level> level, StaffStatus staffStatus) {
        this.staffId = staffId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.staffRoles = staffRoles;
        this.dateOfRegistration = dateOfRegistration;
        this.status = status;
        this.institution = institution;
        this.level = level;
        this.staffStatus = staffStatus;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public List<StaffRolesEntity> getStaffRoles() {
        return staffRoles;
    }

    public void setStaffRoles(List<StaffRolesEntity> staffRoles) {
        this.staffRoles = staffRoles;
    }

    public LocalDate getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(LocalDate dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public List<Level> getLevels() {
        return level;
    }

    public void setLevel(List<Level> level) {
        this.level = level;
    }

    public StaffStatus getStaffStatus() {
        return staffStatus;
    }

    public void setStaffStatus(StaffStatus staffStatus) {
        this.staffStatus = staffStatus;
    }
}
