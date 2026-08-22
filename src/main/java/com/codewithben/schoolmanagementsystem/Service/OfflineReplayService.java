package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Fees.NewPayment;
import com.codewithben.schoolmanagementsystem.DTO.Offline.OfflineAttendanceList;
import com.codewithben.schoolmanagementsystem.DTO.Offline.OfflinePaymentList;
import com.codewithben.schoolmanagementsystem.DTO.Offline.OfflineScoresList;
import com.codewithben.schoolmanagementsystem.DTO.Result.SaveStudentScores;
import com.codewithben.schoolmanagementsystem.DTO.Students.StudentsScoresTable;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OfflineReplayService {

    private final AttendanceService attendanceService;

    private final ScoresService scoresService;

    private final FeesService feesService;

    private final LoggingService loggingService;

    public ResponseEntity<?> saveOfflineAttendanceRecords(String staffId, List<OfflineAttendanceList> list) {
        if (list == null || list.isEmpty()) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Attendance records list is empty", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Attendance records list is empty"
            ));
        }

        int completed = 0;
        for (OfflineAttendanceList offlineRecord : list) {
            ResponseEntity<?> response = attendanceService.saveAttendance(
                    offlineRecord.getLevelId(), offlineRecord.getDateMarked(), offlineRecord.getAttendanceList(), staffId
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                completed++;
            }
        }

        if (completed == list.size()) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Offline records synchronized", staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().build();
        } else {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Some records not synchronized", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Some records not synchronized"
            ));
        }
    }

    public ResponseEntity<?> saveOfflineScores(String staffId, List<OfflineScoresList> scores) {

        if (scores == null || scores.isEmpty()) {
            loggingService.logGeneralActivity(
                    LogType.SUBJECT_SCORE, LogAction.SYNC,
                    "Offline Records came empty",
                    staffId, LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Offline Records came empty"
            ));
        }

        int completed = 0;
        for (OfflineScoresList record : scores) {

            ResponseEntity<?> response = scoresService.saveScores(
                    staffId, record.getSubjectId(), record.getSemesterId(), record.getScores()
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                completed++;
            }
        }

        if (completed == scores.size()) {
            loggingService.logGeneralActivity(
                    LogType.SUBJECT_SCORE, LogAction.SYNC,
                    "Offline Records synchronized",
                    staffId, LogStatus.SUCCESS
            );
            return ResponseEntity.ok().build();
        } else {
            loggingService.logGeneralActivity(
                    LogType.SUBJECT_SCORE, LogAction.SYNC,
                    "Some records were not synchronized",
                    staffId, LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Some records were not synchronized"
            ));
        }
    }

    public ResponseEntity<?> saveOfflinePayments(String staffId, List<OfflinePaymentList> list) {
        if (list == null || list.isEmpty()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.SYNC, "Fees records list is empty", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Fees records list is empty"
            ));
        }

        int completed = 0;
        for (OfflinePaymentList record : list) {
            NewPayment newPayment = NewPayment.builder()
                    .amountPaid(record.getAmountPaid())
                    .levelId(record.getLevelId())
                    .payerName(record.getPayerName())
                    .payerPhone(record.getPayerPhone())
                    .semesterId(record.getSemesterId())
                    .studentId(record.getStudentId())
                    .build();

            ResponseEntity<?> response = feesService.addNewPayment(newPayment, staffId);
            if (response.getStatusCode() == HttpStatus.OK) {
                completed++;
            }
        }

        if (completed == list.size()) {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.SYNC, "Offline Records synchronized", staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().build();
        } else {
            loggingService.logGeneralActivity(LogType.FEES, LogAction.SYNC, "Some records not synchronized", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Some records not synchronized"
            ));
        }
    }
}
