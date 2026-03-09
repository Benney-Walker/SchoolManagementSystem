package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.Contants.StaffRoles;
import com.codewithben.schoolmanagementsystem.DTO.Institution.InstitutionRegistrationDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.EnrollNewStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.LoginRequest;
import com.codewithben.schoolmanagementsystem.DTO.Institution.LoginResponse;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Service.InstitutionService;
import com.codewithben.schoolmanagementsystem.Service.StaffService;
import com.codewithben.schoolmanagementsystem.Utility.JwtUtility;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final InstitutionService institutionService;

    private final StaffService staffService;

    private final InstitutiionRepository institutiionRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtility jwtUtility;

    private final String subscriptionCode = "SC1547863";

    public AuthenticationController(InstitutionService institutionService, StaffService staffService,
                                    InstitutiionRepository institutiionRepository, AuthenticationManager authenticationManager,
                                    JwtUtility jwtUtility) {
        this.institutionService = institutionService;
        this.staffService = staffService;
        this.institutiionRepository = institutiionRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtility = jwtUtility;
    }

    @PostMapping("/v1/school-subscription")
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

    @PostMapping("/v1/enroll-new-staff")
    public ResponseEntity<?> enrollNewStaff(@RequestBody EnrollNewStaffDTO enrollNewStaffDTO) {
        String firstName = enrollNewStaffDTO.getFirstName();
        String lastName = enrollNewStaffDTO.getLastName();
        String gender = enrollNewStaffDTO.getGender();
        String dateOfBirth = enrollNewStaffDTO.getDateOfBirth();
        String email = enrollNewStaffDTO.getEmail();
        String password = enrollNewStaffDTO.getPassword();
        String phoneNumber = enrollNewStaffDTO.getPhoneNumber();
        String role = enrollNewStaffDTO.getRole();
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
                    email, password, phoneNumber, role);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", response
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

            List<String> rolesList = staff.getStaffRoles().stream()
                    .map(Enum::name)
                    .toList();

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
