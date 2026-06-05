package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Holiday.Holiday;
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

    private final ClassService classService;

    private final InstitutionService institutionService;

    private final StudentService studentService;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final ReportService reportService;

    private final JasperReportService jasperReportService;

    private final LoggingService loggingService;

    private final HolidayService holidayService;

    private final SubjectsService subjectsService;



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

        return classService.addNewSemester(semesterName, startDate, endDate, academicYear, staffId);
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

    @PutMapping("/v1/update-grading-criteria")
    public ResponseEntity<?> updateGradingCriteria(@RequestHeader("staffId") String staffId,
                                                   @RequestBody GradingCriteria gradingCriteria) {

        return institutionService.updateGradingCriteria(gradingCriteria, staffId);
    }

    @GetMapping("/v1/find-class-info/{levelId}")
    public ResponseEntity<?> findClassInfo(@RequestHeader("staffId") String staffId,
                                           @PathVariable String levelId) {

        return classService.findClassInfo(levelId, staffId);
    }

    @PutMapping("/v1/update-class-info")
    public ResponseEntity<?> updateClassInfo(@RequestHeader("staffId") String staffId,
                                             @RequestBody FindAndUpdateClassInfo updateInfo) {

        return classService.updateClassInfo(updateInfo, staffId);
    }

    @PutMapping("/v1/update-staff-info")
    public ResponseEntity<?> updateStaffInfo(@RequestHeader("staffId") String staffId,
                                             @RequestBody FindStaffDTO info) {

        return staffService.updateStaffInfo(info, staffId);
    }

    @GetMapping("/v1/search-semester/{semesterId}")
    public ResponseEntity<?> searchSemesterInfo(@RequestHeader("staffId") String staffId,
                                                @PathVariable String semesterId) {
        return classService.findSemesterInfo(semesterId, staffId);
    }

    @PutMapping("/v1/update-semester-info")
    public ResponseEntity<?> updateSemesterInfo(@RequestHeader("staffId") String staffId,
                                                @RequestBody FindSemester updateInfo) {

        return classService.updateSemesterInfo(updateInfo, staffId);
    }

    @PostMapping("/v1/add-new-class")
    public ResponseEntity<?> addNewClass(@RequestHeader("staffId") String staffId,
                                         @RequestParam String gradeName,
                                         @RequestParam String instructorId) {

        return classService.addNewClass(gradeName, instructorId, staffId);
    }

    @PostMapping("/v1/add-subject")
    public ResponseEntity<?> saveNewSubject(@RequestHeader("staffId") String staffId,
                                            @RequestBody AddNewSubject addNewSubject) {
        String subjectName = addNewSubject.getSubjectName();
        String levelId = addNewSubject.getGradeId();

        return subjectsService.addNewSubjects(subjectName, levelId, staffId);

    }

    @GetMapping("/v1/load-subject-data/{subjectId}")
    public ResponseEntity<?> loadSubjectData(@RequestHeader("staffId") String staffId,
                                             @PathVariable String subjectId) {

        return subjectsService.loadSubjectData(subjectId, staffId);
    }

    @PutMapping("/v1/update-subject-details")
    public ResponseEntity<?> updateSubjectData(@RequestHeader("staffId") String staffId,
                                               @RequestBody SubjectDTO subjectDTO) {

        return subjectsService.updateSubjectData(subjectDTO, staffId);
    }

    @DeleteMapping("/v1/delete-subject/{subjectId}")
    public ResponseEntity<?> deleteSubjectData(@RequestHeader("staffId") String staffId,
                                               @PathVariable String subjectId) {

        return subjectsService.deleteSubjectData(subjectId, staffId);
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

    @GetMapping("/v1/get-staff-logs")
    public ResponseEntity<?> getStaffLogs(@RequestHeader("staffId") String staffId,
                                          @RequestParam String selectedStaff,
                                          @RequestParam String fromDate,
                                          @RequestParam String toDate) {

         return loggingService.getStaffLogsBetween(
                 staffId, selectedStaff, LocalDate.parse(fromDate), LocalDate.parse(toDate)
         );
    }

    @GetMapping("/v1/get-timely-logs")
    public ResponseEntity<?> getTimelyLogs(@RequestHeader("staffId") String staffId,
                                           @RequestParam String fromDate,
                                           @RequestParam String toDate) {

        return loggingService.getLogsBetween(staffId, LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    @PostMapping("/v1/add-holiday")
    public ResponseEntity<?> addNewHoliday(@RequestHeader("staffId")String staffId,
                                           @RequestBody Holiday holiday) {

        return holidayService.addOrUpdateHoliday(staffId, holiday);
    }

    @GetMapping("/v1/load-holidays")
    public ResponseEntity<?> loadHolidays(@RequestHeader("staffId") String staffId) {

        return holidayService.loadAllHolidays(staffId);
    }

    @PutMapping("/v1/update-holiday")
    public ResponseEntity<?> updateHoliday(@RequestHeader("staffId")String staffId,
                                           @RequestBody Holiday holiday) {

        return holidayService.addOrUpdateHoliday(staffId, holiday);
    }
}
