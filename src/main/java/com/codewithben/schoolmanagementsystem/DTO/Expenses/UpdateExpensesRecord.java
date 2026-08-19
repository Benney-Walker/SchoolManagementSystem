package com.codewithben.schoolmanagementsystem.DTO.Expenses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class UpdateExpensesRecord {

    @NotBlank(message = "Expenses Id is required")
    private String expensesId;

    @NotBlank(message = "Description is required")
    private String description;

    @Positive(message = "Amount is invalid")
    private float amountSpent;

    @NotBlank(message = "Extra info is required")
    private String extraInfo;

    @NotBlank(message = "Term is required")
    private String semesterId;
}
