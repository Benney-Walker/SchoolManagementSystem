package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StudentFeeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int feeRecordId;

    private double feeAmount;

    private double totalAmountPaid;

    private double balance;

    @ManyToOne
    @JoinColumn(name = "student_studentId")
    private Students student;

    @ManyToOne
    @JoinColumn(name = "fees_feesId")
    private Fees fees;

    @OneToMany(mappedBy = "recordsId", cascade = CascadeType.ALL)
    private List<PaymentRecords> paymentRecords;

    @ManyToOne
    @JoinColumn(name = "level_levelId")
    private Level level;

    @ManyToOne
    @JoinColumn(name = "semester_semesterId")
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "institution_institutionId")
    private Institution institution;
}
