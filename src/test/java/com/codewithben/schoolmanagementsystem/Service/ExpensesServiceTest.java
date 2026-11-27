package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpensesServiceTest {

    private static ExpensesRecord spent(float amount) {
        return ExpensesRecord.builder().amountSpent(amount).build();
    }

    @Test
    void totalHasNoFloatNoise() {
        assertEquals(0.3, ExpensesService.total(List.of(spent(0.1f), spent(0.2f))));
    }

    @Test
    void totalIsRoundedToTwoDecimals() {
        assertEquals(100.85, ExpensesService.total(List.of(spent(0.1f), spent(0.2f), spent(100.55f))));
    }

    @Test
    void totalOfNothingIsZero() {
        assertEquals(0.0, ExpensesService.total(List.of()));
    }
}
