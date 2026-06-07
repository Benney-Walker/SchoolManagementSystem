package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Report.MasterScoreSheet;
import com.codewithben.schoolmanagementsystem.DTO.Report.MasterScoreSheetRow;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@AllArgsConstructor
@Service
public class PdfGenerationService {

    public byte[] generateMasterSheetScore(MasterScoreSheet masterScoreSheet) throws DocumentException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int subjectCount = masterScoreSheet.getSubjects().size();

        Rectangle pageSize = subjectCount > 10 ? PageSize.A3.rotate() : PageSize.A4.rotate();

        Document document = new Document(
                pageSize,
                20,
                20,
                20,
                20
        );

        PdfWriter.getInstance(document, outputStream);

        document.open();

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.TIMES_BOLD,
                        14
                );

        Font headerFont =
                FontFactory.getFont(
                        FontFactory.TIMES_BOLD,
                        9
                );

        Font bodyFont =
                FontFactory.getFont(
                        FontFactory.TIMES,
                        8
                );

        // =========================
        // TITLE
        // =========================

        Paragraph title =
                new Paragraph(
                        "MASTER SCORE SHEET",
                        titleFont
                );

        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);

        Paragraph semester =
                new Paragraph(
                        masterScoreSheet.getSemesterName(),
                        bodyFont
                );

        semester.setAlignment(Element.ALIGN_CENTER);

        document.add(semester);

        document.add(new Paragraph(" "));

        // =========================
        // CLASS + ACADEMIC YEAR
        // =========================

        PdfPTable infoTable =
                new PdfPTable(2);

        infoTable.setWidthPercentage(100);

        PdfPCell classCell =
                new PdfPCell(
                        new Phrase(
                                "CLASS: "
                                        + masterScoreSheet.getClassName(),
                                bodyFont
                        )
                );

        classCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell yearCell =
                new PdfPCell(
                        new Phrase(
                                "ACADEMIC YEAR: "
                                        + masterScoreSheet.getAcademicYear(),
                                bodyFont
                        )
                );

        yearCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        yearCell.setBorder(Rectangle.NO_BORDER);

        infoTable.addCell(classCell);
        infoTable.addCell(yearCell);

        document.add(infoTable);

        document.add(new Paragraph(" "));

        // =========================
        // MASTER TABLE
        // =========================

        int totalColumns =
                masterScoreSheet.getSubjects().size() + 6;

        PdfPTable table =
                new PdfPTable(totalColumns);

        table.setWidthPercentage(100);

        // Header row

        addHeaderCell(table, "No.", headerFont);
        addHeaderCell(table, "Student Id", headerFont);
        addHeaderCell(table, "Student Name", headerFont);

        for (String subject : masterScoreSheet.getSubjects()) {
            addHeaderCell(table, subject, headerFont);
        }

        addHeaderCell(table, "Total Score", headerFont);
        addHeaderCell(table, "Average Score", headerFont);
        addHeaderCell(table, "Position", headerFont);

        // Student rows

        int count = 1;

        for (MasterScoreSheetRow row :
                masterScoreSheet.getRecords()) {

            addBodyCell(table,
                    String.valueOf(count++),
                    bodyFont);

            addBodyCell(table,
                    row.getStudentId(),
                    bodyFont);

            addBodyCell(table,
                    row.getStudentName(),
                    bodyFont);

            for (String subject :
                    masterScoreSheet.getSubjects()) {

                Double score =
                        row.getSubjectScores()
                                .get(subject);

                addBodyCell(
                        table,
                        score == null
                                ? "-"
                                : String.format("%.0f", score),
                        bodyFont
                );
            }

            addBodyCell(
                    table,
                    String.format("%.0f",
                            row.getStudentTotalScore()),
                    bodyFont
            );

            addBodyCell(
                    table,
                    String.format("%.2f",
                            row.getStudentAverageScore()),
                    bodyFont
            );

            addBodyCell(
                    table,
                    String.valueOf(row.getPosition()),
                    bodyFont
            );
        }

        document.add(table);

        document.add(new Paragraph(" "));

        Paragraph footer =
                new Paragraph(
                        "Date Of Report: "
                                + LocalDate.now(),
                        bodyFont
                );

        footer.setAlignment(Element.ALIGN_RIGHT);

        document.add(footer);

        document.close();

        return outputStream.toByteArray();
    }

    /*================================================
                        HELPERS
     =================================================*/

    private void addHeaderCell(
            PdfPTable table,
            String value,
            Font font) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(value, font)
                );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        table.addCell(cell);
    }

    private void addBodyCell(
            PdfPTable table,
            String value,
            Font font) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(value, font)
                );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        table.addCell(cell);
    }
}
