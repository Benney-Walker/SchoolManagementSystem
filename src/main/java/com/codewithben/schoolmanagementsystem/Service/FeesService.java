package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import com.codewithben.schoolmanagementsystem.DTO.Fees.*;
import com.codewithben.schoolmanagementsystem.DTO.Report.GradeFeesReport;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final InstitutiionRepository institutionRepository;

    private final LoggingService loggingService;


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
        String totalFeesAmount = String.valueOf(studentFeeRecord.getFeeAmount());
        String totalFeesPaid = String.valueOf(studentFeeRecord.getTotalAmountPaid());
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
                    .amount(String.valueOf(paymentRecord.getAmountPaid()))
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

        if (paymentRecord.getAmountPaid() != Double.parseDouble(update.getAmount())) {
            paymentRecord.setAmountPaid(Double.parseDouble(update.getAmount()));
            paymentRecordsRepository.save(paymentRecord);

            StudentFeeRecord studentFeeRecord = paymentRecord.getFeeRecord();
            double newTotalPaid = studentFeeRecord.getPaymentRecords()
                    .stream().mapToDouble(PaymentRecords::getAmountPaid).sum();
            double newBalance = studentFeeRecord.getFeeAmount() - newTotalPaid;

            studentFeeRecord.setTotalAmountPaid(newTotalPaid);
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
            double oldTotalAmountPaid = studentFeeRecord.getTotalAmountPaid();
            double newTotalAmountPaid = oldTotalAmountPaid - paymentRecord.getAmountPaid();
            double newBalance = studentFeeRecord.getBalance() + newTotalAmountPaid;
            //Save records
            studentFeeRecord.setTotalAmountPaid(newTotalAmountPaid);
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
            paymentRecord.setTotalAmountToPay(String.valueOf(record.getFeeAmount()));
            paymentRecord.setAmountPayed(String.valueOf(record.getTotalAmountPaid()));
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
                .mapToDouble(StudentFeeRecord::getTotalAmountPaid)
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

        List<Fees> createdRecords = new ArrayList<>();
        for (Level level : levels) {
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
            }
        }
        feesRepository.saveAll(createdRecords);

        //Compare to found classes and not found classes
        List<String> foundClasses = levels.stream().map(
                Level::getLevelID
        ).toList();

        List<String> missingIds = newFees.getLevelIds().stream()
                .filter(id -> !foundClasses.contains(id))
                .toList();

        //Publish fee creation to rabbitMQ
        FeeCreation feeCreation = new FeeCreation();
        feeCreation.setSemesterId(newFees.getSemesterId());
        feeCreation.setLevelIds(foundClasses);
        rabbitMQProducer.sendFeeCreationEvent(feeCreation);

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.CREATE,
                "Added term fee for " + levels.stream().map(Level::getLevelName).collect(Collectors.joining(", ")),
                staffId, LogStatus.SUCCESS);

        if (missingIds.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(Map.of(
                "message", "Could not save for Invalid Ids " + missingIds.stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(", "))
        ));
    }

    public ResponseEntity<?> fetchFeesDetails(String semesterId, String levelId, String staffId) {

        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fee == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Term fee not added for this class", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term fee not added for this class"
            ));
        }

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.READ,
                "Fetched fee details for " + fee.getLevel().getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(
                new FetchFeesDetails(fee.getFeesId(),
                fee.getAmountToBePayed(),
                fee.getSemester().getSemesterID(),
                fee.getLevel().getLevelID()
        ));
    }

    public ResponseEntity<?> updateSemesterFees(FetchFeesDetails update, String staffId) {

        Fees fees = feesRepository.findByFeesId(update.getFeesId()).orElse(null);
        if (fees == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Invalid fee Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid fee Id"
            ));
        }

        fees.setAmountToBePayed(update.getAmount());
        feesRepository.save(fees);

        List<StudentFeeRecord> feeRecords = fees.getFeesRecords();
        if (feeRecords != null && !feeRecords.isEmpty()) {
            for(StudentFeeRecord feeRecord : feeRecords) {
                feeRecord.setFeeAmount(update.getAmount());
            }
            studentFeeRecordRepository.saveAll(feeRecords);
        }

        loggingService.logGeneralActivity(
                LogType.FEES, LogAction.UPDATE,
                "Updated fee details for " + fees.getLevel().getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> addNewPayment(String studentId, Double amountPaid, String personWhoPaid, String phoneNumber, String levelId,
                                           String semesterId, String staffId) {

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId).orElse(null);
        if (fees == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Term fee not added to system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term fee not added to system"
            ));
        }

        StudentFeeRecord studentFeeRecord = studentFeeRecordRepository
                .findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId()).orElse(null);
        if (studentFeeRecord == null) {
            studentFeeRecord = new StudentFeeRecord();
            studentFeeRecord.setFeeAmount(fees.getAmountToBePayed());
            studentFeeRecord.setTotalAmountPaid(0);
            studentFeeRecord.setBalance(fees.getAmountToBePayed());
            studentFeeRecord.setStudent(student);
            studentFeeRecord.setFees(fees);
            studentFeeRecord.setLevel(fees.getLevel());
            studentFeeRecord.setSemester(fees.getSemester());
            studentFeeRecord.setInstitution(fees.getInstitution());
            studentFeeRecordRepository.save(studentFeeRecord);
        }

        double oldTotalPaid = studentFeeRecord.getTotalAmountPaid();
        double newTotalPaid = oldTotalPaid + amountPaid;
        double newBalance = studentFeeRecord.getFeeAmount() - newTotalPaid;

        // Create and save
        PaymentRecords paymentRecord = new PaymentRecords();
        paymentRecord.setRecordsId(utilityClass.generateEntityId("TRANSACTION"));
        paymentRecord.setAmountPaid(amountPaid);
        paymentRecord.setFeesBalance(newBalance);
        paymentRecord.setPersonWhoPaid(personWhoPaid);
        paymentRecord.setPhoneNumber(phoneNumber);
        paymentRecord.setDateOfPayment(LocalDate.now());
        paymentRecord.setInstitution(fees.getInstitution());
        paymentRecord.setFeeRecord(studentFeeRecord);
        paymentRecordsRepository.save(paymentRecord);

        List<PaymentRecords> paymentRecords = studentFeeRecord.getPaymentRecords();
        if (paymentRecords == null) {
            paymentRecords = new ArrayList<>();
        }
        paymentRecords.add(paymentRecord);
        studentFeeRecord.setPaymentRecords(paymentRecords);
        studentFeeRecord.setTotalAmountPaid(newTotalPaid);
        studentFeeRecord.setBalance(newBalance);
        studentFeeRecordRepository.save(studentFeeRecord);

        //Add Payment to institution
        List<PaymentRecords> institutionPaymentRecords = student.getInstitution().getPaymentRecords();
        if (institutionPaymentRecords == null) {
            institutionPaymentRecords = new ArrayList<>();
        }

        institutionPaymentRecords.add(paymentRecord);
        institutionRepository.save(student.getInstitution());

        loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "N/A", staffId, LogStatus.SUCCESS);
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
                StudentFeeRecord::getTotalAmountPaid
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
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Semester currentSemester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (currentSemester == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<PaymentRecords> paymentRecords = paymentRecordsRepository
                .findByInstitution_InstitutionIdAndFeeRecord_Semester_SemesterIDOrderByDateOfPaymentDesc(
                        staff.getInstitution().getInstitutionId(),
                        currentSemester.getSemesterID()
                );
        if (paymentRecords == null || paymentRecords.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "'message", "No recent payments found"
            ));
        }

        List<RecentPaymentRecords> recentPaymentRecords = new ArrayList<>();
        int counter = 0;
        for (PaymentRecords paymentRecord : paymentRecords) {
            if (counter == 20)
                break;

            RecentPaymentRecords recentPayment = RecentPaymentRecords.builder()
                    .paymentDate(paymentRecord.getDateOfPayment().toString())
                    .studentId(
                            paymentRecord.getFeeRecord().getStudent().getStudentId()
                    )
                    .studentNameCol(
                            paymentRecord.getFeeRecord().getStudent().getFirstName() + " " + paymentRecord.getFeeRecord().getStudent().getLastName()
                    )
                    .amountCol(String.valueOf(paymentRecord.getAmountPaid()))
                    .payerCol(paymentRecord.getPersonWhoPaid())
                    .levelCol(paymentRecord.getFeeRecord().getLevel().getLevelName())
                    .build();

            recentPaymentRecords.add(recentPayment);

            counter++;
        }

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(recentPaymentRecords);
    }

    public void createIndividualFeeRecord(FeeCreation feeCreation) {

        List<Fees> feesList = feesRepository.findBySemester_SemesterIDAndLevel_LevelIDIn(
                feeCreation.getSemesterId(), feeCreation.getLevelIds()
        );

        for (Fees fees : feesList) {

            List<Students> studentsList = utilityClass.getActiveStudents(fees.getLevel().getStudents());
            if (studentsList == null || studentsList.isEmpty()) {
                continue;
            }

            List<LevelSpecialPayment> specialPayments = fees.getLevel().getLevelSpecialPayments();

            float newStudent = (float) specialPayments.stream().mapToDouble(LevelSpecialPayment::getAmount).sum();
            float oldStudent = (float) specialPayments.stream()
                    .filter(specialFee -> specialFee.getSpecialPayment().getPaymentType() == SpecialPaymentType.ADMISSION_FEE)
                    .mapToDouble(LevelSpecialPayment::getAmount)
                    .sum();

            List<StudentFeeRecord> newRecords = new ArrayList<>();
            for (Students student : studentsList) {

                StudentFeeRecord newRecord = studentFeeRecordRepository.findByStudent_StudentIdAndFees_FeesId(
                        student.getStudentId(), fees.getFeesId()
                ).orElse(null);
                if (newRecord == null) {
                    newRecord = new StudentFeeRecord();
                    newRecord.setStudent(student);
                    newRecord.setFees(fees);
                    newRecord.setLevel(fees.getLevel());
                    newRecord.setSemester(fees.getSemester());
                    newRecord.setLocked(false);
                    newRecord.setInstitution(fees.getInstitution());

                    if (student.isNew()) {
                        newRecord.setTotalAmount(fees.getAmountToBePayed() + newStudent);
                    } else {
                        newRecord.setTotalAmount(fees.getAmountToBePayed() + oldStudent);
                    }
                    newRecords.add(newRecord);
                }
            }

            if (!newRecords.isEmpty()) {
                studentFeeRecordRepository.saveAll(newRecords);
            }
        }
    }

    public void updateIndividualFeeRecord(FeesUpdate feesUpdate) {

        Fees fees = feesRepository.findByFeesId(feesUpdate.getFeesId()).orElse(null);
        if (fees == null) {
            log.error("Fees with id {} not found", feesUpdate.getFeesId());
            return;
        }

        List<Students> studentsList = utilityClass.getActiveStudents(fees.getLevel().getStudents());
        if (studentsList == null || studentsList.isEmpty()) {
            log.error("Class {} has no students", fees.getLevel().getLevelName());
            return;
        }

        List<LevelSpecialPayment> specialPayments = fees.getLevel().getLevelSpecialPayments();

        float newStudent = (float) specialPayments.stream().mapToDouble(LevelSpecialPayment::getAmount).sum();
        float oldStudent = (float) specialPayments.stream()
                .filter(specialFee -> specialFee.getSpecialPayment().getPaymentType() == SpecialPaymentType.ADMISSION_FEE)
                .mapToDouble(LevelSpecialPayment::getAmount)
                .sum();

        List<StudentFeeRecord> newRecords = new ArrayList<>();
        for (Students student : studentsList) {

            StudentFeeRecord newRecord = studentFeeRecordRepository.findByStudent_StudentIdAndFees_FeesId(
                    student.getStudentId(), fees.getFeesId()
            ).orElse(null);
            if (newRecord == null) {
                newRecord = new StudentFeeRecord();
                newRecord.setStudent(student);
                newRecord.setFees(fees);
                newRecord.setLevel(fees.getLevel());
                newRecord.setSemester(fees.getSemester());
                newRecord.setLocked(false);
                newRecord.setInstitution(fees.getInstitution());

                if (student.isNew()) {
                    newRecord.setTotalAmount(fees.getAmountToBePayed() + newStudent);
                } else {
                    newRecord.setTotalAmount(fees.getAmountToBePayed() + oldStudent);
                }
                newRecords.add(newRecord);
            }
        }

        if (!newRecords.isEmpty()) {
            studentFeeRecordRepository.saveAll(newRecords);
        }
    }
}
