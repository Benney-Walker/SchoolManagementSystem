package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.ConductRatings;
import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConductRulesTest {

    private static StudentConductRecord record() {
        StudentConductRecord record = new StudentConductRecord();
        record.setStudentId("STD100200");
        record.setSemesterId("SE100100");
        record.setRegular("EXCELLENT");
        record.setPunctual("VERY_GOOD");
        record.setPhysicalAppearance("GOOD");
        record.setSocial("FAIR");
        record.setEmotional("NOT_TO_EXPECTATION");
        record.setCognitiveSkills("GOOD");
        record.setConductRemark("Hardworking");
        record.setPeculiarIssue("None");
        return record;
    }

    @Test
    void completeRecordIsValid() {
        assertTrue(ConductRules.validate(record()).isEmpty());
    }

    @Test
    void ratingSpellingAndCaseAreForgiven() {
        assertEquals(Optional.of(ConductRatings.VERY_GOOD), ConductRules.parseRating("very good"));
        assertEquals(Optional.of(ConductRatings.VERY_GOOD), ConductRules.parseRating(" Very-Good "));
        assertEquals(Optional.of(ConductRatings.NOT_TO_EXPECTATION), ConductRules.parseRating("not to expectation"));
        assertEquals(Optional.empty(), ConductRules.parseRating("great"));
        assertEquals(Optional.empty(), ConductRules.parseRating(""));
        assertEquals(Optional.empty(), ConductRules.parseRating(null));
    }

    @Test
    void missingOrUnknownRatingsAreReportedPerField() {
        StudentConductRecord record = record();
        record.setRegular("");
        record.setSocial("great");

        List<ConductRules.FieldError> errors = ConductRules.validate(record);

        assertEquals(2, errors.size());
        assertEquals("regular", errors.get(0).field());
        assertEquals("Regular rating is required", errors.get(0).message());
        assertEquals("social", errors.get(1).field());
    }

    @Test
    void remarksAreRequiredAndMustFitTheColumn() {
        StudentConductRecord record = record();
        record.setConductRemark(" ");
        record.setPeculiarIssue("x".repeat(ConductRules.MAX_REMARK_LENGTH + 1));

        List<ConductRules.FieldError> errors = ConductRules.validate(record);

        assertEquals(2, errors.size());
        assertEquals("Teacher's remark is required", errors.get(0).message());
        assertEquals("Peculiar issue must be 255 characters or fewer", errors.get(1).message());
    }

    @Test
    void remarkOfExactlyTheLimitIsAccepted() {
        StudentConductRecord record = record();
        record.setConductRemark("x".repeat(ConductRules.MAX_REMARK_LENGTH));

        assertTrue(ConductRules.validate(record).isEmpty());
    }

    @Test
    void idsAreRequired() {
        StudentConductRecord record = record();
        record.setStudentId(null);
        record.setSemesterId(" ");

        assertEquals(2, ConductRules.validate(record).size());
        assertEquals(1, ConductRules.validate(null).size());
    }
}
