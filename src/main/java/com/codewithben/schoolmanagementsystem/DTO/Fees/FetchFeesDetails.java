package com.codewithben.schoolmanagementsystem.DTO.Fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchFeesDetails {
    private int feesId;

    private String amount;

    private String semesterId;

    private String classId;

}
