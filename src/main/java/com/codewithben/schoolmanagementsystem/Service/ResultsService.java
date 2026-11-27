package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Report.*;
import com.codewithben.schoolmanagementsystem.DTO.Result.SbaRecords;
import com.codewithben.schoolmanagementsystem.DTO.Result.ViewStudentsSubjectsResults;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ResultsService {

    private final LevelRepository levelRepository;

    private final LoggingService loggingService;

    private final SemesterRepository semesterRepository;

    private final ResultsRepository resultsRepository;

    private final AttendanceService attendanceService;

    private final SubjectsRepository subjectsRepository;

    private final UtilityClass utilityClass;

    private final ConductService conductService;

    private final ResultAccessService resultAccess;

    private final ReportCardService reportCardService;

    public ResponseEntity<?> viewStudentResult(String studentId, String semesterId, String staffId) {

        // Caller's own school only; accountants can't view results.
        ResultAccessService.Lookup<Staffs> viewer = resultAccess.viewer(staffId, LogType.RESULT);
        if (viewer.failed()) {
            return viewer.error();
        }
        ResultAccessService.Lookup<Semester> semesterLookup = resultAccess.semester(viewer.value(), semesterId, LogType.RESULT);
        if (semesterLookup.failed()) {
            return semesterLookup.error();
        }
        Semester semester = semesterLookup.value();

        ResultAccessService.Lookup<Results> resultLookup =
                resultAccess.studentResult(viewer.value(), studentId, semester, LogType.RESULT);
        if (resultLookup.failed()) {
            return resultLookup.error();
        }
        Results studentResult = resultLookup.value();

        // Same rules as the PDF (ReportCardService): conduct recorded, every subject scored,
        // third-term promotion done. The preview used to skip the promotion check.
        ReportCardService.Outcome outcome = reportCardService.assemble(
                studentResult, semester, reportCardService.termContext(semester));
        if (outcome.refused()) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ, outcome.problem(), staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", outcome.problem()
            ));
        }
        GenerateStudentResult result = outcome.reportCard();

        loggingService.logGeneralActivity(
                LogType.RESULT, LogAction.READ,
                "Downloaded report for " + studentResult.getStudent().getFirstName(),
                staffId, LogStatus.SUCCESS);
        // (The "school-name" header is gone: the client doesn't use it, and a non-ASCII school
        // name can be garbled in an HTTP header.)
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> viewClassSemesterReport(String levelId, String semesterId, String staffId) {

        ResultAccessService.Lookup<Staffs> viewer = resultAccess.viewer(staffId, LogType.RESULT);
        if (viewer.failed()) {
            return viewer.error();
        }
        ResultAccessService.Lookup<Level> levelLookup = resultAccess.level(viewer.value(), levelId, LogType.RESULT);
        if (levelLookup.failed()) {
            return levelLookup.error();
        }
        Level level = levelLookup.value();
        ResultAccessService.Lookup<Semester> semesterLookup = resultAccess.semester(viewer.value(), semesterId, LogType.RESULT);
        if (semesterLookup.failed()) {
            return semesterLookup.error();
        }
        Semester semester = semesterLookup.value();

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
            String totalScore = scoreText(result.getTotalScore());
            String averageScore = scoreText(result.getAverageScore());
            String position = result.getPosition() == null ? "" : result.getPosition();
            String totalStudents = String.valueOf(result.getClassSize());
            // There is no term remark yet; this used to send the placeholder "Not field yet".
            String semesterRemark = "";

            List<ViewStudentsSubjectsResults> scoresList = new ArrayList<>();

            // Subjects in a fixed (alphabetical) order, the same for every student.
            for (SubjectScore subjectScore : sortedBySubject(result.getSubjectScores())) {

                String subjectName = subjectName(subjectScore);
                String classTest1Score = scoreText(subjectScore.getClassTest1());
                String classTest2Score = scoreText(subjectScore.getClassTest2());
                String groupWorkScore = scoreText(subjectScore.getGroupWork());
                String projectWorkScore = scoreText(subjectScore.getProjectWork());
                String classScore = scoreText(subjectScore.getClassScore());
                String examScore = scoreText(subjectScore.getExamScore());
                String calculatedExamScore = scoreText(subjectScore.getCalculatedExamScore());
                String total = scoreText(subjectScore.getTotalScore());
                String grade = subjectScore.getGrade() == null ? "" : subjectScore.getGrade();
                String description = subjectScore.getGradeDescriptor() == null ? "" : subjectScore.getGradeDescriptor();

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

        loggingService.logGeneralActivity(
                LogType.RESULT, LogAction.READ,
                "Fetched " + level.getLevelName() + "'s terminal report",
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(viewClassSemesterReports);
    }

    public ResponseEntity<?> viewSba(String staffId, String levelId, String semesterId, String subjectId) {
        ResultAccessService.Lookup<Staffs> viewer = resultAccess.viewer(staffId, LogType.RESULT);
        if (viewer.failed()) {
            return viewer.error();
        }
        ResultAccessService.Lookup<Level> levelLookup = resultAccess.level(viewer.value(), levelId, LogType.RESULT);
        if (levelLookup.failed()) {
            return levelLookup.error();
        }
        ResultAccessService.Lookup<Semester> semesterLookup = resultAccess.semester(viewer.value(), semesterId, LogType.RESULT);
        if (semesterLookup.failed()) {
            return semesterLookup.error();
        }
        // The subject must belong to this class (and so to the caller's school).
        ResultAccessService.Lookup<Subjects> subjectLookup =
                resultAccess.subject(viewer.value(), subjectId, levelLookup.value(), LogType.RESULT);
        if (subjectLookup.failed()) {
            return subjectLookup.error();
        }
        Subjects subject = subjectLookup.value();

        List<Results> resultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId);
        if (resultsList == null || resultsList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,"No records found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No records found"
            ));
        }

        /*if (!utilityClass.isClassResultsComplete(resultsList)) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,
                    "Results not complete! Positions or Promotions not done",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Results not complete! Positions or Promotions not done"
            ));
        }*/

        List<SbaRecords> sbaRecords = generateSbaRecords(resultsList, subjectId);

        loggingService.logGeneralActivity(
                LogType.RESULT, LogAction.READ,
                "Fetched sba records for " + subject.getSubjectName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(sbaRecords);
    }

    public ResponseEntity<?> viewMasterScoreSheet(String levelId, String semesterId, String staffId) {

        // The class and term were not checked at all before.
        ResultAccessService.Lookup<Staffs> viewer = resultAccess.viewer(staffId, LogType.RESULT);
        if (viewer.failed()) {
            return viewer.error();
        }
        ResultAccessService.Lookup<Level> levelLookup = resultAccess.level(viewer.value(), levelId, LogType.RESULT);
        if (levelLookup.failed()) {
            return levelLookup.error();
        }
        ResultAccessService.Lookup<Semester> semesterLookup = resultAccess.semester(viewer.value(), semesterId, LogType.RESULT);
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

        /*if (!utilityClass.isClassResultsComplete(resultsList)) {
            loggingService.logGeneralActivity(LogType.RESULT, LogAction.READ,
                    "Results not complete! Positions or Promotions not done",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Results not complete! Positions or Promotions not done"
            ));
        }*/

        String className = resultsList.getFirst().getLevel().getLevelName();

        MasterScoreSheet masterScoreSheet = generateMasterSheetRecord(resultsList);
        loggingService.logGeneralActivity(
                LogType.RESULT, LogAction.READ,
                "Fetched master score records for " + className,
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(masterScoreSheet);
    }

    /*===============================================
                          Helpers
     ================================================*/
    public List<SbaRecords> generateSbaRecords(List<Results> resultsList, String subjectId) {
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
            String totalScore = "";

            List<SubjectScore> subjectScores = results.getSubjectScores() == null ? List.of() : results.getSubjectScores();
            for (SubjectScore subjectScore : subjectScores) {
                if (subjectScore.getSubject() != null && subjectId.equals(subjectScore.getSubject().getSubjectId())) {
                    classTest1Score = scoreText(subjectScore.getClassTest1());
                    groupWorkScore = scoreText(subjectScore.getGroupWork());
                    classTest2Score = scoreText(subjectScore.getClassTest2());
                    projectScore = scoreText(subjectScore.getProjectWork());
                    classScore = scoreText(subjectScore.getClassScore());
                    examScore = scoreText(subjectScore.getExamScore());
                    calculatedExamScore = scoreText(subjectScore.getCalculatedExamScore());
                    totalScore = scoreText(subjectScore.getTotalScore());
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
                    .totalScore(totalScore)
                    .build();
            sbaRecords.add(sbaRecord);
        }
        // Alphabetical, so the sheet reads like the class register.
        sbaRecords.sort(Comparator.comparing((SbaRecords record) -> record.getStudentName().toLowerCase(Locale.ROOT)));
        return sbaRecords;
    }

    public MasterScoreSheet generateMasterSheetRecord(List<Results> resultsList) {

        String semesterName = null;
        String className = null;
        String academicYear = null;
        List<String> subjects = new ArrayList<>();

        List<MasterScoreSheetRow> rows = new ArrayList<>();
        for (Results result : resultsList) {

            // Header from the first result, even if nobody has scores yet (the sheet used to
            // print "CLASS: null" then).
            if (semesterName == null || className == null || academicYear == null) {
                semesterName = result.getSemester().getSemesterName();
                className = result.getLevel().getLevelName();
                academicYear = result.getSemester().getAcademicYear();
            }

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
            // "1st", "23rd", "105th" -> 1, 23, 105; not ranked yet -> 0. (The old parsing
            // handled only one- or two-digit positions and threw on a null position.)
            String digits = result.getPosition() == null ? "" : result.getPosition().replaceAll("\\D", "");
            String position = digits.isEmpty() ? "0" : digits;

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
        }

        // Subject columns in alphabetical order (they used to follow the order scores were
        // first seen, so two classes' sheets could have different column orders).
        subjects.sort(String.CASE_INSENSITIVE_ORDER);
        rows.sort(BY_POSITION);
        return new MasterScoreSheet(className, semesterName, academicYear, subjects, rows);
    }

    /*
     * Master sheet rows by position (Ben, 2026-11-27): 1st first, students not ranked yet
     * (position 0) at the bottom, ties and unranked students by name. Rows used to come out
     * in database order.
     */
    static final Comparator<MasterScoreSheetRow> BY_POSITION = Comparator
            .comparing((MasterScoreSheetRow row) -> row.getPosition() == null || row.getPosition() <= 0
                    ? Integer.MAX_VALUE : row.getPosition())
            .thenComparing(row -> row.getStudentName() == null ? "" : row.getStudentName().toLowerCase(Locale.ROOT));

    // Results created by attendance (or before totals were computed) may have no total yet.
    private static String scoreText(Double score) {
        return String.valueOf(score == null ? 0.0 : score);
    }

    private static List<SubjectScore> sortedBySubject(List<SubjectScore> scores) {
        List<SubjectScore> sorted = new ArrayList<>(scores == null ? List.of() : scores);
        sorted.sort(Comparator.comparing(score -> subjectName(score).toLowerCase(Locale.ROOT)));
        return sorted;
    }

    private static String subjectName(SubjectScore score) {
        return score.getSubject() == null || score.getSubject().getSubjectName() == null
                ? "" : score.getSubject().getSubjectName();
    }
}
