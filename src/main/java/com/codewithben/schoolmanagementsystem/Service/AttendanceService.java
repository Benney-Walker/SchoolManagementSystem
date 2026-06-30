package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.MarkAttendance_List;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.StudentAttendance;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
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
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    private final UtilityClass utilityClass;

    private final LoggingService loggingService;

    private final StudentsRepository studentsRepository;

    private final SemesterRepository semesterRepository;

    private final LevelRepository levelRepository;

    private final StaffsRepository staffsRepository;

    public ResponseEntity<?> loadStudentsForAttendance(String levelId, String attendanceDate, String staffId) {

        LocalDate selectedDate = LocalDate.parse(attendanceDate, DateTimeFormatter.ISO_DATE);

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Semester semester = utilityClass.getCurrentSemester(level.getInstitution());
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Current term not added to system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Current term not added to system"
            ));
        }

        if (!utilityClass.isSchoolDay(selectedDate)) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Attendance can't be marked on weekends", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance can't be marked on weekends"
            ));
        }

        if (utilityClass.isHoliday(semester, selectedDate)) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Selected date is a holiday", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Selected date is a holiday"
            ));
        }

        List<StudentAttendance> studentsAttendanceRecords = new ArrayList<>();

        AttendanceDate  markedDate = attendanceDateRepository
                .findByLevel_LevelIDAndSemester_SemesterIDAndAttendanceDate(
                        levelId, semester.getSemesterID(), selectedDate
                ).orElse(null);
        if (markedDate == null) {
            studentsAttendanceRecords = utilityClass.getActiveStudents(level.getStudents())
                    .stream().map(s -> {
                        return new StudentAttendance(
                                levelId,
                                s.getStudentId(),
                                s.getFirstName() + " " + s.getLastName(),
                                AttendanceStatus.ABSENT.name()
                        );
                    }).toList();
        } else {
            studentsAttendanceRecords = markedDate.getAttendanceRecords()
                    .stream().map(record -> {
                        return new StudentAttendance(
                                levelId,
                                record.getStudent().getStudentId(),
                                record.getStudent().getFirstName() + " " + record.getStudent().getLastName(),
                                record.getStatus().name()
                        );
                    }).toList();
        }

        loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "Fetched " + level.getLevelName() + " attendance records", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(studentsAttendanceRecords);
    }

    public ResponseEntity<?> markStudentAttendance(String studentId, String levelId, String status, String date, String staffId) {

        //Check if date is accepted for attendance
        LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        if (!utilityClass.isSchoolDay(selectedDate)) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Attendance can't be marked on weekends", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance can't be marked on weekends"
            ));
        }

        Students student = studentsRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid student Id"
            ));
        }


        Semester semester = utilityClass.getCurrentSemester(student.getInstitution());
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Current term not added to system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Current term not added to system"
            ));
        }

        List<SchoolHoliday> holidays = semester.getSchoolHoliday();
        if (holidays != null && !holidays.isEmpty()) {
            for (SchoolHoliday holiday : holidays) {

                if (!selectedDate.isBefore(holiday.getStartDate()) && !selectedDate.isAfter(holiday.getEndDate())) {
                    loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Today is holiday", staffId, LogStatus.FAILED);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                            "message", "Today is holiday"
                    ));
                }
            }
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid student Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid level Id"
            ));
        }

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        AttendanceDate markedDate = attendanceDateRepository
                .findByLevel_LevelIDAndSemester_SemesterIDAndAttendanceDate(
                        levelId, semester.getSemesterID(), selectedDate
                ).orElse(null);
        if (markedDate == null) {
            markedDate = new AttendanceDate();
            markedDate.setLevel(level);
            markedDate.setSemester(semester);
            markedDate.setAttendanceDate(selectedDate);
            markedDate.setStaff(staff);
            attendanceDateRepository.save(markedDate);

            AttendanceRecords attendanceRecords = new AttendanceRecords();
            attendanceRecords.setAttendanceDate(markedDate);
            attendanceRecords.setStudent(student);
            attendanceRecords.setStatus(AttendanceStatus.valueOf(status.toUpperCase()));
            attendanceRecordsRepository.save(attendanceRecords);

            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Marked attendance for " + level.getLevelName(), staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().build();
        } else {
            AttendanceRecords record = attendanceRecordsRepository
                    .findByAttendanceDate_DateIdAndStudent_StudentId(
                            markedDate.getDateId(), studentId
                    ).orElse(null);
            if (record == null) {
                record = new AttendanceRecords();
                record.setAttendanceDate(markedDate);
                record.setStudent(student);
                record.setStatus(AttendanceStatus.valueOf(status.toUpperCase()));
            } else {
                record.setStatus(AttendanceStatus.valueOf(status.toUpperCase()));
            }
            attendanceRecordsRepository.save(record);

            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Updated attendance for " + level.getLevelName(), staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().build();
        }
    }

    public ResponseEntity<?> markStudentAttendance(List<MarkAttendance_List> attendanceList, String date, String staffId) {

        //Check if date is accepted for attendance
        LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        if (!utilityClass.isSchoolDay(selectedDate)) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Attendance can't be marked on weekends", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance can't be marked on weekends"
            ));
        }

        boolean isNotHoliday = false;
        Level attendanceLevel = null;
        Semester attendanceSemester = null;

        for (MarkAttendance_List studentAttendance : attendanceList) {

            Students student = studentsRepository.findByStudentId(studentAttendance.getStudentId()).orElse(null);
            if (student == null) {
                loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid student Id", staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", "Invalid student Id"
                ));
            }

            if (attendanceSemester == null) {
                Semester semester = utilityClass.getCurrentSemester(student.getInstitution());
                if (semester == null) {
                    loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Current term not added to system", staffId, LogStatus.FAILED);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                            "message", "Current term not added to system"
                    ));
                }

                attendanceSemester = semester;
            }

            if (!isNotHoliday) {
                if (utilityClass.isHoliday(attendanceSemester, selectedDate)) {
                    loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Attendance can't be marked on holidays", staffId, LogStatus.FAILED);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                            "message", "Attendance can't be marked on holidays"
                    ));
                }
                isNotHoliday = true;
            }

            if (attendanceLevel == null) {
                Level level = levelRepository.findByLevelID(studentAttendance.getLevelId()).orElse(null);
                if (level == null) {
                    loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid student Id", staffId, LogStatus.FAILED);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                            "message", "Invalid level Id"
                    ));
                }

                attendanceLevel = level;
            }


            Attendance todaysAttendance = attendanceRepository.findByStudent_StudentIdAndDateMarked(
                    studentAttendance.getStudentId(), selectedDate
            ).orElse(null);
            if (todaysAttendance == null) {
                todaysAttendance = new Attendance();
                todaysAttendance.setStudent(student);
                todaysAttendance.setSemester(attendanceSemester);
                todaysAttendance.setLevel(attendanceLevel);
                todaysAttendance.setDateMarked(selectedDate);
                todaysAttendance.setMarkedBy(null);

                List<Attendance> studentAttendanceList = student.getAttendance();
                if (studentAttendanceList == null || studentAttendanceList.isEmpty()) {
                    studentAttendanceList = new ArrayList<>();
                }
                studentAttendanceList.add(todaysAttendance);
                studentsRepository.save(student);
            }

            todaysAttendance.setStatus(AttendanceStatus.valueOf(studentAttendance.getStatus().toUpperCase()));
            attendanceRepository.save(todaysAttendance);
        }

        loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Marked attendance for " + attendanceLevel.getLevelName(), staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
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
}
