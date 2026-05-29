package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogType;
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
import java.util.*;

@Service
public class StaffService {
    private final StaffsRepository staffsRepository;

    private final SubjectsRepository subjectsRepository;

    private final LevelRepository levelRepository;

    private final UtilityClass utilityClass;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final StaffRolesEntityRepo staffRolesEntityRepo;

    private final LoggingService loggingService;

    public StaffService(StaffsRepository staffsRepository, SubjectsRepository subjectsRepository, LevelRepository levelRepository,
                        UtilityClass utilityClass, StaffRolesEntityRepo staffRolesEntityRepo,
                        BCryptPasswordEncoder bCryptPasswordEncoder, LoggingService loggingService) {
        this.staffsRepository = staffsRepository;
        this.subjectsRepository = subjectsRepository;
        this.levelRepository = levelRepository;
        this.utilityClass = utilityClass;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.staffRolesEntityRepo = staffRolesEntityRepo;
        this.loggingService = loggingService;
    }

    public ResponseEntity<?> addNewStaff(String firstName, String surName, String gender, String dateOfBirth,
                              String institutionId, String email, String password, String phoneNumber,
                              List<String> staffRoles, Institution institution, String logData) {

        if (staffsRepository.existsByPhoneNumberAndInstitution_InstitutionId(phoneNumber, institutionId)) {
            loggingService.logActivity(LogType.STAFF_ENROLLMENT, logData, "N/A", "FAILED");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Phone number already exist");
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
        loggingService.logActivity(LogType.STAFF_ENROLLMENT, logData, "N/A", "SUCCESS");
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
            loggingService.logActivity(LogType.FETCH_STAFF_DETAILS, logData, staffId, "FAILED");
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

        loggingService.logActivity(LogType.FETCH_STAFF_DETAILS, logData, staffId, "SUCCESS");
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
            loggingService.logActivity(LogType.UPDATE_STAFF_DETAILS, logData, staffId, "FAILED");
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

        loggingService.logActivity(LogType.UPDATE_STAFF_DETAILS, logData, staffId, "SUCCESS");
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

    public ResponseEntity<?> countTotalStaffs(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.COUNT_STAFFS, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");
        }

        List<Staffs> staffs = staff.getInstitution().getStaff();

        loggingService.logActivity(LogType.COUNT_STAFFS, "N/A", staffId, "SUCCESS");
        return ResponseEntity.ok(staffs.size());
    }

    public ResponseEntity<?> countTotalTeachingStaffs(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.COUNT_STAFFS, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");
        }

        List<Staffs> staffs = staff.getInstitution().getStaff();

        int staffCount = 0;
        for (Staffs teachingStaff : staffs) {
            List<StaffRolesEntity> staffRoles = teachingStaff.getRoles();
            if (staffRoles.stream().anyMatch(staffRole -> staffRole.getStaffRole().equals(StaffRoles.TEACHING_STAFF))) {
                staffCount++;
            }
        }

        loggingService.logActivity(LogType.COUNT_STAFFS, "N/A", staffId, "SUCCESS");
        return ResponseEntity.ok(staffCount);
    }


    public ResponseEntity<?> addNewSubjects(String subjectName, String levelId, String staffId) {
        String logData = "Subject Name: " + subjectName + " Class Id: " + levelId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.NEW_SUBJECT, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Class Id"
            ));
        }

        Subjects  subject = subjectsRepository.findBySubjectNameAndLevel_LevelID(subjectName, levelId).orElse(null);
        if (subject != null) {
            loggingService.logActivity(LogType.NEW_SUBJECT, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Subject already exists"
            ));
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

        loggingService.logActivity(LogType.NEW_SUBJECT, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadSubjectData(String subjectId, String staffId) {
        String logData = "Subject Id: " + subjectId;

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logActivity(LogType.FIND_SUBJECT_DATA, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Subject not found"
            ));
        }

        loggingService.logActivity(LogType.FIND_SUBJECT_DATA, logData, staffId, "SUCCESS");
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
            loggingService.logActivity(LogType.UPDATE_SUBJECT_DATA, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Subject not found"
            ));
        }

        Level level = levelRepository.findByLevelID(subjectDTO.getLevelId()).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.UPDATE_SUBJECT_DATA, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Level not found"
            ));
        }

        subject.setSubjectName(subjectDTO.getSubjectName());
        subject.setLevel(level);
        subjectsRepository.save(subject);

        loggingService.logActivity(LogType.UPDATE_SUBJECT_DATA, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> deleteSubjectData(String subjectId, String staffId) {
        String logData = "Subject Id: " + subjectId;

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logActivity(LogType.DELETE_SUBJECT, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Subject do not exist"
            ));
        }

        subjectsRepository.delete(subject);
        loggingService.logActivity(LogType.DELETE_SUBJECT, logData, staffId, "FAILED");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> resetStaffPassword(String newPasswordStaffId, String newPassword, String staffId) {
        String logData = "staff Id: " + newPasswordStaffId + " New Password: " + "***********";
        Staffs staff = staffsRepository.findByStaffId(newPasswordStaffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.PASSWORD_RESET, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staffId");
        }

        if (bCryptPasswordEncoder.matches(newPassword, staff.getPassword())) {
            loggingService.logActivity(LogType.PASSWORD_RESET, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("You can't use old password");
        }

        staff.setPassword(bCryptPasswordEncoder.encode(newPassword));
        staffsRepository.save(staff);

        loggingService.logActivity(LogType.PASSWORD_RESET, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadAllStaffInfo(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.STAFF_LIST, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load staff list");
        }

        List<Staffs> staffs = staff.getInstitution().getStaff();

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

        loggingService.logActivity(LogType.STAFF_LIST, "N/A", staffId, "SUCCESS");
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
            loggingService.logActivity(LogType.STAFF_LIST, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid staff Id");
        }

        List<Staffs> staffs = staff.getInstitution().getStaff();

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

        loggingService.logActivity(LogType.STAFF_LIST, "N/A", staffId, "SUCCESS");
        return ResponseEntity.ok(viewStaffLists);
    }

    public Staffs getStaffDetails(String staffId) {
        return staffsRepository.findByStaffId(staffId).orElse(null);
    }
}
