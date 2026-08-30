package com.codewithben.schoolmanagementsystem.DTO.SpecialPayments;

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
public class UpdateSpecialPayment {

    @NotBlank(message = "Special Payment id is required")
    private String id;

    @Positive(message = "Amount must be more than 0")
    private int amount;

    @NotBlank(message = "Description is required")
    private String description;
}
