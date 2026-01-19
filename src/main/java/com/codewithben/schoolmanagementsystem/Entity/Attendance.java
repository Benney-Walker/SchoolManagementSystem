package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Contants.AttendanceStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Students student;

    @ManyToOne
    private Level level;

    @ManyToOne
    private Semester semester;

    private LocalDate dateMarked;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @ManyToOne
    private Staffs markedBy;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Students getStudent() {
        return student;
    }

    public void setStudent(Students student) {
        this.student = student;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public LocalDate getDateMarked() {
        return dateMarked;
    }

    public void setDateMarked(LocalDate date) {
        this.dateMarked = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Staffs getMarkedBy() {
        return markedBy;
    }

    public void setMarkedBy(Staffs markedBy) {
        this.markedBy = markedBy;
    }
}
