package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Account.*;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.FeesService;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AccountantController {
    private final FeesService feesService;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final StaffsRepository staffsRepository;

    public AccountantController(FeesService feesService, UtilityClass utilityClass, InstitutiionRepository institutiionRepository,
                                StaffsRepository staffsRepository) {

        this.feesService = feesService;
        this.utilityClass = utilityClass;
        this.institutiionRepository = institutiionRepository;
        this.staffsRepository = staffsRepository;
    }

    @PostMapping("/add-new-fees")
    public ResponseEntity<?> addNewFees(@RequestParam String gradeId,
                                        @RequestParam String semesterId,
                                        @RequestParam String feesAmount) {

        try {
            String response = feesService.addNewSemesterFees(Double.parseDouble(feesAmount), semesterId, gradeId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Fees successfully added."
            ));
        } catch  (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/add-fees-payment")
    public ResponseEntity<?> addNewFeePayment(@RequestBody NewFeesPaymentDTO newFeesPaymentDTO) {
        String studentId = newFeesPaymentDTO.getStudentId();
        Double amountPaid = newFeesPaymentDTO.getAmountPaid();
        String personWhoPaid = newFeesPaymentDTO.getPayerName();
        String phoneNumber = newFeesPaymentDTO.getPayerPhone();
        String semesterId = newFeesPaymentDTO.getSemesterId();
        String levelID = newFeesPaymentDTO.getGradeId();

        try {
            String response = feesService.addNewFeePayment(studentId, amountPaid, personWhoPaid, phoneNumber, semesterId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Payment successfully added."
            ));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/search-fees-report")
    public ResponseEntity<StudentFeesPaymentDisplay> searchStudentFeesReport(@RequestParam String studentId,
                                                             @RequestParam String semesterId,
                                                             @RequestParam String gradeId) {
        try {
            StudentFeesPaymentDisplay paymentHistory = feesService.findStudentFeesPaymentDetails(studentId, gradeId, semesterId);
            return ResponseEntity.ok(paymentHistory);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/class-summary-fees/{staffId}")
    public List<ClassFeesSummary> getClassSummaryFees(@PathVariable String staffId) {
        try {
            return feesService.loadClassFeesSummary(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @GetMapping("/fully-paid-students/{staffId}")
    public List<FeesSummaryTable> getFullyPaidStudents(@PathVariable String staffId) {
        try {
            return feesService.getFullyPaidStudents(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @GetMapping("/partially-paid-students/{staffId}")
    public List<FeesSummaryTable> getPartiallyPaidStudents(@PathVariable String staffId) {
        try {
            return feesService.getPartiallyPaidStudents(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @GetMapping("/not-paid-students/{staffId}")
    public List<FeesSummaryTable> getNotPaidStudents(@PathVariable String staffId) {
        try {
            return feesService.getNotPaidStudents(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @GetMapping("/total-fees/{staffId}")
    public double totalSemesterFees(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            return 0;

        Institution institution = staff.getInstitution();
        if (institution == null)
            return 0;
        String semesterId = utilityClass.getCurrentSemester(institution.getInstitutionId());

        List<Level> levels = institution.getLevel();
        double totalSemesterFees = 0;
        for (Level level : levels) {
            double levelFees = feesService.getTotalLevelFees(level.getLevelID(), semesterId);
            totalSemesterFees += levelFees;
        }
        return totalSemesterFees;
    }

    @GetMapping("/fees-paid/{staffId}")
    public double totalAmountPaid(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            return 0;

        Institution institution = staff.getInstitution();
        if (institution == null)
            return 0;

        String semesterId = utilityClass.getCurrentSemester(institution.getInstitutionId());
        List<Level> levels = institution.getLevel();
        double totalAmountPaid = 0;
        for (Level level : levels) {
            double levelFeesPaid = feesService.getTotalLevelFeesPaid(level.getLevelID(), semesterId);
            totalAmountPaid += levelFeesPaid;
        }
        return totalAmountPaid;
    }
}
