package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesRecord;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.NewExpenses;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.UpdateExpensesRecord;
import com.codewithben.schoolmanagementsystem.Entity.Expenses;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Repository.ExpensesRepo;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ExpensesService {

    private final ExpensesRepo expensesRepo;

    private final LoggingService loggingService;

    private final SemesterRepository semesterRepository;

    public ResponseEntity<?> addNewExpenses(NewExpenses newExpenses, String staffId) {

        String extraInfo = "Extra info empty";

        Semester semester = semesterRepository.findBySemesterID(newExpenses.getSemesterId()).orElse(null);
        if(semester == null){
            loggingService.logGeneralActivity(
                    LogType.EXPENSES,
                    LogAction.CREATE,
                    "Term not found",
                    staffId,
                    LogStatus.FAILED
                    );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term not found"
            ));
        }

        if (newExpenses.getExtraInfo() != null &&  newExpenses.getExtraInfo().isEmpty()) {
            extraInfo = newExpenses.getExtraInfo();
        }

        Expenses expenses = Expenses.builder()
                .description(newExpenses.getDescription())
                .amountSpent(newExpenses.getAmountSpent())
                .extraInfo(extraInfo)
                .semester(semester)
                .expenseDate(LocalDate.now())
                .institution(semester.getInstitution())
                .build();
        expensesRepo.save(expenses);

        loggingService.logGeneralActivity(
                LogType.EXPENSES,
                LogAction.CREATE,
                "Successfully added expenses",
                staffId,
                LogStatus.SUCCESS
        );
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> readExpenses(String semesterId, String staffId) {

        List<Expenses> expensesList = expensesRepo.findBySemester_SemesterID(semesterId);
        if(expensesList == null || expensesList.isEmpty()){
            loggingService.logGeneralActivity(
                    LogType.EXPENSES,
                    LogAction.READ,
                    "No expenses found for this term",
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No expenses found for this term"
            ));
        }

        List<ExpensesRecord> records = new ArrayList<>();
        for (Expenses expenses : expensesList) {
            ExpensesRecord record = ExpensesRecord.builder()
                    .expensesId(expenses.getExpenseId())
                    .description(expenses.getDescription())
                    .amountSpent(expenses.getAmountSpent())
                    .extraInfo(expenses.getExtraInfo())
                    .expenseDate(expenses.getExpenseDate().toString())
                    .build();
            records.add(record);
        }

        return ResponseEntity.ok(records);
    }

    public ResponseEntity<?> updateExpenseRecord(UpdateExpensesRecord expensesRecord, String staffId) {

        Semester semester = semesterRepository.findBySemesterID(expensesRecord.getSemesterId()).orElse(null);
        if(semester == null){
            loggingService.logGeneralActivity(
                    LogType.EXPENSES,
                    LogAction.UPDATE,
                    "Term not found",
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term not found"
            ));
        }

        Expenses existingRecord = expensesRepo.findByExpenseId(expensesRecord.getExpensesId()).orElse(null);
        if(existingRecord == null){
            loggingService.logGeneralActivity(
                    LogType.EXPENSES,
                    LogAction.UPDATE,
                    "Expenses record not found",
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Expenses record not found"
            ));
        }

        existingRecord.setDescription(expensesRecord.getDescription());
        existingRecord.setAmountSpent(expensesRecord.getAmountSpent());
        existingRecord.setExtraInfo(expensesRecord.getExtraInfo());
        existingRecord.setSemester(semester);
        expensesRepo.save(existingRecord);

        loggingService.logGeneralActivity(
                LogType.EXPENSES,
                LogAction.UPDATE,
                expensesRecord.getDescription() + " expenses record updated",
                staffId,
                LogStatus.SUCCESS
        );
        return ResponseEntity.ok().build();
    }
}
