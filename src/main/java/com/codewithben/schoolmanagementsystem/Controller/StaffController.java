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
@RequestMapping("/api/")
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

    @GetMapping("v2/total-staffs/{staffId}")
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

    @GetMapping("v2/total-students/{staffId}")
    public ResponseEntity<?> loadTotalStudents(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    String.valueOf(studentService.countTotalStudents(staffId))
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("v2/add-new-grade")
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

    @GetMapping("v2/staff-list/{staffId}")
    public ResponseEntity<?> loadStaffInfo(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(staffService.loadAllStaffList(staffId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v2/find-student")
    public ResponseEntity<?> findStudent(@RequestParam String searchParameter,
                                         @RequestParam String searchParameter2,
                                         @RequestParam String searchParameter3) {
        try {
            return ResponseEntity.ok(
                    studentService.findStudent(searchParameter, searchParameter2, searchParameter3)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v2/find-staff-by-id/{staffId}")
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

    @PostMapping("v2/add-new-student")
    public ResponseEntity<?> enrollNewStudent(@RequestBody AddNewStudent addNewStudent) {
        String firstName = addNewStudent.getFirstName();
        String lastName = addNewStudent.getLastName();
        String levelId = addNewStudent.getLevelId();
        String gender = addNewStudent.getGender();
        LocalDate dateOfBirth = addNewStudent.getDateOfBirth();
        String hometown = addNewStudent.getHomeTown();
        String parentName = addNewStudent.getParentName();
        String guardianContact = addNewStudent.getGuardianContact();
        String staffId = addNewStudent.getStaffId();

        try {
            String response = studentService.addNewStudent(firstName, lastName, gender, dateOfBirth, hometown, parentName,
                    guardianContact, levelId, staffId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("v2/update-student-data")
    public ResponseEntity<?> updateStudentData(@RequestBody UpdateStudentPersonalData updateStudentPersonalData) {

        try {
            String response = studentService.updateStudentPersonalData(updateStudentPersonalData);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v2/search-student-results")
    public ResponseEntity<?> getStudentResults(@RequestParam String studentId,
                                               @RequestParam String semesterId,
                                               @RequestParam String classId) {
        try {
            return ResponseEntity.ok(
                    studentService.findStudentResults(studentId, semesterId, classId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("v2/add-subject")
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

    @GetMapping("v2/load-staff-grades/{staffId}")
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

    @GetMapping("v2/load-subject-students/{subjectId}")
    public ResponseEntity<?> getSubjectStudents(@PathVariable String subjectId) {
        try {
            SubjectScores subjectScores = studentService.getSubjectStudents(subjectId);
            return ResponseEntity.ok().body(subjectScores);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("v2/save-subject-scores")
    public ResponseEntity<?> saveSubjectScores(@RequestBody SaveStudentScores score) {
        try {
            String response = studentService.addStudentSubjectScores(score);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("v2/print-level-list/{levelId}")
    public ResponseEntity<?> getLevelStudents(@PathVariable String levelId) {
        try {
            return ResponseEntity.ok(studentService.getLevelStudents(levelId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
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

    @GetMapping("v2/view-class-results/{levelId}/{semesterId}")
    public ResponseEntity<?> viewClassSemesterResults(@PathVariable String levelId,
                                                      @PathVariable String semesterId) {
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

    @PostMapping("v2/move-passed-students")
    public ResponseEntity<?> movePassedStudents(@RequestParam String levelId,
                                                @RequestParam String semesterId,
                                                @RequestParam String passTotalScore,
                                                @RequestParam String nextLevelId) {

        try {
            String response = reportService.movePassedStudents(levelId, nextLevelId, semesterId, Double.parseDouble(passTotalScore));
            return ResponseEntity.ok().body(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v1/load-students-for-attendance/{levelId}/{date}")
    public ResponseEntity<?> loadStudentsForAttendance(@PathVariable String levelId,
                                                       @PathVariable String date) {
        try {
            return ResponseEntity.ok(
                    studentService.loadStudentForAttendance(levelId, date)
            );
        }catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("v1/mark-attendance")
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

    @PutMapping("v1/update-attendance")
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

    @GetMapping("v1/print-subjects/{levelId}")
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
}
