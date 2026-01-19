package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Academics.GradeInfoResponse;
import com.codewithben.schoolmanagementsystem.DTO.Academics.LevelCaching;
import com.codewithben.schoolmanagementsystem.DTO.Academics.SemesterCaching;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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

            staffs.setLevel(level);

            staffsRepository.save(staffs);

            return "Success";
        }
        throw new Exception("Level already exists");
    }

    public List<LevelCaching> loadLevelInfo(String staffId) {
        Staffs staffs = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staffs == null)
            return new ArrayList<>();

        Institution institution = staffs.getInstitution();
        if (institution == null)
            return new ArrayList<>();

        List<Level> levels = institution.getLevel();

        return levels.stream().map(
                loadInfo -> {
                    String levelName = loadInfo.getLevelName();
                    String levelID = loadInfo.getLevelID();

                    System.out.println("Level name: " + levelName + " level ID: " + levelID);

                    return new LevelCaching(levelID, levelName);
                }
        ).collect(Collectors.toList());
    }

    public List<SemesterCaching> loadSemesterInfo(String staffId) throws Exception {
        Staffs staffs = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staffs == null)
            throw new Exception("Staff not found");

        Institution institution = institutiionRepository.findByInstitutionId(staffs.getInstitution().getInstitutionId()).orElse(null);
        if (institution == null)
            throw new Exception("Institution not found");

        List<Semester> semesters = institution.getSemester();

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
        return "Success";
    }

    public GradeInfoResponse loadGradeNameAndSize(String staffId) throws  Exception {
        Staffs staffs = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staffs == null)
            throw new Exception("Staff not found");

        Level level = staffs.getLevel();
        if (level == null)
            throw new Exception("Level not found");

        String levelName = level.getLevelName();
        String[] response = levelName.split("_");
        String gradeName = null;
        String gradeNumber = null;
        if (response.length == 2) {
            gradeName = response[0];
            gradeNumber = response[1];
        }
        long studentCount = level.getStudents().size();
        System.out.println("GradeName: " + gradeName + " gradeNumber: " + gradeNumber + " studentCount: " + studentCount);

        return new GradeInfoResponse(gradeName, gradeNumber, String.valueOf(studentCount));
    }
}
