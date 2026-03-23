package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.AddNewSemester;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindAndUpdateClassInfo;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindSemester;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import com.codewithben.schoolmanagementsystem.Utility.JwtUtility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final StaffService staffService;

    private final LevelService levelService;

    private final InstitutionService institutionService;

    private final StudentService studentService;

    private final LevelRepository levelRepository;

    private final ReportService reportService;

    private final JasperReportService jasperReportService;

    public AdminController(StaffService staffService, LevelService levelService, InstitutionService institutionService,
                           StudentService studentService, LevelRepository levelRepository, ReportService reportService,
                           JasperReportService jasperReportService) {
        this.staffService = staffService;
        this.levelService = levelService;
        this.institutionService = institutionService;
        this.studentService = studentService;
        this.levelRepository = levelRepository;
        this.reportService = reportService;
        this.jasperReportService = jasperReportService;
    }

    @PostMapping("/v1/reset-staff-password")
    public ResponseEntity<?> recoverStaffPassword(@RequestParam String staffId,
                                                  @RequestParam String newPasswordStaffId,
                                                  @RequestParam String newPassword) {
        try {

            return ResponseEntity.ok(
                    staffService.resetStaffPassword(newPasswordStaffId, newPassword)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/v1/add-semester")
    public ResponseEntity<?> addNewSemester(@RequestBody AddNewSemester addNewSemester) {
        String staffId = addNewSemester.getStaffId();
        String semesterName = addNewSemester.getSemesterName();
        LocalDate startDate = LocalDate.parse(addNewSemester.getStartDate());
        LocalDate endDate = LocalDate.parse(addNewSemester.getEndDate());
        String academicYear = addNewSemester.getAcademicYear();

        try {
            return ResponseEntity.ok(
                    levelService.addNewSemester(semesterName, startDate, endDate, academicYear, staffId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/v1/set-grades")
    public ResponseEntity<?> setGradingCriteria(@RequestBody GradingCriteria gradingCriteria) {
        try {
            return ResponseEntity.ok(
                    institutionService.setGradingCriteria(gradingCriteria)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/find-class-info/{levelId}")
    public ResponseEntity<?> findClassInfo(@PathVariable String levelId) {
        try {
            return ResponseEntity.ok(levelService.findClassInfo(
                    levelId
            ));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/v1/update-class-info")
    public ResponseEntity<?> updateClassInfo(@RequestBody FindAndUpdateClassInfo updateInfo) {
        try {
            return ResponseEntity.ok(
                    levelService.updateClassInfo(updateInfo)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/v1/update-staff-info")
    public ResponseEntity<?> updateStaffInfo(@RequestBody FindStaffDTO info) {
        try {
            return ResponseEntity.ok(
                    staffService.updateStaffInfo(info)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/v1/search-semester/{semesterId}")
    public ResponseEntity<?> searchSemesterInfo(@PathVariable String semesterId) {
        try {
            return ResponseEntity.ok(
                    levelService.findSemesterInfo(semesterId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/v1/update-semester-info")
    public ResponseEntity<?> updateSemesterInfo(@RequestBody FindSemester updateInfo) {
        try {
            return ResponseEntity.ok(
                    levelService.updateSemesterInfo(updateInfo)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/v1/add-new-class")
    public ResponseEntity<?> addNewGrade(@RequestParam String gradeName,
                                         @RequestParam String instructorId) {
        try {
            return ResponseEntity.ok(
                    levelService.addNewClass(gradeName, instructorId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/v1/add-subject")
    public ResponseEntity<?> saveNewSubject(@RequestBody AddNewSubject addNewSubject) {
        String subjectName = addNewSubject.getSubjectName();
        String levelId = addNewSubject.getGradeId();
        System.out.println("DEBUG: Hit the Add Subject Endpoint!");

        try {
            //Response contains subject Id
            String response = staffService.addNewSubjects(subjectName, levelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/v1/load-subject-data/{staffId}/{subjectId}")
    public ResponseEntity<?> loadSubjectData(@PathVariable String staffId,
                                             @PathVariable String subjectId) {
        return staffService.loadSubjectData(subjectId);
    }

    @PutMapping("/v1/update-subject-details")
    public ResponseEntity<?> updateSubjectData(@RequestBody SubjectDTO subjectDTO) {
        return staffService.updateSubjectData(subjectDTO);
    }

    @DeleteMapping("/v1/delete-subject/{staffId}/{subjectId}")
    public ResponseEntity<?> deleteSubjectData(@PathVariable String staffId,
                                               @PathVariable String subjectId) {

        return staffService.deleteSubjectData(subjectId);
    }

    @GetMapping(
            value = "/v2/generate-class-report",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<?> generateStudentReports(@RequestParam String staffId,
                                                    @RequestParam String levelId,
                                                    @RequestParam String semesterId,
                                                    @RequestParam String promotionGradeId,
                                                    @RequestParam String passScore) {
        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            return ResponseEntity.badRequest().body("Level not found");
        }
        try {
            List<GenerateStudentReport> reportData = reportService.generateClassReports(levelId, semesterId,
                    promotionGradeId);

            byte[] reportPDF = jasperReportService.generateClassReport(reportData, level.getInstitution().getInstitutionId());

            return ResponseEntity.ok().body(reportPDF);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


}
