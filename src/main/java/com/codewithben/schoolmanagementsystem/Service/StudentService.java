package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Contants.StudentStatus;
import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentAttendance;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentListPrint;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentsHolder;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Entity.Attendance;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
    public String addStudentSubjectScores(SaveStudentScores scores) throws Exception {
        String[] scoresInfoSplit = scores.getScoresInfo().split("_");
        String subjectId = "";
        String semesterId = "";
        String staffId = "";
        if (scoresInfoSplit.length == 3) {
            subjectId = scoresInfoSplit[0];
            semesterId = scoresInfoSplit[1];
            staffId = scoresInfoSplit[2];
        }

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);

        Students student = studentsRepository.findByStudentId(scores.getStudentId())
                .orElseThrow(() -> new Exception("Student Not Found"));

        Institution institution = student.getInstitution();

        List<GradeSystem> gradeSystem = gradeSystemRepository.findAllByInstitution_InstitutionId(
                institution.getInstitutionId()
        );

        if (gradeSystem == null || gradeSystem.isEmpty()) {
            throw new Exception("Grading criteria not added to system. Contact your administrator");
        }

        Subjects subject = subjectsRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new Exception("Subject Not Found"));

        String levelId = subject.getLevel().getLevelID();

        Semester semester = semesterRepository.findBySemesterID(semesterId)
                .orElseThrow(() -> new Exception("Semester Not Found"));


        // Get or create Results for this student + semester + level
        Results result = resultsRepository
                .findByStudent_StudentIdAndSemester_SemesterIDAndLevel_LevelID(
                        student.getStudentId(), semesterId, levelId)
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
        }

        // Check if score already exists
        SubjectScore subjectScore = subjectScoreRepository
                .findByStudent_StudentIdAndSubject_SubjectIdAndResults_ResultId(
                        student.getStudentId(),
                        subject.getSubjectId(),
                        result.getResultId()
                ).orElse(new SubjectScore());

        double classScore = Double.parseDouble(scores.getClassScore());
        double calculatedExamScore = Double.parseDouble(scores.getCalculatedExamScore());
        double totalScore =  classScore + calculatedExamScore;

        subjectScore.setSubject(subject);
        subjectScore.setStudent(student);
        subjectScore.setResults(result);
        subjectScore.setExercise1Score(Double.parseDouble(scores.getExercise1Score()));
        subjectScore.setClassTestScore(Double.parseDouble(scores.getClassTestScore()));
        subjectScore.setExercise2Score(Double.parseDouble(scores.getExercise2Score()));
        subjectScore.setProjectScore(Double.parseDouble(scores.getProjectScore()));
        subjectScore.setClassScore(classScore);
        subjectScore.setExamScore(Double.parseDouble(scores.getExamScore()));
        subjectScore.setCalculatedExamScore(calculatedExamScore);
        subjectScore.setGrade(
                utilityClass.extractGrade(totalScore, institution.getInstitutionId())
        );
        subjectScore.setRemarks(
                utilityClass.extractDescription(totalScore, institution.getInstitutionId())
        );
        subjectScore.setTotalScore(totalScore);

        subjectScoreRepository.save(subjectScore);

        //Add scores to results
        List<SubjectScore> resultsScores = result.getSubjectScores();
        if (resultsScores == null || resultsScores.isEmpty()) {
            resultsScores = new ArrayList<>();
        }
        resultsScores.add(subjectScore);
        result.setSubjectScores(resultsScores);
        resultsRepository.save(result);

        updateResultTotals(result, staff, subject);

        return "Scores successfully added";
    }

    private void updateResultTotals(Results result, Staffs staff, Subjects subject) throws Exception {
        List<SubjectScore> scores = subjectScoreRepository.findByResults_ResultId(result.getResultId());

        double total = 0.0;
        if (scores == null || scores.isEmpty()) {
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
        } else {

            for(SubjectScore score : scores) {
                total += score.getTotalScore();
            }

            System.out.println("TOTAL SCORE:" + total);
            result.setTotalScore(total);
            result.setAverageScore(total / scores.size());
        }

        result.setUpdatedAt(LocalDate.now());
        result.setUpdatedBy(staff);
        resultsRepository.save(result);

        System.out.println("Updated Results Successfully");
        utilityClass.reArrangePositions(subject, result.getSemester());
    }

    public StudentResult findStudentResults(String studentId, String semesterId, String levelId) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            throw new Exception("student not found");
        }
        Results results = resultsRepository.findByStudent_StudentIdAndSemester_SemesterIDAndLevel_LevelID(
                studentId, semesterId, levelId
        ).orElse(null);
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

    public FindStudentDTO findStudent(String studentId) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            throw new Exception("student not found. Check if student id is correct.");
        }

        return getStudentData(student);
    }

    private static FindStudentDTO getStudentData(Students student) {
        FindStudentDTO findStudentDTO = new FindStudentDTO();
        findStudentDTO.setStudentId(student.getStudentId());
        findStudentDTO.setFirstName(student.getFirstName());
        findStudentDTO.setLastName(student.getLastName());
        findStudentDTO.setGender(student.getGender());
        findStudentDTO.setParentName(student.getParentName());
        findStudentDTO.setParentPhoneNumber(student.getParentPhoneNumber());
        findStudentDTO.setDateOfBirth(student.getDateOfBirth().toString());
        findStudentDTO.setGradeId(student.getLevel().getLevelID());
        findStudentDTO.setStatus(student.getStudentStatus().toString());
        findStudentDTO.setHomeTown(student.getHomeTown());
        return findStudentDTO;
    }

    public long countTotalStudents(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("staff not found"));

        Institution institution = staff.getInstitution();
        if (institution == null) {
            throw new Exception("institution not found");
        }

        List<Students> students = institution.getStudents();
        if (students == null || students.isEmpty()) {
            throw new Exception("Institution has no students");
        }

        return students.size();
    }

    //This loads all the absentees for the day
    public List<AbsenteesView> findInstitutionAbsentees(String staffId) throws Exception {
        Staffs  staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new Exception("staff not found"));

        String institutionId = staff.getInstitution().getInstitutionId();
        LocalDate currentDate = LocalDate.now();

        List<Attendance> absentees = attendanceRepository.findByStatusAndDateMarkedAndInstitution_InstitutionId(
                AttendanceStatus.ABSENT, currentDate, institutionId
        );

        if (absentees == null || absentees.isEmpty()) {
            return Collections.emptyList();
        }

        List<AbsenteesView> absenteesViews = new ArrayList<>();

        for (Attendance attendance : absentees) {
            Students student  = attendance.getStudent();
            Level level = attendance.getLevel();
            Staffs levelStaff = level.getStaff();

            //Get absentee data
            AbsenteesView absenteesView = new AbsenteesView();
            absenteesView.setStudentId(student.getStudentId());
            absenteesView.setStudentName(
                    student.getFirstName() + " " + student.getLastName()
            );
            absenteesView.setStudentGrade(level.getLevelName());
            absenteesView.setInstructorName(
                    levelStaff.getFirstName() + " " + levelStaff.getLastName()
            );
            absenteesView.setInstructorId(levelStaff.getStaffId());
            absenteesViews.add(absenteesView);
        }
        return absenteesViews;
    }

    public String updateStudentPersonalData(UpdateStudentPersonalData data) throws Exception {
        Students student = studentsRepository.findByStudentId(data.getStudentId())
                .orElseThrow(() -> new Exception("student not found"));

        Level level = levelRepository.findByLevelID(data.getGradeId())
                .orElseThrow(() -> new Exception("New selected grade not found"));

        student.setFirstName(data.getFirstName());
        student.setLastName(data.getLastName());
        student.setGender(data.getGender());
        student.setParentName(data.getParentName());
        student.setParentPhoneNumber(data.getParentPhoneNumber());
        student.setStudentStatus(StudentStatus.valueOf(data.getStatus()));
        student.setLevel(level);
        student.setDateOfBirth(LocalDate.parse(data.getDateOfBirth()));
        student.setHomeTown(data.getHomeTown());
        studentsRepository.save(student);

        return "Student info updated successfully";
    }

    public SubjectScores getSubjectStudents(String subjectId) throws Exception {
        Subjects subject = subjectsRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new Exception("Subject Not Found"));

        String subjectName = subject.getSubjectName();

        Level level = subject.getLevel();
        if (level == null) {
            throw new Exception("Subject has no assigned level");
        }

        List<Students> students = level.getStudents();
        if (students == null || students.isEmpty()) {
            throw new Exception("Students not added to system");
        }

        List<StudentsScoresTable> subjectStudents = new ArrayList<>();
        for (Students student : students) {
            String studentId = student.getStudentId();
            String studentName = student.getFirstName() + " " + student.getLastName();

            StudentsScoresTable scoresTable = new StudentsScoresTable();

            // Check if student has scores for this subject
            SubjectScore score = subjectScoreRepository.findByStudent_StudentIdAndSubject_SubjectId(
                    studentId, subjectId
            ).orElse(null);

            if (score == null) {

                scoresTable.setStudentId(studentId);
                scoresTable.setStudentName(studentName);
                scoresTable.setExercise1Score("0");
                scoresTable.setClassTestScore("0");
                scoresTable.setExercise2Score("0");
                scoresTable.setProjectScore("0");
                scoresTable.setClassScore("0");
                scoresTable.setExamScore("0");
                scoresTable.setCalculatedExamScore("0");

            } else {

                scoresTable.setStudentId(studentId);
                scoresTable.setStudentName(studentName);
                scoresTable.setExercise1Score(String.valueOf(score.getExercise1Score()));
                scoresTable.setClassTestScore(String.valueOf(score.getClassTestScore()));
                scoresTable.setExercise2Score(String.valueOf(score.getExercise2Score()));
                scoresTable.setProjectScore(String.valueOf(score.getProjectScore()));
                scoresTable.setClassScore(String.valueOf(score.getClassScore()));
                scoresTable.setExamScore(String.valueOf(score.getExamScore()));
                scoresTable.setCalculatedExamScore(String.valueOf(score.getCalculatedExamScore()));

            }

            subjectStudents.add(scoresTable);
        }

        return new SubjectScores(subjectName, subjectId, subjectStudents);
    }

    public List<StudentListPrint> getLevelStudents(String levelId) throws Exception {
        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Level Not Found"));

        List<Students> students = level.getStudents();
        if (students == null || students.isEmpty()) {
            return Collections.emptyList();
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
        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Level Not Found"));

        if (attendanceDate.equals("no_date")) {

            List<Attendance> checkAttendance = attendanceRepository
                    .findByLevel_LevelIDAndDateMarked(levelId, LocalDate.now());
            if (checkAttendance != null && !checkAttendance.isEmpty()) {
                throw new Exception("Class attendance already marked. Switch to update if necessary");
            }

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

        LocalDate dateOfAttendance = LocalDate.parse(attendanceDate);
        List<Attendance> markedAttendance = attendanceRepository
                .findByLevel_LevelIDAndDateMarked(levelId, dateOfAttendance);
        if (dateOfAttendance.equals(LocalDate.now()) && markedAttendance == null
                || markedAttendance.isEmpty()) {
            throw new Exception("Today's attendance not marked");
        } else if (markedAttendance == null || markedAttendance.isEmpty()) {
            throw new Exception("Date must be today or earlier.");
        }

        List<Students> students = level.getStudents();
        for (Students student : students) {
            if (student.getStudentStatus() == StudentStatus.ACTIVE) {

                //Get today's attendance for students
                Attendance attendanceStatus = attendanceRepository.findByStudent_StudentIdAndDateMarked(
                        student.getStudentId(), LocalDate.parse(attendanceDate)
                ).orElse(null);

                if (attendanceStatus == null) {
                    StudentAttendance studentAttendance = new StudentAttendance(
                            level.getLevelID(),
                            student.getStudentId(),
                            student.getFirstName() + " " + student.getLastName(),
                            AttendanceStatus.ABSENT.toString() //Set status to absent if it's null
                    );
                    attendanceList.add(studentAttendance);
                } else {
                    StudentAttendance studentAttendance = new StudentAttendance(
                            level.getLevelID(),
                            student.getStudentId(),
                            student.getFirstName() + " " + student.getLastName(),
                            attendanceStatus.getStatus().toString()
                    );
                    attendanceList.add(studentAttendance);
                }
            }
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

        Attendance markedAttendance = attendanceRepository
                .findByStudent_StudentIdAndDateMarked(
                        studentId, LocalDate.now()
                ).orElse(null);
        if (markedAttendance != null) {
            return "marked";
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

    public List<StudentsHolder> getGradeStudents(String levelId) throws Exception {
        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Grade not found"));

        List<Students> levelStudents = level.getStudents();
        if (levelStudents == null || levelStudents.isEmpty()) {
            throw new Exception("Grade has no students");
        }

        List<StudentsHolder> studentsHolders = new ArrayList<>();
        for (Students student : levelStudents) {
            StudentsHolder stu = new StudentsHolder(
                    student.getStudentId(),
                    student.getFirstName() + " " + student.getLastName()
            );
            studentsHolders.add(stu);
        }

        return studentsHolders;
    }
}
