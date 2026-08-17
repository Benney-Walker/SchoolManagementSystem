package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Constants.SpecialPaymentType;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.LevelSpecialPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelSpecialPaymentRepo extends JpaRepository<LevelSpecialPayment, Integer> {

    Optional<LevelSpecialPayment> findBySpecialPayment_IdAndLevel_LevelID(
            String id, String levelId
    );

    List<LevelSpecialPayment> findBySpecialPayment_PaymentTypeAndSpecialPayment_Institution(
            SpecialPaymentType specialPaymentType, Institution institution
    );

    Optional<LevelSpecialPayment> findById(String id);
}
