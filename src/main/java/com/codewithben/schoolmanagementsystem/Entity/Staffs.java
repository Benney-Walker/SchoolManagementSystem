package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Constants.StaffStatus;
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
public class Staffs {

    @Id
    @Column(nullable = false, unique = true)
    private String staffId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @OneToMany(mappedBy = "staff")
    private List<StaffRolesEntity> roles;

    @Column(nullable = false)
    private LocalDate dateOfRegistration;

    private String status;

    @ManyToOne
    @JoinColumn(nullable = false, name = "Institution_institutionId")
    private Institution institution;

    @OneToMany(mappedBy = "staff")
    private List<Level> level;

    @Enumerated(EnumType.STRING)
    private StaffStatus staffStatus;

}
