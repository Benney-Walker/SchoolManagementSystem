package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Attendance.Attendance;
import com.codewithben.schoolmanagementsystem.DTO.Result.SaveStudentScores;
import com.codewithben.schoolmanagementsystem.DTO.Students.AddNewStudent;
import com.codewithben.schoolmanagementsystem.DTO.Students.UpdateStudentPersonalData;
import com.codewithben.schoolmanagementsystem.Service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }


    @GetMapping("/v1/absent-students")
    public ResponseEntity<?> getAbsentees(@RequestHeader("staffId") String staffId) {

        return studentService.findTodaysAbsentees(staffId);
    }

    @GetMapping("/v1/total-students/{staffId}")
    public ResponseEntity<?> loadTotalStudents(@RequestHeader("staffId") String staffId) {

        return studentService.countTotalStudents(staffId);
    }

    @GetMapping("/v1/find-student/{studentId}")
    public ResponseEntity<?> findStudent(@RequestHeader("staffId") String staffId,
                                         @PathVariable String studentId) {

        return studentService.findStudent(studentId, staffId);
    }

    @PostMapping("/v1/add-new-student")
    public ResponseEntity<?> enrollNewStudent(@RequestHeader("staffId") String staffId,
                                              @RequestBody AddNewStudent addNewStudent) {
        String firstName = addNewStudent.getFirstName();
        String lastName = addNewStudent.getLastName();
        String levelId = addNewStudent.getLevelId();
        String gender = addNewStudent.getGender();
        LocalDate dateOfBirth = addNewStudent.getDateOfBirth();
        String hometown = addNewStudent.getHomeTown();
        String parentName = addNewStudent.getParentName();
        String guardianContact = addNewStudent.getGuardianContact();

        return studentService.addNewStudent(firstName, lastName, gender, dateOfBirth, hometown, parentName,
                    guardianContact, levelId, staffId);
    }

    @GetMapping("/v1/search-student-results")
    public ResponseEntity<?> getStudentResults(@RequestHeader("staffId") String staffId,
                                               @RequestParam String studentId,
                                               @RequestParam String semesterId,
                                               @RequestParam String classId) {

        return studentService.findStudentResults(studentId, semesterId, classId, staffId);
    }

    @GetMapping("/v1/load-subject-students/{subjectId}")
    public ResponseEntity<?> getSubjectStudents(@RequestHeader("staffId")String staffId,
                                                @PathVariable String subjectId) {

        return studentService.getSubjectStudents(subjectId, staffId);
    }

    @PostMapping("/v1/save-subject-scores")
    public ResponseEntity<?> saveSubjectScores(@RequestHeader("staffId") String staffId,
                                               @RequestHeader("subjectId") String subjectId,
                                               @RequestHeader("semesterId") String semesterId,
                                               @RequestBody SaveStudentScores score) {

        return studentService.addStudentSubjectScores(score, staffId, subjectId, semesterId);
    }

    @PutMapping("/v1/update-student-data")
    public ResponseEntity<?> updateStudentData(@RequestHeader("staffId") String staffId,
                                               @RequestBody UpdateStudentPersonalData data) {

        return studentService.updateStudentPersonalData(data, staffId);
    }

    @GetMapping("/v1/load-students-for-attendance/{levelId}/{date}")
    public ResponseEntity<?> loadStudentsForAttendance(@RequestHeader("staffId") String staffId,
                                                       @PathVariable String levelId,
                                                       @PathVariable String date) {

        return studentService.loadStudentForAttendance(levelId, date, staffId);
    }

    @PostMapping("/v1/mark-attendance")
    public ResponseEntity<?> markAttendance(@RequestHeader("staffId") String staffId,
                                            @RequestHeader("selectedDate") String date,
                                            @RequestBody Attendance attendance) {
        String studentId = attendance.getStudentId();
        String levelId = attendance.getLevelId();
        String status = attendance.getStatus().toUpperCase();

        return studentService.markStudentAttendance(studentId, levelId, status, date, staffId);
    }

    @GetMapping("/v1/load-students/{levelId}")
    public ResponseEntity<?> loadGradeStudents(@RequestHeader("staffId") String staffId,
                                               @PathVariable String levelId) {

        return studentService.getGradeStudents(levelId, staffId);
    }
}
