package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffsRepository extends JpaRepository<Staffs, Long> {
    Optional<Staffs> findByStaffId(String staffId);

    boolean existsByPhoneNumberAndInstitution_InstitutionId(String phoneNumber, String institutionId);

    Optional<Staffs> findByStatusAndInstitution_InstitutionId(String status, String institutionId);

    boolean existsByFirstNameAndLastName(String firstName, String lastName);
}
