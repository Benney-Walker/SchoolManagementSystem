package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Entity.GradeSystem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GradeBandsTest {

    private static GradeSystem band(int id, double lower, double upper, String grade) {
        GradeSystem band = new GradeSystem();
        band.setId(id);
        band.setLowerRange(lower);
        band.setUpperRange(upper);
        band.setGrade(grade);
        band.setGradeDescription(grade + "_description");
        return band;
    }

    // Typical whole-number bands, deliberately out of order.
    private static final List<GradeSystem> BANDS = List.of(
            band(3, 60, 69, "C"), band(1, 80, 100, "A"), band(4, 0, 59, "F"), band(2, 70, 79, "B"));

    @Test
    void totalsBetweenWholeNumberBandsGetTheLowerBand() {
        assertEquals("B", GradeBands.bandFor(79.5, BANDS).getGrade());   // used to get no grade
        assertEquals("C", GradeBands.bandFor(69.9, BANDS).getGrade());
        assertEquals("F", GradeBands.bandFor(59.5, BANDS).getGrade());
    }

    @Test
    void totalsInsideBandsAreUnchanged() {
        assertEquals("A", GradeBands.bandFor(80.0, BANDS).getGrade());
        assertEquals("A", GradeBands.bandFor(100.0, BANDS).getGrade());
        assertEquals("B", GradeBands.bandFor(70.0, BANDS).getGrade());
        assertEquals("F", GradeBands.bandFor(0.0, BANDS).getGrade());
    }

    @Test
    void descriptionWithUnderscoreSurvives() {
        assertEquals("B_description", GradeBands.bandFor(75.0, BANDS).getGradeDescription());
    }

    @Test
    void noGradeBelowTheLowestBandOrWithoutData() {
        assertNull(GradeBands.bandFor(10.0, List.of(band(1, 40, 100, "P"))));
        assertNull(GradeBands.bandFor(null, BANDS));
        assertNull(GradeBands.bandFor(50.0, List.of()));
        assertNull(GradeBands.bandFor(50.0, null));
    }

    @Test
    void sharedEndPointGoesToTheHigherBand() {
        List<GradeSystem> touching = List.of(band(1, 70, 80, "B"), band(2, 80, 100, "A"));
        assertEquals("A", GradeBands.bandFor(80.0, touching).getGrade());
    }

    @Test
    void rangeRules() {
        assertNull(GradeBands.rangeProblem(70.0, 79.0));
        assertNull(GradeBands.rangeProblem(80.0, 80.0));
        assertNotNull(GradeBands.rangeProblem(80.0, 70.0));
        assertNotNull(GradeBands.rangeProblem(-1.0, 10.0));
        assertNotNull(GradeBands.rangeProblem(90.0, 101.0));
        assertNotNull(GradeBands.rangeProblem(null, 10.0));
    }

    @Test
    void overlapRules() {
        assertNull(GradeBands.overlapping(81, 100, List.of(band(2, 70, 80, "B")), null));   // gap
        assertNull(GradeBands.overlapping(80, 100, List.of(band(2, 70, 80, "B")), null));   // shared end point
        assertEquals("B", GradeBands.overlapping(75, 100, List.of(band(2, 70, 80, "B")), null).getGrade());
        assertEquals("B", GradeBands.overlapping(72, 78, List.of(band(2, 70, 80, "B")), null).getGrade());
        assertNull(GradeBands.overlapping(60, 85, List.of(band(2, 70, 80, "B")), 2));       // the band itself
    }
}
