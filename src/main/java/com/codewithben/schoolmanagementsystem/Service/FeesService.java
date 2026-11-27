package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.*;
import com.codewithben.schoolmanagementsystem.DTO.Fees.*;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeeCreation;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeesUpdate;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.PaymentReport;
import com.codewithben.schoolmanagementsystem.DTO.Report.GradeFeesReport;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.PaymentRules;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class FeesService {
    private final FeesRepository feesRepository;

    private final StudentsRepository studentsRepository;

    private final PaymentRecordsRepository paymentRecordsRepository;

    private final StudentFeeRecordRepository studentFeeRecordRepository;

    private final SemesterRepository semesterRepository;

    private final LevelRepository levelRepository;

    private final StaffsRepository staffsRepository;

    private final UtilityClass utilityClass;

    private final LoggingService loggingService;

    private final RabbitMQProducer rabbitMQProducer;


    // Displays student Fees
    public ResponseEntity<?> findStudentPaymentRecords(String studentId, String semesterId, String levelId, String staffId) {

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fees == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Semester Fees not added", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Semester Fees not added"
            ));
        }

        StudentFeeRecord studentFeeRecord = studentFeeRecordRepository
                .findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId()).orElse(null);
        if (studentFeeRecord == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }


        //ArrayList to Store Fees Report
        List<PaymentHistory> paymentHistoryList = new ArrayList<>();

        // Deleted payments are left out (issue 3).
        for (PaymentRecords paymentRecords : PaymentRules.activePayments(studentFeeRecord.getPaymentRecords())) {

            PaymentHistory paymentHistory = new PaymentHistory(
                    paymentRecords.getDateOfPayment().toString(),
                    String.valueOf(paymentRecords.getAmountPaid()),
                    String.valueOf(paymentRecords.getFeesBalance()),
                    paymentRecords.getPersonWhoPaid(),
                    paymentRecords.getPhoneNumber()
            );
            paymentHistoryList.add(paymentHistory);

        }

        //Build Student Info
        String studentIdLoadInfo = student.getStudentId();
        String studentFullName = student.getFirstName() + " " + student.getLastName();
        String semesterName = fees.getSemester().getSemesterName();
        String levelName = fees.getLevel().getLevelName();
        String totalFeesAmount = String.valueOf(studentFeeRecord.getTotalAmount());
        String totalFeesPaid = String.valueOf(studentFeeRecord.getAmountPaid());
        String feesBalance = String.valueOf(studentFeeRecord.getBalance());

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.READ,
                "Fetched payments records for " + student.getFirstName() + " " + student.getLastName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(new StudentFeesPaymentDisplay(
                studentIdLoadInfo,
                studentFullName,
                semesterName,
                levelName,
                totalFeesAmount,
                totalFeesPaid,
                feesBalance,
                paymentHistoryList
        ));

    }

    //Fetches individual payment records according to grade and semester
    public ResponseEntity<?> fetchIndividualPaymentRecords(String studentId, String levelId, String semesterId, String staffId) {

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fees == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Term fees not found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term fees not found"
            ));
        }

        StudentFeeRecord studentFeeRecord = studentFeeRecordRepository
                .findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId()).orElse(null);
        if (studentFeeRecord == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        List<StudentPaymentRecords> studentPaymentRecordsList = new ArrayList<>();
        // Deleted payments are left out (issue 3).
        for (PaymentRecords paymentRecord : PaymentRules.activePayments(studentFeeRecord.getPaymentRecords())) {


            StudentPaymentRecords studentPaymentRecords = StudentPaymentRecords.builder()
                    .paymentId(paymentRecord.getRecordsId())
                    .dateOfPayment(paymentRecord.getDateOfPayment().toString())
                    .studentId(studentFeeRecord.getStudent().getStudentId())
                    .studentName(
                            studentFeeRecord.getStudent().getFirstName() + " " + studentFeeRecord.getStudent().getLastName()
                    )
                    .amount(paymentRecord.getAmountPaid())
                    .semesterId(
                            studentFeeRecord.getSemester().getSemesterName() + " " + studentFeeRecord.getSemester().getAcademicYear()
                    )
                    .payerName(paymentRecord.getPersonWhoPaid())
                    .payerContact(paymentRecord.getPhoneNumber())
                    .build();

            studentPaymentRecordsList.add(studentPaymentRecords);
        }

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.READ,
                "Fetched payments record for " + studentFeeRecord.getStudent().getFirstName() + " " + studentFeeRecord.getStudent().getLastName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(studentPaymentRecordsList);
    }

    @Transactional
    public ResponseEntity<?> updatePaymentRecord(StudentPaymentRecords update, String staffId) {

        Staffs staff = staffId == null ? null : staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getInstitution() == null) {
            return sessionExpired();
        }

        // Only this school's payments, and never a deleted one (editing it would bring its
        // amount back into the totals).
        PaymentRecords paymentRecord = update.getPaymentId() == null ? null : paymentRecordsRepository
                .findByRecordsIdAndInstitution_InstitutionIdAndDeletedAtIsNull(
                        update.getPaymentId(), staff.getInstitution().getInstitutionId())
                .orElse(null);
        if (paymentRecord == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Invalid payment Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid payment Id"
            ));
        }

        if (paymentRecord.getAmountPaid() != update.getAmount()) {
            paymentRecord.setAmountPaid(update.getAmount());
            paymentRecordsRepository.save(paymentRecord);

            StudentFeeRecord studentFeeRecord = paymentRecord.getFeeRecord();
            float newTotalPaid = PaymentRules.totalPaid(studentFeeRecord.getPaymentRecords());
            float newBalance = PaymentRules.balance(studentFeeRecord.getTotalAmount(), newTotalPaid);

            studentFeeRecord.setAmountPaid(newTotalPaid);
            studentFeeRecord.setBalance(newBalance);
            paymentRecord.setFeesBalance(newBalance);
            studentFeeRecordRepository.save(studentFeeRecord);
        }

        paymentRecord.setPersonWhoPaid(update.getPayerName());
        paymentRecord.setPhoneNumber(update.getPayerContact());
        paymentRecordsRepository.save(paymentRecord);

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.UPDATE,
                "Updated payment record with Id: " + paymentRecord.getRecordsId() + " for " + paymentRecord.getFeeRecord().getStudent().getFirstName() + " " +
                        paymentRecord.getFeeRecord().getStudent().getLastName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    /*
     * Deletes a fee payment entered by mistake (issue 3). Ben, 2026-11-27: the accountant only,
     * a reason is required, current term only.
     *
     * Before: the balance became balance + (amountPaid - deleted amount) instead of
     * balance + deleted amount (fee 1000, payments 300 + 200, delete the 200 -> balance 800,
     * not 700), the payment row was written back unchanged (still in history, and deleting it
     * again subtracted it again), and any school's payment could be deleted by id.
     *
     * Now: the payment is soft-deleted (deletedAt, deletedBy, deletionReason) and the fee
     * record's amountPaid and balance are recomputed from the payments that remain.
     */
    @Transactional
    public ResponseEntity<?> deletePaymentRecord(String transactionId, String reason, String staffId) {

        Staffs staff = staffId == null ? null : staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getInstitution() == null) {
            return sessionExpired();
        }

        boolean accountant = staff.getRoles() != null && staff.getRoles().stream()
                .anyMatch(role -> role != null && role.getStaffRole() == StaffRoles.ACCOUNTANT);
        if (!accountant) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE,
                    "Payment delete refused: caller is not the accountant", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Only the accountant can delete a payment record"
            ));
        }

        String reasonProblem = PaymentRules.deletionReasonProblem(reason);
        if (reasonProblem != null) {
            return ResponseEntity.badRequest().body(Map.of("message", reasonProblem));
        }

        PaymentRecords paymentRecord = transactionId == null ? null : paymentRecordsRepository
                .findByRecordsIdAndInstitution_InstitutionIdAndDeletedAtIsNull(
                        transactionId, staff.getInstitution().getInstitutionId())
                .orElse(null);
        if (paymentRecord == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE, "Invalid payment record Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid payment record Id"
            ));
        }

        // A past term's balance was carried into the next term as its opening balance, so
        // changing it now wouldn't flow forward.
        StudentFeeRecord studentFeeRecord = paymentRecord.getFeeRecord();
        Semester currentSemester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (currentSemester == null || studentFeeRecord.getSemester() == null
                || !currentSemester.getSemesterID().equals(studentFeeRecord.getSemester().getSemesterID())) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE,
                    "Payment " + paymentRecord.getRecordsId() + " is not in the current term", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Only payments of the current term can be deleted"
            ));
        }

        paymentRecord.setDeletedAt(LocalDateTime.now());
        paymentRecord.setDeletedBy(staffId);
        paymentRecord.setDeletionReason(reason.strip());
        paymentRecordsRepository.save(paymentRecord);

        float newTotalPaid = PaymentRules.totalPaid(studentFeeRecord.getPaymentRecords());
        float newBalance = PaymentRules.balance(studentFeeRecord.getTotalAmount(), newTotalPaid);
        studentFeeRecord.setAmountPaid(newTotalPaid);
        studentFeeRecord.setBalance(newBalance);
        studentFeeRecordRepository.save(studentFeeRecord);

        Students student = studentFeeRecord.getStudent();
        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.DELETE,
                "Deleted payment " + paymentRecord.getRecordsId() + " of GHS " + paymentRecord.getAmountPaid()
                        + " for " + student.getFirstName() + " " + student.getLastName()
                        + ". Reason: " + reason.strip(),
                staffId, LogStatus.SUCCESS);

        return ResponseEntity.ok(Map.of(
                "message", "Payment deleted",
                "amountPaid", newTotalPaid,
                "balance", newBalance
        ));
    }

    private ResponseEntity<?> sessionExpired() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", "Session expired. Login again"
        ));
    }

    public ResponseEntity<?> loadClassFeesSummary(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        //Retrieve all classes
        List<Level> levels = staff.getInstitution().getLevel();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Institution has no class yet", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no class yet"
            ));
        }

        //Figures are for the current term, looked up once for every class
        Semester currentSemester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (currentSemester == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Current semester not added to system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<ClassFeesSummary> classFeesSummaryList = new ArrayList<>();
        for (Level level : levels) {
            Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                    currentSemester.getSemesterID(), level.getLevelID()
            ).orElse(null);

            float expectedFees = getExpectedLevelFees(level, fees);
            double feesPaid = getLevelFeesPaid(fees);

            ClassFeesSummary classFeesSummary = new ClassFeesSummary(
                    level.getLevelName(),
                    String.valueOf(billedStudentCount(level)),
                    String.valueOf(expectedFees),
                    String.valueOf(feesPaid),
                    String.valueOf(expectedFees - feesPaid)
            );
            classFeesSummaryList.add(classFeesSummary);
        }

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Fetched class level fees summary for the school", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(classFeesSummaryList);
    }

    public ResponseEntity<?> fetchGradeFeesReport(String levelId, String semesterId, String staffId) {

        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fee == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Term fees not added", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term fees not added"
            ));
        }

        List<StudentFeeRecord> records = fee.getFeesRecords();
        if (records == null || records.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        List<GradeFeesReport> gradeFeesReportList = new ArrayList<>();
        for (StudentFeeRecord record : records) {

            if (record.getStudent().getStudentStatus() == StudentStatus.INACTIVE) continue;

            GradeFeesReport paymentRecord = new GradeFeesReport();
            paymentRecord.setStudentId(record.getStudent().getStudentId());
            paymentRecord.setStudentName(record.getStudent().getFirstName() + " " + record.getStudent().getLastName());
            paymentRecord.setTotalAmountToPay(String.valueOf(record.getTotalAmount()));
            paymentRecord.setAmountPayed(String.valueOf(record.getAmountPaid()));
            paymentRecord.setOutstanding(String.valueOf(record.getBalance()));

            gradeFeesReportList.add(paymentRecord);
        }

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.READ,
                "Fetched fees report for " + fee.getLevel().getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(gradeFeesReportList);
    }

    /*
     * Only ACTIVE students are billed: inactive students are not charged, and
     * graduated students have left their class. Every fee total (number of students,
     * expected fees, amount paid, outstanding) uses this same set, so the figures
     * compare like with like. Term fee records are already created for active
     * students only (AsyncService), and the class fee report skips inactive ones.
     */
    private int billedStudentCount(Level level) {
        return level.getStudents() == null ? 0 : utilityClass.getActiveStudents(level.getStudents()).size();
    }

    private static boolean isBilled(StudentFeeRecord record) {
        return record.getStudent() != null && record.getStudent().getStudentStatus() == StudentStatus.ACTIVE;
    }

    //Term fee x billed students. 0 when the class has no fees set for the term.
    private float getExpectedLevelFees(Level level, Fees fees) {
        if (fees == null) {
            return 0;
        }
        return fees.getAmountToBePayed() * billedStudentCount(level);
    }

    //Total paid by billed students. 0 when the class has no fees set for the term.
    private double getLevelFeesPaid(Fees fees) {
        if (fees == null) {
            return 0.0;
        }

        List<StudentFeeRecord> studentFeeRecords = fees.getFeesRecords();
        if (studentFeeRecords == null || studentFeeRecords.isEmpty()) {
            return 0.0;
        }
        return studentFeeRecords.stream()
                .filter(FeesService::isBilled)
                .mapToDouble(StudentFeeRecord::getAmountPaid)
                .sum();
    }

    public ResponseEntity<?> addNewSemesterFees(NewFees newFees, String staffId) {

        Semester semester = semesterRepository.findBySemesterID(newFees.getSemesterId()).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Invalid term Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid term Id"
            ));
        }

        List<Level> levels = levelRepository.findAllByLevelIDIn(newFees.getLevelIds());
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "All selected classes are not found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "All selected classes are not found"
            ));
        }

        //Compare found classes and not found classes
        List<String> foundClasses = levels.stream().map(
                Level::getLevelID
        ).toList();

        List<String> missingIds = newFees.getLevelIds().stream()
                .filter(id -> !foundClasses.contains(id))
                .toList();
        if (!missingIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "These Ids are invalid: " + String.join(", ", missingIds)
            ));
        }

        List<Fees> createdRecords = new ArrayList<>();
        List<String> notAffectedClasses = new ArrayList<>();
        List<String> affectedClasses = new ArrayList<>();
        for (Level level : levels) {

            if (level.getLevelSpecialPayments() == null ||
            level.getLevelSpecialPayments().isEmpty()) {
                notAffectedClasses.add(level.getLevelName());
                continue;
            }

            Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(newFees.getSemesterId(), level.getLevelID()).orElse(null);
            if (fees == null) {
                fees = new Fees();
                fees.setFeesId(utilityClass.generateEntityId("FEES"));
                fees.setAmountToBePayed(newFees.getFeesAmount());
                fees.setSemester(semester);
                fees.setLevel(level);
                fees.setInstitution(level.getInstitution());
                fees.setLocked(false);
                createdRecords.add(fees);
                affectedClasses.add(level.getLevelID());
            }
        }
        if (!createdRecords.isEmpty() && !affectedClasses.isEmpty()) {
            feesRepository.saveAll(createdRecords);

            //Publish fee creation to rabbitMQ
            FeeCreation feeCreation = new FeeCreation();
            feeCreation.setSemesterId(newFees.getSemesterId());
            feeCreation.setLevelIds(affectedClasses);
            rabbitMQProducer.sendFeeCreationEvent(feeCreation);
        }

        if (notAffectedClasses.isEmpty()) {
            loggingService.logGeneralActivity(
                    LogType.FEES, LogAction.CREATE,
                    "Added term fee for " + levels.stream().map(Level::getLevelName).collect(Collectors.joining(", ")),
                    staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok(Map.of(
                    "message", "Added term fee for " + levels.stream().map(Level::getLevelName).collect(Collectors.joining(", "))
            ));
        } else {
            loggingService.logGeneralActivity(
                    LogType.FEES, LogAction.CREATE,
                    String.join(", ", notAffectedClasses) + " has no special fees added so could not process actual fees",
                    staffId, LogStatus.SUCCESS);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", String.join(", ", notAffectedClasses) + " has no special fees added so could not process actual fees"
            ));
        }
    }

    public ResponseEntity<?> fetchFeesDetails(String semesterId, String staffId) {

        List<Fees> fees = feesRepository.findBySemester_SemesterID(semesterId);
        if (fees == null || fees.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Term fee not added", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term fees not added"
            ));
        }

        List<FetchFeesDetails> records = new ArrayList<>();
        for (Fees fee : fees) {
            FetchFeesDetails record = FetchFeesDetails.builder()
                    .feesId(fee.getFeesId())
                    .semesterId(fee.getSemester().getSemesterID())
                    .levelName(fee.getLevel().getLevelName())
                    .amount(fee.getAmountToBePayed())
                    .build();
            records.add(record);
        }

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.READ,
                "Fetched term fees",
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(records);
    }

    public ResponseEntity<?> updateSemesterFees(FetchFeesDetails update, String staffId) {

        Fees fees = feesRepository.findByFeesId(update.getFeesId()).orElse(null);
        if (fees == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Invalid fee Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid fee Id"
            ));
        }

        if (fees.isLocked()) {
            loggingService.logGeneralActivity(
                    LogType.FEES,
                    LogAction.UPDATE,
                    "Fee update is not allowed at this period",
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Fee update is not allowed at this period"
            ));
        }

        fees.setAmountToBePayed(update.getAmount());
        feesRepository.save(fees);

        FeesUpdate feesUpdate = FeesUpdate.builder().feesId(update.getFeesId()).build();
        rabbitMQProducer.sendFeeUpdateEvent(feesUpdate);

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.UPDATE,
                "Updated fee details for " + fees.getLevel().getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> addNewPayment(NewPayment newPayment, String staffId) {

        StudentFeeRecord studentFeeRecord = studentFeeRecordRepository
                .findByStudent_StudentIdAndSemester_SemesterID(
                        newPayment.getStudentId(), newPayment.getSemesterId()
                ).orElse(null);
        if (studentFeeRecord == null) {
            loggingService.logGeneralActivity(
                    LogType.PAYMENT,
                    LogAction.CREATE,
                    "Term fees not added",
                    staffId, LogStatus.FAILED

            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term fees not added"
            ));
        }

        float newTotalPaid = studentFeeRecord.getAmountPaid() + newPayment.getAmountPaid();
        float newBalance = studentFeeRecord.getTotalAmount() - newTotalPaid;

        // Create and save
        PaymentRecords paymentRecord = new PaymentRecords();
        paymentRecord.setRecordsId(utilityClass.generateEntityId("FEES_PAYMENT"));
        paymentRecord.setAmountPaid(newPayment.getAmountPaid());
        paymentRecord.setFeesBalance(newBalance);
        paymentRecord.setPersonWhoPaid(newPayment.getPayerName());
        paymentRecord.setPhoneNumber(newPayment.getPayerPhone());
        paymentRecord.setDateOfPayment(LocalDate.now());
        paymentRecord.setFeeRecord(studentFeeRecord);
        paymentRecord.setInstitution(studentFeeRecord.getInstitution());
        paymentRecordsRepository.saveAndFlush(paymentRecord);

        //Update fee record balance
        studentFeeRecord.setAmountPaid(newTotalPaid);
        studentFeeRecord.setBalance(newBalance);
        studentFeeRecord.setLocked(true);
        studentFeeRecordRepository.saveAndFlush(studentFeeRecord);

        if (!studentFeeRecord.getFees().isLocked()) {
            studentFeeRecord.getFees().setLocked(true);
            feesRepository.save(studentFeeRecord.getFees());
        }

        if (studentFeeRecord.getStudent().isNew()) {
            studentFeeRecord.getStudent().setNew(false);
            studentsRepository.saveAndFlush(studentFeeRecord.getStudent());
        }

        PaymentReport paymentReport = PaymentReport.builder()
                .studentId(newPayment.getStudentId())
                .amountPaid(newPayment.getAmountPaid())
                .newBalance(newBalance)
                .build();
        rabbitMQProducer.paymentReportBroadcast(paymentReport);

        loggingService.logGeneralActivity(
                LogType.PAYMENT,
                LogAction.CREATE,
                "New payment for " + studentFeeRecord.getStudent().getFirstName(),
                staffId,
                LogStatus.SUCCESS
        );
        return ResponseEntity.ok().build();
    }

    private double getTotalLevelFees(String levelId, String semesterId, int numberOfStudents) {
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId).orElse(null);
        if (fees == null) {
            return 0;
        }

        float feeAmount = fees.getAmountToBePayed();

        return feeAmount * numberOfStudents;
    }

    private double getTotalLevelFeesPaid(String semesterId, String levelId) {

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId)
                .orElse(null);

        if (fees == null)
            return 0.0;

        List<StudentFeeRecord> studentFeeRecords = fees.getFeesRecords();
        if (studentFeeRecords == null || studentFeeRecords.isEmpty())
            return 0.0;
        return studentFeeRecords.stream()
                .filter(FeesService::isBilled)
                .mapToDouble(StudentFeeRecord::getAmountPaid)
                .sum();
    }

    public ResponseEntity<?> getTotalSemesterFees(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        //Get current Semester
        Semester currentSemester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (currentSemester == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<Level> levels = staff.getInstitution().getLevel();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no class yet"
            ));
        }
        double totalSemesterFees = 0;
        for (Level level : levels) {
            double levelFees = getTotalLevelFees(
                    level.getLevelID(), currentSemester.getSemesterID(), billedStudentCount(level)
            );
            totalSemesterFees += levelFees;
        }

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(totalSemesterFees);
    }

    public ResponseEntity<?> getTotalFeesPaid(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        //Get current Semester
        Semester currentSemester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (currentSemester == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<Level> levels = staff.getInstitution().getLevel();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no class yet"
            ));
        }

        double totalAmountPaid = 0;
        for (Level level : levels) {
            double levelFeesPaid = getTotalLevelFeesPaid(currentSemester.getSemesterID(), level.getLevelID());
            totalAmountPaid += levelFeesPaid;
        }

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(totalAmountPaid);
    }

    public ResponseEntity<?> getRecentPayments(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(
                    LogType.FEES,
                    LogAction.READ,
                    "Invalid staff Id",
                    staffId, LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Semester currentSemester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (currentSemester == null) {
            loggingService.logGeneralActivity(
                    LogType.FEES,
                    LogAction.READ,
                    "Current semester not added to system",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<PaymentRecords> paymentRecords = paymentRecordsRepository
                .findFirst15ByInstitution_InstitutionIdAndFeeRecord_Semester_SemesterIDAndDeletedAtIsNullOrderByDateOfPaymentDesc(
                        currentSemester.getInstitution().getInstitutionId(), currentSemester.getSemesterID()
                );
        if (paymentRecords == null || paymentRecords.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "'message", "No recent payments found"
            ));
        }

        List<RecentPaymentRecords> recentPaymentRecords = new ArrayList<>();
        for (PaymentRecords paymentRecord : paymentRecords) {

            RecentPaymentRecords recentPayment = RecentPaymentRecords.builder()
                    .amount(String.valueOf(paymentRecord.getAmountPaid()))
                    .studentName(
                            paymentRecord.getFeeRecord().getStudent().getFirstName()
                            + " " +
                            paymentRecord.getFeeRecord().getStudent().getLastName()
                    )
                    .date(paymentRecord.getDateOfPayment().toString())
                    .build();

            recentPaymentRecords.add(recentPayment);
        }

        loggingService.logGeneralActivity(
                LogType.FEES,
                LogAction.READ,
                "Fetched recent payment records",
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(recentPaymentRecords);
    }
}
