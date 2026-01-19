package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.RecentStaffView;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentListPrint;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class StaffController {
    private final LevelService levelService;

    private final StaffService staffService;

    private final StaffsRepository staffsRepository;

    private final StudentService studentService;

    private final ReportService reportService;

    private final JasperReportService jasperReportService;

    public StaffController(LevelService levelService, StaffService staffService,
                           StaffsRepository staffsRepository, StudentService studentService,
                           ReportService reportService,  JasperReportService jasperReportService) {
        this.levelService = levelService;
        this.staffService = staffService;
        this.staffsRepository = staffsRepository;
        this.studentService = studentService;
        this.reportService = reportService;
        this.jasperReportService = jasperReportService;
    }

    @GetMapping("/total-staffs/{staffId}")
    public long getTotalStaffs(@PathVariable String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            return 0;

        String institutionId = staff.getInstitution().getInstitutionId();
        long totalStaffs = 0;
        try {
            totalStaffs = staffService.countTotalStaffs(institutionId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return totalStaffs;
    }

    @GetMapping("/total-students/{staffId}")
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

    @PostMapping("/add-new-grade")
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

    @GetMapping("/staff-list/{staffId}")
    public List<RecentStaffView> loadStaffInfo(@PathVariable String staffId) {
        try {
            return staffService.loadAllStaffData(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @GetMapping("/find-student-by-id/{studentId}")
    public FindStudentDTO findStudentById(@PathVariable String studentId) {
        try {
            return studentService.findStudentByStudentId(studentId);
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    @GetMapping("/find-staff-by-id/{staffId}")
    public FindStaffDTO findStaffById(@PathVariable String staffId) {

        try {
            return staffService.findStaffById(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    @PostMapping("/add-new-student")
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

    @PutMapping("/update-student-data")
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

    @GetMapping("/search-student-results")
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

    @PostMapping("/add-subject")
    public ResponseEntity<?> addNewSubject(@RequestBody AddNewSubject addNewSubject) {
        String subjectName = addNewSubject.getSubjectName();
        String levelId = addNewSubject.getGradeId();
        String semesterId = addNewSubject.getSemesterId();

        try {
            //Response contains subject Id
            String response = staffService.addNewSubjects(subjectName, semesterId, levelId);
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

    @GetMapping("/load-grade-and-size/{staffId}")
    public GradeInfoResponse loadGradeAndSize(@PathVariable String staffId) {
        try {
            return levelService.loadGradeNameAndSize(staffId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new GradeInfoResponse(ex.getMessage(), null, null);
        }
    }

    @GetMapping("/load-grade-students/{staffId}")
    public List<StudentsTableDTO> loadGradeStudents(@PathVariable String staffId) {
        try {
            return studentService.loadGradeStudents(staffId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    @GetMapping("/load-subject-students/{subjectId}")
    public ResponseEntity<SubjectScores> getSubjectStudents(@PathVariable String subjectId) {
        try {
            SubjectScores subjectScores = studentService.getSubjectStudents(subjectId);
            return ResponseEntity.ok().body(subjectScores);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/save-student-scores")
    public ResponseEntity<?> saveStudentScores(@RequestParam String studentId,
                                               @RequestParam String subjectId,
                                               @RequestParam String classScore,
                                               @RequestParam String examScore,
                                               @RequestParam String staffId) {
        try {
            String response = studentService.addStudentSubjectScores(
                    studentId, subjectId, Double.parseDouble(classScore), Double.parseDouble(examScore),
                    staffId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/students-by-level/{levelId}")
    public ResponseEntity<List<StudentListPrint>> getLevelStudents(@PathVariable String levelId) {
        try {
            return ResponseEntity.ok(studentService.getLevelStudents(levelId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/generate-class-report/{staffId}/{semesterId}/{promotionGradeId}")
    public ResponseEntity<?> generateStudentReports(@PathVariable String staffId,
                                                    @PathVariable String semesterId,
                                                    @PathVariable String promotionGradeId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getLevel() == null) {
            return ResponseEntity.badRequest().body("Staff not found or Not assigned Grade");
        }

        try {
            List<GenerateStudentReport> reportData = reportService.generateClassReports(staff.getLevel().getLevelID(), semesterId,
                    promotionGradeId);

            byte[] reportPDF = jasperReportService.generateClassReport(reportData, staff.getInstitution().getInstitutionName());

            return ResponseEntity.ok().body(reportPDF);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/view-class-results/{staffId}/{semesterId}")
    public ResponseEntity<?> viewClassSemesterResults(@PathVariable String staffId,
                                                      @PathVariable String semesterId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getLevel() == null) {
            return ResponseEntity.badRequest().body("Staff not found or Not assigned Grade");
        }

        try {
            List<GenerateStudentReport> reportData = reportService.generateClassReports(staff.getLevel().getLevelID(), semesterId, null);
            return ResponseEntity.ok().body(reportData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/move-passed-students")
    public ResponseEntity<?> movePassedStudents(@RequestParam String staffId, @RequestParam String semesterId,
            @RequestParam String passTotalScore, @RequestParam String nextLevelId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getLevel() == null) {
            return ResponseEntity.badRequest().body("Staff not found or Not assigned Grade");
        }

        try {
            String response = reportService.movePassedStudents(staffId, nextLevelId, semesterId, Double.parseDouble(passTotalScore));
            return ResponseEntity.ok().body(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/load-students-for-attendance/{levelId}/{date}")
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

    @PostMapping("/mark-attendance")
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

    @PutMapping("/update-attendance")
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
}
