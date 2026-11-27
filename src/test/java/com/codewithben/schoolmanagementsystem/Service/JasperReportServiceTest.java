package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesRecord;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesReport;
import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentResult;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JasperReportServiceTest {

    private static final Set<String> ALL_BRANDING = new HashSet<>(JasperReportService.BRANDING_PARAMETERS);

    @Test
    void matchingTemplateHasNoProblems() {
        Set<String> fields = Set.of("studentId", "studentName", "subjectScores", "studentConductReport");

        assertEquals(List.of(), JasperReportService.problems("main", ALL_BRANDING, fields, GenerateStudentResult.class));
    }

    @Test
    void oldTemplateWithoutBrandingParametersIsReported() {
        // A .jasper compiled before the branding change declares logoPath, not logo/signature.
        Set<String> oldParameters = Set.of("schoolName", "logoPath");

        List<String> problems = JasperReportService.problems("main", oldParameters, Set.of(), GenerateStudentResult.class);

        assertEquals(4, problems.size());
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("'logo'")));
        assertTrue(problems.stream().anyMatch(problem -> problem.contains("'signature'")));
    }

    @Test
    void fieldTheBeanLacksIsReported() {
        List<String> problems = JasperReportService.problems(
                "main", ALL_BRANDING, Set.of("semesterName", "grandTotal"), ExpensesReport.class);

        assertEquals(1, problems.size());
        assertTrue(problems.getFirst().contains("grandTotal"));
    }

    @Test
    void tableDatasetsAreCheckedWithoutParameters() {
        assertEquals(List.of(), JasperReportService.problems(
                "dataset Rows", null, Set.of("description", "amountSpent", "expenseDate", "_THIS"), ExpensesRecord.class));
    }
}
