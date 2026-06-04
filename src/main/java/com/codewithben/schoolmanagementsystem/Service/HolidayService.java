package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.HolidayType;
import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Holiday.Holiday;
import com.codewithben.schoolmanagementsystem.Entity.SchoolHoliday;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.SchoolHolidayRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class HolidayService {

    private final SchoolHolidayRepository schoolHolidayRepository;

    private final SemesterRepository semesterRepository;

    private final StaffsRepository staffsRepository;

    private final UtilityClass utilityClass;

    private final LoggingService loggingService;

    public ResponseEntity<?> addOrUpdateHoliday(String staffId, Holiday holiday) {
        String logData
                = "Holiday Name= " + holiday.getHolidayName() +
                " Start Date= " + holiday.getStartDate() +
                " End Date= " + holiday.getEndDate() +
                " Semester Name= " + holiday.getSemesterName() +
                " Academic Year= " + holiday.getAcademicYear();

        Semester semester = semesterRepository.findBySemesterID(holiday.getSemesterId()).orElse(null);
        if (semester == null) {
            loggingService.logActivity(LogType.HOLIDAY, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid semester Id"
            ));
        }

        //0 means new holiday
        if (holiday.getHolidayId() == 0) {

            //Check if holiday name exist for school
            SchoolHoliday existedHoliday =
                    schoolHolidayRepository.findByHolidayNameAndInstitution_InstitutionId(
                            HolidayType.valueOf(holiday.getHolidayName()),
                            semester.getInstitution().getInstitutionId()
                    ).orElse(null);
            if (existedHoliday == null) {

                existedHoliday = new SchoolHoliday();
                existedHoliday.setHolidayName(HolidayType.valueOf(holiday.getHolidayName()));
                existedHoliday.setStartDate(LocalDate.parse(holiday.getStartDate()));
                existedHoliday.setEndDate(LocalDate.parse(holiday.getEndDate()));
                existedHoliday.setSemester(semester);
                existedHoliday.setInstitution(semester.getInstitution());

                loggingService.logActivity(LogType.HOLIDAY, logData, staffId, LogStatus.SUCCESS.name());
                schoolHolidayRepository.save(existedHoliday);
            }

            loggingService.logActivity(LogType.HOLIDAY, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Holiday already exists"
            ));
        }

        SchoolHoliday schoolHoliday = schoolHolidayRepository.findByHolidayId(holiday.getHolidayId()).orElse(null);
        if (schoolHoliday == null) {
            loggingService.logActivity(LogType.HOLIDAY, logData, staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid holiday Id"
            ));
        }

        schoolHoliday.setHolidayName(HolidayType.valueOf(holiday.getHolidayName()));
        schoolHoliday.setStartDate(LocalDate.parse(holiday.getStartDate()));
        schoolHoliday.setEndDate(LocalDate.parse(holiday.getEndDate()));
        schoolHoliday.setSemester(semester);
        schoolHoliday.setInstitution(semester.getInstitution());
        schoolHolidayRepository.save(schoolHoliday);

        loggingService.logActivity(LogType.HOLIDAY, logData, staffId, LogStatus.SUCCESS.name());
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadAllHolidays(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            loggingService.logActivity(LogType.HOLIDAY, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        String currentSemesterId = utilityClass.getCurrentSemesterId(
                staff.getInstitution().getInstitutionId()
        );

        List<SchoolHoliday> semesterHolidays = schoolHolidayRepository
                .findBySemester_SemesterID(currentSemesterId);
        if (semesterHolidays == null || semesterHolidays.isEmpty()) {
            loggingService.logActivity(LogType.HOLIDAY, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "School has no holidays"
            ));
        }

        //Retrieve holidays
        List<Holiday> holidayList = new ArrayList<>();
        for (SchoolHoliday schoolHoliday : semesterHolidays) {

            Holiday holiday = Holiday.builder()
                    .holidayId(schoolHoliday.getHolidayId())
                    .holidayName(schoolHoliday.getHolidayName().name())
                    .startDate(schoolHoliday.getStartDate().toString())
                    .endDate(schoolHoliday.getEndDate().toString())
                    .semesterId(currentSemesterId)
                    .semesterName(schoolHoliday.getSemester().getSemesterName())
                    .academicYear(schoolHoliday.getSemester().getAcademicYear())
                    .build();

            holidayList.add(holiday);
        }

        loggingService.logActivity(LogType.HOLIDAY, "N/A", staffId, LogStatus.SUCCESS.name());
        return ResponseEntity.ok(holidayList);
    }
}
