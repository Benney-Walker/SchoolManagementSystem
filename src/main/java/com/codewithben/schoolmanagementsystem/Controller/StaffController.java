package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductRecord;
import com.codewithben.schoolmanagementsystem.DTO.Staff.NewStaff;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Service.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final ClassService classService;

    private final StaffService staffService;

    private final ReportService reportService;

    private final ResultsService resultsService;

    private final LoggingService loggingService;

    private final ConductService conductService;

    private final StudentService studentService;


    @GetMapping("/v1/total-staffs")
    public ResponseEntity<?> loadTotalStaffs(@RequestHeader("staffId") String staffId) {

        return staffService.countTotalStaffs(staffId);
    }

    @GetMapping("/v1/total-teaching-staffs")
    public ResponseEntity<?> loadTotalTeachingStaffs(@RequestHeader("staffId") String staffId) {

        return staffService.countTotalTeachingStaffs(staffId);
    }

    @GetMapping("/v1/staff-list")
    public ResponseEntity<?> loadStaffInfo(@RequestHeader("staffid") String staffId) {

        return staffService.loadAllStaffList(staffId);
    }

    @GetMapping("/v1/find-staff-by-id/{instructorId}")
    public ResponseEntity<?> findStaffById(@RequestHeader("staffId") String staffId,
                                           @PathVariable String instructorId) {

        return staffService.findStaffById(instructorId, staffId);
    }

    @GetMapping("/v1/load-staff-grades")
    public ResponseEntity<?> loadStaffGrades(@RequestHeader("staffId") String staffId) {

        return classService.loadStaffClasses(staffId);
    }


    @GetMapping("/v1/view-class-results")
    public ResponseEntity<?> viewClassSemesterResults(@RequestHeader("staffId") String staffId,
                                                      @RequestParam String levelId,
                                                      @RequestParam String semesterId) {

        return resultsService.viewClassSemesterReport(levelId, semesterId, staffId);
    }

    @GetMapping("/v1/view-sba")
    public ResponseEntity<?> viewSba(@RequestHeader("staffId") String staffId,
                                     @RequestParam String classId,
                                     @RequestParam String subjectId,
                                     @RequestParam String semesterId) {

        return resultsService.viewSba(staffId, classId, semesterId, subjectId);
    }

    @PostMapping("/v1/promote-student/{studentId}/{levelId}")
    public ResponseEntity<?> movePassedStudents(@RequestHeader("staffId") String staffId,
                                                @PathVariable String levelId,
                                                @PathVariable String studentId) {

        return studentService.promoteStudent(studentId, levelId, staffId);
    }

    @GetMapping("/v1/load-staffs-info")
    public ResponseEntity<?> loadStaffCache(@RequestHeader("staffId") String staffId) {

        return staffService.loadAllStaffInfo(staffId);
    }

    @GetMapping("/v1/load-semesters")
    public ResponseEntity<?> loadSemesterCaching(@RequestHeader("staffId") String staffId) {

        return classService.loadSemesterCaching(staffId);
    }

    @GetMapping("/v1/load-levels")
    public ResponseEntity<?> loadGradesCache(@RequestHeader("staffId") String staffId) {

        return classService.loadClassesForCache(staffId);
    }

    @GetMapping("/v1/load-classes")
    public ResponseEntity<?> loadClassesForCache(@RequestHeader("staffId") String staffId) {

        return classService.loadClassesForCache(staffId);
    }

    @GetMapping("/v1/load-subjects/{levelId}")
    public ResponseEntity<?> loadGradeSubjects(@RequestHeader("staffId") String staffId,
                                               @PathVariable String levelId) {

        return classService.getClassSubjects(levelId, staffId);
    }

    @PostMapping("/v1/add-new-staff")
    public ResponseEntity<?> enrollNewStaff(@RequestHeader("staffId")String staffId,
                                            @RequestBody NewStaff newStaff) {

        String firstName = newStaff.getFirstName();
        String lastName = newStaff.getLastName();
        String gender = newStaff.getGender();
        String dateOfBirth = newStaff.getDateOfBirth();
        String email = newStaff.getEmail();
        String password = newStaff.getPassword();
        String phoneNumber = newStaff.getPhoneNumber();
        List<String> role = newStaff.getRoles();

        return staffService.addNewStaff(staffId, firstName, lastName, gender, dateOfBirth,
                email, password, phoneNumber, role);
    }

    @GetMapping("/v1/conduct-records")
    public ResponseEntity<?> getStudentsConduct(@RequestHeader("staffId")String staffId,
                                               @RequestParam String levelId,
                                               @RequestParam String semesterId) {

        return conductService.getStudentsConduct(levelId, semesterId, staffId);
    }

    @PutMapping("/v1/save-conduct-record")
    public ResponseEntity<?> saveStudentConduct(@RequestHeader("staffId")String staffId,
                                                @RequestBody StudentConductRecord record) {

        return conductService.saveStudentConducts(staffId, record);
    }
}
