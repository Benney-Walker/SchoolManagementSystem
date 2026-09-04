package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.StudentStatus;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@Component
public class UtilityClass {

    private final EntityID_generationRepository entityID_generationRepository;

    private final GradeSystemRepository gradeSystemRepository;

    private final InstitutiionRepository institutiionRepository;

    private final ResultsRepository resultsRepository;

    private final SemesterRepository semesterRepository;

    private final AtomicReference<List<GradeSystem>> cache = new AtomicReference<>();

    //Id generation method
    public String generateEntityId(String entityName) {
        String newCode = "";

        if (entityName.equals("STAFF")) {
            String prefix = "ST";
            long entityCode = 100100L;
            newCode = getStringCode(entityName, prefix, entityCode);

        } else if (entityName.equals("STUDENT")) {
            String prefix = "STD";
            long entityCode = 100200L;
            newCode = getStringCode(entityName, prefix, entityCode);

        } else if (entityName.equals("SUBJECT")) {
            String prefix = "SUB";
            long entityCode = 100300L;
            newCode = getStringCode(entityName, prefix, entityCode);

        } else if (entityName.equals("LEVEL")) {
            String prefix = "LV";
            long entityCode = 100400L;
            newCode = getStringCode(entityName, prefix, entityCode);

        } else if (entityName.equals("REPORT")) {
            String prefix = "RP";
            long entityCode = 100500L;
            newCode = getStringCode(entityName, prefix, entityCode);

        } else if (entityName.equals("INSTITUTION")) {
            String prefix = "INS";
            long entityCode = 100600L;
            newCode = getStringCode(entityName, prefix, entityCode);

        } else if (entityName.equals("SEMESTER")) {
            String prefix = "SE";
            long entityCode = 100700L;
            newCode = getStringCode(entityName, prefix, entityCode);

        } else if (entityName.equals("FEES_PAYMENT")) {
            String prefix = "TX";
            long entityCode = 100800300L;
            newCode = getStringCode(entityName, prefix, entityCode);
        } else if (entityName.equals("FEES")) {
            String prefix = "FE";
            long entityCode = 100900300L;
            newCode = getStringCode(entityName, prefix, entityCode);
        }

        return newCode;
    }

    private String getStringCode(String entityName, String prefix, long entityCode) {
        try {
            EntityID_generation generateId = entityID_generationRepository.findByEntityName(entityName).orElse(
                    new EntityID_generation(entityName, entityCode)
            );
            long code = generateId.getCode();
            generateId.setCode(code + 1L);
            entityID_generationRepository.save(generateId);

            return prefix + code;
        }catch (Exception ex) {
            log.error("Failed to generate entity ID for entityName='{}' (prefix='{}')", entityName, prefix, ex);
            return null;
        }
    }

    //Load (Cached) list ordered by LowerRange
    private List<GradeSystem> getOrderedCacheList(String institutionId) {
        List<GradeSystem> list = cache.get();
        if (list == null) {
            list = gradeSystemRepository.findAllByInstitution_InstitutionId(institutionId);
            cache.set(list);
        }
        return list;
    }

    //Returns subject grade and its description based on the score
    public String getGradeAndDescription(Double totalScore, String institutionId) {
        List<GradeSystem> list = getOrderedCacheList(institutionId);

        //Find matching range
        for (GradeSystem grade : list) {
            if (totalScore >= grade.getLowerRange() && totalScore <= grade.getUpperRange()) {
                return grade.getGrade() + "_" + grade.getGradeDescription();
            }
        }
        return " ";
    }

    //Extract grade
    public String extractGrade(Double totalScore, String institutionId) {
        String[] gradeAndDescription = getGradeAndDescription(totalScore, institutionId).split("_");

        if (gradeAndDescription.length == 2) {
            return gradeAndDescription[0];
        }
        return null;
    }

