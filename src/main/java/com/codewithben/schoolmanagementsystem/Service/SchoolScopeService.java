package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Constants.StaffRoles;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Repository.StudentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Lookups limited to the caller's school (issue 2).
 *
 * Ids are sequential (ST100100, STD100200, LV..., SE...), so any id taken from a request must be
 * checked against the caller's school before it is read or changed. Another school's record
 * is answered exactly like a missing one (404), so responses don't reveal other schools' ids.
 *
 * Usage:
 * <pre>
 *   Scoped&lt;Staffs&gt; caller = scope.caller(staffId);
 *   if (caller.failed()) return caller.error();
 *   Scoped&lt;Level&gt; level = scope.level(caller.value(), levelId, LogType.STUDENT, LogAction.READ);
 *   if (level.failed()) return level.error();
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class SchoolScopeService {

    private final StaffsRepository staffsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final StudentsRepository studentsRepository;

    private final LoggingService loggingService;

    /** A value, or the response to return instead. */
    public record Scoped<T>(T value, ResponseEntity<?> error) {

        static <T> Scoped<T> found(T value) {
            return new Scoped<>(value, null);
        }

        static <T> Scoped<T> refused(ResponseEntity<?> error) {
            return new Scoped<>(null, error);
        }

        public boolean failed() {
            return error != null;
        }
    }

    /** The logged-in staff member, with a school; else 401. */
    public Scoped<Staffs> caller(String staffId) {
        Staffs staff = staffId == null ? null : staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getInstitution() == null) {
            return Scoped.refused(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Session expired. Login again"
            )));
        }
        return Scoped.found(staff);
    }

    public Scoped<Level> level(Staffs staff, String levelId, LogType logType, LogAction action) {
        Level level = levelId == null ? null : levelRepository
                .findByLevelIDAndInstitution_InstitutionId(levelId, staff.getInstitution().getInstitutionId())
                .orElse(null);
        return level == null ? notFound(staff, logType, action, "Invalid class Id") : Scoped.found(level);
    }

    public Scoped<Semester> semester(Staffs staff, String semesterId, LogType logType, LogAction action) {
        Semester semester = semesterId == null ? null : semesterRepository.findBySemesterID(semesterId).orElse(null);
        return semester == null || !sameSchool(staff, semester.getInstitution())
                ? notFound(staff, logType, action, "Invalid term Id")
                : Scoped.found(semester);
    }

    public Scoped<Students> student(Staffs staff, String studentId, LogType logType, LogAction action) {
        Students student = studentId == null ? null : studentsRepository
                .findByStudentIdAndInstitution_InstitutionId(studentId, staff.getInstitution().getInstitutionId())
                .orElse(null);
        return student == null ? notFound(staff, logType, action, "Invalid student Id") : Scoped.found(student);
    }

    public static boolean sameSchool(Staffs staff, Institution institution) {
        return staff.getInstitution() != null && institution != null
                && Objects.equals(staff.getInstitution().getInstitutionId(), institution.getInstitutionId());
    }

    public static boolean hasAnyRole(Staffs staff, StaffRoles... roles) {
        if (staff.getRoles() == null) {
            return false;
        }
        for (StaffRolesEntity role : staff.getRoles()) {
            if (role == null || role.getStaffRole() == null) {
                continue;
            }
            for (StaffRoles wanted : roles) {
                if (role.getStaffRole() == wanted) {
                    return true;
                }
            }
        }
        return false;
    }

    private <T> Scoped<T> notFound(Staffs staff, LogType logType, LogAction action, String message) {
        loggingService.logGeneralActivity(logType, action, message, staff.getStaffId(), LogStatus.FAILED);
        return Scoped.refused(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", message
        )));
    }
}
