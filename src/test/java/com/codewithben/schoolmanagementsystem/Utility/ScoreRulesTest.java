package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsScoresTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreRulesTest {

    private static StudentsScoresTable row(String ct1, String ct2, String gw, String project, String exam) {
        StudentsScoresTable row = new StudentsScoresTable();
        row.setStudentId("STD100200");
        row.setClassTest1Score(ct1);
        row.setClassTest2Score(ct2);
        row.setGroupWorkScore(gw);
        row.setProjectScore(project);
        row.setExamScore(exam);
        return row;
    }

    @Test
    void derivedScoresAreComputedOnTheServer() {
        StudentsScoresTable row = row("25", "27", "18", "15", "75");
        row.setClassScore("50");            // whatever the client sends is ignored
        row.setCalculatedExamScore("50");

        ScoreRules.ComputedScores computed = ScoreRules.compute(row);

        assertEquals(42.5, computed.classScore());          // (25+27+18+15)/2
        assertEquals(37.5, computed.calculatedExamScore()); // 75/2
        assertEquals(80.0, computed.total());
    }

    @Test
    void decimalMarksAreAcceptedAndTotalsCarryNoFloatingPointNoise() {
        ScoreRules.ComputedScores computed = ScoreRules.compute(row("12.5", "0.1", "0.2", "0", "0.4"));

        assertEquals(6.4, computed.classScore());
        assertEquals(0.2, computed.calculatedExamScore());
        assertEquals(6.6, computed.total());
        assertTrue(ScoreRules.validate(row("12.5", "0.1", "0.2", "0", "0.4")).isEmpty());
    }

    @Test
    void maximumMarksGiveOneHundred() {
        assertTrue(ScoreRules.validate(row("30", "30", "20", "20", "100")).isEmpty());
        assertEquals(100.0, ScoreRules.compute(row("30", "30", "20", "20", "100")).total());
    }

    @Test
    void outOfRangeMissingOrNonNumericMarksAreReportedPerField() {
        List<ScoreRules.FieldError> errors = ScoreRules.validate(row("31", "-1", null, "abc", "100.5"));

        assertEquals(5, errors.size());
        assertEquals("Class test 1 score must be between 0 and 30", errors.get(0).message());
        assertEquals("classTest2Score", errors.get(1).field());
        assertEquals("Group work score is required", errors.get(2).message());
        assertEquals("Project score must be a number", errors.get(3).message());
        assertEquals("Exam score must be between 0 and 100", errors.get(4).message());
        assertEquals("STD100200", errors.get(0).studentId());
    }

    @Test
    void nanAndInfinityAreRefused() {
        assertEquals(2, ScoreRules.validate(row("NaN", "Infinity", "1", "1", "1")).size());
    }

    @Test
    void missingStudentIdIsReported() {
        StudentsScoresTable row = row("1", "1", "1", "1", "1");
        row.setStudentId(" ");

        assertEquals("studentId", ScoreRules.validate(row).getFirst().field());
        assertEquals(1, ScoreRules.validate(null).size());
    }
}
