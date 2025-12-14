package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.GradeSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeSystemRepository extends JpaRepository<GradeSystem, Long> {
    List<GradeSystem> findAllByOrderByLowerRange();
}
