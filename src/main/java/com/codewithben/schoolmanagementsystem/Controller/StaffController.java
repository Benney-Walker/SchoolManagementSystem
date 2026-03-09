package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentListPrint;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final LevelService levelService;

    private final StaffService staffService;

    private final StaffsRepository staffsRepository;

    private final StudentService studentService;

    private final ReportService reportService;

    private final JasperReportService jasperReportService;

    private final LevelRepository levelRepository;

    private final String versionSupport = "Version no longer support this function so contact the developer";

    public StaffController(LevelService levelService, StaffService staffService,
                           StaffsRepository staffsRepository, StudentService studentService,
                           ReportService reportService,  JasperReportService jasperReportService,
                           LevelRepository levelRepository) {
        this.levelService = levelService;
        this.staffService = staffService;
        this.staffsRepository = staffsRepository;
        this.studentService = studentService;
        this.reportService = reportService;
        this.jasperReportService = jasperReportService;
        this.levelRepository = levelRepository;
    }

    @GetMapping("/v1/total-staffs/{staffId}")
    public ResponseEntity<?> loadTotalStaffs(@PathVariable String staffId) {

        try {
            return ResponseEntity.ok().body(
                    String.valueOf(staffService.countTotalStaffs(staffId))
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/staff-list/{staffId}")
    public ResponseEntity<?> loadStaffInfo(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(staffService.loadAllStaffList(staffId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/find-staff-by-id/{staffId}")
    public ResponseEntity<?> findStaffById(@PathVariable String staffId) {

        try {
            return ResponseEntity.ok(
                    staffService.findStaffById(staffId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/load-staff-grades/{staffId}")
    public ResponseEntity<?> loadStaffGrades(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    levelService.loadStaffGrades(staffId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/print-student-list/{levelId}")
    public ResponseEntity<?> getLevelStudents(@PathVariable String levelId) {
        try {
            return ResponseEntity.ok(studentService.getLevelStudents(levelId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v2/view-class-results")
    public ResponseEntity<?> viewClassSemesterResults(@RequestParam String levelId,
                                                      @RequestParam String semesterId) {
        try {
            List<ViewClassSemesterReport> reportData = reportService.viewClassSemesterReport(
                    levelId, semesterId
            );
            return ResponseEntity.ok(reportData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/v1/move-passed-students/{levelId}")
    public ResponseEntity<?> movePassedStudents(@PathVariable String levelId) {

        try {
            String response = reportService.movePassedStudents(levelId);
            return ResponseEntity.ok().body(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/v1/mark-attendance")
    public ResponseEntity<?> markAttendance(@RequestBody Attendance attendance) {
        String studentId = attendance.getStudentId();
        String staffId = attendance.getStaffId();
        String levelId = attendance.getLevelId();
        String status = attendance.getStatus().toUpperCase();

        try {
            return ResponseEntity.ok(studentService.markStudentAttendance(
                    studentId, staffId, levelId, status
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/v1/update-attendance")
    public ResponseEntity<?> updatedAttendance(@RequestParam String studentId,
                                               @RequestParam String staffId,
                                               @RequestParam String levelId,
                                               @RequestParam String status,
                                               @RequestParam String dateMarked) {
        try {
            return ResponseEntity.ok(
                    studentService.updateStudentAttendance(
                            studentId, staffId, levelId, status, LocalDate.parse(dateMarked)
                    )
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/print-subjects/{levelId}")
    public ResponseEntity<?> printLevelSubjects(@PathVariable String levelId) {
        try {
            return ResponseEntity.ok(
                    staffService.printLevelSubjects(levelId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/load-staffs-info/{staffId}")
    public ResponseEntity<?> loadStaffCache(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    staffService.loadAllStaffInfo(staffId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/load-semesters/{staffId}")
    public ResponseEntity<?> loadSemesterCaching(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(levelService.loadSemesterCaching(staffId));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/load-levels/{staffId}")
    public ResponseEntity<?> loadGradesCache(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(levelService.loadLevelInfo(staffId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/load-subjects/{levelId}/{staffId}")
    public ResponseEntity<?> loadGradeSubjects(@PathVariable String levelId,
                                               @PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    levelService.getLevelSubjects(levelId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
