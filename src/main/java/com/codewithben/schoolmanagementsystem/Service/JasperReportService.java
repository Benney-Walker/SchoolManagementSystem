package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesRecord;
import com.codewithben.schoolmanagementsystem.DTO.Expenses.ExpensesReport;
import com.codewithben.schoolmanagementsystem.DTO.Report.GenerateStudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Report.SbaReport;
import com.codewithben.schoolmanagementsystem.DTO.Report.SubjectReportDTO;
import com.codewithben.schoolmanagementsystem.DTO.Result.SbaRecords;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fills the compiled report templates and exports them to PDF.
 *
 * Every report carries the school's own branding (name, motto, address, logo, principal's
 * signature) through the same five parameters; see {@link #brandingParameters}. Logo and
 * signature come from storage via BrandingAssetService and are passed as images, or null
 * when the school hasn't uploaded one (the templates then leave the space blank).
 *
 * The PDFs come from the committed .jasper files, not the .jrxml. When a .jrxml changes and
 * its .jasper isn't recompiled, the two drift apart and printing fails at runtime, the first
 * time a teacher prints. {@link #verifyTemplates} checks every template when the app starts
 * and logs exactly what is wrong.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JasperReportService {

    /** Parameters every template must declare (the code always passes all five). */
    static final List<String> BRANDING_PARAMETERS =
            List.of("schoolName", "schoolMotto", "schoolAddress", "logo", "signature");

    /**
     * Each template, the bean that fills its main dataset, and the beans that fill its table
     * datasets (by dataset name; "*" for any dataset not named).
     */
    enum ReportTemplate {
        STUDENT_REPORT_CARD("/reports/student_report_card.jasper", GenerateStudentResult.class,
                Map.of("SubjectsDataset", SubjectReportDTO.class)),
        SBA_REPORT("/reports/sba_report.jasper", SbaReport.class,
                Map.of("SbaRecordsDataset", SbaRecords.class)),
        EXPENSES_REPORT("/reports/expenses_report.jasper", ExpensesReport.class,
                Map.of("*", ExpensesRecord.class));

        final String path;
        final Class<?> bean;
        final Map<String, Class<?>> datasetBeans;

        ReportTemplate(String path, Class<?> bean, Map<String, Class<?>> datasetBeans) {
            this.path = path;
            this.bean = bean;
            this.datasetBeans = datasetBeans;
        }
    }

    private final BrandingAssetService brandingAssetService;

    // Thread-safe: two first requests at the same moment used to load a template twice.
    private final Map<ReportTemplate, JasperReport> templates = new ConcurrentHashMap<>();


    public byte[] generateStudentReportCard(
            GenerateStudentResult generateStudentResults,
            Institution institution) throws Exception {

        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(Collections.singletonList(generateStudentResults));

        return fill(ReportTemplate.STUDENT_REPORT_CARD, institution, dataSource);
    }

    public byte[] generateClassReportCards(
            List<GenerateStudentResult> generateStudentResults,
            Institution institution) throws Exception {

        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(generateStudentResults);

        return fill(ReportTemplate.STUDENT_REPORT_CARD, institution, dataSource);
    }

    public byte[] generateSbaReport(SbaReport sbaReport, Institution institution) throws Exception {
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                Collections.singletonList(sbaReport)
        );

        return fill(ReportTemplate.SBA_REPORT, institution, dataSource);
    }

    /*
     * One ExpensesReport object as the single record; the template lists its rows through a
     * table datasetRun over $F{records}. (This used to wrap a bare List, so the template
     * could only reach the rows through $F{_THIS} and had no term name or total.)
     */
    public byte[] generateExpensesReport(ExpensesReport expensesReport, Institution institution) throws Exception {
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                Collections.singletonList(expensesReport)
        );

        return fill(ReportTemplate.EXPENSES_REPORT, institution, dataSource);
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
                   TEMPLATE CHECK AT STARTUP
     =================================================*/

    /**
     * Loads every template once at startup (so the first print doesn't pay for it) and logs
     * an ERROR for each mismatch with the code: a missing template, a branding parameter the
     * template doesn't declare (a .jasper not recompiled after the .jrxml changed), or a
     * field the data bean doesn't have (filling would fail). The app still starts; only the
     * affected report fails.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void verifyTemplates() {
        int problemCount = 0;
        for (ReportTemplate reportTemplate : ReportTemplate.values()) {
            JasperReport report;
            try {
                report = template(reportTemplate);
            } catch (Exception e) {
                log.error("Report template {} could not be loaded; that report will fail until it is added or fixed: {}",
                        reportTemplate.path, e.getMessage());
                problemCount++;
                continue;
            }
            for (String problem : templateProblems(reportTemplate, report)) {
                log.error("Report template {}: {}. Recompile the .jasper from its .jrxml in Jaspersoft Studio 7.0.3.",
                        reportTemplate.path, problem);
                problemCount++;
            }
        }
        if (problemCount == 0) {
            log.info("All {} report templates match the code", ReportTemplate.values().length);
        }
    }

    private static List<String> templateProblems(ReportTemplate reportTemplate, JasperReport report) {
        Set<String> parameters = new HashSet<>();
        for (JRParameter parameter : nonNull(report.getParameters())) {
            if (!parameter.isSystemDefined()) {
                parameters.add(parameter.getName());
            }
        }
        List<String> problems = new ArrayList<>(problems("the main dataset", parameters,
                fieldNames(report.getFields()), reportTemplate.bean));

        for (JRDataset dataset : nonNull(report.getDatasets())) {
            Class<?> rowBean = reportTemplate.datasetBeans.getOrDefault(
                    dataset.getName(), reportTemplate.datasetBeans.get("*"));
            if (rowBean != null) {
                problems.addAll(problems("dataset " + dataset.getName(), null, fieldNames(dataset.getFields()), rowBean));
            }
        }
        return problems;
    }

    /*
     * The pure part of the check, so it can be tested without a compiled template.
     * declaredParameters null = don't check parameters (table datasets have none of ours).
     */
    static List<String> problems(String where, Set<String> declaredParameters, Set<String> declaredFields, Class<?> bean) {
        List<String> problems = new ArrayList<>();
        if (declaredParameters != null) {
            for (String name : BRANDING_PARAMETERS) {
                if (!declaredParameters.contains(name)) {
                    problems.add("parameter '" + name + "' is not declared, so the school's branding won't print");
                }
            }
        }
        Set<String> properties = readableProperties(bean);
        for (String field : declaredFields) {
            if (!"_THIS".equals(field) && !properties.contains(field)) {
                problems.add(where + " declares field '" + field + "', which " + bean.getSimpleName()
                        + " doesn't have; filling the report will fail");
            }
        }
        return problems;
    }

    static Set<String> readableProperties(Class<?> bean) {
        try {
            Set<String> names = new HashSet<>();
            for (PropertyDescriptor property : Introspector.getBeanInfo(bean, Object.class).getPropertyDescriptors()) {
                if (property.getReadMethod() != null) {
                    names.add(property.getName());
                }
            }
            return names;
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot inspect " + bean.getName(), e);
        }
    }

    private static Set<String> fieldNames(JRField[] fields) {
        Set<String> names = new LinkedHashSet<>();
        for (JRField field : nonNull(fields)) {
            names.add(field.getName());
        }
        return names;
    }

    private static <T> List<T> nonNull(T[] values) {
        return values == null ? List.of() : Arrays.asList(values);
    }


    /*================================================
                        HELPERS
     =================================================*/

    private byte[] fill(ReportTemplate reportTemplate, Institution institution, JRDataSource dataSource) throws Exception {
        JasperPrint print = JasperFillManager.fillReport(
                template(reportTemplate), brandingParameters(institution), dataSource);
        return JasperExportManager.exportReportToPdf(print);
    }

    private JasperReport template(ReportTemplate reportTemplate) throws JRException, IOException {
        JasperReport cached = templates.get(reportTemplate);
        if (cached != null) {
            return cached;
        }
        try (InputStream template = getClass().getResourceAsStream(reportTemplate.path)) {
            if (template == null) {
                throw new FileNotFoundException("Report template not found: " + reportTemplate.path);
            }
            JasperReport loaded = (JasperReport) JRLoader.loadObject(template);
            JasperReport existing = templates.putIfAbsent(reportTemplate, loaded);
            return existing != null ? existing : loaded;
        }
    }
}
