package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Fees {

    @Id
    private String feesId;

    @Column(nullable = false)
    private float amountToBePayed;

    @OneToMany(mappedBy = "fees")
    private List<StudentFeeRecord> feesRecords;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Semester_semesterID")
    private Semester semester;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Level_levelID")
    private Level level;

    @ManyToOne
    @JoinColumn(name = "institution_institutionId")
    private Institution institution;

    private boolean isLocked;
}
