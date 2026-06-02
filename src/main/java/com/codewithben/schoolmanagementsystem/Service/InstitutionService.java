package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Result.GradingCriteria;
import com.codewithben.schoolmanagementsystem.Entity.GradeSystem;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.GradeSystemRepository;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InstitutionService {
    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final GradeSystemRepository gradeSystemRepository;

    private final StaffsRepository staffsRepository;

    private final LoggingService loggingService;

    public InstitutionService(UtilityClass utilityClass, InstitutiionRepository institutiionRepository,
                              GradeSystemRepository gradeSystemRepository,  StaffsRepository staffsRepository,
                              LoggingService loggingService) {
        this.utilityClass = utilityClass;
        this.institutiionRepository = institutiionRepository;
        this.gradeSystemRepository = gradeSystemRepository;
        this.staffsRepository = staffsRepository;
        this.loggingService = loggingService;
    }

    public ResponseEntity<?> addNewInstitution(String institutionName, String logData) {
        Institution institution = institutiionRepository.findByInstitutionName(institutionName).orElse(null);
        if (institution == null) {
            institution = new Institution();
            String id = utilityClass.generateEntityId("INSTITUTION");
            institution.setInstitutionId(id);
            institution.setInstitutionName(institutionName);
            institutiionRepository.save(institution);

            loggingService.logActivity(LogType.SUBSCRIPTION, logData, "N/A", "SUCCESS");
            return ResponseEntity.ok(id);
        }

        loggingService.logActivity(LogType.SUBSCRIPTION, logData, "N/A", "FAILED");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Institution already exist");
    }

    public ResponseEntity<?> saveGradingCriteria(GradingCriteria gradingCriteria, String staffId) {
        String logData = "Lower Range: " + gradingCriteria.getLowerRange() + " Higher Range: " + gradingCriteria.getUpperRange()
                + " Grade: " + gradingCriteria.getGrade() + " Description: " + gradingCriteria.getGradeDescription();

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            loggingService.logActivity(LogType.GRADING_CRITERIA, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Staff Id"
            ));
        }

        if (gradingCriteria.getId() == 0) {

            GradeSystem criteria =
                    gradeSystemRepository.findByGradeAndInstitution_InstitutionId(
                            gradingCriteria.getGrade(),
                            staff.getInstitution().getInstitutionId()
                    ).orElse(null);

            if (criteria != null) {
                loggingService.logActivity(LogType.GRADING_CRITERIA, logData, staffId, "FAILED");
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

            loggingService.logActivity(LogType.GRADING_CRITERIA, logData, staffId, "SUCCESS");
            return ResponseEntity.ok().build();
        }

        GradeSystem criteria = gradeSystemRepository.findById(gradingCriteria.getId()).orElse(null);
        if (criteria == null) {
            loggingService.logActivity(LogType.GRADING_CRITERIA, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Criteria do not exist on system"
            ));
        }

        criteria.setLowerRange(gradingCriteria.getLowerRange());
        criteria.setUpperRange(gradingCriteria.getUpperRange());
        criteria.setGrade(gradingCriteria.getGrade());
        criteria.setGradeDescription(gradingCriteria.getGradeDescription());
        gradeSystemRepository.save(criteria);

        loggingService.logActivity(LogType.GRADING_CRITERIA, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadAllGradingCriteria(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            loggingService.logActivity(LogType.GRADING_CRITERIA, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Staff Id"
            ));
        }

        List<GradeSystem> gradingList =
                gradeSystemRepository.findAllByInstitution_InstitutionId(staff.getInstitution().getInstitutionId());
        if(gradingList == null || gradingList.isEmpty()){
            loggingService.logActivity(LogType.GRADING_CRITERIA, "N/A", staffId, "FAILED");
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

        return ResponseEntity.ok(gradingCriteriaList);
    }


}
