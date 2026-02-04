package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Academics.GradingCriteria;
import com.codewithben.schoolmanagementsystem.Entity.GradeSystem;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.GradeSystemRepository;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.stereotype.Service;

@Service
public class InstitutionService {
    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    private final GradeSystemRepository gradeSystemRepository;

    private final StaffsRepository staffsRepository;

    public InstitutionService(UtilityClass utilityClass, InstitutiionRepository institutiionRepository,
                              GradeSystemRepository gradeSystemRepository,  StaffsRepository staffsRepository) {
        this.utilityClass = utilityClass;
        this.institutiionRepository = institutiionRepository;
        this.gradeSystemRepository = gradeSystemRepository;
        this.staffsRepository = staffsRepository;
    }

    public String addNewInstitution(String institutionName) throws Exception {

        Institution institution = new Institution();
        String id = utilityClass.generateEntityId("INSTITUTION");
        institution.setInstitutionId(id);
        institution.setInstitutionName(institutionName);
        institutiionRepository.save(institution);
        return id;
    }

    public String setGradingCriteria(GradingCriteria gradingCriteria) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(gradingCriteria.getStaffId()).orElse(null);
        if(staff == null){
            throw new Exception("Staff not found");
        }
        Institution institution = staff.getInstitution();


        GradeSystem gradeSystem = new GradeSystem();
        gradeSystem.setLowerRange(gradingCriteria.getLowerRange());
        gradeSystem.setUpperRange(gradingCriteria.getUpperRange());
        gradeSystem.setGrade(gradingCriteria.getGrade());
        gradeSystem.setGradeDescription(gradingCriteria.getGradeDescription());
        gradeSystem.setInstitution(institution);
        gradeSystemRepository.save(gradeSystem);

        return "Grade Criteria added successfully";
    }
}
