package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesRecord;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.NewExpenses;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.UpdateExpensesRecord;
import com.codewithben.schoolmanagementsystem.Entity.Expenses;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.ExpensesRepo;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExpensesService {

    private final ExpensesRepo expensesRepo;

    private final LoggingService loggingService;

    private final SemesterRepository semesterRepository;

    private final StaffsRepository staffsRepository;

    private final JasperReportService jasperReportService;

    private final UtilityClass utilityClass;

    private String schoolName;

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

        List<ExpensesRecord> expensesList = readExpensesRecords(semesterId);
        if(expensesList.isEmpty()){
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

        return ResponseEntity.ok(expensesList);
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

    public ResponseEntity<?> totalSemesterExpenses(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            log.error("Invalid staff id provided for total expenses");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Semester semester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (semester == null) {
            loggingService.logGeneralActivity(
                    LogType.EXPENSES,
                    LogAction.READ,
                    "Current semester not added",
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not found"
            ));
        }

        List<Expenses> semesterExpenses = expensesRepo.findBySemester_SemesterID(
                semester.getSemesterID()
        );
        if(semesterExpenses == null || semesterExpenses.isEmpty()){
            loggingService.logGeneralActivity(
                    LogType.EXPENSES,
                    LogAction.READ,
                    "No expenses found",
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No expenses found"
            ));
        }

        return ResponseEntity.ok(
                semesterExpenses.stream().mapToDouble(Expenses::getAmountSpent).sum()
        );
    }

    public ResponseEntity<?> generateExpensesReport(String semesterId, String staffId) {

        schoolName = null;

        List<ExpensesRecord> expensesList = readExpensesRecords(semesterId);
        if(expensesList.isEmpty()){
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

        try {
            byte[] expensesPdfReport = jasperReportService.generateExpensesReport(
                    expensesList, schoolName
            );
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(expensesPdfReport);
        } catch (Exception e) {
            log.error(String.valueOf(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> deleteExpenses(String expensesId, String staffId) {
        Expenses expenses = expensesRepo.findByExpenseId(expensesId).orElse(null);
        if(expenses == null){
            loggingService.logGeneralActivity(
                    LogType.EXPENSES,
                    LogAction.DELETE,
                    "Expenses record not found",
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Expenses record not found"
            ));
        }

        expensesRepo.delete(expenses);
        loggingService.logGeneralActivity(
                LogType.EXPENSES,
                LogAction.DELETE,
                "Expenses record deleted",
                staffId,
                LogStatus.SUCCESS
        );
        return ResponseEntity.ok().build();
    }

    private List<ExpensesRecord> readExpensesRecords(String semesterId){

        List<Expenses> expensesList = expensesRepo.findBySemester_SemesterID(semesterId);
        if(expensesList == null || expensesList.isEmpty()){

            return Collections.emptyList();
        }

        List<ExpensesRecord> records = new ArrayList<>();
        for (Expenses expenses : expensesList) {
            if(schoolName == null){
                schoolName = expenses.getInstitution().getInstitutionName();
            }
            ExpensesRecord record = ExpensesRecord.builder()
                    .expensesId(expenses.getExpenseId())
                    .description(expenses.getDescription())
                    .amountSpent(expenses.getAmountSpent())
                    .extraInfo(expenses.getExtraInfo())
                    .expenseDate(expenses.getExpenseDate().toString())
                    .build();
            records.add(record);
        }
        return records;
    }

}
