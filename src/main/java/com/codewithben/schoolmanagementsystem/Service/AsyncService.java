package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsResponse;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.SmsResponse;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Attendance.DailyAttendance;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeeCreation;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeesUpdate;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.PaymentReport;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private String attendanceMessageHeader;

    @Value("${payment.report.header}")
    private String paymentReportHeader;

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

                    float openingBalance = getOpeningBalance(student.getStudentId(), student.getInstitution(), fees.getSemester().getSemesterStartDate());
                    float specialPayment = getSpecialPaymentAmount(specialPayments, student);
                    float total = fees.getAmountToBePayed() + specialPayment + openingBalance;

                    newRecord.setOpeningBalance(openingBalance);
                    newRecord.setTotalAmount(total);
                    newRecord.setBalance(total);
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

            float openingBalance = getOpeningBalance(student.getStudentId(), student.getInstitution(), fees.getSemester().getSemesterStartDate());
            float specialPayment = getSpecialPaymentAmount(specialPayments, student);
            float total = fees.getAmountToBePayed() + specialPayment + openingBalance;

            newRecord.setOpeningBalance(openingBalance);
            newRecord.setTotalAmount(total);
            newRecord.setBalance(total);
            newRecords.add(newRecord);
        }

        studentFeeRecordRepository.saveAll(newRecords);
    }

    @Transactional
    public void createNewStudentFee(NewStudentFee newStudentFee) {
        Students student = studentsRepository.findByStudentId(newStudentFee.getStudentId()).orElse(null);
        if (student == null) {
            log.error("Could not create student fee. Invalid student id {}", newStudentFee.getStudentId());
            return;
        }

        Semester currentSemester = utilityClass.getCurrentSemester(student.getInstitution());
        if (currentSemester == null) {
            log.error("Could not create student fee. Current term not added");
            return;
        }

        if (LocalDate.now().isBefore(currentSemester.getSemesterStartDate()) || LocalDate.now().isAfter(currentSemester.getSemesterEndDate())) {
            log.error("Current term not added or has not started");
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

        float specialPaymentAmount = getSpecialPaymentAmount(specialPayments, student);

        float openingBalance = getOpeningBalance(student.getStudentId(), student.getInstitution(), currentSemester.getSemesterStartDate());
        StudentFeeRecord newRecord = new StudentFeeRecord();
        newRecord.setStudent(student);
        newRecord.setFees(fee);
        newRecord.setLevel(fee.getLevel());
        newRecord.setSemester(fee.getSemester());
        newRecord.setLocked(false);
        newRecord.setInstitution(fee.getInstitution());
        newRecord.setOpeningBalance(openingBalance);

        float total = fee.getAmountToBePayed() + specialPaymentAmount + openingBalance;

        newRecord.setTotalAmount(total);
        newRecord.setBalance(total);
        studentFeeRecordRepository.saveAndFlush(newRecord);
    }

    @Transactional
    public void create_updateResults(CreateResults createResults) {
        Semester semester = semesterRepository.findBySemesterID(createResults.getSemesterId()).orElse(null);
        Level level = levelRepository.findByLevelID(createResults.getLevelId()).orElse(null);
        if (semester == null || level == null) {
            return;
        }

        List<Results> resultsList = resultsRepository
                .findAllBySemester_SemesterIDAndStudent_StudentIdIn(createResults.getSemesterId(), createResults.getStudentIds());

        boolean found;
        List<Results> newResultsList = new ArrayList<>();
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
                newResultsList.add(result);
            }
        }
        if (!newResultsList.isEmpty()) {
            resultsRepository.saveAllAndFlush(newResultsList);
            resultsList.addAll(newResultsList);
        }

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

    @Transactional
    public void broadcastAttendanceStatus(DailyAttendance dailyAttendance) {

        List<Students> students = studentsRepository.findByStudentIdIn(dailyAttendance.getStudentsIds());

        if (students != null && !students.isEmpty()) {

            int audienceCount = 0;
            float smsCost = 0;
            int successCount  = 0;
            int failureCount = 0;
            SmsResponse smsResponse;
            String preparedMessage = "";
            for (Students student : students) {
                smsResponse = null;
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
                preparedMessage = attendanceMessageHeader + " - "
                        + dailyAttendance.getAttendanceDate() + ". "
                        + fullName + " was marked " + status.name() + " today. "
                        + "Regards, "
                        + student.getInstitution().getInstitutionName() + ".";

                smsResponse = smsInterface.sendSms(preparedMessage, formattedNumber);
                if (!smsResponse.isSuccess()) {
                    log.error(
                            "SMS broadcast failed for student {}",
                            student.getStudentId()
                    );
                    failureCount++;
                    continue;
                }
                smsCost += smsResponse.getSmsCost();
                successCount++;
                audienceCount++;
            }

            Messages messages = new Messages();
            messages.setSmsCost(smsCost);
            messages.setAudienceCount(audienceCount);
            messages.setAudience(Collections.singletonList(students.getFirst().getLevel().getLevelName()));
            messages.setSuccessCount(successCount);
            messages.setFailureCount(failureCount);
            messages.setMessage("Daily attendance broadcast");
            messagesRepository.saveAndFlush(messages);
        }
    }

    public void broadcastPaymentReport(PaymentReport paymentReport) {
         Students student = studentsRepository.findByStudentId(paymentReport.getStudentId()).orElse(null);
         if (student == null) {
             log.error("Could not find student with id {}", paymentReport.getStudentId());
             return;
         }

         String fullName = student.getFirstName() + " " + student.getLastName();

        String preparedMessage =
                paymentReportHeader + " - "
                        + fullName.toUpperCase() + ", "
                        + "an amount of GHS " + paymentReport.getAmountPaid()
                        + " has been credited to your student account. "
                        + "Your new outstanding balance is GHS "
                        + paymentReport.getNewBalance() + ". "
                        + "Regards, "
                        + student.getInstitution().getInstitutionName() + ".";

         String formattedNumber = UtilityClass.toInternational(student.getParentPhoneNumber());
         if (formattedNumber == null) {
             log.error("Could not send Sms message {}", student.getParentPhoneNumber());
         }
         SmsResponse smsResponse = smsInterface.sendSms(preparedMessage, formattedNumber);
         if (smsResponse.isSuccess()) {
             Messages messages = new Messages();
             messages.setMessage(preparedMessage);
             messages.setAudience(Collections.singletonList(fullName));
             messages.setAudienceCount(1);
             messages.setSmsCost(smsResponse.getSmsCost());
             messages.setSuccessCount(1);
             messages.setFailureCount(0);
             messagesRepository.saveAndFlush(messages);
         }
    }

    private float getOpeningBalance(String studentId, Institution institution, LocalDate date) {

        List<Semester> previousSemesters =
                semesterRepository.findByInstitutionAndSemesterStartDateBeforeOrderBySemesterStartDateDesc(
                        institution, date
                );
        Semester lastSemester = previousSemesters.isEmpty() ? null : previousSemesters.getFirst();

        if (lastSemester == null) {
            return 0;
        }

        StudentFeeRecord previousRecord = studentFeeRecordRepository
                .findByStudent_StudentIdAndSemester_SemesterID(studentId, lastSemester.getSemesterID()).orElse(null);
        if (previousRecord == null) {
            return 0;
        }

        return previousRecord.getBalance();
    }

    private float getSpecialPaymentAmount(List<LevelSpecialPayment> specialPayments, Students student) {
        float newStudentBorder = 0;
        float newStudentNotBorder = 0;
        float oldStudentBorder = 0;
        float oldStudentNotBorder = 0;

        for (LevelSpecialPayment specialPayment : specialPayments) {

            float amount = specialPayment.getAmount();

            switch (specialPayment.getSpecialPayment().getPaymentType()) {

                case ADMISSION_FEE -> {
                    newStudentBorder += amount;
                    newStudentNotBorder += amount;
                }

                case BOARDING -> {
                    newStudentBorder += amount;
                    oldStudentBorder += amount;
                }

                case STATIONARY, OTHER -> {
                    newStudentBorder += amount;
                    newStudentNotBorder += amount;
                    oldStudentBorder += amount;
                    oldStudentNotBorder += amount;
                }
            }
        }

        float specialPaymentAmount;

        if (student.isNew()) {
            specialPaymentAmount = student.isBorder()
                    ? newStudentBorder
                    : newStudentNotBorder;
        } else {
            specialPaymentAmount = student.isBorder()
                    ? oldStudentBorder
                    : oldStudentNotBorder;
        }
        return specialPaymentAmount;
    }
}
