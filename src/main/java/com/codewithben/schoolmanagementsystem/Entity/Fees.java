package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Fees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int feesId;

    @Column(nullable = false)
    private Double amountToBePayed;

    @OneToMany(mappedBy = "fees")
    private List<FeesReport> feesReport;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Semester_semesterID")
    private Semester semester;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Level_levelID")
    private Level level;

    @ManyToOne
    @JoinColumn(name = "institution_institutionId")
    private Institution institution;

    public int getFeesId() {

        return feesId;
    }

    public void setFeesId(int feesId) {

        this.feesId = feesId;
    }

    public Double getAmountToBePayed() {

        return amountToBePayed;
    }

    public void setAmountToBePayed(Double amountToBePayed) {

        this.amountToBePayed = amountToBePayed;
    }

    public List<FeesReport> getFeesReport() {
        return feesReport;
    }

    public void setFeesReport(List<FeesReport> feesReport) {
        this.feesReport = feesReport;
    }

    public Semester getSemester() {

        return semester;
    }

    public void setSemester(Semester semester) {

        this.semester = semester;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
}
