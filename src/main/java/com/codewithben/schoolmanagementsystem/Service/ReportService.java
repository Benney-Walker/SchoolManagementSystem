package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Academics.GenerateStudentReport;
import com.codewithben.schoolmanagementsystem.DTO.Academics.SubjectReportDTO;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {
    private final StudentsRepository studentsRepository;

    private final ResultsRepository resultsRepository;

    private final LevelRepository levelRepository;

    private final SemesterRepository semesterRepository;

    private final AttendanceRepository attendanceRepository;

    public ReportService(StudentsRepository studentsRepository, ResultsRepository resultsRepository,
                         LevelRepository levelRepository, SemesterRepository semesterRepository,
                         AttendanceRepository attendanceRepository) {
        this.studentsRepository = studentsRepository;
        this.resultsRepository = resultsRepository;
        this.levelRepository = levelRepository;
        this.semesterRepository = semesterRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<GenerateStudentReport> generateClassReports(String levelId, String semesterId,
                                                            String promotionGradeId) throws Exception{
        Level level = levelRepository.findByLevelID(levelId)
                .orElseThrow(() -> new Exception("Level Not Found"));
        String nextGrade = null;
        if (promotionGradeId != null) {
            Level promotionGrade = levelRepository.findByLevelID(promotionGradeId).orElse(null);
            if (promotionGrade != null) {
                nextGrade = promotionGrade.getLevelName();
            }
        }

        Semester semester = semesterRepository.findBySemesterID(semesterId)
                .orElseThrow(()-> new Exception("Semester not found"));


        List<Semester> nextSemester = semesterRepository
                .findByInstitution_InstitutionId(level.getInstitution().getInstitutionId());
        String resumingDate = null;
        for (Semester findNextSemester : nextSemester) {
            if(findNextSemester.getSemesterStartDate().isAfter(semester.getSemesterEndDate())) {
                resumingDate = findNextSemester.getSemesterStartDate().toString();
            }
        }


        List<Students> students = level.getStudents();
        String classSize = String.valueOf(students.size());

        List<GenerateStudentReport> generateStudentReports = new ArrayList<>();

        for (Students student: students) {
            if (student.getStudentStatus().toString().equals("ACTIVE") &&
            student.getResults() == null) {
                throw new Exception("Class results not complete for " + student.getStudentId());
            }
            Results results = resultsRepository
                    .findByStudentAndSemester(student, semester)
                    .orElseThrow(()-> new Exception("Student " + student.getStudentId() + "result not found"));

            //Getting total attendance
            List<Attendance> attendance = student.getAttendance();
            String total = String.valueOf(attendance.size());
            int totalMarked = 0;
            for (Attendance attendance1 : attendance) {
                if (attendance1.getStatus().toString().equals("PRESENT")) {
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


            List<SubjectScore> subjectScores = results.getSubjectScores();
            List<SubjectReportDTO> subjectReportDTOs = new ArrayList<>();

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
