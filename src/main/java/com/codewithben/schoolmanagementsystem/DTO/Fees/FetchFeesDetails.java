package com.codewithben.schoolmanagementsystem.DTO.Fees;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Fees Id must not be null")
    private String feesId;

    @Positive(message = "Fees amount should be a positive amount")
    private float amount;

    private String semesterId;

    private String levelName;

}
