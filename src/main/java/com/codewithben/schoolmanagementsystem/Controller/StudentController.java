package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Attendance.AttendanceRequestList;
import com.codewithben.schoolmanagementsystem.DTO.Result.SaveStudentScores;
import com.codewithben.schoolmanagementsystem.DTO.Students.AddNewStudent;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsScoresTable;
import com.codewithben.schoolmanagementsystem.DTO.Students.UpdateStudentPersonalData;
import com.codewithben.schoolmanagementsystem.Service.*;
import com.codewithben.schoolmanagementsystem.Utility.AuthenticatedStaffProvider;
import jakarta.validation.Valid;
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

    private final AuthenticatedStaffProvider authenticatedStaffProvider;

    @GetMapping("/v1/absent-students")
    public ResponseEntity<?> getAbsentees(@RequestHeader("staffId") String Id) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.getAbsentees(staffId);
    }

    @GetMapping("/v1/total-students")
    public ResponseEntity<?> loadTotalStudents(@RequestHeader("staffId") String Id) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.countTotalStudents(staffId);
    }

    @GetMapping("/v1/find-student/{studentId}")
    public ResponseEntity<?> findStudent(@RequestHeader("staffId") String Id,
                                         @PathVariable String studentId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.findStudent(studentId, staffId);
    }

    @PostMapping("/v1/add-new-student")
    public ResponseEntity<?> enrollNewStudent(@RequestHeader("staffId") String Id,
                                              @Valid @RequestBody AddNewStudent addNewStudent) {
        String firstName = addNewStudent.getFirstName();
        String lastName = addNewStudent.getLastName();
        String levelId = addNewStudent.getLevelId();
        String gender = addNewStudent.getGender();
        String dateOfBirth = addNewStudent.getDateOfBirth();
        String hometown = addNewStudent.getHometown();
        String parentName = addNewStudent.getParentName();
        String guardianContact = addNewStudent.getGuardianContact();

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.addNewStudent(firstName, lastName, gender, dateOfBirth, hometown, parentName,
                    guardianContact, levelId, staffId);
    }

    @GetMapping("/v1/load-subject-students/{subjectId}/{semesterId}")
    public ResponseEntity<?> getSubjectStudents(@RequestHeader("staffId")String Id,
                                                @PathVariable String subjectId,
                                                @PathVariable String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return scoresService.loadStudentsForScores(semesterId, subjectId, staffId);
    }

    @PostMapping("/v1/save-scores")
    public ResponseEntity<?> saveSubjectScores(@RequestHeader("staffId") String Id,
                                               @RequestHeader("subjectId") String subjectId,
                                               @RequestHeader("semesterId") String semesterId,
                                               @RequestBody List<StudentsScoresTable> scores) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return scoresService.saveScores(staffId, subjectId, semesterId, scores);
    }

    @PutMapping("/v1/update-student-data")
    public ResponseEntity<?> updateStudentData(@RequestHeader("staffId") String Id,
                                               @RequestBody UpdateStudentPersonalData data) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.updateStudentPersonalData(data, staffId);
    }

    @GetMapping("/v1/load-students-for-attendance/{levelId}/{date}")
    public ResponseEntity<?> loadStudentsForAttendance(@RequestHeader("staffId") String Id,
                                                       @PathVariable String levelId,
                                                       @PathVariable String date) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.loadStudentsForAttendance(levelId, date, staffId);
    }

    @PostMapping("/v2/mark-attendance")
    public ResponseEntity<?> markAttendance(@RequestHeader("staffId") String Id,
                                            @RequestHeader("selectedDate") String date,
                                            @RequestHeader("levelId") String levelId,
                                            @RequestBody List<AttendanceRequestList> list) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.saveAttendance(levelId, date, list, staffId);
    }

    @GetMapping("/v1/attendance-records")
    public ResponseEntity<?> loadAttendanceRecords(@RequestHeader("staffId") String Id,
                                                   @RequestParam String levelId,
                                                   @RequestParam String semesterId,
                                                   @RequestParam String date) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.loadAttendanceRecords(staffId, levelId, semesterId, date);
    }

    @GetMapping("/v1/dates-marked/{levelId}/{semesterId}")
    public ResponseEntity<?> loadDatesMarked(@RequestHeader("staffId") String Id,
                                             @PathVariable String levelId,
                                             @PathVariable String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.loadDatesMarked(staffId, levelId, semesterId);
    }

    @GetMapping("/v1/load-students/{levelId}")
    public ResponseEntity<?> loadClassStudents(@RequestHeader("staffId") String Id,
                                               @PathVariable String levelId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.getGradeStudents(levelId, staffId);
    }

    @PutMapping("/v1/promote-student")
    public ResponseEntity<?> promoteStudent(@RequestHeader("staffId") String Id,
                                            @RequestParam String studentId,
                                            @RequestParam String promotionClassId,
                                            @RequestParam String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.promoteStudent(studentId, promotionClassId, semesterId, staffId);
    }

    @PutMapping("/v1/repeat-student")
    public ResponseEntity<?> repeatStudent(@RequestHeader("staffId") String Id,
                                           @RequestParam String studentId,
                                           @RequestParam String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.repeatStudent(studentId, semesterId, staffId);
    }

    @GetMapping(value = "/v3/generate-report-card")
    public ResponseEntity<?> generateStudentReport(@RequestHeader("staffId") String Id,
                                                   @RequestParam String studentId,
                                                   @RequestParam String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return reportService.generateStudentReport(studentId, semesterId, staffId);
    }
}
