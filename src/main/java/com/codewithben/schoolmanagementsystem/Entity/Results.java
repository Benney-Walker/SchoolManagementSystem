package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Results {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long resultId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "student_studentId")
    private Students student;

    @ManyToOne
    @JoinColumn(nullable = false, name = "level_levelID")
    private Level level;

    @ManyToOne
    @JoinColumn(nullable = false, name = "semester_semesterID")
    private Semester semester;

    @OneToMany(mappedBy = "results", cascade = CascadeType.ALL)
    private List<SubjectScore> subjectScores;

    @OneToOne(mappedBy = "results")
    private Conduct conduct;

    private LocalDate createdAt;

    private LocalDate updatedAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Staffs updatedBy;

    private Double totalScore;

    private Double averageScore;

    private String position;

}
