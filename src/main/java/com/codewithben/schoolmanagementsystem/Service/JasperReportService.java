package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Report.SbaReport;
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

@Service
public class JasperReportService {

    private JasperReport cachedStudentReport;

    private JasperReport cachedSbaReport;

    public byte[] generateClassReportCards(
            List<GenerateStudentResult> generateStudentResults,
            String schoolName) throws Exception {

        JasperReport jasperReport = getCompiledStudentReport();

        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(generateStudentResults);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("schoolName", schoolName);
        parameters.put("logoPath", "reports/Daffodils/daffodils_Logo.png");
        parameters.put("signaturePath", "reports/Daffodils/principal_signature.png");

        JasperPrint print =
                JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(print);
    }

    public byte[] generateSbaReport(SbaReport sbaReport, String schoolName) throws Exception {
        JasperReport jasperReport = getCompiledSbaReport();

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                Collections.singletonList(sbaReport)
        );

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("schoolName", schoolName);
        parameters.put("logoPath", "reports/Daffodils/daffodils_Logo.png");
        parameters.put("signaturePath", "reports/Daffodils/principal_signature.png");

        JasperPrint print =
                JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(print);
    }


    /*================================================
                        HELPERS
     =================================================*/
    private JasperReport getCompiledStudentReport() throws JRException, IOException {
        if (cachedStudentReport == null) {
            InputStream template = getClass()
                    .getResourceAsStream("/reports/Daffodils/student_report_card.jasper");
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
                    .getResourceAsStream("/reports/Daffodils/sba_report.jasper");
            if (template == null) {
                throw new FileNotFoundException("Report template not found");
            }
            cachedSbaReport = (JasperReport)  JRLoader.loadObject(template);
        }
        return cachedSbaReport;
    }
}
