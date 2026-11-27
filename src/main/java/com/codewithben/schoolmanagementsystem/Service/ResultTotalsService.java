package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Results;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Entity.SubjectScore;
import com.codewithben.schoolmanagementsystem.Entity.Subjects;
import com.codewithben.schoolmanagementsystem.Repository.ResultsRepository;
import com.codewithben.schoolmanagementsystem.Repository.SubjectScoreRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recomputes a student's term totals, the "ready" flag and the class positions.
 *
 * Runs inside the caller's transaction, right after the scores are saved. It used to run in a
 * RabbitMQ consumer that was published before the save committed, so the consumer could read
 * the old scores (or not find newly created results at all) and the totals, ready flag and
 * positions silently stayed wrong.
 *
 * Totals are always rebuilt from the saved subject scores (never adjusted by deltas), so
 * running it twice gives the same result.
 */
@Service
@RequiredArgsConstructor
public class ResultTotalsService {

    private final ResultsRepository resultsRepository;

    private final SubjectScoreRepository subjectScoreRepository;

    private final UtilityClass utilityClass;

    /**
     * @param affectedResults the results whose scores just changed (already flushed)
     * @param level           the class the scores were entered for
     * @param semesterId      the term
     * @param updatedBy       the staff member who saved the scores
     */
    public void recalculate(Collection<Results> affectedResults, Level level, String semesterId, Staffs updatedBy) {
        List<Results> results = affectedResults.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (results.isEmpty()) {
            return;
        }

        // One query for all subject scores of the affected results. Reading the scores
        // through a query (not result.getSubjectScores()) also sees scores created in this
        // transaction, which the in-memory collection does not contain.
        List<Long> resultIds = results.stream().map(Results::getResultId).toList();
        Map<Long, List<SubjectScore>> scoresByResult = new HashMap<>();
        for (SubjectScore score : subjectScoreRepository.findAllByResults_ResultIdIn(resultIds)) {
            scoresByResult.computeIfAbsent(score.getResults().getResultId(), id -> new ArrayList<>()).add(score);
        }

        Set<String> classSubjectIds = level.getSubjects() == null ? Set.of()
                : level.getSubjects().stream().map(Subjects::getSubjectId).collect(Collectors.toSet());
        int classSize = level.getStudents() == null ? 0 : utilityClass.getActiveStudents(level.getStudents()).size();

        for (Results result : results) {
            List<SubjectScore> scores = scoresByResult.getOrDefault(result.getResultId(), List.of());
            applyTotals(result, scores, classSize, updatedBy);
            result.setReady(isReady(scores, classSubjectIds));
        }
        resultsRepository.saveAll(results);

        rankClass(level.getLevelID(), semesterId);
    }

    /** Total and average from the subject scores, rounded to one decimal place. */
    static void applyTotals(Results result, List<SubjectScore> scores, int classSize, Staffs updatedBy) {
        double total = 0.0;
        int counted = 0;
        for (SubjectScore score : scores) {
            if (score.getTotalScore() != null) {
                total += score.getTotalScore();
                counted++;
            }
        }
        result.setTotalScore(roundOneDecimal(total));
        result.setAverageScore(counted == 0 ? 0.0 : roundOneDecimal(total / counted));
        result.setClassSize(classSize);
        if (updatedBy != null) {
            result.setUpdatedBy(updatedBy);
        }
    }

    /**
     * Ready when the student has a score in every subject the class currently has. Scores for
     * subjects no longer on the class don't count, and the class must have at least one subject.
     */
    static boolean isReady(List<SubjectScore> scores, Set<String> classSubjectIds) {
        if (classSubjectIds.isEmpty()) {
            return false;
        }
        Set<String> scored = new HashSet<>();
        for (SubjectScore score : scores) {
            if (score.getSubject() != null && classSubjectIds.contains(score.getSubject().getSubjectId())) {
                scored.add(score.getSubject().getSubjectId());
            }
        }
        return scored.size() == classSubjectIds.size();
    }

    /** Re-ranks every result in the class for the term by total score (ties share a position). */
    public void rankClass(String levelId, String semesterId) {
        List<Results> classResults = new ArrayList<>(
                resultsRepository.findByLevel_LevelIDAndSemester_SemesterID(levelId, semesterId));
        if (classResults.isEmpty()) {
            return;
        }
        // Results created by attendance may not have a total yet: rank them as 0 instead of
        // failing the whole ranking with a NullPointerException.
        classResults.sort(Comparator.comparingDouble(ResultTotalsService::totalOrZero).reversed());
        assignPositions(classResults);
        resultsRepository.saveAll(classResults);
    }

    static void assignPositions(List<Results> sortedResults) {
        int currentRank = 0;
        Double lastScore = null;
        for (int i = 0; i < sortedResults.size(); i++) {
            Results result = sortedResults.get(i);
            double score = totalOrZero(result);
            if (lastScore == null || score != lastScore) {
                currentRank = i + 1;
                lastScore = score;
            }
            result.setPosition(ordinal(currentRank));
        }
    }

    static double totalOrZero(Results result) {
        return result.getTotalScore() == null ? 0.0 : result.getTotalScore();
    }

    static double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    static String ordinal(int number) {
        int lastTwo = number % 100;
        if (lastTwo >= 11 && lastTwo <= 13) {
            return number + "th";
        }
        return switch (number % 10) {
            case 1 -> number + "st";
            case 2 -> number + "nd";
            case 3 -> number + "rd";
            default -> number + "th";
        };
    }
}
