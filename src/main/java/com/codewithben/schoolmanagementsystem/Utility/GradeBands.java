package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Entity.GradeSystem;

import java.util.List;
import java.util.Objects;

/**
 * How a total score maps to a school's grade bands, and what a valid band is (H1).
 *
 * Lookup: the band with the highest lower limit at or below the total. Bands are usually
 * entered as whole numbers (80-100, 70-79, ...), but totals have one decimal place, so a
 * total of 79.5 fell between 70-79 and 80-100 and got no grade at all. Now it gets the 70-79
 * grade (no rounding: 79.5 is not 80). A total above the top band's upper limit gets the top
 * band; a total below the lowest band gets no grade.
 *
 * Bands: lower limit at most the upper limit, both within 0-100, and no two bands overlapping
 * (sharing one end point, as in 70-80 and 80-100, is allowed: 80 then gets the 80-100 grade).
 */
public final class GradeBands {

    public static final double MAX_SCORE = 100.0;

    private GradeBands() {
    }

    /** The band for this total, or null when there is none (no total, no bands, below the lowest). */
    public static GradeSystem bandFor(Double total, List<GradeSystem> bands) {
        if (total == null || bands == null) {
            return null;
        }
        GradeSystem best = null;
        for (GradeSystem band : bands) {
            if (band == null || band.getLowerRange() == null) {
                continue;
            }
            if (band.getLowerRange() <= total && (best == null || band.getLowerRange() > best.getLowerRange())) {
                best = band;
            }
        }
        return best;
    }

    /** Null when the range is acceptable, else the message for the 400. */
    public static String rangeProblem(Double lower, Double upper) {
        if (lower == null || upper == null) {
            return "Lower and upper range are required";
        }
        if (lower < 0 || upper > MAX_SCORE) {
            return "Ranges must be between 0 and " + (int) MAX_SCORE;
        }
        if (lower > upper) {
            return "Lower range can't be above the upper range";
        }
        return null;
    }

    /**
     * A band among {@code existing} that overlaps [lower, upper], ignoring the band with id
     * {@code ignoreId} (the one being updated), or null when none does.
     */
    public static GradeSystem overlapping(double lower, double upper, List<GradeSystem> existing, Integer ignoreId) {
        if (existing == null) {
            return null;
        }
        for (GradeSystem band : existing) {
            if (band == null || band.getLowerRange() == null || band.getUpperRange() == null
                    || Objects.equals(ignoreId, band.getId())) {
                continue;
            }
            if (lower < band.getUpperRange() && band.getLowerRange() < upper) {
                return band;
            }
        }
        return null;
    }
}
