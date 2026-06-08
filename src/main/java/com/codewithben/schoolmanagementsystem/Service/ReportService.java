package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Report.MasterScoreSheet;
import com.codewithben.schoolmanagementsystem.DTO.Report.SbaReport;
import com.codewithben.schoolmanagementsystem.DTO.Result.SbaRecords;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ResultsRepository resultsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final JasperReportService jasperReportService;

    private final ResultsService resultsService;

    private final AttendanceService attendanceService;

    private final LoggingService loggingService;

    private final SubjectsRepository subjectsRepository;

    private final PdfGenerationService pdfGenerationService;

    private final ConductService conductService;

    private final UtilityClass utilityClass;

    public ResponseEntity<?> generateClassBulkReport(String staffId, String levelId, String semesterId) {

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Level ID"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "Invalid Term Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Semester ID"
            ));
        }

        String totalAttendance = String.valueOf(
                attendanceService.getTotalAttendanceCount(semester)
        );
        String resumingDate = utilityClass.getResumingDate(level,  semester);

        List<Results> classResultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId);
        if (classResultsList == null || classResultsList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "No records are found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records are found"
            ));
        }

        List<GenerateStudentResult> reportData = new ArrayList<>();
        //retrieve results
        for (Results result : classResultsList) {
            if (result.getConduct() == null) {
                loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "No Conduct records found for " + result.getStudent().getFirstName(), staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "message", "No Conduct records found for " + result.getStudent().getFirstName()
                ));
            }

            GenerateStudentResult generateStudentResult = resultsService.generateStudentResult(result, resumingDate, totalAttendance);
            generateStudentResult.setStudentConductReport(
                    conductService.getStudentConductReport(result.getConduct())
            );
            reportData.add(generateStudentResult);
        }

        try {

            byte[] studentBulkReportPdf = jasperReportService.generateClassReportCards(reportData, level.getInstitution().getInstitutionName());

            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().body(studentBulkReportPdf);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating report for " + e);
        }
    }

    public ResponseEntity<?> generateSbaReport(String staffId, String levelId, String semesterId, String subjectId) {
        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "Invalid subject Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Subject ID"
            ));
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Level ID"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "Invalid Term Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Semester ID"
            ));
        }

        List<Results> resultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId);
        if (resultsList == null || resultsList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ,"No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        List<SbaRecords> sbaRecords = resultsService.generateSbaRecords(resultsList, subjectId);

        //Create sba report object
        SbaReport sbaReport = SbaReport.builder()
                .semesterName(semester.getSemesterName())
                .academicYear(semester.getAcademicYear())
                .className(level.getLevelName())
                .subjectName(subject.getSubjectName())
                .sbaRecords(sbaRecords)
                .dateOfReport(LocalDate.now().toString())
                .build();

        try {
            byte[] sbaReportPdf = jasperReportService.generateSbaReport(sbaReport, level.getInstitution().getInstitutionName());

            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ,"N/A", staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok(sbaReportPdf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> generateMasterScoreSheet(String levelId, String semesterId, String staffId) {

        List<Results> resultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId);
        if (resultsList == null || resultsList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        MasterScoreSheet masterScoreSheet = resultsService.generateMasterSheetRecord(resultsList);

        try {
            byte[] scoreSheetPdf = pdfGenerationService.generateMasterSheetScore(masterScoreSheet);
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"N/A", staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok(scoreSheetPdf);
        } catch (Exception e)  {
            throw new RuntimeException("Error while generating report for " + e);
        }
    }

    /*================================================
                        HELPERS
     =================================================*/

    private String getResumingDate(Level level, Semester semester) {
        List<Semester> nextSemester = semesterRepository
                .findByInstitution_InstitutionId(level.getInstitution().getInstitutionId());
        return nextSemester.stream()
                .filter(s -> s.getSemesterStartDate().isAfter(semester.getSemesterEndDate()))
                .min(Comparator.comparing(Semester::getSemesterStartDate))
                .map(s -> s.getSemesterStartDate().toString())
                .orElse(null);
    }
}
