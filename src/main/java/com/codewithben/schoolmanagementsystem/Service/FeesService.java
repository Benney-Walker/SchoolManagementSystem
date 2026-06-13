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

        StudentFeeRecord feeRecord = studentFeeRecordRepository
                .findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId()).orElse(null);
        if (feeRecord == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }


        //ArrayList to Store Fees Report
        List<PaymentHistory> paymentHistoryList = new ArrayList<>();

        for (PaymentRecords paymentRecords : feeRecord.getPaymentRecords()) {

            if (!paymentRecords.isDeleted()) {

                PaymentHistory paymentHistory = new PaymentHistory(
                        paymentRecords.getDateOfPayment().toString(),
                        String.valueOf(paymentRecords.getAmountPaid()),
                        String.valueOf(paymentRecords.getFeesBalance()),
                        paymentRecords.getPersonWhoPaid(),
                        paymentRecords.getPhoneNumber()
                );
                paymentHistoryList.add(paymentHistory);
            }
        }

        //Build Student Info
        String studentIdLoadInfo = student.getStudentId();
        String studentFullName = student.getFirstName() + " " + student.getLastName();
        String semesterName = fees.getSemester().getSemesterName();
        String levelName = fees.getLevel().getLevelName();
        String totalFeesAmount = String.valueOf(feeRecord.getFeeAmount());
        String totalFeesPaid = String.valueOf(feeRecord.getTotalAmountPaid());
        String feesBalance = String.valueOf(feeRecord.getBalance());

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
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

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fees == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Term fees not found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term fees not found"
            ));
        }

        StudentFeeRecord feeRecord = studentFeeRecordRepository
                .findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId()).orElse(null);
        if (feeRecord == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        List<StudentPaymentRecords> studentPaymentRecordsList = new ArrayList<>();
        for (PaymentRecords paymentRecord : feeRecord.getPaymentRecords()) {

            if (!paymentRecord.isDeleted()) {
                StudentPaymentRecords studentPaymentRecords = StudentPaymentRecords.builder()
                        .paymentId(paymentRecord.getRecordsId())
                        .dateOfPayment(paymentRecord.getDateOfPayment().toString())
                        .studentId(feeRecord.getStudent().getStudentId())
                        .studentName(
                                feeRecord.getStudent().getFirstName() + " " + feeRecord.getStudent().getLastName()
                        )
                        .amount(String.valueOf(paymentRecord.getAmountPaid()))
                        .semesterId(
                                feeRecord.getSemester().getSemesterName() + " " + feeRecord.getSemester().getAcademicYear()
                        )
                        .payerName(paymentRecord.getPersonWhoPaid())
                        .payerContact(paymentRecord.getPhoneNumber())
                        .build();

                studentPaymentRecordsList.add(studentPaymentRecords);

            }
        }

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(studentPaymentRecordsList);
    }

    public ResponseEntity<?> updatePaymentRecords(StudentPaymentRecords update, String staffId) {

        PaymentRecords paymentRecord = paymentRecordsRepository.findByRecordsId(update.getPaymentId()).orElse(null);
        if (paymentRecord == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Invalid payment Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid payment Id"
            ));
        }

        if (paymentRecord.isDeleted()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Payment record marked deleted", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Payment record already deleted"
            ));
        }

        if (paymentRecord.getAmountPaid() != Double.parseDouble(update.getAmount())) {
            paymentRecord.setAmountPaid(Double.parseDouble(update.getAmount()));
            paymentRecordsRepository.save(paymentRecord);

            StudentFeeRecord feeRecord = paymentRecord.getFeeRecord();
            double newTotalPaid = feeRecord.getPaymentRecords()
                    .stream().mapToDouble(PaymentRecords::getAmountPaid).sum();
            double newBalance = feeRecord.getFeeAmount() - newTotalPaid;

            feeRecord.setTotalAmountPaid(newTotalPaid);
            feeRecord.setBalance(newBalance);
            paymentRecord.setFeesBalance(newBalance);
            studentFeeRecordRepository.save(feeRecord);
        }

        paymentRecord.setPersonWhoPaid(update.getPayerName());
        paymentRecord.setPhoneNumber(update.getPayerContact());
        paymentRecordsRepository.save(paymentRecord);

        loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.status(HttpStatus.OK).build();
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

            if (paymentRecord.isDeleted()) {
                loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE, "Payment record already deleted", staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                        "message", "Payment record already deleted"
                ));
            }

            StudentFeeRecord feeRecord = paymentRecord.getFeeRecord();
            //Calculate total paid and new balance
            double oldTotalAmountPaid = feeRecord.getTotalAmountPaid();
            double newTotalAmountPaid = oldTotalAmountPaid - paymentRecord.getAmountPaid();
            double newBalance = feeRecord.getBalance() + paymentRecord.getAmountPaid();
            //Save records
            feeRecord.setTotalAmountPaid(newTotalAmountPaid);
            feeRecord.setBalance(newBalance);
            studentFeeRecordRepository.save(feeRecord);

            paymentRecord.setDeleted(true);
            paymentRecordsRepository.save(paymentRecord);

            loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    public ResponseEntity<?> loadClassFeesSummary(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "invalid staff Id"
            ));
        }
        //Get institution from staff
        Institution institution = staff.getInstitution();
        if (institution == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution not found"
            ));
        }

        //Retrieve all classes
        List<Level> levels = institution.getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
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

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(classFeesSummaryList);
    }

    public ResponseEntity<?> fetchGradeFeesReport(String levelId, String semesterId, String staffId) {

        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fee == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Semester fees not added", staffId, LogStatus.FAILED);
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

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(gradeFeesReportList);
    }

    private Double getExpectedLevelFees(String levelId) {
        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            return 0.0;
        }
        String currentSemesterId = utilityClass.getCurrentSemesterId(level.getInstitution().getInstitutionId());
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemesterId, levelId).orElse(null);
        if (fees == null) {
            return 0.0;
        }

        Double feesAmount = fees.getAmountToBePayed();

        return feesAmount * level.getStudents().size();
    }

    private Double getLevelFeesPaid(String levelId) {
        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            return 0.0;
        }

        String currentSemesterId = utilityClass.getCurrentSemesterId(level.getInstitution().getInstitutionId());
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemesterId, levelId).orElse(null);
        if (fees == null) {
            return 0.0;
        }

        List<StudentFeeRecord> feeRecords = fees.getFeesRecords();
        if (feeRecords == null || feeRecords.isEmpty()) {
            return 0.0;
        }
        return feeRecords.stream()
                .mapToDouble(StudentFeeRecord::getTotalAmountPaid)
                .sum();
    }



    public ResponseEntity<?> addNewSemesterFees(Double feesAmount, String semesterId, String levelID, String staffId) {

        if (feesAmount < 0) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Fee amount can't be negative", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Fee amount can't be negative"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Invalid term Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid term Id"
            ));
        }

        Level level = levelRepository.findByLevelID(levelID).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelID).orElse(null);
        if (fees != null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Fees already added", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Fees already added"
            ));
        }

        Institution institution = level.getInstitution();

        fees = new Fees();
        fees.setAmountToBePayed(feesAmount);
        fees.setSemester(semester);
        fees.setLevel(level);
        fees.setInstitution(institution);
        feesRepository.save(fees);

        List<Fees> levelFees = level.getFees();
        if (levelFees == null) {
            levelFees = new ArrayList<>();
        }
        levelFees.add(fees);
        levelRepository.save(level);

        List<Fees> institutionFees = institution.getFees();
        if (institutionFees == null) {
            institutionFees = new ArrayList<>();
        }
        institutionFees.add(fees);
        institutionRepository.save(institution);

        loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> fetchFeesDetails(String semesterId, String levelId, String staffId) {

        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fee == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Fees for selected class not added for the selected term", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Fees for selected class not added for this selected term"
            ));
        }

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(
                new FetchFeesDetails(
                String.valueOf(fee.getAmountToBePayed()),
                fee.getSemester().getSemesterID(),
                fee.getLevel().getLevelID()
        ));
    }

    public ResponseEntity<?> updateSemesterFees(FetchFeesDetails update, String staffId) {

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                update.getSemesterId(), update.getClassId()
        ).orElse(null);
        if (fees == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Fee not added to system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Fee not added to system"
            ));
        }

        fees.setAmountToBePayed(Double.parseDouble(update.getAmount()));
        feesRepository.save(fees);

        loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
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

        StudentFeeRecord feeRecord = studentFeeRecordRepository
                .findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId()).orElse(null);
        if (feeRecord == null) {
            feeRecord = new StudentFeeRecord();
            feeRecord.setFeeAmount(fees.getAmountToBePayed());
            feeRecord.setTotalAmountPaid(0);
            feeRecord.setBalance(fees.getAmountToBePayed());
            feeRecord.setStudent(student);
            feeRecord.setFees(fees);
            feeRecord.setLevel(fees.getLevel());
            feeRecord.setSemester(fees.getSemester());
            feeRecord.setInstitution(fees.getInstitution());
            studentFeeRecordRepository.save(feeRecord);
        }

        double oldTotalPaid = feeRecord.getTotalAmountPaid();
        double newTotalPaid = oldTotalPaid + amountPaid;
        double newBalance = feeRecord.getBalance() - newTotalPaid;

        // Create and save
        PaymentRecords paymentRecord = new PaymentRecords();
        paymentRecord.setRecordsId(utilityClass.generateEntityId("TRANSACTION"));
        paymentRecord.setAmountPaid(amountPaid);
        paymentRecord.setFeesBalance(newBalance);
        paymentRecord.setPersonWhoPaid(personWhoPaid);
        paymentRecord.setPhoneNumber(phoneNumber);
        paymentRecord.setDateOfPayment(LocalDate.now());
        paymentRecord.setInstitution(fees.getInstitution());
        paymentRecord.setDeleted(false);
        paymentRecordsRepository.save(paymentRecord);

        List<PaymentRecords> paymentRecords = feeRecord.getPaymentRecords();
        if (paymentRecords == null) {
            paymentRecords = new ArrayList<>();
        }
        paymentRecords.add(paymentRecord);
        feeRecord.setPaymentRecords(paymentRecords);
        feeRecord.setTotalAmountPaid(newTotalPaid);
        feeRecord.setBalance(newBalance);
        studentFeeRecordRepository.save(feeRecord);

        //Add Payment to institution
        List<PaymentRecords> institutionPaymentRecords = student.getInstitution().getFeesReport();
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

        Double feeAmount = fees.getAmountToBePayed();

        return feeAmount * numberOfStudents;
    }

    private double getTotalLevelFeesPaid(String semesterId, String levelId) {

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId)
                .orElse(null);

        if (fees == null)
            return 0.0;

        List<StudentFeeRecord> feeRecords = fees.getFeesRecords();
        if (feeRecords == null || feeRecords.isEmpty())
            return 0.0;
        return feeRecords.stream().mapToDouble(
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
        String currentSemesterId = utilityClass.getCurrentSemesterId(staff.getInstitution().getInstitutionId());
        if (currentSemesterId == null || currentSemesterId.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<Level> levels = staff.getInstitution().getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no class yet"
            ));
        }
        double totalSemesterFees = 0;
        for (Level level : levels) {
            double levelFees = getTotalLevelFees(
                    level.getLevelID(), currentSemesterId, level.getStudents().size()
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
        String currentSemesterId = utilityClass.getCurrentSemesterId(staff.getInstitution().getInstitutionId());
        if (currentSemesterId == null || currentSemesterId.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<Level> levels = staff.getInstitution().getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no class yet"
            ));
        }

        double totalAmountPaid = 0;
        for (Level level : levels) {
            double levelFeesPaid = getTotalLevelFeesPaid(currentSemesterId, level.getLevelID());
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

        String currentSemesterId = utilityClass.getCurrentSemesterId(staff.getInstitution().getInstitutionId());
        if (currentSemesterId == null || currentSemesterId.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<PaymentRecords> paymentRecords = paymentRecordsRepository
                .findByInstitution_InstitutionIdAndFeeRecord_Semester_SemesterIDOrderByDateOfPaymentDesc(
                        staff.getInstitution().getInstitutionId(),
                        currentSemesterId
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


            if (!paymentRecord.isDeleted()) {

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
        }

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(recentPaymentRecords);
    }

}
