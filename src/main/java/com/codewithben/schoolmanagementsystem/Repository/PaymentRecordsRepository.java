package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.PaymentRecords;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRecordsRepository extends JpaRepository<PaymentRecords, Long> {
    /** A payment of the given school that isn't deleted, so other schools' ids are not found. */
    Optional<PaymentRecords> findByRecordsIdAndInstitution_InstitutionIdAndDeletedAtIsNull(String id, String institutionId);

    List<PaymentRecords>
    findFirst15ByInstitution_InstitutionIdAndFeeRecord_Semester_SemesterIDAndDeletedAtIsNullOrderByDateOfPaymentDesc(
                    String institutionId, String semesterId
            );
}
