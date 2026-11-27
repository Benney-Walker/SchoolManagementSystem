package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesReport;
import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Report.SbaReport;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fills the compiled report templates and exports them to PDF.
 *
 * Every report carries the school's own branding (name, motto, address, logo, principal's
 * signature) through the same five parameters; see {@link #brandingParameters}. Logo and
 * signature come from storage via BrandingAssetService and are passed as images, or null
 * when the school hasn't uploaded one (the templates then leave the space blank).
 */
@Service
@RequiredArgsConstructor
public class JasperReportService {

    private final BrandingAssetService brandingAssetService;

    private JasperReport cachedStudentReport;

    private JasperReport cachedSbaReport;

    private JasperReport cachedExpensesReport;


    public byte[] generateStudentReportCard(
            GenerateStudentResult generateStudentResults,
            Institution institution) throws Exception {

        JasperReport jasperReport = getCompiledStudentReport();

        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(Collections.singletonList(generateStudentResults));

        JasperPrint print =
                JasperFillManager.fillReport(jasperReport, brandingParameters(institution), dataSource);

        return JasperExportManager.exportReportToPdf(print);
    }

    public byte[] generateClassReportCards(
            List<GenerateStudentResult> generateStudentResults,
            Institution institution) throws Exception {

        JasperReport jasperReport = getCompiledStudentReport();

        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(generateStudentResults);

        JasperPrint print =
                JasperFillManager.fillReport(jasperReport, brandingParameters(institution), dataSource);

        return JasperExportManager.exportReportToPdf(print);
    }

    public byte[] generateSbaReport(SbaReport sbaReport, Institution institution) throws Exception {
        JasperReport jasperReport = getCompiledSbaReport();

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                Collections.singletonList(sbaReport)
        );

        JasperPrint print =
                JasperFillManager.fillReport(jasperReport, brandingParameters(institution), dataSource);

        return JasperExportManager.exportReportToPdf(print);
    }

    /*
     * One ExpensesReport object as the single record; the template lists its rows through a
     * table datasetRun over $F{records}. (This used to wrap a bare List, so the template
     * could only reach the rows through $F{_THIS} and had no term name or total.)
     */
    public byte[] generateExpensesReport(ExpensesReport expensesReport, Institution institution) throws Exception {
        JasperReport jasperReport = getCompiledExpensesReport();

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                Collections.singletonList(expensesReport)
        );

        JasperPrint print =
                JasperFillManager.fillReport(jasperReport, brandingParameters(institution), dataSource);

        return JasperExportManager.exportReportToPdf(print);
    }

    /*
     * Report parameters shared by every template:
     *   schoolName    (String)          always set
     *   schoolMotto   (String)          null when not set
     *   schoolAddress (String)          null when not set
     *   logo          (java.awt.Image)  null when not uploaded or unavailable
     *   signature     (java.awt.Image)  null when not uploaded or unavailable
     * Images are decoded once and reused, so class report cards print the same logo on
     * every page (an InputStream would be used up by the first page).
     */
    Map<String, Object> brandingParameters(Institution institution) {
        BrandingAssetService.ReportBranding branding = brandingAssetService.brandingFor(institution);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("schoolName", branding.schoolName());
        parameters.put("schoolMotto", branding.motto());
        parameters.put("schoolAddress", branding.address());
        parameters.put("logo", branding.logo());
        parameters.put("signature", branding.signature());
        return parameters;
    }


    /*================================================
                        HELPERS
     =================================================*/
    private JasperReport getCompiledStudentReport() throws JRException, IOException {
        if (cachedStudentReport == null) {
            InputStream template = getClass()
                    .getResourceAsStream("/reports/student_report_card.jasper");
            if (template == null) {
                throw new FileNotFoundException("Report template not found");
            }
            cachedStudentReport = (JasperReport)  JRLoader.loadObject(template);
        }
        return cachedStudentReport;
    }

    private JasperReport getCompiledSbaReport() throws JRException, IOException {
        if (cachedSbaReport == null) {
            InputStream template = getClass()
                    .getResourceAsStream("/reports/sba_report.jasper");
            if (template == null) {
                throw new FileNotFoundException("Report template not found");
            }
            cachedSbaReport = (JasperReport)  JRLoader.loadObject(template);
        }
        return cachedSbaReport;
    }

    private JasperReport getCompiledExpensesReport() throws JRException, IOException {
        if (cachedExpensesReport == null) {
            InputStream template = getClass()
                    .getResourceAsStream("/reports/expenses_report.jasper");
            if (template == null) {
                throw new FileNotFoundException("Report template not found");
            }

            cachedExpensesReport = (JasperReport) JRLoader.loadObject(template);
        }

        return cachedExpensesReport;
    }
}
