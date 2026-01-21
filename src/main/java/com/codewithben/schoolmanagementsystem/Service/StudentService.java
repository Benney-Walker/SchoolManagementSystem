package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Contants.StudentStatus;
import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentAttendance;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentListPrint;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Entity.Attendance;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final StaffsRepository staffsRepository;

    private final LevelRepository levelRepository;

    private final StudentsRepository studentsRepository;

    private final UtilityClass utilityClass;

    private final SubjectsRepository subjectsRepository;

    private final SemesterRepository semesterRepository;

    private final ResultsRepository resultsRepository;

    private final InstitutiionRepository institutionRepository;

    private final SubjectScoreRepository subjectScoreRepository;

    private final GradeSystemRepository gradeSystemRepository;

    private final AttendanceRepository attendanceRepository;

    public StudentService(StaffsRepository staffsRepository, LevelRepository levelRepository, StudentsRepository studentsRepository,
                          UtilityClass utilityClass, SubjectsRepository subjectsRepository, SemesterRepository semesterRepository,
                          ResultsRepository resultsRepository, InstitutiionRepository institutionRepository, SubjectScoreRepository subjectScoreRepository,
                          GradeSystemRepository gradeSystemRepository,  AttendanceRepository attendanceRepository) {
        this.staffsRepository = staffsRepository;
        this.levelRepository = levelRepository;
        this.studentsRepository = studentsRepository;
        this.utilityClass = utilityClass;
        this.subjectsRepository = subjectsRepository;
        this.semesterRepository = semesterRepository;
        this.resultsRepository = resultsRepository;
        this.institutionRepository = institutionRepository;
        this.subjectScoreRepository = subjectScoreRepository;
        this.gradeSystemRepository = gradeSystemRepository;
        this.attendanceRepository = attendanceRepository;
    }

    //Method for adding new student
    @Transactional
    public String addNewStudent(String firstName, String lastName, String gender, LocalDate dateOfBirth, String hometown,
                                String parentName, String parentContact, String levelId, String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff not found");
        }

        Institution institution = staff.getInstitution();

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null)
            throw new Exception("Level Not Found");

        Students student = studentsRepository.findByFirstNameAndLastName(firstName, lastName).orElse(null);
        if (student == null) {
            String studentId = utilityClass.generateEntityId("STUDENT");//This line gets student id

            //Saving new student
            student = new Students();
            student.setStudentId(studentId);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setGender(gender);
            student.setDateOfBirth(dateOfBirth);
            student.setHomeTown(hometown);
            student.setParentName(parentName);
            student.setParentPhoneNumber(parentContact);
            student.setLevel(level);
            student.setRegistrationDate(LocalDate.now());
            student.setInstitution(institution);
            student.setStudentStatus(StudentStatus.ACTIVE);
            studentsRepository.saveAndFlush(student);

            //Add student to level list
            List<Students> levelStudents = level.getStudents();
            if (levelStudents == null) {
                levelStudents = new ArrayList<>();
            }
            levelStudents.add(student);
            level.setStudents(levelStudents);
            levelRepository.save(level);

            //Adding student to institution
            List<Students> students = institution.getStudents();
            if (students == null) {
                students = new ArrayList<>();
            }
            students.add(student);
            institution.setStudents(students);
            institutionRepository.save(institution);
            System.out.println(studentId);
            return studentId;
        }
        throw new Exception("Student Already Exists");
    }

    @Transactional
    public String addStudentSubjectScores(String studentId, String subjectId, Double classScore, Double examScore,
                                          String staffId, String semesterId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).
                orElseThrow(() -> new Exception("Staff not found"));

        List<GradeSystem> gradeSystem = gradeSystemRepository.findAllByInstitution_InstitutionId(
                staff.getInstitution().getInstitutionId()
        );
        if (gradeSystem == null) {
            throw new Exception("Grading criteria not added to system");
        }

        Students student = studentsRepository.findByStudentId(studentId)
                .orElseThrow(() -> new Exception("Student Not Found"));

        Subjects subject = subjectsRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new Exception("Subject Not Found"));

        String levelId = subject.getLevel().getLevelID();

        Semester semester = semesterRepository.findBySemesterID(semesterId)
                .orElseThrow(() -> new Exception("Semester Not Found"));


        // Get or create Results for this student + semester + level
        Results result = resultsRepository
                .findByStudent_StudentIdAndSemester_SemesterIDAndLevel_LevelID(studentId, semesterId, levelId)
                .orElse(null);

        if (result == null) {
            result = new Results();
            result.setStudent(student);
            result.setLevel(subject.getLevel());
            result.setSemester(semester);
            result.setCreatedAt(LocalDate.now());
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
            result.setUpdatedBy(staff);
            result = resultsRepository.save(result);
        }

        // Check if score already exists
        SubjectScore existingScore = subjectScoreRepository
                .findByStudent_StudentIdAndSubject_SubjectId(studentId, subjectId)
                .orElse(null);

        Double totalScore = classScore + examScore;
        Double oldTotalScore = 0.0;

        if (existingScore != null) {
            // Update existing
            oldTotalScore = existingScore.getTotalScore() != null ? existingScore.getTotalScore() : 0.0;
            existingScore.setClassScore(classScore);
            existingScore.setExamScore((examScore/100)*50);
            existingScore.setTotalScore(totalScore);
            existingScore.setGrade(utilityClass.extractGrade(totalScore));
            existingScore.setRemarks(utilityClass.extractDescription(totalScore));
            subjectScoreRepository.save(existingScore);
        } else {
            // Create new
            SubjectScore subjectScore = new SubjectScore();
            subjectScore.setStudent(student);
            subjectScore.setSubject(subject);
            subjectScore.setResults(result);
            subjectScore.setClassScore(classScore);
            subjectScore.setExamScore(examScore);
            subjectScore.setTotalScore(totalScore);
            subjectScore.setGrade(utilityClass.extractGrade(totalScore));
            subjectScore.setRemarks(utilityClass.extractDescription(totalScore));
            subjectScoreRepository.save(subjectScore);
        }

        // Recalculate result totals
        updateResultTotals(result, staff, subject);

        return "success";
    }

    private void updateResultTotals(Results result, Staffs staff, Subjects subject) {
        List<SubjectScore> scores = subjectScoreRepository.findByResults_ResultId(result.getResultId());

        double total = 0.0;
        if (scores == null || scores.isEmpty()) {
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
        } else {
            total = scores.stream()
                    .mapToDouble(s -> s.getTotalScore() != null ? s.getTotalScore() : 0.0)
                    .sum();
            result.setTotalScore(total);
            result.setAverageScore(total / scores.size());
        }

        result.setUpdatedAt(LocalDate.now());
        result.setUpdatedBy(staff);

        result.setPosition(utilityClass.getResultPosition(subject, result.getSemester(), total));
        resultsRepository.save(result);

    }

    public StudentResult findStudentResults(String studentId, String semesterId, String levelId) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            throw new Exception("student not found");
        }
        Results results = resultsRepository.findByStudent_StudentIdAndSemester_SemesterIDAndLevel_LevelID(studentId, semesterId, levelId).orElse(null);
        if (results == null) {
            throw new Exception("results not found");
        }

        List<StudentSubjectResults> subjectResults = new ArrayList<>();

            List<SubjectScore> subjectScores = results.getSubjectScores();

            for (SubjectScore subjectScore : subjectScores) {
                if (subjectScore.getStudent().getStudentId().equals(studentId)) {
                    StudentSubjectResults studentSubjectResults = new StudentSubjectResults(
                            subjectScore.getSubject().getSubjectName(),
                            String.valueOf(subjectScore.getTotalScore()),
                            subjectScore.getGrade(),
                            subjectScore.getRemarks()
                    );
                    subjectResults.add(studentSubjectResults);
                }
            }

        String studentID = results.getStudent().getStudentId();
        String studentName = student.getFirstName() + " " + student.getLastName();
        String semesterName = results.getSemester().getSemesterName();
        String levelName = results.getLevel().getLevelName();
        Double totalScore = results.getTotalScore();
        Double averageScore = results.getAverageScore();
        String position = results.getPosition();
        return new StudentResult(
                studentID, studentName, levelName, semesterName, totalScore,
                averageScore, position, subjectResults
        );
    }

    public FindStudentDTO findStudentByStudentId(String studentId) throws Exception {
        return studentsRepository.findByStudentId(studentId)
                .map(student -> new FindStudentDTO(
                        student.getStudentId(), student.getFirstName(),
                        student.getLastName(), student.getGender(),
                        student.getDateOfBirth().toString(), student.getParentName(),
                        student.getParentPhoneNumber(), student.getLevel().getLevelName()
                )).orElse(null);
    }

    public long countTotalStudents(String institutionId) throws Exception {
        Institution institution = institutionRepository.findByInstitutionId(institutionId).orElse(null);
        if (institution == null) {
            return 0;
        }

        List<Students> students = institution.getStudents();
        return students.size();
    }

    public List<RecentStudentsDTO> findRecentlyAddedStudents(LocalDate startDate, LocalDate endDate) throws Exception {
        return studentsRepository.findByRegistrationDateBetween(startDate, endDate).stream().map(
                students -> {
                    String studentId = students.getStudentId();
                    String fullName = students.getFirstName() + " " + students.getLastName();
                    String studentLevel = students.getLevel().getLevelName();
                    String dateOfRegistration = students.getRegistrationDate().toString();
                    String status = students.getStudentStatus().toString();


                    return new RecentStudentsDTO(studentId, fullName, studentLevel, dateOfRegistration, status);
                }
        ).collect(Collectors.toList());
    }

    //This loads all the absentees for the day
    public List<AbsenteesView> findAbsentees(String staffId) throws Exception {
        Staffs  staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("staff not found"));

        String institutionId = staff.getInstitution().getInstitutionId();
        LocalDate currentDate = LocalDate.now();

        List<Attendance> absentees = attendanceRepository.findByStatusAndDateMarkedAndInstitution_InstitutionId(
                AttendanceStatus.ABSENT, currentDate, institutionId
        );

        if (absentees == null || absentees.isEmpty()) {
            throw new Exception("No absentees for today");
        }

        List<AbsenteesView> absenteesViews = new ArrayList<>();
        for (Attendance attendance : absentees) {
            AbsenteesView absenteesView = null;
            absenteesView.setStudentId(attendance.getStudent().getStudentId());
            absenteesView.setStudentName(attendance.getStudent().getFirstName() + " " + attendance.getStudent().getLastName());
            absenteesView.setStudentGrade(attendance.getStudent().getLevel().getLevelName());
            absenteesView.setInstructorName(attendance.getLevel().getStaff().getFirstName() + " " + attendance.getLevel().getStaff().getLastName());
            absenteesView.setInstructorId(attendance.getLevel().getStaff().getStaffId());
            absenteesViews.add(absenteesView);
        }
        return absenteesViews;
    }

    public String updateStudentPersonalData(String studentId, String gender, String dataOfBirth, String guardianName,
                                            String guardianContact, String status) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            throw new Exception("Student Not Found");
        }

        StudentStatus studentStatus = StudentStatus.valueOf(status);

        student.setGender(gender);
        student.setDateOfBirth(LocalDate.parse(dataOfBirth));
        student.setParentName(guardianName);
        student.setParentPhoneNumber(guardianContact);
        student.setStudentStatus(studentStatus);
        studentsRepository.save(student);
        return "Student information updated successfully";
    }

    public SubjectScores getSubjectStudents(String subjectId) throws Exception {
        Subjects subject = subjectsRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new Exception("Subject Not Found"));

        String subjectName = subject.getSubjectName();
        List<StudentsScoresTable> subjectStudents = new ArrayList<>();

        Level level = subject.getLevel();
        if (level == null) {
            throw new Exception("Subject has no assigned level");
        }

        List<Students> students = level.getStudents();
        if (students == null || students.isEmpty()) {
            return new SubjectScores(subjectId, subjectName, subjectStudents);  // Return empty list
        }

        for (Students student : students) {
            String studentId = student.getStudentId();
            String studentName = student.getFirstName() + " " + student.getLastName();

            // Default scores for students without scores yet
            String classScore = "0";
            String examScore = "0";

            // Check if student has scores for this subject
            List<SubjectScore> scores = student.getSubjectScore();
            if (scores != null) {
                for (SubjectScore score : scores) {
                    if (score.getSubject() != null &&
                            subjectId.equals(score.getSubject().getSubjectId())) {
                        classScore = score.getClassScore() != null ? score.getClassScore().toString() : "0";
                        examScore = score.getExamScore() != null ? score.getExamScore().toString() : "0";
                        break;
                    }
                }
            }

            // Add ALL students, with or without scores
            subjectStudents.add(new StudentsScoresTable(studentId, studentName, classScore, examScore));
        }

        return new SubjectScores(subjectId, subjectName, subjectStudents);
    }

    public List<StudentListPrint> getLevelStudents(String levelId) throws Exception {
        Level level = levelRepository.findByLevelID(levelId).orElseThrow(() -> new Exception("Level Not Found"));

        List<Students> students = level.getStudents();
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }

        return students.stream().map(
                student -> {
                    String studentId = student.getStudentId();
                    String fullName = student.getFirstName() + " " + student.getLastName();

                    return new StudentListPrint(studentId, fullName);
                }
        ).collect(Collectors.toList());
    }

    public List<StudentAttendance> loadStudentForAttendance(String levelId, String attendanceDate) throws Exception {
        List<StudentAttendance> attendanceList = new ArrayList<>();

        if (attendanceDate.equals("no_date")) {
            Level level = levelRepository.findByLevelID(levelId)
                    .orElseThrow(() -> new Exception("Level Not Found"));

            List<Students> students = level.getStudents();

            for (Students student : students) {
                if (student.getStudentStatus() == StudentStatus.ACTIVE) {
                    StudentAttendance studentAttendance = new StudentAttendance(
                            level.getLevelID(),
                            student.getStudentId(),
                            student.getFirstName() + " " + student.getLastName(),
                            AttendanceStatus.ABSENT.toString()
                    );
                    attendanceList.add(studentAttendance);
                }
            }

            return attendanceList;
        }

        List<Attendance> markedAttendance = attendanceRepository
                .findByLevel_LevelIDAndDateMarked(levelId, LocalDate.parse(attendanceDate));
        if (markedAttendance == null || markedAttendance.isEmpty()) {
            throw new Exception("Date must today or earlier.");
        }

        for (Attendance attendance : markedAttendance) {
            String studentName =
                    attendance.getStudent().getFirstName() + " " + attendance.getStudent().getLastName();
            StudentAttendance studentAttendance = new StudentAttendance(
                    attendance.getLevel().getLevelID(),
                    attendance.getStudent().getStudentId(),
                    studentName,
                    attendance.getStatus().toString()
            );
            attendanceList.add(studentAttendance);
        }

        return attendanceList;
    }

    public String markStudentAttendance(String studentId, String staffId, String levelId,
                                        String status) throws Exception {

        Students student = studentsRepository.findByStudentId(studentId)
                .orElseThrow(() -> new Exception("Student Not Found"));

        Institution institution = student.getInstitution();

        String currentSemesterId = utilityClass.getCurrentSemesterId(
                student.getInstitution().getInstitutionId()
        );

        List<Attendance> markedAttendance = attendanceRepository
                .findByLevel_LevelIDAndDateMarked(
                        levelId, LocalDate.now()
                );
        if (markedAttendance != null && !markedAttendance.isEmpty()) {
            throw new Exception("Today's attendance already marked");
        }

        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Level Not Found"));

        Semester semester = semesterRepository
                .findBySemesterID(currentSemesterId).orElseThrow(() -> new Exception("Semester Not Found"));

        Staffs staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("Staff Not Found"));


        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSemester(semester);
        attendance.setLevel(level);
        attendance.setDateMarked(LocalDate.now());
        attendance.setStatus(AttendanceStatus.valueOf(status));
        attendance.setMarkedBy(staff);
        attendance.setInstitution(institution);
        attendanceRepository.save(attendance);

        List<Attendance> studentAttendance = student.getAttendance();
        if (studentAttendance == null || studentAttendance.isEmpty()) {
            studentAttendance = new ArrayList<>();
        }
        studentAttendance.add(attendance);
        studentsRepository.save(student);

        return "success";
    }

    public String updateStudentAttendance(String studentId, String staffId, String levelId, String status,
                                          LocalDate dateMarked) throws Exception {
        if (dateMarked.isBefore(LocalDate.now())) {
            throw new Exception("You can't update previous attendance");
        }

        Staffs staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("Staff Not Found"));

        List<Attendance> markedAttendance = attendanceRepository
                .findByLevel_LevelIDAndDateMarked(
                        levelId, LocalDate.now()
                );

        if (markedAttendance == null) {
            throw new Exception("Today's attendance not marked");
        }

        for (Attendance attendance : markedAttendance) {
            if (attendance.getStudent().getStudentId().equals(studentId)) {
                attendance.setStatus(AttendanceStatus.valueOf(status));
                attendance.setMarkedBy(staff);
                attendanceRepository.save(attendance);
            }
        }

        return "success";
    }


}
