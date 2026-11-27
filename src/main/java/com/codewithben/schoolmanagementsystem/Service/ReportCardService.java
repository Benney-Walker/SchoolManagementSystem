package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Report.SubjectReportDTO;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Results;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.SubjectScore;
import com.codewithben.schoolmanagementsystem.Entity.Subjects;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Builds a student's report card, with one set of rules for every place that shows one:
 * the report-card preview, the report-card PDF and class report cards (and later the
 * guardian portal). The preview used to skip the third-term promotion check that the PDF
 * applied, so a card could preview fine and then refuse to download.
 *
 * A report card is produced only when:
 *   1. conduct has been recorded for the term;
 *   2. every subject the class has is scored (Ben, 2026-11-27);
 *   3. in the third term, promotion or repetition has been done.
 *
 * Building it never fails on missing data: an absent class teacher, term date, total or
 * position prints as blank or "To be announced" instead of a 500.
 */
@Service
@RequiredArgsConstructor
public class ReportCardService {

    static final String TO_BE_ANNOUNCED = "To be announced";

    private final AttendanceService attendanceService;

    private final ConductService conductService;

    private final UtilityClass utilityClass;

    /** The report card, or the reason it can't be produced yet. */
    public record Outcome(GenerateStudentResult reportCard, String problem) {

        public boolean refused() {
            return problem != null;
        }
    }

    /** Term-wide values shared by every report card of the term (computed once per request). */
    public record TermContext(String totalAttendance, String resumingDate) {
    }

    public TermContext termContext(Semester semester) {
        String resumingDate = utilityClass.getResumingDate(semester);
        return new TermContext(
                String.valueOf(attendanceService.getTotalAttendanceCount(semester)),
                resumingDate == null ? TO_BE_ANNOUNCED : resumingDate
        );
    }

    public Outcome assemble(Results result, Semester semester, TermContext term) {
        String problem = problemWith(result, semester);
        if (problem != null) {
            return new Outcome(null, problem);
        }
        GenerateStudentResult reportCard = build(result, term);
        reportCard.setStudentConductReport(conductService.getStudentConductReport(result.getConduct()));
        return new Outcome(reportCard, null);
    }

    /** Why the report card can't be produced, or null when it can. */
    String problemWith(Results result, Semester semester) {
        if (result.getConduct() == null) {
            return "Student conducts not uploaded yet";
        }
        List<String> missing = missingSubjects(result);
        if (!missing.isEmpty()) {
            return "Results not complete: no score yet for " + String.join(", ", missing);
        }
        boolean thirdTerm = semester != null && "THIRD_TERM".equals(semester.getSemesterName());
        if (thirdTerm && (result.getPromotionTo() == null || "-".equals(result.getPromotionTo()))) {
            return "Promotion activity not carried out";
        }
        return null;
    }

    /** Names of the class's current subjects the student has no score for, alphabetically. */
    static List<String> missingSubjects(Results result) {
        Level level = result.getLevel();
        if (level == null || level.getSubjects() == null || level.getSubjects().isEmpty()) {
            return List.of();
        }
        Set<String> scored = new HashSet<>();
        if (result.getSubjectScores() != null) {
            for (SubjectScore score : result.getSubjectScores()) {
                if (score.getSubject() != null && score.getTotalScore() != null) {
                    scored.add(score.getSubject().getSubjectId());
                }
            }
        }
        List<String> missing = new ArrayList<>();
        for (Subjects subject : level.getSubjects()) {
            if (!scored.contains(subject.getSubjectId())) {
                missing.add(subject.getSubjectName());
            }
        }
        missing.sort(String.CASE_INSENSITIVE_ORDER);
        return missing;
    }

    GenerateStudentResult build(Results result, TermContext term) {
        Semester semester = result.getSemester();
        Level level = result.getLevel();
        String studentId = result.getStudent().getStudentId();

        // Subjects in a fixed (alphabetical) order, the same on every report card.
        List<SubjectScore> subjectScores = new ArrayList<>(result.getSubjectScores() == null ? List.of() : result.getSubjectScores());
        subjectScores.sort(Comparator.comparing((SubjectScore score) ->
                score.getSubject() == null || score.getSubject().getSubjectName() == null
                        ? "" : score.getSubject().getSubjectName().toLowerCase(Locale.ROOT)));
        List<SubjectReportDTO> scores = new ArrayList<>();
        for (SubjectScore subjectScore : subjectScores) {
            scores.add(new SubjectReportDTO(
                    subjectScore.getSubject() == null ? "" : subjectScore.getSubject().getSubjectName(),
                    text(subjectScore.getClassScore()),
                    text(subjectScore.getCalculatedExamScore()),
                    text(subjectScore.getTotalScore()),
                    nullToEmpty(subjectScore.getGrade()),
                    nullToEmpty(subjectScore.getGradeDescriptor())
            ));
        }

        String instructorName = level == null || level.getStaff() == null ? ""
                : (nullToEmpty(level.getStaff().getFirstName()) + " " + nullToEmpty(level.getStaff().getLastName())).trim();

        return new GenerateStudentResult(
                studentId,
                result.getStudent().getFirstName() + " " + result.getStudent().getLastName(),
                level == null ? "" : level.getLevelName(),
                semester == null ? "" : semester.getSemesterName(),
                text(result.getTotalScore()),
                text(result.getAverageScore()),
                nullToEmpty(result.getPosition()),
                semester == null ? "" : nullToEmpty(semester.getAcademicYear()),
                String.valueOf(result.getClassSize()),
                semester == null || semester.getSemesterEndDate() == null
                        ? TO_BE_ANNOUNCED : semester.getSemesterEndDate().toString(),
                term.resumingDate(),
                String.valueOf(semester == null ? 0
                        : attendanceService.getStudentPresentAttendanceCount(studentId, semester.getSemesterID())),
                term.totalAttendance(),
                instructorName,
                LocalDate.now().toString(),
                result.getPromotionTo() == null ? "-" : result.getPromotionTo(),
                scores,
                null
        );
    }

    static String text(Double value) {
        return String.valueOf(value == null ? 0.0 : value);
    }

    static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
