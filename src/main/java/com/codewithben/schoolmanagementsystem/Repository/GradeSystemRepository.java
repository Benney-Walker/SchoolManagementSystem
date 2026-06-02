package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.GradeSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeSystemRepository extends JpaRepository<GradeSystem, Long> {

    List<GradeSystem> findAllByInstitution_InstitutionId(String institutionId);

    Optional<GradeSystem> findByGradeAndInstitution_InstitutionId(String grade, String institutionId);

    Optional<GradeSystem> findById(int id);
}
