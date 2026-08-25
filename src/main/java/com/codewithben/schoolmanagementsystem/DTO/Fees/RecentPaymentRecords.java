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
    private String date;

    private String studentName;

    private String amount;

}
