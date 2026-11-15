package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Students;

import java.time.LocalDate;

/**
 * Rules for changing a student's status and class.
 *
 * Graduation is a status, not a class: a graduated student is taken out of their
 * class (level = null) and the graduation details are recorded on the student.
 * Because class rosters and counts are built from a class's students, this keeps
 * graduated students out of every class count without filtering at each call site.
 *
 * Kept free of Spring and repositories so the rules can be unit-tested directly.
 */
public final class StudentEnrolment {

    /**
     * Name of the placeholder class some schools created by hand to hold graduated
     * students under the previous approach. It is not a real class: never record it
     * as the class a student graduated from, and never enrol a student into it.
     */
    public static final String GRADUATED_PLACEHOLDER_CLASS = StudentStatus.GRADUATED.name();

    private StudentEnrolment() {
    }

    public static boolean isGraduatedPlaceholder(Level level) {
        return level != null && GRADUATED_PLACEHOLDER_CLASS.equals(level.getLevelName());
    }

    /**
     * True when a student who is not being billed (INACTIVE or GRADUATED) is set back
     * to ACTIVE. Only ACTIVE students are billed, so such a student must be billed for
     * the current term.
     */
    public static boolean isReactivation(StudentStatus currentStatus, StudentStatus newStatus) {
        return newStatus == StudentStatus.ACTIVE && currentStatus != StudentStatus.ACTIVE;
    }

    /**
     * Applies a status change to the student. Does not save.
     *
     * <ul>
     *   <li>GRADUATED: the student leaves their class. On the first graduation the
     *       date and academic year are recorded; the class they leave is recorded
     *       if it is a real class. Re-saving an already graduated student keeps the
     *       original graduation details.</li>
     *   <li>ACTIVE / INACTIVE: the student is enrolled in {@code newClass} and any
     *       graduation details are cleared (for a graduation made in error).</li>
     * </ul>
     *
     * @param newClass     class to enrol in; required unless {@code newStatus} is GRADUATED
     * @param academicYear academic year to record on graduation (may be null)
     * @param today        graduation date to record
     */
    public static void applyStatus(Students student, StudentStatus newStatus, Level newClass,
                                   String academicYear, LocalDate today) {
        if (newStatus == StudentStatus.GRADUATED) {
            if (student.getStudentStatus() != StudentStatus.GRADUATED) {
                student.setGraduationDate(today);
                student.setGraduationAcademicYear(academicYear);
            }
            Level currentClass = student.getLevel();
            if (currentClass != null) {
                if (student.getGraduationClassName() == null && !isGraduatedPlaceholder(currentClass)) {
                    student.setGraduationClassName(currentClass.getLevelName());
                }
                student.setLevel(null);
            }
        } else {
            if (newClass == null) {
                throw new IllegalArgumentException("A class is required for status " + newStatus);
            }
            student.setLevel(newClass);
            student.setGraduationDate(null);
            student.setGraduationClassName(null);
            student.setGraduationAcademicYear(null);
        }
        student.setStudentStatus(newStatus);
    }
}
