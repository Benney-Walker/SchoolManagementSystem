package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentReport;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JasperReportService {

    private JasperReport cachedReport;

    public byte[] generateClassReportCards(
            List<GenerateStudentReport> generateStudentReports,
            String schoolName) throws Exception {

        if (generateStudentReports == null) {
            throw new IllegalArgumentException("No report data available");
        }

        JasperReport jasperReport = getCompiledReport();

        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(generateStudentReports);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("schoolName", schoolName);
        parameters.put("logoPath", "reports/daffodils Logo.png");
        parameters.put("signaturePath", "reports/Signature.png");

        JasperPrint print =
                JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(print);
    }


    private JasperReport getCompiledReport() throws JRException, IOException {
        if (cachedReport == null) {
            InputStream template = getClass()
                    .getResourceAsStream("/reports/class_results_report.jasper");
            if (template == null) {
                throw new FileNotFoundException("Report template not found");
            }
            cachedReport = (JasperReport)  JRLoader.loadObject(template);
        }
        return cachedReport;
    }
}
