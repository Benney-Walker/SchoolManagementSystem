package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.SubjectScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectScoreRepository extends JpaRepository<SubjectScore, Long> {
    Optional<SubjectScore> findBySubject_SubjectIdAndResults_ResultId(
            String subjectId, Long resultId
    );

    List<SubjectScore> findByResults_ResultId(long resultId);

    Optional<SubjectScore> findByStudent_StudentIdAndSubject_SubjectIdAndSemester_SemesterID(
            String studentId, String subjectId, String semesterId
    );

    Optional<SubjectScore> findBySubject_SubjectId(String subjectId);
}
