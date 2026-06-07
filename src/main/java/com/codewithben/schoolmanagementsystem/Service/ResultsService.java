package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Report.*;
import com.codewithben.schoolmanagementsystem.DTO.Result.SbaRecords;
import com.codewithben.schoolmanagementsystem.DTO.Result.ViewStudentsSubjectsResults;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class ResultsService {

    private final LevelRepository levelRepository;

    private final LoggingService loggingService;

    private final SemesterRepository semesterRepository;

    private final ResultsRepository resultsRepository;

    private final AttendanceService attendanceService;

    public ResponseEntity<?> viewClassSemesterReport(String levelId, String semesterId, String staffId) {

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "Invalid Class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Class Id"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "Invalid Term Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Term Id"
            ));
        }

        List<ViewClassSemesterReport> viewClassSemesterReports = new ArrayList<>();

        List<Results> studentResults = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterIDOrderByTotalScoreDesc(levelId, semesterId);
        if (studentResults == null || studentResults.isEmpty()) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "No Results Found for this criteria", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No Results Found for this criteria"
            ));
        }

        for (Results result : studentResults) {

            String studentId = result.getStudent().getStudentId();
            String studentName = result.getStudent().getFirstName() +
                    " " + result.getStudent().getLastName();
            String className = level.getLevelName();
            String semesterName = semester.getSemesterName();
            String totalScore = result.getTotalScore().toString();
            String averageScore = result.getAverageScore().toString();
            String position = result.getPosition();
            String totalStudents = String.valueOf(studentResults.size());
            String semesterRemark = "Not field yet";

            List<ViewStudentsSubjectsResults> scoresList = new ArrayList<>();

            //Fetch subject scores
            List<SubjectScore> subjectScores = result.getSubjectScores();
            for (SubjectScore subjectScore : subjectScores) {

                String subjectName = subjectScore.getSubject().getSubjectName();
                String classTest1Score = subjectScore.getClassTest1().toString();
                String classTest2Score = subjectScore.getClassTest2().toString();
                String groupWorkScore = subjectScore.getGroupWork().toString();
                String projectWorkScore = subjectScore.getProjectWork().toString();
                String classScore = subjectScore.getClassScore().toString();
                String examScore = subjectScore.getExamScore().toString();
                String calculatedExamScore = subjectScore.getCalculatedExamScore().toString();
                String total = subjectScore.getTotalScore().toString();
                String grade = subjectScore.getGrade();
                String description = subjectScore.getGradeDescriptor();

                ViewStudentsSubjectsResults scores = new ViewStudentsSubjectsResults(
                        subjectName, classTest1Score, groupWorkScore,
                        classTest2Score, projectWorkScore, classScore,
                        examScore, calculatedExamScore, total,
                        grade, description
                );

                scoresList.add(scores);
            }

            ViewClassSemesterReport results =  new ViewClassSemesterReport(
                    studentId, studentName, className, semesterName, position,
                    totalScore, averageScore, semesterRemark, scoresList, totalStudents
            );

            viewClassSemesterReports.add(results);
        }

        loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(viewClassSemesterReports);
    }

    public ResponseEntity<?> viewSba(String staffId, String levelId, String semesterId, String subjectId) {
        List<Results> resultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId);
        if (resultsList == null || resultsList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        List<SbaRecords> sbaRecords = generateSbaRecords(resultsList, subjectId);

        loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(sbaRecords);
    }

    public ResponseEntity<?> viewMasterScoreSheet(String levelId, String semesterId, String staffId) {

        List<Results> resultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId);
        if (resultsList == null || resultsList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        MasterScoreSheet masterScoreSheet = generateMasterSheetRecord(resultsList);
        loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(masterScoreSheet);
    }

    public GenerateStudentResult generateStudentResult(
            Results result, String resumingDate, String totalAttendance
    ) {

        try {
            String studentId = result.getStudent().getStudentId();
            String studentName = result.getStudent().getFirstName() +
                    " " + result.getStudent().getLastName();
            String className = result.getLevel().getLevelName();
            String semesterName = result.getSemester().getSemesterName();
            String totalScore = result.getTotalScore().toString();
            String averageScore = result.getAverageScore().toString();
            String position = result.getPosition();
            String academicYear = result.getSemester().getAcademicYear();
            String totalStudents = String.valueOf(result.getLevel().getStudents().size());
            String vacationDate = result.getSemester().getSemesterEndDate().toString();
            String attendancePresent = String.valueOf(
                    attendanceService.getStudentPresentAttendance(studentId, result.getSemester().getSemesterID())
            );

            String instructorName = result.getLevel().getStaff().getFirstName() +
                    " " + result.getLevel().getStaff().getLastName();
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

            return new GenerateStudentResult(
                    studentId, studentName, className, semesterName,
                    position, totalScore, averageScore, academicYear,
                    totalStudents, vacationDate, resumingDate, attendancePresent,
                    totalAttendance, instructorName, currentDate, "-", scores, null
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /*===============================================
                          Helpers
     ================================================*/
    public List<SbaRecords> generateSbaRecords(List<Results> resultsList, String subjectId) {

        try {
            //Retrieve sba
            List<SbaRecords> sbaRecords = new ArrayList<>();
            for (Results results : resultsList) {
                String studentId = results.getStudent().getStudentId();
                String studentName = results.getStudent().getFirstName() + " " + results.getStudent().getLastName();
                String classTest1Score = "";
                String groupWorkScore = "";
                String classTest2Score = "";
                String projectScore = "";
                String classScore = "";
                String examScore = "";
                String calculatedExamScore = "";

                List<SubjectScore> subjectScores = results.getSubjectScores();
                for (SubjectScore subjectScore : subjectScores) {
                    if (subjectScore.getSubject().getSubjectId().equals(subjectId)) {
                        classTest1Score = subjectScore.getClassTest1().toString();
                        groupWorkScore = subjectScore.getGroupWork().toString();
                        classTest2Score = subjectScore.getClassTest2().toString();
                        projectScore = subjectScore.getProjectWork().toString();
                        classScore = subjectScore.getClassScore().toString();
                        examScore = subjectScore.getExamScore().toString();
                        calculatedExamScore = subjectScore.getCalculatedExamScore().toString();
                        break;
                    }
                }

                SbaRecords sbaRecord = SbaRecords.builder()
                        .studentId(studentId)
                        .studentName(studentName)
                        .classTest1Score(classTest1Score)
                        .classTest2Score(classTest2Score)
                        .groupWorkScore(groupWorkScore)
                        .projectScore(projectScore)
                        .examScore(examScore)
                        .classScore(classScore)
                        .calculatedExamScore(calculatedExamScore)
                        .build();
                sbaRecords.add(sbaRecord);
            }

            return sbaRecords;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MasterScoreSheet generateMasterSheetRecord(List<Results> resultsList) {

        try {
            String semesterName = null;
            String className = null;
            String academicYear = null;
            List<String> subjects = new ArrayList<>();

            List<MasterScoreSheetRow> rows = new ArrayList<>();
            for (Results result : resultsList) {

                List<SubjectScore> subjectScores = result.getSubjectScores();
                if (subjectScores == null || subjectScores.isEmpty()) continue;

                Map<String, Double> scores = new LinkedHashMap<>();
                for (SubjectScore subjectScore : subjectScores) {

                    if (!subjects.contains(subjectScore.getSubject().getSubjectName())) {
                        subjects.add(subjectScore.getSubject().getSubjectName());
                    }

                    scores.put(
                            subjectScore.getSubject().getSubjectName(),
                            subjectScore.getTotalScore()
                    );
                }
                String[] splitPositionString = result.getPosition().split("");
                String position = "";
                if (splitPositionString.length == 3) {
                    position = splitPositionString[0];
                } else if (splitPositionString.length == 4) {
                    position = splitPositionString[0] + splitPositionString[1];
                }

                MasterScoreSheetRow record = MasterScoreSheetRow.builder()
                        .studentId(result.getStudent().getStudentId())
                        .studentName(
                                result.getStudent().getFirstName() + " " + result.getStudent().getLastName()
                                )
                        .subjectScores(scores)
                        .studentTotalScore(result.getTotalScore())
                        .studentAverageScore(result.getAverageScore())
                        .position(Integer.parseInt(position))
                        .build();
                rows.add(record);

                if (semesterName == null || className == null || academicYear == null) {
                    semesterName = result.getSemester().getSemesterName();
                    className = result.getLevel().getLevelName();
                    academicYear = result.getSemester().getAcademicYear();
                }
            }

            return new MasterScoreSheet(className, semesterName, academicYear, subjects, rows);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }
}
