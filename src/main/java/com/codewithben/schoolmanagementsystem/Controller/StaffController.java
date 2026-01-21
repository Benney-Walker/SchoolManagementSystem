package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.RecentStaffView;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentListPrint;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @GetMapping("v1/total-staffs/{staffId}")
    public ResponseEntity<?> getTotalStaffs(@PathVariable String staffId) {

        try {
            return ResponseEntity.ok().body(staffService.countTotalStaffs(staffId));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v1/total-students/{staffId}")
    public long getTotalStudents(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            return 0;

        String institutionId = staff.getInstitution().getInstitutionId();
        long totalStudents = 0;
        try {
            totalStudents = studentService.countTotalStudents(institutionId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return totalStudents;
    }

    @PostMapping("v1/add-new-grade")
    public ResponseEntity<?> addNewGrade(@RequestParam String gradeName,
                                         @RequestParam String instructorId) {
        try {
            String response = levelService.addNewClass(gradeName, instructorId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", response
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "status", ex.getMessage()
            ));
        }
    }

    @GetMapping("v2/staff-list/{staffId}")
    public ResponseEntity<?> loadStaffInfo(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(staffService.loadAllStaffData(staffId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v1/staff-list/{staffId}")
    public ResponseEntity<?> noSupport1(@PathVariable String staffId) {
        return ResponseEntity.badRequest().body(versionSupport);
    }

    @GetMapping("v1/find-student-by-id/{studentId}")
    public FindStudentDTO findStudentById(@PathVariable String studentId) {
        try {
            return studentService.findStudentByStudentId(studentId);
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    @GetMapping("v1/find-staff-by-id/{staffId}")
    public FindStaffDTO findStaffById(@PathVariable String staffId) {

        try {
            return staffService.findStaffById(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    @PostMapping("v1/add-new-student")
    public ResponseEntity<?> addNewStudent(@RequestBody AddNewStudent addNewStudent) {
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
            System.out.println(response);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "studentId", response
            ));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", ex.getMessage()
            ));
        }
    }

    @PutMapping("v1/update-student-data")
    public ResponseEntity<?> updateStudentData(@RequestBody UpdateStudentPersonalData updateStudentPersonalData) {
        String studentId = updateStudentPersonalData.getStudentId();
        String gender = updateStudentPersonalData.getGender();
        String dateOfBirth = updateStudentPersonalData.getDateOfBirth().toString();
        String guardianName = updateStudentPersonalData.getGuardianName();
        String guardianContact = updateStudentPersonalData.getGuardianContact();
        String status = updateStudentPersonalData.getStatus();

        try {
            String response = studentService.updateStudentPersonalData(studentId, gender, dateOfBirth, guardianName, guardianContact,
                    status);
            return ResponseEntity.ok(Map.of(
                    "status", response
            ));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", ex.getMessage()
            ));
        }
    }

    @GetMapping("v1/search-student-results")
    public StudentResult getStudentResults(@RequestParam String studentId,
                                           @RequestParam String semesterId,
                                           @RequestParam String classId) {
        try {
            return studentService.findStudentResults(studentId, semesterId, classId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    @PostMapping("v1/add-subject")
    public ResponseEntity<?> addNewSubject(@RequestBody AddNewSubject addNewSubject) {
        String subjectName = addNewSubject.getSubjectName();
        String levelId = addNewSubject.getGradeId();

        try {
            //Response contains subject Id
            String response = staffService.addNewSubjects(subjectName, levelId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", response
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", e.getMessage()
            ));
        }


    }

    @GetMapping("v1/load-grade-and-size/{staffId}")
    public GradeInfoResponse loadGradeAndSize(@PathVariable String staffId) {
        try {
            return levelService.loadGradeNameAndSize(staffId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new GradeInfoResponse(ex.getMessage(), null, null);
        }
    }

    @GetMapping("v2/load-grade-students/{levelId}")
    public ResponseEntity<?> loadGradeStudents(@PathVariable String levelId) {
        try {
            return ResponseEntity.ok(levelService.loadGradeStudents(levelId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("v1/load-grade-students/{staffId}")
    public ResponseEntity<?> noSupport(@PathVariable String staffId) {
        return ResponseEntity.badRequest().body(versionSupport);
    }

    @GetMapping("v1/load-subject-students/{subjectId}")
    public ResponseEntity<SubjectScores> getSubjectStudents(@PathVariable String subjectId) {
        try {
            SubjectScores subjectScores = studentService.getSubjectStudents(subjectId);
            return ResponseEntity.ok().body(subjectScores);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("v1/save-student-scores")
    public ResponseEntity<?> saveStudentScores(@RequestParam String staffId,
                                               @RequestParam String studentId,
                                               @RequestParam String subjectId,
                                               @RequestParam String semesterId,
                                               @RequestParam String classScore,
                                               @RequestParam String examScore) {
        try {
            String response = studentService.addStudentSubjectScores(
                    studentId, subjectId, Double.parseDouble(classScore), Double.parseDouble(examScore),
                    staffId, semesterId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("v1/students-by-level/{levelId}")
    public ResponseEntity<List<StudentListPrint>> getLevelStudents(@PathVariable String levelId) {
        try {
            return ResponseEntity.ok(studentService.getLevelStudents(levelId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("v2/generate-class-report/{levelId}/{semesterId}/{promotionGradeId}")
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

    @GetMapping("v1/generate-class-report/{staffId}/{semesterId}/{promotionGradeId}")
    public ResponseEntity<?> noSupport(@PathVariable String staffId,
                                       @PathVariable String semesterId,
                                       @PathVariable String promotionGradeId) {
        return ResponseEntity.badRequest().body(versionSupport);
    }

    @GetMapping("v2/view-class-results/{levelId}/{semesterId}")
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

    @GetMapping("v1/view-class-results/{staffId}/{semesterId}")
    public ResponseEntity<?> noSupport(@PathVariable String staffId,
                                       @PathVariable String semesterId) {
        return ResponseEntity.badRequest().body(versionSupport);
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

    @GetMapping("v1/move-passed-students")
    public ResponseEntity<?> noSupport(@RequestParam String levelId,
                                       @RequestParam String semesterId,
                                       @RequestParam String passTotalScore,
                                       @RequestParam String nextLevelId) {
        return ResponseEntity.badRequest().body(versionSupport);
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
