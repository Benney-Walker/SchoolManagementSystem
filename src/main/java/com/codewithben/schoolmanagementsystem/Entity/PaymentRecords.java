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

    private double amountPaid;

    private double feesBalance;

    private String personWhoPaid;

    private String phoneNumber;

    private LocalDate dateOfPayment;

    private boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "fee_record_recordId")
    private StudentFeeRecord feeRecord;

    @ManyToOne
    @JoinColumn(name = "institution_institutionId")
    private Institution institution;
}
