package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Academics.LevelCaching;
import com.codewithben.schoolmanagementsystem.DTO.Academics.SemesterCaching;
import com.codewithben.schoolmanagementsystem.DTO.Institution.*;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
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

    private final StudentsRepository studentsRepository;

    private final InstitutiionRepository institutiionRepository;

    private final SemesterRepository semesterRepository;

    public LevelService(StaffsRepository staffsRepository, LevelRepository levelRepository,  UtilityClass utilityClass,
                        StudentsRepository studentsRepository, InstitutiionRepository institutiionRepository, SemesterRepository semesterRepository) {
        this.staffsRepository = staffsRepository;
        this.levelRepository = levelRepository;
        this.utilityClass = utilityClass;
        this.studentsRepository = studentsRepository;
        this.institutiionRepository = institutiionRepository;
        this.semesterRepository = semesterRepository;
    }

    @Transactional
    public String addNewClass(String className, String staffID) throws Exception {
        Staffs staffs = staffsRepository.findByStaffId(staffID).orElse(null);
        if (staffs == null) {
            throw new Exception("Staff not found");
        }

        Institution institution = staffs.getInstitution();
        if (institution == null) {
            throw new Exception("Institution not found");
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

            return "Grade added successfully";
        }
        throw new Exception("Level already exists");
    }

    public List<LevelCaching> loadLevelInfo(String staffId) throws Exception {
        Staffs staffs = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("You can't access staff list"));

        Institution institution = staffs.getInstitution();
        if (institution == null) {
            throw new Exception("Institution not found");
        }

        List<Level> levels = institution.getLevel();
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

    public String addNewSemester(String semesterName, LocalDate startDate, LocalDate endDate,
                                 String academicYear, String staffId) throws Exception {
        Staffs staffs = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staffs == null)
            throw new Exception("Staff not found");

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
        return "Successfully added semester";
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

    public FindAndUpdateClassInfo findClassInfo(String levelId) throws Exception {

        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Level not found. Verify entered Id"));

        return new FindAndUpdateClassInfo(
                level.getLevelID(), level.getLevelName(), level.getStaff().getStaffId()
        );
    }

    public String updateClassInfo(FindAndUpdateClassInfo updateInfo) throws Exception {
        if (updateInfo == null) {
            throw new Exception("Update info empty");
        }

        Staffs staff = staffsRepository.findByStaffId(updateInfo.getStaffId())
                .orElseThrow(() -> new Exception("Staff not found"));

        Level level = levelRepository.findByLevelID(updateInfo.getLevelId())
                .orElseThrow(() -> new Exception("Level not found"));

        level.setLevelName(updateInfo.getLevelName());
        level.setStaff(staff);
        levelRepository.save(level);

        return "Grade information updated";
    }

    public FindSemester findSemesterInfo(String semesterId) throws Exception {
        Semester semester = semesterRepository.findBySemesterID(semesterId)
                .orElseThrow(() -> new Exception("Semester not found"));

        FindSemester findSemester = new FindSemester();
        findSemester.setSemesterID(semesterId);
        findSemester.setSemesterName(semester.getSemesterName());
        findSemester.setSemesterStartDate(semester.getSemesterStartDate().toString());
        findSemester.setSemesterEndDate(semester.getSemesterEndDate().toString());
        findSemester.setAcademicYear(semester.getAcademicYear());

        return findSemester;

    }

    public String updateSemesterInfo(FindSemester updateInfo) throws Exception {
        Semester semester = semesterRepository.findBySemesterID(updateInfo.getSemesterID())
                .orElseThrow(() -> new Exception("Semester not found"));

        semester.setSemesterName(updateInfo.getSemesterName());
        semester.setSemesterStartDate(LocalDate.parse(updateInfo.getSemesterStartDate()));
        semester.setSemesterEndDate(LocalDate.parse(updateInfo.getSemesterEndDate()));
        semester.setAcademicYear(updateInfo.getAcademicYear());
        semesterRepository.save(semester);

        return "Semester information updated";
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
