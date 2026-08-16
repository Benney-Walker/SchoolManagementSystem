package com.codewithben.schoolmanagementsystem.DTO.Expenses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpensesRecord {

    private long expenseId;

    private String description;

    private int amountSpent;

    private String extraInfo;

    private String expenseDate;

    private String semesterId;
}
