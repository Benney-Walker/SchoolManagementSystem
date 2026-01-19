package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.RecentStaffView;
import com.codewithben.schoolmanagementsystem.DTO.Institution.ReportsDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StaffCaching;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public String addNewStaff(String firstName, String surName, String gender, LocalDate dateOfBirth,
                              String institutionId, String email, String password, String phoneNumber,
                              String status) throws Exception {

        Institution institution = institutionRepository.findByInstitutionId(institutionId).orElse(null);
        if (institution == null) {
            throw new Exception("invalid institution ID");
        }

        if (staffsRepository.existsByPhoneNumberAndInstitution_InstitutionId(phoneNumber, institutionId)) {
            throw new Exception("Phone number already in use");
        }

        Staffs existingStaff = staffsRepository.findByStatusAndInstitution_InstitutionId(status, institutionId).orElse(null);
        if (existingStaff != null) {
            if (status.equals("Accountant")) {
                throw new Exception("Institution already has " + status);
            }
        }

        if (staffsRepository.existsByFirstNameAndLastName(firstName, surName)) {
            throw new Exception("Staff already exists");
        }

        String staffID = utilityClass.generateEntityId("STAFF");
        Staffs staff = new Staffs();
        staff.setStaffId(staffID);
        staff.setFirstName(firstName);
        staff.setLastName(surName);
        staff.setGender(gender);
        staff.setDateOfBirth(dateOfBirth);
        staff.setEmail(email);

        String hashedPassword = bCryptPasswordEncoder.encode(password);
        staff.setPassword(hashedPassword);
        staff.setPhoneNumber(phoneNumber);
        staff.setDateOfRegistration(LocalDate.now());
        staff.setStatus(status);

        // Set the owning side of the relationship - this is ALL you need!
        staff.setInstitution(institution);

        // Save only the staff - the FK will be set automatically
        staffsRepository.save(staff);

        return staffID;
    }

    public FindStaffDTO findStaffById(String id) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(id).orElse(null);

        if (staff == null) {
            throw new Exception("Staff not found");
        }

        return new FindStaffDTO(
                staff.getStaffId(), staff.getFirstName(),
                staff.getLastName(), staff.getGender(),
                staff.getDateOfBirth(), staff.getEmail(), staff.getPhoneNumber(),
                staff.getStatus(), staff.getDateOfRegistration().toString()
        );
    }

    /*public FindStaffDTO findStaffByFullName(String firstName, String lastName) throws Exception {
        Staffs staff = staffsRepository.findByFirstNameAndLastName(firstName, lastName).orElse(null);

        if (staff == null) {
            return null;
        }

        return new FindStaffDTO(
                staff.getStaffId(), staff.getFirstName(),
                staff.getLastName(), staff.getGender(),
                staff.getDateOfBirth(), staff.getEmail(), staff.getPhoneNumber(),
                staff.getStatus(), staff.getDateOfRegistration(),
                staff.getSubject().stream().toList(), staff.getLevelId().stream().toList()
        );
    }*/

    public String staffAuthentication(String loginId, String password) throws Exception {

        Staffs staffs = staffsRepository.findByStaffId(loginId).orElse(null);

        if(staffs == null){
            throw new Exception("Invalid ID");
        }

        String storedPassword = staffs.getPassword();
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
            staffs.setPassword(newHashedPassword);
            staffsRepository.save(staffs);
        }

        String staffStatus = staffs.getStatus();
        String institutionName = staffs.getInstitution().getInstitutionName();
        String staffFullName = staffs.getFirstName() + " " + staffs.getLastName();
        String staffId = staffs.getStaffId();

        return staffStatus + "_" + institutionName + "_" + staffFullName + "_" + staffId;
    }

    public long countTotalStaffs(String institutionId) throws Exception {
        Institution institution = institutionRepository.findByInstitutionId(institutionId).orElse(null);
        if (institution == null) {
            return 0;
        }

        List<Staffs> staffs = institution.getStaff();
        return staffs.size();
    }

    private boolean isBCrypt(String password) {
        return password != null && password.startsWith("$2");
    }


    public String addNewSubjects(String subjectName, String semesterId, String levelId) throws Exception {
            Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
            if (semester == null) {
                throw new Exception("Invalid semesterId");
            }

            Level level = levelRepository.findByLevelID(levelId).orElse(null);
            if (level == null) {
                throw new Exception("Invalid levelId");
            }

            Institution institution = level.getInstitution();

            Subjects  subject = subjectsRepository.findBySubjectName(subjectName).orElse(null);
            if (subject != null) {
                throw new Exception("Subject already exists");
            }

            Subjects subjects = new Subjects();
            String subjectId = utilityClass.generateEntityId("SUBJECT");
            subjects.setSubjectId(subjectId);
            subjects.setSubjectName(subjectName.toUpperCase());
            subjects.setLevel(level);
            subjects.setSemester(semester);
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

    public String updateSubjectDetails(String subjectName, String semesterId, String levelId) throws Exception {


            Subjects subject = subjectsRepository.findBySubjectName(subjectName.toUpperCase()).orElse(null);
            if (subject == null) {
                throw new Exception("Subject does not exist");
            }

            Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
            if (semester == null) {
                throw new Exception("Invalid semesterId");
            }

            Level level = levelRepository.findByLevelID(levelId).orElse(null);
            if (level == null) {
                throw new Exception("Invalid levelId");
            }

            subject.setSubjectName(subjectName.toUpperCase());
            subject.setLevel(level);
            subject.setSemester(semester);
            subjectsRepository.save(subject);
            return "success";
    }

    public String deleteSubject(String subjectName) {
        if (subjectName == null) {
            return "null input data";
        }

        try {
            Subjects subject = subjectsRepository.findBySubjectName(subjectName.toUpperCase()).orElse(null);
            if (subject == null) {
                return "subject not found";
            }

            subjectsRepository.delete(subject);
            return "success";
        } catch (Exception e) {
            return "Failed Error: " + e.getMessage();
        }
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

        return staffs.stream().map(
                loadInfo -> {
                    String staffIdMapping = loadInfo.getStaffId();
                    String staffFullNameMapping = loadInfo.getFirstName() + " " + loadInfo.getLastName();
                    String staffRole = loadInfo.getStatus();

                    return new StaffCaching(staffFullNameMapping, staffIdMapping, staffRole);
                }
        ).collect(Collectors.toList());
    }

    public List<RecentStaffView> loadAllStaffData(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff not found");
        }
        Institution institution = institutionRepository.findByInstitutionId(staff.getInstitution().getInstitutionId()).orElse(null);
        if (institution == null) {
            throw new Exception("Institution not found");
        }

        List<Staffs> staffs = institution.getStaff();
        List<RecentStaffView> recentStaffViews = new ArrayList<>();
        for(Staffs staffMember: staffs){
            RecentStaffView recentStaffView;
            if(staffMember.getLevel() == null) {
                recentStaffView = new RecentStaffView(
                        staffMember.getStaffId(),
                        staffMember.getFirstName() + " " + staffMember.getLastName(),
                        null
                );
            } else {
                recentStaffView = new RecentStaffView(
                        staffMember.getStaffId(),
                        staffMember.getFirstName() + " " + staffMember.getLastName(),
                        staffMember.getLevel().getLevelName()
                );
            }
            recentStaffViews.add(recentStaffView);
        }
        return recentStaffViews;
    }


}
