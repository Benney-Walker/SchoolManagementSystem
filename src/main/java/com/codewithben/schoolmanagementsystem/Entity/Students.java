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

    @ManyToOne
    private Level level;

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
    private List<Attendance> attendance;


}
