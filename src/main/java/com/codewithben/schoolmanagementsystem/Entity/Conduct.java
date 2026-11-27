package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Constants.ConductRatings;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Conduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Student_studentId")
    private Students student;

    // One conduct record per term result. The unique key stops two devices saving the same
    // student at the same moment from creating two rows (which made loading that result fail).
    // Hibernate adds it on startup; if duplicates already exist it only logs a warning.
    @OneToOne
    @JoinColumn(name = "Results_resultId", unique = true)
    private Results results;

    @Enumerated(EnumType.STRING)
    private ConductRatings regular;

    @Enumerated(EnumType.STRING)
    private ConductRatings punctual;

    @Enumerated(EnumType.STRING)
    private ConductRatings physicalAppearance;

    @Enumerated(EnumType.STRING)
    private ConductRatings social;

    @Enumerated(EnumType.STRING)
    private ConductRatings emotional;

    @Enumerated(EnumType.STRING)
    private ConductRatings cognitiveSkills;

    @Column(length = 255)
    private String classTeacherRemark;

    @Column(length = 255)
    private String peculiarIssue;

}
