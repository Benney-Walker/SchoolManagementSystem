package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Report.StudentSubjectReport;
import com.codewithben.schoolmanagementsystem.DTO.Report.ViewClassSemesterReport;
import com.codewithben.schoolmanagementsystem.DTO.Result.SbaRecords;
import com.codewithben.schoolmanagementsystem.DTO.Result.StudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Result.ViewStudentsSubjectsResults;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class ResultsService {

    private final LevelRepository levelRepository;

    private final LoggingService loggingService;

    private final SemesterRepository semesterRepository;

    private final ResultsRepository resultsRepository;

    private final StudentsRepository studentsRepository;

    public ResponseEntity<?> viewClassSemesterReport(String levelId, String semesterId, String staffId) {
        String logData = "Class Id: " + levelId + " semester Id: " + semesterId;

        //This finds the level and the semester with their IDs
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

    public ResponseEntity<?> findStudentResults(String studentId, String semesterId, String levelId, String staffId) {
        String logData = "Student Id: " + " Semester Id: " + semesterId + " class Id: " + levelId;

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }

        Results results = resultsRepository.findByStudent_StudentIdAndSemester_SemesterID(
                studentId, semesterId
        ).orElse(null);
        if (results == null) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"No result available for filter", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Results not found");
        }

        List<StudentSubjectReport> subjectResults = new ArrayList<>();

        List<SubjectScore> subjectScores = results.getSubjectScores();

        for (SubjectScore subjectScore : subjectScores) {
            if (subjectScore.getStudent().getStudentId().equals(studentId)) {
                StudentSubjectReport studentSubjectReport = new StudentSubjectReport(
                        subjectScore.getSubject().getSubjectName(),
                        String.valueOf(subjectScore.getTotalScore()),
                        subjectScore.getGrade(),
                        subjectScore.getGradeDescriptor()
                );
                subjectResults.add(studentSubjectReport);
            }
        }

        String studentID = results.getStudent().getStudentId();
        String studentName = student.getFirstName() + " " + student.getLastName();
        String semesterName = results.getSemester().getSemesterName();
        String levelName = results.getLevel().getLevelName();
        Double totalScore = results.getTotalScore();
        Double averageScore = results.getAverageScore();
        String position = results.getPosition();

        loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(new StudentResult(
                studentID, studentName, levelName, semesterName, totalScore,
                averageScore, position, subjectResults
        ));
    }

    public ResponseEntity<?> viewSba(String staffId, String levelId, String semesterId, String subjectId) {

        List<Results> resultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId);
        if (resultsList == null || resultsList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"No records found  for filter", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

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

        return ResponseEntity.ok(sbaRecords);
    }
}
