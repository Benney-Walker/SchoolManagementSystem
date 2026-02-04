package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Academics.GenerateStudentReport;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
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

    public byte[] generateClassReport(
            List<GenerateStudentReport> generateStudentReports,
            String schoolName) throws Exception {

        if (generateStudentReports == null || generateStudentReports.isEmpty()) {
            throw new IllegalArgumentException("No report data available");
        }

        JasperReport jasperReport = getCompiledReport();

        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(generateStudentReports);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("schoolName", schoolName);

        JasperPrint print =
                JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(print);
    }


    private JasperReport getCompiledReport() throws JRException, IOException {
        if (cachedReport == null) {
            InputStream template = getClass()
                    .getResourceAsStream("/reports/class_results_report.jrxml");
            if (template == null) {
                throw new FileNotFoundException("Jasper template not found");
            }
            cachedReport = JasperCompileManager.compileReport(template);
        }
        return cachedReport;
    }
}
