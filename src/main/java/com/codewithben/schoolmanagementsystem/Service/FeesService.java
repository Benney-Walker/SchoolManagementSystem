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
        String logData = "Student Id: " + studentId + ", Semester Id: " + semesterId + ", Level Id: " + levelId;

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fees == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fees not found");
        }

        List<FeesReport> feesReports = feesReportRepository.findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId());
        if (feesReports == null || feesReports.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No records found");
        }


        //ArrayList to Store Fees Report
        List<PaymentHistory> paymentHistoryList = new ArrayList<>();
        double totalAmountPaid = 0.0;

        for (FeesReport feesReport : feesReports) {
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

        //Build Student Info
        String studentIdLoadInfo = student.getStudentId();
        String studentFullName = student.getFirstName() + " " + student.getLastName();
        String semesterName = fees.getSemester().getSemesterName();
        String levelName = student.getLevel().getLevelName();
        String totalFeesAmount = String.valueOf(fees.getAmountToBePayed());
        String totalFeesPaid = String.valueOf(totalAmountPaid);
        String feesBalance = String.valueOf(fees.getAmountToBePayed() - totalAmountPaid);

        loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.SUCCESS);
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
        String logData = "studentId: " + studentId + ", semesterId: " + semesterId + ", levelId: " + levelId;
        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Level not found");
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fees == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fees not found");
        }

        List<FeesReport> feesReports = feesReportRepository.findByStudent_StudentIdAndFees_FeesId(studentId, fees.getFeesId());
        if (feesReports == null || feesReports.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No records found");
        }

        List<StudentPaymentRecords> studentPaymentRecordsList = getStudentPaymentRecords(feesReports);

        loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(studentPaymentRecordsList);
    }

    private List<StudentPaymentRecords> getStudentPaymentRecords(List<FeesReport> feesReports) {
        List<StudentPaymentRecords> studentPaymentRecordsList = new ArrayList<>();
        for (FeesReport feesReport : feesReports) {
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
        return studentPaymentRecordsList;
    }

    public ResponseEntity<?> updatePaymentRecords(StudentPaymentRecords update, String staffId) {
        String logData = update.toString();

        FeesReport feesReport = feesReportRepository.findByFeesReportId(update.getPaymentId()).orElse(null);
        if (feesReport == null) {
            loggingService.logActivity(LogType.FEES, LogAction.UPDATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment record not found");
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

        loggingService.logActivity(LogType.FEES, LogAction.UPDATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.status(HttpStatus.OK).body("Payment record updated successfully");
    }

    public ResponseEntity<?> deletePaymentRecord(String transactionId, String staffId) {
        String logData = "Record Id: " + transactionId;
        FeesReport paymentRecord = feesReportRepository.findByFeesReportId(transactionId).orElse(null);
        if (paymentRecord == null) {
            loggingService.logActivity(LogType.FEES, LogAction.DELETE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment record not found");
        }

        feesReportRepository.delete(paymentRecord);

        loggingService.logActivity(LogType.FEES, LogAction.DELETE, logData, staffId, LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.OK).body("Payment record deleted successfully");
    }

    public ResponseEntity<?> loadClassFeesSummary(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "invalid staff Id"
            ));
        }
        //Get institution from staff
        Institution institution = staff.getInstitution();
        if (institution == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution not found"
            ));
        }

        //Retrieve all classes
        List<Level> levels = institution.getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
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

        loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(classFeesSummaryList);
    }

    public ResponseEntity<?> fetchGradeFeesReport(String levelId, String semesterId, String staffId) {
        String logData = "levelId: " + levelId + ", semesterId: " + semesterId;
        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fee == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Semester fees not added");
        }

        List<FeesReport> records = fee.getFeesReport();
        if (records == null || records.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No records found");
        }

        List<Students> levelStudents = fee.getLevel().getStudents();
        if (levelStudents == null || levelStudents.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No students found for selected grade");
        }

        List<GradeFeesReport> gradeFeesReportList = new ArrayList<>();
        for (Students student : levelStudents) {
            if (student.getStudentStatus() == StudentStatus.INACTIVE ||
            student.getStudentStatus() == StudentStatus.COMPLETED) {
                continue;
            }

            double amountPaid = 0;
            for (FeesReport feesReport : records) {
                if (feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
                    amountPaid = feesReport.getAmountPaid();
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

        loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
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
            totalAmountPaid += feesReport.getAmountPaid();
        }
        return totalAmountPaid;
    }



    public ResponseEntity<?> addNewSemesterFees(Double feesAmount, String semesterId, String levelID, String staffId) {
        String logData = "Amount: " + feesAmount + " Semester: " + semesterId + " grade: " + levelID;

        if (feesAmount < 0) {
            loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fee amount can't be negative");
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Semester Not Found");
        }

        Level level = levelRepository.findByLevelID(levelID).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Level Not Found");
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelID).orElse(null);
        if (fees != null) {
            loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Fees already added");
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

        loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body("Fees added successfully");
    }

    public ResponseEntity<?> fetchFeesDetails(String semesterId, String levelId, String staffId) {
        String logData = "Semester: " + semesterId + " Level: " + levelId;

        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fee == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fees for selected class not added for the selected semester");
        }

        loggingService.logActivity(LogType.FEES, LogAction.READ, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(
                new FetchFeesDetails(
                String.valueOf(fee.getAmountToBePayed()),
                fee.getSemester().getSemesterID(),
                fee.getLevel().getLevelID()
        ));
    }

    public ResponseEntity<?> updateSemesterFees(FetchFeesDetails update, String staffId) {
        String logData = "Amount: " + update.getAmount() + " SemesterId: " + update.getSemesterId() + " LevelId: " + update.getLevelId();

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                update.getSemesterId(), update.getLevelId()
        ).orElse(null);
        if (fees == null) {
            loggingService.logActivity(LogType.FEES, LogAction.UPDATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fees not found in the system");
        }

        fees.setAmountToBePayed(Double.parseDouble(update.getAmount()));
        feesRepository.save(fees);

        loggingService.logActivity(LogType.FEES, LogAction.UPDATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok("Fees Updated Successfully");
    }

    @Transactional
    public ResponseEntity<?> addNewFeePayment(String studentId, Double amountPaid, String personWhoPaid, String phoneNumber, String levelId,
                                   String semesterId, String staffId) {
        String logData = "studentId: " + studentId + " amountPaid: " + amountPaid + " personWhoPaid: " +
                personWhoPaid + " phoneNumber: " + phoneNumber + " levelId: " + levelId + " semesterId: " + semesterId;

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student Not Found");
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Level Not Found");
        }

        Institution institution = student.getInstitution();

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId).orElse(null);
        if (fees == null) {
            loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grade or semester fee not found");
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

        loggingService.logActivity(LogType.FEES, LogAction.CREATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body("Fees added successfully");
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
        System.out.println("getTotalLevelFeesPaid: " + semesterId + " " + levelId);
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId)
                .orElse(null);
        System.out.println("fees: " + fees);
        if (fees == null)
            return 0.0;

        List<FeesReport> feesReports = fees.getFeesReport();
        if (feesReports == null || feesReports.isEmpty())
            return 0.0;

        return feesReports.stream()
                .mapToDouble(report -> report.getAmountPaid() != null ? report.getAmountPaid() : 0.0)
                .sum();
    }

    public ResponseEntity<?> getTotalSemesterFees(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        //Get current Semester
        String currentSemesterId = utilityClass.getCurrentSemesterId(staff.getInstitution().getInstitutionId());
        if (currentSemesterId == null || currentSemesterId.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<Level> levels = staff.getInstitution().getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
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

        loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(totalSemesterFees);
    }

    public ResponseEntity<?> getTotalFeesPaid(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        //Get current Semester
        String currentSemesterId = utilityClass.getCurrentSemesterId(staff.getInstitution().getInstitutionId());
        if (currentSemesterId == null || currentSemesterId.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<Level> levels = staff.getInstitution().getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no class yet"
            ));
        }

        double totalAmountPaid = 0;
        for (Level level : levels) {
            double levelFeesPaid = getTotalLevelFeesPaid(currentSemesterId, level.getLevelID());
            totalAmountPaid += levelFeesPaid;
        }

        loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(totalAmountPaid);
    }

    public ResponseEntity<?> getRecentPayments(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        String currentSemesterId = utilityClass.getCurrentSemesterId(staff.getInstitution().getInstitutionId());
        if (currentSemesterId == null || currentSemesterId.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Current semester not added to system"
            ));
        }

        List<FeesReport> feesReports = feesReportRepository
                .findByInstitution_InstitutionIdOrderByDateOfPaymentDesc(staff.getInstitution().getInstitutionId());
        if (feesReports == null || feesReports.isEmpty()) {
            loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "'message", "No recent payments found"
            ));
        }

        List<RecentPaymentTable> recentPaymentTables = new ArrayList<>();
        int counter = 0;
        for (FeesReport feesReport: feesReports) {
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

        loggingService.logActivity(LogType.FEES, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(recentPaymentTables);
    }

}
