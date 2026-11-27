package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesRecord;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesReport;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Expenses of the caller's own school.
 *
 * Every lookup is limited to the caller's school: a term or expense id from another school
 * is answered exactly like a missing one (404). Before this, terms and expenses were looked
 * up by id alone, so an accountant could read, edit, delete or print another school's
 * expenses by changing the id. /api/finance/** already admits only ACCOUNTANT and PRINCIPAL.
 */
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

    public ResponseEntity<?> addNewExpenses(NewExpenses newExpenses, String staffId) {

        Staffs staff = caller(staffId);
        if (staff == null) {
            return sessionExpired();
        }

        Semester semester = schoolSemester(staff, newExpenses.getSemesterId());
        if(semester == null){
            return notFound(LogAction.CREATE, "Term not found", staffId);
        }

        // Was "!= null && isEmpty()", which kept the typed text only when it was empty, so every
        // expense was saved as "Extra info empty".
        String extraInfo = newExpenses.getExtraInfo() == null || newExpenses.getExtraInfo().isBlank()
                ? "Extra info empty"
                : newExpenses.getExtraInfo().trim();

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

        Staffs staff = caller(staffId);
        if (staff == null) {
            return sessionExpired();
        }

        Semester semester = schoolSemester(staff, semesterId);
        if (semester == null) {
            return notFound(LogAction.READ, "Term not found", staffId);
        }

        List<ExpensesRecord> expensesList = readExpensesRecords(semester);
        if(expensesList.isEmpty()){
            return notFound(LogAction.READ, "No expenses found for this term", staffId);
        }

        return ResponseEntity.ok(expensesList);
    }

    public ResponseEntity<?> updateExpenseRecord(UpdateExpensesRecord expensesRecord, String staffId) {

        Staffs staff = caller(staffId);
        if (staff == null) {
            return sessionExpired();
        }

        Semester semester = schoolSemester(staff, expensesRecord.getSemesterId());
        if(semester == null){
            return notFound(LogAction.UPDATE, "Term not found", staffId);
        }

        Expenses existingRecord = schoolExpense(staff, expensesRecord.getExpensesId());
        if(existingRecord == null){
            return notFound(LogAction.UPDATE, "Expenses record not found", staffId);
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

        Staffs staff = caller(staffId);
        if(staff == null){
            return sessionExpired();
        }

        Semester semester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (semester == null) {
            return notFound(LogAction.READ, "Current semester not found", staffId);
        }

        List<ExpensesRecord> semesterExpenses = readExpensesRecords(semester);
        if(semesterExpenses.isEmpty()){
            return notFound(LogAction.READ, "No expenses found", staffId);
        }

        return ResponseEntity.ok(total(semesterExpenses));
    }

    public ResponseEntity<?> generateExpensesReport(String semesterId, String staffId) {

        // The school comes from the caller, never from shared state on this singleton.
        Staffs staff = caller(staffId);
        if (staff == null) {
            return sessionExpired();
        }

        Semester semester = schoolSemester(staff, semesterId);
        if (semester == null) {
            return notFound(LogAction.READ, "Term not found", staffId);
        }

        List<ExpensesRecord> expensesList = readExpensesRecords(semester);
        if(expensesList.isEmpty()){
            return notFound(LogAction.READ, "No expenses found for this term", staffId);
        }

        ExpensesReport report = ExpensesReport.builder()
                .semesterName(semester.getSemesterName())
                .academicYear(semester.getAcademicYear())
                .records(expensesList)
                .totalAmount(total(expensesList))
                .recordCount(expensesList.size())
                .dateOfReport(LocalDate.now().toString())
                .build();

        try {
            byte[] expensesPdfReport = jasperReportService.generateExpensesReport(report, staff.getInstitution());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(expensesPdfReport);
        } catch (Exception e) {
            log.error("Could not generate expenses report for semester {}", semesterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> deleteExpenses(String expensesId, String staffId) {

        Staffs staff = caller(staffId);
        if (staff == null) {
            return sessionExpired();
        }

        Expenses expenses = schoolExpense(staff, expensesId);
        if(expenses == null){
            return notFound(LogAction.DELETE, "Expenses record not found", staffId);
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


    /*================================================
                        HELPERS
     =================================================*/

    /** The term's expenses, oldest first. The term must already be checked against the caller's school. */
    private List<ExpensesRecord> readExpensesRecords(Semester semester) {

        List<Expenses> expensesList = expensesRepo.findBySemester_SemesterID(semester.getSemesterID());
        if (expensesList == null) {
            return List.of();
        }

        return expensesList.stream()
                .sorted(Comparator.comparing(Expenses::getExpenseDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Expenses::getDescription, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(expenses -> ExpensesRecord.builder()
                        .expensesId(expenses.getExpenseId())
                        .description(expenses.getDescription())
                        .amountSpent(expenses.getAmountSpent())
                        .extraInfo(expenses.getExtraInfo())
                        .expenseDate(expenses.getExpenseDate() == null ? "" : expenses.getExpenseDate().toString())
                        .build())
                .toList();
    }

    /* Summed in BigDecimal so float noise (0.1 + 0.2) doesn't reach the report. */
    static double total(List<ExpensesRecord> records) {
        BigDecimal sum = BigDecimal.ZERO;
        for (ExpensesRecord record : records) {
            sum = sum.add(new BigDecimal(Float.toString(record.getAmountSpent())));
        }
        return sum.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Staffs caller(String staffId) {
        Staffs staff = staffId == null ? null : staffsRepository.findByStaffId(staffId).orElse(null);
        return staff == null || staff.getInstitution() == null ? null : staff;
    }

    /** The term if it belongs to the caller's school, else null. */
    private Semester schoolSemester(Staffs staff, String semesterId) {
        Semester semester = semesterId == null ? null : semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null || semester.getInstitution() == null
                || !Objects.equals(semester.getInstitution().getInstitutionId(), staff.getInstitution().getInstitutionId())) {
            return null;
        }
        return semester;
    }

    private Expenses schoolExpense(Staffs staff, String expenseId) {
        return expenseId == null ? null : expensesRepo
                .findByExpenseIdAndSemester_Institution_InstitutionId(expenseId, staff.getInstitution().getInstitutionId())
                .orElse(null);
    }

    private ResponseEntity<?> notFound(LogAction action, String message, String staffId) {
        loggingService.logGeneralActivity(LogType.EXPENSES, action, message, staffId, LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", message));
    }

    private ResponseEntity<?> sessionExpired() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", "Session expired. Login again"
        ));
    }
}
