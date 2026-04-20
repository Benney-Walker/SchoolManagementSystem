package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Institution.InstitutionRegistrationDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.EnrollNewStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.LoginRequest;
import com.codewithben.schoolmanagementsystem.DTO.Institution.LoginResponse;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Service.InstitutionService;
import com.codewithben.schoolmanagementsystem.Service.LoggingService;
import com.codewithben.schoolmanagementsystem.Service.StaffService;
import com.codewithben.schoolmanagementsystem.Utility.JwtUtility;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final InstitutionService institutionService;

    private final StaffService staffService;

    private final InstitutiionRepository institutiionRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtility jwtUtility;

    private final String subscriptionCode = "SC1547863";

    private final LoggingService loggingService;

    public AuthenticationController(InstitutionService institutionService, StaffService staffService,
                                    InstitutiionRepository institutiionRepository, AuthenticationManager authenticationManager,
                                    JwtUtility jwtUtility, LoggingService loggingService) {
        this.institutionService = institutionService;
        this.staffService = staffService;
        this.institutiionRepository = institutiionRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtility = jwtUtility;
        this.loggingService = loggingService;
    }

    @PostMapping("/v1/school-subscription")
    public ResponseEntity<?> schoolSubscription(@RequestBody InstitutionRegistrationDTO data) {
        String logData = "Institution Name: " + data.getInstitutionName() + " Subscription code: " + "N/A";

        if (!data.getSubscriptionCode().equals(subscriptionCode)) {

            loggingService.logActivity(LogType.SUBSCRIPTION, logData, "N/A", "FAILED");
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", "Invalid subscriptionCode"
            ));
        }

        return institutionService.addNewInstitution(data.getInstitutionName(), logData);
    }

    @PostMapping("/v1/enroll-new-staff")
    public ResponseEntity<?> enrollNewStaff(@RequestBody EnrollNewStaffDTO enrollNewStaffDTO) {
        String firstName = enrollNewStaffDTO.getFirstName();
        String lastName = enrollNewStaffDTO.getLastName();
        String gender = enrollNewStaffDTO.getGender();
        String dateOfBirth = enrollNewStaffDTO.getDateOfBirth();
        String email = enrollNewStaffDTO.getEmail();
        String password = enrollNewStaffDTO.getPassword();
        String phoneNumber = enrollNewStaffDTO.getPhoneNumber();
        List<String> role = enrollNewStaffDTO.getRoles();
        String institutionID = enrollNewStaffDTO.getInstitutionId();

        String logData = "First Name: " + firstName +
                ", Last Name: " + lastName +
                ", Gender: " + gender +
                ", DOB: " + dateOfBirth +
                ", Email: " + email +
                ", Phone: " + phoneNumber +
                ", Roles: " + role.toString() +
                ", Institution ID: " + institutionID;

        Institution institution = institutiionRepository.findByInstitutionId(institutionID).orElse(null);
        if (institution == null) {

            loggingService.logActivity(LogType.STAFF_ENROLLMENT, logData, "N/A", "FAILED");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid institution Id");
        }

        return staffService.addNewStaff(firstName, lastName, gender, dateOfBirth, institutionID,
                    email, password, phoneNumber, role, institution, logData);
    }

    @PostMapping("/v1/staff-login")
    public ResponseEntity<?> staffAuthentication(@RequestBody LoginRequest loginRequest) {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getStaffId(),
                            loginRequest.getPassword()
                    )
            );

            Staffs staff = staffService.getStaffDetails(loginRequest.getStaffId());

            List<String> rolesList = staff.getRoles().stream().map(
                    role -> role.getStaffRole().name()).toList();


            String token = jwtUtility.generateToken(
                    loginRequest.getStaffId(),
                    rolesList
            );

            return ResponseEntity.ok(
                    new LoginResponse(
                            staff.getStaffId(),
                            staff.getFirstName() + " " + staff.getLastName(),
                            rolesList,
                            staff.getInstitution().getInstitutionName(),
                            token
                    )
            );

    }
}
