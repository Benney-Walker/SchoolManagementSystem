package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Students;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentEnrolmentTest {

    private static final LocalDate GRADUATION_DAY = LocalDate.of(2026, 7, 31);

    private static Level level(String name) {
        Level level = new Level();
        level.setLevelID("LV-" + name);
        level.setLevelName(name);
        return level;
    }

    private static Students student(StudentStatus status, Level level) {
        Students student = new Students();
        student.setStudentId("STD100200");
        student.setStudentStatus(status);
        student.setLevel(level);
        return student;
    }

    @Test
    void graduatingAnActiveStudentRemovesThemFromTheirClassAndRecordsDetails() {
        Students student = student(StudentStatus.ACTIVE, level("JHS 3"));

        StudentEnrolment.applyStatus(student, StudentStatus.GRADUATED, null, "2025/2026", GRADUATION_DAY);

        assertEquals(StudentStatus.GRADUATED, student.getStudentStatus());
        assertNull(student.getLevel());
        assertEquals("JHS 3", student.getGraduationClassName());
        assertEquals("2025/2026", student.getGraduationAcademicYear());
        assertEquals(GRADUATION_DAY, student.getGraduationDate());
    }

    @Test
    void resavingAGraduatedStudentKeepsTheOriginalGraduationDetails() {
        Students student = student(StudentStatus.ACTIVE, level("JHS 3"));
        StudentEnrolment.applyStatus(student, StudentStatus.GRADUATED, null, "2025/2026", GRADUATION_DAY);

        StudentEnrolment.applyStatus(student, StudentStatus.GRADUATED, null, "2026/2027", GRADUATION_DAY.plusYears(1));

        assertEquals("JHS 3", student.getGraduationClassName());
        assertEquals("2025/2026", student.getGraduationAcademicYear());
        assertEquals(GRADUATION_DAY, student.getGraduationDate());
        assertNull(student.getLevel());
    }

    @Test
    void graduatedStudentStillInAClassIsDetachedAndTheClassRecorded() {
        // Data written before this change: status GRADUATED but still in a real class.
        Students student = student(StudentStatus.GRADUATED, level("JHS 3"));

        StudentEnrolment.applyStatus(student, StudentStatus.GRADUATED, null, null, GRADUATION_DAY);

        assertNull(student.getLevel());
        assertEquals("JHS 3", student.getGraduationClassName());
    }

    @Test
    void graduatedPlaceholderClassIsNeverRecordedAsTheClassGraduatedFrom() {
        Students student = student(StudentStatus.GRADUATED, level("GRADUATED"));

        StudentEnrolment.applyStatus(student, StudentStatus.GRADUATED, null, null, GRADUATION_DAY);

        assertNull(student.getLevel());
        assertNull(student.getGraduationClassName());
    }

    @Test
    void reactivatingAGraduatedStudentEnrolsThemAndClearsGraduationDetails() {
        Students student = student(StudentStatus.ACTIVE, level("JHS 3"));
        StudentEnrolment.applyStatus(student, StudentStatus.GRADUATED, null, "2025/2026", GRADUATION_DAY);
        Level jhs3 = level("JHS 3");

        StudentEnrolment.applyStatus(student, StudentStatus.ACTIVE, jhs3, null, GRADUATION_DAY);

        assertEquals(StudentStatus.ACTIVE, student.getStudentStatus());
        assertSame(jhs3, student.getLevel());
        assertNull(student.getGraduationDate());
        assertNull(student.getGraduationClassName());
        assertNull(student.getGraduationAcademicYear());
    }

    @Test
    void movingAnActiveStudentToAnotherClassLeavesNoGraduationDetails() {
        Students student = student(StudentStatus.ACTIVE, level("JHS 2"));
        Level jhs3 = level("JHS 3");

        StudentEnrolment.applyStatus(student, StudentStatus.INACTIVE, jhs3, null, GRADUATION_DAY);

        assertEquals(StudentStatus.INACTIVE, student.getStudentStatus());
        assertSame(jhs3, student.getLevel());
        assertNull(student.getGraduationDate());
    }

    @Test
    void nonGraduatedStatusWithoutAClassIsRejected() {
        Students student = student(StudentStatus.ACTIVE, level("JHS 2"));

        assertThrows(IllegalArgumentException.class,
                () -> StudentEnrolment.applyStatus(student, StudentStatus.ACTIVE, null, null, GRADUATION_DAY));
    }

    @Test
    void settingAnInactiveOrGraduatedStudentToActiveIsAReactivation() {
        assertTrue(StudentEnrolment.isReactivation(StudentStatus.INACTIVE, StudentStatus.ACTIVE));
        assertTrue(StudentEnrolment.isReactivation(StudentStatus.GRADUATED, StudentStatus.ACTIVE));
    }

    @Test
    void otherStatusChangesAreNotReactivations() {
        assertFalse(StudentEnrolment.isReactivation(StudentStatus.ACTIVE, StudentStatus.ACTIVE));
        assertFalse(StudentEnrolment.isReactivation(StudentStatus.ACTIVE, StudentStatus.INACTIVE));
        assertFalse(StudentEnrolment.isReactivation(StudentStatus.INACTIVE, StudentStatus.GRADUATED));
        assertFalse(StudentEnrolment.isReactivation(StudentStatus.GRADUATED, StudentStatus.INACTIVE));
    }

    @Test
    void placeholderDetection() {
        assertTrue(StudentEnrolment.isGraduatedPlaceholder(level("GRADUATED")));
        assertFalse(StudentEnrolment.isGraduatedPlaceholder(level("JHS 3")));
        assertFalse(StudentEnrolment.isGraduatedPlaceholder(null));
    }
}
