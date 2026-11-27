package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Report.MasterScoreSheet;
import com.codewithben.schoolmanagementsystem.DTO.Report.SbaReport;
import com.codewithben.schoolmanagementsystem.DTO.Result.SbaRecords;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@AllArgsConstructor
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

    private final ResultAccessService resultAccess;

    private final ReportCardService reportCardService;

    public ResponseEntity<?> generateStudentReport(String studentId, String semesterId, String staffId) {

        // Caller's own school only; accountants can't download report cards.
        ResultAccessService.Lookup<Staffs> viewer = resultAccess.viewer(staffId, LogType.REPORT);
        if (viewer.failed()) {
            return viewer.error();
        }
        ResultAccessService.Lookup<Semester> semesterLookup = resultAccess.semester(viewer.value(), semesterId, LogType.REPORT);
        if (semesterLookup.failed()) {
            return semesterLookup.error();
        }
        Semester semester = semesterLookup.value();

        ResultAccessService.Lookup<Results> resultLookup =
                resultAccess.studentResult(viewer.value(), studentId, semester, LogType.REPORT);
        if (resultLookup.failed()) {
            return resultLookup.error();
        }
        Results studentResult = resultLookup.value();

        // One set of rules for preview, PDF and class report cards (ReportCardService):
        // conduct recorded, every subject scored, third-term promotion done.
        ReportCardService.Outcome outcome = reportCardService.assemble(
                studentResult, semester, reportCardService.termContext(semester));
        if (outcome.refused()) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, outcome.problem(), staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", outcome.problem()
            ));
        }
        GenerateStudentResult result = outcome.reportCard();

        try {
            byte[] studentReport = jasperReportService.generateStudentReportCard(result, studentResult.getStudent().getInstitution());

            loggingService.logGeneralActivity(
                    LogType.REPORT, LogAction.READ,
                    "Downloaded report card for " + studentResult.getStudent().getFirstName() + " " +
                            studentResult.getStudent().getLastName(),
                    staffId, LogStatus.SUCCESS
            );
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(studentReport);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> generateClassBulkReport(String staffId, String levelId, String semesterId) {

        ResultAccessService.Lookup<Staffs> viewer = resultAccess.viewer(staffId, LogType.REPORT);
        if (viewer.failed()) {
            return viewer.error();
        }
        ResultAccessService.Lookup<Level> levelLookup = resultAccess.level(viewer.value(), levelId, LogType.REPORT);
        if (levelLookup.failed()) {
            return levelLookup.error();
        }
        Level level = levelLookup.value();
        ResultAccessService.Lookup<Semester> semesterLookup = resultAccess.semester(viewer.value(), semesterId, LogType.REPORT);
        if (semesterLookup.failed()) {
            return semesterLookup.error();
        }
        Semester semester = semesterLookup.value();

        ReportCardService.TermContext term = reportCardService.termContext(semester);

        List<Results> classResultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId);
        if (classResultsList == null || classResultsList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "No records are found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records are found"
            ));
        }

        // Every report card in the class follows the same rules as a single one. Students who
        // have left (INACTIVE) get no report card, so they don't block the class.
        List<GenerateStudentResult> reportData = new ArrayList<>();
        for (Results result : classResultsList) {
            if (result.getStudent() == null || result.getStudent().getStudentStatus() == StudentStatus.INACTIVE) {
                continue;
            }
            ReportCardService.Outcome outcome = reportCardService.assemble(result, semester, term);
            if (outcome.refused()) {
                String studentName = result.getStudent().getFirstName() + " " + result.getStudent().getLastName();
                loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ,
                        studentName + ": " + outcome.problem(), staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", studentName + ": " + outcome.problem()
                ));
            }
            reportData.add(outcome.reportCard());
        }
        if (reportData.isEmpty()) {
            loggingService.logGeneralActivity(LogType.REPORT, LogAction.READ, "No records are found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records are found"
            ));
        }

        try {

            byte[] studentBulkReportPdf = jasperReportService.generateClassReportCards(reportData, level.getInstitution());

            loggingService.logGeneralActivity(
                    LogType.REPORT, LogAction.READ,
                    "Downloaded bulk report for " + level.getLevelName(),
                    staffId, LogStatus.SUCCESS
            );
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(studentBulkReportPdf);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating report for " + e);
        }
    }

    public ResponseEntity<?> generateSbaReport(String staffId, String levelId, String semesterId, String subjectId) {
        ResultAccessService.Lookup<Staffs> viewer = resultAccess.viewer(staffId, LogType.REPORT);
        if (viewer.failed()) {
            return viewer.error();
        }
        ResultAccessService.Lookup<Level> levelLookup = resultAccess.level(viewer.value(), levelId, LogType.REPORT);
        if (levelLookup.failed()) {
            return levelLookup.error();
        }
        Level level = levelLookup.value();
        ResultAccessService.Lookup<Semester> semesterLookup = resultAccess.semester(viewer.value(), semesterId, LogType.REPORT);
        if (semesterLookup.failed()) {
            return semesterLookup.error();
        }
        Semester semester = semesterLookup.value();
        // The subject must belong to this class (and so to the caller's school).
        ResultAccessService.Lookup<Subjects> subjectLookup =
                resultAccess.subject(viewer.value(), subjectId, level, LogType.REPORT);
        if (subjectLookup.failed()) {
            return subjectLookup.error();
        }
        Subjects subject = subjectLookup.value();

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
            byte[] sbaReportPdf = jasperReportService.generateSbaReport(sbaReport, level.getInstitution());

            loggingService.logGeneralActivity(
                    LogType.REPORT, LogAction.READ,
                    "Generated Sba report for " + level.getLevelName() + "' " +
                            subject.getSubjectName(), staffId, LogStatus.SUCCESS
            );
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(sbaReportPdf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> generateMasterScoreSheet(String levelId, String semesterId, String staffId) {

        // The class and term were not checked at all before.
        ResultAccessService.Lookup<Staffs> viewer = resultAccess.viewer(staffId, LogType.REPORT);
        if (viewer.failed()) {
            return viewer.error();
        }
        ResultAccessService.Lookup<Level> levelLookup = resultAccess.level(viewer.value(), levelId, LogType.REPORT);
        if (levelLookup.failed()) {
            return levelLookup.error();
        }
        ResultAccessService.Lookup<Semester> semesterLookup = resultAccess.semester(viewer.value(), semesterId, LogType.REPORT);
        if (semesterLookup.failed()) {
            return semesterLookup.error();
        }

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
            loggingService.logGeneralActivity(
                    LogType.RESULT, LogAction.READ,
                    "Generated master score sheet for " + masterScoreSheet.getClassName(),
                    staffId, LogStatus.SUCCESS
            );
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(scoreSheetPdf);
        } catch (Exception e)  {
            throw new RuntimeException("Error while generating report for " + e);
        }
    }

}
