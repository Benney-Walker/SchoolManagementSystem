package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Fees.FetchFeesDetails;
import com.codewithben.schoolmanagementsystem.DTO.Fees.NewFeesPaymentDTO;
import com.codewithben.schoolmanagementsystem.DTO.Fees.StudentPaymentRecords;
import com.codewithben.schoolmanagementsystem.Service.FeesService;
import com.codewithben.schoolmanagementsystem.Utility.AuthenticatedStaffProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FeesService feesService;

    private final AuthenticatedStaffProvider authenticatedStaffProvider;


    @PostMapping("/v1/add-new-fees")
    public ResponseEntity<?> addNewFees(@RequestHeader("staffId") String Id,
                                     @RequestParam String gradeId,
                                     @RequestParam String semesterId,
                                     @RequestParam String feesAmount) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.addNewSemesterFees(Double.parseDouble(feesAmount), semesterId, gradeId, staffId);
    }

    @PostMapping("/v2/new-fees")
    public ResponseEntity<?> newFees(@RequestHeader("staffId") String Id,
                                     @RequestParam String classId,
                                     @RequestParam String semesterId,
                                     @RequestParam String feesAmount) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.addNewSemesterFees(Double.parseDouble(feesAmount), semesterId, classId, staffId);
    }

    @PostMapping("/v1/add-fees-payment")
    public ResponseEntity<?> addNewFeePayment(@RequestHeader("staffId") String Id,
                                              @RequestBody NewFeesPaymentDTO data) {
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
                                                  @RequestBody StudentPaymentRecords update) {

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
                                              @RequestBody FetchFeesDetails fetchFeesDetails) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.updateSemesterFees(fetchFeesDetails, staffId);
    }

    @GetMapping("/v1/recent-fees-transactions")
    public ResponseEntity<?> getRecentPayment(@RequestHeader("staffId") String Id) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return feesService.getRecentPayments(staffId);
    }
}
