package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Academics.GenerateStudentReport;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JasperReportService {

    public byte[] generateClassReport(List<GenerateStudentReport> generateStudentReports, String schoolName)throws Exception {
        InputStream template = getClass().getResourceAsStream("/reports/class_results_report.jrxml");

        JasperReport jasperReport = JasperCompileManager.compileReport(template);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(generateStudentReports);

        Map<String,Object> parameters = new HashMap<>();
        parameters.put("schoolName", schoolName);

        JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(print);

    }
}
