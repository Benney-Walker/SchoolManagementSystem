package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    Optional<Level> findByLevelID(String levelID);

    Optional<Level> findByLevelNameAndInstitution_InstitutionId(String levelName, String institutionID);

    // Class lookup limited to the caller's school.
    Optional<Level> findByLevelIDAndInstitution_InstitutionId(String levelID, String institutionId);

    List<Level> findAllByLevelIDIn(List<String> levelIds);
}
