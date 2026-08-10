package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Holiday.Holiday;
import com.codewithben.schoolmanagementsystem.DTO.Result.GradingCriteria;
import com.codewithben.schoolmanagementsystem.DTO.Semester.AddNewSemester;
import com.codewithben.schoolmanagementsystem.DTO.Class.FindAndUpdateClassInfo;
import com.codewithben.schoolmanagementsystem.DTO.Semester.FindSemester;
import com.codewithben.schoolmanagementsystem.DTO.Staff.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Subject.AddNewSubject;
import com.codewithben.schoolmanagementsystem.DTO.Subject.SubjectDTO;
import jakarta.validation.Valid;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import com.codewithben.schoolmanagementsystem.Utility.AuthenticatedStaffProvider;
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

    private final AuthenticatedStaffProvider authenticatedStaffProvider;

    @PostMapping("/v1/reset-staff-password")
    public ResponseEntity<?> recoverStaffPassword(@RequestHeader("staffId") String Id,
                                                  @RequestParam String newPasswordStaffId,
                                                  @RequestParam String newPassword) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.resetStaffPassword(newPasswordStaffId, newPassword, staffId);
    }

    @PostMapping("/v1/add-semester")
    public ResponseEntity<?> addNewSemester(@RequestHeader("staffId") String Id,
                                            @Valid @RequestBody AddNewSemester addNewSemester) {

        String semesterName = addNewSemester.getSemesterName();
        LocalDate startDate = LocalDate.parse(addNewSemester.getStartDate());
        LocalDate endDate = LocalDate.parse(addNewSemester.getEndDate());
        String academicYear = addNewSemester.getAcademicYear();

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.addNewSemester(semesterName, startDate, endDate, academicYear, staffId);
    }

    @PostMapping("/v1/new-grading-criteria")
    public ResponseEntity<?> setGradingCriteria(@RequestHeader("staffId") String Id,
                                                @Valid @RequestBody GradingCriteria gradingCriteria) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return institutionService.saveGradingCriteria(gradingCriteria, staffId);
    }

    @GetMapping("/v1/load-grading-criteria")
    public ResponseEntity<?> loadAllGradingCriteria(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return institutionService.loadAllGradingCriteria(staffId);
    }

    @PutMapping("/v1/update-grading-criteria")
    public ResponseEntity<?> updateGradingCriteria(@RequestHeader("staffId") String Id,
                                                   @Valid @RequestBody GradingCriteria gradingCriteria) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return institutionService.updateGradingCriteria(gradingCriteria, staffId);
    }

    @GetMapping("/v1/find-class-info/{levelId}")
    public ResponseEntity<?> findClassInfo(@RequestHeader("staffId") String Id,
                                           @PathVariable String levelId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.findClassInfo(levelId, staffId);
    }

    @PutMapping("/v1/update-class-info")
    public ResponseEntity<?> updateClassInfo(@RequestHeader("staffId") String Id,
                                             @Valid @RequestBody FindAndUpdateClassInfo updateInfo) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.updateClassInfo(updateInfo, staffId);
    }

    @PutMapping("/v1/update-staff-info")
    public ResponseEntity<?> updateStaffInfo(@RequestHeader("staffId") String Id,
                                             @Valid @RequestBody FindStaffDTO info) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.updateStaffInfo(info, staffId);
    }

    @GetMapping("/v1/search-semester/{semesterId}")
    public ResponseEntity<?> searchSemesterInfo(@RequestHeader("staffId") String Id,
                                                @PathVariable String semesterId) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.findSemesterInfo(semesterId, staffId);
    }

    @PutMapping("/v1/update-semester-info")
    public ResponseEntity<?> updateSemesterInfo(@RequestHeader("staffId") String Id,
                                                @Valid @RequestBody FindSemester updateInfo) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.updateSemesterInfo(updateInfo, staffId);
    }

    @PostMapping("/v1/add-new-class")
    public ResponseEntity<?> addNewClass(@RequestHeader("staffId") String Id,
                                         @RequestParam String gradeName,
                                         @RequestParam String instructorId) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.addNewClass(gradeName, instructorId, staffId);
    }

    @PostMapping("/v1/add-subject")
    public ResponseEntity<?> saveNewSubject(@RequestHeader("staffId") String Id,
                                            @Valid @RequestBody AddNewSubject addNewSubject) {
        String subjectName = addNewSubject.getSubjectName();
        String levelId = addNewSubject.getGradeId();

        String staffId = authenticatedStaffProvider.getStaffId();

        return subjectsService.addNewSubject(subjectName, levelId, staffId);

    }

    @GetMapping("/v1/load-subject-data/{subjectId}")
    public ResponseEntity<?> loadSubjectData(@RequestHeader("staffId") String Id,
                                             @PathVariable String subjectId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return subjectsService.loadSubjectData(subjectId, staffId);
    }

    @PutMapping("/v1/update-subject-details")
    public ResponseEntity<?> updateSubjectData(@RequestHeader("staffId") String Id,
                                               @Valid @RequestBody SubjectDTO subjectDTO) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return subjectsService.updateSubjectData(subjectDTO, staffId);
    }

    @DeleteMapping("/v1/delete-subject/{subjectId}")
    public ResponseEntity<?> deleteSubjectData(@RequestHeader("staffId") String Id,
                                               @PathVariable String subjectId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return subjectsService.deleteSubjectData(subjectId, staffId);
    }

    @GetMapping(value = "/v2/generate-class-report")
    public ResponseEntity<?> generateBulkClassReports(@RequestHeader("staffId") String Id,
                                                      @RequestParam String levelId,
                                                      @RequestParam String semesterId) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return reportService.generateClassBulkReport(staffId, levelId, semesterId);
    }

    @GetMapping(value = "/v2/generate-sba-report")
    public ResponseEntity<?> generateSbaReport(@RequestHeader("staffId") String Id,
                                               @RequestParam String levelId,
                                               @RequestParam String semesterId,
                                               @RequestParam String subjectId) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return reportService.generateSbaReport(staffId, levelId, semesterId, subjectId);
    }

    @GetMapping(value = "/v2/generate-master-sheet")
    public ResponseEntity<?> generateMasterScoreSheet(@RequestHeader("staffId") String Id,
                                                      @RequestParam String levelId,
                                                      @RequestParam String semesterId) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return reportService.generateMasterScoreSheet(levelId, semesterId, staffId);
    }

    @GetMapping("/v1/recent-logs")
    public ResponseEntity<?> getRecentActivities(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return loggingService.getRecentActivity(staffId);
    }

    @GetMapping("/v1/get-staff-logs")
    public ResponseEntity<?> getStaffLogs(@RequestHeader("staffId") String Id,
                                          @RequestParam String selectedStaffId,
                                          @RequestParam String fromDate,
                                          @RequestParam String toDate) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return loggingService.getStaffLogsBetween(
                staffId, selectedStaffId, LocalDate.parse(fromDate), LocalDate.parse(toDate)
        );
    }

    @GetMapping("/v1/get-timely-logs")
    public ResponseEntity<?> getTimelyLogs(@RequestHeader("staffId") String Id,
                                           @RequestParam String fromDate,
                                           @RequestParam String toDate) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return loggingService.getLogsBetween(staffId, LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    @PostMapping("/v1/add-holiday")
    public ResponseEntity<?> addNewHoliday(@RequestHeader("staffId")String Id,
                                           @Valid @RequestBody Holiday holiday) {
        String staffId = authenticatedStaffProvider.getStaffId();

        return holidayService.addNewHoliday(staffId, holiday);
    }

    @GetMapping("/v1/load-holidays")
    public ResponseEntity<?> loadHolidays(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return holidayService.loadAllHolidays(staffId);
    }

    @PutMapping("/v1/update-holiday")
    public ResponseEntity<?> updateHoliday(@RequestHeader("staffId")String Id,
                                           @Valid @RequestBody Holiday holiday) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return holidayService.updateHoliday(staffId, holiday);
    }
}