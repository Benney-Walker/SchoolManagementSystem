package com.codewithben.schoolmanagementsystem.DTO.Fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassFeesSummary {
    private String className;

    private String totalStudents;

    private String expectedFees;

    private String amountPaid;

    private String outStandingFees;


}
