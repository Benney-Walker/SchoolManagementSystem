package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.AddNewSemester;
import com.codewithben.schoolmanagementsystem.DTO.Institution.PasswordRecoveryDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StaffCaching;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class PrincipalController {
    private final LevelService levelService;

    private final StaffService staffService;

    private final StudentService studentService;

    private final StaffsRepository staffsRepository;

    private final SemesterRepository semesterRepository;

    private final UtilityClass utilityClass;

    private final InstitutionService institutionService;

    private final ReportService reportService;

    public PrincipalController( LevelService levelService, StaffService staffService, StudentService studentService,
                                StaffsRepository staffsRepository, InstitutionService institutionService,
                                SemesterRepository semesterRepository, UtilityClass utilityClass,
                                ReportService reportService) {
        this.levelService = levelService;
        this.staffService = staffService;
        this.studentService = studentService;
        this.staffsRepository = staffsRepository;
        this.institutionService = institutionService;
        this.semesterRepository = semesterRepository;
        this.utilityClass = utilityClass;
        this.reportService = reportService;
    }

    @GetMapping("v1/recently-added-students/{staffId}")
    public List<RecentStudentsDTO> getRecentlyAddedStudents(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            return new ArrayList<>();
        String institutionId = staff.getInstitution().getInstitutionId();
        String semesterId = utilityClass.getCurrentSemesterId(institutionId);
        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null)
            return new ArrayList<>();
        try {
            return studentService.findRecentlyAddedStudents(semester.getSemesterStartDate(), semester.getSemesterEndDate());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }


    @GetMapping("v2/absent-students/{staffId}")
    public ResponseEntity<?> getAbsentees(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok().body(studentService.findAbsentees(staffId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }

    @GetMapping("v1/load-levels/{staffId}")
    public List<LevelCaching> loadLevelInfo(@PathVariable String staffId) {

        return levelService.loadLevelInfo(staffId);
    }

    @GetMapping("v1/load-staffs-info/{staffId}")
    public List<StaffCaching> getAllStaffInfo(@PathVariable String staffId) {
        if (staffId == null)
            return new ArrayList<>();

        try {
            return staffService.loadAllStaffInfo(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @PostMapping("v1/reset-staff-password/{staffId}/{newPassword}")
    public ResponseEntity<PasswordRecoveryDTO> recoverStaffPassword(@PathVariable String staffId,
                                                                    @PathVariable String newPassword) {
        try {
            String response = staffService.resetStaffPassword(staffId, newPassword);

            return ResponseEntity.ok().body(new PasswordRecoveryDTO("success", response));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(new PasswordRecoveryDTO("failed", ex.getMessage()));
        }
    }

    @GetMapping("v1/load-semesters/{staffId}")
    public List<SemesterCaching> loadSemesterInfo(@PathVariable String staffId) {
        try {
            return levelService.loadSemesterInfo(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @PostMapping("v1/add-semester")
    public ResponseEntity<?> addNewSemester(@RequestBody AddNewSemester addNewSemester) {
        String semesterName = addNewSemester.getSemesterName();
        LocalDate startDate = LocalDate.parse(addNewSemester.getStartDate());
        LocalDate endDate = LocalDate.parse(addNewSemester.getEndDate());
        String academicYear = addNewSemester.getAcademicYear();
        String staffId = addNewSemester.getStaffId();

        try {
            String response = levelService.addNewSemester(semesterName, startDate, endDate, academicYear, staffId);
            return ResponseEntity.ok(Map.of(
                    "status", "success"
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", ex.getMessage()
            ));
        }
    }

    @PostMapping("v1/set-grades")
    public ResponseEntity<String> setGradingCriteria(@RequestBody GradingCriteria gradingCriteria) {
        try {
            String response = institutionService.setGradingCriteria(gradingCriteria);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v1/principal-view-class-results/{levelId}/{semesterId}")
    public ResponseEntity<?> viewClassSemesterResults(@PathVariable String levelId,
                                                      @PathVariable String semesterId) {

        try {
            List<GenerateStudentReport> reportData = reportService.generateClassReports(levelId, semesterId, null);
            return ResponseEntity.ok().body(reportData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
