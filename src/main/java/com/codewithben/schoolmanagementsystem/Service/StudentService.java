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

    private final SemesterRepository semesterRepository;

    private final InstitutiionRepository institutionRepository;

    private final AttendanceRecordsRepository attendanceRecordsRepository;

    private final LoggingService loggingService;

    //Method for adding new student
    @Transactional
    public ResponseEntity<?> addNewStudent(String firstName, String lastName, String gender, String dateOfBirth, String hometown,
                                           String parentName, String parentContact, String levelId, String staffId) {

        if (parentContact.length() != 10) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.CREATE, "Invalid parent phone number", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Invalid parent phone number"
            ));
        }

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

        Students student = studentsRepository.findByFirstNameAndLastName(firstName, lastName).orElse(null);
        if (student == null) {
            String studentId = utilityClass.generateEntityId("STUDENT");

            //Saving new student
            student = new Students();
            student.setStudentId(studentId);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setGender(gender);
            student.setDateOfBirth(LocalDate.parse(dateOfBirth));
            student.setHomeTown(hometown);
            student.setParentName(parentName);
            student.setParentPhoneNumber(parentContact);
            student.setLevel(level);
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

            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.CREATE, "N/A", staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok(studentId);
        }

        loggingService.logGeneralActivity(LogType.STUDENT, LogAction.CREATE, "Student already exist", staffId, LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Student already exist"
        ));
    }

    public ResponseEntity<?> findStudent(String studentId, String staffId) {

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid Student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
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

        loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
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

        if (data.getParentPhoneNumber().length() != 10) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Invalid parent phone number", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Invalid parent phone number"
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

        loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }


    public ResponseEntity<?> getGradeStudents(String levelId, String staffId) {
        String logData = "Class Id: " + levelId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        List<Students> levelStudents = utilityClass.getActiveStudents(level.getStudents());
        if (levelStudents == null || levelStudents.isEmpty()) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Class has no students", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
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

        loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid class Id", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(studentsHolders);
    }

    public ResponseEntity<?> promoteStudent(String studentId, String levelId, String staffId) {

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.PROMOTE, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.PROMOTE, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        student.setLevel(level);
        studentsRepository.save(student);

        loggingService.logGeneralActivity(LogType.STUDENT, LogAction.PROMOTE, "Invalid student Id", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

}
