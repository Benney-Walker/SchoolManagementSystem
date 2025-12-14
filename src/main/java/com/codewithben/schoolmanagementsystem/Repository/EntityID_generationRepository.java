package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.EntityID_generation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntityID_generationRepository extends JpaRepository<EntityID_generation,Long> {
    public Optional<EntityID_generation> findByEntityName(String entityName);
}
