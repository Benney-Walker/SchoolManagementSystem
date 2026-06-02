package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.ConductRatings;
import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductRecord;
import com.codewithben.schoolmanagementsystem.Entity.Conduct;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Results;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Repository.ConductRepository;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.ResultsRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@Service
public class ConductService {

    private final ConductRepository conductRepository;

    private final LoggingService loggingService;

    private final ResultsRepository resultsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    public ResponseEntity<?> getStudentsConduct(String levelId, String semesterId, String staffId) {
        String logData = "Level= " + levelId + ", Semester= " + semesterId + ", staffId= " + staffId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.CONDUCT, logData, staffId, LogStatus.FAILED.name());
            return ResponseEntity.ok(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.CONDUCT, logData, staffId, LogStatus.FAILED.name());
            return ResponseEntity.ok(Map.of(
                    "message", "Invalid semester Id"
            ));
        }

        List<Results> studentList = resultsRepository.findByLevel_LevelIDAndSemester_SemesterIDOrderByTotalScoreDesc(levelId, semesterId);
        if (studentList == null || studentList.isEmpty()) {
            loggingService.logActivity(LogType.CONDUCT, logData, staffId, LogStatus.FAILED.name());
            return ResponseEntity.ok(Map.of(
                    "message", "No results found to contain conduct records"
            ));
        }

        List<StudentConductRecord> conductList = new ArrayList<>();
        //Retrieve conducts
        for (Results result : studentList) {

            Conduct conduct = result.getConduct();

            String studentId = result.getStudent().getStudentId();
            String studentName = result.getStudent().getFirstName() + " " +
                    result.getStudent().getLastName();
            String regular = conduct.getRegular().toString() == null ?
                    ConductRatings.GOOD.name() : conduct.getRegular().toString();
            String punctual = conduct.getPunctual().toString() == null ?
                    ConductRatings.GOOD.name() : conduct.getPunctual().toString();
            String physicalAppearance = conduct.getPhysicalAppearance() == null ?
                    ConductRatings.GOOD.name() : conduct.getPhysicalAppearance().toString();
            String social = conduct.getSocial().toString() == null ?
                    ConductRatings.GOOD.name() : conduct.getSocial().toString();
            String emotional = conduct.getEmotional().toString() == null ?
                    ConductRatings.GOOD.name() : conduct.getEmotional().toString();
            String cognitiveSkills = conduct.getCognitiveSkills().toString() == null ?
                    ConductRatings.GOOD.name() : conduct.getCognitiveSkills().toString();
            String conductRemark = conduct.getClassTeacherRemark() == null ?
                    "" : conduct.getClassTeacherRemark();

            StudentConductRecord studentConductRecord = StudentConductRecord.builder()
                    .studentId(studentId)
                    .studentName(studentName)
                    .semesterId(semesterId)
                    .regular(regular)
                    .punctual(punctual)
                    .physicalAppearance(physicalAppearance)
                    .social(social)
                    .emotional(emotional)
                    .cognitiveSkills(cognitiveSkills)
                    .conductRemark(conductRemark)
                    .build();

            conductList.add(studentConductRecord);
        }

        return ResponseEntity.ok(conductList);
    }
}
