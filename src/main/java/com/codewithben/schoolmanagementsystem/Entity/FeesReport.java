package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FeesReport {
    @Id
    private String feesReportId;

    private Double amountPaid;

    private double feesBalance;

    private String personWhoPaid;

    private String phoneNumber;

    private LocalDate dateOfPayment;

    private boolean isDeleted;

    @ManyToOne
    @JoinColumn(nullable = false, name = "student_studentId")
    private Students student;

    @ManyToOne
    @JoinColumn(name = "fees_feesId")
    private Fees fees;

    @ManyToOne
    @JoinColumn(name = "institution_institutionId")
    private Institution institution;
}
