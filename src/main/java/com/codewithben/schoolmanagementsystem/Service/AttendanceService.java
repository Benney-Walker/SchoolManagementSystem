package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.AttendanceRequestList;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.DatesMarked;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.StudentAttendance;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.TodaysAbsentees;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Attendance.DailyAttendance;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Results.CreateResults;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Repository.*;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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

    private final SemesterRepository semesterRepository;

    private final RabbitMQProducer rabbitMQProducer;

    private final SchoolScopeService schoolScopeService;

    public ResponseEntity<?> loadStudentsForAttendance(String levelId, String attendanceDate, String staffId) {

        LocalDate selectedDate = LocalDate.parse(attendanceDate, DateTimeFormatter.ISO_DATE);

        SchoolScopeService.Scoped<Staffs> caller = schoolScopeService.caller(staffId);
        if (caller.failed()) return caller.error();
        SchoolScopeService.Scoped<Level> foundLevel =
                schoolScopeService.level(caller.value(), levelId, LogType.ATTENDANCE, LogAction.READ);
        if (foundLevel.failed()) return foundLevel.error();
        Level level = foundLevel.value();

        Semester semester = utilityClass.getCurrentSemester(level.getInstitution());
        if (semester == null) {
            loggingService.logGeneralActivity(
                    LogType.ATTENDANCE,
                    LogAction.READ,
                    "Attendance can't be marked today! Vacation or term has not started yet",
                    staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance can't be marked today! Vacation or term has not started yet"
            ));
        }

        if (utilityClass.isNotSchoolday(semester, selectedDate)) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "Selected date is a holiday", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance cannot be marked today"
            ));
        }

        List<StudentAttendance> records;

        AttendanceDate  markedDate = attendanceDateRepository
                .findByLevel_LevelIDAndSemester_SemesterIDAndAttendanceDate(
                        levelId, semester.getSemesterID(), selectedDate
                ).orElse(null);
        if (markedDate == null) {
            List<Students> activeStudents = utilityClass.getActiveStudents(level.getStudents());
            if (activeStudents == null || activeStudents.isEmpty()) {
                loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "Class has no students yet", staffId, LogStatus.FAILED);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "message", "Class has no students yet"
                ));
            }
            records = activeStudents
                    .stream().map(s -> new StudentAttendance(
                            levelId,
                            s.getStudentId(),
                            s.getFirstName() + " " + s.getLastName(),
                            AttendanceStatus.ABSENT.name()
                    )).toList();
        } else {
            records = markedDate.getAttendanceRecords()
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
        return ResponseEntity.ok(records);
    }

    public ResponseEntity<?> saveAttendance(String levelId, String date, List<AttendanceRequestList> attendanceList, String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        SchoolScopeService.Scoped<Level> foundLevel =
                schoolScopeService.level(staff, levelId, LogType.ATTENDANCE, LogAction.CREATE);
        if (foundLevel.failed()) return foundLevel.error();
        Level level = foundLevel.value();

        Semester semester = utilityClass.getCurrentSemester(staff.getInstitution());
        if (semester == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Current term not added to system", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Current term not added to system"
            ));
        }

        //Check if date is accepted for attendance
        LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);

        if (utilityClass.isNotSchoolday(semester, selectedDate)) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "Selected date is a holiday", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance cannot be marked today"
            ));
        }

        Map<String, AttendanceStatus> attendanceMap = new HashMap<>();

        // Only students of this class at the caller's school. Ids are sequential, so the list
        // could otherwise record attendance for, create results for, and text the parents of
        // another school's students (issue 2). Ids that don't belong are ignored.
        List<String> requestedIds = attendanceList.stream().map(
                AttendanceRequestList::getStudentId
        ).toList();
        List<Students> students = studentsRepository.findByStudentIdIn(requestedIds).stream()
                .filter(student -> SchoolScopeService.sameSchool(staff, student.getInstitution())
                        && student.getLevel() != null
                        && Objects.equals(student.getLevel().getLevelID(), level.getLevelID()))
                .toList();
        Set<String> classStudentIds = students.stream().map(Students::getStudentId).collect(Collectors.toSet());
        List<String> studentIds = requestedIds.stream().filter(classStudentIds::contains).distinct().toList();
        if (studentIds.size() < requestedIds.size()) {
            log.warn("Attendance for class {} ignored {} student id(s) not in that class", levelId,
                    requestedIds.size() - studentIds.size());
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

                for (Students student : students) {
                    if (student.getStudentId().equals(record.getStudentId())) {
                        AttendanceStatus status = AttendanceStatus.valueOf(record.getStatus().toUpperCase());

                        AttendanceRecords attendanceRecord = AttendanceRecords.builder()
                                .attendanceDate(attendanceDate)
                                .student(student)
                                .status(status)
                                .build();

                        attendanceRecords.add(attendanceRecord);
                        attendanceMap.put(student.getStudentId(), status);
                    }
                }
            }

            attendanceRecordsRepository.saveAll(attendanceRecords);
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.CREATE, "Marked attendance for " + level.getLevelName(), staffId, LogStatus.SUCCESS);
        } else {

            List<AttendanceRecords> existingRecords = attendanceDate.getAttendanceRecords();
            for (AttendanceRequestList record : attendanceList) {

                 for (AttendanceRecords existingRecord : existingRecords) {

                     if (record.getStudentId().equals(existingRecord.getStudent().getStudentId()) &&
                     !AttendanceStatus.valueOf(record.getStatus().toUpperCase()).equals(existingRecord.getStatus())) {

                         existingRecord.setStatus(AttendanceStatus.valueOf(record.getStatus().toUpperCase()));
                         break;
                     }
                 }
            }
            attendanceRecordsRepository.saveAll(existingRecords);

            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.UPDATE,
                    "Updated attendance for " + level.getLevelName(),
                    staffId, LogStatus.SUCCESS);
        }
        CreateResults createResults = CreateResults.builder()
                .levelId(levelId)
                .semesterId(semester.getSemesterID())
                .studentIds(studentIds)
                .staffId(staffId)
                .build();
        rabbitMQProducer.sendCreateResultsEvent(createResults);

        DailyAttendance dailyAttendance = DailyAttendance.builder()
                .attendanceMap(attendanceMap)
                .studentsIds(studentIds)
                .attendanceDate(selectedDate)
                .build();
        rabbitMQProducer.sendDailyAttendanceEvent(dailyAttendance);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadAttendanceRecords(String staffId, String levelId, String semesterId, String date) {

        LocalDate formattedDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);

        SchoolScopeService.Scoped<Staffs> caller = schoolScopeService.caller(staffId);
        if (caller.failed()) return caller.error();
        SchoolScopeService.Scoped<Level> foundLevel =
                schoolScopeService.level(caller.value(), levelId, LogType.ATTENDANCE, LogAction.READ);
        if (foundLevel.failed()) return foundLevel.error();
        SchoolScopeService.Scoped<Semester> foundSemester =
                schoolScopeService.semester(caller.value(), semesterId, LogType.ATTENDANCE, LogAction.READ);
        if (foundSemester.failed()) return foundSemester.error();

        AttendanceDate  markedDate = attendanceDateRepository
                .findByLevel_LevelIDAndSemester_SemesterIDAndAttendanceDate(
                        levelId, semesterId, formattedDate
                ).orElse(null);
        if (markedDate == null) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "Attendance was not marked", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Attendance was not marked for this date"
            ));
        }

        if (markedDate.getAttendanceRecords() == null || markedDate.getAttendanceRecords().isEmpty()) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "No records found for this class", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "No records found for this class"
            ));
        }

        List<StudentAttendance> records = new ArrayList<>();
        for (AttendanceRecords record : markedDate.getAttendanceRecords()) {
            StudentAttendance existingRecord = StudentAttendance.builder()
                    .studentId(record.getStudent().getStudentId())
                    .studentName(
                            record.getStudent().getFirstName() + " " +
                                    record.getStudent().getLastName()
                    )
                    .status(record.getStatus().name())
                    .build();
            records.add(existingRecord);
        }

        loggingService.logGeneralActivity(
                LogType.ATTENDANCE, LogAction.READ,
                "Fetched " + markedDate.getLevel().getLevelName() + " attendance for " + date,
                staffId, LogStatus.SUCCESS
        );
        return ResponseEntity.ok(records);
    }

    public ResponseEntity<?> loadDatesMarked(String staffId, String levelId, String semesterId) {

        SchoolScopeService.Scoped<Staffs> caller = schoolScopeService.caller(staffId);
        if (caller.failed()) return caller.error();
        SchoolScopeService.Scoped<Semester> foundSemester =
                schoolScopeService.semester(caller.value(), semesterId, LogType.ATTENDANCE, LogAction.READ);
        if (foundSemester.failed()) return foundSemester.error();
        SchoolScopeService.Scoped<Level> foundLevel =
                schoolScopeService.level(caller.value(), levelId, LogType.ATTENDANCE, LogAction.READ);
        if (foundLevel.failed()) return foundLevel.error();
        Semester semester = foundSemester.value();

        LocalDate startDate = semester.getSemesterStartDate();
        LocalDate endDate = semester.getSemesterEndDate();

        List<LocalDate> schoolDays = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            if (!utilityClass.isWeekend(date) && !utilityClass.isNotSchoolday(semester, date)) {
                schoolDays.add(date);
            }
        }

        List<AttendanceDate> datesMarked = attendanceDateRepository
                .findByLevel_LevelIDAndSemester_SemesterIDOrderByAttendanceDateAsc(levelId, semesterId);
        if (datesMarked == null || datesMarked.isEmpty()) {
            loggingService.logGeneralActivity(LogType.ATTENDANCE, LogAction.READ, "No attendance marked for this term", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "No attendance marked for this term"
            ));
        }

        List<DatesMarked> datesMarkedList = new ArrayList<>();
        boolean isMarked;
        for (LocalDate date :  schoolDays) {
            isMarked = false;

            DatesMarked datemarked = DatesMarked.builder()
                    .dateMarked(date.toString())
                    .dayMarked(date.getDayOfWeek().name())
                    .build();
            for (AttendanceDate attendanceDate : datesMarked) {

                if (attendanceDate.getAttendanceDate().equals(date)) {
                    isMarked = true;
                    datemarked.setStaffName(
                            attendanceDate.getStaff().getFirstName() + " "
                                    + attendanceDate.getStaff().getLastName()
                    );
                    break;
                }
            }
            if (!isMarked) {
                datemarked.setStaffName("NOT MARKED");
            }

            datesMarkedList.add(datemarked);
        }

        loggingService.logGeneralActivity(
                LogType.ATTENDANCE, LogAction.READ,
                "Fetched the days attendance has been marked",
                staffId, LogStatus.SUCCESS
        );
        return ResponseEntity.ok(datesMarkedList);
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

                if (attendanceRecord.getStatus() == AttendanceStatus.ABSENT) {
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
        if (startDate == null || endDate == null) {
            return 0;
        }
        List<SchoolHoliday> holidays = semester.getInstitution() == null || semester.getInstitution().getSchoolHolidays() == null
                ? List.of() : semester.getInstitution().getSchoolHolidays();

        // School days in the term: weekdays from start to end, minus school holidays (Ben, 2026-11-27).
        int totalAttendance = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            DayOfWeek day = date.getDayOfWeek();
            if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
                continue;
            }

            boolean isHoliday = false;

            for (SchoolHoliday holiday : holidays) {
                if (holiday.getStartDate() == null || holiday.getEndDate() == null) {
                    continue;
                }
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
