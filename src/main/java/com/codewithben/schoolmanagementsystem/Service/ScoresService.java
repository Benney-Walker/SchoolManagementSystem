package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Report.StudentSubjectReport;
import com.codewithben.schoolmanagementsystem.DTO.Result.SaveStudentScores;
import com.codewithben.schoolmanagementsystem.DTO.Result.StudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsScoresTable;
import com.codewithben.schoolmanagementsystem.DTO.Subject.SubjectScores;
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
public class ScoresService {

    private final StaffsRepository staffsRepository;

    private final StudentsRepository studentsRepository;

    private final LoggingService loggingService;

    private final GradeSystemRepository gradeSystemRepository;

    private final SubjectsRepository subjectsRepository;

    private final SemesterRepository semesterRepository;

    private final ResultsRepository resultsRepository;

    private final SubjectScoreRepository subjectScoreRepository;

    private final UtilityClass utilityClass;

    @Transactional
    public ResponseEntity<?> addStudentSubjectScores(SaveStudentScores scores, String staffId, String subjectId, String semesterId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.CREATE,"Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Students student = studentsRepository.findByStudentId(scores.getStudentId()).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.CREATE,"Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        List<GradeSystem> gradeSystem = gradeSystemRepository.findAllByInstitution_InstitutionId(
                student.getInstitution().getInstitutionId()
        );
        if (gradeSystem == null || gradeSystem.isEmpty()) {
            loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.CREATE,"No grading criteria saved on system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No grading criteria saved on system"
            ));
        }

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.CREATE,"Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid subject ID"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.CREATE,"Invalid semester Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid semester ID"
            ));
        }


        // Get or create Results for this student + semester + level
        Results result = resultsRepository
                .findByStudent_StudentIdAndSemester_SemesterID(
                        student.getStudentId(), semesterId)
                .orElse(null);

        if (result == null) {
            result = new Results();
            result.setStudent(student);
            result.setLevel(subject.getLevel());
            result.setSemester(semester);
            result.setCreatedAt(LocalDate.now());
            result.setTotalScore(Double.valueOf(String.format("%.2f", 0.0)));
            result.setAverageScore(Double.valueOf(String.format("%.2f", 0.0)));
            result.setUpdatedBy(staff);
        }

        // Check if score already exists
        SubjectScore subjectScore = subjectScoreRepository
                .findByStudent_StudentIdAndSubject_SubjectIdAndResults_ResultId(
                        student.getStudentId(),
                        subject.getSubjectId(),
                        result.getResultId()
                ).orElse(new SubjectScore());

        double projectWork = Double.parseDouble(scores.getProjectScore());
        double classTest1 = Double.parseDouble(scores.getClassTest1Score());
        double classTest2 = Double.parseDouble(scores.getClassTest2Score());
        double groupWork = Double.parseDouble(scores.getGroupWorkScore());
        double classScore = Double.parseDouble(scores.getClassScore());
        double examScore = Double.parseDouble(scores.getExamScore());
        double calculatedExamScore = Double.parseDouble(scores.getCalculatedExamScore());
        double totalScore =  classScore + calculatedExamScore;

        subjectScore.setSubject(subject);
        subjectScore.setStudent(student);
        subjectScore.setResults(result);
        subjectScore.setProjectWork(Double.parseDouble(String.format("%.2f", projectWork)));
        subjectScore.setClassTest1(Double.parseDouble(String.format("%.2f", classTest1)));
        subjectScore.setGroupWork(Double.parseDouble(String.format("%.2f", groupWork)));
        subjectScore.setClassTest2(Double.parseDouble(String.format("%.2f", classTest2)));
        subjectScore.setClassScore(Double.valueOf(String.format("%.2f", classScore)));
        subjectScore.setExamScore(Double.parseDouble(String.format("%.2f", examScore)));
        subjectScore.setCalculatedExamScore(calculatedExamScore);
        subjectScore.setSemester(semester);
        subjectScore.setGrade(
                utilityClass.extractGrade(totalScore, student.getInstitution().getInstitutionId())
        );
        subjectScore.setGradeDescriptor(
                utilityClass.extractDescription(totalScore, student.getInstitution().getInstitutionId())
        );
        subjectScore.setTotalScore(totalScore);

        subjectScoreRepository.save(subjectScore);

        //Add scores to results
        List<SubjectScore> resultsScores = result.getSubjectScores();
        if (resultsScores == null || resultsScores.isEmpty()) {
            resultsScores = new ArrayList<>();
        }
        resultsScores.add(subjectScore);
        result.setSubjectScores(resultsScores);
        resultsRepository.save(result);

        updateResultTotals(result, staff, subject);

        loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.CREATE,"N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadStudentsForScores(String semesterId, String subjectId, String staffId) {

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.READ, "Invalid subject Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid subject Id"
            ));
        }

        //Gets the active students
        List<Students> students =
                utilityClass.getActiveStudents(subject.getLevel().getStudents());
        if (students == null || students.isEmpty()) {
            loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.READ, "Class has no students", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Class has no students"
            ));
        }

        String subjectName = subject.getSubjectName();
        List<StudentsScoresTable> subjectStudents = new ArrayList<>();
        for (Students student : students) {
            String studentId = student.getStudentId();
            String studentName = student.getFirstName() + " " + student.getLastName();

            StudentsScoresTable scoresTable = new StudentsScoresTable();

            // Check if student has scores for this subject
            SubjectScore score = subjectScoreRepository.findByStudent_StudentIdAndSubject_SubjectIdAndSemester_SemesterID(
                    studentId, subjectId, semesterId
            ).orElse(null);

            if (score == null) {

                scoresTable.setStudentId(studentId);
                scoresTable.setStudentName(studentName);
                scoresTable.setClassTest1Score("0");
                scoresTable.setClassTest2Score("0");
                scoresTable.setProjectScore("0");
                scoresTable.setGroupWorkScore("0");
                scoresTable.setClassScore("0");
                scoresTable.setExamScore("0");
                scoresTable.setCalculatedExamScore("0");

            } else {

                scoresTable.setStudentId(studentId);
                scoresTable.setStudentName(studentName);
                scoresTable.setClassTest1Score(
                        String.valueOf(score.getClassTest1())
                );
                scoresTable.setClassTest2Score(
                        String.valueOf(score.getClassTest2())
                );
                scoresTable.setGroupWorkScore(
                        String.valueOf(score.getGroupWork())
                );
                scoresTable.setProjectScore(
                        String.valueOf(score.getProjectWork())
                );
                scoresTable.setClassScore(
                        String.valueOf(score.getClassScore())
                );
                scoresTable.setExamScore(
                        String.valueOf(score.getExamScore())
                );
                scoresTable.setCalculatedExamScore(
                        String.valueOf(score.getCalculatedExamScore())
                );

            }

            subjectStudents.add(scoresTable);
        }

        loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(
                new SubjectScores(subjectName, subjectId, subjectStudents)
        );
    }

    /* ===================================
                    HELPERS
    =====================================*/

    private void updateResultTotals(Results result, Staffs staff, Subjects subject) {
        List<SubjectScore> scores = subjectScoreRepository.findByResults_ResultId(result.getResultId());

        double total = 0.0;
        if (scores == null || scores.isEmpty()) {
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
        } else {

            for(SubjectScore score : scores) {
                total += score.getTotalScore();
            }

            result.setTotalScore(total);
            result.setAverageScore(total / scores.size());
        }

        result.setUpdatedAt(LocalDate.now());
        result.setUpdatedBy(staff);
        resultsRepository.save(result);

        utilityClass.reArrangePositions(subject, result.getSemester());
    }
}
