package com.codewithben.schoolmanagementsystem.DTO.Offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflinePaymentList {

    private String studentId;

    private String levelId;

    private String semesterId;

    private String amountPaid;

    private String payerName;

    private String payerPhone;

    private String staffId;
}
