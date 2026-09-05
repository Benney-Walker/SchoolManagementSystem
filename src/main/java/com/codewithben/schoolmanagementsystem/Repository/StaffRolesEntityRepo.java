package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.StaffRolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRolesEntityRepo extends JpaRepository<StaffRolesEntity,Integer> {
    List<StaffRolesEntity> findByStaff_StaffId(String staffId);
}

