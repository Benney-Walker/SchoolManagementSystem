package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Subject.SubjectDTO;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.Subjects;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.SubjectsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class SubjectsService {

    private final LevelRepository levelRepository;

    private final LoggingService loggingService;

    private final SubjectsRepository subjectsRepository;

    private final UtilityClass utilityClass;

    public ResponseEntity<?> addNewSubject(String subjectName, String levelId, String staffId) {

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.CREATE, "Invalid Class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Invalid Class Id"
            ));
        }

        Subjects subject = subjectsRepository.findBySubjectNameAndLevel_LevelID(subjectName, levelId).orElse(null);
        if (subject != null) {
            loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.CREATE, "Subject already exist", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Subject already exist"
            ));
        }

        Subjects subjects = new Subjects();
        String subjectId = utilityClass.generateEntityId("SUBJECT");
        subjects.setSubjectId(subjectId);
        subjects.setSubjectName(subjectName.toUpperCase());
        subjects.setLevel(level);
        subjectsRepository.saveAndFlush(subjects);

        loggingService.logGeneralActivity(
                LogType.SUBJECT, LogAction.CREATE, "Added subject: " + subjectName + " for " + level.getLevelName(), staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadSubjectData(String subjectId, String staffId) {

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.READ, "Subject not found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Subject not found"
            ));
        }

        loggingService.logGeneralActivity(
                LogType.SUBJECT, LogAction.READ, "Fetched subject data", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok( new SubjectDTO(
                subject.getSubjectId(),
                subject.getSubjectName(),
                subject.getLevel().getLevelID()
        ));
    }

    public ResponseEntity<?> updateSubjectData(SubjectDTO subjectDTO, String staffId) {

        Subjects subject = subjectsRepository.findBySubjectId(subjectDTO.getSubjectId()).orElse(null);
        if (subject == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.UPDATE, "Invalid subject Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Subject Id"
            ));
        }

        Level level = levelRepository.findByLevelID(subjectDTO.getLevelId()).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.UPDATE, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        subject.setSubjectName(subjectDTO.getSubjectName());
        subject.setLevel(level);
        subjectsRepository.save(subject);

        loggingService.logGeneralActivity(
                LogType.SUBJECT, LogAction.UPDATE, "Updated subject: " + subject.getSubjectName() + " for " + level.getLevelName(), staffId, LogStatus.SUCCESS
        );
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> deleteSubjectData(String subjectId, String staffId) {

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logGeneralActivity(LogType.SUBJECT, LogAction.DELETE, "Invalid subject Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Subject Id"
            ));
        }

        subjectsRepository.delete(subject);
        loggingService.logGeneralActivity(
                LogType.SUBJECT, LogAction.DELETE,
                "Deleted subject: " + subject.getSubjectName() + " for " + subject.getLevel().getLevelName(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }
}
