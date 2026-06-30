package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.AttendanceRequestList;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.MarkAttendance_List;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.StudentAttendance;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.TodaysAbsentees;
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

    private final AttendanceRecordsRepository attendanceRecordsRepository;

    private final UtilityClass utilityClass;

    private final LoggingService loggingService;

    private final StudentsRepository studentsRepository;

    private final AttendanceDateRepository attendanceDateRepository;

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

    public ResponseEntity<?> markStudentAttendance(String levelId, String date, List<AttendanceRequestList> attendanceList, String staffId) {

        if (attendanceList == null || attendanceList.isEmpty()) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Attendance can't be marked on weekends", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Invalid request! Attendance records empty"
            ));
        }
        //Check if date is accepted for attendance
        LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        if (!utilityClass.isSchoolDay(selectedDate)) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Attendance can't be marked on weekends", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance can't be marked on weekends"
            ));
        }

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        Level level = levelRepository.findByLevelID(levelId).orElse(null);
        if (level == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid class Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Invalid class Id"
            ));
        }

        Semester semester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Current term not added to system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Current term not added to system"
            ));
        }

        AttendanceDate attendanceDate = attendanceDateRepository
                .findByLevel_LevelIDAndSemester_SemesterIDAndAttendanceDate(
                        levelId, semester.getSemesterID(), selectedDate
                ).orElse(null);
        if (attendanceDate == null) {
            attendanceDate = new AttendanceDate();
            attendanceDate.setLevel(level);
            attendanceDate.setSemester(semester);
            attendanceDate.setAttendanceDate(selectedDate);
            attendanceDate.setStaff(staff);
            attendanceDateRepository.save(attendanceDate);

            List<AttendanceRecords> attendanceRecords = new ArrayList<>();
            for (AttendanceRequestList record : attendanceList) {
                Students student = studentsRepository.findByStudentId(record.getStudentId()).orElse(null);
                if (student == null) {
                    loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid student Id", staffId, LogStatus.FAILED);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                            "message", "Invalid student Id"
                    ));
                }
                AttendanceRecords attendanceRecord = AttendanceRecords.builder()
                        .attendanceDate(attendanceDate)
                        .student(student)
                        .status(AttendanceStatus.valueOf(record.getStatus().toUpperCase()))
                        .build();

                attendanceRecords.add(attendanceRecord);
            }

            attendanceRecordsRepository.saveAll(attendanceRecords);
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Marked attendance for " + level.getLevelName(), staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().build();
        } else {
            //Holds names of students whose attendance is being updated
            List<String> updatedRecordsStudents = new ArrayList<>();

            List<AttendanceRecords> existingRecords = attendanceDate.getAttendanceRecords();
            for (AttendanceRequestList record : attendanceList) {

                 for (AttendanceRecords existingRecord : existingRecords) {

                     if (record.getStudentId().equals(existingRecord.getStudent().getStudentId()) &&
                     !AttendanceStatus.valueOf(record.getStatus().toUpperCase()).equals(existingRecord.getStatus())) {

                         existingRecord.setStatus(AttendanceStatus.valueOf(record.getStatus().toUpperCase()));
                         attendanceRecordsRepository.save(existingRecord);
                         updatedRecordsStudents.add(existingRecord.getStudent().getFirstName());
                     }
                 }
            }

            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.UPDATE,
                    "Updated attendance for " + updatedRecordsStudents.stream() + " of " + level.getLevelName(),
                    staffId, LogStatus.SUCCESS);
            return ResponseEntity.ok().build();
        }
    }

    //This loads all the absentees for the day
    public ResponseEntity<?> getAbsentees(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid Staff Id"
            ));
        }

        List<Level> levels = staff.getInstitution().getLevel();
        if (levels == null || levels.isEmpty()) {
            loggingService.logGeneralActivity(LogType.STUDENT, LogAction.READ, "Institution has no classes yet", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Institution has no class yet"
            ));
        }

        LocalDate currentDate = LocalDate.now();
        List<TodaysAbsentees> absentees = new ArrayList<>();

        List<AttendanceDate> markedClasses =  attendanceDateRepository
                .findByAttendanceDateAndSemester_Institution_InstitutionId(
                        LocalDate.now(), staff.getInstitution().getInstitutionId()
                );
        if (markedClasses == null || markedClasses.isEmpty()) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "No class has marked attendance yet", staffId, LogStatus.FAILED);
            return ResponseEntity.ok(absentees);
        }

        for (AttendanceDate attendanceDate : markedClasses) {

            if (attendanceDate.getAttendanceRecords() == null || attendanceDate.getAttendanceRecords().isEmpty()) {
                continue;
            }

            for (AttendanceRecords attendanceRecord : attendanceDate.getAttendanceRecords()) {
                TodaysAbsentees absentStudent = TodaysAbsentees.builder()
                        .studentId(attendanceRecord.getStudent().getStudentId())
                        .studentName(
                                attendanceRecord.getStudent().getFirstName() + " " +
                                attendanceRecord.getStudent().getLastName()
                        )
                        .studentGrade(attendanceDate.getLevel().getLevelName())
                        .instructorId(attendanceDate.getStaff().getStaffId())
                        .instructorName(
                                attendanceDate.getStaff().getFirstName() + " " +
                                attendanceDate.getStaff().getLastName()
                        )
                        .build();
                absentees.add(absentStudent);
            }
        }

        loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "fetched absentees for today", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(absentees);
    }

    public int getStudentPresentAttendanceCount(String studentId, String semesterId) {

        List<AttendanceRecords> presentDays = attendanceRecordsRepository
                .findByStudent_StudentIdAndStatusAndAttendanceDate_Semester_SemesterID(
                        studentId, AttendanceStatus.PRESENT, semesterId
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
