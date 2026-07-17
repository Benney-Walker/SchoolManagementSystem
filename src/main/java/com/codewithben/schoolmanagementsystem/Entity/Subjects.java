package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Subjects {

    @Id
    private String subjectId;

    private String subjectName;

    @OneToMany(mappedBy = "subject")
    private List<SubjectScore> subjectScore;

    @ManyToOne
    @JoinColumn(name = "Level_levelID")
    private Level level;
}
