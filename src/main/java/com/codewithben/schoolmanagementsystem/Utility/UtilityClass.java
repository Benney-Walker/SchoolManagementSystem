package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Contants.StudentStatus;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.EntityID_generationRepository;
import com.codewithben.schoolmanagementsystem.Repository.GradeSystemRepository;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.ResultsRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class UtilityClass {

    private final EntityID_generationRepository entityID_generationRepository;

    private final GradeSystemRepository gradeSystemRepository;

    private final InstitutiionRepository institutiionRepository;

    private final ResultsRepository resultsRepository;

    private final AtomicReference<List<GradeSystem>> cache = new AtomicReference<>();

    public UtilityClass(EntityID_generationRepository entityID_generationRepository, InstitutiionRepository institutiionRepository,
                        GradeSystemRepository gradeSystemRepository,  ResultsRepository resultsRepository) {
        this.entityID_generationRepository = entityID_generationRepository;
        this.gradeSystemRepository = gradeSystemRepository;
        this.institutiionRepository = institutiionRepository;
        this.resultsRepository = resultsRepository;
    }

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
            
        } else if (entityName.equals("TRANSACTION")) {
            String prefix = "TX";
            long entityCode = 100800300L;
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

            return prefix + String.valueOf(code);
        }catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
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
    public String getCurrentSemesterId(String institutionId) {
        LocalDate currentDate = LocalDate.now();

        Institution institution = institutiionRepository.findByInstitutionId(institutionId).orElse(null);
        if (institution == null)
            return "";

        List<Semester> semesters = institution.getSemester();
        if (semesters == null)
            return "";

        for (Semester semester : semesters) {
            LocalDate startDate = semester.getSemesterStartDate();
            LocalDate endDate = semester.getSemesterEndDate();
            LocalDate gradePeriod = endDate.plusDays(10);

            // Check if current date is within semester range (inclusive)
            if (currentDate.isEqual(startDate) || currentDate.isAfter(startDate)) {
                if (currentDate.isEqual(endDate) || currentDate.isBefore(gradePeriod)) {
                    return semester.getSemesterID();
                }
            }
        }
        return "";
    }

    public void reArrangePositions(Subjects subject, Semester semester) {
        Level level = subject.getLevel();

        // 1. Fetch results sorted by score (High to Low)
        List<Results> resultsList = resultsRepository
                .findByLevel_LevelIDAndSemester_SemesterIDOrderByTotalScoreDesc(
                        level.getLevelID(), semester.getSemesterID()
                );

        if (resultsList == null || resultsList.isEmpty()) {
            return;
        }

        int currentRank = 0;
        double lastScore = -1.0;

        for (int i = 0; i < resultsList.size(); i++) {
            Results currentResult = resultsList.get(i);
            double score = currentResult.getTotalScore();
            System.out.println("PRINT OUT OF TOTAL SCORE" + score);


            if (score != lastScore) {
                currentRank = i + 1;
                lastScore = score;
            }

            currentResult.setPosition(ordinal(currentRank));
            resultsRepository.save(currentResult);
        }
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
        }
    }

    public boolean isSchoolDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();

        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
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
}
