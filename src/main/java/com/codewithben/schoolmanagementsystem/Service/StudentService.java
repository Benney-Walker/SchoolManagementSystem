package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.*;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.TodaysAbsentees;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Student.NewStudentFee;
import com.codewithben.schoolmanagementsystem.DTO.Students.AddNewStudent;
import com.codewithben.schoolmanagementsystem.DTO.Students.FindStudentDTO;
import com.codewithben.schoolmanagementsystem.DTO.Students.GraduatedStudentDTO;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsHolder;
import com.codewithben.schoolmanagementsystem.DTO.Students.UpdateStudentPersonalData;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Entity.AttendanceRecords;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.StudentEnrolment;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private final RabbitMQProducer rabbitMQProducer;

    private final LoggingService loggingService;

    //Method for adding new student
    @Transactional
    public ResponseEntity<?> addNewStudent(AddNewStudent addNewStudent, String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(
                    LogType.STUDENT,
                    LogAction.CREATE,
                    "Could not add student! Contact developer",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Could not add student! Contact developer"
            ));
        }

        Level level = levelRepository.findByLevelID(addNewStudent.getLevelId()).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.CREATE, "Invalid Class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        LocalDate dob = LocalDate.parse(addNewStudent.getDateOfBirth(), DateTimeFormatter.ISO_DATE);

        boolean duplicateExists = studentsRepository
                .existsByFirstNameAndLastNameAndDateOfBirthAndInstitution_InstitutionId(
                        addNewStudent.getFirstName(), addNewStudent.getLastName(), dob, staff.getInstitution().getInstitutionId()
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
        student.setFirstName(addNewStudent.getFirstName());
        student.setLastName(addNewStudent.getLastName());
        student.setGender(addNewStudent.getGender());
        student.setDateOfBirth(dob);
        student.setHomeTown(addNewStudent.getHometown());
        student.setParentName(addNewStudent.getParentName());
        student.setParentPhoneNumber(addNewStudent.getGuardianContact());
        student.setLevel(level);
        student.setNew(addNewStudent.isNew());
        student.setBorder(addNewStudent.isBorder());
        student.setRegistrationDate(LocalDate.now());
        student.setInstitution(staff.getInstitution());
        student.setStudentStatus(StudentStatus.ACTIVE);

        studentsRepository.saveAndFlush(student);

        NewStudentFee newStudentFee = NewStudentFee.builder().levelId(level.getLevelID()).studentId(studentId).build();
        rabbitMQProducer.sendStudentFeeCreationEvent(newStudentFee);

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.CREATE,
                "Added new student: "
                        + addNewStudent.getFirstName()
                        + " " + addNewStudent.getLastName()
                        + " for " + level.getLevelName(),
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

    /**
     * The caller's school's graduated students, newest graduation first (students
     * graduated before graduation details were recorded come last), then by name.
     * Optionally limited to one academic year, e.g. "2025/2026".
     * Returns 200 with an empty list when there are none.
     */
    public ResponseEntity<?> getGraduatedStudents(String academicYear, String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Could not load graduated students! Contact developer"
            ));
        }
        String institutionId = staff.getInstitution().getInstitutionId();

        Sort newestFirst = Sort.by(
                Sort.Order.desc("graduationDate"),
                Sort.Order.asc("lastName"),
                Sort.Order.asc("firstName"));
        List<Students> graduates = (academicYear == null || academicYear.isBlank())
                ? studentsRepository.findAllByInstitution_InstitutionIdAndStudentStatus(
                        institutionId, StudentStatus.GRADUATED, newestFirst)
                : studentsRepository.findAllByInstitution_InstitutionIdAndStudentStatusAndGraduationAcademicYear(
                        institutionId, StudentStatus.GRADUATED, academicYear.trim(), newestFirst);

        List<GraduatedStudentDTO> rows = new ArrayList<>(graduates.size());
        for (Students student : graduates) {
            rows.add(GraduatedStudentDTO.builder()
                    .studentId(student.getStudentId())
                    .studentName(student.getFirstName() + " " + student.getLastName())
                    .gender(student.getGender())
                    .parentName(student.getParentName())
                    .parentPhoneNumber(student.getParentPhoneNumber())
                    .graduationClassName(student.getGraduationClassName())
                    .graduationAcademicYear(student.getGraduationAcademicYear())
                    .graduationDate(student.getGraduationDate() == null ? null : student.getGraduationDate().toString())
                    .build());
        }

        loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Fetched graduated students", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(rows);
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
            // Graduated students have no current class.
            findStudentDTO.setGradeId(student.getLevel() == null ? null : student.getLevel().getLevelID());
            findStudentDTO.setStatus(student.getStudentStatus().toString());
            findStudentDTO.setHomeTown(student.getHomeTown());
            findStudentDTO.setGraduationDate(
                    student.getGraduationDate() == null ? null : student.getGraduationDate().toString());
            findStudentDTO.setGraduationClassName(student.getGraduationClassName());
            findStudentDTO.setGraduationAcademicYear(student.getGraduationAcademicYear());
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

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Could not update student! Contact developer"
            ));
        }
        String institutionId = staff.getInstitution().getInstitutionId();

        // Scoped to the caller's school: another school's student is treated as not found.
        Students student = studentsRepository
                .findByStudentIdAndInstitution_InstitutionId(data.getStudentId(), institutionId)
                .orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Student Id"
            ));
        }

        StudentStatus newStatus = parseStudentStatus(data.getStatus());
        if (newStatus == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Invalid student status", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Status must be ACTIVE, INACTIVE or GRADUATED"
            ));
        }

        // A graduated student has no class, so gradeId is only needed for other statuses.
        Level newClass = null;
        if (newStatus != StudentStatus.GRADUATED) {
            if (data.getGradeId() == null || data.getGradeId().isBlank()) {
                loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Class is required", staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "message", "Class (grade) is required"
                ));
            }
            newClass = levelRepository
                    .findByLevelIDAndInstitution_InstitutionId(data.getGradeId(), institutionId)
                    .orElse(null);
            if (newClass == null || StudentEnrolment.isGraduatedPlaceholder(newClass)) {
                loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Invalid class Id", staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "message", "Invalid class Id"
                ));
            }
        }

        boolean graduatingNow = newStatus == StudentStatus.GRADUATED
                && student.getStudentStatus() != StudentStatus.GRADUATED;

        applyPersonalDetails(data, student);
        StudentEnrolment.applyStatus(
                student, newStatus, newClass,
                graduatingNow ? resolveAcademicYear(student.getInstitution()) : null,
                LocalDate.now());
        studentsRepository.save(student);

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.UPDATE,
                (graduatingNow ? "Graduated student: " : "Updated student information: ")
                        + data.getFirstName() + " " + data.getLastName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    private void applyPersonalDetails(UpdateStudentPersonalData data, Students student) {
        student.setFirstName(data.getFirstName());
        student.setLastName(data.getLastName());
        student.setGender(data.getGender());
        student.setParentName(data.getParentName());
        student.setParentPhoneNumber(data.getParentPhoneNumber());
        // Format is guaranteed by @Pattern on the DTO.
        student.setDateOfBirth(LocalDate.parse(data.getDateOfBirth()));
        student.setHomeTown(data.getHomeTown());
    }

    private static StudentStatus parseStudentStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return StudentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Academic year to record on a graduation: the current term's, or, between
     * terms (graduation usually happens after third term ends), the academic year
     * of the most recent term that has started. Null if the school has no terms.
     */
    private String resolveAcademicYear(Institution institution) {
        Semester current = utilityClass.getCurrentSemester(institution);
        if (current != null) {
            return current.getAcademicYear();
        }
        List<Semester> semesters = institution.getSemester();
        if (semesters == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        Semester latestStarted = null;
        for (Semester semester : semesters) {
            LocalDate start = semester.getSemesterStartDate();
            if (start != null && !start.isAfter(today)
                    && (latestStarted == null || start.isAfter(latestStarted.getSemesterStartDate()))) {
                latestStarted = semester;
            }
        }
        return latestStarted == null ? null : latestStarted.getAcademicYear();
    }


    public ResponseEntity<?> getGradeStudents(String levelId, String staffId) {

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        List<Students> levelStudents = level.getStudents();
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

        if (result.getStudent().getStudentStatus() == StudentStatus.GRADUATED) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Student has graduated", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Student has graduated and can't be promoted"
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

        if (result.getStudent().getStudentStatus() == StudentStatus.GRADUATED) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.UPDATE, "Student has graduated", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Student has graduated and can't repeat a class"
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
        // The result's own class: same as the student's for a repeat, and never null.
        result.setClassSize(utilityClass.getActiveStudents(result.getLevel().getStudents()).size());
        resultsRepository.save(result);

        loggingService.logGeneralActivity(
                LogType.STUDENT, LogAction.PROMOTE,
                "Repeated " + result.getStudent().getFirstName() + " " + result.getStudent().getLastName() + " at " + result.getLevel().getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

}