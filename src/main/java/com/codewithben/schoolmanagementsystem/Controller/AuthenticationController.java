package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Institution.InstitutionRegistrationDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.EnrollNewStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StaffLoginDTO;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Service.InstitutionService;
import com.codewithben.schoolmanagementsystem.Service.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final InstitutionService institutionService;

    private final StaffService staffService;

    private final InstitutiionRepository institutiionRepository;

    private final String subscriptionCode = "SC1547863";

    public AuthenticationController(InstitutionService institutionService, StaffService staffService,
                                    InstitutiionRepository institutiionRepository) {
        this.institutionService = institutionService;
        this.staffService = staffService;
        this.institutiionRepository = institutiionRepository;
    }

    @PostMapping("/school-subscription")
    public ResponseEntity<?> schoolSubscription(@RequestBody InstitutionRegistrationDTO institutionRegistrationDTO) {
        if (!institutionRegistrationDTO.getSubscriptionCode().equals(subscriptionCode))
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", "Invalid subscriptionCode"
            ));

        try {
            String response = institutionService.addNewInstitution(institutionRegistrationDTO.getInstitutionName());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", response
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/enroll-new-staff")
    public ResponseEntity<?> enrollNewStaff(@RequestBody EnrollNewStaffDTO enrollNewStaffDTO) {
        String firstName = enrollNewStaffDTO.getFirstName();
        String lastName = enrollNewStaffDTO.getLastName();
        String gender = enrollNewStaffDTO.getGender();
        LocalDate dateOfBirth = enrollNewStaffDTO.getDateOfBirth();
        String email = enrollNewStaffDTO.getEmail();
        String password = enrollNewStaffDTO.getPassword();
        String phoneNumber = enrollNewStaffDTO.getPhoneNumber();
        String status = enrollNewStaffDTO.getStatus();
        String institutionID = enrollNewStaffDTO.getInstitutionId();

        Institution institution = institutiionRepository.findByInstitutionId(institutionID).orElse(null);
        if (institution == null) {
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", "Institution not found"
            ));
        }

        try {
            //Response contains just the staff Id
            String response = staffService.addNewStaff(firstName, lastName, gender, dateOfBirth, institutionID,
                    email, password, phoneNumber, status);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", response
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/staff-login")
    public ResponseEntity<?> staffAuthentication(@RequestBody StaffLoginDTO staffLoginDTO) {
        String staffId = staffLoginDTO.getStaffId();
        String password = staffLoginDTO.getPassword();

        try {
            String response = staffService.staffAuthentication(staffId, password);
            String[] responseSplit = response.split("_");
            String role = "";
            String institutionName = "";
            String staffFullName = "";
            String returnStaffId = "";
            if (responseSplit.length == 4) {
                role = responseSplit[0];
                institutionName = responseSplit[1];
                staffFullName = responseSplit[2];
                returnStaffId = responseSplit[3];
            }

            if (role.equals("Principal"))
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "role", role,
                        "institutionName", institutionName,
                        "staffFullName", staffFullName,
                        "returnStaffId", returnStaffId
                ));
            if (role.equals("Accountant"))
                return ResponseEntity.ok(Map.of(
                         "status", "success",
                         "role", role,
                         "institutionName", institutionName,
                         "staffFullName", staffFullName,
                        "returnStaffId", returnStaffId
                ));



            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "role", role,
                    "institutionName", institutionName,
                    "staffFullName", staffFullName,
                    "returnStaffId", returnStaffId
            ));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", e.getMessage()
            ));
        }

    }
}
