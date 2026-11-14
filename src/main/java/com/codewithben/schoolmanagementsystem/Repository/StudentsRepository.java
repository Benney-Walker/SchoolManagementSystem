package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import com.codewithben.schoolmanagementsystem.Entity.Students;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentsRepository extends JpaRepository<Students, Long> {
    Optional<Students> findByStudentId(String studentId);

    List<Students> findByStudentIdIn(List<String> studentIds);

    boolean existsByFirstNameAndLastNameAndDateOfBirthAndInstitution_InstitutionId(
            String firstName, String lastName, LocalDate dateOfBirth, String institutionId
    );

    List<Students> findAllByInstitution_InstitutionIdAndLevel_LevelIDIn(String institutionId, List<String> levelIds);

    // Student lookup limited to the caller's school.
    Optional<Students> findByStudentIdAndInstitution_InstitutionId(String studentId, String institutionId);

    // A school's students in one status, e.g. its graduates.
    List<Students> findAllByInstitution_InstitutionIdAndStudentStatus(
            String institutionId, StudentStatus studentStatus, Sort sort
    );

    List<Students> findAllByInstitution_InstitutionIdAndStudentStatusAndGraduationAcademicYear(
            String institutionId, StudentStatus studentStatus, String graduationAcademicYear, Sort sort
    );

    // Students in a status who are still attached to a class (used to backfill graduates).
    List<Students> findAllByStudentStatusAndLevelIsNotNull(StudentStatus studentStatus);
}
