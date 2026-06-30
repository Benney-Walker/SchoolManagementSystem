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
@Table(name = "attendance_date")
public class AttendanceDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int dateId;

    private LocalDate attendanceDate;

    @OneToMany(mappedBy = "attendanceDate")
    private List<AttendanceRecords> attendanceRecords;

    @ManyToOne
    @JoinColumn(name = "semester_semesterID")
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "level_levelID")
    private Level level;

    @ManyToOne
    @JoinColumn(name = "staff_staffId")
    private Staffs staff;
}
