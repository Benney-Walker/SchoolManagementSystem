package com.codewithben.schoolmanagementsystem.DTO.Fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPaymentRecords {
    private String paymentId;

    private String dateOfPayment;

    private String studentId;

    private String studentName;

    private String amount;

    private String semesterId;

    private String payerName;

    private String payerContact;
}
