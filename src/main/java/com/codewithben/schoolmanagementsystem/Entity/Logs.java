package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
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

    @Column(nullable = false)
    private LocalDate actionDate;

    @Column(nullable = false)
    private LocalTime actionTime;

    @Enumerated(EnumType.STRING)
    private LogType type;

    @Enumerated(EnumType.STRING)
    private LogAction action;

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
