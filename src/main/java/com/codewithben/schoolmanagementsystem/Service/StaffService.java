package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Institution.FindStaffDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.RecentStaffView;
import com.codewithben.schoolmanagementsystem.DTO.Institution.ReportsDTO;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StaffCaching;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
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

    private final ReportsRepository reportsRepository;

    private final InstitutiionRepository institutionRepository;

    public StaffService(StaffsRepository staffsRepository, StudentsRepository studentsRepository, ResultsRepository resultsRepository,
                        SubjectsRepository subjectsRepository, SubjectScoreRepository subjectScoreRepository, SemesterRepository semesterRepository,
                        LevelRepository levelRepository, UtilityClass utilityClass, ReportsRepository reportsRepository,
                        InstitutiionRepository institutionRepository) {
        this.staffsRepository = staffsRepository;
        this.studentsRepository = studentsRepository;
        this.resultsRepository = resultsRepository;
        this.subjectsRepository = subjectsRepository;
        this.subjectScoreRepository = subjectScoreRepository;
        this.semesterRepository = semesterRepository;
        this.levelRepository = levelRepository;
        this.utilityClass = utilityClass;
        this.reportsRepository = reportsRepository;
        this.institutionRepository = institutionRepository;
    }

    public String addNewStaff(String firstName, String surName, String gender, LocalDate dateOfBirth,
                              String institutionId, String email, String password, String phoneNumber,
                              String status) throws Exception {

        Institution institution = institutionRepository.findByInstitutionId(institutionId).orElse(null);
        if (institution == null) {
            return "invalid institution ID";
        }

        Staffs existingStaff = staffsRepository.findByphoneNumberAndInstitution_InstitutionId(phoneNumber, institutionId).orElse(null);
        if (existingStaff != null) {
            return "Staff already exists";
        }

        String staffID = utilityClass.generateEntityId("STAFF");
        Staffs staff = new Staffs();
        staff.setStaffId(staffID);
        staff.setFirstName(firstName);
        staff.setLastName(surName);
        staff.setGender(gender);
        staff.setDateOfBirth(dateOfBirth);
        staff.setEmail(email);
        staff.setPassword(password);
        staff.setPhoneNumber(phoneNumber);
        staff.setDateOfRegistration(LocalDate.now());
        staff.setStatus(status);

        // Set the owning side of the relationship - this is ALL you need!
        staff.setInstitution(institution);

        // Save only the staff - the FK will be set automatically
        staffsRepository.save(staff);

        return staffID;
    }

    public List<ReportsDTO> displayReportsBasedOnStatus(String status) {
        List<Reports> reports = reportsRepository.findReportsByStatus(status);

        //Convert each entity into a DTO
        return reports.stream().map(
                report -> new ReportsDTO(
                        report.getStaff(),
                        report.getCreationDate(),
                        report.getReportData()
                )
        ).collect(Collectors.toList());
    }

    public List<ReportsDTO> displayReportsBetween(LocalDateTime from,
                                                                LocalDateTime to) {
        List<Reports> reports = reportsRepository.findByCreationDateBetween(from, to);

        return reports.stream().map(
                report -> new ReportsDTO(
                        report.getStaff(),
                        report.getCreationDate(),
                        report.getReportData()
                )
        ).collect(Collectors.toList());
    }

    public FindStaffDTO findStaffById(String id) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(id).orElse(null);

        if (staff == null) {
            return null;
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

        if(!password.equals(staffs.getPassword())){
            throw new Exception("Invalid Password");
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


    public String recoverStaffPassword(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null)
            throw new Exception("Staff not found");

        return staff.getPassword();
    }

    //Method for adding complain or important message
    public String addComplainOrMessage(String instructorId, String viewerConstraint, String message) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(instructorId).orElse(null);
        if (staff == null) {
            return "staff not found";
        }
        Reports report = new Reports();
        report.setReportId(instructorId);
        report.setCreationDate(LocalDateTime.now());
        report.setReportData(message);
        report.setStatus("UNCHECKED");
        report.setConstraint(viewerConstraint);
        reportsRepository.save(report);
        return "success";
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

                    return new StaffCaching(staffFullNameMapping, staffIdMapping);
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
        return staffs.stream().map(
                allStaff -> {
                    String staffFullNameMapping = allStaff.getFirstName() + " " + allStaff.getLastName();
                    String staffIdMapping = allStaff.getStaffId();
                    String levelNameMapping = allStaff.getLevel().toString();

                    return new RecentStaffView(staffIdMapping, staffFullNameMapping, levelNameMapping);
                }
        ).collect(Collectors.toList());
    }


}
