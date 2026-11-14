package com.codewithben.schoolmanagementsystem.Migration;

import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import com.codewithben.schoolmanagementsystem.Entity.Students;
import com.codewithben.schoolmanagementsystem.Repository.StudentsRepository;
import com.codewithben.schoolmanagementsystem.Utility.StudentEnrolment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * One-off data fix, run at startup: students marked GRADUATED before graduation
 * took them out of their class are still attached to a class (their last real
 * class, or the hand-made "GRADUATED" placeholder class). This detaches them and
 * records the real class they left, using the same rules as a new graduation.
 *
 * Idempotent: once applied, the query finds nothing and the runner does no work.
 * Graduation date and academic year are unknown for these students and stay null.
 * Safe to delete once it has run in every environment (the log line reports it).
 *
 * Deliberately not @Transactional and wrapped in a catch: saveAll is atomic on its
 * own, and a failure here must be logged, not stop the application from starting.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraduatedStudentsBackfill implements ApplicationRunner {

    private final StudentsRepository studentsRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Students> stillInAClass =
                    studentsRepository.findAllByStudentStatusAndLevelIsNotNull(StudentStatus.GRADUATED);
            if (stillInAClass.isEmpty()) {
                return;
            }

            for (Students student : stillInAClass) {
                // Already GRADUATED, so this only detaches and records the class left.
                StudentEnrolment.applyStatus(student, StudentStatus.GRADUATED, null, null, null);
            }
            studentsRepository.saveAll(stillInAClass);

            log.info("Graduated-students backfill: removed {} graduated student(s) from their class", stillInAClass.size());
        } catch (Exception e) {
            log.error("Graduated-students backfill failed; graduated students may still count in class totals", e);
        }
    }
}
