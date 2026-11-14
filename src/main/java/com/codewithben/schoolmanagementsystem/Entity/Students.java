package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Students {

    @Id
    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String homeTown;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Institution_institutionId")
    private Institution institution;

    @Column(nullable = false)
    private String parentName;

    @Column(nullable = false)
    private String parentPhoneNumber;

    @Column(nullable = false)
    private LocalDate registrationDate;

    // Current class. Null once the student has graduated: a graduated student is no
    // longer enrolled anywhere, so class rosters and class counts never include them.
    // Past results, attendance and fee records keep their own link to the class.
    @ManyToOne
    private Level level;
    // Set when the student graduates (see Utility.StudentEnrolment); cleared if they
    // are re-activated. graduationClassName is a snapshot so a later class rename
    // doesn't rewrite history.
    private LocalDate graduationDate;
    private String graduationClassName;
    private String graduationAcademicYear;

    private boolean isNew;

    private boolean isBorder;

    @OneToMany(mappedBy = "student")
    private List<SubjectScore> subjectScore;

    @OneToMany(mappedBy = "student")
    private List<Conduct> conducts;

    @OneToMany(mappedBy = "student")
    private List<StudentFeeRecord> studentFeeRecords;

    @OneToMany(mappedBy = "student")
    private List<Results> results;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus studentStatus;

    @OneToMany(mappedBy = "student")
    private List<AttendanceRecords> attendanceRecords;


}
