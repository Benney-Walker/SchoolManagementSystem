package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.*;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final LevelService levelService;

    private final StaffService staffService;

    private final StudentService studentService;

    private final StaffsRepository staffsRepository;

    private final SemesterRepository semesterRepository;

    private final UtilityClass utilityClass;

    private final InstitutionService institutionService;

    private final ReportService reportService;

    public StudentController(LevelService levelService, StaffService staffService, StudentService studentService,
                             StaffsRepository staffsRepository, InstitutionService institutionService,
                             SemesterRepository semesterRepository, UtilityClass utilityClass,
                             ReportService reportService) {
        this.levelService = levelService;
        this.staffService = staffService;
        this.studentService = studentService;
        this.staffsRepository = staffsRepository;
        this.institutionService = institutionService;
        this.semesterRepository = semesterRepository;
        this.utilityClass = utilityClass;
        this.reportService = reportService;
    }


    @GetMapping("/v1/absent-students/{staffId}")
    public ResponseEntity<?> getAbsentees(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok().body(studentService.findInstitutionAbsentees(staffId));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }

    @GetMapping("/v1/total-students/{staffId}")
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

    @GetMapping("/v1/find-student/{studentId}/{staffId}")
    public ResponseEntity<?> findStudent(@PathVariable String studentId,
                                         @PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    studentService.findStudent(studentId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/v1/add-new-student")
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

    @GetMapping("/v1/search-student-results")
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

    @GetMapping("/v1/load-subject-students/{subjectId}")
    public ResponseEntity<?> getSubjectStudents(@PathVariable String subjectId) {
        try {
            SubjectScores subjectScores = studentService.getSubjectStudents(subjectId);
            return ResponseEntity.ok().body(subjectScores);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/v1/save-subject-scores")
    public ResponseEntity<?> saveSubjectScores(@RequestBody SaveStudentScores score) {
        try {
            String response = studentService.addStudentSubjectScores(score);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/v1/update-student-data")
    public ResponseEntity<?> updateStudentData(@RequestBody UpdateStudentPersonalData updateStudentPersonalData) {

        try {
            String response = studentService.updateStudentPersonalData(updateStudentPersonalData);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/v1/load-students-for-attendance/{levelId}/{date}")
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
}
