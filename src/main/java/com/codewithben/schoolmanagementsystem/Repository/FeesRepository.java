package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Fees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeesRepository extends JpaRepository<Fees, Long> {
    Optional<Fees> findByFeesId(int feesId);

    Optional<Fees> findBySemester_SemesterIDAndLevel_LevelID(
            String semesterId, String levelId
    );
}
