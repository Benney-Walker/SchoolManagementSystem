package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.ConductRatings;
import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductRecord;
import com.codewithben.schoolmanagementsystem.DTO.Conduct.StudentConductReport;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class ConductService {

    private final ConductRepository conductRepository;

    private final LoggingService loggingService;

    private final ResultsRepository resultsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    public ResponseEntity<?> getStudentsConduct(String levelId, String semesterId, String staffId) {

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.READ, "Invalid semester Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid semester Id"
            ));
        }

        List<Results> studentList = resultsRepository.findByLevel_LevelIDAndSemester_SemesterIDOrderByTotalScoreDesc(levelId, semesterId);
        if (studentList == null || studentList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.READ, "No results found to contain conduct records", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No results found to contain conduct records"
            ));
        }

        List<StudentConductRecord> conductList = new ArrayList<>();
        //Retrieve conducts
        for (Results result : studentList) {

            String studentId = result.getStudent().getStudentId();
            String studentName = result.getStudent().getFirstName() + " " +
                    result.getStudent().getLastName();
            String regular;
            String punctual;
            String physicalAppearance;
            String social;
            String emotional;
            String cognitiveSkills;
            String conductRemark;

            Conduct conduct = result.getConduct();
            if (conduct == null) {
                regular = ConductRatings.GOOD.name();
                punctual = ConductRatings.GOOD.name();
                physicalAppearance = ConductRatings.GOOD.name();
                social = ConductRatings.GOOD.name();
                emotional = ConductRatings.GOOD.name();
                cognitiveSkills = ConductRatings.GOOD.name();
                conductRemark = " ";
            } else {

                regular = conduct.getRegular().toString();
                punctual = conduct.getPunctual().toString();
                physicalAppearance = conduct.getPhysicalAppearance().toString();
                social = conduct.getSocial().toString();
                emotional = conduct.getEmotional().toString();
                cognitiveSkills = conduct.getCognitiveSkills().toString();
                conductRemark = conduct.getClassTeacherRemark();
            }

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

        loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.READ, "Loaded conducts records for " + level.getLevelName(), staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(conductList);
    }

    public ResponseEntity<?> saveStudentConducts(String staffId, StudentConductRecord record) {

        Results studentResult =
                resultsRepository.findByStudent_StudentIdAndSemester_SemesterID(record.getStudentId(), record.getSemesterId()).orElse(null);
        if (studentResult == null) {
            loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.CREATE, "No semester result found for this student", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No semester result found for this student"
            ));
        }

        Conduct conduct = studentResult.getConduct();
        if (conduct == null) {

            conduct = new Conduct();
            conduct.setStudent(studentResult.getStudent());
            conduct.setResults(studentResult);
            conduct.setRegular(ConductRatings.valueOf(record.getRegular()));
            conduct.setPunctual(ConductRatings.valueOf(record.getPunctual()));
            conduct.setPhysicalAppearance(ConductRatings.valueOf(record.getPhysicalAppearance()));
            conduct.setSocial(ConductRatings.valueOf(record.getSocial()));
            conduct.setEmotional(ConductRatings.valueOf(record.getEmotional()));
            conduct.setCognitiveSkills(ConductRatings.valueOf(record.getCognitiveSkills()));
            conduct.setClassTeacherRemark(record.getConductRemark());
            conductRepository.save(conduct);

            studentResult.setConduct(conduct);
            resultsRepository.save(studentResult);

        } else {
            conduct.setRegular(ConductRatings.valueOf(record.getRegular()));
            conduct.setPunctual(ConductRatings.valueOf(record.getPunctual()));
            conduct.setPhysicalAppearance(ConductRatings.valueOf(record.getPhysicalAppearance()));
            conduct.setSocial(ConductRatings.valueOf(record.getSocial()));
            conduct.setEmotional(ConductRatings.valueOf(record.getEmotional()));
            conduct.setCognitiveSkills(ConductRatings.valueOf(record.getCognitiveSkills()));
            conduct.setClassTeacherRemark(record.getConductRemark());
            conductRepository.save(conduct);
        }

        loggingService.logGeneralActivity(LogType.CONDUCT, LogAction.CREATE, "Saved conduct record for " + studentResult.getStudent().getFirstName(), staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public StudentConductReport getStudentConductReport(Conduct conduct) {

        return StudentConductReport.builder()
                .regular(conduct.getRegular().name())
                .punctual(conduct.getPunctual().name())
                .physicalAppearance(conduct.getPhysicalAppearance().name())
                .social(conduct.getSocial().name())
                .emotional(conduct.getEmotional().name())
                .cognitiveSkills(conduct.getCognitiveSkills().name())
                .facilitatorRemark(conduct.getClassTeacherRemark())
                .build();
    }
}
