package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.PaymentRecords;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRecordsRepository extends JpaRepository<PaymentRecords, Long> {
    Optional<PaymentRecords> findByRecordsId(String id);

    List<PaymentRecords>
    findByInstitution_InstitutionIdAndFeeRecord_Semester_SemesterIDOrderByDateOfPaymentDesc(
                    String institutionId, String feeRecordId
            );
}
