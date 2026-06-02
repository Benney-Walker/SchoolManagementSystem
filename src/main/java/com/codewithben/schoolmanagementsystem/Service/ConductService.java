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
import org.springframework.http.HttpStatus;
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.CONDUCT, logData, staffId, LogStatus.FAILED.name());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid semester Id"
            ));
        }

        List<Results> studentList = resultsRepository.findByLevel_LevelIDAndSemester_SemesterIDOrderByTotalScoreDesc(levelId, semesterId);
        if (studentList == null || studentList.isEmpty()) {
            loggingService.logActivity(LogType.CONDUCT, logData, staffId, LogStatus.FAILED.name());
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

        return ResponseEntity.ok(conductList);
    }

    public ResponseEntity<?> saveStudentConducts(String staffId, StudentConductRecord record) {

        String logData
                = "studentId= " + record.getStudentId() + " studentName= " + record.getStudentName() +
                " semesterId= " + record.getSemesterId() + " regular= " + record.getRegular() +
                " punctual= " + record.getPunctual() + " physical Appearance= " + record.getPhysicalAppearance() +
                " social= " + record.getSocial() + " emotional= " + record.getEmotional() +
                " cognitiveSkills= " + record.getCognitiveSkills() + " conductRemark= " + record.getConductRemark();

        Results studentResult =
                resultsRepository.findByStudent_StudentIdAndSemester_SemesterID(record.getStudentId(), record.getSemesterId()).orElse(null);
        if (studentResult == null) {
            loggingService.logActivity(LogType.CONDUCT, logData, staffId, LogStatus.FAILED.name());
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
            loggingService.logActivity(LogType.CONDUCT, logData, staffId, LogStatus.SUCCESS.name());
            return ResponseEntity.ok().build();
        }

        conduct.setRegular(ConductRatings.valueOf(record.getRegular()));
        conduct.setPunctual(ConductRatings.valueOf(record.getPunctual()));
        conduct.setPhysicalAppearance(ConductRatings.valueOf(record.getPhysicalAppearance()));
        conduct.setSocial(ConductRatings.valueOf(record.getSocial()));
        conduct.setEmotional(ConductRatings.valueOf(record.getEmotional()));
        conduct.setCognitiveSkills(ConductRatings.valueOf(record.getCognitiveSkills()));
        conduct.setClassTeacherRemark(record.getConductRemark());
        conductRepository.save(conduct);
        loggingService.logActivity(LogType.CONDUCT, logData, staffId, LogStatus.SUCCESS.name());
        return ResponseEntity.ok().build();
    }
}
