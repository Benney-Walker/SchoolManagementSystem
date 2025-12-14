package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutiionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByInstitutionId(String institutionId);
}
