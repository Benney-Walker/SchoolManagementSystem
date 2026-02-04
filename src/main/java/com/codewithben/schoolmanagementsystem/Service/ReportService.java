package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Contants.StudentStatus;
import com.codewithben.schoolmanagementsystem.DTO.Academics.GenerateStudentReport;
import com.codewithben.schoolmanagementsystem.DTO.Academics.SubjectReportDTO;
import com.codewithben.schoolmanagementsystem.DTO.Academics.ViewClassSemesterReport;
import com.codewithben.schoolmanagementsystem.DTO.Academics.ViewStudentsSubjectsResults;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {
    private final StudentsRepository studentsRepository;

    private final ResultsRepository resultsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final AttendanceRepository attendanceRepository;

    private final SubjectScoreRepository subjectScoreRepository;

    public ReportService(StudentsRepository studentsRepository, ResultsRepository resultsRepository,
                         LevelRepository levelRepository, SemesterRepository semesterRepository,
                         AttendanceRepository attendanceRepository, SubjectScoreRepository subjectScoreRepository) {
        this.studentsRepository = studentsRepository;
        this.resultsRepository = resultsRepository;
        this.levelRepository = levelRepository;
        this.semesterRepository = semesterRepository;
        this.attendanceRepository = attendanceRepository;
        this.subjectScoreRepository = subjectScoreRepository;
    }

    public List<ViewClassSemesterReport> viewClassSemesterReport(String levelId, String semesterId) throws Exception {
        List<ViewClassSemesterReport> viewClassSemesterReports = new ArrayList<>();

        //This finds the level and the semester with their IDs
        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Level Not Found"));
        Semester semester = semesterRepository.findBySemesterID(semesterId)
                .orElseThrow(()-> new Exception("Semester not found"));

        //Get student list for report
        List<Students> studentsListForReport = checkStudentStatus(level.getStudents());
        String classSize = String.valueOf(studentsListForReport.size());

        for (Students student : studentsListForReport) {
            //Check if the results is complete else stop the whole operation and throw an exception "Class results not complete"
            Results results = resultsRepository
                    .findByStudentAndSemester(student, semester)
                    .orElseThrow(()-> new Exception("Class results not complete"));

            //Check if all subjects scores are uploaded else throw an exception and cancel the whole operation
            List<Subjects> levelSubjects = level.getSubjects();
            List<SubjectScore> expectedSubjectsScores = subjectScoreRepository
                    .findByResults_ResultId(results.getResultId());
            if (expectedSubjectsScores.size() != levelSubjects.size()) {
                throw new Exception("Class results not complete");
            }

            //Get results parameters
            String studentId = results.getStudent().getStudentId();
            String studentName = results.getStudent().getFirstName() + " " + results.getStudent().getLastName();
            String levelName = results.getLevel().getLevelName();
            String semesterName = results.getSemester().getSemesterName() + "(" + results.getSemester().getAcademicYear() + ")";
            String totalScore = results.getTotalScore().toString();
            String averageScore = results.getAverageScore().toString();
            String position = results.getPosition();

            List<ViewStudentsSubjectsResults> subScoresValues = new ArrayList<>();

            List<SubjectScore> subjectScores = results.getSubjectScores();
            for (SubjectScore subjectScore: subjectScores) {
                ViewStudentsSubjectsResults scores = new ViewStudentsSubjectsResults();
                scores.setSubjectName(subjectScore.getSubject().getSubjectName());
                scores.setExercise1Score(String.valueOf(subjectScore.getExercise1Score()));
                scores.setClassTestScore(String.valueOf(subjectScore.getClassTestScore()));
                scores.setExercise2Score(String.valueOf(subjectScore.getExercise2Score()));
                scores.setProjectScore(String.valueOf(subjectScore.getProjectScore()));
                scores.setClassScore(String.valueOf(subjectScore.getClassScore()));
                scores.setExamScore(String.valueOf(subjectScore.getExamScore()));
                scores.setCalculatedExamScore(String.valueOf(subjectScore.getCalculatedExamScore()));
                scores.setTotal(String.valueOf(subjectScore.getTotalScore()));
                scores.setGrade(String.valueOf(subjectScore.getGrade()));
                scores.setDescription(subjectScore.getRemarks());

                subScoresValues.add(scores);
            }

            ViewClassSemesterReport classReport = new ViewClassSemesterReport(
                    studentId, studentName, levelName,
                    semesterName, position, totalScore,
                    averageScore, subScoresValues, classSize
            );

            viewClassSemesterReports.add(classReport);
        }

        return viewClassSemesterReports;
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
        String nextGrade = getPromotionGrade(promotionGradeId);

        //This gets the resuming date for the next semester
        String resumingDate = getResumingDate(level, semester);
        if (resumingDate == null) {
            throw new Exception("Resuming Date Not Found because next semester not added");
        }

        //Get student list for report
        List<Students> studentsListForReport = checkStudentStatus(level.getStudents());
        String classSize = String.valueOf(studentsListForReport.size());

        for (Students student: studentsListForReport) {
            //Check if the results is complete else stop the whole operation and throw an exception "Class results not complete"
            Results results = resultsRepository
                    .findByStudentAndSemester(student, semester)
                    .orElseThrow(()-> new Exception("Class results not complete"));

            //Check if all subjects scores are uploaded else throw an exception and cancel the whole operation
            List<Subjects> levelSubjects = level.getSubjects();
            List<SubjectScore> expectedSubjectsScores = subjectScoreRepository
                    .findByResults_ResultId(results.getResultId());
            if (expectedSubjectsScores.size() != levelSubjects.size()) {
                throw new Exception("Class results not complete");
            }

            //Getting total attendance
            List<Attendance> attendance = attendanceRepository.findByStudent_StudentIdAndSemester_SemesterID(
                    student.getStudentId(), semesterId
            );
            String total = String.valueOf(attendance.size());
            int totalMarked = 0;
            for (Attendance attendance1 : attendance) {
                if (attendance1.getStatus() == AttendanceStatus.PRESENT) {
                    totalMarked++;
                }
            }

            String studentId = results.getStudent().getStudentId();
            String studentName = results.getStudent().getFirstName() + " " + results.getStudent().getLastName();
            String levelName = results.getLevel().getLevelName();
            String semesterName = results.getSemester().getSemesterName() + "(" + results.getSemester().getAcademicYear() + ")";
            String totalScore = results.getTotalScore().toString();
            String averageScore = results.getAverageScore().toString();
            String position = results.getPosition();
            String academicYear = results.getSemester().getAcademicYear();
            String vacationDate = results.getSemester().getSemesterEndDate().toString();
            String attendanceMarked = String.valueOf(totalMarked);
            String instructorName = level.getStaff().getFirstName() + " " + level.getStaff().getLastName();
            String currentDate = LocalDate.now().toString();


            List<SubjectReportDTO> subjectReportDTOs = new ArrayList<>();

            List<SubjectScore> subjectScores = results.getSubjectScores();
            for (SubjectScore subjectScore: subjectScores) {
                SubjectReportDTO subjectReportDTO = new SubjectReportDTO(
                        subjectScore.getSubject().getSubjectName(),
                        subjectScore.getClassScore().toString(),
                        subjectScore.getExamScore().toString(),
                        subjectScore.getTotalScore().toString(),
                        subjectScore.getGrade(),
                        subjectScore.getRemarks()
                );
                subjectReportDTOs.add(subjectReportDTO);
            }

            GenerateStudentReport generateStudentReport = new GenerateStudentReport(
                    studentId, studentName, levelName,
                    semesterName, position, totalScore,
                    averageScore, subjectReportDTOs,
                    academicYear, classSize, vacationDate,
                    resumingDate, attendanceMarked,
                    total, instructorName, currentDate,
                    nextGrade
            );

            generateStudentReports.add(generateStudentReport);
        }
        return generateStudentReports;
    }

    private String getPromotionGrade(String promotionGradeId) {
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

    private List<Students> checkStudentStatus(List<Students> students) {
        List<Students> studentsListForReport = new ArrayList<>();
        for (Students student: students) {
            if (student.getStudentStatus() == StudentStatus.ACTIVE) {
                studentsListForReport.add(student);
            }
        }
        return studentsListForReport;
    }

    public String movePassedStudents(String staffId, String nextLevelId, String semesterId,
                                     double passedTotalScore) throws Exception{
        Level level = levelRepository.findByStaff_StaffId(staffId)
                .orElseThrow(()-> new Exception("Staff is not assigned grade"));

        Level nextLevel = levelRepository.findByLevelID(nextLevelId).
                orElseThrow(()-> new Exception("Next level not found"));

        Semester semester = semesterRepository.findBySemesterID(semesterId).
                orElseThrow(()-> new Exception("Semester not found"));

        List<Students> students = level.getStudents();
        int count = 0;
        for (Students student: students) {
            Results result = resultsRepository.findByStudent_StudentIdAndSemester_SemesterIDAndLevel_LevelID(
                    student.getStudentId(),
                    semester.getSemesterID(),
                    level.getLevelID()
            ).orElseThrow(()-> new Exception("Student " + student.getStudentId() + "result not found"));

            if (result.getTotalScore() >= passedTotalScore) {
                student.setLevel(nextLevel);
                studentsRepository.save(student);
                count++;
            }
        }

        return count + "Students passed to " + nextLevel.getLevelName();
    }
}
