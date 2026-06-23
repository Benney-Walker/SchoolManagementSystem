package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Attendance.MarkAttendance_List;
import com.codewithben.schoolmanagementsystem.DTO.Result.SaveStudentScores;
import com.codewithben.schoolmanagementsystem.DTO.Students.AddNewStudent;
import com.codewithben.schoolmanagementsystem.DTO.Students.UpdateStudentPersonalData;
import com.codewithben.schoolmanagementsystem.Service.*;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    private final ScoresService scoresService;

    private final AttendanceService attendanceService;

    private final ReportService reportService;

    @GetMapping("/v1/absent-students")
    public ResponseEntity<?> getAbsentees(@RequestHeader("staffId") String staffId) {

        return studentService.getAbsentees(staffId);
    }

    @GetMapping("/v1/total-students")
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
        String dateOfBirth = addNewStudent.getDateOfBirth();
        String hometown = addNewStudent.getHomeTown();
        String parentName = addNewStudent.getParentName();
        String guardianContact = addNewStudent.getGuardianContact();

        return studentService.addNewStudent(firstName, lastName, gender, dateOfBirth, hometown, parentName,
                    guardianContact, levelId, staffId);
    }

    @GetMapping("/v1/load-subject-students/{subjectId}")
    public ResponseEntity<?> getSubjectStudents(@RequestHeader("staffId")String staffId,
                                                @PathVariable String subjectId) {

        return scoresService.loadStudentsForScores(subjectId, staffId);
    }

    @PostMapping("/v1/save-subject-scores")
    public ResponseEntity<?> saveSubjectScores(@RequestHeader("staffId") String staffId,
                                               @RequestHeader("subjectId") String subjectId,
                                               @RequestHeader("semesterId") String semesterId,
                                               @RequestBody SaveStudentScores score) {

        return scoresService.addStudentSubjectScores(score, staffId, subjectId, semesterId);
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

        return attendanceService.loadStudentsForAttendance(levelId, date, staffId);
    }

    @PostMapping("/v1/mark-attendance")
    public ResponseEntity<?> markAttendance(@RequestHeader("staffId") String staffId,
                                            @RequestHeader("selectedId") String date,
                                            @RequestBody MarkAttendance_List markAttendanceList) {
        String studentId = markAttendanceList.getStudentId();
        String levelId = markAttendanceList.getLevelId();
        String status = markAttendanceList.getStatus().toUpperCase();

        return attendanceService.markStudentAttendance(studentId, levelId, status, date, staffId);
    }

    @PostMapping("/v2/mark-attendance")
    public ResponseEntity<?> markAttendance(@RequestHeader("staffId") String staffId,
                                            @RequestHeader("selectedDate") String date,
                                            @RequestBody List<MarkAttendance_List> markAttendanceList) {

        return attendanceService.markStudentAttendance(markAttendanceList, date, staffId);
    }

    @GetMapping("/v1/load-students/{levelId}")
    public ResponseEntity<?> loadGradeStudents(@RequestHeader("staffId") String staffId,
                                               @PathVariable String levelId) {

        return studentService.getGradeStudents(levelId, staffId);
    }

    @GetMapping(
            value = "/v2/generate-report-card",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<?> generateStudentReport(@RequestHeader("staffId") String staffId,
                                                   @RequestHeader("promotionId") String promotionId,
                                                   @RequestParam String studentId,
                                                   @RequestParam String semesterId) {

        return reportService.generateStudentReport(studentId, semesterId, promotionId, staffId);
    }
}
