package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.Service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final LevelService levelService;

    private final StaffService staffService;

    private final StudentService studentService;

    private final ReportService reportService;

    public StaffController(LevelService levelService, StaffService staffService, StudentService studentService,
                           ReportService reportService) {
        this.levelService = levelService;
        this.staffService = staffService;
        this.studentService = studentService;
        this.reportService = reportService;
    }

    @GetMapping("/v1/total-staffs")
    public ResponseEntity<?> loadTotalStaffs(@RequestHeader("staffId") String staffId) {

        return staffService.countTotalStaffs(staffId);
    }

    @GetMapping("/v1/total-teaching-staffs")
    public ResponseEntity<?> loadTotalTeachingStaffs(@RequestHeader("staffId") String staffId) {

        return staffService.countTotalTeachingStaffs(staffId);
    }

    @GetMapping("/v1/staff-list")
    public ResponseEntity<?> loadStaffInfo(@RequestHeader("staffid") String staffId) {

        return staffService.loadAllStaffList(staffId);
    }

    @GetMapping("/v1/find-staff-by-id/{instructorId}")
    public ResponseEntity<?> findStaffById(@RequestHeader("staffId") String staffId,
                                           @PathVariable String instructorId) {

        return staffService.findStaffById(instructorId, staffId);
    }

    @GetMapping("/v1/load-staff-grades")
    public ResponseEntity<?> loadStaffGrades(@RequestHeader("staffId") String staffId) {

        return levelService.loadStaffGrades(staffId);
    }

    /*@GetMapping("/v1/print-student-list/{levelId}")
    public ResponseEntity<?> getLevelStudents(@PathVariable String levelId) {

        return studentService.getLevelStudents(levelId));
    }*/

    @GetMapping("/v2/view-class-results")
    public ResponseEntity<?> viewClassSemesterResults(@RequestHeader("staffId") String staffId,
                                                      @RequestParam String levelId,
                                                      @RequestParam String semesterId) {

        return reportService.viewClassSemesterReport(levelId, semesterId, staffId);
    }

    @PostMapping("/v1/promote-student/{studentId}/{levelId}")
    public ResponseEntity<?> movePassedStudents(@RequestHeader("staffId") String staffId,
                                                @PathVariable String levelId,
                                                @PathVariable String studentId) {

        return reportService.promoteStudent(studentId, levelId, staffId);
    }

    @GetMapping("/v1/load-staffs-info")
    public ResponseEntity<?> loadStaffCache(@RequestHeader("staffId") String staffId) {

        return staffService.loadAllStaffInfo(staffId);
    }

    @GetMapping("/v1/load-semesters")
    public ResponseEntity<?> loadSemesterCaching(@RequestHeader("staffId") String staffId) {

        return levelService.loadSemesterCaching(staffId);
    }

    @GetMapping("/v1/load-levels")
    public ResponseEntity<?> loadGradesCache(@RequestHeader("staffId") String staffId) {

        return levelService.loadLevelInfo(staffId);
    }

    @GetMapping("/v1/load-subjects/{levelId}")
    public ResponseEntity<?> loadGradeSubjects(@RequestHeader("staffId") String staffId,
                                               @PathVariable String levelId) {

        return levelService.getLevelSubjects(levelId, staffId);
    }
}
