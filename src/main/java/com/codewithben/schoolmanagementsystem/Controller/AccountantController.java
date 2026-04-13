package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Account.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FetchFeesDetails;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.FeesService;
import com.codewithben.schoolmanagementsystem.Service.LoggingService;
import com.codewithben.schoolmanagementsystem.Service.StudentService;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
public class AccountantController {
    private final FeesService feesService;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final StaffsRepository staffsRepository;

    private final StudentService studentService;

    private final LoggingService loggingService;

    public AccountantController(FeesService feesService, UtilityClass utilityClass, InstitutiionRepository institutiionRepository,
                                StaffsRepository staffsRepository, StudentService studentService, LoggingService loggingService) {

        this.feesService = feesService;
        this.utilityClass = utilityClass;
        this.institutiionRepository = institutiionRepository;
        this.staffsRepository = staffsRepository;
        this.studentService = studentService;
        this.loggingService = loggingService;
    }

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

    @GetMapping("/v1/class-summary-fees/{staffId}")
    public ResponseEntity<?> getClassSummaryFees(@PathVariable String staffId) {

        return feesService.loadClassFeesSummary(staffId);
    }

    @GetMapping("/v1/fully-paid-students/{staffId}")
    public ResponseEntity<?> getFullyPaidStudents(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity("FETCH_PAYMENTS_REPORT", "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staffId");
        }
        return feesService.getFullyPaidStudents(staffId, staff.getInstitution());
    }

    @GetMapping("/v1/partially-paid-students/{staffId}")
    public ResponseEntity<?> getPartiallyPaidStudents(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity("FETCH_PAYMENTS_REPORT", "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staffId");
        }
        return feesService.getPartiallyPaidStudents(staffId, staff.getInstitution());
    }

    @GetMapping("/v1/not-paid-students/{staffId}")
    public ResponseEntity<?> getNotPaidStudents(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity("FETCH_PAYMENTS_REPORT", "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staffId");
        }
        return feesService.getNotPaidStudents(staffId, staff.getInstitution());
    }

    @GetMapping("/v1/total-fees/{staffId}")
    public ResponseEntity<?> totalSemesterFees(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity("FETCH_PAYMENTS_REPORT", "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staffId");
        }
        return feesService.getTotalSemesterFees(staffId, staff.getInstitution());
    }

    @GetMapping("/v1/fees-paid/{staffId}")
    public ResponseEntity<?> totalAmountPaid(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            return ResponseEntity.badRequest().body("Staff not found");

        Institution institution = staff.getInstitution();
        if (institution == null)
            return ResponseEntity.badRequest().body("Institution not found");

        String semesterId = utilityClass.getCurrentSemesterId(institution.getInstitutionId());
        List<Level> levels = institution.getLevels();
        double totalAmountPaid = 0;
        for (Level level : levels) {
            double levelFeesPaid = feesService.getTotalLevelFeesPaid(semesterId, level.getLevelID());
            totalAmountPaid += levelFeesPaid;
        }
        return ResponseEntity.ok(String.valueOf(totalAmountPaid));
    }

    @GetMapping("/v1/fetch-fees-details/{semesterId}/{levelId}")
    public ResponseEntity<?> fetchFeesDetails(@PathVariable String semesterId,
                                              @PathVariable String levelId) {
        try {
            return ResponseEntity.ok(
                    feesService.fetchFeesDetails(semesterId, levelId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/v1/update-semester-fees")
    public ResponseEntity<?> updateFeesAmount(@RequestBody FetchFeesDetails fetchFeesDetails) {
        try {
            return ResponseEntity.ok(
                    feesService.updateSemesterFees(fetchFeesDetails)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/v1/recent-fees-transactions/{staffId}")
    public ResponseEntity<?> getRecentPayment(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok().body(feesService.getRecentPayments(staffId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
