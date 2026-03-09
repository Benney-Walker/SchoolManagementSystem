package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Account.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FetchFeesDetails;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.AdminService;
import com.codewithben.schoolmanagementsystem.Service.FeesService;
import com.codewithben.schoolmanagementsystem.Service.StudentService;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class AccountantController {
    private final FeesService feesService;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final StaffsRepository staffsRepository;

    private final StudentService studentService;

    private final AdminService adminService;

    public AccountantController(FeesService feesService, UtilityClass utilityClass, InstitutiionRepository institutiionRepository,
                                StaffsRepository staffsRepository, StudentService studentService, AdminService adminService) {

        this.feesService = feesService;
        this.utilityClass = utilityClass;
        this.institutiionRepository = institutiionRepository;
        this.staffsRepository = staffsRepository;
        this.studentService = studentService;
        this.adminService = adminService;
    }

    @PostMapping("/v1/add-new-fees")
    public ResponseEntity<?> addNewFees(@RequestParam String gradeId,
                                        @RequestParam String semesterId,
                                        @RequestParam String feesAmount) {

        try {
            return ResponseEntity.ok().body(
                    feesService.addNewSemesterFees(Double.parseDouble(feesAmount), semesterId, gradeId)
            );
        } catch  (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/v2/add-fees-payment")
    public ResponseEntity<?> addNewFeePayment(@RequestBody NewFeesPaymentDTO data) {
        String studentId = data.getStudentId();
        Double amountPaid = data.getAmountPaid();
        String personWhoPaid = data.getPayerName();
        String phoneNumber = data.getPayerPhone();
        String semesterId = data.getSemesterId();

        Staffs staff = staffsRepository.findByStaffId(data.getStaffId()).orElse(null);

        try {

            return ResponseEntity.ok(
                    feesService.addNewFeePayment(studentId, amountPaid, personWhoPaid, phoneNumber, semesterId)
            );
        } catch (Exception e) {
            adminService.logSystemActivities(LogType.TRANSACTION, LogStatus.FAILED,
                    e.getMessage(), staff);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/v1/fetch-payment-records")
    public ResponseEntity<?> fetchPaymentRecords(@RequestParam String studentId,
                                                 @RequestParam String semesterId) {

        try {

            return ResponseEntity.ok(
                    feesService.fetchPaymentRecords(studentId, semesterId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/v1/update-payment-details")
    public ResponseEntity<?> updatePaymentRecords(@RequestBody StudentPaymentRecords update) {
        try {
            return ResponseEntity.ok(
                    feesService.updatePaymentRecords(update)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/v1/delete-payment-record/{transactionId}")
    public ResponseEntity<?> deletePaymentRecord(@PathVariable String transactionId) {
        try {
            return ResponseEntity.ok(
                    feesService.deletePaymentRecord(transactionId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v2/get-student-name/{studentId}")
    public ResponseEntity<?> getStudentName(@PathVariable String studentId) {
        try {
            return ResponseEntity.ok(
                    studentService.getStudentName(studentId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/search-fees-report")
    public ResponseEntity<?> searchStudentFeesReport(@RequestParam String studentId,
                                                     @RequestParam String semesterId,
                                                     @RequestParam String gradeId) {

        try {

            return ResponseEntity.ok(
                    feesService.findStudentFeesPaymentDetails(studentId, semesterId, gradeId)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/v1/fetch-grade-fees-report/{levelId}/{semesterId}")
    public ResponseEntity<?> fetchGradesFeesReport(@PathVariable String levelId,
                                                   @PathVariable String semesterId) {
        try {
            return ResponseEntity.ok(
                    feesService.fetchGradeFeesReport(levelId, semesterId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/class-summary-fees/{staffId}")
    public ResponseEntity<?> getClassSummaryFees(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    feesService.loadClassFeesSummary(staffId)
            );
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/fully-paid-students/{staffId}")
    public ResponseEntity<?> getFullyPaidStudents(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    feesService.getFullyPaidStudents(staffId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/partially-paid-students/{staffId}")
    public ResponseEntity<?> getPartiallyPaidStudents(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    feesService.getPartiallyPaidStudents(staffId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/not-paid-students/{staffId}")
    public ResponseEntity<?> getNotPaidStudents(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    feesService.getNotPaidStudents(staffId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/total-fees/{staffId}")
    public ResponseEntity<?> totalSemesterFees(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            return ResponseEntity.badRequest().body("Staff not found");

        Institution institution = staff.getInstitution();
        if (institution == null)
            return ResponseEntity.badRequest().body("Institution not found");

        String semesterId = utilityClass.getCurrentSemesterId(institution.getInstitutionId());

        List<Level> levels = institution.getLevel();
        double totalSemesterFees = 0;
        for (Level level : levels) {
            double levelFees = feesService.getTotalLevelFees(
                    level.getLevelID(), semesterId, level.getStudents().size()
            );
            totalSemesterFees += levelFees;
        }
        return ResponseEntity.ok(String.valueOf(totalSemesterFees));
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
        List<Level> levels = institution.getLevel();
        double totalAmountPaid = 0;
        for (Level level : levels) {
            double levelFeesPaid = feesService.getTotalLevelFeesPaid(semesterId, level.getLevelID());
            System.out.println("levelFeesPaid: " + levelFeesPaid);
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

    @PutMapping("/v2/update-semester-fees")
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
