package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Results;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultsRepository extends JpaRepository<Results, Long> {
    Optional<Results> findByStudent_StudentIdAndSemester_SemesterIDAndLevel_LevelID(
            String studentId, String semesterId, String levelId
    );

    List<Results> findByLevel_LevelIDAndSemester_SemesterID(String levelId, String semesterId);

    Optional<Results> findByStudentAndSemester(Students student, Semester semester);
}
