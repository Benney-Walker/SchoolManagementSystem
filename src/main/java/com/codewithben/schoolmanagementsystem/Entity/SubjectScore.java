package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SubjectScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Subject_subjectId")
    private Subjects subject;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Student_studentId")
    private Students student;

    @ManyToOne
    @JoinColumn(name = "results_resultId")
    private Results results;

    @ManyToOne
    @JoinColumn(nullable = false, name = "semester_semesterId")
    private Semester semester;

    //Then exercise1Score
    @Column(name = "exercise1score")
    private Double projectWork;

    //Then exercise2Score
    @Column(name = "exercise2score")
    private Double groupWork;

    //Then classTestScore
    @Column(name = "class_test_score")
    private Double classTest1;

    //Then projectScore
    @Column(name = "project_score")
    private Double classTest2;

    private Double classScore;

    private Double examScore;

    private Double calculatedExamScore;

    private Double totalScore;

    private String grade;

    @Column(name = "remarks")
    private String gradeDescriptor;
}
