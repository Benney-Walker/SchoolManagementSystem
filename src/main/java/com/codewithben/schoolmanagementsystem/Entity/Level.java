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
public class Level {

    @Id
    private String levelID;

    private String levelName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_staffId")
    private Staffs staff;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Institution_institutionId")
    private Institution institution;

    @OneToMany(mappedBy = "level")
    private List<Subjects> subjects;

    @OneToMany(mappedBy = "level")
    private List<Students> students;

    @OneToMany(mappedBy = "level")
    private List<Fees> fees;
}
