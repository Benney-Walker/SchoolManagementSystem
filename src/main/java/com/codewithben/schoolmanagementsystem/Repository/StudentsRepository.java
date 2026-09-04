package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Students;
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
}
