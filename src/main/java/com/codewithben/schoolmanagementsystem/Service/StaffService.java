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
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public StaffService(StaffsRepository staffsRepository, StudentsRepository studentsRepository, ResultsRepository resultsRepository,
                        SubjectsRepository subjectsRepository, SubjectScoreRepository subjectScoreRepository, SemesterRepository semesterRepository,
                        LevelRepository levelRepository, UtilityClass utilityClass,
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
    }

    public String addNewStaff(String firstName, String surName, String gender, String dateOfBirth,
                              String institutionId, String email, String password, String phoneNumber,
                              String staffRole) throws Exception {

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
        staff.setStaffRoles(StaffRoles.valueOf(staffRole));
        staff.setStaffStatus(StaffStatus.ACTIVE);

        staff.setInstitution(institution);
        staffsRepository.save(staff);

        return staffID;
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
        staffData.setStaffRole(staff.getStaffRoles().toString());
        staffData.setStaffStatus(staff.getStaffStatus().toString());
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
        staff.setStaffRoles(StaffRoles.valueOf(updateInfo.getStaffRole()));
        staff.setStaffStatus(StaffStatus.valueOf(updateInfo.getStaffStatus()));
        staffsRepository.save(staff);

        return "Staff Info Updated Successfully";
    }

    @Transactional
    public String staffAuthentication(String loginId, String password) throws Exception {

        Staffs staff = staffsRepository.findByStaffId(loginId).orElse(null);

        if(staff == null){
            throw new Exception("Invalid ID");
        }

        if (staff.getStaffStatus() != null) {
            if (staff.getStaffStatus() != StaffStatus.ACTIVE) {
                throw new Exception("Staff account inactive");
            }
        } else {
            staff.setStaffStatus(StaffStatus.ACTIVE);
        }

        String storedPassword = staff.getPassword();
        if (isBCrypt(storedPassword)) {
            //Checks password for new account
            if (!bCryptPasswordEncoder.matches(password, storedPassword)) {
                throw new Exception("Invalid password");
            }

        } else {
            //Checks password for old account, hash it and store hashed
            if (!storedPassword.equals(password)) {
                throw new Exception("Invalid password");
            }

            String newHashedPassword = bCryptPasswordEncoder.encode(password);
            staff.setPassword(newHashedPassword);
            staffsRepository.save(staff);
        }

        String staffRole = staff.getStaffRoles().toString();
        String institutionName = staff.getInstitution().getInstitutionName();
        String staffFullName = staff.getFirstName() + " " + staff.getLastName();
        String staffId = staff.getStaffId();

        return staffRole + "," + institutionName + "," + staffFullName + "," + staffId;
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

    private boolean isBCrypt(String password) {
        return password != null && password.startsWith("$2");
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
        if (staff == null) {
            throw new Exception("Staff not found");
        }
        Institution institution = institutionRepository.findByInstitutionId(staff.getInstitution().getInstitutionId()).orElse(null);
        if (institution == null) {
            throw new Exception("Institution not found");
        }

        List<Staffs> staffs = institution.getStaff();
        if (staffs == null || staffs.isEmpty()) {
            return Collections.emptyList();
        }

        return staffs.stream().map(
                loadInfo -> {
                    String staffIdMapping = loadInfo.getStaffId();
                    String staffFullNameMapping = loadInfo.getFirstName() + " " + loadInfo.getLastName();
                    String staffRole = loadInfo.getStaffRoles().name();

                    return new StaffCaching(staffFullNameMapping, staffIdMapping, staffRole);
                }
        ).collect(Collectors.toList());
    }

    public List<ViewStaffList> loadAllStaffList(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff not found");
        }

        Institution institution = staff.getInstitution();
        if (institution == null) {
            throw new Exception("Institution not found");
        }

        List<Staffs> staffs = institution.getStaff();
        if (staffs == null || staffs.isEmpty()) {
            return Collections.emptyList();
        }

        List<ViewStaffList> viewStaffLists = new ArrayList<>();

        for(Staffs staffMember: staffs){
            ViewStaffList viewStaffList = new ViewStaffList();
            if(staffMember.getLevel() == null) {
                viewStaffList.setStaffId(staffMember.getStaffId());
                viewStaffList.setStaffName(staffMember.getFirstName() + " " + staffMember.getLastName());
                viewStaffList.setStaffRole(staffMember.getStaffRoles().name());
                viewStaffList.setAssignedLevel(new ArrayList<>());

            } else {
                viewStaffList.setStaffId(staffMember.getStaffId());
                viewStaffList.setStaffName(staffMember.getFirstName() + " " + staffMember.getLastName());

                viewStaffList.setStaffRole(staffMember.getStaffRoles().name());

                List<Level> levels = staffMember.getLevel();
                List<String> assignedLevels = new ArrayList<>();
                for(Level level: levels){
                    assignedLevels.add(level.getLevelName());
                }

                viewStaffList.setAssignedLevel(assignedLevels);
            }
            viewStaffLists.add(viewStaffList);
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
}
