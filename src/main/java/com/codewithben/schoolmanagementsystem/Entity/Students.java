package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Students {

    @Id
    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String homeTown;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Institution_institutionId")
    private Institution institution;

    @Column(nullable = false)
    private String parentName;

    @Column(nullable = false)
    private String parentPhoneNumber;

    @Column(nullable = false)
    private LocalDate registrationDate;

    @ManyToOne
    private Level level;

    @OneToMany(mappedBy = "student")
    private List<SubjectScore> subjectScore;

    @OneToMany(mappedBy = "student")
    private List<FeesReport> feesReport;

    @OneToMany(mappedBy = "student")
    private List<Results> results;

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public void setHomeTown(String homeTown) {
        this.homeTown = homeTown;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
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

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Level getLevel() {

        return level;
    }

    public void setLevel(Level levelId) {
        this.level = levelId;
    }

    public List<FeesReport> getFeesReport() {
        return feesReport;
    }

    public void setFeesReport(List<FeesReport> feesReport) {
        this.feesReport = feesReport;
    }

    public List<Results> getResults() {

        return results;
    }

    public void setResults(List<Results> results) {

        this.results = results;
    }

    public List<SubjectScore> getSubjectScore() {
        return subjectScore;
    }

    public void setSubjectScore(List<SubjectScore> subjectScore) {
        this.subjectScore = subjectScore;
    }
}
