package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Report.MasterScoreSheet;
import com.codewithben.schoolmanagementsystem.DTO.Report.MasterScoreSheetRow;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Master score sheet PDF (OpenPDF). The other reports are Jasper templates; this one is built
 * in code because its columns depend on the class's subjects.
 *
 * Layout: A4 landscape always (A3 was used for more than 10 subjects, which most school
 * printers can't print; the font shrinks instead). School header from the school's own
 * branding, the header row repeats on every page, the name column is wider than the score
 * columns, and every score prints with one decimal place, the same as the report card.
 *
 * Font: DejaVu Sans Condensed, embedded, the same family as the Jasper reports (from the
 * jasperreports-fonts jar). The built-in PDF fonts (Times, Helvetica) cover Latin-1 only, so
 * Akan letters in names (Ɛ ɛ Ɔ ɔ) and the cedi sign (₵) used to print blank.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PdfGenerationService {

    private static final Color HEADER_SHADE = new Color(235, 235, 235);

    private static final String FONT_DIR = "net/sf/jasperreports/fonts/dejavu/";
    private static final String SYSTEM_FONT_DIR = "/usr/share/fonts/truetype/dejavu/";

    /* Loaded once; BaseFont is thread-safe to share between documents. */
    private static final class Fonts {
        static final BaseFont REGULAR = load("DejaVuSansCondensed.ttf", BaseFont.HELVETICA);
        static final BaseFont BOLD = load("DejaVuSansCondensed-Bold.ttf", BaseFont.HELVETICA_BOLD);
        static final BaseFont ITALIC = load("DejaVuSansCondensed-Oblique.ttf", BaseFont.HELVETICA_OBLIQUE);
    }

    private final BrandingAssetService brandingAssetService;

    public byte[] generateMasterSheetScore(MasterScoreSheet masterScoreSheet, Institution institution) throws DocumentException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        List<String> subjects = masterScoreSheet.getSubjects() == null ? List.of() : masterScoreSheet.getSubjects();
        boolean crowded = subjects.size() > 10;

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font schoolFont = new Font(Fonts.BOLD, 15);
        Font titleFont = new Font(Fonts.BOLD, 12);
        Font infoFont = new Font(Fonts.REGULAR, 9);
        Font mottoFont = new Font(Fonts.ITALIC, 9);
        Font headerFont = new Font(Fonts.BOLD, crowded ? 6f : 8);
        Font bodyFont = new Font(Fonts.REGULAR, crowded ? 7 : 8);

        // =========================
        // SCHOOL HEADER
        // =========================
        document.add(schoolHeader(brandingAssetService.brandingFor(institution), schoolFont, mottoFont, infoFont));

        Paragraph title = new Paragraph("MASTER SCORE SHEET", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(6);
        document.add(title);

        // =========================
        // CLASS + TERM + ACADEMIC YEAR
        // =========================
        PdfPTable infoTable = new PdfPTable(3);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(4);
        infoTable.setSpacingAfter(6);
        infoTable.addCell(infoCell("CLASS: " + textOrBlank(masterScoreSheet.getClassName()), infoFont, Element.ALIGN_LEFT));
        infoTable.addCell(infoCell("TERM: " + textOrBlank(masterScoreSheet.getSemesterName()), infoFont, Element.ALIGN_CENTER));
        infoTable.addCell(infoCell("ACADEMIC YEAR: " + textOrBlank(masterScoreSheet.getAcademicYear()), infoFont, Element.ALIGN_RIGHT));
        document.add(infoTable);

        // =========================
        // MASTER TABLE
        // =========================
        PdfPTable table = new PdfPTable(columnWidths(subjects.size()));
        table.setWidthPercentage(100);
        table.setHeaderRows(1);   // repeat the column names on every page

        addHeaderCell(table, "No.", headerFont);
        addHeaderCell(table, "Student Id", headerFont);
        addHeaderCell(table, "Student Name", headerFont);
        for (String subject : subjects) {
            addHeaderCell(table, subject, headerFont);
        }
        addHeaderCell(table, "Total", headerFont);
        addHeaderCell(table, "Average", headerFont);
        addHeaderCell(table, "Position", headerFont);

        int count = 1;
        List<MasterScoreSheetRow> rows = masterScoreSheet.getRecords() == null ? List.of() : masterScoreSheet.getRecords();
        for (MasterScoreSheetRow row : rows) {
            addBodyCell(table, String.valueOf(count++), bodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, textOrBlank(row.getStudentId()), bodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, textOrBlank(row.getStudentName()), bodyFont, Element.ALIGN_LEFT);
            for (String subject : subjects) {
                Double score = row.getSubjectScores() == null ? null : row.getSubjectScores().get(subject);
                addBodyCell(table, scoreText(score), bodyFont, Element.ALIGN_CENTER);
            }
            addBodyCell(table, scoreText(row.getStudentTotalScore()), bodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, scoreText(row.getStudentAverageScore()), bodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, positionText(row.getPosition()), bodyFont, Element.ALIGN_CENTER);
        }

        document.add(table);

        Paragraph footer = new Paragraph("Date Of Report: " + LocalDate.now(), infoFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(8);
        document.add(footer);

        document.close();

        return outputStream.toByteArray();
    }

    /*================================================
                        HELPERS
     =================================================*/

    /*
     * DejaVu from the jasperreports-fonts jar, else from the system (the Docker image installs
     * fonts-dejavu), else a built-in PDF font with a warning: the sheet still prints, but
     * letters outside Latin-1 come out blank.
     */
    static BaseFont load(String file, String builtInFallback) {
        try {
            byte[] bytes = null;
            try (InputStream in = PdfGenerationService.class.getClassLoader().getResourceAsStream(FONT_DIR + file)) {
                if (in != null) {
                    bytes = in.readAllBytes();
                }
            }
            if (bytes == null && Files.isReadable(Path.of(SYSTEM_FONT_DIR, file))) {
                bytes = Files.readAllBytes(Path.of(SYSTEM_FONT_DIR, file));
            }
            if (bytes != null) {
                return BaseFont.createFont(file, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
            }
            log.warn("Font {} not found; the master score sheet will use {} and can't print letters like Ɛ or Ɔ",
                    file, builtInFallback);
        } catch (IOException | DocumentException e) {
            log.warn("Could not load font {}; the master score sheet will use {}", file, builtInFallback, e);
        }
        try {
            return BaseFont.createFont(builtInFallback, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("Cannot load the built-in PDF font " + builtInFallback, e);
        }
    }

    /*
     * Relative widths: No. 3, Student Id 6, Student Name 15, each subject 8, Total 6,
     * Average 5.5, Position 6.5. With equal widths the names wrapped onto three lines; subject
     * columns are wide enough for "Mathematics" in the header with 13 subjects.
     */
    static float[] columnWidths(int subjectCount) {
        float[] widths = new float[subjectCount + 6];
        widths[0] = 3f;
        widths[1] = 6f;
        widths[2] = 15f;
        for (int i = 0; i < subjectCount; i++) {
            widths[3 + i] = 8f;
        }
        widths[subjectCount + 3] = 6f;
        widths[subjectCount + 4] = 5.5f;
        widths[subjectCount + 5] = 6.5f;
        return widths;
    }

    /* One decimal place, like the report card ("%.0f" printed 79.5 as 80). No score -> "-". */
    static String scoreText(Double score) {
        return score == null ? "-" : String.format(Locale.ROOT, "%.1f", score);
    }

    /* 1 -> "1st", 22 -> "22nd"; 0 or none means not ranked yet -> "-". */
    static String positionText(Integer position) {
        if (position == null || position <= 0) {
            return "-";
        }
        int lastTwo = position % 100;
        String suffix = (lastTwo >= 11 && lastTwo <= 13) ? "th" : switch (position % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
        return position + suffix;
    }

    private static String textOrBlank(String value) {
        return value == null ? "" : value;
    }

    /* Logo on the left (when uploaded), school name, motto and address centred. */
    private PdfPTable schoolHeader(BrandingAssetService.ReportBranding branding, Font schoolFont, Font mottoFont, Font infoFont)
            throws DocumentException {

        PdfPTable header = new PdfPTable(new float[]{1f, 6f, 1f});
        header.setWidthPercentage(100);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (branding.logo() != null) {
            try {
                Image logo = Image.getInstance(branding.logo(), (Color) null);
                logo.scaleToFit(48, 48);
                logoCell.addElement(logo);
            } catch (Exception e) {
                // The sheet is still useful without the logo.
                log.warn("Could not place the school logo on the master score sheet", e);
            }
        }
        header.addCell(logoCell);

        PdfPCell nameCell = new PdfPCell();
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        addCentred(nameCell, textOrBlank(branding.schoolName()).toUpperCase(Locale.ROOT), schoolFont);
        if (branding.motto() != null) {
            addCentred(nameCell, branding.motto(), mottoFont);
        }
        if (branding.address() != null) {
            addCentred(nameCell, branding.address(), infoFont);
        }
        header.addCell(nameCell);

        PdfPCell spacer = new PdfPCell();
        spacer.setBorder(Rectangle.NO_BORDER);
        header.addCell(spacer);

        return header;
    }

    private static void addCentred(PdfPCell cell, String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(paragraph);
    }

    private static PdfPCell infoCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private static void addHeaderCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(HEADER_SHADE);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private static void addBodyCell(PdfPTable table, String value, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2);
        table.addCell(cell);
    }
}
