package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Fees.FetchFeesDetails;
import com.codewithben.schoolmanagementsystem.DTO.Fees.NewFeesPaymentDTO;
import com.codewithben.schoolmanagementsystem.DTO.Fees.StudentPaymentRecords;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.FeesService;
import com.codewithben.schoolmanagementsystem.Service.LoggingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/finance")
public class AccountantController {
    private final FeesService feesService;

    private final LoggingService loggingService;

    private final StaffsRepository staffsRepository;

    @PostMapping("/v1/add-new-fees")
    public ResponseEntity<?> addNewFees(@RequestHeader("staffId") String staffId,
                                        @RequestParam String gradeId,
                                        @RequestParam String semesterId,
                                        @RequestParam String feesAmount) {

        return feesService.addNewSemesterFees(Double.parseDouble(feesAmount), semesterId, gradeId, staffId);
    }

    @PostMapping("/v2/add-fees-payment")
    public ResponseEntity<?> addNewFeePayment(@RequestHeader("staffId") String staffId,
                                              @RequestBody NewFeesPaymentDTO data) {
        String studentId = data.getStudentId();
        Double amountPaid = data.getAmountPaid();
        String personWhoPaid = data.getPayerName();
        String phoneNumber = data.getPayerPhone();
        String levelId = data.getLevelId();
        String semesterId = data.getSemesterId();

        return feesService.addNewFeePayment(studentId, amountPaid, personWhoPaid, phoneNumber, levelId, semesterId, staffId);
    }

    @GetMapping("/v1/fetch-payment-records")
    public ResponseEntity<?> fetchPaymentRecords(@RequestHeader("StaffId") String staffId,
                                                 @RequestParam String studentId,
                                                 @RequestParam String levelId,
                                                 @RequestParam String semesterId) {

        return feesService.fetchIndividualPaymentRecords(studentId, levelId, semesterId, staffId);
    }

    @PutMapping("/v1/update-payment-details")
    public ResponseEntity<?> updatePaymentRecords(@RequestHeader("staffId") String staffId,
                                                  @RequestBody StudentPaymentRecords update) {

        return feesService.updatePaymentRecords(update, staffId);
    }

    @DeleteMapping("/v1/delete-payment-record/{transactionId}")
    public ResponseEntity<?> deletePaymentRecord(@RequestHeader("staffId") String staffId,
                                                 @PathVariable String transactionId) {

        return feesService.deletePaymentRecord(transactionId, staffId);
    }

    @GetMapping("/v1/search-fees-report")
    public ResponseEntity<?> searchStudentFeesReport(@RequestHeader("staffId") String staffId,
                                                     @RequestParam String studentId,
                                                     @RequestParam String semesterId,
                                                     @RequestParam String gradeId) {

        return feesService.findStudentFeesPaymentDetails(studentId, semesterId, gradeId, staffId);
    }

    @GetMapping("/v1/fetch-grade-fees-report/{levelId}/{semesterId}")
    public ResponseEntity<?> fetchGradesFeesReport(@RequestHeader("staffId") String staffId,
                                                   @PathVariable String levelId,
                                                   @PathVariable String semesterId) {
        return feesService.fetchGradeFeesReport(levelId, semesterId, staffId);
    }

    @GetMapping("/v1/class-summary-fees")
    public ResponseEntity<?> getClassSummaryFees(@RequestHeader("staffId") String staffId) {

        return feesService.loadClassFeesSummary(staffId);
    }

    @GetMapping("/v1/fully-paid-students")
    public ResponseEntity<?> getFullyPaidStudents(@RequestHeader("staffId") String staffId) {
        Staffs staff = getValidatedStaff(staffId);
        if (staff == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");

        return feesService.getFullyPaidStudents(staffId, staff.getInstitution());
    }

    @GetMapping("/v1/partially-paid-students")
    public ResponseEntity<?> getPartiallyPaidStudents(@RequestHeader("staffId") String staffId) {
        Staffs staff = getValidatedStaff(staffId);
        if (staff == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");

        return feesService.getPartiallyPaidStudents(staffId, staff.getInstitution());
    }

    @GetMapping("/v1/not-paid-students")
    public ResponseEntity<?> getNotPaidStudents(@RequestHeader("staffId") String staffId) {
        Staffs staff = getValidatedStaff(staffId);
        if (staff == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");

        return feesService.getNotPaidStudents(staffId, staff.getInstitution());
    }

    @GetMapping("/v1/total-fees")
    public ResponseEntity<?> totalSemesterFees(@RequestHeader("staffId") String staffId) {


        return feesService.getTotalSemesterFees(staffId);
    }

    @GetMapping("/v1/fees-paid")
    public ResponseEntity<?> totalAmountPaid(@RequestHeader("staffId") String staffId) {


        return feesService.getTotalFeesPaid(staffId);
    }

    @GetMapping("/v1/fetch-fees-details/{semesterId}/{levelId}")
    public ResponseEntity<?> fetchFeesDetails(@RequestHeader("staffId") String staffId,
                                              @PathVariable String semesterId,
                                              @PathVariable String levelId) {

        return feesService.fetchFeesDetails(semesterId, levelId, staffId);
    }

    @PutMapping("/v1/update-semester-fees")
    public ResponseEntity<?> updateFeesAmount(@RequestHeader("staffId") String staffId,
                                              @RequestBody FetchFeesDetails fetchFeesDetails) {

        return feesService.updateSemesterFees(fetchFeesDetails, staffId);
    }

    @GetMapping("/v1/recent-fees-transactions")
    public ResponseEntity<?> getRecentPayment(@RequestHeader("staffId") String staffId) {
        Staffs staff = getValidatedStaff(staffId);
        if (staff == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");

        return feesService.getRecentPayments(staffId, staff.getInstitution());
    }

    private Staffs getValidatedStaff(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.FETCH_REPORT, "N/A", staffId, "FAILED");
            return null;
        }
        return staff;
    }
}
