package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Account.*;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public FeesService(FeesRepository feesRepository, StudentsRepository studentsRepository, FeesReportRepository feesReportRepository,
                       SemesterRepository semesterRepository, LevelRepository levelRepository,  StaffsRepository staffsRepository, UtilityClass utilityClass) {
        this.feesRepository = feesRepository;
        this.studentsRepository = studentsRepository;
        this.feesReportRepository = feesReportRepository;
        this.semesterRepository = semesterRepository;
        this.levelRepository = levelRepository;
        this.staffsRepository = staffsRepository;
        this.utilityClass = utilityClass;
    }

    // Displays student Fees
    public StudentFeesPaymentDisplay findStudentFeesPaymentDetails(String studentId, String levelId, String semesterId) throws Exception {

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                semesterId, levelId
        ).orElse(null);
        if (fees == null) {
            throw new Exception("Fees Not Found");
        }

        List<FeesReport> feesReports = fees.getFeesReport();

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            throw new Exception("Invalid student id");
        }

        //ArrayList to Store Fees Report
        List<StudentPaymentHistory> paymentHistoryList = new ArrayList<>();
        double totalAmountPaid = 0.0;

        for (FeesReport feesReport : feesReports) {
            if (feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
                totalAmountPaid += feesReport.getAmountPaid();

                StudentPaymentHistory paymentHistory = new StudentPaymentHistory(
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

    public List<ClassFeesSummary> loadClassFeesSummary(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff Not Found");
        }
        //Get institution from staff
        Institution institution = staff.getInstitution();

        //Retrieve all classes
        List<Level> levels = institution.getLevel();

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

        fees = new Fees();
        fees.setAmountToBePayed(feesAmount);
        fees.setSemester(semester);
        fees.setLevel(level);
        feesRepository.save(fees);

        List<Fees> levelFees = level.getFees();
        if (levelFees == null) {
            levelFees = new ArrayList<>();
        }
        levelFees.add(fees);
        levelRepository.save(level);
        return "Success";
    }

    public String updateSemesterFees(Double feesAmount, String semesterId, String levelID) throws Exception {
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelID).orElse(null);

        if (fees == null) {
            throw new Exception("Fees not found");
        }

        fees.setAmountToBePayed(feesAmount);
        feesRepository.save(fees);
        return "Fees Updated Successfully";
    }

    @Transactional
    public String addNewFeePayment(String studentId, Double amountPaid, String personWhoPaid, String phoneNumber,
                                   String semesterId) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId)
                .orElseThrow(() -> new Exception("Student not found"));

        String levelId = student.getLevel().getLevelID();

        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId)
                .orElseThrow(() -> new Exception("Fee not found"));

        // Calculate total already paid by this student for this fee
        double totalPreviouslyPaid = feesReportRepository
                .findByStudent_StudentIdAndFees_FeesId(student.getStudentId(), fees.getFeesId())
                .stream()
                .mapToDouble(FeesReport::getAmountPaid)
                .sum();

        double feesBalance = fees.getAmountToBePayed() - totalPreviouslyPaid - amountPaid;

        // Create and save
        FeesReport feesReport = new FeesReport();
        feesReport.setAmountPaid(amountPaid);
        feesReport.setPersonWhoPaid(personWhoPaid);
        feesReport.setPhoneNumber(phoneNumber);
        feesReport.setDateOfPayment(LocalDate.now());
        feesReport.setFees(fees);
        feesReport.setStudent(student);
        feesReport.setFeesBalance(feesBalance);

        feesReportRepository.save(feesReport);

        List<FeesReport> feesReports = fees.getFeesReport();
        if (feesReports == null) {
            feesReports = new ArrayList<>();
        }
        feesReports.add(feesReport);
        feesRepository.save(fees);

        return "Success";
    }

    public double getTotalLevelFees(String levelId, String semesterId) {
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId).orElse(null);
        if (fees == null) {
            return 0;
        }

        Level level = levelRepository.findByLevelIDAndFees_FeesId(levelId, fees.getFeesId()).orElse(null);
        if (level == null) {
            return 0;
        }

        Double feeAmount = fees.getAmountToBePayed();
        int numberOfStudents = level.getStudents().size();
        return feeAmount * numberOfStudents;
    }

    public double getTotalLevelFeesPaid(String semesterId, String levelId) {
        Fees fees = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(semesterId, levelId)
                .orElse(null);
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

        for (Students student: students) {
            String levelId = student.getLevel().getLevelID();

            Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemester, levelId).orElse(null);
            if (fee == null) {
                continue;
            }
            double feeAmount = fee.getAmountToBePayed();
            double amountPaid = 0.0;

            //Get feesReport List
            List<FeesReport> feesReports = fee.getFeesReport();
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
            return new ArrayList<>();
        }

        List<FeesSummaryTable> feesSummaryTables = new ArrayList<>();

        String currentSemester = utilityClass.getCurrentSemesterId(institution.getInstitutionId());
        if (currentSemester == null || currentSemester.isEmpty()) {
            return new ArrayList<>();
        }

        for (Students student : students) {
            if (student.getLevel() == null) continue;

            String levelId = student.getLevel().getLevelID();

            Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemester, levelId)
                    .orElse(null);
            if (fee == null) {
                continue;
            }

            double feeAmount = fee.getAmountToBePayed() != null ? fee.getAmountToBePayed() : 0.0;
            double amountPaid = 0.0;

            List<FeesReport> feesReports = fee.getFeesReport();
            if (feesReports != null) {
                for (FeesReport feesReport : feesReports) {
                    if (feesReport.getStudent() != null &&
                            feesReport.getStudent().getStudentId().equals(student.getStudentId())) {
                        amountPaid += feesReport.getAmountPaid() != null ? feesReport.getAmountPaid() : 0.0;
                    }
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

        for (Students student: students) {
            String levelId = student.getLevel().getLevelID();

            Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(currentSemester, levelId).orElse(null);
            if (fee == null) {
                continue;
            }
            double feeAmount = fee.getAmountToBePayed();
            double amountPaid = 0.0;

            //Get feesReport List
            List<FeesReport> feesReports = fee.getFeesReport();
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
        }
        return recentPaymentTables;
    }
}
