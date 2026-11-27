package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffsRepository extends JpaRepository<Staffs, Long> {
    Optional<Staffs> findByStaffId(String staffId);

    /** A staff member of the given school only, so ids from other schools are not found. */
    Optional<Staffs> findByStaffIdAndInstitution_InstitutionId(String staffId, String institutionId);

    boolean existsByPhoneNumberAndInstitution_InstitutionId(String phoneNumber, String institutionId);

    boolean existsByFirstNameAndLastNameAndInstitution_InstitutionId(
            String firstName, String lastName, String institutionId);

    List<Staffs> findByInstitution_InstitutionId(String institutionId);
}
