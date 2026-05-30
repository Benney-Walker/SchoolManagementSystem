package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Academics.GenerateStudentReport;
import com.codewithben.schoolmanagementsystem.DTO.Academics.SubjectReportDTO;
import com.codewithben.schoolmanagementsystem.DTO.Academics.ViewClassSemesterReport;
import com.codewithben.schoolmanagementsystem.DTO.Academics.ViewStudentsSubjectsResults;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final StudentsRepository studentsRepository;

    private final ResultsRepository resultsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final AttendanceRepository attendanceRepository;

    private final SubjectScoreRepository subjectScoreRepository;

    private final UtilityClass utilityClass;

    private final LoggingService loggingService;

    public ReportService(StudentsRepository studentsRepository, ResultsRepository resultsRepository,
                         LevelRepository levelRepository, SemesterRepository semesterRepository,
                         AttendanceRepository attendanceRepository, SubjectScoreRepository subjectScoreRepository,
                         UtilityClass utilityClass, LoggingService loggingService) {
        this.studentsRepository = studentsRepository;
        this.resultsRepository = resultsRepository;
        this.levelRepository = levelRepository;
        this.semesterRepository = semesterRepository;
        this.attendanceRepository = attendanceRepository;
        this.subjectScoreRepository = subjectScoreRepository;
        this.utilityClass = utilityClass;
        this.loggingService = loggingService;
    }

    public ResponseEntity<?> viewClassSemesterReport(String levelId, String semesterId, String staffId) {
        String logData = "Class Id: " + levelId + " semester Id: " + semesterId;

        //This finds the level and the semester with their IDs
        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.RESULTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Selected class not found");
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.RESULTS, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Selected semester not found");
        }

        //Get student list for report
        List<Students> studentsListForReport = utilityClass.getActiveStudents(level.getStudents());
        String classSize = String.valueOf(studentsListForReport.size());

        List<Subjects> levelSubjects = level.getSubjects();

        List<ViewClassSemesterReport> viewClassSemesterReports = new ArrayList<>();
        for (Students student : studentsListForReport) {
            //Check if the results is complete else stop the whole operation and throw an exception "Class results not complete"
            Results results = resultsRepository.findByStudentAndSemester(student, semester).orElse(null);
            if (results == null){
                loggingService.logActivity(LogType.RESULTS, logData, staffId, "FAILED");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(student.getFirstName() + "'s result not created");
            }

            //Check if all subjects scores are uploaded else throw an exception and cancel the whole operation
            List<SubjectScore> subjectScores = results.getSubjectScores();
            if (subjectScores.size() != levelSubjects.size()) {
                loggingService.logActivity(LogType.RESULTS, logData, staffId, "FAILED");
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Not all subjects are uploaded");
            }

            //Get results parameters
            String studentId = results.getStudent().getStudentId();
            String studentName = results.getStudent().getFirstName() + " " + results.getStudent().getLastName();
            String levelName = results.getLevel().getLevelName();
            String semesterName = results.getSemester().getSemesterName() + "(" + results.getSemester().getAcademicYear() + ")";
            String totalScore = results.getTotalScore().toString();
            String averageScore = results.getAverageScore().toString();
            String position = results.getPosition();

            List<ViewStudentsSubjectsResults> subScoresValues = getResultsSubjectScores(subjectScores);

            ViewClassSemesterReport classReport = new ViewClassSemesterReport(
                    studentId, studentName, levelName,
                    semesterName, position, totalScore,
                    averageScore, subScoresValues, classSize
            );

            viewClassSemesterReports.add(classReport);
        }

        return ResponseEntity.ok(viewClassSemesterReports);
    }

    private List<ViewStudentsSubjectsResults> getResultsSubjectScores(List<SubjectScore> subjectScores) {
        List<ViewStudentsSubjectsResults> subScoresValues = new ArrayList<>();

        for (SubjectScore subjectScore: subjectScores) {
            ViewStudentsSubjectsResults scores = new ViewStudentsSubjectsResults();
            scores.setSubjectName(subjectScore.getSubject().getSubjectName());
            scores.setExercise1Score(String.valueOf(subjectScore.getProjectWork()));
            scores.setClassTestScore(String.valueOf(subjectScore.getClassTest1()));
            scores.setExercise2Score(String.valueOf(subjectScore.getGroupWork()));
            scores.setProjectScore(String.valueOf(subjectScore.getClassTest2()));
            scores.setClassScore(String.valueOf(subjectScore.getClassScore()));
            scores.setExamScore(String.valueOf(subjectScore.getExamScore()));
            scores.setCalculatedExamScore(String.valueOf(subjectScore.getCalculatedExamScore()));
            scores.setTotal(String.valueOf(subjectScore.getTotalScore()));
            scores.setGrade(String.valueOf(subjectScore.getGrade()));
            scores.setDescription(subjectScore.getGradeDescriptor());

            subScoresValues.add(scores);
        }
        return subScoresValues;
    }

    public List<GenerateStudentReport> generateClassReports(String levelId, String semesterId,
                                                            String promotionGradeId) throws Exception{
        //Report List container
        List<GenerateStudentReport> generateStudentReports = new ArrayList<>();

        //This finds the level and the semester with their IDs
        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Level Not Found"));
        Semester semester = semesterRepository.findBySemesterID(semesterId)
                .orElseThrow(()-> new Exception("Semester not found"));

        //This gets the promotion grade if it's third term
        String nextGrade = getPromotionGradeName(promotionGradeId);

        //This gets the resuming date for the next semester
        String resumingDate = getResumingDate(level, semester);
        if (resumingDate == null) {
            throw new Exception("Resuming Date Not Found because next semester not added");
        }

        //Get student list for report
        List<Students> studentsListForReport = utilityClass.getActiveStudents(level.getStudents());
        String classSize = String.valueOf(studentsListForReport.size());

        for (Students student: studentsListForReport) {
            //Check if the results is complete else stop the whole operation and throw an exception "Class results not complete"
            Results results = resultsRepository
                    .findByStudentAndSemester(student, semester)
                    .orElseThrow(()-> new Exception(student.getFirstName() + "'s result not created"));

            //Check if all subjects scores are uploaded else throw an exception and cancel the whole operation
            List<Subjects> levelSubjects = level.getSubjects();
            List<SubjectScore> expectedSubjectsScores = subjectScoreRepository
                    .findByResults_ResultId(results.getResultId());
            if (expectedSubjectsScores.size() != levelSubjects.size()) {
                throw new Exception("Not all subject scores are added");
            }

            //Getting totalAttendance attendance
            List<Attendance> attendance = attendanceRepository.findByStudent_StudentIdAndSemester_SemesterID(
                    student.getStudentId(), semesterId
            );
            String totalAttendance = "-"; //String.valueOf(attendance.size());
            int totalMarked = 0;
            /*for (Attendance attendance1 : attendance) {
                if (attendance1.getStatus() == AttendanceStatus.PRESENT) {
                    totalMarked++;
                }
            }*/

            String studentId = results.getStudent().getStudentId();
            String studentName = results.getStudent().getFirstName() + " " + results.getStudent().getLastName();
            String levelName = results.getLevel().getLevelName();
            String semesterName = results.getSemester().getSemesterName() + "(" + results.getSemester().getAcademicYear() + ")";
            String totalScore = results.getTotalScore().toString();
            String averageScore = results.getAverageScore().toString();
            String position = results.getPosition();
            String academicYear = results.getSemester().getAcademicYear();
            String vacationDate = results.getSemester().getSemesterEndDate().toString();
            String attendanceMarked =  "-"; //String.valueOf(totalMarked);
            String instructorName = level.getStaff().getFirstName() + " " + level.getStaff().getLastName();
            String currentDate = LocalDate.now().toString();


            List<SubjectReportDTO> subjectReportDTOs = new ArrayList<>();

            List<SubjectScore> subjectScores = results.getSubjectScores();
            for (SubjectScore subjectScore: subjectScores) {
                SubjectReportDTO subjectReportDTO = new SubjectReportDTO(
                        subjectScore.getSubject().getSubjectName(),
                        subjectScore.getClassScore().toString(),
                        subjectScore.getCalculatedExamScore().toString(),
                        subjectScore.getTotalScore().toString(),
                        subjectScore.getGrade(),
                        subjectScore.getGradeDescriptor()
                );
                subjectReportDTOs.add(subjectReportDTO);
            }

            GenerateStudentReport generateStudentReport = new GenerateStudentReport(
                    studentId, studentName, levelName,
                    semesterName, position, totalScore,
                    averageScore, subjectReportDTOs,
                    academicYear, classSize, vacationDate,
                    resumingDate, attendanceMarked,
                    totalAttendance, instructorName, currentDate,
                    nextGrade
            );

            generateStudentReports.add(generateStudentReport);
        }
        return generateStudentReports;
    }

    private String getPromotionGradeName(String promotionGradeId) {
        String nextGrade = "-";
        if (!promotionGradeId.equals("no_promotion")) {
            Level promotionGrade = levelRepository.findByLevelID(promotionGradeId).orElse(null);
            if (promotionGrade != null) {
                nextGrade = promotionGrade.getLevelName();
            }
        }
        return nextGrade;
    }

    private String getResumingDate(Level level, Semester semester) {
        List<Semester> nextSemester = semesterRepository
                .findByInstitution_InstitutionId(level.getInstitution().getInstitutionId());
        return nextSemester.stream()
                .filter(s -> s.getSemesterStartDate().isAfter(semester.getSemesterEndDate()))
                .min(Comparator.comparing(Semester::getSemesterStartDate))
                .map(s -> s.getSemesterStartDate().toString())
                .orElse(null);
    }

    public ResponseEntity<?> promoteStudent(String studentId, String levelId, String staffId) {
        String logData = "Level Id: " + levelId + " studentId: " + studentId;

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logActivity(LogType.PROMOTION, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logActivity(LogType.PROMOTION, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }

        student.setLevel(level);
        studentsRepository.save(student);
        return ResponseEntity.ok().build();
    }
}
