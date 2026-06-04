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

    @OneToOne
    @JoinColumn(name = "Results_resultId")
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

    private String classTeacherRemark;

}
