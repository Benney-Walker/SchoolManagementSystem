package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.StaffRoles;
import com.codewithben.schoolmanagementsystem.Contants.StaffStatus;
import com.codewithben.schoolmanagementsystem.DTO.Academics.PrintLevelSubjects;
import com.codewithben.schoolmanagementsystem.DTO.Academics.SubjectDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.ViewStaffList;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StaffCaching;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class StaffService {
    private final StaffsRepository staffsRepository;

    private final SubjectsRepository subjectsRepository;

    private final LevelRepository levelRepository;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutionRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final StaffRolesEntityRepo staffRolesEntityRepo;

    private final LoggingService loggingService;

    public StaffService(StaffsRepository staffsRepository, SubjectsRepository subjectsRepository, LevelRepository levelRepository,
                        UtilityClass utilityClass, StaffRolesEntityRepo staffRolesEntityRepo,
                        InstitutiionRepository institutionRepository,  BCryptPasswordEncoder bCryptPasswordEncoder, LoggingService loggingService) {
        this.staffsRepository = staffsRepository;
        this.subjectsRepository = subjectsRepository;
        this.levelRepository = levelRepository;
        this.utilityClass = utilityClass;
        this.institutionRepository = institutionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.staffRolesEntityRepo = staffRolesEntityRepo;
        this.loggingService = loggingService;
    }

    public String addNewStaff(String firstName, String surName, String gender, String dateOfBirth,
                              String institutionId, String email, String password, String phoneNumber,
                              List<String> staffRoles) throws Exception {

        Institution institution = institutionRepository.findByInstitutionId(institutionId).orElse(null);
        if (institution == null) {
            throw new Exception("invalid institution ID");
        }

        if (staffsRepository.existsByPhoneNumberAndInstitution_InstitutionId(phoneNumber, institutionId)) {
            throw new Exception("Phone number already in use");
        }

        String staffID = utilityClass.generateEntityId("STAFF");
        Staffs staff = new Staffs();
        staff.setStaffId(staffID);
        staff.setFirstName(firstName);
        staff.setLastName(surName);
        staff.setGender(gender);
        staff.setStatus("NULL");
        staff.setDateOfBirth(LocalDate.parse(dateOfBirth));
        staff.setEmail(email);

        String hashedPassword = bCryptPasswordEncoder.encode(password);
        staff.setPassword(hashedPassword);
        staff.setPhoneNumber(phoneNumber);
        staff.setDateOfRegistration(LocalDate.now());
        staff.setStaffStatus(StaffStatus.ACTIVE);
        staff.setInstitution(institution);
        staffsRepository.save(staff);

        staff.setRoles(saveStaffRoles(staff, staffRoles));
        staffsRepository.save(staff);

        return staffID;
    }

    private List<StaffRolesEntity> saveStaffRoles(Staffs staff, List<String> staffRoles) throws Exception {

        List<StaffRolesEntity> roles = new ArrayList<>();
        for (String staffRole : staffRoles) {
            StaffRolesEntity findStaff = new StaffRolesEntity();
            findStaff.setStaff(staff);
            findStaff.setStaffRole(StaffRoles.valueOf(staffRole));
            staffRolesEntityRepo.save(findStaff);
            roles.add(findStaff);
        }

        return roles;
    }

    public FindStaffDTO findStaffById(String id) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(id)
                .orElseThrow(() -> new Exception("Invalid staff ID"));

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

        return staffData;
    }

    public ResponseEntity<?> updateStaffInfo(FindStaffDTO updateInfo, String staffId) {
        String logData = "staff Id: " + updateInfo.getStaffId() + " first Name: " + updateInfo.getFirstName() +
                " Surname: " + updateInfo.getSurname() + " gender: " + updateInfo.getGender() + " DOB: " + updateInfo.getDateOfBirth() +
                " Email: " + updateInfo.getEmail() + " Contact: " + updateInfo.getPhoneNumber() +
                " Roles: " + Arrays.toString(updateInfo.getStaffRoles().toArray()) + " Status: " + updateInfo.getStaffStatus() +
                " DOR: " + updateInfo.getDateOfRegistration();

        Staffs staff = staffsRepository.findByStaffId(updateInfo.getStaffId()).orElse(null);
        if (staff == null) {
            loggingService.logActivity("UPDATE_STAFF_DETAILS", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Staff Id");
        }

        staff.setFirstName(updateInfo.getFirstName());
        staff.setLastName(updateInfo.getSurname());
        staff.setGender(updateInfo.getGender());
        staff.setDateOfBirth(LocalDate.parse(updateInfo.getDateOfBirth()));
        staff.setEmail(updateInfo.getEmail());
        staff.setPhoneNumber(updateInfo.getPhoneNumber());

        List<String> newRoles = updateInfo.getStaffRoles();

        staff.setRoles(updateStaffRoles(staff, newRoles));
        staff.setStaffStatus(StaffStatus.valueOf(updateInfo.getStaffStatus()));
        staffsRepository.save(staff);

        loggingService.logActivity("UPDATE_STAFF_DETAILS", logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    private List<StaffRolesEntity> updateStaffRoles(Staffs staff, List<String> staffRoles) {
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
    }

    public long countTotalStaffs(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow( () -> new Exception("Staff not found"));

        Institution institution = staff.getInstitution();
        if (institution == null) {
            throw new Exception("institution not found");
        }

        List<Staffs> staffs = institution.getStaff();
        if (staffs == null || staffs.isEmpty()) {
            throw new Exception("Institution has no staff");
        }

        return staffs.size();
    }

    public long countTotalTeachingStaffs(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow( () -> new Exception("Staff not found"));

        Institution institution = staff.getInstitution();
        if (institution == null) {
            throw new Exception("institution not found");
        }

        List<Staffs> staffs = institution.getStaff();
        if (staffs == null || staffs.isEmpty()) {
            throw new Exception("Institution has no staff");
        }

        int staffCount = 0;
        for (Staffs teachingStaff : staffs) {
            List<StaffRolesEntity> staffRoles = teachingStaff.getRoles();
            if (staffRoles.stream().anyMatch(staffRole -> staffRole.getStaffRole().equals(StaffRoles.TEACHING_STAFF))) {
                staffCount++;
            }
        }

        return staffCount;
    }


    public ResponseEntity<?> addNewSubjects(String subjectName, String levelId, String staffId) {
        String logData = "Subject Name: " + subjectName + " Class Id: " + levelId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity("NEW_SUBJECT", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Selected class do not exist");
        }

        Subjects  subject = subjectsRepository.findBySubjectNameAndLevel_LevelID(subjectName, levelId).orElse(null);
        if (subject != null) {
            loggingService.logActivity("NEW_SUBJECT", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Subject already exist");
        }

        Subjects subjects = new Subjects();
        String subjectId = utilityClass.generateEntityId("SUBJECT");
        subjects.setSubjectId(subjectId);
        subjects.setSubjectName(subjectName.toUpperCase());
        subjects.setLevel(level);
        subjectsRepository.saveAndFlush(subjects);

        //Add subject to Level Subjects
        List<Subjects> levelSubjects = level.getSubjects();
        if (levelSubjects == null) {
            levelSubjects = new ArrayList<>();
        }
        levelSubjects.add(subjects);
        level.setSubjects(levelSubjects);
        levelRepository.save(level);

        loggingService.logActivity("NEW_SUBJECT", logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadSubjectData(String subjectId, String staffId) {
        String logData = "Subject Id: " + subjectId;

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logActivity("FIND_SUBJECT_DATA", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Subject not found");
        }

        loggingService.logActivity("FIND_SUBJECT_DATA", logData, staffId, "SUCCESS");
        return ResponseEntity.ok( new SubjectDTO(
                        subject.getSubjectId(),
                        subject.getSubjectName(),
                        subject.getLevel().getLevelID()
                ));
    }

    public ResponseEntity<?> updateSubjectData(SubjectDTO subjectDTO, String staffId) {
        String logData = "Subject Id: " + subjectDTO.getSubjectId() + " Subject Name: " + subjectDTO.getSubjectName() +
                " Class Id: " + subjectDTO.getLevelId();

        Subjects subject = subjectsRepository.findBySubjectId(subjectDTO.getSubjectId()).orElse(null);
        if (subject == null) {
            loggingService.logActivity("UPDATE_SUBJECT_DATA", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Subject not found");
        }

        Level level = levelRepository.findByLevelID(subjectDTO.getLevelId()).orElse(null);
        if (level == null) {
            loggingService.logActivity("UPDATE_SUBJECT_DATA", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grade not found");
        }

        subject.setSubjectName(subjectDTO.getSubjectName());
        subject.setLevel(level);
        subjectsRepository.save(subject);

        loggingService.logActivity("UPDATE_SUBJECT_DATA", logData, staffId, "SUCCESS");
        return ResponseEntity.ok("Subject updated successfully");
    }

    public ResponseEntity<?> deleteSubjectData(String subjectId, String staffId) {
        String logData = "Subject Id: " + subjectId;

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logActivity("DELETE_SUBJECT", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Subject not found");
        }

        subjectsRepository.delete(subject);
        loggingService.logActivity("DELETE_SUBJECT", logData, staffId, "FAILED");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> resetStaffPassword(String newPasswordStaffId, String newPassword, String staffId) {
        String logData = "staff Id: " + newPasswordStaffId + " New Password: " + "***********";
        Staffs staff = staffsRepository.findByStaffId(newPasswordStaffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity("PASSWORD_RESET", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staffId");
        }

        if (bCryptPasswordEncoder.matches(newPassword, staff.getPassword())) {
            loggingService.logActivity("PASSWORD_RESET", logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("You can't use old password");
        }

        staff.setPassword(bCryptPasswordEncoder.encode(newPassword));
        staffsRepository.save(staff);

        loggingService.logActivity("PASSWORD_RESET", logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public List<StaffCaching> loadAllStaffInfo(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);

        Institution institution = staff.getInstitution();

        List<Staffs> staffs = institution.getStaff();
        if (staffs == null || staffs.isEmpty()) {
            return Collections.emptyList();
        }

        List<StaffCaching> staffList = new ArrayList<>();
        for (Staffs staffMember : staffs) {
            List<String> roles = new ArrayList<>();
            List<StaffRolesEntity> staffRoles = staffMember.getRoles();
            for (StaffRolesEntity staffRole : staffRoles) {
                roles.add(staffRole.getStaffRole().name());
            }

            StaffCaching foundStaff = new StaffCaching(
                    staffMember.getFirstName() + " " + staffMember.getLastName(),
                    staffMember.getStaffId(),
                    roles
            );

            staffList.add(foundStaff);
        }

        return getFinalStaffList(staffList);
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

    public List<ViewStaffList> loadAllStaffList(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);

        Institution institution = staff.getInstitution();

        List<Staffs> staffs = institution.getStaff();
        if (staffs == null || staffs.isEmpty()) {
            return Collections.emptyList();
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
        return viewStaffLists;
    }

    public List<PrintLevelSubjects> printLevelSubjects(String levelId) throws Exception {
        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Level not found"));

        List<Subjects> subjects = level.getSubjects();
        if (subjects == null || subjects.isEmpty()) {
            throw new Exception("Level has no subjects added");
        }

        List<PrintLevelSubjects> printLevelSubjects = new ArrayList<>();
        for (Subjects subject:subjects) {
            PrintLevelSubjects printLevelSubjects1 = new PrintLevelSubjects(
                    subject.getSubjectName(),
                    subject.getSubjectId()
            );
            printLevelSubjects.add(printLevelSubjects1);
        }
        return printLevelSubjects;
    }

    public Staffs getStaffDetails(String staffId) {
        return staffsRepository.findByStaffId(staffId).orElse(null);
    }

    public Staffs getValidatedStaff(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity("FETCH_PAYMENTS_REPORT", "N/A", staffId, "FAILED");
            return null;
        }
        return staff;
    }
}
