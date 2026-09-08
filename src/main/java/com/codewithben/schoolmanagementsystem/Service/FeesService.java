package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.*;
import com.codewithben.schoolmanagementsystem.DTO.Fees.*;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeeCreation;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeesUpdate;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.PaymentReport;
import com.codewithben.schoolmanagementsystem.DTO.Report.GradeFeesReport;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        for (PaymentRecords paymentRecords : studentFeeRecord.getPaymentRecords()) {

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
        for (PaymentRecords paymentRecord : studentFeeRecord.getPaymentRecords()) {


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

    public ResponseEntity<?> updatePaymentRecord(StudentPaymentRecords update, String staffId) {

        PaymentRecords paymentRecord = paymentRecordsRepository.findByRecordsId(update.getPaymentId()).orElse(null);
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
            float newTotalPaid = (float) studentFeeRecord.getPaymentRecords()
                    .stream().mapToDouble(PaymentRecords::getAmountPaid).sum();
            float newBalance = (float) studentFeeRecord.getTotalAmount() - newTotalPaid;

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

    public ResponseEntity<?> deletePaymentRecord(String transactionId, String staffId) {

        synchronized (transactionId.intern()) {
            PaymentRecords paymentRecord = paymentRecordsRepository.findByRecordsId(transactionId).orElse(null);
            if (paymentRecord == null) {
                loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE, "Invalid payment record Id", staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "message", "Invalid payment record Id"
                ));
            }

            StudentFeeRecord studentFeeRecord = paymentRecord.getFeeRecord();
            //Calculate total paid and new balance
            float oldTotalAmountPaid = studentFeeRecord.getAmountPaid();
            float newTotalAmountPaid = oldTotalAmountPaid - paymentRecord.getAmountPaid();
            float newBalance = studentFeeRecord.getBalance() + newTotalAmountPaid;
            //Save records
            studentFeeRecord.setAmountPaid(newTotalAmountPaid);
            studentFeeRecord.setBalance(newBalance);
            studentFeeRecordRepository.save(studentFeeRecord);

            paymentRecordsRepository.save(paymentRecord);

            loggingService.logGeneralActivity(
                    LogType.FEES, LogAction.DELETE,
                    "Deleted Payment record with Id: " + paymentRecord.getRecordsId() +
                    " for " + paymentRecord.getFeeRecord().getStudent().getFirstName() + " " +
                    paymentRecord.getFeeRecord().getStudent().getLastName(),
                    staffId, LogStatus.SUCCESS);

            return ResponseEntity.ok().build();
        }
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

        List<ClassFeesSummary> classFeesSummaryList = new ArrayList<>();
        for (Level level : levels) {

            ClassFeesSummary classFeesSummary = new ClassFeesSummary(
                    level.getLevelName(),
                    String.valueOf(level.getStudents().size()),
                    String.valueOf(getExpectedLevelFees(level.getLevelID())),
                    String.valueOf(getLevelFeesPaid(level.getLevelID())),
                    String.valueOf(getExpectedLevelFees(level.getLevelID()) - getLevelFeesPaid(level.getLevelID()))
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

    private float getExpectedLevelFees(String levelId) {
        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            return 0;
        }
        Semester currentSemester = utilityClass.getCurrentSemester(level.getInstitution());
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemester.getSemesterID(), levelId).orElse(null);
        if (fees == null) {
            return 0;
        }

        float feesAmount = fees.getAmountToBePayed();

        return feesAmount * level.getStudents().size();
    }

    private Double getLevelFeesPaid(String levelId) {
        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            return 0.0;
        }

        Semester currentSemester = utilityClass.getCurrentSemester(level.getInstitution());
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemester.getSemesterID(), levelId).orElse(null);
        if (fees == null) {
            return 0.0;
        }

        List<StudentFeeRecord> studentFeeRecords = fees.getFeesRecords();
        if (studentFeeRecords == null || studentFeeRecords.isEmpty()) {
            return 0.0;
        }
        return studentFeeRecords.stream()
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
        return studentFeeRecords.stream().mapToDouble(
                StudentFeeRecord::getAmountPaid
        ).sum();
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
                    level.getLevelID(), currentSemester.getSemesterID(), level.getStudents().size()
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
                .findFirst15ByInstitution_InstitutionIdAndFeeRecord_Semester_SemesterIDOrderByDateOfPaymentDesc(
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
