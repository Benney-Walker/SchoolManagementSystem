package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Result.GradingCriteria;
import com.codewithben.schoolmanagementsystem.Entity.GradeSystem;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.GradeSystemRepository;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
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
public class InstitutionService {
    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final GradeSystemRepository gradeSystemRepository;

    private final StaffsRepository staffsRepository;

    private final LoggingService loggingService;


    public ResponseEntity<?> addNewInstitution(String institutionName, String logData) {
        Institution institution = institutiionRepository.findByInstitutionName(institutionName).orElse(null);
        if (institution == null) {
            institution = new Institution();
            String id = utilityClass.generateEntityId("INSTITUTION");
            institution.setInstitutionId(id);
            institution.setInstitutionName(institutionName);
            institutiionRepository.save(institution);

            loggingService.logActivity(LogType.INSTITUTION, LogAction.CREATE, "N/A", "N/A", LogStatus.SUCCESS);
            return ResponseEntity.ok(id);
        }

        loggingService.logActivity(LogType.INSTITUTION, LogAction.CREATE, "N/A", "N/A", LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Institution already exist");
    }

    public ResponseEntity<?> saveGradingCriteria(GradingCriteria gradingCriteria, String staffId) {
        String logData = "Lower Range: " + gradingCriteria.getLowerRange() + " Higher Range: " + gradingCriteria.getUpperRange()
                + " Grade: " + gradingCriteria.getGrade() + " Description: " + gradingCriteria.getGradeDescription();

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            loggingService.logActivity(LogType.GRADE, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Staff Id"
            ));
        }

        GradeSystem criteria =
                gradeSystemRepository.findByGradeAndInstitution_InstitutionId(
                        gradingCriteria.getGrade(),
                        staff.getInstitution().getInstitutionId()
                ).orElse(null);

        if (criteria != null) {
            loggingService.logActivity(LogType.GRADE, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Criteria already exist"
            ));
        }

        GradeSystem gradeSystem = new GradeSystem();
        gradeSystem.setLowerRange(gradingCriteria.getLowerRange());
        gradeSystem.setUpperRange(gradingCriteria.getUpperRange());
        gradeSystem.setGrade(gradingCriteria.getGrade());
        gradeSystem.setGradeDescription(gradingCriteria.getGradeDescription());
        gradeSystem.setInstitution(staff.getInstitution());
        gradeSystemRepository.save(gradeSystem);

        loggingService.logActivity(LogType.GRADE, LogAction.CREATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();

    }

    public ResponseEntity<?> loadAllGradingCriteria(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            loggingService.logActivity(LogType.GRADE, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Staff Id"
            ));
        }

        List<GradeSystem> gradingList =
                gradeSystemRepository.findAllByInstitution_InstitutionId(staff.getInstitution().getInstitutionId());
        if(gradingList == null || gradingList.isEmpty()){
            loggingService.logActivity(LogType.GRADE, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no grading criteria set"
            ));
        }

        List<GradingCriteria> gradingCriteriaList = new ArrayList<>();
        //Retrieve all criteria
        for(GradeSystem grading : gradingList){

            GradingCriteria gradingCriteria = GradingCriteria.builder()
                    .id(grading.getId())
                    .grade(grading.getGrade())
                    .gradeDescription(grading.getGradeDescription())
                    .lowerRange(grading.getLowerRange())
                    .upperRange(grading.getUpperRange())
                    .build();

            gradingCriteriaList.add(gradingCriteria);
        }

        loggingService.logActivity(LogType.GRADE, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(gradingCriteriaList);
    }

    public ResponseEntity<?> updateGradingCriteria(GradingCriteria gradingCriteria, String staffId) {

        String logData = "Lower Range: " + gradingCriteria.getLowerRange() + " Higher Range: " + gradingCriteria.getUpperRange()
                + " Grade: " + gradingCriteria.getGrade() + " Description: " + gradingCriteria.getGradeDescription();

        GradeSystem criteria = gradeSystemRepository.findById(gradingCriteria.getId()).orElse(null);
        if (criteria == null) {
            loggingService.logActivity(LogType.GRADE, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Criteria do not exist on system"
            ));
        }

        criteria.setLowerRange(gradingCriteria.getLowerRange());
        criteria.setUpperRange(gradingCriteria.getUpperRange());
        criteria.setGrade(gradingCriteria.getGrade());
        criteria.setGradeDescription(gradingCriteria.getGradeDescription());
        gradeSystemRepository.save(criteria);

        loggingService.logActivity(LogType.GRADE, LogAction.CREATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }
}
