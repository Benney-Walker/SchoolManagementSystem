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

        LocalDate date = LocalDate.parse(attendanceDate, DateTimeFormatter.ISO_DATE);

        List<Attendance> markedAttendance =
                attendanceRepository.findByLevel_LevelIDAndDateMarked(levelId, date);

        List<Students> students = utilityClass.getActiveStudents(level.getStudents());

        List<StudentAttendance> attendanceList = new ArrayList<>();

        for (Students student : students) {

            String status = AttendanceStatus.ABSENT.name();

            if (markedAttendance != null) {
                for (Attendance attendance : markedAttendance) {
                    if (attendance.getStudent().getStudentId().equals(student.getStudentId())) {
                        status = attendance.getStatus().name();
                        break;
                    }
                }
            }

            attendanceList.add(new StudentAttendance(
                    level.getLevelID(),
                    student.getStudentId(),
                    student.getFirstName() + " " + student.getLastName(),
                    status
            ));
        }

        loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "Fetched " + level.getLevelName() + " students for attendance", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(attendanceList);
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

        String currentSemesterId = utilityClass.getCurrentSemesterId(
                student.getInstitution().getInstitutionId()
        );
        Semester semester = semesterRepository.findBySemesterID(currentSemesterId).orElse(null);
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

        Attendance todaysAttendance = attendanceRepository.findByStudent_StudentIdAndDateMarked(
                studentId, selectedDate
        ).orElse(null);
        if (todaysAttendance == null) {
            todaysAttendance = new Attendance();
            todaysAttendance.setStudent(student);
            todaysAttendance.setSemester(semester);
            todaysAttendance.setLevel(level);
            todaysAttendance.setDateMarked(selectedDate);
            todaysAttendance.setMarkedBy(staff);

            List<Attendance> studentAttendance = student.getAttendance();
            if (studentAttendance == null || studentAttendance.isEmpty()) {
                studentAttendance = new ArrayList<>();
            }
            studentAttendance.add(todaysAttendance);
            studentsRepository.save(student);
        }

        todaysAttendance.setStatus(AttendanceStatus.valueOf(status.toUpperCase()));
        attendanceRepository.save(todaysAttendance);

        loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "N/A", staffId, LogStatus.SUCCESS);
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
