package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GradeSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private Double lowerRange;

    @Column(nullable = false)
    private Double upperRange;

    @Column(nullable = false)
    private String grade;

    @Column(length = 50)
    private String gradeDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;
}
