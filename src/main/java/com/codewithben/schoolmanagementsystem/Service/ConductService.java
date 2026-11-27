package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.ConductRatings;
import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Constants.StaffRoles;
import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductRecord;
import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductReport;
import com.codewithben.schoolmanagementsystem.Entity.Conduct;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Results;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.StaffRolesEntity;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Entity.Students;
import com.codewithben.schoolmanagementsystem.Repository.ConductRepository;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.ResultsRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Repository.StudentsRepository;
import com.codewithben.schoolmanagementsystem.Utility.ConductRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Conduct records: six ratings plus the class teacher's remark and a peculiar issue, one per
 * student per term, attached to the student's term result. Report cards require it.
 *
 * Who: TEACHING_STAFF, PRINCIPAL and ADMINISTRATOR of the student's own school. /api/staff
 * admits every staff role (including ACCOUNTANT), so the role is checked here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConductService {

    private static final Set<StaffRoles> CONDUCT_ROLES =
            Set.of(StaffRoles.TEACHING_STAFF, StaffRoles.PRINCIPAL, StaffRoles.ADMINISTRATOR);

    private final ConductRepository conductRepository;

    private final LoggingService loggingService;

    private final ResultsRepository resultsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final StaffsRepository staffsRepository;

    private final StudentsRepository studentsRepository;

    /*
     * The class's conduct sheet for a term, sorted by student name.
     *
     * Rows: every student with a result in this class and term, plus, while the term hasn't
     * ended, every ACTIVE student of the class who has no result yet (a late joiner, or a
     * student whose attendance hasn't been marked), so the teacher can record their conduct
     * too. A row without a saved record has blank ratings and saved=false; it used to show
     * GOOD everywhere, which looked saved while the report card still refused the student.
     */
    public ResponseEntity<?> getStudentsConduct(String levelId, String semesterId, String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getInstitution() == null) {
            return sessionExpired();
        }
        String institutionId = staff.getInstitution().getInstitutionId();

        Level level = levelRepository.findByLevelIDAndInstitution_InstitutionId(levelId, institutionId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null || !sameSchool(staff, semester.getInstitution())) {
            loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.READ, "Invalid semester Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid semester Id"
            ));
        }

        if (!canRecordConduct(staff)) {
            return forbidden(staffId, LogAction.READ);
        }

        List<StudentConductRecord> conductList = new ArrayList<>();
        Set<String> listed = new HashSet<>();
        for (Results result : resultsRepository.findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId)) {
            Students student = result.getStudent();
            if (student == null || !listed.add(student.getStudentId())) {
                continue;
            }
            conductList.add(toRecord(student, semesterId, result.getConduct()));
        }

        boolean termOngoing = semester.getSemesterEndDate() == null
                || !semester.getSemesterEndDate().isBefore(LocalDate.now());
        if (termOngoing && level.getStudents() != null) {
            for (Students student : level.getStudents()) {
                if (student.getStudentStatus() == StudentStatus.ACTIVE && listed.add(student.getStudentId())) {
                    conductList.add(toRecord(student, semesterId, null));
                }
            }
        }

        conductList.sort(Comparator.comparing(
                (StudentConductRecord record) -> nullToEmpty(record.getStudentName()).toLowerCase(Locale.ROOT)));

        return ResponseEntity.ok(conductList);
    }

    /*
     * Saves one student's conduct for a term (create or update).
     *
     * - The record is validated first (ConductRules): a bad rating or an over-long remark is a
     *   400 naming the field. It used to be a 500 (ConductRatings.valueOf / "Data too long"),
     *   which the desktop outbox retries forever and which blocked every queued change.
     * - The student and term must belong to the caller's school (404 otherwise, the same as
     *   an unknown id).
     * - If the student has no result for the term yet, one is created (ACTIVE students in a
     *   class only), the same way saving scores does. It used to be a 404 that never cleared.
     */
    public ResponseEntity<?> saveStudentConducts(String staffId, StudentConductRecord record) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getInstitution() == null) {
            return sessionExpired();
        }
        if (!canRecordConduct(staff)) {
            return forbidden(staffId, LogAction.UPDATE);
        }

        List<ConductRules.FieldError> errors = ConductRules.validate(record);
        if (!errors.isEmpty()) {
            loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.UPDATE,
                    "Rejected conduct record: " + errors.getFirst().message(), staffId, LogStatus.FAILED);
            return ResponseEntity.badRequest().body(Map.of(
                    "message", errors.getFirst().message(),
                    "errors", errors
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(record.getSemesterId()).orElse(null);
        if (semester == null || !sameSchool(staff, semester.getInstitution())) {
            loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.UPDATE, "Invalid semester Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid term Id"
            ));
        }

        // A list lookup, so a duplicated result row (possible from concurrent score saves)
        // doesn't make every conduct save for that student fail.
        List<Results> found = resultsRepository.findAllBySemester_SemesterIDAndStudent_StudentIdIn(
                semester.getSemesterID(), List.of(record.getStudentId()));
        if (found.size() > 1) {
            log.warn("Student {} has {} results for term {}; using the first", record.getStudentId(), found.size(),
                    semester.getSemesterID());
        }
        Results result = found.isEmpty() ? null : found.getFirst();

        if (result == null) {
            Students student = studentsRepository.findByStudentId(record.getStudentId()).orElse(null);
            if (student == null || !sameSchool(staff, student.getInstitution())) {
                return studentNotFound(staffId);
            }
            if (student.getStudentStatus() != StudentStatus.ACTIVE || student.getLevel() == null) {
                loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.UPDATE,
                        "Conduct refused, student not active: " + student.getStudentId(), staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", "This student is not active, so no conduct can be recorded for this term"
                ));
            }
            result = newResult(student, semester, staff);
        } else if (result.getStudent() == null || !sameSchool(staff, result.getStudent().getInstitution())) {
            return studentNotFound(staffId);
        }

        Conduct conduct = result.getConduct();
        boolean creating = conduct == null;
        if (creating) {
            conduct = new Conduct();
            conduct.setStudent(result.getStudent());
            conduct.setResults(result);
        }
        conduct.setRegular(rating(record.getRegular()));
        conduct.setPunctual(rating(record.getPunctual()));
        conduct.setPhysicalAppearance(rating(record.getPhysicalAppearance()));
        conduct.setSocial(rating(record.getSocial()));
        conduct.setEmotional(rating(record.getEmotional()));
        conduct.setCognitiveSkills(rating(record.getCognitiveSkills()));
        conduct.setClassTeacherRemark(record.getConductRemark().trim());
        conduct.setPeculiarIssue(record.getPeculiarIssue().trim());

        try {
            conductRepository.saveAndFlush(conduct);
        } catch (DataIntegrityViolationException e) {
            // One conduct per result (unique key): another device created it at the same moment.
            log.warn("Conduct for student {} term {} was created concurrently", record.getStudentId(),
                    semester.getSemesterID(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "This conduct record was just saved from another device. Reload and try again"
            ));
        }

        loggingService.logGeneralActivity(LogType.CONDUCT, creating ? LogAction.CREATE : LogAction.UPDATE,
                (creating ? "Added" : "Updated") + " conduct record for " + record.getStudentId()
                        + " (" + result.getStudent().getFirstName() + " " + result.getStudent().getLastName() + ")",
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    /** Conduct as printed on report cards; missing values come out empty rather than failing. */
    public StudentConductReport getStudentConductReport(Conduct conduct) {
        return StudentConductReport.builder()
                .regular(ratingName(conduct.getRegular()))
                .punctual(ratingName(conduct.getPunctual()))
                .physicalAppearance(ratingName(conduct.getPhysicalAppearance()))
                .social(ratingName(conduct.getSocial()))
                .emotional(ratingName(conduct.getEmotional()))
                .cognitiveSkills(ratingName(conduct.getCognitiveSkills()))
                .facilitatorRemark(nullToEmpty(conduct.getClassTeacherRemark()))
                .peculiarIssue(nullToEmpty(conduct.getPeculiarIssue()))
                .build();
    }

    private StudentConductRecord toRecord(Students student, String semesterId, Conduct conduct) {
        StudentConductRecord.StudentConductRecordBuilder row = StudentConductRecord.builder()
                .studentId(student.getStudentId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .semesterId(semesterId)
                .saved(conduct != null);
        if (conduct == null) {
            return row.regular("").punctual("").physicalAppearance("").social("").emotional("")
                    .cognitiveSkills("").conductRemark("").peculiarIssue("").build();
        }
        // Null-safe: one bad row used to fail the whole class sheet with a NullPointerException.
        return row.regular(ratingName(conduct.getRegular()))
                .punctual(ratingName(conduct.getPunctual()))
                .physicalAppearance(ratingName(conduct.getPhysicalAppearance()))
                .social(ratingName(conduct.getSocial()))
                .emotional(ratingName(conduct.getEmotional()))
                .cognitiveSkills(ratingName(conduct.getCognitiveSkills()))
                .conductRemark(nullToEmpty(conduct.getClassTeacherRemark()))
                .peculiarIssue(nullToEmpty(conduct.getPeculiarIssue()))
                .build();
    }

    /** A term result for a student who has none yet, with the same starting values as on save-scores. */
    private Results newResult(Students student, Semester semester, Staffs staff) {
        Results result = new Results();
        result.setStudent(student);
        result.setSemester(semester);
        result.setLevel(student.getLevel());
        result.setUpdatedBy(staff);
        result.setCreatedAt(LocalDate.now());
        result.setTotalScore(0.0);
        result.setAverageScore(0.0);
        return resultsRepository.save(result);
    }

    private static ConductRatings rating(String value) {
        return ConductRules.parseRating(value).orElseThrow(); // validated above
    }

    private static String ratingName(ConductRatings rating) {
        return rating == null ? "" : rating.name();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean sameSchool(Staffs staff, Institution institution) {
        return staff.getInstitution() != null && institution != null
                && Objects.equals(staff.getInstitution().getInstitutionId(), institution.getInstitutionId());
    }

    private static boolean canRecordConduct(Staffs staff) {
        return staff.getRoles() != null && staff.getRoles().stream()
                .map(StaffRolesEntity::getStaffRole)
                .anyMatch(CONDUCT_ROLES::contains);
    }

    private ResponseEntity<?> forbidden(String staffId, LogAction action) {
        loggingService.logGeneralActivity(LogType.CONDUCT, action, "Not allowed to record conduct", staffId, LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "message", "You are not allowed to record conduct"
        ));
    }

    private ResponseEntity<?> studentNotFound(String staffId) {
        loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.UPDATE, "Student not found", staffId, LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", "Student not found"
        ));
    }

    private static ResponseEntity<?> sessionExpired() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", "Session expired. Login again"
        ));
    }
}
