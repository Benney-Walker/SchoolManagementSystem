package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.StaffRoles;
import com.codewithben.schoolmanagementsystem.Contants.StaffStatus;
import com.codewithben.schoolmanagementsystem.DTO.Academics.PrintLevelSubjects;
import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.ViewStaffList;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StaffCaching;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StaffService {
    private final StaffsRepository staffsRepository;

    private final StudentsRepository studentsRepository;

    private final ResultsRepository resultsRepository;

    private final SubjectsRepository subjectsRepository;

    private final SubjectScoreRepository subjectScoreRepository;

    private final SemesterRepository semesterRepository;

    private final LevelRepository levelRepository;

    private final UtilityClass utilityClass;

    private final InstitutiionRepository institutionRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final StaffRolesEntityRepo staffRolesEntityRepo;

    public StaffService(StaffsRepository staffsRepository, StudentsRepository studentsRepository, ResultsRepository resultsRepository,
                        SubjectsRepository subjectsRepository, SubjectScoreRepository subjectScoreRepository, SemesterRepository semesterRepository,
                        LevelRepository levelRepository, UtilityClass utilityClass, StaffRolesEntityRepo staffRolesEntityRepo,
                        InstitutiionRepository institutionRepository,  BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.staffsRepository = staffsRepository;
        this.studentsRepository = studentsRepository;
        this.resultsRepository = resultsRepository;
        this.subjectsRepository = subjectsRepository;
        this.subjectScoreRepository = subjectScoreRepository;
        this.semesterRepository = semesterRepository;
        this.levelRepository = levelRepository;
        this.utilityClass = utilityClass;
        this.institutionRepository = institutionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.staffRolesEntityRepo = staffRolesEntityRepo;
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

        staff.setStaffRoles(saveStaffRoles(staff, staffRoles));
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

        for (StaffRolesEntity staffRole : staff.getStaffRoles()) {
            roles.add(staffRole.getStaffRole().name());
        }

        staffData.setStaffRoles(roles);
        staffData.setStaffStatus(staff.getStaffStatus().name());
        staffData.setDateOfRegistration(staff.getDateOfRegistration().toString());

        return staffData;
    }

    public String updateStaffInfo(FindStaffDTO updateInfo) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(updateInfo.getStaffId())
                .orElseThrow(() -> new Exception("Staff not found"));

        staff.setFirstName(updateInfo.getFirstName());
        staff.setLastName(updateInfo.getSurname());
        staff.setGender(updateInfo.getGender());
        staff.setDateOfBirth(LocalDate.parse(updateInfo.getDateOfBirth()));
        staff.setEmail(updateInfo.getEmail());
        staff.setPhoneNumber(updateInfo.getPhoneNumber());

        List<String> newRoles = updateInfo.getStaffRoles();
        System.out.println("Message: " + newRoles);
        List<StaffRoles> staffRoles = new ArrayList<>();

        for (String role : newRoles) {
            staffRoles.add(StaffRoles.valueOf(role));
        }
        staff.setStaffRoles(updateStaffRoles(staff, newRoles));
        staff.setStaffStatus(StaffStatus.valueOf(updateInfo.getStaffStatus()));
        staffsRepository.save(staff);

        return "Staff Info Updated Successfully";
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
            List<StaffRolesEntity> staffRoles = teachingStaff.getStaffRoles();
            if (staffRoles.stream().anyMatch(staffRole -> staffRole.getStaffRole().equals(StaffRoles.TEACHING_STAFF))) {
                staffCount++;
            }
        }

        return staffCount;
    }


    public String addNewSubjects(String subjectName, String levelId) throws Exception {

            Level level = levelRepository.findByLevelID(levelId).orElse(null);
            if (level == null) {
                throw new Exception("Invalid levelId");
            }

            Subjects  subject = subjectsRepository.findBySubjectNameAndLevel_LevelID(subjectName, levelId).orElse(null);
            if (subject != null) {
                throw new Exception("Subject already exists");
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

            return subjectId;
    }

    public String resetStaffPassword(String staffId, String newPassword) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            throw new Exception("Staff not found");

        if (bCryptPasswordEncoder.matches(newPassword, staff.getPassword())) {
            throw new Exception("You can't use old password");
        }

        staff.setPassword(bCryptPasswordEncoder.encode(newPassword));
        staffsRepository.save(staff);

        return "Password reset successfull";
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
            List<StaffRolesEntity> staffRoles = staffMember.getStaffRoles();
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

        return staffList;
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

            List<StaffRolesEntity> staffRoles = staffMember.getStaffRoles();
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
}
