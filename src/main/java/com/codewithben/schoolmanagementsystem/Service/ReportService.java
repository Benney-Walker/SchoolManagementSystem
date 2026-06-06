package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentReport;
import com.codewithben.schoolmanagementsystem.DTO.Report.SubjectReportDTO;
import com.codewithben.schoolmanagementsystem.DTO.Report.ViewClassSemesterReport;
import com.codewithben.schoolmanagementsystem.DTO.Result.ViewStudentsSubjectsResults;
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
    private final StudentsRepository studentsRepository;

    private final ResultsRepository resultsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final JasperReportService jasperReportService;

    private final LoggingService loggingService;

    private final StudentService studentService;

    public ResponseEntity<?> getClassBulkReport(String staffId, String levelId, String semesterId) {
        String logData = "Class Id: " + levelId + " Semester Id: " + semesterId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Level ID"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "Invalid Term Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Semester ID"
            ));
        }

        String totalAttendance = String.valueOf(
                studentService.getTotalAttendanceCount(semester)
        );
        String resumingDate = getResumingDate(level,  semester);

        try {
            List<GenerateStudentReport> reportData = generateClassReports(level, semester, resumingDate, totalAttendance);

            byte[] reportPDF = jasperReportService.generateClassReportCards(reportData, level.getInstitution().getInstitutionId());

            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().body(reportPDF);

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();

            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "Class has no records for this semester", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Class has no records for this semester"
            ));
        } catch (Exception e) {
            e.printStackTrace();

            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "Internal Server Error! Contact developers", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Internal Server Error! Contact developers"
            ));
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

    public ResponseEntity<?> promoteStudent(String studentId, String levelId, String staffId) {
        String logData = "Level Id: " + levelId + " studentId: " + studentId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.PROMOTE, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.PROMOTE, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        student.setLevel(level);
        studentsRepository.save(student);

        loggingService.logGeneralActivity(LogType.STUDENT, LogAction.PROMOTE, "Invalid student Id", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }
}
