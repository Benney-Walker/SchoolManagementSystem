package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Expenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpensesRepo extends JpaRepository<Expenses, Long> {

    List<Expenses> findBySemester_SemesterID(String semesterId);

    Optional<Expenses> findByExpenseId(Long expenseId);
}
