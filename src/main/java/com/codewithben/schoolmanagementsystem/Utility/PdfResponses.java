package com.codewithben.schoolmanagementsystem.Utility;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.text.Normalizer;

/**
 * The one way report endpoints send a PDF.
 *
 * - Content-Disposition with a readable file name ("Basic-6-First-Term-2026-2027-master-sheet.pdf"),
 *   so a browser (the guardian portal) saves it under that name instead of "download". It is
 *   "inline" so the browser can still show it.
 * - Cache-Control: no-store, because report cards and expense reports carry personal and
 *   financial data that browsers and proxies must not keep.
 */
public final class PdfResponses {

    private static final int MAX_NAME_LENGTH = 120;

    private PdfResponses() {
    }

    public static ResponseEntity<byte[]> pdf(byte[] content, String... nameParts) {
        String fileName = fileName(nameParts);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(content.length)
                .cacheControl(CacheControl.noStore())
                // fileName() is plain ASCII, so a simple filename="..." works in every browser.
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(fileName)
                        .build()
                        .toString())
                .body(content);
    }

    /*
     * "Basic 6", "First Term", "2026/2027", "report card" -> "Basic-6-First-Term-2026-2027-report-card.pdf".
     * Accents are dropped (Ɛ/Ɔ become E/O) and anything but letters, digits and hyphens becomes a hyphen, so the
     * name is safe on every operating system. Blank parts are skipped.
     */
    public static String fileName(String... nameParts) {
        StringBuilder joined = new StringBuilder();
        if (nameParts != null) {
            for (String part : nameParts) {
                if (part != null && !part.isBlank()) {
                    joined.append(joined.isEmpty() ? "" : "-").append(part.trim());
                }
            }
        }
        // Akan letters have no accent to strip, so map them to their nearest Latin letter.
        String latin = joined.toString()
                .replace('Ɛ', 'E').replace('ɛ', 'e').replace('Ɔ', 'O').replace('ɔ', 'o');
        String ascii = Normalizer.normalize(latin, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
        String safe = ascii.replaceAll("[^A-Za-z0-9-]+", "-").replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
        if (safe.length() > MAX_NAME_LENGTH) {
            safe = safe.substring(0, MAX_NAME_LENGTH).replaceAll("-$", "");
        }
        return (safe.isEmpty() ? "report" : safe) + ".pdf";
    }
}
