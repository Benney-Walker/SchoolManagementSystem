package com.codewithben.schoolmanagementsystem.Utility;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class PdfResponsesTest {

    @Test
    void fileNameIsReadableAndSafe() {
        assertEquals("Basic-6-First-Term-2026-2027-master-sheet.pdf",
                PdfResponses.fileName("Basic 6", "First Term", "2026/2027", "master sheet"));
    }

    @Test
    void akanLettersAndAccentsBecomeLatinAndBlankPartsAreSkipped() {
        assertEquals("Esi-Owusu-Ansah-Adwoa-report-card.pdf",
                PdfResponses.fileName("Ɛsi", "Ɔwusu-Ansah", "Ádwoa", null, " ", "report card"));
    }

    @Test
    void pathsCannotEscapeAndEmptyNamesFallBack() {
        assertEquals("etc-passwd.pdf", PdfResponses.fileName("../../etc/passwd"));
        assertEquals("report.pdf", PdfResponses.fileName());
        assertTrue(PdfResponses.fileName("x".repeat(300)).length() <= 124);
    }

    @Test
    void pdfIsNotCachedAndHasAFileName() {
        ResponseEntity<byte[]> response = PdfResponses.pdf(new byte[]{1, 2, 3}, "Basic 6", "report cards");

        assertEquals("no-store", response.getHeaders().getCacheControl());
        String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(disposition);
        assertTrue(disposition.startsWith("inline"));
        assertTrue(disposition.contains("Basic-6-report-cards.pdf"));
        assertEquals(3, response.getHeaders().getContentLength());
    }
}