    //Extract grade Description
    public String extractDescription(Double totalScore, String institutionId) {
        String[] gradeAndDescription = getGradeAndDescription(totalScore, institutionId).split("_");

        if (gradeAndDescription.length == 2) {
            return gradeAndDescription[1];
        }
        return null;
    }

    //Method for finding the current semester
    public Semester getCurrentSemester(Institution institution) {
        LocalDate currentDate = LocalDate.now();

        List<Semester> semesters = institution.getSemester();
        if (semesters == null || semesters.isEmpty())
            return null;

        for (Semester semester : semesters) {
            LocalDate startDate = semester.getSemesterStartDate();
            LocalDate endDate = semester.getSemesterEndDate();
            LocalDate gradePeriod = endDate.plusDays(7);

            // Check if current date is within semester range (inclusive)
            if (currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) {
                if (currentDate.isEqual(endDate) || currentDate.isBefore(gradePeriod)) {
                    return semester;
                }
            }
        }
        return null;
    }

    public boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();

        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public boolean isNotSchoolday(Semester semester, LocalDate selectedDate) {
        List<SchoolHoliday> holidays = semester.getSchoolHoliday();
        if (holidays != null && !holidays.isEmpty()) {
            for (SchoolHoliday holiday : holidays) {
                if (!selectedDate.isBefore(holiday.getStartDate()) && !selectedDate.isAfter(holiday.getEndDate())) {
                    return true;
                }
            }
        }

        return isWeekend(selectedDate);
    }

    public List<Students> getActiveStudents(List<Students> students) {
        List<Students> studentsListForReport = new ArrayList<>();
        for (Students student: students) {
            if (student.getStudentStatus() == StudentStatus.ACTIVE) {
                studentsListForReport.add(student);
            }
        }
        return studentsListForReport;
    }

    public String getResumingDate(Semester semester) {
        List<Semester> nextSemester = semester.getInstitution().getSemester();
        return nextSemester.stream()
                .filter(s -> s.getSemesterStartDate().isAfter(semester.getSemesterEndDate()))
                .min(Comparator.comparing(Semester::getSemesterStartDate))
                .map(s -> s.getSemesterStartDate().toString())
                .orElse(null);
    }

    public void updateResultTotals(Results result, Staffs updatedBy, int classSize) {
        List<SubjectScore> scores = result.getSubjectScores();

        double total = 0.0;
        if (scores == null || scores.isEmpty()) {
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
        } else {

            for(SubjectScore score : scores) {
                total += score.getTotalScore();
            }

            result.setUpdatedBy(updatedBy);
            result.setTotalScore(Double.parseDouble(String.format("%.1f", total)));
            result.setAverageScore(Double.parseDouble(String.format("%.1f", total / scores.size())));
            result.setClassSize(classSize);
        }
    }

    public void reArrangePositions(List<Results> resultsList) {

        if (resultsList == null || resultsList.isEmpty()) {
            return;
        }

        int currentRank = 0;
        double lastScore = -1.0;

        for (int i = 0; i < resultsList.size(); i++) {
            Results currentResult = resultsList.get(i);
            double score = currentResult.getTotalScore();

            if (score != lastScore) {
                currentRank = i + 1;
                lastScore = score;
            }

            currentResult.setPosition(ordinal(currentRank));
        }

        resultsRepository.saveAll(resultsList);
    }

    private String ordinal(int number) {
        if (number >= 11 && number <= 13) {
            return number + "th";
        }
        switch (number % 10) {
            case 1: return number + "st";
            case 2: return number + "nd";
            case 3: return number + "rd";
            default: return number + "th";
    public static String toInternational(String rawNumber) {
        if (rawNumber == null || rawNumber.isEmpty()) {
            return null;
        }

        String digits = rawNumber.replaceAll("[\\s()\\-]", "").replaceFirst("^\\+", "");
        if (digits.isEmpty()) {
            return null;
        }

        if (digits.startsWith("0") && digits.length() == 10) {
            return "+233" + digits.substring(1);
        } else if (digits.startsWith("233")){
            return "+" + digits;
        }
        return digits;
    }
}