package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.*;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.TodaysAbsentees;
import com.codewithben.schoolmanagementsystem.DTO.Students.FindStudentDTO;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsHolder;
import com.codewithben.schoolmanagementsystem.DTO.Students.UpdateStudentPersonalData;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Entity.AttendanceRecords;
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

@AllArgsConstructor
@Service
public class StudentService {
    private final StaffsRepository staffsRepository;

    private final LevelRepository levelRepository;

    private final StudentsRepository studentsRepository;

    private final UtilityClass utilityClass;

    private final ResultsRepository resultsRepository;

    private final InstitutiionRepository institutionRepository;

    private final LoggingService loggingService;

    //Method for adding new student
    @Transactional
    public ResponseEntity<?> addNewStudent(String firstName, String lastName, String gender, String dateOfBirth, String hometown,
                                           String parentName, String parentContact, String levelId, boolean isNew, String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.CREATE, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.CREATE, "Invalid Class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        LocalDate dob = LocalDate.parse(dateOfBirth);

        boolean duplicateExists = studentsRepository
                .existsByFirstNameAndLastNameAndDateOfBirthAndInstitution_InstitutionId(
                        firstName, lastName, dob, staff.getInstitution().getInstitutionId()
                );
        if (duplicateExists) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.CREATE, "Student already exist", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Student already exist"
            ));
        }

        String studentId = utilityClass.generateEntityId("STUDENT");

        //Saving new student
        Students student = new Students();
        student.setStudentId(studentId);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setGender(gender);
        student.setDateOfBirth(LocalDate.parse(dateOfBirth));
        student.setHomeTown(hometown);
        student.setParentName(parentName);
        student.setParentPhoneNumber(parentContact);
        student.setLevel(level);
        student.setNew(isNew);
        student.setRegistrationDate(LocalDate.now());
        student.setInstitution(staff.getInstitution());
        student.setStudentStatus(StudentStatus.ACTIVE);

        studentsRepository.saveAndFlush(student);

        //Add student to level list
        List<Students> levelStudents = level.getStudents();
        if (levelStudents == null) {
            levelStudents = new ArrayList<>();
        }
        levelStudents.add(student);
        level.setStudents(levelStudents);
        levelRepository.save(level);

        //Adding student to institution
        List<Students> students = staff.getInstitution().getStudents();
        if (students == null) {
            students = new ArrayList<>();
        }
        students.add(student);
        staff.getInstitution().setStudents(students);
        institutionRepository.save(staff.getInstitution());

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.CREATE,
                "Added new student: " + firstName + " " + lastName + " for " + level.getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(studentId);

    }

    public ResponseEntity<?> findStudent(String studentId, String staffId) {

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid Student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.READ,
                "Fetched student information: " + student.getFirstName() + " " + student.getLastName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(getStudentData(student));
    }

    private FindStudentDTO getStudentData(Students student) {
        try {
            FindStudentDTO findStudentDTO = new FindStudentDTO();
            findStudentDTO.setStudentId(student.getStudentId());
            findStudentDTO.setFirstName(student.getFirstName());
            findStudentDTO.setLastName(student.getLastName());
            findStudentDTO.setGender(student.getGender());
            findStudentDTO.setParentName(student.getParentName());
            findStudentDTO.setParentPhoneNumber(student.getParentPhoneNumber());
            findStudentDTO.setDateOfBirth(student.getDateOfBirth().toString());
            findStudentDTO.setGradeId(student.getLevel().getLevelID());
            findStudentDTO.setStatus(student.getStudentStatus().toString());
            findStudentDTO.setHomeTown(student.getHomeTown());
            return findStudentDTO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> countTotalStudents(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Students> students = utilityClass.getActiveStudents(staff.getInstitution().getStudents());
        if (students == null || students.isEmpty()) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Institution has no students", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no student"
            ));
        }

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.READ,
                "Read total student count",
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(students.size());
    }

    public ResponseEntity<?> updateStudentPersonalData(UpdateStudentPersonalData data, String staffId) {

        Students student = studentsRepository.findByStudentId(data.getStudentId()).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Student Id"
            ));
        }

        Level level = levelRepository.findByLevelID(data.getGradeId()).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        try {
            student.setFirstName(data.getFirstName());
            student.setLastName(data.getLastName());
            student.setGender(data.getGender());
            student.setParentName(data.getParentName());
            student.setParentPhoneNumber(data.getParentPhoneNumber());
            student.setStudentStatus(StudentStatus.valueOf(data.getStatus()));
            student.setLevel(level);
            student.setDateOfBirth(LocalDate.parse(data.getDateOfBirth()));
            student.setHomeTown(data.getHomeTown());
            studentsRepository.save(student);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.UPDATE,
                "Updated student information: " + data.getFirstName() + " " + data.getLastName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }


    public ResponseEntity<?> getGradeStudents(String levelId, String staffId) {

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        List<Students> levelStudents = utilityClass.getActiveStudents(level.getStudents());
        if (levelStudents == null || levelStudents.isEmpty()) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Class has no students", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Class has no students"
            ));
        }

        List<StudentsHolder> studentsHolders = new ArrayList<>();
        for (Students student : levelStudents) {
            StudentsHolder stu = new StudentsHolder(
                    student.getStudentId(),
                    student.getFirstName() + " " + student.getLastName()
            );
            studentsHolders.add(stu);
        }

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.READ,
                "Fetched student: " + level.getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(studentsHolders);
    }

    public ResponseEntity<?> promoteStudent(String studentId, String promotionClassId, String semesterId, String staffId) {

        Results result = resultsRepository.findByStudent_StudentIdAndSemester_SemesterID(
                studentId, semesterId
        ).orElse(null);
        if (result == null) {
            loggingService.logGeneralActivity(
                    LogType.STUDENT, LogAction.UPDATE,
                    "No reference results found for promotion",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No reference results found for promotion"
            ));
        }

        if (!result.isReady()) {
            loggingService.logGeneralActivity(
                    LogType.STUDENT, LogAction.UPDATE,
                    "Student result not complete",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Student result not complete"
            ));
        }

        if (!result.getSemester().getSemesterName().equals("THIRD_TERM")) {
            loggingService.logGeneralActivity(
                    LogType.STUDENT, LogAction.UPDATE,
                    "Promotions can only be done on Third Terms",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(Map.of(
                    "message", "Promotions can only be done on Third Terms"
            ));
        }

        Level promotionClass = levelRepository.findByLevelID(promotionClassId).orElse(null);
        if (promotionClass == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Invalid promotion Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Promotion class Id"
            ));
        }

        Students student = result.getStudent();

        result.setPromotionTo(promotionClass.getLevelName());
        resultsRepository.save(result);

        student.setLevel(promotionClass);
        studentsRepository.save(student);

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.PROMOTE,
                "Promoted " + student.getFirstName() + " " + student.getLastName() + " to " + promotionClass.getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> repeatStudent(String studentId, String semesterId, String staffId) {

        Results result = resultsRepository.findByStudent_StudentIdAndSemester_SemesterID(
                studentId, semesterId
        ).orElse(null);
        if (result == null) {
            loggingService.logGeneralActivity(
                    LogType.STUDENT, LogAction.UPDATE,
                    "No reference results found for Repetition",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No reference results found for Repetition"
            ));
        }

        if (!result.getSemester().getSemesterName().equals("THIRD_TERM")) {
            loggingService.logGeneralActivity(
                    LogType.STUDENT, LogAction.UPDATE,
                    "Repetition can only be done on Third Terms",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(Map.of(
                    "message", "Repetition can only be done on Third Terms"
            ));
        }

        if (!result.isReady()) {
            loggingService.logGeneralActivity(
                    LogType.STUDENT, LogAction.UPDATE,
                    "Student result not complete",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Student result not complete"
            ));
        }

        result.setPromotionTo("Repeated");
        result.setClassSize(utilityClass.getActiveStudents(result.getStudent().getLevel().getStudents()).size());
        resultsRepository.save(result);

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.PROMOTE,
                "Repeated " + result.getStudent().getFirstName() + " " + result.getStudent().getLastName() + " at " + result.getLevel().getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

}