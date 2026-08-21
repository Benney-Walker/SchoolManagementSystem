package com.codewithben.schoolmanagementsystem.DTO.Fees;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
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

    @Positive(message = "Fees amount should be a positive amount")
    private float amount;

    @NotBlank(message = "Semester is required")
    private String semesterId;

    @NotBlank(message = "Class is required")
    private String classId;

}
