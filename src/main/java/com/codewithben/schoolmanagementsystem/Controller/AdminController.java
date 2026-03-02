package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.AddNewSubject;
import com.codewithben.schoolmanagementsystem.DTO.Academics.GenerateStudentReport;
import com.codewithben.schoolmanagementsystem.DTO.Academics.GradingCriteria;
import com.codewithben.schoolmanagementsystem.DTO.Academics.UpdateStudentPersonalData;
import com.codewithben.schoolmanagementsystem.DTO.Institution.AddNewSemester;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindAndUpdateClassInfo;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindSemester;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
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

    @PostMapping("/v2/reset-staff-password/{staffId}/{newPassword}")
    public ResponseEntity<?> recoverStaffPassword(@PathVariable String staffId,
                                                  @PathVariable String newPassword) {
        try {

            return ResponseEntity.ok(
                    staffService.resetStaffPassword(staffId, newPassword)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/v1/add-semester")
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

    @PostMapping("/v2/set-grades")
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

    @GetMapping("/v2/find-class-info/{searchParameter}/{staffId}")
    public ResponseEntity<?> findClassInfo(@PathVariable String searchParameter,
                                           @PathVariable String staffId) {
        try {
            return ResponseEntity.ok(levelService.findClassInfo(
                    searchParameter, staffId
            ));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/v2/update-class-info")
    public ResponseEntity<?> updateClassInfo(@RequestBody FindAndUpdateClassInfo updateInfo) {
        try {
            return ResponseEntity.ok(
                    levelService.updateClassInfo(updateInfo)
            );
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/v2/update-staff-info")
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

    @GetMapping("/v2/search-semester/{semesterId}")
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

    @PutMapping("/v2/update-semester-info")
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

    @PostMapping("/v2/add-new-grade")
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

    @PostMapping("/v2/add-subject")
    public ResponseEntity<?> saveNewSubject(@RequestBody AddNewSubject addNewSubject) {
        String subjectName = addNewSubject.getSubjectName();
        String levelId = addNewSubject.getGradeId();

        try {
            //Response contains subject Id
            String response = staffService.addNewSubjects(subjectName, levelId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(
            value = "/v2/generate-class-report/{levelId}/{semesterId}/{promotionGradeId}",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<?> generateStudentReports(@PathVariable String levelId,
                                                    @PathVariable String semesterId,
                                                    @PathVariable String promotionGradeId) {
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

    @PutMapping("/v2/update-student-data")
    public ResponseEntity<?> updateStudentData(@RequestBody UpdateStudentPersonalData updateStudentPersonalData) {

        try {
            String response = studentService.updateStudentPersonalData(updateStudentPersonalData);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
