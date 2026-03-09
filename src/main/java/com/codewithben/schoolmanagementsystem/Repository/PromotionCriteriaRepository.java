package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.PromotionCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionCriteriaRepository extends JpaRepository<PromotionCriteria, Integer> {
    Optional<PromotionCriteria> findByLevelId(String id);
}
