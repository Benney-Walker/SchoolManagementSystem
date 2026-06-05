package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.*;
import com.codewithben.schoolmanagementsystem.DTO.Staff.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Staff.ViewStaffList;
import com.codewithben.schoolmanagementsystem.DTO.Staff.StaffCaching;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@AllArgsConstructor
@Service
public class StaffService {
    private final StaffsRepository staffsRepository;

    private final InstitutiionRepository institutiionRepository;

    private final UtilityClass utilityClass;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final StaffRolesEntityRepo staffRolesEntityRepo;

    private final LoggingService loggingService;


    public ResponseEntity<?> addNewStaff(String staffId, String firstName, String surName, String gender, String dateOfBirth,
                                         String email, String password, String phoneNumber,
                                         List<String> staffRoles) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.CREATE, "Invalid newStaff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Invalid newStaff Id"
            ));
        }


        if (staffsRepository.existsByPhoneNumberAndInstitution_InstitutionId(phoneNumber, staff.getInstitution().getInstitutionId())) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.CREATE, "Phone number already exist", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Phone number already exist"
            ));
        }

        if (staffsRepository.existsByFirstNameAndLastName(firstName, surName)) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.CREATE, "Staff already exist", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Staff already exist"
            ));
        }

        String staffID = utilityClass.generateEntityId("STAFF");
        Staffs newStaff = new Staffs();
        newStaff.setStaffId(staffID);
        newStaff.setFirstName(firstName.toUpperCase());
        newStaff.setLastName(surName.toUpperCase());
        newStaff.setGender(gender.toUpperCase());
        newStaff.setStatus("NULL");
        newStaff.setDateOfBirth(LocalDate.parse(dateOfBirth));
        newStaff.setEmail(email);

        String hashedPassword = bCryptPasswordEncoder.encode(password);
        newStaff.setPassword(hashedPassword);
        newStaff.setPhoneNumber(phoneNumber);
        newStaff.setDateOfRegistration(LocalDate.now());
        newStaff.setStaffStatus(StaffStatus.ACTIVE);
        newStaff.setInstitution(staff.getInstitution());
        staffsRepository.save(newStaff);

        newStaff.setRoles(saveStaffRoles(newStaff, staffRoles));
        staffsRepository.save(newStaff);

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.CREATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(staffID);
    }

    public ResponseEntity<?> addNewPrincipal(Institution institution, String firstName, String surName, String gender, String dateOfBirth,
                                         String email, String password, String phoneNumber,
                                         List<String> staffRoles) {


        if (staffsRepository.existsByPhoneNumberAndInstitution_InstitutionId(phoneNumber, institution.getInstitutionId())) {
            loggingService.logNewSubscription(LogType.STAFF, LogAction.CREATE, "Phone number already exist", LogStatus.FAILED, institution);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Phone number already exist"
            ));
        }

        if (staffsRepository.existsByFirstNameAndLastName(firstName, surName)) {
            loggingService.logNewSubscription(LogType.STAFF, LogAction.CREATE, "Staff already exist", LogStatus.FAILED, institution);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Staff already exist"
            ));
        }

        String staffID = utilityClass.generateEntityId("STAFF");
        Staffs newPrincipal = new Staffs();
        newPrincipal.setStaffId(staffID);
        newPrincipal.setFirstName(firstName.toUpperCase());
        newPrincipal.setLastName(surName.toUpperCase());
        newPrincipal.setGender(gender.toUpperCase());
        newPrincipal.setStatus("NULL");
        newPrincipal.setDateOfBirth(LocalDate.parse(dateOfBirth));
        newPrincipal.setEmail(email);

        String hashedPassword = bCryptPasswordEncoder.encode(password);
        newPrincipal.setPassword(hashedPassword);
        newPrincipal.setPhoneNumber(phoneNumber);
        newPrincipal.setDateOfRegistration(LocalDate.now());
        newPrincipal.setStaffStatus(StaffStatus.ACTIVE);
        newPrincipal.setInstitution(institution);
        staffsRepository.save(newPrincipal);

        newPrincipal.setRoles(saveStaffRoles(newPrincipal, staffRoles));
        staffsRepository.save(newPrincipal);

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.CREATE, "New Subscription", "", LogStatus.SUCCESS);
        return ResponseEntity.ok(staffID);
    }

    private List<StaffRolesEntity> saveStaffRoles(Staffs staff, List<String> staffRoles) {

        try {
            List<StaffRolesEntity> roles = new ArrayList<>();
            for (String staffRole : staffRoles) {
                StaffRolesEntity findStaff = new StaffRolesEntity();
                findStaff.setStaff(staff);
                findStaff.setStaffRole(StaffRoles.valueOf(staffRole));
                staffRolesEntityRepo.save(findStaff);
                roles.add(findStaff);
            }

            return roles;
        } catch (IllegalArgumentException e) {
            e.getMessage();
            return null;
        }
    }

    public ResponseEntity<?> findStaffById(String instructorId, String staffId) {
        String logData = "Staff Id: " + instructorId;

        Staffs staff = staffsRepository.findByStaffId(instructorId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");
        }

        FindStaffDTO staffData = new FindStaffDTO();
        staffData.setStaffId(staff.getStaffId());
        staffData.setFirstName(staff.getFirstName());
        staffData.setSurname(staff.getLastName());
        staffData.setGender(staff.getGender());
        staffData.setDateOfBirth(String.valueOf(staff.getDateOfBirth()));
        staffData.setEmail(staff.getEmail());
        staffData.setPhoneNumber(staff.getPhoneNumber());

        List<String> roles = new ArrayList<>();

        if (staff.getRoles() != null && !staff.getRoles().isEmpty()) {
            for (StaffRolesEntity staffRole : staff.getRoles()) {
                roles.add(staffRole.getStaffRole().name());
            }
        }

        staffData.setStaffRoles(roles);
        staffData.setStaffStatus(staff.getStaffStatus().name());
        staffData.setDateOfRegistration(staff.getDateOfRegistration().toString());

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(staffData);
    }

    public ResponseEntity<?> updateStaffInfo(FindStaffDTO updateInfo, String staffId) {
        String logData = "staff Id: " + updateInfo.getStaffId() + " first Name: " + updateInfo.getFirstName() +
                " Surname: " + updateInfo.getSurname() + " gender: " + updateInfo.getGender() + " DOB: " + updateInfo.getDateOfBirth() +
                " Email: " + updateInfo.getEmail() + " Contact: " + updateInfo.getPhoneNumber() +
                " Roles: " + Arrays.toString(updateInfo.getStaffRoles().toArray()) + " Status: " + updateInfo.getStaffStatus() +
                " DOR: " + updateInfo.getDateOfRegistration();

        Staffs staff = staffsRepository.findByStaffId(updateInfo.getStaffId()).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.UPDATE, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Staff Id");
        }

        staff.setFirstName(updateInfo.getFirstName().toUpperCase());
        staff.setLastName(updateInfo.getSurname().toUpperCase());
        staff.setGender(updateInfo.getGender().toUpperCase());
        staff.setDateOfBirth(LocalDate.parse(updateInfo.getDateOfBirth()));
        staff.setEmail(updateInfo.getEmail());
        staff.setPhoneNumber(updateInfo.getPhoneNumber());

        List<String> newRoles = updateInfo.getStaffRoles();

        staff.setRoles(updateStaffRoles(staff, newRoles));
        staff.setStaffStatus(StaffStatus.valueOf(updateInfo.getStaffStatus()));
        staffsRepository.save(staff);

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    private List<StaffRolesEntity> updateStaffRoles(Staffs staff, List<String> staffRoles) {
        try {
            List<StaffRolesEntity> existingRoles = staffRolesEntityRepo.findByStaff_StaffId(staff.getStaffId());
            if (existingRoles != null && !existingRoles.isEmpty()) {
                staffRolesEntityRepo.deleteAll(existingRoles);
            }

            List<StaffRolesEntity> newRoles = new ArrayList<>();
            for (String staffRole : staffRoles) {
                StaffRolesEntity findStaff = new StaffRolesEntity();
                findStaff.setStaff(staff);
                findStaff.setStaffRole(StaffRoles.valueOf(staffRole));
                staffRolesEntityRepo.save(findStaff);

                newRoles.add(findStaff);
            }

            return newRoles;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> countTotalStaffs(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Staff Id"
            ));
        }

        List<Staffs> staffs = getActiveStaff(staff.getInstitution());
        if (staffs == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "Institution has no staff", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no staff"
            ));
        }

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(staffs.size());
    }

    public ResponseEntity<?> countTotalTeachingStaffs(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "Invalid Staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message",  "Invalid Staff Id"
            ));
        }

        List<Staffs> staffs = getActiveStaff(staff.getInstitution());
        if (staffs == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "Institution has no staff", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message",  "Institution has no staff"
            ));
        }

        int staffCount = 0;
        for (Staffs teachingStaff : staffs) {
            List<StaffRolesEntity> staffRoles = teachingStaff.getRoles();
            if (staffRoles.stream().anyMatch(staffRole -> staffRole.getStaffRole().equals(StaffRoles.TEACHING_STAFF))) {
                staffCount++;
            }
        }

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(staffCount);
    }

    public ResponseEntity<?> resetStaffPassword(String newPasswordStaffId, String newPassword, String staffId) {
        String logData = "staff Id: " + newPasswordStaffId + " New Password: " + "***********";

        Staffs staff = staffsRepository.findByStaffId(newPasswordStaffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.RESET, "Invalid Staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Staff Id"
            ));
        }

        if (bCryptPasswordEncoder.matches(newPassword, staff.getPassword())) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.RESET, "You can't use old password", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "You can't use old password"
            ));
        }

        staff.setPassword(bCryptPasswordEncoder.encode(newPassword));
        staffsRepository.save(staff);

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.RESET, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadAllStaffInfo(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Staffs> staffs = staff.getInstitution().getStaff();

        List<StaffCaching> staffList = new ArrayList<>();
        for (Staffs staffMember : staffs) {
            List<String> roles = new ArrayList<>();
            List<StaffRolesEntity> staffRoles = staffMember.getRoles();
            for (StaffRolesEntity staffRole : staffRoles) {
                roles.add(staffRole.getStaffRole().name());
            }

            StaffCaching foundStaff = StaffCaching.builder()
                    .staffName(
                            staffMember.getFirstName() + " " + staffMember.getLastName()
                    )
                    .staffId(staffMember.getStaffId())
                    .staffRoles(roles)
                    .build();


            staffList.add(foundStaff);
        }

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(getFinalStaffList(staffList));
    }

    private List<StaffCaching> getFinalStaffList(List<StaffCaching> staffList) {
        List<StaffCaching> finalStaffList = new ArrayList<>();
        if (staffList == null || staffList.isEmpty()) {
            return Collections.emptyList();
        }

        for (StaffCaching staffCaching : staffList) {
            if (!staffCaching.getStaffRoles().contains("GENERAL_STAFF")) {
                finalStaffList.add(staffCaching);
            }
        }
        return finalStaffList;
    }

    public ResponseEntity<?> loadAllStaffList(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Staffs> staffs = staff.getInstitution().getStaff();
        if (staffs == null || staffs.isEmpty()) {
            loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "Institution has no staff", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no staff"
            ));
        }

        List<ViewStaffList> viewStaffLists = new ArrayList<>();

        for(Staffs staffMember: staffs){
            String staff_Id = "";
            String staff_Name = "";
            List<String> staff_Roles = new ArrayList<>();

            staff_Id = staffMember.getStaffId();
            staff_Name = staffMember.getFirstName() + " " + staffMember.getLastName();

            List<StaffRolesEntity> staffRoles = staffMember.getRoles();
                for (StaffRolesEntity staffRole : staffRoles) {
                    staff_Roles.add(staffRole.getStaffRole().name());
                }

            ViewStaffList newStaff =  new ViewStaffList(
                    staff_Id, staff_Name, staff_Roles
            );
            viewStaffLists.add(newStaff);
        }

        loggingService.logGeneralActivity(LogType.STAFF, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(viewStaffLists);
    }

    public Staffs getStaffDetails(String staffId) {
        return staffsRepository.findByStaffId(staffId).orElse(null);
    }

    private List<Staffs> getActiveStaff(Institution institution) {
        List<Staffs> staffList = institution.getStaff();
        if (staffList == null || staffList.isEmpty()) {
            return null;
        }

        List<Staffs> activeStaffList = new ArrayList<>();
        //Retrieve active staffs
        for (Staffs staff : staffList) {

            if (staff.getStaffStatus().equals(StaffStatus.ACTIVE)) {
                activeStaffList.add(staff);
            }
        }

        return activeStaffList;
    }
}
