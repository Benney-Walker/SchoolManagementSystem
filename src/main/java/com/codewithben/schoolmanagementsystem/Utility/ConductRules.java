package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.ConductRatings;
import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductRecord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Validation for one student's conduct record, kept free of Spring so it can be unit-tested.
 *
 * Every rating is required and must be one of {@link ConductRatings}. Spacing, hyphens and
 * letter case are forgiven ("very good", "Very-Good" and "VERY_GOOD" are the same), anything
 * else is reported as a field error instead of failing with a 500. Both remarks are required
 * (the report card prints them) and must fit their database columns.
 */
public final class ConductRules {

    /** Length of the classTeacherRemark and peculiarIssue columns. */
    public static final int MAX_REMARK_LENGTH = 255;

    public record FieldError(String field, String message) {
    }

    private ConductRules() {
    }

    /** "very good" / "Very-Good" / "VERY_GOOD" -> VERY_GOOD; empty for anything else. */
    public static Optional<ConductRatings> parseRating(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalised = value.trim().toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
        for (ConductRatings rating : ConductRatings.values()) {
            if (rating.name().equals(normalised)) {
                return Optional.of(rating);
            }
        }
        return Optional.empty();
    }

    /** The errors in a record, in field order; empty when it is valid. */
    public static List<FieldError> validate(StudentConductRecord record) {
        List<FieldError> errors = new ArrayList<>();
        if (record == null) {
            errors.add(new FieldError("record", "Conduct record is required"));
            return errors;
        }
        if (record.getStudentId() == null || record.getStudentId().isBlank()) {
            errors.add(new FieldError("studentId", "Student ID is required"));
        }
        if (record.getSemesterId() == null || record.getSemesterId().isBlank()) {
            errors.add(new FieldError("semesterId", "Term is required"));
        }
        for (Map.Entry<String, String[]> rating : ratings(record).entrySet()) {
            String label = rating.getValue()[0];
            String value = rating.getValue()[1];
            if (value == null || value.isBlank()) {
                errors.add(new FieldError(rating.getKey(), label + " rating is required"));
            } else if (parseRating(value).isEmpty()) {
                errors.add(new FieldError(rating.getKey(),
                        label + " rating must be Excellent, Very good, Good, Fair or Not to expectation"));
            }
        }
        checkRemark(errors, "conductRemark", "Teacher's remark", record.getConductRemark());
        checkRemark(errors, "peculiarIssue", "Peculiar issue", record.getPeculiarIssue());
        return errors;
    }

    private static void checkRemark(List<FieldError> errors, String field, String label, String value) {
        if (value == null || value.isBlank()) {
            errors.add(new FieldError(field, label + " is required"));
        } else if (value.trim().length() > MAX_REMARK_LENGTH) {
            errors.add(new FieldError(field, label + " must be " + MAX_REMARK_LENGTH + " characters or fewer"));
        }
    }

    /** field -> {label, value}, in the order the desktop shows them. */
    private static Map<String, String[]> ratings(StudentConductRecord record) {
        Map<String, String[]> ratings = new LinkedHashMap<>();
        ratings.put("regular", new String[]{"Regular", record.getRegular()});
        ratings.put("punctual", new String[]{"Punctual", record.getPunctual()});
        ratings.put("physicalAppearance", new String[]{"Physical appearance", record.getPhysicalAppearance()});
        ratings.put("social", new String[]{"Social", record.getSocial()});
        ratings.put("emotional", new String[]{"Emotional", record.getEmotional()});
        ratings.put("cognitiveSkills", new String[]{"Cognitive skills", record.getCognitiveSkills()});
        return ratings;
    }
}
