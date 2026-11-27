package com.codewithben.schoolmanagementsystem.DTO.Expenses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Everything the expenses report template prints, as one object (the same pattern as
 * SbaReport). The template reads the header fields directly and the rows through a
 * table datasetRun over {@code records}.
 *
 * Template fields: semesterName, academicYear, records, totalAmount, recordCount,
 * dateOfReport. Row fields (ExpensesRecord): expensesId, description, amountSpent,
 * extraInfo, expenseDate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpensesReport {

    private String semesterName;

    private String academicYear;

    private List<ExpensesRecord> records;

    /** Sum of amountSpent, rounded to 2 decimal places. */
    private Double totalAmount;

    private Integer recordCount;

    private String dateOfReport;
}
