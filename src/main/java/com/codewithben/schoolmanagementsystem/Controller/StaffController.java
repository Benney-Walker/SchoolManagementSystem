package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.RecentStaffView;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentListPrint;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.LevelService;
import com.codewithben.schoolmanagementsystem.Service.StaffService;
import com.codewithben.schoolmanagementsystem.Service.StudentService;
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

    public StaffController(LevelService levelService, StaffService staffService,
                           StaffsRepository staffsRepository, StudentService studentService) {
        this.levelService = levelService;
        this.staffService = staffService;
        this.staffsRepository = staffsRepository;
        this.studentService = studentService;
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
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(levelId);
        System.out.println(gender);
        System.out.println(dateOfBirth);
        System.out.println(hometown);
        System.out.println(parentName);
        System.out.println(guardianContact);

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

        try {
            String response = studentService.updateStudentPersonalData(studentId, gender, dateOfBirth, guardianName, guardianContact);
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
        System.out.println(semesterId);

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
    public String loadGradeAndSize(@PathVariable String staffId) {
        try {
            return levelService.loadGradeNameAndSize(staffId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
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
    public SubjectScores getSubjectStudents(@PathVariable String subjectId) {
        try {
            return studentService.getSubjectStudents(subjectId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @PostMapping("/save-student-scores")
    public ResponseEntity<?> saveStudentScores(@RequestParam String studentId,
                                               @RequestParam String subjectId,
                                               @RequestParam String classScore,
                                               @RequestParam String examScore) {
        try {
            String response = studentService.addStudentSubjectScores(
                    studentId, subjectId, Double.parseDouble(classScore), Double.parseDouble(examScore)
            );
            return ResponseEntity.ok(Map.of(
                    "status", response
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", e.getMessage()
            ));
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
}
