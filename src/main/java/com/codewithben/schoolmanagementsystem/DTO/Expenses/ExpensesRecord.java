package com.codewithben.schoolmanagementsystem.DTO.Expenses;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpensesRecord {

    private String expensesId;

    private String description;

    private int amountSpent;

    private String extraInfo;

    private String expenseDate;

    private String semesterId;
}
