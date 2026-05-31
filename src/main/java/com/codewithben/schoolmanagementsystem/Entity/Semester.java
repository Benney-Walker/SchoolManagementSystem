package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Semester {

    @Id
    private String semesterID;

    private String semesterName;

    private LocalDate semesterStartDate;

    private LocalDate semesterEndDate;

    private String academicYear;

    @OneToMany(mappedBy = "semester")
    private List<SchoolHoliday> schoolHoliday;

    @ManyToOne
    @JoinColumn(name = "Institution_institutionId")
    private Institution institution;
}
