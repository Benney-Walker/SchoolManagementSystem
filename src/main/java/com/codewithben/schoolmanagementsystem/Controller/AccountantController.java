package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Account.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FetchFeesDetails;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.FeesService;
import com.codewithben.schoolmanagementsystem.Service.StudentService;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class AccountantController {
    private final FeesService feesService;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final StaffsRepository staffsRepository;

    private final StudentService studentService;

    public AccountantController(FeesService feesService, UtilityClass utilityClass, InstitutiionRepository institutiionRepository,
                                StaffsRepository staffsRepository, StudentService studentService) {

        this.feesService = feesService;
        this.utilityClass = utilityClass;
        this.institutiionRepository = institutiionRepository;
        this.staffsRepository = staffsRepository;
        this.studentService = studentService;
    }

    @PostMapping("v2/add-new-fees")
    public ResponseEntity<?> addNewFees(@RequestParam String gradeId,
                                        @RequestParam String semesterId,
                                        @RequestParam String feesAmount) {

        try {
            String response = feesService.addNewSemesterFees(Double.parseDouble(feesAmount), semesterId, gradeId);
            return ResponseEntity.ok().body(response);
        } catch  (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("v2/add-fees-payment")
    public ResponseEntity<?> addNewFeePayment(@RequestBody NewFeesPaymentDTO newFeesPaymentDTO) {
        String studentId = newFeesPaymentDTO.getStudentId();
        Double amountPaid = newFeesPaymentDTO.getAmountPaid();
        String personWhoPaid = newFeesPaymentDTO.getPayerName();
        String phoneNumber = newFeesPaymentDTO.getPayerPhone();
        String semesterId = newFeesPaymentDTO.getSemesterId();

        try {
            return ResponseEntity.ok(
                    feesService.addNewFeePayment(studentId, amountPaid, personWhoPaid, phoneNumber, semesterId)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("v2/fetch-payment-records/{studentId}/{semesterId}")
    public ResponseEntity<?> fetchPaymentRecords(@PathVariable String studentId,
                                                 @PathVariable String semesterId) {
        try {
            return ResponseEntity.ok(
                    feesService.fetchPaymentRecords(studentId, semesterId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("v2/update-payment-details")
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

    @DeleteMapping("v2/delete-payment-record/{transactionId}")
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

    @GetMapping("v2/get-student-name/{studentId}")
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

    @GetMapping("v2/search-fees-report")
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

    @GetMapping("v2/class-summary-fees/{staffId}")
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

    @GetMapping("v2/fully-paid-students/{staffId}")
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

    @GetMapping("v2/partially-paid-students/{staffId}")
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

    @GetMapping("v2/not-paid-students/{staffId}")
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

    @GetMapping("v2/total-fees/{staffId}")
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

    @GetMapping("v2/fees-paid/{staffId}")
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

    @GetMapping("v2/fetch-fees-details/{semesterId}/{levelId}")
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

    @PutMapping("v2/update-semester-fees")
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

    @GetMapping("v1/recent-fees-transactions/{staffId}")
    public ResponseEntity<?> getRecentPayment(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok().body(feesService.getRecentPayments(staffId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
