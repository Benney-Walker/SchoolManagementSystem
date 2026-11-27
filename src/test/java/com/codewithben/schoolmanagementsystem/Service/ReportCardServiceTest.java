package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Results;
import com.codewithben.schoolmanagementsystem.Entity.SubjectScore;
import com.codewithben.schoolmanagementsystem.Entity.Subjects;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportCardServiceTest {

    private static Subjects subject(String id, String name) {
        Subjects subject = new Subjects();
        subject.setSubjectId(id);
        subject.setSubjectName(name);
        return subject;
    }

    private static SubjectScore score(Subjects subject, Double total) {
        SubjectScore score = new SubjectScore();
        score.setSubject(subject);
        score.setTotalScore(total);
        return score;
    }

    private static Results result(List<Subjects> classSubjects, List<SubjectScore> scores) {
        Level level = new Level();
        level.setSubjects(classSubjects);
        Results result = new Results();
        result.setLevel(level);
        result.setSubjectScores(new ArrayList<>(scores));
        return result;
    }

    @Test
    void missingSubjectsAreListedAlphabetically() {
        Subjects maths = subject("M", "Maths");
        Subjects english = subject("E", "English");
        Subjects science = subject("S", "Science");

        Results result = result(List.of(maths, science, english), List.of(score(maths, 70.0)));

        assertEquals(List.of("English", "Science"), ReportCardService.missingSubjects(result));
    }

    @Test
    void aScoreWithoutATotalStillCountsAsMissing() {
        Subjects maths = subject("M", "Maths");

        assertEquals(List.of("Maths"), ReportCardService.missingSubjects(result(List.of(maths), List.of(score(maths, null)))));
    }

    @Test
    void completeResultHasNothingMissing() {
        Subjects maths = subject("M", "Maths");
        Subjects removed = subject("X", "Old subject");

        // A score for a subject the class no longer has doesn't matter.
        assertTrue(ReportCardService.missingSubjects(
                result(List.of(maths), List.of(score(maths, 70.0), score(removed, 50.0)))).isEmpty());
    }

    @Test
    void classWithoutSubjectsBlocksNothing() {
        assertTrue(ReportCardService.missingSubjects(result(List.of(), List.of())).isEmpty());
    }
}
