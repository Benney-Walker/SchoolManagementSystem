package com.codewithben.schoolmanagementsystem.DTO.Fees;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Amount must be a valid number")
    private String amount;

    @NotBlank(message = "Semester is required")
    private String semesterId;

    @NotBlank(message = "Class is required")
    private String classId;

}
