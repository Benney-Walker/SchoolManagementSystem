package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Result.GradingCriteria;
import com.codewithben.schoolmanagementsystem.DTO.Semester.AddNewSemester;
import com.codewithben.schoolmanagementsystem.DTO.Class.FindAndUpdateClassInfo;
import com.codewithben.schoolmanagementsystem.DTO.Semester.FindSemester;
import com.codewithben.schoolmanagementsystem.DTO.Staff.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Subject.AddNewSubject;
import com.codewithben.schoolmanagementsystem.DTO.Subject.SubjectDTO;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final StaffService staffService;

    private final LevelService levelService;

    private final InstitutionService institutionService;

    private final StudentService studentService;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final ReportService reportService;

    private final JasperReportService jasperReportService;

    private final LoggingService loggingService;



    @PostMapping("/v1/reset-staff-password")
    public ResponseEntity<?> recoverStaffPassword(@RequestHeader("staffId") String staffId,
                                                  @RequestParam String newPasswordStaffId,
                                                  @RequestParam String newPassword) {

        return staffService.resetStaffPassword(newPasswordStaffId, newPassword, staffId);
    }

    @PostMapping("/v1/add-semester")
    public ResponseEntity<?> addNewSemester(@RequestHeader("staffId") String staffId,
                                            @RequestBody AddNewSemester addNewSemester) {

        String semesterName = addNewSemester.getSemesterName();
        LocalDate startDate = LocalDate.parse(addNewSemester.getStartDate());
        LocalDate endDate = LocalDate.parse(addNewSemester.getEndDate());
        String academicYear = addNewSemester.getAcademicYear();

        return levelService.addNewSemester(semesterName, startDate, endDate, academicYear, staffId);
    }

    @PostMapping("/v1/new-grading-criteria")
    public ResponseEntity<?> setGradingCriteria(@RequestHeader("staffId") String staffId,
                                                @RequestBody GradingCriteria gradingCriteria) {

        return institutionService.saveGradingCriteria(gradingCriteria, staffId);
    }

    @GetMapping("/v1/load-grading-criteria")
    public ResponseEntity<?> loadAllGradingCriteria(@RequestHeader("staffId") String staffId) {

        return institutionService.loadAllGradingCriteria(staffId);
    }

    @GetMapping("/v1/find-class-info/{levelId}")
    public ResponseEntity<?> findClassInfo(@RequestHeader("staffId") String staffId,
                                           @PathVariable String levelId) {

        return levelService.findClassInfo(levelId, staffId);
    }

    @PutMapping("/v1/update-class-info")
    public ResponseEntity<?> updateClassInfo(@RequestHeader("staffId") String staffId,
                                             @RequestBody FindAndUpdateClassInfo updateInfo) {

        return levelService.updateClassInfo(updateInfo, staffId);
    }

    @PutMapping("/v1/update-staff-info")
    public ResponseEntity<?> updateStaffInfo(@RequestHeader("staffId") String staffId,
                                             @RequestBody FindStaffDTO info) {

        return staffService.updateStaffInfo(info, staffId);
    }

    @GetMapping("/v1/search-semester/{semesterId}")
    public ResponseEntity<?> searchSemesterInfo(@RequestHeader("staffId") String staffId,
                                                @PathVariable String semesterId) {
        return levelService.findSemesterInfo(semesterId, staffId);
    }

    @PutMapping("/v1/update-semester-info")
    public ResponseEntity<?> updateSemesterInfo(@RequestHeader("staffId") String staffId,
                                                @RequestBody FindSemester updateInfo) {

        return levelService.updateSemesterInfo(updateInfo, staffId);
    }

    @PostMapping("/v1/add-new-class")
    public ResponseEntity<?> addNewClass(@RequestHeader("staffId") String staffId,
                                         @RequestParam String gradeName,
                                         @RequestParam String instructorId) {

        return levelService.addNewClass(gradeName, instructorId, staffId);
    }

    @PostMapping("/v1/add-subject")
    public ResponseEntity<?> saveNewSubject(@RequestHeader("staffId") String staffId,
                                            @RequestBody AddNewSubject addNewSubject) {
        String subjectName = addNewSubject.getSubjectName();
        String levelId = addNewSubject.getGradeId();

        return staffService.addNewSubjects(subjectName, levelId, staffId);

    }

    @GetMapping("/v1/load-subject-data/{subjectId}")
    public ResponseEntity<?> loadSubjectData(@RequestHeader("staffId") String staffId,
                                             @PathVariable String subjectId) {

        return staffService.loadSubjectData(subjectId, staffId);
    }

    @PutMapping("/v1/update-subject-details")
    public ResponseEntity<?> updateSubjectData(@RequestHeader("staffId") String staffId,
                                               @RequestBody SubjectDTO subjectDTO) {

        return staffService.updateSubjectData(subjectDTO, staffId);
    }

    @DeleteMapping("/v1/delete-subject/{subjectId}")
    public ResponseEntity<?> deleteSubjectData(@RequestHeader("staffId") String staffId,
                                               @PathVariable String subjectId) {

        return staffService.deleteSubjectData(subjectId, staffId);
    }

    @GetMapping(
            value = "/v2/generate-class-report",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<?> generateStudentReports(@RequestHeader("staffId") String staffId,
                                                    @RequestParam String levelId,
                                                    @RequestParam String semesterId) {

        return reportService.getClassBulkReport(staffId, levelId, semesterId);
    }

    @GetMapping("/v1/recent-logs")
    public ResponseEntity<?> getRecentActivities(@RequestHeader("staffId") String staffId) {

        return loggingService.getRecentActivity(staffId);
    }
}
