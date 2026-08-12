package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductRecord;
import com.codewithben.schoolmanagementsystem.DTO.Staff.NewStaff;
import com.codewithben.schoolmanagementsystem.Service.*;
import com.codewithben.schoolmanagementsystem.Utility.AuthenticatedStaffProvider;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    private final AuthenticatedStaffProvider authenticatedStaffProvider;


    @GetMapping("/v1/total-staffs")
    public ResponseEntity<?> loadTotalStaffs(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.countTotalStaffs(staffId);
    }

    @GetMapping("/v1/total-teaching-staffs")
    public ResponseEntity<?> loadTotalTeachingStaffs(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.countTotalTeachingStaffs(staffId);
    }

    //Unused endpoint
    @GetMapping("/v1/staff-list")
    public ResponseEntity<?> loadStaffInfo(@RequestHeader("staffid") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.loadAllStaffList(staffId);
    }

    @GetMapping("/v1/find-staff-by-id/{instructorId}")
    public ResponseEntity<?> findStaffById(@RequestHeader("staffId") String Id,
                                           @PathVariable String instructorId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.findStaffById(instructorId, staffId);
    }

    @GetMapping("/v1/load-staff-grades")
    public ResponseEntity<?> loadStaffGrades(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.loadStaffClasses(staffId);
    }


    @GetMapping("/v1/view-class-results")
    public ResponseEntity<?> viewClassSemesterResults(@RequestHeader("staffId") String Id,
                                                      @RequestParam String levelId,
                                                      @RequestParam String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return resultsService.viewClassSemesterReport(levelId, semesterId, staffId);
    }

    @GetMapping("/v1/report-card")
    public ResponseEntity<?> viewStudentResults(@RequestHeader("staffId") String Id,
                                                @RequestParam String studentId,
                                                @RequestParam String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return resultsService.viewStudentResult(studentId, semesterId, staffId);
    }

    @GetMapping("/v1/view-sba")
    public ResponseEntity<?> viewSba(@RequestHeader("staffId") String Id,
                                     @RequestParam String classId,
                                     @RequestParam String subjectId,
                                     @RequestParam String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return resultsService.viewSba(staffId, classId, semesterId, subjectId);
    }

    @GetMapping("/v1/view-master-sheet")
    public ResponseEntity<?> viewMasterSheet(@RequestHeader("staffId") String Id,
                                             @RequestParam String levelId,
                                             @RequestParam String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return resultsService.viewMasterScoreSheet(levelId, semesterId, staffId);
    }

    @GetMapping("/v1/load-staffs-info")
    public ResponseEntity<?> loadStaffCache(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.loadStaffList(staffId);
    }

    @GetMapping("/v2/staff-list")
    public ResponseEntity<?> loadStaffList(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.loadStaffList(staffId);
    }

    @GetMapping("/v1/load-semesters")
    public ResponseEntity<?> loadSemesterCaching(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.loadSemesterCaching(staffId);
    }

    @GetMapping("/v1/load-levels")
    public ResponseEntity<?> loadGradesCache(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.loadClassesForCache(staffId);
    }

    @GetMapping("/v1/load-classes")
    public ResponseEntity<?> loadClassesForCache(@RequestHeader("staffId") String Id) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.loadClassesForCache(staffId);
    }

    @GetMapping("/v1/load-subjects/{levelId}")
    public ResponseEntity<?> loadGradeSubjects(@RequestHeader("staffId") String Id,
                                               @PathVariable String levelId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return classService.getClassSubjects(levelId, staffId);
    }

    @PostMapping("/v1/add-new-staff")
    public ResponseEntity<?> enrollNewStaff(@RequestHeader("staffId")String Id,
                                            @Valid @RequestBody NewStaff newStaff) {

        String firstName = newStaff.getFirstName();
        String lastName = newStaff.getLastName();
        String gender = newStaff.getGender();
        String dateOfBirth = newStaff.getDateOfBirth();
        String email = newStaff.getEmail();
        String password = newStaff.getPassword();
        String phoneNumber = newStaff.getPhoneNumber();
        List<String> role = newStaff.getRoles();

        String staffId = authenticatedStaffProvider.getStaffId();

        return staffService.addNewStaff(staffId, firstName, lastName, gender, dateOfBirth,
                email, password, phoneNumber, role);
    }

    @GetMapping("/v1/conduct-records")
    public ResponseEntity<?> getStudentsConduct(@RequestHeader("staffId")String Id,
                                                @RequestParam String levelId,
                                                @RequestParam String semesterId) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return conductService.getStudentsConduct(levelId, semesterId, staffId);
    }

    @PutMapping("/v1/save-conduct-record")
    public ResponseEntity<?> saveStudentConduct(@RequestHeader("staffId")String Id,
                                                @Valid @RequestBody StudentConductRecord record) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return conductService.saveStudentConducts(staffId, record);
    }

    /*@PutMapping("/v2/save-conduct-record")
    public ResponseEntity<?> saveStudentConduct(@RequestHeader("staffId")String Id,
                                                @Valid @RequestBody List<@Valid StudentConductRecord> records) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return conductService.saveStudentConducts(staffId, records);
    }*/
}