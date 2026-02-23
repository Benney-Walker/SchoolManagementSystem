package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.Contants.StudentStatus;
import com.codewithben.schoolmanagementsystem.DTO.Account.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FetchFeesDetails;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FeesService {
    private final FeesRepository feesRepository;

    private final StudentsRepository studentsRepository;

    private final FeesReportRepository feesReportRepository;

    private final SemesterRepository semesterRepository;

    private final LevelRepository levelRepository;

    private final StaffsRepository staffsRepository;

    private final UtilityClass utilityClass;

    private final AdminService adminService;

    private final InstitutiionRepository institutionRepository;

    public FeesService(FeesRepository feesRepository, StudentsRepository studentsRepository, FeesReportRepository feesReportRepository,
                       SemesterRepository semesterRepository, LevelRepository levelRepository,  StaffsRepository staffsRepository, UtilityClass utilityClass,
                       InstitutiionRepository institutionRepository, AdminService adminService) {
        this.feesRepository = feesRepository;
        this.studentsRepository = studentsRepository;
        this.feesReportRepository = feesReportRepository;
        this.semesterRepository = semesterRepository;
        this.levelRepository = levelRepository;
        this.staffsRepository = staffsRepository;
        this.utilityClass = utilityClass;
        this.institutionRepository = institutionRepository;
        this.adminService = adminService;
    }

    // Displays student Fees
    public StudentFeesPaymentDisplay findStudentFeesPaymentDetails(String studentId, String semesterId, String levelId, String staffId) throws Exception {
        
        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            throw new Exception("Invalid student id");
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fees == null) {
            throw new Exception("Semester fee not added");
        }

        List<FeesReport> feesReports = fees.getFeesReport();
        if (feesReports == null || feesReports.isEmpty()) {
            throw new Exception("No Records found");
        }


        //ArrayList to Store Fees Report
        List<PaymentHistory> paymentHistoryList = new ArrayList<>();
        double totalAmountPaid = 0.0;

        for (FeesReport feesReport : feesReports) {
            if (feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
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

        if (paymentHistoryList.isEmpty()) {
            throw new Exception("No Records found");
        }

        //Build Student Info
        String studentIdLoadInfo = student.getStudentId();
        String studentFullName = student.getFirstName() + " " + student.getLastName();
        String semesterName = fees.getSemester().getSemesterName();
        String levelName = student.getLevel().getLevelName();
        String totalFeesAmount = String.valueOf(fees.getAmountToBePayed());
        String totalFeesPaid = String.valueOf(totalAmountPaid);
        String feesBalance = String.valueOf(fees.getAmountToBePayed() - totalAmountPaid);

        return new StudentFeesPaymentDisplay(
                studentIdLoadInfo,
                studentFullName,
                semesterName,
                levelName,
                totalFeesAmount,
                totalFeesPaid,
                feesBalance,
                paymentHistoryList
        );

    }

    public List<StudentPaymentRecords> fetchPaymentRecords(String studentId, String semesterId) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            throw new Exception("Invalid student id");
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, student.getLevel().getLevelID()
        ).orElse(null);
        if (fees == null) {
            throw new Exception("Fees Not Found");
        }

        List<FeesReport> feesReports = fees.getFeesReport();
        if (feesReports == null || feesReports.isEmpty()) {
            throw new Exception("No records found");
        }

        List<StudentPaymentRecords> studentPaymentRecordsList = new ArrayList<>();
        for (FeesReport feesReport : feesReports) {
            if (feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
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

    public String updatePaymentRecords(StudentPaymentRecords update) throws Exception {
        FeesReport feesReport = feesReportRepository.findByFeesReportId(update.getPaymentId())
                .orElseThrow(() -> new Exception("Record Not Found"));

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

        return "Fees Records updated successfully";
    }

    public String deletePaymentRecord(String transactionId) throws Exception {
        FeesReport paymentRecord = feesReportRepository.findByFeesReportId(transactionId)
                .orElseThrow(() -> new Exception("Record Not Found"));

        feesReportRepository.delete(paymentRecord);

        return "Payment Record deleted successfully";
    }

    public List<ClassFeesSummary> loadClassFeesSummary(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff Not Found");
        }
        //Get institution from staff
        Institution institution = staff.getInstitution();
        if (institution == null) {
            throw new Exception("Institution Not Found");
        }

        //Retrieve all classes
        List<Level> levels = institution.getLevel();
        if (levels == null || levels.isEmpty()) {
            return Collections.emptyList();
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
        return classFeesSummaryList;
    }

    public List<GradeFeesReport> fetchGradeFeesReport(String levelId, String semesterId) throws Exception {
        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElseThrow(() -> new Exception("Semester Fee not Added for the selected class"));

        List<FeesReport> records = fee.getFeesReport();
        List<Students> levelStudents = fee.getLevel().getStudents();

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

        return gradeFeesReportList;
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



    public String addNewSemesterFees(Double feesAmount, String semesterId, String levelID) throws Exception {
        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            throw new Exception("Semester not found");
        }

        Level level = levelRepository.findByLevelID(levelID).orElse(null);
        if (level == null) {
            throw new Exception("Level not found");
        }

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelID).orElse(null);
        if (fees != null)
            throw new Exception("Fees already exists");

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
        return "Fees added successfully";
    }

    public FetchFeesDetails fetchFeesDetails(String semesterId, String levelId) throws Exception {
        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElseThrow( () -> new Exception("Fees not Added to system"));

        return new FetchFeesDetails(
                String.valueOf(fee.getAmountToBePayed()),
                fee.getSemester().getSemesterID(),
                fee.getLevel().getLevelID()
        );
    }

    public String updateSemesterFees(FetchFeesDetails update) throws Exception {
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                update.getSemesterId(), update.getLevelId()
        ).orElseThrow( () -> new Exception("Fees not Found"));

        fees.setAmountToBePayed(Double.parseDouble(update.getAmount()));
        feesRepository.save(fees);
        return "Fees Updated Successfully";
    }

    @Transactional
    public String addNewFeePayment(String studentId, Double amountPaid, String personWhoPaid, String phoneNumber,
                                   String semesterId) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId)
                .orElseThrow(() -> new Exception("Student not found"));

        String levelId = student.getLevel().getLevelID();

        Institution institution = student.getInstitution();

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId)
                .orElseThrow(() -> new Exception("Fees for the expected class not added to system"));

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

        return "Payment added successfully";
    }

    public double getTotalLevelFees(String levelId, String semesterId, int numberOfStudents) {
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId).orElse(null);
        if (fees == null) {
            return 0;
        }

        Double feeAmount = fees.getAmountToBePayed();

        return feeAmount * numberOfStudents;
    }

    public double getTotalLevelFeesPaid(String semesterId, String levelId) {
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

    public List<FeesSummaryTable> getFullyPaidStudents(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff not found");
        }

        Institution institution = staff.getInstitution();

        List<Students> students = institution.getStudents();
        List<FeesSummaryTable> feesSummaryTables = new ArrayList<>();

        //Get current Semester
        String currentSemester = utilityClass.getCurrentSemesterId(institution.getInstitutionId());
        if (currentSemester == null || currentSemester.isEmpty()) {
            throw new Exception("Semester not found");
        }

        for (Students student: students) {
            String levelId = student.getLevel().getLevelID();

            Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemester, levelId).orElse(null);
            if (fee == null) {
                throw new Exception("Not all semester fees added");
            }
            double feeAmount = fee.getAmountToBePayed();


            //Get feesReport List
            List<FeesReport> feesReports = fee.getFeesReport();
            if (feesReports == null || feesReports.isEmpty()) {
                throw new Exception("No records found");
            }

            double amountPaid = 0.0;
            for (FeesReport feesReport: feesReports) {
                if (feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
                    amountPaid += feesReport.getAmountPaid();
                }
            }

            if (Math.abs(feeAmount - amountPaid) < 0.01) {
                FeesSummaryTable feesSummaryTable = new FeesSummaryTable(
                        student.getStudentId(),
                        student.getFirstName() + " " + student.getLastName(),
                        student.getLevel().getLevelName(),
                        String.valueOf(feeAmount),
                        String.valueOf(amountPaid),
                        String.valueOf(feeAmount - amountPaid)
                );
                feesSummaryTables.add(feesSummaryTable);
            }
        }

        if (feesSummaryTables.isEmpty()) {
            throw new Exception("No records found");
        }
        return feesSummaryTables;
    }

    public List<FeesSummaryTable> getPartiallyPaidStudents(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("Staff not found"));

        Institution institution = staff.getInstitution();
        if (institution == null) {
            throw new Exception("Institution not found");
        }

        List<Students> students = institution.getStudents();
        if (students == null || students.isEmpty()) {
            throw new Exception("Institution has no students");
        }

        List<FeesSummaryTable> feesSummaryTables = new ArrayList<>();

        String currentSemester = utilityClass.getCurrentSemesterId(institution.getInstitutionId());
        if (currentSemester == null || currentSemester.isEmpty()) {
            throw new Exception("Semester not found");
        }

        for (Students student : students) {

            String levelId = student.getLevel().getLevelID();

            Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemester, levelId)
                    .orElseThrow(() -> new Exception("Not all semester fees added"));

            double feeAmount = fee.getAmountToBePayed() != null ? fee.getAmountToBePayed() : 0.0;
            double amountPaid = 0.0;

            List<FeesReport> feesReports = fee.getFeesReport();
            if (feesReports == null || feesReports.isEmpty()) {
                throw new Exception("No records found");
            }

            for (FeesReport feesReport : feesReports) {
                if (feesReport.getStudent() != null &&
                        feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
                    amountPaid += feesReport.getAmountPaid() != null ? feesReport.getAmountPaid() : 0.0;
                }
            }


            if (amountPaid > 0 && amountPaid < feeAmount) {
                double balance = feeAmount - amountPaid;
                FeesSummaryTable feesSummaryTable = new FeesSummaryTable(
                        student.getStudentId(),
                        student.getFirstName() + " " + student.getLastName(),
                        student.getLevel().getLevelName(),
                        String.valueOf(feeAmount),
                        String.valueOf(amountPaid),
                        String.valueOf(balance)
                );
                feesSummaryTables.add(feesSummaryTable);
            }
        }

        if (feesSummaryTables.isEmpty()) {
            throw new Exception("No records found");
        }
        return feesSummaryTables;
    }

    public List<FeesSummaryTable> getNotPaidStudents(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff not found");
        }

        Institution institution = staff.getInstitution();

        List<Students> students = institution.getStudents();
        List<FeesSummaryTable> feesSummaryTables = new ArrayList<>();

        //Get current Semester
        String currentSemester = utilityClass.getCurrentSemesterId(institution.getInstitutionId());
        if (currentSemester == null || currentSemester.isEmpty()) {
            throw new Exception("Semester not found");
        }

        for (Students student: students) {
            String levelId = student.getLevel().getLevelID();

            Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemester, levelId).orElse(null);
            if (fee == null) {
                throw new Exception("Not all semester fees added");
            }

            double feeAmount = fee.getAmountToBePayed();

            //Get feesReport List
            List<FeesReport> feesReports = fee.getFeesReport();
            if (feesReports == null || feesReports.isEmpty()) {
                throw new Exception("No records found");
            }
            double amountPaid = 0.0;
            for (FeesReport feesReport: feesReports) {
                if (feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
                    amountPaid += feesReport.getAmountPaid();
                }
            }

            if (amountPaid < 0.01) {
                FeesSummaryTable feesSummaryTable = new FeesSummaryTable(
                        student.getStudentId(),
                        student.getFirstName() + " " + student.getLastName(),
                        student.getLevel().getLevelName(),
                        String.valueOf(feeAmount),
                        String.valueOf(amountPaid),
                        String.valueOf(feeAmount - amountPaid)
                );
                feesSummaryTables.add(feesSummaryTable);
            }
        }

        if (feesSummaryTables.isEmpty()) {
            throw new Exception("No records found");
        }
        return feesSummaryTables;
    }

    public List<RecentPaymentTable> getRecentPayments(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff not found");
        }

        String institutionId = staff.getInstitution().getInstitutionId();

        String currentSemester = utilityClass.getCurrentSemesterId(institutionId);

        List<FeesReport> feesReports = feesReportRepository
                .findByInstitution_InstitutionIdOrderByDateOfPaymentDesc(institutionId);

        List<RecentPaymentTable> recentPaymentTables = new ArrayList<>();
        int counter = 0;
        for (FeesReport feesReport: feesReports) {
            if (feesReport.getFees().getSemester().getSemesterID().equals(currentSemester)) {
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
        return recentPaymentTables;
    }

}
