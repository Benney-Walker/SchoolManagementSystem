package com.codewithben.schoolmanagementsystem.DTO.SpecialPayments;

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
public class UpdateSpecialPayment {

    @NotBlank(message = "Special Payment id is required")
    private String id;

    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Amount must be a valid number")
    private int amount;

    @NotBlank(message = "Description is required")
    private String description;
}
