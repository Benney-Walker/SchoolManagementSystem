package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Results;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultsRepository extends JpaRepository<Results, Long> {
    Optional<Results> findByStudent_StudentIdAndSemester_SemesterIDAndLevel_LevelID(
            String studentId, String semesterId, String levelId
    );

    Optional<Results> findByStudent_StudentIdAndLevel_LevelID(String studentId, String levelId);
}
