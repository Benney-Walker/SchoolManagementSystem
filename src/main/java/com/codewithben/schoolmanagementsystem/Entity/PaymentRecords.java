package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private float amountPaid;

    @Column(nullable = false)
    private float feesBalance;

    @Column(nullable = false)
    private String personWhoPaid;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate dateOfPayment;

    @ManyToOne
    @JoinColumn(name = "feeRecord_recordId", nullable = false)
    private StudentFeeRecord feeRecord;

    @ManyToOne
    @JoinColumn(name = "institution_Id", nullable = false)
    private Institution institution;

    /*
     * Soft delete (issue 3): a deleted payment keeps its row with when, by whom and why, so
     * the money trail can be audited. Null means not deleted (existing rows need no update).
     * Deleted payments count nowhere: use PaymentRules.activePayments / the ...DeletedAtIsNull
     * finders whenever payments are read or summed.
     */
    private LocalDateTime deletedAt;

    @Column(length = 64)
    private String deletedBy;

    @Column(length = 255)
    private String deletionReason;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
