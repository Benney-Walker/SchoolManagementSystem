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


    public List<GenerateStudentReport> generateClassReports(
            Level level, Semester semester, String resumingDate, String totalAttendance
    ) {
        //Report List container
        List<GenerateStudentReport> generateStudentReports = new ArrayList<>();

        //Retrieve list of results from database
        List<Results> classResults = resultsRepository.findByLevel_LevelIDAndSemester_SemesterIDOrderByTotalScoreDesc(
                level.getLevelID(), semester.getSemesterID()
        );
        if (classResults == null || classResults.isEmpty()) {
            throw new IllegalArgumentException("No Results Found for this criteria");
        }

        for (Results result : classResults) {
            String studentId = result.getStudent().getStudentId();
            String studentName = result.getStudent().getFirstName() +
                    " " + result.getStudent().getLastName();
            String className = level.getLevelName();
            String semesterName = semester.getSemesterName();
            String totalScore = result.getTotalScore().toString();
            String averageScore = result.getAverageScore().toString();
            String position = result.getPosition();
            String academicYear = semester.getAcademicYear();
            String totalStudents = String.valueOf(classResults.size());
            String vacationDate = semester.getSemesterEndDate().toString();
            String attendancePresent = String.valueOf(
                    studentService.getStudentPresentAttendance(studentId, semester.getSemesterID())
            );
            String semesterRemark = "Not field yet"; //Not included on report yet
            String instructorName = level.getStaff().getFirstName() +
                    " " + level.getStaff().getLastName();
            String currentDate = LocalDate.now().toString();

            //Get subject scores
            List<SubjectReportDTO> scores = new ArrayList<>();

            List<SubjectScore> subjectScores = result.getSubjectScores();
            for (SubjectScore subjectScore : subjectScores) {

                SubjectReportDTO score = new SubjectReportDTO(
                        subjectScore.getSubject().getSubjectName(),
                        subjectScore.getClassScore().toString(),
                        subjectScore.getCalculatedExamScore().toString(),
                        subjectScore.getTotalScore().toString(),
                        subjectScore.getGrade(),
                        subjectScore.getGradeDescriptor()
                );

                scores.add(score);
            }

            GenerateStudentReport generateStudentReport = new GenerateStudentReport(
                    studentId, studentName, className, semesterName,
                    position, totalScore, averageScore, academicYear,
                    totalStudents, vacationDate, resumingDate, attendancePresent,
                    totalAttendance, instructorName, currentDate, "-", scores
            );

            generateStudentReports.add(generateStudentReport);
        }

        return generateStudentReports;
    }

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
