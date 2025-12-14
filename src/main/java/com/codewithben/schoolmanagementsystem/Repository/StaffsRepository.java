package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffsRepository extends JpaRepository<Staffs, Long> {
    public Optional<Staffs> findByStaffId(String staffId);

    public Optional<Staffs> findByphoneNumberAndInstitution_InstitutionId(String phoneNumber, String institutionId);

    public Optional<Staffs> findByFirstNameAndLastName(String firstName, String lastName);
}
