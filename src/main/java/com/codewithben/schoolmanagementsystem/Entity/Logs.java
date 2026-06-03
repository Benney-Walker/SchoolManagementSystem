package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Logs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int actionId;

    private LocalDate actionDate;

    private LocalTime actionTime;

    @Enumerated(EnumType.STRING)
    private LogType actionType;

    @Column(length = 5000)
    private String actionData;

    @ManyToOne
    @JoinColumn(name = "Staff_StaffId")
    private Staffs createdBy;

    @Enumerated(EnumType.STRING)
    private LogStatus status;

    @ManyToOne
    @JoinColumn(name = "Institution_InstitutionId")
    private Institution institution;


}
