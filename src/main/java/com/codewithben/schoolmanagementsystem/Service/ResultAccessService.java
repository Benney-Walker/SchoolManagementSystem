package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Constants.StaffRoles;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Results;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.StaffRolesEntity;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Entity.Subjects;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.ResultsRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Repository.SubjectsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Who may see results and report cards, and lookups limited to the caller's own school.
 *
 * Used by the four results views and the four report PDFs. They used to look up classes,
 * terms, subjects and students by id alone, so any logged-in staff member could read any
 * school's results (ids are sequential). Another school's record is answered exactly like a
 * missing one (404), so nothing about other schools leaks.
 *
 * Roles (Ben, 2026-11-27): every staff role except ACCOUNTANT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultAccessService {

    static final Set<StaffRoles> RESULT_VIEWERS =
            Set.of(StaffRoles.TEACHING_STAFF, StaffRoles.PRINCIPAL, StaffRoles.ADMINISTRATOR);

    private final StaffsRepository staffsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final SubjectsRepository subjectsRepository;

    private final ResultsRepository resultsRepository;

    private final LoggingService loggingService;

    /** A looked-up value, or the response to send instead. */
    public record Lookup<T>(T value, ResponseEntity<?> error) {

        static <T> Lookup<T> found(T value) {
            return new Lookup<>(value, null);
        }

        static <T> Lookup<T> refused(ResponseEntity<?> error) {
            return new Lookup<>(null, error);
        }

        public boolean failed() {
            return error != null;
        }
    }

    /** The caller, if they may view results: 401 when unknown, 403 for accountants. */
    public Lookup<Staffs> viewer(String staffId, LogType logType) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getInstitution() == null) {
            return Lookup.refused(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Session expired. Login again"
            )));
        }
        if (!canViewResults(staff)) {
            loggingService.logGeneralActivity(logType, LogAction.READ, "Not allowed to view results", staffId, LogStatus.FAILED);
            return Lookup.refused(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "You are not allowed to view results"
            )));
        }
        return Lookup.found(staff);
    }

    public Lookup<Level> level(Staffs staff, String levelId, LogType logType) {
        Level level = levelId == null ? null : levelRepository
                .findByLevelIDAndInstitution_InstitutionId(levelId, staff.getInstitution().getInstitutionId())
                .orElse(null);
        if (level == null) {
            return notFound(staff, logType, "Invalid class Id");
        }
        return Lookup.found(level);
    }

    public Lookup<Semester> semester(Staffs staff, String semesterId, LogType logType) {
        Semester semester = semesterId == null ? null : semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null || !sameSchool(staff, semester.getInstitution())) {
            return notFound(staff, logType, "Invalid term Id");
        }
        return Lookup.found(semester);
    }

    /** A subject of the given class (and therefore of the caller's school). */
    public Lookup<Subjects> subject(Staffs staff, String subjectId, Level level, LogType logType) {
        Subjects subject = subjectId == null ? null : subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null || subject.getLevel() == null
                || !Objects.equals(subject.getLevel().getLevelID(), level.getLevelID())) {
            return notFound(staff, logType, "Invalid subject Id");
        }
        return Lookup.found(subject);
    }

    /*
     * A student's result for a term at the caller's school. A list lookup, so a duplicated
     * result row (possible from concurrent score saves) doesn't turn every report card for
     * that student into a 500.
     */
    public Lookup<Results> studentResult(Staffs staff, String studentId, Semester semester, LogType logType) {
        List<Results> found = studentId == null ? List.of() : resultsRepository
                .findAllBySemester_SemesterIDAndStudent_StudentIdIn(semester.getSemesterID(), List.of(studentId));
        if (found.size() > 1) {
            log.warn("Student {} has {} results for term {}; using the first", studentId, found.size(), semester.getSemesterID());
        }
        Results result = found.isEmpty() ? null : found.getFirst();
        if (result == null || result.getStudent() == null || !sameSchool(staff, result.getStudent().getInstitution())) {
            return notFound(staff, logType, "No record found for this student for this term");
        }
        return Lookup.found(result);
    }

    static boolean canViewResults(Staffs staff) {
        return staff.getRoles() != null && staff.getRoles().stream()
                .map(StaffRolesEntity::getStaffRole)
                .anyMatch(RESULT_VIEWERS::contains);
    }

    static boolean sameSchool(Staffs staff, Institution institution) {
        return staff.getInstitution() != null && institution != null
                && Objects.equals(staff.getInstitution().getInstitutionId(), institution.getInstitutionId());
    }

    private <T> Lookup<T> notFound(Staffs staff, LogType logType, String message) {
        loggingService.logGeneralActivity(logType, LogAction.READ, message, staff.getStaffId(), LogStatus.FAILED);
        return Lookup.refused(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", message
        )));
    }
}
