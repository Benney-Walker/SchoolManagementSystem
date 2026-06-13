package com.codewithben.schoolmanagementsystem.DTO.Fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentPaymentRecords {
    private String paymentDate;

    private String studentId;

    private String studentNameCol;

    private String amountCol;

    private String payerCol;

    private String levelCol;

}
