package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.*;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/principal/")
public class PrincipalController {
    private static final Logger log = LogManager.getLogger(PrincipalController.class);
    private final LevelService levelService;

    private final StaffService staffService;

    private final StudentService studentService;

    private final StaffsRepository staffsRepository;

    private final SemesterRepository semesterRepository;

    private final UtilityClass utilityClass;

    private final InstitutionService institutionService;

    private final ReportService reportService;

    public PrincipalController( LevelService levelService, StaffService staffService, StudentService studentService,
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


    @GetMapping("v2/absent-students/{staffId}")
    public ResponseEntity<?> getAbsentees(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok().body(studentService.findInstitutionAbsentees(staffId));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }

    @GetMapping("v2/load-levels/{staffId}")
    public ResponseEntity<?> loadGradesCache(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(levelService.loadLevelInfo(staffId));
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v1/load-staffs-info/{staffId}")
    public ResponseEntity<?> loadStaffCache(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(
                    staffService.loadAllStaffInfo(staffId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("v2/reset-staff-password/{staffId}/{newPassword}")
    public ResponseEntity<?> recoverStaffPassword(@PathVariable String staffId,
                                                  @PathVariable String newPassword) {
        try {

            return ResponseEntity.ok(
                    staffService.resetStaffPassword(staffId, newPassword)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v1/load-semesters/{staffId}")
    public ResponseEntity<?> loadSemesterCaching(@PathVariable String staffId) {
        try {
            return ResponseEntity.ok(levelService.loadSemesterCaching(staffId));
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("v1/add-semester")
    public ResponseEntity<?> addNewSemester(@RequestBody AddNewSemester addNewSemester) {
        String semesterName = addNewSemester.getSemesterName();
        LocalDate startDate = LocalDate.parse(addNewSemester.getStartDate());
        LocalDate endDate = LocalDate.parse(addNewSemester.getEndDate());
        String academicYear = addNewSemester.getAcademicYear();
        String staffId = addNewSemester.getStaffId();

        try {
            String response = levelService.addNewSemester(semesterName, startDate, endDate, academicYear, staffId);
            return ResponseEntity.ok(Map.of(
                    "status", "success"
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", ex.getMessage()
            ));
        }
    }

    @PostMapping("v2/set-grades")
    public ResponseEntity<?> setGradingCriteria(@RequestBody GradingCriteria gradingCriteria) {
        try {
            return ResponseEntity.ok(
                    institutionService.setGradingCriteria(gradingCriteria)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("v2/find-and-update-classes/{searchParameter}/{staffId}")
    public ResponseEntity<?> findClassInfo(@PathVariable String searchParameter,
                                           @PathVariable String staffId) {
        try {
            return ResponseEntity.ok(levelService.findClassInfo(
                    searchParameter, staffId
            ));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("v2/update-class-info")
    public ResponseEntity<?> updateClassInfo(@RequestBody FindAndUpdateClassInfo updateInfo) {
        try {
            return ResponseEntity.ok(
                    levelService.updateClassInfo(updateInfo)
            );
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("v2/update-staff-info")
    public ResponseEntity<?> updateStaffInfo(@RequestBody FindStaffDTO info) {
        try {
            return ResponseEntity.ok(
                    staffService.updateStaffInfo(info)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("v2/search-semester/{semesterId}")
    public ResponseEntity<?> searchSemesterInfo(@PathVariable String semesterId) {
        try {
            return ResponseEntity.ok(
                    levelService.findSemesterInfo(semesterId)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("v2/update-semester-info")
    public ResponseEntity<?> updateSemesterInfo(@RequestBody FindSemester updateInfo) {
        try {
            return ResponseEntity.ok(
                    levelService.updateSemesterInfo(updateInfo)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
