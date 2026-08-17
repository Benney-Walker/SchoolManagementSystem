package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Constants.SpecialPaymentType;
import com.codewithben.schoolmanagementsystem.Entity.SpecialPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialPaymentRepo extends JpaRepository<SpecialPayment, Integer> {

    Optional<SpecialPayment> findByInstitution_InstitutionIdAndPaymentType(
            String institutionId, SpecialPaymentType paymentType
    );
}
