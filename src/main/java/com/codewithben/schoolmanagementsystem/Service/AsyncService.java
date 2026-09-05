package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Constants.SpecialPaymentType;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsResponse;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Attendance.DailyAttendance;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeeCreation;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeesUpdate;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Results.CreateResults;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Results.ResultsUpdate;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Student.NewStudentFee;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Interface.SmsInterface;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncService {

    private final FeesRepository feesRepository;

    private final UtilityClass utilityClass;

    private final StudentFeeRecordRepository studentFeeRecordRepository;

    private final LevelRepository levelRepository;

    private final StudentsRepository studentsRepository;

    private final SubjectsRepository subjectsRepository;

    private final ResultsRepository resultsRepository;

    private final StaffsRepository staffsRepository;

    private final SubjectScoreRepository subjectScoreRepository;

    private final AttendanceRecordsRepository attendanceRecordsRepository;

    private final SemesterRepository semesterRepository;

    private final MessagesRepository messagesRepository;

    private final SmsInterface smsInterface;

    @Value("${attendance.default.message}")
    private String attendanceMessage;

    @Transactional
    public void createIndividualFeeRecord(FeeCreation feeCreation) {

        List<Fees> feesList = feesRepository.findBySemester_SemesterIDAndLevel_LevelIDIn(
                feeCreation.getSemesterId(), feeCreation.getLevelIds()
        );

        for (Fees fees : feesList) {

            List<Students> studentsList = utilityClass.getActiveStudents(fees.getLevel().getStudents());
            if (studentsList == null || studentsList.isEmpty()) {
                log.error("ASYNC: Could not create results, no students found");
                continue;
            }

            List<LevelSpecialPayment> specialPayments = fees.getLevel().getLevelSpecialPayments();

            float newStudent = (float) specialPayments.stream().mapToDouble(LevelSpecialPayment::getAmount).sum();
            float oldStudent = (float) specialPayments.stream()
                    .filter(specialFee -> specialFee.getSpecialPayment().getPaymentType() == SpecialPaymentType.ADMISSION_FEE)
                    .mapToDouble(LevelSpecialPayment::getAmount)
                    .sum();

            List<StudentFeeRecord> newRecords = new ArrayList<>();
            for (Students student : studentsList) {

                StudentFeeRecord newRecord = studentFeeRecordRepository.findByStudent_StudentIdAndFees_FeesId(
                        student.getStudentId(), fees.getFeesId()
                ).orElse(null);
                if (newRecord == null) {
                    newRecord = new StudentFeeRecord();
                    newRecord.setStudent(student);
                    newRecord.setFees(fees);
                    newRecord.setLevel(fees.getLevel());
                    newRecord.setSemester(fees.getSemester());
                    newRecord.setLocked(false);
                    newRecord.setInstitution(fees.getInstitution());

                    if (student.isNew()) {
                        float total = fees.getAmountToBePayed() + newStudent;
                        newRecord.setTotalAmount(total);
                        newRecord.setBalance(total);
                    } else {
                        float total = fees.getAmountToBePayed() + oldStudent;
                        newRecord.setTotalAmount(total);
                        newRecord.setBalance(total);
                    }
                    newRecords.add(newRecord);
                }
            }

            if (!newRecords.isEmpty()) {
                studentFeeRecordRepository.saveAll(newRecords);
            }
        }
    }

    @Transactional
    public void updateIndividualFeeRecord(FeesUpdate feesUpdate) {

        Fees fees = feesRepository.findByFeesId(feesUpdate.getFeesId()).orElse(null);
        if (fees == null) {
            log.error("ASYNC: Fees with id {} not found", feesUpdate.getFeesId());
            return;
        }

        List<Students> studentsList = utilityClass.getActiveStudents(fees.getLevel().getStudents());
        if (studentsList == null || studentsList.isEmpty()) {
            log.error("ASYNC: Class {} has no students", fees.getLevel().getLevelName());
            return;
        }

        List<LevelSpecialPayment> specialPayments = fees.getLevel().getLevelSpecialPayments();

        float newStudent = (float) specialPayments.stream().mapToDouble(LevelSpecialPayment::getAmount).sum();
        float oldStudent = (float) specialPayments.stream()
                .filter(specialFee -> specialFee.getSpecialPayment().getPaymentType() == SpecialPaymentType.ADMISSION_FEE)
                .mapToDouble(LevelSpecialPayment::getAmount)
                .sum();

        List<StudentFeeRecord> newRecords = new ArrayList<>();
        for (Students student : studentsList) {

            StudentFeeRecord newRecord = studentFeeRecordRepository.findByStudent_StudentIdAndFees_FeesId(
                    student.getStudentId(), fees.getFeesId()
            ).orElse(null);
            if (newRecord == null) {
                newRecord = new StudentFeeRecord();
                newRecord.setStudent(student);
                newRecord.setFees(fees);
                newRecord.setLevel(fees.getLevel());
                newRecord.setSemester(fees.getSemester());
                newRecord.setLocked(false);
                newRecord.setInstitution(fees.getInstitution());
            }

            if (student.isNew()) {
                newRecord.setTotalAmount(fees.getAmountToBePayed() + newStudent);
            } else {
                newRecord.setTotalAmount(fees.getAmountToBePayed() + oldStudent);
            }
            newRecords.add(newRecord);
        }

        studentFeeRecordRepository.saveAll(newRecords);
    }

    @Transactional
    public void createNewStudentFee(NewStudentFee newStudentFee) {

        Level level = levelRepository.findByLevelID(newStudentFee.getLevelId()).orElse(null);
        if (level == null) {
            log.error("Could not create student fee. Invalid level Id {}", newStudentFee.getLevelId());
            return;
        }

        Semester currentSemester = utilityClass.getCurrentSemester(level.getInstitution());
        if (currentSemester == null) {
            log.error("Could not create student fee. Current term not added");
            return;
        }

        if (LocalDate.now().isBefore(currentSemester.getSemesterStartDate()) || LocalDate.now().isAfter(currentSemester.getSemesterEndDate())) {
            log.error("Could not create student fee. Current term not added");
            return;
        }

        Fees fee = feesRepository.findBySemester_SemesterIDAndLevel_LevelID(
                currentSemester.getSemesterID(), newStudentFee.getLevelId()
        ).orElse(null);
        if (fee == null) {
            log.error("Could not create student fee. Current term fee not added");
            return;
        }

        List<LevelSpecialPayment> specialPayments = fee.getLevel().getLevelSpecialPayments();

        float newStudent = (float) specialPayments.stream().mapToDouble(LevelSpecialPayment::getAmount).sum();
        float oldStudent = (float) specialPayments.stream()
                .filter(specialFee -> specialFee.getSpecialPayment().getPaymentType() == SpecialPaymentType.ADMISSION_FEE)
                .mapToDouble(LevelSpecialPayment::getAmount)
                .sum();

        Students student = studentsRepository.findByStudentId(newStudentFee.getStudentId()).orElse(null);
        if (student == null) {
            log.error("Could not create student fee. Invalid student id {}", newStudentFee.getStudentId());
            return;
        }

        StudentFeeRecord newRecord = new StudentFeeRecord();
        newRecord.setStudent(student);
        newRecord.setFees(fee);
        newRecord.setLevel(fee.getLevel());
        newRecord.setSemester(fee.getSemester());
        newRecord.setLocked(false);
        newRecord.setInstitution(fee.getInstitution());

        if (student.isNew()) {
            float total = fee.getAmountToBePayed() + newStudent;
            newRecord.setTotalAmount(total);
            newRecord.setBalance(total);
        } else {
            float total = fee.getAmountToBePayed() + oldStudent;
            newRecord.setTotalAmount(total);
            newRecord.setBalance(total);
        }
        studentFeeRecordRepository.saveAndFlush(newRecord);
    }

    public void create_updateResults(CreateResults createResults) {
        Semester semester = semesterRepository.findBySemesterID(createResults.getSemesterId()).orElse(null);
        Level level = levelRepository.findByLevelID(createResults.getLevelId()).orElse(null);
        if (semester == null || level == null) {
            return;
        }

        List<Results> resultsList = resultsRepository
                .findAllBySemester_SemesterIDAndStudent_StudentIdIn(createResults.getSemesterId(), createResults.getStudentIds());

        boolean found;
        for (String studentId : createResults.getStudentIds()) {
            found = false;

            for (Results results : resultsList) {
                if (results.getStudent().getStudentId().equals(studentId)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Students student = studentsRepository.findByStudentId(studentId).orElse(null);
                if (student == null) {
                    continue;
                }
                Results result = resultsRepository
                        .findByStudent_StudentIdAndSemester_SemesterID(studentId, createResults.getSemesterId())
                        .orElse(new Results());
                result.setStudent(student);
                result.setSemester(semester);
                result.setLevel(level);
                resultsList.add(result);
            }
        }
        resultsRepository.saveAllAndFlush(resultsList);

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository
                .findByAttendanceDate_Level_LevelIDAndAttendanceDate_Semester_SemesterID(createResults.getLevelId(), createResults.getSemesterId());

        List<Results> affectedResults = new ArrayList<>();
        for (Results result : resultsList) {
            int presentCount = 0;
            for (AttendanceRecords attendanceRecord : attendanceRecords) {
                if (result.getStudent().getStudentId().equals(attendanceRecord.getStudent().getStudentId())) {
                    presentCount++;
                }
            }
            if (presentCount > 0) {
                result.setPresentAttendanceCount(presentCount);
                affectedResults.add(result);
            }
        }
        resultsRepository.saveAllAndFlush(affectedResults);
    }

    @Transactional
    public void updateResults(ResultsUpdate resultsUpdate) {
        Staffs staff = staffsRepository.findByStaffId(resultsUpdate.getStaffId()).orElse(null);
        if (staff == null) {
            log.error("Could not find staff with id {}", resultsUpdate.getStaffId());
            return;
        }

        Subjects subject = subjectsRepository.findBySubjectId(resultsUpdate.getSubjectId()).orElse(null);
        if (subject == null) {
            log.error("Could not find subject with id {}", resultsUpdate.getSubjectId());
            return;
        }

        List<Results> affectedResults = resultsRepository.findAllByResultIdIn(resultsUpdate.getAffectedResultsIds());
        if (affectedResults == null || affectedResults.isEmpty()) {
            log.error("Could not update results, no results found");
            return;
        }

        int classSize = utilityClass.getActiveStudents(subject.getLevel().getStudents()).size();
        int savedSubjects = subject.getLevel().getSubjects().size();

        String levelId = null;
        String semesterId = null;
        for (Results result : affectedResults) {
            if (levelId == null || semesterId == null) {
                levelId = result.getLevel().getLevelID();
                semesterId = result.getSemester().getSemesterID();
            }
            utilityClass.updateResultTotals(result, staff, classSize);
            int savedScoresSubjects = subjectScoreRepository.countByResults_ResultId(result.getResultId());
            result.setReady(savedScoresSubjects == savedSubjects);
        }

        resultsRepository.saveAllAndFlush(affectedResults);

        List<Results> classResultsList = resultsRepository.findByLevel_LevelIDAndSemester_SemesterID(
                levelId, semesterId
        );
        if (classResultsList == null || classResultsList.isEmpty()) return;
        classResultsList.sort(Comparator.comparing(Results::getTotalScore).reversed());
        utilityClass.reArrangePositions(classResultsList);
    }

    public void broadcastAttendanceStatus(DailyAttendance dailyAttendance) {

        List<Students> students = studentsRepository.findByStudentIdIn(dailyAttendance.getStudentsIds());

        if (students != null && !students.isEmpty()) {

            int audienceCount = 0;
            int successCount  = 0;
            int failureCount = 0;
            AgooSmsResponse agooSmsResponse = null;
            for (Students student : students) {
                String formattedNumber = UtilityClass.toInternational(student.getParentPhoneNumber());
                if (formattedNumber == null)
                    continue;

                AttendanceStatus status =
                        dailyAttendance.getAttendanceMap().get(student.getStudentId());

                if (status == null) {
                    log.warn("No attendance status found for student {}",
                            student.getStudentId());
                    continue;
                }

                String fullName = student.getFirstName() + " " + student.getLastName();
                String preparedMessage = student.getInstitution().getBroadcastHeader() +
                        attendanceMessage + " " + dailyAttendance.getAttendanceDate().toString() + " " +
                        fullName + " is " + status.name();

                agooSmsResponse = smsInterface.sendSms(preparedMessage, formattedNumber);
                if (agooSmsResponse == null) {
                    log.error(
                            "SMS broadcast failed for student {}",
                            student.getStudentId()
                    );
                    failureCount++;
                    continue;
                }
                successCount++;
                audienceCount++;
            }

            if (agooSmsResponse != null && audienceCount > 0) {
                List<String> audience = new ArrayList<>();
                audience.add(students.getFirst().getLevel().getLevelName());
                Messages newMessage = Messages.builder()
                        .message(attendanceMessage)
                        .audience(audience)
                        .audienceCount(audienceCount)
                        .successCount(successCount)
                        .failureCount(failureCount)
                        .build();
                messagesRepository.save(newMessage);
            }
        }
    }
}
