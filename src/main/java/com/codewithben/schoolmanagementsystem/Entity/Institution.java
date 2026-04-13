package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Institution {

    @Id
    private String institutionId;

    @Column(length = 100, nullable = false)
    private String institutionName;

    @OneToMany(mappedBy = "institution")
    private List<Staffs> staff;

    @OneToMany(mappedBy = "institution")
    private List<Students> students;

    @OneToMany(mappedBy = "institution")
    private List<Level> level;

    @OneToMany(mappedBy = "institution")
    private List<Semester> semester;

    @OneToMany(mappedBy = "institution")
    private List<Fees> fees;

    @OneToMany(mappedBy = "institution")
    private List<FeesReport> feesReport;

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public List<Staffs> getStaff() {
        return staff;
    }

    public void setStaff(List<Staffs> staff) {
        this.staff = staff;
    }

    public List<Students> getStudents() {
        return students;
    }

    public void setStudents(List<Students> students) {
        this.students = students;
    }

    public List<Level> getLevels() {
        return level;
    }

    public void setLevel(List<Level> level) {
        this.level = level;
    }

    public List<Semester> getSemester() {
        return semester;
    }

    public void setSemester(List<Semester> semester) {
        this.semester = semester;
    }

    public List<Fees> getFees() {
        return fees;
    }

    public void setFees(List<Fees> fees) {
        this.fees = fees;
    }

    public List<FeesReport> getFeesReport() {
        return feesReport;
    }

    public void setFeesReport(List<FeesReport> feesReport) {
        this.feesReport = feesReport;
    }
}
