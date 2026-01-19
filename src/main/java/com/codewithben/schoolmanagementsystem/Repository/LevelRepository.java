package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    Optional<Level> findByLevelID(String levelID);

    Optional<Level> findByLevelNameAndInstitution_InstitutionId(String levelName, String institutionID);

    Optional<Level> findByLevelIDAndFees_FeesId(String levelID, int feesId);

    Optional<Level> findByStaff_StaffId(String staffID);
}
