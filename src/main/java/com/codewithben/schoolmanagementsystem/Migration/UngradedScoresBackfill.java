package com.codewithben.schoolmanagementsystem.Migration;

import com.codewithben.schoolmanagementsystem.Entity.GradeSystem;
import com.codewithben.schoolmanagementsystem.Entity.SubjectScore;
import com.codewithben.schoolmanagementsystem.Repository.SubjectScoreRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * One-off data fix, run at startup (H1): subject scores whose total fell between two grade
 * bands (79.5 between 70-79 and 80-100) were saved with no grade. This grades them with the
 * corrected lookup (UtilityClass.gradeBandFor).
 *
 * Idempotent: only scores with no grade and a total are touched. Scores of a school with no
 * grade bands, or with a total below its lowest band, stay ungraded and are looked at again
 * at the next start (one cheap query). Safe to delete once it has run in every environment
 * and the log shows nothing left to grade.
 *
 * Deliberately not @Transactional and wrapped in a catch: saveAll is atomic on its own, and
 * a failure here must be logged, not stop the application from starting.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UngradedScoresBackfill implements ApplicationRunner {

    private final SubjectScoreRepository subjectScoreRepository;

    private final UtilityClass utilityClass;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<SubjectScore> ungraded = subjectScoreRepository.findAllByGradeIsNullAndTotalScoreIsNotNull();
            if (ungraded.isEmpty()) {
                return;
            }

            List<SubjectScore> graded = new ArrayList<>();
            for (SubjectScore score : ungraded) {
                if (score.getStudent() == null || score.getStudent().getInstitution() == null) {
                    continue;
                }
                GradeSystem band = utilityClass.gradeBandFor(
                        score.getTotalScore(), score.getStudent().getInstitution().getInstitutionId());
                if (band != null) {
                    score.setGrade(band.getGrade());
                    score.setGradeDescriptor(band.getGradeDescription());
                    graded.add(score);
                }
            }
            if (!graded.isEmpty()) {
                subjectScoreRepository.saveAll(graded);
            }

            log.info("Ungraded-scores backfill: graded {} of {} score(s) saved without a grade",
                    graded.size(), ungraded.size());
        } catch (Exception e) {
            log.error("Ungraded-scores backfill failed; some subject scores still show no grade", e);
        }
    }
}
