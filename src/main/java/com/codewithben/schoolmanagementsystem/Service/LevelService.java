package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Academics.LevelCaching;
import com.codewithben.schoolmanagementsystem.DTO.Academics.SemesterCaching;
import com.codewithben.schoolmanagementsystem.DTO.Institution.*;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LevelService {
    private final StaffsRepository staffsRepository;

    private final LevelRepository levelRepository;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final SemesterRepository semesterRepository;

    private final LoggingService loggingService;

    public LevelService(StaffsRepository staffsRepository, LevelRepository levelRepository,  UtilityClass utilityClass,
                        InstitutiionRepository institutiionRepository, SemesterRepository semesterRepository,
                        LoggingService loggingService) {
        this.staffsRepository = staffsRepository;
        this.levelRepository = levelRepository;
        this.utilityClass = utilityClass;
        this.institutiionRepository = institutiionRepository;
        this.semesterRepository = semesterRepository;
        this.loggingService = loggingService;
    }

    @Transactional
    public ResponseEntity<?> addNewClass(String className, String instructorId, String staffId) {
        String logData = "Class Name: " + className + " Instructor Id: " + instructorId;

        Staffs staffs = staffsRepository.findByStaffId(instructorId).orElse(null);
        if (staffs == null) {
            loggingService.logActivity("ADD_NEW_CLASS", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Selected instructor not found");
        }

        Institution institution = staffs.getInstitution();
        if (institution == null) {
            loggingService.logActivity("ADD_NEW_CLASS", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Institution not found");
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

            loggingService.logActivity("ADD_NEW_CLASS", logData, staffId, "SUCCESS");
            return ResponseEntity.ok().build();
        }

        loggingService.logActivity("ADD_NEW_CLASS", logData, staffId, "FAILED");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Level already exist");
    }

    public List<LevelCaching> loadLevelInfo(String staffId) throws Exception {
        Staffs staffs = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("You can't access staff list"));

        Institution institution = staffs.getInstitution();
        if (institution == null) {
            throw new Exception("Institution not found");
        }

        List<Level> levels = institution.getLevels();
        if (levels == null || levels.isEmpty()) {
            return Collections.emptyList();
        }

        return levels.stream().map(
                loadInfo -> {
                    String levelName = loadInfo.getLevelName();
                    String levelID = loadInfo.getLevelID();

                    return new LevelCaching(levelID, levelName);
                }
        ).collect(Collectors.toList());
    }

    public List<SemesterCaching> loadSemesterCaching(String staffId) throws Exception {
        Staffs staffs = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staffs == null)
            throw new Exception("You can access semester list");

        Institution institution = institutiionRepository.findByInstitutionId(staffs.getInstitution().getInstitutionId()).orElse(null);
        if (institution == null)
            throw new Exception("Institution not found");

        List<Semester> semesters = institution.getSemester();
        if (semesters == null || semesters.isEmpty()) {
            return Collections.emptyList();
        }

        return semesters.stream().map(
                loadInfo -> {
                    String semesterId = loadInfo.getSemesterID();
                    String semesterName = loadInfo.getSemesterName();
                    String academicYear = loadInfo.getAcademicYear();

                    return new SemesterCaching(semesterId, semesterName, academicYear);
                }
        ).collect(Collectors.toList());
    }

    public ResponseEntity<?> addNewSemester(String semesterName, LocalDate startDate, LocalDate endDate,
                                         String academicYear, String staffId) {

        String logData = "SemesterName: " + semesterName + " StartDate: " + startDate + " EndDate: " + endDate + " AcademicYear: " + academicYear;

        Staffs staffs = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staffs == null) {
            loggingService.logActivity("NEW_SEMESTER", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Staff id");
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

        loggingService.logActivity("NEW_SEMESTER", logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public List<GradeInformation> loadStaffGrades(String staffId) throws Exception {
        Staffs staffs = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("Staff not found"));

        List<Level> levels = staffs.getLevels();
        if (levels == null || levels.isEmpty())
            return Collections.emptyList();

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
            loggingService.logActivity("FETCH_GRADE_INFO", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Level Id");
        }

        loggingService.logActivity("FETCH_GRADE_INFO", logData, staffId, "SUCCESS");
        return ResponseEntity.ok(new FindAndUpdateClassInfo(
                level.getLevelID(), level.getLevelName(), level.getStaff().getStaffId()
        ));
    }

    public ResponseEntity<?> updateClassInfo(FindAndUpdateClassInfo updateInfo, String staffId) {
        String logData = "Level Id: " + updateInfo.getLevelId() + " Selected Staff Id: " + updateInfo.getStaffId() + " Level Name: " + updateInfo.getLevelName();

        Staffs staff = staffsRepository.findByStaffId(updateInfo.getStaffId()).orElse(null);
        if (staff == null) {
            loggingService.logActivity("UPDATE_GRADE_INFO", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Selected Staff Not Found");
        }

        Level level = levelRepository.findByLevelID(updateInfo.getLevelId()).orElse(null);
        if (level == null) {
            loggingService.logActivity("UPDATE_GRADE_INFO", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grade Not Found");
        }

        level.setLevelName(updateInfo.getLevelName());
        level.setStaff(staff);
        levelRepository.save(level);

        loggingService.logActivity("UPDATE_GRADE_INFO", logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> findSemesterInfo(String semesterId, String staffId) {
        String logData = "Semester Id: " + semesterId;

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logActivity("FIND_SEMESTER", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Semester not found");
        }

        FindSemester findSemester = new FindSemester();
        findSemester.setSemesterID(semesterId);
        findSemester.setSemesterName(semester.getSemesterName());
        findSemester.setSemesterStartDate(semester.getSemesterStartDate().toString());
        findSemester.setSemesterEndDate(semester.getSemesterEndDate().toString());
        findSemester.setAcademicYear(semester.getAcademicYear());

        loggingService.logActivity("FIND_SEMESTER", logData, staffId, "SUCCESS");
        return ResponseEntity.ok(findSemester);
    }

    public ResponseEntity<?> updateSemesterInfo(FindSemester updateInfo, String staffId) {
        String logData = "Semester Id: " + updateInfo.getSemesterID() + " Semester Name: " + updateInfo.getSemesterName() +
                " Semester start: " + updateInfo.getSemesterStartDate() + " Semester End: " + updateInfo.getSemesterEndDate() +
                " Academic Year: " + updateInfo.getAcademicYear();

        Semester semester = semesterRepository.findBySemesterID(updateInfo.getSemesterID()).orElse(null);
        if (semester == null) {
            loggingService.logActivity("UPDATE_SEMESTER", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Semester do not exist");
        }

        semester.setSemesterName(updateInfo.getSemesterName());
        semester.setSemesterStartDate(LocalDate.parse(updateInfo.getSemesterStartDate()));
        semester.setSemesterEndDate(LocalDate.parse(updateInfo.getSemesterEndDate()));
        semester.setAcademicYear(updateInfo.getAcademicYear());
        semesterRepository.save(semester);

        loggingService.logActivity("UPDATE_SEMESTER", logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public List<SubjectsHolder> getLevelSubjects(String levelId) throws Exception {
        List<SubjectsHolder> subjectsHolders = new ArrayList<>();

        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Grade not found"));

        List<Subjects> subjects = level.getSubjects();
        if (subjects == null || subjects.isEmpty()) {
            throw new Exception("Grade has no subjects");
        }

        for (Subjects subject: subjects) {
            SubjectsHolder sub = new SubjectsHolder(
                    subject.getSubjectId(),
                    subject.getSubjectName()
            );
            subjectsHolders.add(sub);
        }
        return subjectsHolders;
    }

}
