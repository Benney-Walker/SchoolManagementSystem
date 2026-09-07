package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findBySemesterID(String semesterId);

    List<Semester> findByInstitutionAndSemesterStartDateBeforeOrderBySemesterStartDateDesc(
            Institution institution, LocalDate startDate
    );

    Optional<Semester> findBySemesterNameAndAcademicYearAndInstitution_InstitutionId(
            String semesterName, String academicYear, String institutionId
    );
}
