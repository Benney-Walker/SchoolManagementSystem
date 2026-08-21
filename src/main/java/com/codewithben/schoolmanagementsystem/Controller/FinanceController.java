package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Expenses.NewExpenses;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.UpdateExpensesRecord;
import com.codewithben.schoolmanagementsystem.DTO.Fees.FetchFeesDetails;
import com.codewithben.schoolmanagementsystem.DTO.Fees.NewFees;
import com.codewithben.schoolmanagementsystem.DTO.Fees.NewPayment;
import com.codewithben.schoolmanagementsystem.DTO.Fees.StudentPaymentRecords;
import com.codewithben.schoolmanagementsystem.DTO.SpecialPayments.NewSpecialPayment;
import com.codewithben.schoolmanagementsystem.DTO.SpecialPayments.UpdateSpecialPayment;
import com.codewithben.schoolmanagementsystem.Service.ExpensesService;
import com.codewithben.schoolmanagementsystem.Service.FeesService;
import com.codewithben.schoolmanagementsystem.Service.SpecialPaymentService;
import com.codewithben.schoolmanagementsystem.Utility.AuthenticatedStaffProvider;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FeesService feesService;

    private final AuthenticatedStaffProvider authenticatedStaffProvider;

    private final ExpensesService expensesService;

    private final SpecialPaymentService specialPaymentService;

    @PostMapping("/v2/new-fees")
    public ResponseEntity<?> newFees(@Valid @RequestBody NewFees newFees) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.addNewSemesterFees(newFees, staffId);
    }

    @PostMapping("/v1/add-fees-payment")
    public ResponseEntity<?> addNewFeePayment(@RequestHeader("staffId") String Id,
                                              @Valid @RequestBody NewPayment data) {
        String studentId = data.getStudentId();
        Double amountPaid = data.getAmountPaid();
        String personWhoPaid = data.getPayerName();
        String phoneNumber = data.getPayerPhone();
        String levelId = data.getLevelId();
        String semesterId = data.getSemesterId();

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.addNewPayment(studentId, amountPaid, personWhoPaid, phoneNumber, levelId, semesterId, staffId);
    }

    @GetMapping("/v1/fetch-payment-records")
    public ResponseEntity<?> fetchPaymentRecords(@RequestHeader("StaffId") String Id,
                                                 @RequestParam String studentId,
                                                 @RequestParam String levelId,
                                                 @RequestParam String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.fetchIndividualPaymentRecords(studentId, levelId, semesterId, staffId);
    }

    @PutMapping("/v1/update-payment-details")
    public ResponseEntity<?> updatePaymentRecords(@RequestHeader("staffId") String Id,
                                                  @Valid @RequestBody StudentPaymentRecords update) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.updatePaymentRecord(update, staffId);
    }

    @DeleteMapping("/v1/delete-payment-record/{transactionId}")
    public ResponseEntity<?> deletePaymentRecord(@RequestHeader("staffId") String Id,
                                                 @PathVariable String transactionId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.deletePaymentRecord(transactionId, staffId);
    }

    @GetMapping("/v1/search-fees-report")
    public ResponseEntity<?> searchStudentFeesReport(@RequestHeader("staffId") String Id,
                                                     @RequestParam String studentId,
                                                     @RequestParam String semesterId,
                                                     @RequestParam String gradeId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.findStudentPaymentRecords(studentId, semesterId, gradeId, staffId);
    }

    @GetMapping("/v1/fetch-grade-fees-report/{levelId}/{semesterId}")
    public ResponseEntity<?> fetchGradesFeesReport(@RequestHeader("staffId") String Id,
                                                   @PathVariable String levelId,
                                                   @PathVariable String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.fetchGradeFeesReport(levelId, semesterId, staffId);
    }

    @GetMapping("/v1/class-summary-fees")
    public ResponseEntity<?> getClassSummaryFees(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.loadClassFeesSummary(staffId);
    }

    @GetMapping("/v1/total-fees")
    public ResponseEntity<?> totalSemesterFees(@RequestHeader("staffId") String Id) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.getTotalSemesterFees(staffId);
    }

    @GetMapping("/v1/fees-paid")
    public ResponseEntity<?> totalAmountPaid(@RequestHeader("staffId") String Id) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.getTotalFeesPaid(staffId);
    }

    @GetMapping("/v1/fetch-fees-details/{semesterId}/{levelId}")
    public ResponseEntity<?> fetchFeesDetails(@RequestHeader("staffId") String Id,
                                              @PathVariable String semesterId,
                                              @PathVariable String levelId) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.fetchFeesDetails(semesterId, levelId, staffId);
    }

    @PutMapping("/v2/update-semester-fees")
    public ResponseEntity<?> updateFeesAmount(@RequestHeader("staffId") String Id,
                                              @Valid @RequestBody FetchFeesDetails fetchFeesDetails) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.updateSemesterFees(fetchFeesDetails, staffId);
    }

    @GetMapping("/v1/recent-fees-transactions")
    public ResponseEntity<?> getRecentPayment(@RequestHeader("staffId") String Id) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.getRecentPayments(staffId);
    }

    @PostMapping("/v1/add-new-expenses")
    public ResponseEntity<?> addNewExpenses(@Valid @RequestBody NewExpenses newExpenses) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return expensesService.addNewExpenses(newExpenses, staffId);
    }

    @GetMapping("/v1/read-expenses-records/{semesterId}")
    public ResponseEntity<?> readExpensesRecords(@PathVariable String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return expensesService.readExpenses(semesterId, staffId);
    }

    @GetMapping("/v1/total-expenses")
    public ResponseEntity<?> totalSemesterExpenses() {

        String staffId = authenticatedStaffProvider.getStaffId();

        return expensesService.totalSemesterExpenses(staffId);
    }

    @PatchMapping("/v1/update-expense-record")
    public ResponseEntity<?> updateExpensesRecord(@Valid @RequestBody UpdateExpensesRecord updateExpensesRecord) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return expensesService.updateExpenseRecord(updateExpensesRecord, staffId);
    }

    @PostMapping("/v1/add-special-fee")
    public ResponseEntity<?> addSpecialPayment(@Valid @RequestBody NewSpecialPayment newSpecialPayment) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return specialPaymentService.addSpecialPayment(newSpecialPayment, staffId);
    }

    @GetMapping("/v1/load-special-fees/{paymentType}")
    public ResponseEntity<?> loadSpecialPayments(@PathVariable String paymentType) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return specialPaymentService.loadSpecialPayments(paymentType, staffId);
    }

    @PatchMapping("/v1/update-special-fee")
    public ResponseEntity<?> updateSpecialPayment(@Valid @RequestBody UpdateSpecialPayment updateSpecialPayment) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return specialPaymentService.updateSpecialPayment(updateSpecialPayment, staffId);
    }

    @GetMapping("/v1/generate-expenses-report/{semesterId}")
    public ResponseEntity<?> generateExpensesReport(@PathVariable String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return expensesService.generateExpensesReport(semesterId, staffId);
    }
}