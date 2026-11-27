package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Expenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpensesRepo extends JpaRepository<Expenses, Long> {

    /** Callers must first check the term belongs to the caller's school. */
    List<Expenses> findBySemester_SemesterID(String semesterId);

    /** An expense of the given school (through its term), so ids from other schools are not found. */
    Optional<Expenses> findByExpenseIdAndSemester_Institution_InstitutionId(String expenseId, String institutionId);
}
