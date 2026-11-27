package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Report.MasterScoreSheetRow;
import com.lowagie.text.pdf.BaseFont;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfGenerationServiceTest {

    @Test
    void scoresPrintWithOneDecimalLikeTheReportCard() {
        assertEquals("79.5", PdfGenerationService.scoreText(79.5));
        assertEquals("80.0", PdfGenerationService.scoreText(80.0));
        assertEquals("645.5", PdfGenerationService.scoreText(645.5));
        assertEquals("-", PdfGenerationService.scoreText(null));
    }

    @Test
    void positionsPrintAsOrdinalsAndUnrankedAsDash() {
        assertEquals("1st", PdfGenerationService.positionText(1));
        assertEquals("22nd", PdfGenerationService.positionText(22));
        assertEquals("13th", PdfGenerationService.positionText(13));
        assertEquals("111th", PdfGenerationService.positionText(111));
        assertEquals("-", PdfGenerationService.positionText(0));
        assertEquals("-", PdfGenerationService.positionText(null));
    }

    @Test
    void nameColumnIsWiderThanScoreColumns() {
        float[] widths = PdfGenerationService.columnWidths(9);
        assertEquals(15, widths.length);
        assertTrue(widths[2] > widths[3] * 2);
    }

    @Test
    void masterSheetRowsAreOrderedByPositionWithUnrankedLast() {
        List<MasterScoreSheetRow> rows = new ArrayList<>(List.of(
                row("Zed", 0), row("Ama", 3), row("abena", null), row("Kofi", 1), row("Esi", 3), row("Yaw", 10)));

        rows.sort(ResultsService.BY_POSITION);

        assertEquals(List.of("Kofi", "Ama", "Esi", "Yaw", "abena", "Zed"),
                rows.stream().map(MasterScoreSheetRow::getStudentName).toList());
    }

    private static MasterScoreSheetRow row(String name, Integer position) {
        return MasterScoreSheetRow.builder().studentName(name).position(position).build();
    }

    @Test
    void masterSheetFontPrintsAkanLettersAndTheCediSign() {
        // DejaVu comes from the jasperreports-fonts dependency.
        BaseFont font = PdfGenerationService.load("DejaVuSansCondensed.ttf", BaseFont.HELVETICA);

        assertTrue(font.charExists('Ɛ'));
        assertTrue(font.charExists('ɔ'));
        assertTrue(font.charExists('₵'));
    }

    @Test
    void missingFontFallsBackToABuiltInOne() {
        BaseFont font = PdfGenerationService.load("NoSuchFont.ttf", BaseFont.HELVETICA);

        assertEquals("Helvetica", font.getPostscriptFontName());
    }
}
