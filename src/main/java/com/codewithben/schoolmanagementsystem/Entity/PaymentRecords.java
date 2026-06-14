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
public class PaymentRecords {
    @Id
    private String recordsId;

    @Column(nullable = false)
    private double amountPaid;

    @Column(nullable = false)
    private double feesBalance;

    @Column(nullable = false)
    private String personWhoPaid;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate dateOfPayment;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "feeRecord_recordId", nullable = false)
    private StudentFeeRecord feeRecord;

    @ManyToOne
    @JoinColumn(name = "institution_institutionId", nullable = false)
    private Institution institution;
}
