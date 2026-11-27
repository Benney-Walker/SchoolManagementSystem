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
import com.codewithben.schoolmanagementsystem.Utility.GradeBands;
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


    public ResponseEntity<?> addNewInstitution(String institutionName) {
        Institution institution = institutiionRepository.findByInstitutionName(institutionName).orElse(null);
        if (institution == null) {
            institution = new Institution();
            String id = utilityClass.generateEntityId("INSTITUTION");
            institution.setInstitutionId(id);
            institution.setInstitutionName(institutionName.toUpperCase());
            institution.setBroadcastHeader("::" + institutionName.toUpperCase() + ":: ");
            institutiionRepository.save(institution);
            return ResponseEntity.ok(id);
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body("Institution already exist");
    }

    public ResponseEntity<?> saveGradingCriteria(GradingCriteria gradingCriteria, String staffId) {
        String logData = "Lower Range: " + gradingCriteria.getLowerRange() + " Higher Range: " + gradingCriteria.getUpperRange()
                + " Grade: " + gradingCriteria.getGrade() + " Description: " + gradingCriteria.getGradeDescription();

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            loggingService.logGeneralActivity(LogType.GRADE, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
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
            loggingService.logGeneralActivity(LogType.GRADE, LogAction.CREATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Criteria already exist"
            ));
        }

        ResponseEntity<?> invalidBand = checkBand(gradingCriteria, null,
                staff.getInstitution().getInstitutionId(), LogAction.CREATE, logData, staffId);
        if (invalidBand != null) {
            return invalidBand;
        }

        GradeSystem gradeSystem = new GradeSystem();
        gradeSystem.setLowerRange(gradingCriteria.getLowerRange());
        gradeSystem.setUpperRange(gradingCriteria.getUpperRange());
        gradeSystem.setGrade(gradingCriteria.getGrade());
        gradeSystem.setGradeDescription(gradingCriteria.getGradeDescription());
        gradeSystem.setInstitution(staff.getInstitution());
        gradeSystemRepository.save(gradeSystem);
        utilityClass.evictGradeCache(staff.getInstitution().getInstitutionId());

        loggingService.logGeneralActivity(LogType.GRADE, LogAction.CREATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();

    }

    public ResponseEntity<?> loadAllGradingCriteria(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            loggingService.logGeneralActivity(LogType.GRADE, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Staff Id"
            ));
        }

        List<GradeSystem> gradingList =
                gradeSystemRepository.findAllByInstitution_InstitutionId(staff.getInstitution().getInstitutionId());
        if(gradingList == null || gradingList.isEmpty()){
            loggingService.logGeneralActivity(LogType.GRADE, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
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

        loggingService.logGeneralActivity(LogType.GRADE, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(gradingCriteriaList);
    }

    public ResponseEntity<?> updateGradingCriteria(GradingCriteria gradingCriteria, String staffId) {

        String logData = "Lower Range: " + gradingCriteria.getLowerRange() + " Higher Range: " + gradingCriteria.getUpperRange()
                + " Grade: " + gradingCriteria.getGrade() + " Description: " + gradingCriteria.getGradeDescription();

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        GradeSystem criteria = gradeSystemRepository.findById(gradingCriteria.getId()).orElse(null);

        // Criteria IDs are sequential, so the lookup must also confirm the row belongs
        // to the caller's institution; otherwise one school could edit another's bands.
        // Both cases return the same 404 so the response doesn't reveal other tenants' IDs.
        boolean belongsToCaller = staff != null && criteria != null
                && criteria.getInstitution() != null
                && criteria.getInstitution().getInstitutionId()
                        .equals(staff.getInstitution().getInstitutionId());
        if (!belongsToCaller) {
            loggingService.logGeneralActivity(LogType.GRADE, LogAction.UPDATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Criteria do not exist on system"
            ));
        }

        String institutionId = criteria.getInstitution().getInstitutionId();

        // Renaming a band to another band's grade would leave two bands with one grade.
        GradeSystem sameGrade = gradeSystemRepository
                .findByGradeAndInstitution_InstitutionId(gradingCriteria.getGrade(), institutionId)
                .orElse(null);
        if (sameGrade != null && sameGrade.getId() != criteria.getId()) {
            loggingService.logGeneralActivity(LogType.GRADE, LogAction.UPDATE, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Criteria already exist"
            ));
        }

        ResponseEntity<?> invalidBand = checkBand(gradingCriteria, criteria.getId(),
                institutionId, LogAction.UPDATE, logData, staffId);
        if (invalidBand != null) {
            return invalidBand;
        }

        criteria.setLowerRange(gradingCriteria.getLowerRange());
        criteria.setUpperRange(gradingCriteria.getUpperRange());
        criteria.setGrade(gradingCriteria.getGrade());
        criteria.setGradeDescription(gradingCriteria.getGradeDescription());
        gradeSystemRepository.save(criteria);
        utilityClass.evictGradeCache(criteria.getInstitution().getInstitutionId());

        loggingService.logGeneralActivity(LogType.GRADE, LogAction.UPDATE, logData, staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    /*
     * A band must have lower <= upper within 0-100 (400) and must not overlap another band of
     * the school (409); sharing one end point is allowed (GradeBands). Overlapping or reversed
     * bands made the grade a score got depend on the order bands were loaded.
     */
    private ResponseEntity<?> checkBand(GradingCriteria band, Integer ignoreId, String institutionId,
                                        LogAction action, String logData, String staffId) {
        String rangeProblem = GradeBands.rangeProblem(band.getLowerRange(), band.getUpperRange());
        if (rangeProblem != null) {
            loggingService.logGeneralActivity(LogType.GRADE, action, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.badRequest().body(Map.of("message", rangeProblem));
        }

        GradeSystem clash = GradeBands.overlapping(band.getLowerRange(), band.getUpperRange(),
                gradeSystemRepository.findAllByInstitution_InstitutionId(institutionId), ignoreId);
        if (clash != null) {
            loggingService.logGeneralActivity(LogType.GRADE, action, logData, staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Range overlaps grade " + clash.getGrade() + " ("
                            + clash.getLowerRange() + " - " + clash.getUpperRange() + ")"
            ));
        }
        return null;
    }
}
