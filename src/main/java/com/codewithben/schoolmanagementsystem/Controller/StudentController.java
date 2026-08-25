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
    public ResponseEntity<?> getAbsentees() {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.getAbsentees(staffId);
    }

    @GetMapping("/v1/total-students")
    public ResponseEntity<?> loadTotalStudents() {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.countTotalStudents(staffId);
    }

    @GetMapping("/v1/find-student/{studentId}")
    public ResponseEntity<?> findStudent(@PathVariable String studentId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.findStudent(studentId, staffId);
    }

    @PostMapping("/v1/add-new-student")
    public ResponseEntity<?> enrollNewStudent(@Valid @RequestBody AddNewStudent addNewStudent) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.addNewStudent(addNewStudent, staffId);
    }

    @GetMapping("/v1/load-subject-students/{subjectId}/{semesterId}")
    public ResponseEntity<?> getSubjectStudents(@PathVariable String subjectId,
                                                @PathVariable String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return scoresService.loadStudentsForScores(semesterId, subjectId, staffId);
    }

    @PostMapping("/v1/save-scores")
    public ResponseEntity<?> saveSubjectScores(@RequestHeader("subjectId") String subjectId,
                                               @RequestHeader("semesterId") String semesterId,
                                               @RequestBody List<StudentsScoresTable> scores) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return scoresService.saveScores(staffId, subjectId, semesterId, scores);
    }

    @PutMapping("/v1/update-student-data")
    public ResponseEntity<?> updateStudentData(@RequestBody UpdateStudentPersonalData data) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.updateStudentPersonalData(data, staffId);
    }

    @GetMapping("/v1/load-students-for-attendance/{levelId}/{date}")
    public ResponseEntity<?> loadStudentsForAttendance(@PathVariable String levelId,
                                                       @PathVariable String date) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.loadStudentsForAttendance(levelId, date, staffId);
    }

    @PostMapping("/v2/mark-attendance")
    public ResponseEntity<?> markAttendance(@RequestHeader("selectedDate") String date,
                                            @RequestHeader("levelId") String levelId,
                                            @RequestBody List<AttendanceRequestList> list) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.saveAttendance(levelId, date, list, staffId);
    }

    @GetMapping("/v1/attendance-records")
    public ResponseEntity<?> loadAttendanceRecords(@RequestParam String levelId,
                                                   @RequestParam String semesterId,
                                                   @RequestParam String date) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.loadAttendanceRecords(staffId, levelId, semesterId, date);
    }

    @GetMapping("/v1/dates-marked/{levelId}/{semesterId}")
    public ResponseEntity<?> loadDatesMarked(@PathVariable String levelId,
                                             @PathVariable String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return attendanceService.loadDatesMarked(staffId, levelId, semesterId);
    }

    @GetMapping("/v1/load-students/{levelId}")
    public ResponseEntity<?> loadClassStudents(@PathVariable String levelId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.getGradeStudents(levelId, staffId);
    }

    @PutMapping("/v1/promote-student")
    public ResponseEntity<?> promoteStudent(@RequestParam String studentId,
                                            @RequestParam String promotionClassId,
                                            @RequestParam String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.promoteStudent(studentId, promotionClassId, semesterId, staffId);
    }

    @PutMapping("/v1/repeat-student")
    public ResponseEntity<?> repeatStudent(@RequestParam String studentId,
                                           @RequestParam String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return studentService.repeatStudent(studentId, semesterId, staffId);
    }

    @GetMapping(value = "/v3/generate-report-card")
    public ResponseEntity<?> generateStudentReport(@RequestParam String studentId,
                                                   @RequestParam String semesterId) {

        String staffId  = authenticatedStaffProvider.getStaffId();

        return reportService.generateStudentReport(studentId, semesterId, staffId);
    }
}
