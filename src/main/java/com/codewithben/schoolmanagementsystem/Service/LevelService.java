package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Class.LevelCaching;
import com.codewithben.schoolmanagementsystem.DTO.Semester.SemesterCaching;
import com.codewithben.schoolmanagementsystem.DTO.Class.FindAndUpdateClassInfo;
import com.codewithben.schoolmanagementsystem.DTO.Class.GradeInformation;
import com.codewithben.schoolmanagementsystem.DTO.Semester.FindSemester;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentRoaster;
import com.codewithben.schoolmanagementsystem.DTO.Subject.SubjectsHolder;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class LevelService {
    private final StaffsRepository staffsRepository;

    private final LevelRepository levelRepository;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final SemesterRepository semesterRepository;

    private final LoggingService loggingService;


    @Transactional
    public ResponseEntity<?> addNewClass(String className, String selectedStaff, String staffId) {
        String logData = "Class Name: " + className + " Instructor Id: " + selectedStaff;

        Staffs staffs = staffsRepository.findByStaffId(selectedStaff).orElse(null);
        if (staffs == null) {
            loggingService.logActivity(LogType.ADD_NEW_CLASS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Selected staff does not exist"
            ));
        }

        Institution institution = staffs.getInstitution();
        if (institution == null) {
            loggingService.logActivity(LogType.ADD_NEW_CLASS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution do not exist"
            ));
        }

        Level level = levelRepository.findByLevelNameAndInstitution_InstitutionId(className, institution.getInstitutionId()).orElse(null);

        if (level == null) {
            level = new Level();
            level.setLevelName(className);
            level.setLevelID(utilityClass.generateEntityId("LEVEL"));
            level.setInstitution(staffs.getInstitution());
            level.setStaff(staffs); // Link Level -> Staff

            level = levelRepository.saveAndFlush(level);

            List<Level> levels = staffs.getLevels();
            if (levels == null) {
                levels = new ArrayList<>();
            }
            levels.add(level);

            staffs.setLevel(levels);

            staffsRepository.save(staffs);

            loggingService.logActivity(LogType.ADD_NEW_CLASS, logData, staffId, "SUCCESS");
            return ResponseEntity.ok().build();
        }

        loggingService.logActivity(LogType.ADD_NEW_CLASS, logData, staffId, "FAILED");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Class already exists in system"
        ));
    }

    public ResponseEntity<?> loadLevelInfo(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.FETCH_CLASSES, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load classes");
        }

        List<Level> levels = staff.getInstitution().getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logActivity(LogType.FETCH_CLASSES, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No classes found");
        }

        return ResponseEntity.ok(levels.stream().map(
                loadInfo -> {
                    String levelName = loadInfo.getLevelName();
                    String levelID = loadInfo.getLevelID();

                    loggingService.logActivity(LogType.FETCH_CLASSES, "N/A", staffId, "SUCCESS");
                    return new LevelCaching(levelID, levelName);
                }
        ).collect(Collectors.toList()));
    }

    public ResponseEntity<?> loadSemesterCaching(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.FETCH_SEMESTERS, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load semesters");
        }

        List<Semester> semesters = staff.getInstitution().getSemester();
        if (semesters == null || semesters.isEmpty()) {
            loggingService.logActivity(LogType.FETCH_SEMESTERS, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No semesters found");
        }

        return ResponseEntity.ok(semesters.stream().map(
                loadInfo -> {
                    String semesterId = loadInfo.getSemesterID();
                    String semesterName = loadInfo.getSemesterName();
                    String academicYear = loadInfo.getAcademicYear();

                    loggingService.logActivity(LogType.FETCH_SEMESTERS, "N/A", staffId, "SUCCESS");
                    return new SemesterCaching(semesterId, semesterName, academicYear);
                }
        ).collect(Collectors.toList()));
    }

    public ResponseEntity<?> addNewSemester(String semesterName, LocalDate startDate, LocalDate endDate,
                                         String academicYear, String staffId) {

        String logData = "SemesterName: " + semesterName + " StartDate: " + startDate + " EndDate: " + endDate + " AcademicYear: " + academicYear;

        Staffs staffs = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staffs == null) {
            loggingService.logActivity(LogType.NEW_SEMESTER, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Internal Server Error! Contact Developer"
            ));
        }

        Institution institution = staffs.getInstitution();


        Semester semester = new Semester();
        semester.setSemesterName(semesterName);
        semester.setSemesterID(utilityClass.generateEntityId("SEMESTER"));
        semester.setSemesterStartDate(startDate);
        semester.setSemesterEndDate(endDate);
        semester.setAcademicYear(academicYear);
        semester.setInstitution(institution);
        semesterRepository.saveAndFlush(semester);

        //Add semester to institution Semesters
        List<Semester> semesters = institution.getSemester();
        if (semesters == null) {
            semesters = new ArrayList<>();
        }
        semesters.add(semester);
        institution.setSemester(semesters);
        institutiionRepository.save(institution);

        loggingService.logActivity(LogType.NEW_SEMESTER, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadStaffGrades(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.FETCH_CLASSES, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");
        }

        List<Level> levels = staff.getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logActivity(LogType.FETCH_CLASSES, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No class assigned to your account");
        }

        List<GradeInformation> gradesInformation = getGradesInformation(levels);

        loggingService.logActivity(LogType.FETCH_CLASSES, "N/A", staffId, "SUCCESS");
        return ResponseEntity.ok(gradesInformation);
    }


    private List<GradeInformation> getGradesInformation(List<Level> levels) {
        List<GradeInformation> gradesInformation = new ArrayList<>();
        for (Level level : levels) {
            String gradeId = level.getLevelID();
            String gradeName = level.getLevelName();
            int countStudent = level.getStudents() == null ? 0 : level.getStudents().size();
            String gradeStudentsCount = String.valueOf(countStudent);
            List<StudentRoaster> gradeStudents = new ArrayList<>();

            List<Students> students = level.getStudents() == null ?
                    new ArrayList<>() : level.getStudents();
                for (Students gradeStudent : students) {
                    String studentId = gradeStudent.getStudentId();
                    String fullName = gradeStudent.getFirstName() + " " + gradeStudent.getLastName();
                    String gender = gradeStudent.getGender();
                    String homeTown = gradeStudent.getHomeTown();
                    String parentName = gradeStudent.getParentName();
                    String parentPhoneNumber = gradeStudent.getParentPhoneNumber();

                    StudentRoaster student = new StudentRoaster(
                            studentId, fullName, gender, homeTown, parentName, parentPhoneNumber
                    );
                    gradeStudents.add(student);
                }

            GradeInformation gradeInformation = new GradeInformation(
                    gradeId, gradeName, gradeStudentsCount, gradeStudents
            );

            gradesInformation.add(gradeInformation);
        }
        return gradesInformation;
    }

    public ResponseEntity<?> findClassInfo(String levelId, String staffId) {
        String logData = "Level Id: " + levelId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.FETCH_GRADE_INFO, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        loggingService.logActivity(LogType.FETCH_GRADE_INFO, logData, staffId, "SUCCESS");
        return ResponseEntity.ok(new FindAndUpdateClassInfo(
                level.getLevelID(), level.getLevelName(), level.getStaff().getStaffId()
        ));
    }

    public ResponseEntity<?> updateClassInfo(FindAndUpdateClassInfo updateInfo, String staffId) {
        String logData = "Level Id: " + updateInfo.getLevelId() + " Selected Staff Id: " + updateInfo.getStaffId() + " Level Name: " + updateInfo.getLevelName();

        Staffs staff = staffsRepository.findByStaffId(updateInfo.getStaffId()).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.UPDATE_GRADE_INFO, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Level level = levelRepository.findByLevelID(updateInfo.getLevelId()).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.UPDATE_GRADE_INFO, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid level Id"
            ));
        }

        level.setLevelName(updateInfo.getLevelName());
        level.setStaff(staff);
        levelRepository.save(level);

        loggingService.logActivity(LogType.UPDATE_GRADE_INFO, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> findSemesterInfo(String semesterId, String staffId) {
        String logData = "Semester Id: " + semesterId;

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.FIND_SEMESTER, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid semester Id"
            ));
        }

        FindSemester findSemester = new FindSemester();
        findSemester.setSemesterID(semesterId);
        findSemester.setSemesterName(semester.getSemesterName());
        findSemester.setSemesterStartDate(semester.getSemesterStartDate().toString());
        findSemester.setSemesterEndDate(semester.getSemesterEndDate().toString());
        findSemester.setAcademicYear(semester.getAcademicYear());

        loggingService.logActivity(LogType.FIND_SEMESTER, logData, staffId, "SUCCESS");
        return ResponseEntity.ok(findSemester);
    }

    public ResponseEntity<?> updateSemesterInfo(FindSemester updateInfo, String staffId) {
        String logData = "Semester Id: " + updateInfo.getSemesterID() + " Semester Name: " + updateInfo.getSemesterName() +
                " Semester start: " + updateInfo.getSemesterStartDate() + " Semester End: " + updateInfo.getSemesterEndDate() +
                " Academic Year: " + updateInfo.getAcademicYear();

        Semester semester = semesterRepository.findBySemesterID(updateInfo.getSemesterID()).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.UPDATE_SEMESTER, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid semester Id"
            ));
        }

        semester.setSemesterName(updateInfo.getSemesterName());
        semester.setSemesterStartDate(LocalDate.parse(updateInfo.getSemesterStartDate()));
        semester.setSemesterEndDate(LocalDate.parse(updateInfo.getSemesterEndDate()));
        semester.setAcademicYear(updateInfo.getAcademicYear());
        semesterRepository.save(semester);

        loggingService.logActivity(LogType.UPDATE_SEMESTER, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getLevelSubjects(String levelId, String staffId) {

        String logData = "Class Id: " + levelId;
        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.FETCH_SUBJECTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Class do not exist in the system"
            ));
        }

        List<Subjects> subjects = level.getSubjects();
        if (subjects == null || subjects.isEmpty()) {
            loggingService.logActivity(LogType.FETCH_SUBJECTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Class has no subjects"
            ));
        }

        List<SubjectsHolder> subjectsHolders = new ArrayList<>();
        for (Subjects subject: subjects) {
            SubjectsHolder sub = new SubjectsHolder(
                    subject.getSubjectId(),
                    subject.getSubjectName()
            );
            subjectsHolders.add(sub);
        }
        loggingService.logActivity(LogType.FETCH_SUBJECTS, logData, staffId, "SUCCESS");
        return ResponseEntity.ok(subjectsHolders);
    }

}
