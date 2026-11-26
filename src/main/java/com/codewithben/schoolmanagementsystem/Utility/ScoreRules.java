package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsScoresTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates one student's score row and computes the derived scores on the server.
 *
 * Raw marks and their maximums: class test 1 and 2 (30 each), group work and project (20 each),
 * exam (100). Derived, never taken from the client:
 *   class score  = (class test 1 + class test 2 + group work + project) / 100 * 50
 *   exam score   = exam / 100 * 50
 *   total        = class score + exam score
 * Each is rounded to one decimal place, which is also how the desktop app displays them.
 *
 * Kept free of Spring so it can be unit-tested. Bean Validation on the request DTO does not run
 * for a List request body, so this is where score input is actually checked.
 */
public final class ScoreRules {

    public record ComputedScores(
            double classTest1,
            double classTest2,
            double groupWork,
            double project,
            double exam,
            double classScore,
            double calculatedExamScore,
            double total
    ) {
    }

    public record FieldError(String studentId, String field, String message) {
    }

    private ScoreRules() {
    }

    /** The errors in a row, in field order; empty when the row is valid. */
    public static List<FieldError> validate(StudentsScoresTable row) {
        List<FieldError> errors = new ArrayList<>();
        String studentId = row == null ? null : row.getStudentId();
        if (row == null || studentId == null || studentId.isBlank()) {
            errors.add(new FieldError(studentId, "studentId", "Student Id is required"));
            if (row == null) {
                return errors;
            }
        }
        for (Map.Entry<String, Mark> entry : marks(row).entrySet()) {
            String message = entry.getValue().problem();
            if (message != null) {
                errors.add(new FieldError(studentId, entry.getKey(), message));
            }
        }
        return errors;
    }

    /** Computes the stored scores of a row that passed {@link #validate}. */
    public static ComputedScores compute(StudentsScoresTable row) {
        Map<String, Mark> marks = marks(row);
        double classTest1 = marks.get("classTest1Score").value();
        double classTest2 = marks.get("classTest2Score").value();
        double groupWork = marks.get("groupWorkScore").value();
        double project = marks.get("projectScore").value();
        double exam = marks.get("examScore").value();

        double classScore = round((classTest1 + classTest2 + groupWork + project) / 100.0 * 50.0);
        double calculatedExamScore = round(exam / 100.0 * 50.0);
        return new ComputedScores(
                classTest1, classTest2, groupWork, project, exam,
                classScore, calculatedExamScore, round(classScore + calculatedExamScore)
        );
    }

    public static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static Map<String, Mark> marks(StudentsScoresTable row) {
        Map<String, Mark> marks = new LinkedHashMap<>();
        marks.put("classTest1Score", new Mark("Class test 1", row.getClassTest1Score(), 30));
        marks.put("classTest2Score", new Mark("Class test 2", row.getClassTest2Score(), 30));
        marks.put("groupWorkScore", new Mark("Group work", row.getGroupWorkScore(), 20));
        marks.put("projectScore", new Mark("Project", row.getProjectScore(), 20));
        marks.put("examScore", new Mark("Exam", row.getExamScore(), 100));
        return marks;
    }

    private record Mark(String label, String raw, double max) {

        String problem() {
            if (raw == null || raw.isBlank()) {
                return label + " score is required";
            }
            double value;
            try {
                value = Double.parseDouble(raw.trim());
            } catch (NumberFormatException e) {
                return label + " score must be a number";
            }
            if (Double.isNaN(value) || Double.isInfinite(value) || value < 0 || value > max) {
                return label + " score must be between 0 and " + (int) max;
            }
            return null;
        }

        double value() {
            return round(Double.parseDouble(raw.trim()));
        }
    }
}
