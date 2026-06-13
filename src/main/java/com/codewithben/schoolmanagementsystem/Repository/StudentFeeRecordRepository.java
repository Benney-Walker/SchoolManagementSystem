package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.PaymentRecords;
import com.codewithben.schoolmanagementsystem.Entity.StudentFeeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentFeeRecordRepository extends JpaRepository<StudentFeeRecord, Integer> {

    Optional<StudentFeeRecord> findByStudent_StudentIdAndFees_FeesId(String studentId, int feesId);
}
