package com.codewithben.schoolmanagementsystem.DTO.Fees;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewFees {

    @NotEmpty(message = "Term must not be empty")
    private String semesterId;

    @NotEmpty(message = "At least one class must be selected")
    private List<String> levelIds;

    @Positive(message = "Invalid fees amount")
    private float feesAmount;
}
