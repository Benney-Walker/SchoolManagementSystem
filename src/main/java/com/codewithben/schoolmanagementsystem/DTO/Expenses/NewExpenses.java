package com.codewithben.schoolmanagementsystem.DTO.Expenses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewExpenses {

    private String description;

    private String extraInfo;

    private int amountSpent;

    private String semesterId;
}
