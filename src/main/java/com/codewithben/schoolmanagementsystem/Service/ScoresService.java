package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsScoresTable;
import com.codewithben.schoolmanagementsystem.DTO.Subject.SubjectScores;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

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

    @Transactional
    public ResponseEntity<?> saveScores(String staffId, String subjectId, String semesterId, List<StudentsScoresTable> scores) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.CREATE,"Invalid semester Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid term Id"
            ));
        }

        List<GradeSystem> gradeSystem = gradeSystemRepository.findAllByInstitution_InstitutionId(
                semester.getInstitution().getInstitutionId()
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

        Set<Results> affectedResults = new LinkedHashSet<>();

        for (StudentsScoresTable score : scores) {

            // Get or create Results for this student + semester + level
            Results result = resultsRepository
                    .findByStudent_StudentIdAndSemester_SemesterID(
                            score.getStudentId(), semesterId)
                    .orElse(null);

            if (result == null) {

                Students student = studentsRepository.findByStudentId(score.getStudentId()).orElse(null);
                if (student == null) {
                    loggingService.logGeneralActivity(LogType.SUBJECT_SCORE, LogAction.CREATE,"Invalid student Id", staffId, LogStatus.FAILED);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                            "message", "Invalid student Id"
                    ));
                }

                result = new Results();
                result.setStudent(student);
                result.setLevel(subject.getLevel());
                result.setSemester(semester);
                result.setUpdatedBy(staff);
                result.setCreatedAt(LocalDate.now());
                result.setTotalScore(Double.valueOf(String.format("%.2f", 0.0)));
                result.setAverageScore(Double.valueOf(String.format("%.2f", 0.0)));
                resultsRepository.save(result);
            }

            SubjectScore subjectScore = subjectScoreRepository
                    .findBySubject_SubjectIdAndResults_ResultId(
                            subjectId,
                            result.getResultId()
                    ).orElse(new SubjectScore());

            saveNewScore(subjectScore, score, result, subject, result.getStudent(), semester);

            affectedResults.add(result);
        }

        subjectScoreRepository.flush();

        int classSize = utilityClass.getActiveStudents(subject.getLevel().getStudents()).size();
        int subjectsInLevel = subject.getLevel().getSubjects().size();
        for (Results result : affectedResults) {
            updateResultTotals(result, staff, classSize);

            long scoresSubjects = subjectScoreRepository.countByResults_ResultId(result.getResultId());
            result.setReady(scoresSubjects == subjectsInLevel);

            result.setUpdatedAt(LocalDate.now());
            result.setUpdatedBy(staff);
        }

        resultsRepository.saveAll(affectedResults);

        loggingService.logGeneralActivity(
                LogType.SUBJECT_SCORE, LogAction.CREATE,
                "Saved " + subject.getSubjectName() + " scores for " + subject.getLevel().getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }


    public void saveNewScore(SubjectScore subjectScore, StudentsScoresTable score, Results result, Subjects subject, Students student, Semester semester) {

        double projectWork = Double.parseDouble(score.getProjectScore());
        double classTest1 = Double.parseDouble(score.getClassTest1Score());
        double classTest2 = Double.parseDouble(score.getClassTest2Score());
        double groupWork = Double.parseDouble(score.getGroupWorkScore());
        double classScore = Double.parseDouble(score.getClassScore());
        double examScore = Double.parseDouble(score.getExamScore());
        double calculatedExamScore = Double.parseDouble(score.getCalculatedExamScore());
        double totalScore =  classScore + calculatedExamScore;

        subjectScore.setSubject(subject);
        subjectScore.setStudent(student);
        subjectScore.setResults(result);
        subjectScore.setProjectWork(Double.parseDouble(String.format("%.1f", projectWork)));
        subjectScore.setClassTest1(Double.parseDouble(String.format("%.1f", classTest1)));
        subjectScore.setGroupWork(Double.parseDouble(String.format("%.1f", groupWork)));
        subjectScore.setClassTest2(Double.parseDouble(String.format("%.1f", classTest2)));
        subjectScore.setClassScore(Double.valueOf(String.format("%.1f", classScore)));
        subjectScore.setExamScore(Double.parseDouble(String.format("%.1f", examScore)));
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
    }

    /* ===================================
                    HELPERS
    =====================================*/

    public void updateResultTotals(Results result, Staffs updatedBy, int classSize) {
        List<SubjectScore> scores = result.getSubjectScores();

        double total = 0.0;
        if (scores == null || scores.isEmpty()) {
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
        } else {

            for(SubjectScore score : scores) {
                total += score.getTotalScore();
            }

            result.setUpdatedBy(updatedBy);
            result.setTotalScore(Double.parseDouble(String.format("%.1f", total)));
            result.setAverageScore(Double.parseDouble(String.format("%.1f", total / scores.size())));
            result.setClassSize(classSize);
        }

        resultsRepository.save(result);
    }
}
