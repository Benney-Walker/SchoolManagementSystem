package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Academics.*;
import com.codewithben.schoolmanagementsystem.DTO.Institution.StudentListPrint;
import com.codewithben.schoolmanagementsystem.Entity.*;
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

    public StudentService(StaffsRepository staffsRepository, LevelRepository levelRepository, StudentsRepository studentsRepository,
                          UtilityClass utilityClass, SubjectsRepository subjectsRepository, SemesterRepository semesterRepository,
                          ResultsRepository resultsRepository, InstitutiionRepository institutionRepository, SubjectScoreRepository subjectScoreRepository) {
        this.staffsRepository = staffsRepository;
        this.levelRepository = levelRepository;
        this.studentsRepository = studentsRepository;
        this.utilityClass = utilityClass;
        this.subjectsRepository = subjectsRepository;
        this.semesterRepository = semesterRepository;
        this.resultsRepository = resultsRepository;
        this.institutionRepository = institutionRepository;
        this.subjectScoreRepository = subjectScoreRepository;
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
    public String addStudentSubjectScores(String studentId, String subjectId, Double classScore, Double examScore) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId)
                .orElseThrow(() -> new Exception("Student Not Found"));

        Subjects subject = subjectsRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new Exception("Subject Not Found"));

        String semesterId = subject.getSemester().getSemesterID();
        String levelId = subject.getLevel().getLevelID();

        // Get or create Results for this student + semester + level
        Results result = resultsRepository
                .findByStudent_StudentIdAndSemester_SemesterIDAndLevel_LevelID(studentId, semesterId, levelId)
                .orElse(null);

        if (result == null) {
            result = new Results();
            result.setStudent(student);
            result.setLevel(subject.getLevel());
            result.setSemester(subject.getSemester());
            result.setCreatedAt(LocalDate.now());
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
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
            existingScore.setExamScore(examScore);
            existingScore.setTotalScore(totalScore);
            existingScore.setGrade(utilityClass.extractGrade(totalScore));
            existingScore.setRemarks(utilityClass.extractDescription(totalScore));
            subjectScoreRepository.save(existingScore);
        } else {
            // Create new
            SubjectScore subjectScore = new SubjectScore();
            subjectScore.setStudent(student);
            subjectScore.setSubject(subject);
            subjectScore.setResults(result);  // 👈 Link to result
            subjectScore.setClassScore(classScore);
            subjectScore.setExamScore(examScore);
            subjectScore.setTotalScore(totalScore);
            subjectScore.setGrade(utilityClass.extractGrade(totalScore));
            subjectScore.setRemarks(utilityClass.extractDescription(totalScore));
            subjectScoreRepository.save(subjectScore);
        }

        // Recalculate result totals
        updateResultTotals(result);

        return "success";
    }

    private void updateResultTotals(Results result) {
        List<SubjectScore> scores = subjectScoreRepository.findByResults_ResultId(result.getResultId());

        if (scores == null || scores.isEmpty()) {
            result.setTotalScore(0.0);
            result.setAverageScore(0.0);
        } else {
            double total = scores.stream()
                    .mapToDouble(s -> s.getTotalScore() != null ? s.getTotalScore() : 0.0)
                    .sum();
            result.setTotalScore(total);
            result.setAverageScore(total / scores.size());
        }

        result.setUpdatedAt(LocalDate.now());
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
        String position = String.valueOf(results.getPosition());
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

                    return new RecentStudentsDTO(studentId, fullName, studentLevel, dateOfRegistration);
                }
        ).collect(Collectors.toList());
    }

    private SubjectScore getSubjectScore(Double classScore, Double examScore, Subjects subject, Students student) {
        //Add new subject scores for the student
        SubjectScore subjectScore = new SubjectScore();
        subjectScore.setClassScore(classScore);
        subjectScore.setExamScore(examScore);
        Double totalScore = subjectScore.getExamScore() + subjectScore.getExamScore();
        subjectScore.setTotalScore(totalScore);
        subjectScore.setRemarks(utilityClass.extractDescription(totalScore));
        subjectScore.setGrade(utilityClass.extractGrade(totalScore));
        subjectScore.setSubject(subject);
        subjectScore.setStudent(student);
        subjectScoreRepository.save(subjectScore);
        return subjectScore;
    }

    public String updateStudentPersonalData(String studentId, String gender, String dataOfBirth, String guardianName,
                                            String guardianContact) throws Exception {
        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            throw new Exception("Student Not Found");
        }

        student.setGender(gender);
        student.setDateOfBirth(LocalDate.parse(dataOfBirth));
        student.setParentName(guardianName);
        student.setParentPhoneNumber(guardianContact);
        studentsRepository.save(student);
        return "success";
    }

    public List<StudentsTableDTO> loadGradeStudents(String staffId) throws Exception {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            throw new Exception("Staff Not Found");
        }

        Level level = staff.getLevel();
        if (level == null) {
            throw new Exception("No Grade is assigned to staff");
        }

        List<Students> getStudents = level.getStudents();

        return getStudents.stream().map(
                students -> {
                    String studentId = students.getStudentId();
                    String fullName = students.getFirstName() + " " + students.getLastName();
                    String getGender = students.getGender();
                    String homeTown = students.getHomeTown();
                    String parentName = students.getParentName();
                    String parentPhoneNumber = students.getParentPhoneNumber();

                    return new StudentsTableDTO(studentId, fullName, getGender, homeTown, parentName, parentPhoneNumber);
                }
        ).collect(Collectors.toList());
    }

    public SubjectScores getSubjectStudents(String subjectId) throws Exception {
        Subjects subject = subjectsRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new Exception("Subject Not Found"));

        String subjectName = subject.getSubjectName();
        List<GetSubjectStudents> subjectStudents = new ArrayList<>();

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
            subjectStudents.add(new GetSubjectStudents(studentId, studentName, classScore, examScore));
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

}
