package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.Contants.StudentStatus;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.AbsenteesView;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.StudentAttendance;
import com.codewithben.schoolmanagementsystem.DTO.Report.StudentSubjectReport;
import com.codewithben.schoolmanagementsystem.DTO.Result.SaveStudentScores;
import com.codewithben.schoolmanagementsystem.DTO.Result.StudentResult;
import com.codewithben.schoolmanagementsystem.DTO.Students.FindStudentDTO;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsHolder;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsScoresTable;
import com.codewithben.schoolmanagementsystem.DTO.Students.UpdateStudentPersonalData;
import com.codewithben.schoolmanagementsystem.DTO.Subject.SubjectScores;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Entity.Attendance;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
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

    private final LoggingService loggingService;

    //Method for adding new student
    @Transactional
    public ResponseEntity<?> addNewStudent(String firstName, String lastName, String gender, LocalDate dateOfBirth, String hometown,
                                String parentName, String parentContact, String levelId, String staffId) {
        String logData = "First Name: " + firstName +
                ", Last Name: " + lastName +
                ", Gender: " + gender +
                ", DOB: " + dateOfBirth +
                ", Hometown: " + hometown +
                ", Parent: " + parentName +
                ", Guardian Contact: " + parentContact +
                ", Level ID: " + levelId;

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.NEW_STUDENT, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not save student");
        }

        Institution institution = staff.getInstitution();

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.NEW_STUDENT, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Selected class not found");
        }

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
            return ResponseEntity.ok(studentId);
        }

        loggingService.logActivity(LogType.NEW_STUDENT, logData, staffId, "SUCCESS");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Student already exist");
    }

    @Transactional
    public ResponseEntity<?> addStudentSubjectScores(SaveStudentScores scores, String staffId, String subjectId, String semesterId) {
        String logData = "Student ID: " + scores.getStudentId() +
                ", Ex1: " + scores.getProjectScore() +
                ", Class Test: " + scores.getClassTest1Score() +
                ", Ex2: " + scores.getGroupWorkScore() +
                ", Project: " + scores.getClassTest2Score() +
                ", Class Score: " + scores.getClassScore() +
                ", Exam Score: " + scores.getExamScore() +
                ", Calculated Exam: " + scores.getCalculatedExamScore();

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);

        Students student = studentsRepository.findByStudentId(scores.getStudentId()).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.SAVE_SCORES, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Student not found"
            ));
        }

        List<GradeSystem> gradeSystem = gradeSystemRepository.findAllByInstitution_InstitutionId(
                student.getInstitution().getInstitutionId()
        );

        if (gradeSystem == null || gradeSystem.isEmpty()) {
            loggingService.logActivity(LogType.SAVE_SCORES, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Grade information not found"
            ));
        }

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logActivity(LogType.SAVE_SCORES, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid subject ID"
            ));
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.SAVE_SCORES, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid semester ID"
            ));
        }


        // Get or create Results for this student + semester + level
        Results result = resultsRepository
                .findByStudent_StudentIdAndSemester_SemesterID(
                        student.getStudentId(), semesterId)
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
        subjectScore.setProjectWork(Double.parseDouble(scores.getProjectScore()));
        subjectScore.setClassTest1(Double.parseDouble(scores.getClassTest1Score()));
        subjectScore.setGroupWork(Double.parseDouble(scores.getGroupWorkScore()));
        subjectScore.setClassTest2(Double.parseDouble(scores.getClassTest2Score()));
        subjectScore.setClassScore(classScore);
        subjectScore.setExamScore(Double.parseDouble(scores.getExamScore()));
        subjectScore.setCalculatedExamScore(calculatedExamScore);
        subjectScore.setGrade(
                utilityClass.extractGrade(totalScore, student.getInstitution().getInstitutionId())
        );
        subjectScore.setGradeDescriptor(
                utilityClass.extractDescription(totalScore, student.getInstitution().getInstitutionId())
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

        loggingService.logActivity(LogType.SAVE_SCORES, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    private void updateResultTotals(Results result, Staffs staff, Subjects subject) {
        List<SubjectScore> scores = subjectScoreRepository.findByResults_ResultId(result.getResultId());

        double total = 0.0;
        if (scores == null || scores.isEmpty()) {
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
        } else {

            for(SubjectScore score : scores) {
                total += score.getTotalScore();
            }

            result.setTotalScore(total);
            result.setAverageScore(total / scores.size());
        }

        result.setUpdatedAt(LocalDate.now());
        result.setUpdatedBy(staff);
        resultsRepository.save(result);

        utilityClass.reArrangePositions(subject, result.getSemester());
    }

    public ResponseEntity<?> findStudentResults(String studentId, String semesterId, String levelId, String staffId) {
        String logData = "Student Id: " + " Semester Id: " + semesterId + " class Id: " + levelId;

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.RESULTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }

        Results results = resultsRepository.findByStudent_StudentIdAndSemester_SemesterID(
                studentId, semesterId
        ).orElse(null);
        if (results == null) {
            loggingService.logActivity(LogType.RESULTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Results not found");
        }

        List<StudentSubjectReport> subjectResults = new ArrayList<>();

            List<SubjectScore> subjectScores = results.getSubjectScores();

            for (SubjectScore subjectScore : subjectScores) {
                if (subjectScore.getStudent().getStudentId().equals(studentId)) {
                    StudentSubjectReport studentSubjectReport = new StudentSubjectReport(
                            subjectScore.getSubject().getSubjectName(),
                            String.valueOf(subjectScore.getTotalScore()),
                            subjectScore.getGrade(),
                            subjectScore.getGradeDescriptor()
                    );
                    subjectResults.add(studentSubjectReport);
                }
            }

        String studentID = results.getStudent().getStudentId();
        String studentName = student.getFirstName() + " " + student.getLastName();
        String semesterName = results.getSemester().getSemesterName();
        String levelName = results.getLevel().getLevelName();
        Double totalScore = results.getTotalScore();
        Double averageScore = results.getAverageScore();
        String position = results.getPosition();

        loggingService.logActivity(LogType.RESULTS, logData, staffId, "SUCCESS");
        return ResponseEntity.ok(new StudentResult(
                studentID, studentName, levelName, semesterName, totalScore,
                averageScore, position, subjectResults
        ));
    }

    public ResponseEntity<?> findStudent(String studentId, String staffId) {
        String logData = "Student Id: " + studentId;

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.FETCH_STUDENT_DETAILS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }

        loggingService.logActivity(LogType.FETCH_STUDENT_DETAILS, logData, staffId, "SUCCESS");
        return ResponseEntity.ok(getStudentData(student));
    }

    private FindStudentDTO getStudentData(Students student) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> countTotalStudents(String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.COUNT_STUDENTS, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not count students");
        }

        List<Students> students = utilityClass.getActiveStudents(staff.getInstitution().getStudents());
        if (students == null || students.isEmpty()) {
            loggingService.logActivity(LogType.COUNT_STUDENTS, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Institution has no students");
        }

        loggingService.logActivity(LogType.COUNT_STUDENTS, "N/A", staffId, "SUCCESS");
        return ResponseEntity.ok(students.size());
    }

    //This loads all the absentees for the day
    public ResponseEntity<?> findTodaysAbsentees(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.ATTENDANCE_ABSENTEES, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not load absentees");
        }

        List<Level> levels = staff.getInstitution().getLevels();
        if (levels == null || levels.isEmpty()) {
            loggingService.logActivity(LogType.ATTENDANCE_ABSENTEES, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Institution has no class");
        }

        LocalDate currentDate = LocalDate.now();
        List<AbsenteesView> absenteesViews = new ArrayList<>();

        for (Level level : levels) {

            List<Attendance> absentees =
                    attendanceRepository.findByStatusAndDateMarkedAndLevel_levelID(
                            AttendanceStatus.ABSENT,
                            currentDate,
                            level.getLevelID()
                    );

            if (absentees == null || absentees.isEmpty()) {
                continue; // don't kill the whole response
            }

            for (Attendance attendance : absentees) {

                Students student = attendance.getStudent();
                Level attendanceLevel = attendance.getLevel();
                Staffs levelStaff = attendanceLevel.getStaff();

                AbsenteesView view = new AbsenteesView();
                view.setStudentId(student.getStudentId());
                view.setStudentName(student.getFirstName() + " " + student.getLastName());
                view.setStudentGrade(attendanceLevel.getLevelName());
                view.setInstructorName(levelStaff.getFirstName() + " " + levelStaff.getLastName());
                view.setInstructorId(levelStaff.getStaffId());

                absenteesViews.add(view);
            }
        }

        loggingService.logActivity(LogType.ATTENDANCE_ABSENTEES, "N/A", staffId, "SUCCESS");
        return ResponseEntity.ok(absenteesViews);
    }

    public ResponseEntity<?> updateStudentPersonalData(UpdateStudentPersonalData data, String staffId) {
        String logData = "Student ID: " + data.getStudentId() +
                ", First Name: " + data.getFirstName() +
                ", Last Name: " + data.getLastName() +
                ", Gender: " + data.getGender() +
                ", DOB: " + data.getDateOfBirth() +
                ", Parent: " + data.getParentName() +
                ", Parent Phone: " + data.getParentPhoneNumber() +
                ", Grade ID: " + data.getGradeId() +
                ", Status: " + data.getStatus() +
                ", Hometown: " + data.getHomeTown();

        if (data.getParentPhoneNumber().length() != 10) {
            loggingService.logActivity(LogType.UPDATE_STUDENT_DETAILS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parent phone number");
        }

        Students student = studentsRepository.findByStudentId(data.getStudentId()).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.UPDATE_STUDENT_DETAILS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }

        Level level = levelRepository.findByLevelID(data.getGradeId()).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.UPDATE_STUDENT_DETAILS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Class not found");
        }

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

        loggingService.logActivity(LogType.UPDATE_STUDENT_DETAILS, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getSubjectStudents(String subjectId, String staffId) {
        String logData = "Subject Id: " + subjectId;

        Subjects subject = subjectsRepository.findBySubjectId(subjectId).orElse(null);
        if (subject == null) {
            loggingService.logActivity(LogType.FETCH_STUDENTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Subject not found"
            ));
        }

        //Gets the active students
        List<Students> students =
                utilityClass.getActiveStudents(subject.getLevel().getStudents());

        if (students == null || students.isEmpty()) {
            loggingService.logActivity(LogType.FETCH_STUDENTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Class has no students"
            ));
        }

        String subjectName = subject.getSubjectName();
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
                scoresTable.setClassTest1Score("0");
                scoresTable.setClassTest2Score("0");
                scoresTable.setProjectScore("0");
                scoresTable.setGroupWorkScore("0");
                scoresTable.setClassScore("0");
                scoresTable.setExamScore("0");
                scoresTable.setCalculatedExamScore("0");

            } else {

                scoresTable.setStudentId(studentId);
                scoresTable.setStudentName(studentName);
                scoresTable.setClassTest1Score(
                        String.valueOf(score.getClassTest1())
                );
                scoresTable.setClassTest2Score(
                        String.valueOf(score.getClassTest2())
                );
                scoresTable.setGroupWorkScore(
                        String.valueOf(score.getGroupWork())
                );
                scoresTable.setProjectScore(
                        String.valueOf(score.getProjectWork())
                );
                scoresTable.setClassScore(
                        String.valueOf(score.getClassScore())
                );
                scoresTable.setExamScore(
                        String.valueOf(score.getExamScore())
                );
                scoresTable.setCalculatedExamScore(
                        String.valueOf(score.getCalculatedExamScore())
                );

            }

            subjectStudents.add(scoresTable);
        }

        loggingService.logActivity(LogType.FETCH_STUDENTS, logData, staffId, "SUCCESS");
        return ResponseEntity.ok(
                new SubjectScores(subjectName, subjectId, subjectStudents)
        );
    }

    public ResponseEntity<?> loadStudentForAttendance(String levelId, String attendanceDate, String staffId) {
        String logData = "Class Id: " + levelId + " Attendance date: " + attendanceDate;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.FETCH_STUDENTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        LocalDate date = LocalDate.parse(attendanceDate, DateTimeFormatter.ISO_DATE);

        List<Attendance> markedAttendance =
                attendanceRepository.findByLevel_LevelIDAndDateMarked(levelId, date);

        List<Students> students = utilityClass.getActiveStudents(level.getStudents());

        List<StudentAttendance> attendanceList = new ArrayList<>();

        for (Students student : students) {

            String status = AttendanceStatus.ABSENT.name();

            if (markedAttendance != null) {
                for (Attendance attendance : markedAttendance) {
                    if (attendance.getStudent().getStudentId().equals(student.getStudentId())) {
                        status = attendance.getStatus().name();
                        break;
                    }
                }
            }

            attendanceList.add(new StudentAttendance(
                    level.getLevelID(),
                    student.getStudentId(),
                    student.getFirstName() + " " + student.getLastName(),
                    status
            ));
        }

        loggingService.logActivity(LogType.FETCH_STUDENTS, logData, staffId, "SUCCESS");
        return ResponseEntity.ok(attendanceList);
    }

    public ResponseEntity<?> markStudentAttendance(String studentId, String levelId, String status, String date, String staffId) {
        String logData = "Student Id: " + studentId + " levelId: " + levelId + " status: " + status + " date: " + date;

        //Check if date is accepted for attendance
        LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        if (!utilityClass.isSchoolDay(selectedDate)) {
            loggingService.logActivity(LogType.ATTENDANCE, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance can't be marked on weekends"
            ));
        }

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.ATTENDANCE, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        String currentSemesterId = utilityClass.getCurrentSemesterId(
                student.getInstitution().getInstitutionId()
        );
        Semester semester = semesterRepository.findBySemesterID(currentSemesterId).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.ATTENDANCE, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Internal server error! Please try again"
            ));
        }

        List<SchoolHoliday> holidays = semester.getSchoolHoliday();
        if (holidays != null && !holidays.isEmpty()) {
            for (SchoolHoliday holiday : holidays) {

                if (!selectedDate.isBefore(holiday.getStartDate()) && !selectedDate.isAfter(holiday.getEndDate())) {
                    loggingService.logActivity(LogType.ATTENDANCE, logData, staffId, "FAILED");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                            "message", "Today is holiday"
                    ));
                }
            }
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.ATTENDANCE, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid level Id"
            ));
        }

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.ATTENDANCE, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Server error! Please try again"
            ));
        }

        Attendance todaysAttendance = attendanceRepository.findByStudent_StudentIdAndDateMarked(
                        studentId, LocalDate.now()
                ).orElse(null);
        if (todaysAttendance == null) {
            todaysAttendance = new Attendance();
            todaysAttendance.setStudent(student);
            todaysAttendance.setSemester(semester);
            todaysAttendance.setLevel(level);
            todaysAttendance.setDateMarked(selectedDate);
            todaysAttendance.setMarkedBy(staff);

            List<Attendance> studentAttendance = student.getAttendance();
            if (studentAttendance == null || studentAttendance.isEmpty()) {
                studentAttendance = new ArrayList<>();
            }
            studentAttendance.add(todaysAttendance);
            studentsRepository.save(student);
        }

        todaysAttendance.setStatus(AttendanceStatus.valueOf(status));
        attendanceRepository.save(todaysAttendance);

        loggingService.logActivity(LogType.ATTENDANCE, logData, staffId, "SUCCESS");
        return ResponseEntity.ok().build();
    }


    public ResponseEntity<?> getGradeStudents(String levelId, String staffId) {
        String logData = "Class Id: " + levelId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.FETCH_STUDENTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        List<Students> levelStudents = utilityClass.getActiveStudents(level.getStudents());
        if (levelStudents == null || levelStudents.isEmpty()) {
            loggingService.logActivity(LogType.FETCH_STUDENTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Class has no students"
            ));
        }

        List<StudentsHolder> studentsHolders = new ArrayList<>();
        for (Students student : levelStudents) {
            StudentsHolder stu = new StudentsHolder(
                    student.getStudentId(),
                    student.getFirstName() + " " + student.getLastName()
            );
            studentsHolders.add(stu);
        }

        loggingService.logActivity(LogType.FETCH_STUDENTS, logData, staffId, "SUCCESS");
        return ResponseEntity.ok(studentsHolders);
    }

    public int getTotalAttendanceCount(Semester semester) {
        LocalDate startDate = semester.getSemesterStartDate();
        LocalDate endDate = semester.getSemesterEndDate();

        int totalAttendance = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            DayOfWeek day = date.getDayOfWeek();
            if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
                continue;
            }

            boolean isHoliday = false;

            for (SchoolHoliday holiday : semester.getSchoolHoliday()) {

                if (!date.isBefore(holiday.getStartDate()) && !date.isAfter(holiday.getEndDate())) {
                    isHoliday = true;
                    break;
                }
            }

            if (!isHoliday) {
                totalAttendance++;
            }
        }

        return totalAttendance;
    }

    public int getStudentPresentAttendance(String studentId, String semesterId) {

        List<Attendance> presentDays = attendanceRepository.findByStudent_StudentIdAndSemester_SemesterIDAndStatus(
                studentId, semesterId, AttendanceStatus.PRESENT
        );
        if (presentDays == null || presentDays.isEmpty()) {
            return 0;
        }
        return presentDays.size();
    }
}
