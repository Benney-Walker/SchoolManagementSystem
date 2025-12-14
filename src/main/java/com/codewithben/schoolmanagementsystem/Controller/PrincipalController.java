package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Academics.LevelCaching;
import com.codewithben.schoolmanagementsystem.DTO.Academics.RecentStudentsDTO;
import com.codewithben.schoolmanagementsystem.DTO.Academics.SemesterCaching;
import com.codewithben.schoolmanagementsystem.DTO.Institution.AddNewSemester;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StaffCaching;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.FeesService;
import com.codewithben.schoolmanagementsystem.Service.LevelService;
import com.codewithben.schoolmanagementsystem.Service.StaffService;
import com.codewithben.schoolmanagementsystem.Service.StudentService;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
public class PrincipalController {
    private final LevelService levelService;

    private final StaffService staffService;

    private final StudentService studentService;

    private final FeesService feesService;

    private final StaffsRepository staffsRepository;

    private final InstitutiionRepository institutionRepository;

    private final SemesterRepository semesterRepository;

    private final UtilityClass utilityClass;

    public PrincipalController( LevelService levelService, StaffService staffService, StudentService studentService,
                                FeesService feesService, StaffsRepository staffsRepository, InstitutiionRepository institutionRepository,
                                SemesterRepository semesterRepository, UtilityClass utilityClass) {
        this.levelService = levelService;
        this.staffService = staffService;
        this.studentService = studentService;
        this.feesService = feesService;
        this.staffsRepository = staffsRepository;
        this.institutionRepository = institutionRepository;
        this.semesterRepository = semesterRepository;
        this.utilityClass = utilityClass;
    }

    @GetMapping("recently-added-students/{staffId}")
    public List<RecentStudentsDTO> getRecentlyAddedStudents(@PathVariable String staffId) {
        Staffs  staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            return new ArrayList<>();
        String institutionId = staff.getInstitution().getInstitutionId();
        String semesterId = utilityClass.getCurrentSemester(institutionId);
        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null)
            return new ArrayList<>();

        try {
            return studentService.findRecentlyAddedStudents(semester.getSemesterStartDate(), semester.getSemesterEndDate());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }

    }

    @GetMapping("/load-levels/{staffId}")
    public List<LevelCaching> loadLevelInfo(@PathVariable String staffId) {

        return levelService.loadLevelInfo(staffId);
    }

    @GetMapping("/load-staffs-info/{staffId}")
    public List<StaffCaching> getAllStaffInfo(@PathVariable String staffId) {
        System.out.println("Error: " + staffId);
        if (staffId == null)
            return new ArrayList<>();

        try {
            return staffService.loadAllStaffInfo(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @PostMapping("/recover-staff-password/{staffId}")
    public ResponseEntity<?> recoverStaffPassword(@PathVariable String staffId) {
        try {
            //Response contains staff password from database
            String response = staffService.recoverStaffPassword(staffId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", response
            ));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/load-semesters/{staffId}")
    public List<SemesterCaching> loadSemesterInfo(@PathVariable String staffId) {
        try {
            return levelService.loadSemesterInfo(staffId);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
    }

    @PostMapping("/add-semester")
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
}
