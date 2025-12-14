package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.stereotype.Service;

@Service
public class InstitutionService {
    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutiionRepository;

    public InstitutionService(UtilityClass utilityClass, InstitutiionRepository institutiionRepository) {
        this.utilityClass = utilityClass;
        this.institutiionRepository = institutiionRepository;
    }

    public String addNewInstitution(String institutionName) throws Exception {

        Institution institution = new Institution();
        String id = utilityClass.generateEntityId("INSTITUTION");
        institution.setInstitutionId(id);
        institution.setInstitutionName(institutionName);
        institutiionRepository.save(institution);
        return id;
    }
}
