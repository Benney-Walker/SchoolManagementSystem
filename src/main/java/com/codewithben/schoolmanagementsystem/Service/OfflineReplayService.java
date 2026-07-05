package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Offline.OfflineAttendanceList;
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

    private final AttendanceDateRepository attendanceDateRepository;

    private final AttendanceRecordsRepository attendanceRecordsRepository;

    private final StudentsRepository studentsRepository;

    private final LevelRepository levelRepository;

    private final StaffsRepository staffsRepository;

    private final LoggingService loggingService;

    private final UtilityClass utilityClass;

    public ResponseEntity<?> saveOfflineAttendanceRecords(String staffId, List<OfflineAttendanceList> list) {
        if (list == null || list.isEmpty()) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Attendance records list is empty", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Attendance records list is empty"
            ));
        }

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Semester currentSemester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (currentSemester == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Current Term not added", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Current Term not added"
            ));
        }

        for (OfflineAttendanceList record : list) {
            LocalDate currentDate = LocalDate.parse(record.getDateMarked(), DateTimeFormatter.ISO_DATE);

            Level level = levelRepository.findByLevelID(record.getLevelId()).orElse(null);
            if (level == null) {
                loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Invalid class Id", staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", "Invalid class Id"
                ));
            }

            Students student = studentsRepository.findByStudentId(record.getStudentId()).orElse(null);
            if (student == null) {
                loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Invalid student Id", staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", "Invalid Student Id"
                ));
            }

            AttendanceDate attendanceDate = attendanceDateRepository
                    .findByLevel_LevelIDAndSemester_SemesterIDAndAttendanceDate(
                            record.getLevelId(),
                            currentSemester.getSemesterID(),
                            currentDate
                    ).orElse(null);

            if (attendanceDate == null) {
                attendanceDate = new AttendanceDate();
                attendanceDate.setStaff(staff);
                attendanceDate.setAttendanceDate(currentDate);
                attendanceDate.setSemester(currentSemester);
                attendanceDate.setLevel(level);
                attendanceDateRepository.save(attendanceDate);
            }

            AttendanceRecords existingRecord = attendanceRecordsRepository
                    .findByAttendanceDate_DateIdAndStudent_StudentId(
                            attendanceDate.getDateId(),
                            record.getStudentId()
                    ).orElse(null);
            if (existingRecord == null) {
                existingRecord = new AttendanceRecords();
                existingRecord.setAttendanceDate(attendanceDate);
                existingRecord.setStudent(student);
            }

            existingRecord.setStatus(
                    AttendanceStatus.valueOf(record.getStatus().toUpperCase())
            );
            attendanceRecordsRepository.save(existingRecord);
        }

        loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.SYNC, "Successfully synchronized attendance records", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }
}
