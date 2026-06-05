package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
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
import lombok.AllArgsConstructor;
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
public class ClassService {
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
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Selected staff does not exist"
            ));
        }

        Institution institution = staffs.getInstitution();
        if (institution == null) {
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
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

            List<Level> levels = staffs.getLevel();
            if (levels == null) {
                levels = new ArrayList<>();
            }
            levels.add(level);

            staffs.setLevel(levels);

            staffsRepository.save(staffs);

            loggingService.logGeneralActivity(LogType.CLASS, LogAction.CREATE, logData, staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().build();
        }

        loggingService.logGeneralActivity(LogType.CLASS, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Class already exists in system"
        ));
    }

    public ResponseEntity<?> loadClassesForCache(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Level> levels = staff.getInstitution().getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no class yet"
            ));
        }

        List<LevelCaching> classesList = new ArrayList<>();
        for (Level level : levels) {
            LevelCaching levelCaching = LevelCaching.builder()
                    .levelId(level.getLevelID())
                    .levelName(level.getLevelName())
                    .build();

            classesList.add(levelCaching);
        }

        loggingService.logGeneralActivity(LogType.CLASS, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(classesList);
    }

    public ResponseEntity<?> loadSemesterCaching(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.TERM, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Semester> semesters = staff.getInstitution().getSemester();
        if (semesters == null || semesters.isEmpty()) {
            loggingService.logGeneralActivity(LogType.TERM, LogAction.READ, "No semesters found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No semesters found"
            ));
        }

        //Retrieve semesters
        List<SemesterCaching> semesterList = new ArrayList<>();
        for (Semester semester : semesters) {
            SemesterCaching semesterCaching = SemesterCaching.builder()
                    .semesterId(semester.getSemesterID())
                    .semesterName(semester.getSemesterName())
                    .academicYear(semester.getAcademicYear())
                    .build();
            semesterList.add(semesterCaching);
        }

        loggingService.logGeneralActivity(LogType.TERM, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(semesterList);
    }

    public ResponseEntity<?> addNewSemester(String semesterName, LocalDate startDate, LocalDate endDate,
                                         String academicYear, String staffId) {

        String logData = "SemesterName: " + semesterName + " StartDate: " + startDate + " EndDate: " + endDate + " AcademicYear: " + academicYear;

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.TERM, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Semester semester = semesterRepository.findBySemesterNameAndAcademicYearAndInstitution_InstitutionId(
                semesterName, academicYear, staff.getInstitution().getInstitutionId()
        ).orElse(null);
        if (semester != null) {
            loggingService.logGeneralActivity(LogType.TERM, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Term already exists"
            ));
        }


        semester = new Semester();
        semester.setSemesterName(semesterName);
        semester.setSemesterID(utilityClass.generateEntityId("SEMESTER"));
        semester.setSemesterStartDate(startDate);
        semester.setSemesterEndDate(endDate);
        semester.setAcademicYear(academicYear);
        semester.setInstitution(staff.getInstitution());
        semesterRepository.saveAndFlush(semester);

        //Add semester to institution Semesters
        List<Semester> semesters = staff.getInstitution().getSemester();
        if (semesters == null) {
            semesters = new ArrayList<>();
        }
        semesters.add(semester);
        staff.getInstitution().setSemester(semesters);
        institutiionRepository.save(staff.getInstitution());

        loggingService.logGeneralActivity(LogType.TERM, LogAction.CREATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadStaffClasses(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Level> levels = staff.getLevel();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.READ, "No class is assigned to your account", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No class is assigned to your account"
            ));
        }

        List<GradeInformation> gradesInformation = getGradesInformation(levels);

        loggingService.logGeneralActivity(LogType.CLASS, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
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

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        loggingService.logGeneralActivity(LogType.CLASS, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(new FindAndUpdateClassInfo(
                level.getLevelID(), level.getLevelName(), level.getStaff().getStaffId()
        ));
    }

    public ResponseEntity<?> updateClassInfo(FindAndUpdateClassInfo updateInfo, String staffId) {

        Staffs staff = staffsRepository.findByStaffId(updateInfo.getStaffId()).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.UPDATE, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Level level = levelRepository.findByLevelID(updateInfo.getLevelId()).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.CLASS, LogAction.UPDATE, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid level Id"
            ));
        }

        level.setLevelName(updateInfo.getLevelName());
        level.setStaff(staff);
        levelRepository.save(level);

        loggingService.logGeneralActivity(LogType.CLASS, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> findSemesterInfo(String semesterId, String staffId) {

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.TERM, LogAction.READ, "Invalid term Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid term Id"
            ));
        }

        FindSemester findSemester = new FindSemester();
        findSemester.setSemesterID(semesterId);
        findSemester.setSemesterName(semester.getSemesterName());
        findSemester.setSemesterStartDate(semester.getSemesterStartDate().toString());
        findSemester.setSemesterEndDate(semester.getSemesterEndDate().toString());
        findSemester.setAcademicYear(semester.getAcademicYear());

        loggingService.logGeneralActivity(LogType.TERM, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(findSemester);
    }

    public ResponseEntity<?> updateSemesterInfo(FindSemester updateInfo, String staffId) {

        Semester semester = semesterRepository.findBySemesterID(updateInfo.getSemesterID()).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.TERM, LogAction.UPDATE, "Invalid term Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid term Id"
            ));
        }

        semester.setSemesterName(updateInfo.getSemesterName());
        semester.setSemesterStartDate(LocalDate.parse(updateInfo.getSemesterStartDate()));
        semester.setSemesterEndDate(LocalDate.parse(updateInfo.getSemesterEndDate()));
        semester.setAcademicYear(updateInfo.getAcademicYear());
        semesterRepository.save(semester);

        loggingService.logGeneralActivity(LogType.TERM, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getClassSubjects(String levelId, String staffId) {

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.READ, "Class do not exist in the system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Class do not exist in the system"
            ));
        }

        List<Subjects> subjects = level.getSubjects();
        if (subjects == null || subjects.isEmpty()) {
            loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.READ, "Class has no subjects", staffId, LogStatus.FAILED);
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
        loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(subjectsHolders);
    }

}
