package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Fees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeesRepository extends JpaRepository<Fees, Long> {
    Optional<Fees> findByFeesId(String feesId);

    Optional<Fees> findBySemester_SemesterIDAndLevel_LevelID(
            String semesterId, String levelId
    );

    List<Fees> findBySemester_SemesterID(String semesterId);

    List<Fees> findBySemester_SemesterIDAndLevel_LevelIDIn(String semesterId, List<String> levelIds);
}
