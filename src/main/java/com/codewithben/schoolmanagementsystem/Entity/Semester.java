package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Semester {

    @Id
    private String semesterID;

    private String semesterName;

    private LocalDate semesterStartDate;

    private LocalDate semesterEndDate;

    private String academicYear;

    @ManyToOne
    @JoinColumn(name = "Institution_institutionId")
    private Institution institution;

    public String getSemesterID() {
        return semesterID;
    }

    public void setSemesterID(String semesterID) {

        this.semesterID = semesterID;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public LocalDate getSemesterStartDate() {

        return semesterStartDate;
    }

    public void setSemesterStartDate(LocalDate semesterStartDate) {

        this.semesterStartDate = semesterStartDate;
    }

    public LocalDate getSemesterEndDate() {

        return semesterEndDate;
    }

    public void setSemesterEndDate(LocalDate semesterEndDate) {

        this.semesterEndDate = semesterEndDate;
    }

    public String getAcademicYear() {

        return academicYear;
    }

    public void setAcademicYear(String academicYear) {

        this.academicYear = academicYear;
    }

    public Institution getInstitution() {

        return institution;
    }

    public void setInstitution(Institution institution) {

        this.institution = institution;
    }
}
