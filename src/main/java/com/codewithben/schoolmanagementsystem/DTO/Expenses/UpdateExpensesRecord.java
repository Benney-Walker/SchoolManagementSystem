package com.codewithben.schoolmanagementsystem.DTO.Expenses;

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
public class UpdateExpensesRecord {

    @NotBlank(message = "Expenses Id is required")
    private String expensesId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Invalid amount value")
    private int amountSpent;

    @NotBlank(message = "Extra info is required")
    private String extraInfo;

    @NotBlank(message = "Term is required")
    private String semesterId;
}
