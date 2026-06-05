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

    private final FeesReportRepository feesReportRepository;

    private final SemesterRepository semesterRepository;

    private final LevelRepository levelRepository;

    private final StaffsRepository staffsRepository;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutionRepository;

    private final LoggingService loggingService;


    // Displays student Fees
    public ResponseEntity<?> findStudentFeesPaymentDetails(String studentId, String semesterId, String levelId, String staffId) {

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

        List<FeesReport> feesReports = feesReportRepository.findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId());
        if (feesReports == null || feesReports.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }


        //ArrayList to Store Fees Report
        List<PaymentHistory> paymentHistoryList = new ArrayList<>();
        double totalAmountPaid = 0.0;

        for (FeesReport feesReport : feesReports) {

            if (!feesReport.isDeleted()) {
                totalAmountPaid += feesReport.getAmountPaid();

                PaymentHistory paymentHistory = new PaymentHistory(
                        feesReport.getDateOfPayment().toString(),
                        String.valueOf(feesReport.getAmountPaid()),
                        String.valueOf(feesReport.getFeesBalance()),
                        feesReport.getPersonWhoPaid(),
                        feesReport.getPhoneNumber()
                );
                paymentHistoryList.add(paymentHistory);
            }
        }

        //Build Student Info
        String studentIdLoadInfo = student.getStudentId();
        String studentFullName = student.getFirstName() + " " + student.getLastName();
        String semesterName = fees.getSemester().getSemesterName();
        String levelName = student.getLevel().getLevelName();
        String totalFeesAmount = String.valueOf(fees.getAmountToBePayed());
        String totalFeesPaid = String.valueOf(totalAmountPaid);
        String feesBalance = String.valueOf(fees.getAmountToBePayed() - totalAmountPaid);

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

        List<FeesReport> feesReports = feesReportRepository.findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId());
        if (feesReports == null || feesReports.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        List<StudentPaymentRecords> studentPaymentRecordsList = getStudentPaymentRecords(feesReports);

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(studentPaymentRecordsList);
    }

    private List<StudentPaymentRecords> getStudentPaymentRecords(List<FeesReport> feesReports) {
        List<StudentPaymentRecords> studentPaymentRecordsList = new ArrayList<>();
        for (FeesReport feesReport : feesReports) {

            if (!feesReport.isDeleted()) {
                StudentPaymentRecords records = new StudentPaymentRecords();
                records.setPaymentId(feesReport.getFeesReportId());
                records.setStudentId(feesReport.getStudent().getStudentId());
                records.setStudentName(feesReport.getStudent().getFirstName() + " " + feesReport.getStudent().getLastName());
                records.setDateOfPayment(feesReport.getDateOfPayment().toString());
                records.setAmount(feesReport.getAmountPaid().toString());
                records.setSemesterId(feesReport.getFees().getSemester().getSemesterID());
                records.setPayerName(feesReport.getPersonWhoPaid());
                records.setPayerContact(feesReport.getPhoneNumber());
                studentPaymentRecordsList.add(records);
            }
        }
        return studentPaymentRecordsList;
    }

    public ResponseEntity<?> updatePaymentRecords(StudentPaymentRecords update, String staffId) {
        String logData = update.toString();

        FeesReport feesReport = feesReportRepository.findByFeesReportId(update.getPaymentId()).orElse(null);
        if (feesReport == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Invalid payment Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid payment Id"
            ));
        }

        if (feesReport.isDeleted()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Payment record marked deleted", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Payment record marked deleted"
            ));
        }

        feesReport.setAmountPaid(Double.parseDouble(update.getAmount()));
        feesReport.setPhoneNumber(update.getPayerContact());
        feesReport.setPersonWhoPaid(update.getPayerName());
        feesReportRepository.save(feesReport);

        double totalPreviouslyPaid = feesReportRepository
                .findByStudent_StudentIdAndFees_FeesId(update.getStudentId(), feesReport.getFees().getFeesId())
                .stream()
                .mapToDouble(FeesReport::getAmountPaid)
                .sum();

        double feesBalance = feesReport.getFees().getAmountToBePayed() - totalPreviouslyPaid;
        feesReport.setFeesBalance(feesBalance);
        feesReportRepository.save(feesReport);

        loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public ResponseEntity<?> deletePaymentRecord(String transactionId, String staffId) {

        FeesReport paymentRecord = feesReportRepository.findByFeesReportId(transactionId).orElse(null);
        if (paymentRecord == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE, "Invalid payment record Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid payment record Id"
            ));
        }

        if (paymentRecord.isDeleted()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE, "Invalid payment record Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "Invalid payment record Id"
            ));
        }

        paymentRecord.setDeleted(true);
        feesReportRepository.save(paymentRecord);

        loggingService.logGeneralActivity(LogType.FEES, LogAction.DELETE, "N/A", staffId, LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.OK).build();
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

        List<FeesReport> records = fee.getFeesReport();
        if (records == null || records.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        List<Students> levelStudents = fee.getLevel().getStudents();
        if (levelStudents == null || levelStudents.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "No students found for selected grade", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No students found for selected class"
            ));
        }

        List<GradeFeesReport> gradeFeesReportList = new ArrayList<>();
        for (Students student : levelStudents) {
            if (student.getStudentStatus() == StudentStatus.INACTIVE ||
            student.getStudentStatus() == StudentStatus.COMPLETED) {
                continue;
            }

            double amountPaid = 0;
            for (FeesReport feesReport : records) {
                if (!feesReport.isDeleted()) {
                    if (feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
                        amountPaid = feesReport.getAmountPaid();
                    }
                }
            }

            double totalFees = fee.getAmountToBePayed();
            double outstandingBalance = totalFees - amountPaid;

            GradeFeesReport paymentRecord = new GradeFeesReport();
            paymentRecord.setStudentId(student.getStudentId());
            paymentRecord.setStudentName(student.getFirstName() + " " + student.getLastName());
            paymentRecord.setTotalAmountToPay(String.valueOf(totalFees));
            paymentRecord.setAmountPayed(String.valueOf(amountPaid));
            paymentRecord.setOutstanding(String.valueOf(outstandingBalance));

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

        List<FeesReport> feesReports = fees.getFeesReport();
        double totalAmountPaid = 0.0;
        for (FeesReport feesReport : feesReports) {
            if (!feesReport.isDeleted()) {
                totalAmountPaid += feesReport.getAmountPaid();
            }
        }
        return totalAmountPaid;
    }



    public ResponseEntity<?> addNewSemesterFees(Double feesAmount, String semesterId, String levelID, String staffId) {
        String logData = "Amount: " + feesAmount + " Semester: " + semesterId + " grade: " + levelID;

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
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "Fees for selected class not added for the selected semester", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Fees for selected class not added for the selected semester"
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
            loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "Invalid fee Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid fee Id"
            ));
        }

        fees.setAmountToBePayed(Double.parseDouble(update.getAmount()));
        feesRepository.save(fees);

        loggingService.logGeneralActivity(LogType.FEES, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> addNewFeePayment(String studentId, Double amountPaid, String personWhoPaid, String phoneNumber, String levelId,
                                   String semesterId, String staffId) {
        String logData = "studentId: " + studentId + " amountPaid: " + amountPaid + " personWhoPaid: " +
                personWhoPaid + " phoneNumber: " + phoneNumber + " levelId: " + levelId + " semesterId: " + semesterId;

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Institution institution = student.getInstitution();

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId).orElse(null);
        if (fees == null) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.CREATE, "Term fee not added to system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Term fee not added to system"
            ));
        }

        // Calculate total already paid by this student for this fee
        double totalPreviouslyPaid = feesReportRepository
                .findByStudent_StudentIdAndFees_FeesId(student.getStudentId(), fees.getFeesId())
                .stream()
                .mapToDouble(FeesReport::getAmountPaid)
                .sum();

        double feesBalance = fees.getAmountToBePayed() - totalPreviouslyPaid - amountPaid;

        // Create and save
        FeesReport feesReport = new FeesReport();
        feesReport.setFeesReportId(utilityClass.generateEntityId("TRANSACTION"));
        feesReport.setAmountPaid(amountPaid);
        feesReport.setPersonWhoPaid(personWhoPaid);
        feesReport.setPhoneNumber(phoneNumber);
        feesReport.setDateOfPayment(LocalDate.now());
        feesReport.setDeleted(false);
        feesReport.setFees(fees);
        feesReport.setStudent(student);
        feesReport.setFeesBalance(feesBalance);
        feesReport.setInstitution(institution);

        feesReportRepository.save(feesReport);

        List<FeesReport> feesReports = fees.getFeesReport();
        if (feesReports == null) {
            feesReports = new ArrayList<>();
        }
        feesReports.add(feesReport);
        feesRepository.save(fees);

        List<FeesReport> institutionFeesReports = institution.getFeesReport();
        if (institutionFeesReports == null) {
            institutionFeesReports = new ArrayList<>();
        }
        institutionFeesReports.add(feesReport);
        institutionRepository.save(institution);

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

        List<FeesReport> feesReports = fees.getFeesReport();
        if (feesReports == null || feesReports.isEmpty())
            return 0.0;

        double amountPaid = 0;
        for (FeesReport feesReport : feesReports) {
            if (!feesReport.isDeleted()) {
                amountPaid += feesReport.getAmountPaid();
            }
        }

        return amountPaid;
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

        List<FeesReport> feesReports = feesReportRepository
                .findByInstitution_InstitutionIdOrderByDateOfPaymentDesc(staff.getInstitution().getInstitutionId());
        if (feesReports == null || feesReports.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "'message", "No recent payments found"
            ));
        }

        List<RecentPaymentTable> recentPaymentTables = new ArrayList<>();
        int counter = 0;
        for (FeesReport feesReport: feesReports) {

            if (!feesReport.isDeleted()) {
                if (feesReport.getFees().getSemester().getSemesterID().equals(currentSemesterId)) {
                    RecentPaymentTable recentPaymentTable = new RecentPaymentTable(
                            feesReport.getDateOfPayment().toString(),
                            feesReport.getStudent().getStudentId(),
                            feesReport.getStudent().getFirstName() + " " + feesReport.getStudent().getLastName(),
                            feesReport.getAmountPaid().toString(),
                            feesReport.getPersonWhoPaid(),
                            feesReport.getFees().getLevel().getLevelName()
                    );
                    recentPaymentTables.add(recentPaymentTable);
                }
                counter++;
                if (counter == 15) {
                    break;
                }
            }
        }

        loggingService.logGeneralActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(recentPaymentTables);
    }

}
