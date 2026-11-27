package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Entity.Results;
import com.codewithben.schoolmanagementsystem.Entity.SubjectScore;
import com.codewithben.schoolmanagementsystem.Entity.Subjects;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResultTotalsServiceTest {

    private static SubjectScore score(String subjectId, Double total) {
        Subjects subject = new Subjects();
        subject.setSubjectId(subjectId);
        SubjectScore score = new SubjectScore();
        score.setSubject(subject);
        score.setTotalScore(total);
        return score;
    }

    private static Results result(Double total) {
        Results result = new Results();
        result.setTotalScore(total);
        return result;
    }

    @Test
    void totalAndAverageAreRoundedToOneDecimal() {
        Results result = new Results();

        ResultTotalsService.applyTotals(result, List.of(score("A", 0.1), score("B", 0.2), score("C", 70.0)), 40, null);

        assertEquals(70.3, result.getTotalScore());
        assertEquals(23.4, result.getAverageScore());
        assertEquals(40, result.getClassSize());
    }

    @Test
    void noScoresGiveZeroNotNull() {
        Results result = new Results();

        ResultTotalsService.applyTotals(result, List.of(), 30, null);

        assertEquals(0.0, result.getTotalScore());
        assertEquals(0.0, result.getAverageScore());
    }

    @Test
    void readyOnlyWhenEveryCurrentSubjectHasAScore() {
        Set<String> classSubjects = Set.of("MATH", "ENG");

        assertTrue(ResultTotalsService.isReady(List.of(score("MATH", 50.0), score("ENG", 60.0)), classSubjects));
        assertFalse(ResultTotalsService.isReady(List.of(score("MATH", 50.0)), classSubjects));
        // A score for a subject the class no longer has doesn't stand in for a missing one.
        assertFalse(ResultTotalsService.isReady(List.of(score("MATH", 50.0), score("OLD", 60.0)), classSubjects));
        // Extra scores for removed subjects don't stop a complete result being ready.
        assertTrue(ResultTotalsService.isReady(
                List.of(score("MATH", 50.0), score("ENG", 60.0), score("OLD", 10.0)), classSubjects));
        assertFalse(ResultTotalsService.isReady(List.of(score("MATH", 50.0)), Set.of()));
    }

    @Test
    void tiesSharePositionAndMissingTotalsRankLastWithoutFailing() {
        Results first = result(300.0);
        Results tiedA = result(250.0);
        Results tiedB = result(250.0);
        Results attendanceOnly = result(null);
        List<Results> sorted = new ArrayList<>(List.of(first, tiedA, tiedB, attendanceOnly));
        sorted.sort(java.util.Comparator.comparingDouble(ResultTotalsService::totalOrZero).reversed());

        ResultTotalsService.assignPositions(sorted);

        assertEquals("1st", first.getPosition());
        assertEquals("2nd", tiedA.getPosition());
        assertEquals("2nd", tiedB.getPosition());
        assertEquals("4th", attendanceOnly.getPosition());
    }

    @Test
    void ordinalsIncludingTheTeens() {
        assertEquals("1st", ResultTotalsService.ordinal(1));
        assertEquals("11th", ResultTotalsService.ordinal(11));
        assertEquals("12th", ResultTotalsService.ordinal(12));
        assertEquals("22nd", ResultTotalsService.ordinal(22));
        assertEquals("103rd", ResultTotalsService.ordinal(103));
        assertEquals("111th", ResultTotalsService.ordinal(111));
    }
}
