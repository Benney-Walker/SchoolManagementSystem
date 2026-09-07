package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.HolidayType;
import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Holiday.Holiday;
import com.codewithben.schoolmanagementsystem.Entity.SchoolHoliday;
import com.codewithben.schoolmanagementsystem.Entity.Semester;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.SchoolHolidayRepository;
import com.codewithben.schoolmanagementsystem.Repository.SemesterRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class HolidayService {

    private final SchoolHolidayRepository schoolHolidayRepository;

    private final StaffsRepository staffsRepository;

    private final LoggingService loggingService;

    public ResponseEntity<?> addNewHoliday(String staffId, Holiday holiday) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            log.error("Could not find staff with Id {}", staffId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Internal server error! Contact developer."
            ));
        }

        //Check if holiday name exist for school
        SchoolHoliday existedHoliday =
                schoolHolidayRepository.findByStartDateAndEndDateAndHolidayNameAndInstitution_InstitutionId(
                        LocalDate.parse(holiday.getStartDate()),
                        LocalDate.parse(holiday.getEndDate()),
                        HolidayType.valueOf(holiday.getHolidayName()),
                        staff.getInstitution().getInstitutionId()
                ).orElse(null);
        if (existedHoliday == null) {

            existedHoliday = new SchoolHoliday();
            existedHoliday.setHolidayName(HolidayType.valueOf(holiday.getHolidayName()));
            existedHoliday.setStartDate(LocalDate.parse(holiday.getStartDate()));
            existedHoliday.setEndDate(LocalDate.parse(holiday.getEndDate()));
            existedHoliday.setInstitution(staff.getInstitution());

            loggingService.logGeneralActivity(LogType.SCHOOL_HOLIDAY, LogAction.CREATE, "N/A", staffId, LogStatus.SUCCESS);
            schoolHolidayRepository.save(existedHoliday);
        }

        loggingService.logGeneralActivity(LogType.SCHOOL_HOLIDAY, LogAction.CREATE, "Holiday already exists", staffId, LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Holiday already exists"
        ));
    }

    public ResponseEntity<?> updateHoliday(String staffId, Holiday holiday) {

        SchoolHoliday schoolHoliday = schoolHolidayRepository.findByHolidayId(holiday.getHolidayId()).orElse(null);
        if (schoolHoliday == null) {
            loggingService.logGeneralActivity(LogType.SCHOOL_HOLIDAY, LogAction.UPDATE, "Invalid holiday Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid holiday Id"
            ));
        }

        schoolHoliday.setHolidayName(HolidayType.valueOf(holiday.getHolidayName()));
        schoolHoliday.setStartDate(LocalDate.parse(holiday.getStartDate()));
        schoolHoliday.setEndDate(LocalDate.parse(holiday.getEndDate()));
        schoolHolidayRepository.save(schoolHoliday);

        loggingService.logGeneralActivity(LogType.SCHOOL_HOLIDAY, LogAction.UPDATE, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> loadAllHolidays(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            log.error("Could not find staff with Id {}", staffId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Internal server error! Contact developer."
            ));
        }

        List<SchoolHoliday> semesterHolidays = schoolHolidayRepository
                .findByInstitution_InstitutionId(staff.getInstitution().getInstitutionId());
        if (semesterHolidays == null || semesterHolidays.isEmpty()) {
            loggingService.logGeneralActivity(LogType.SCHOOL_HOLIDAY, LogAction.READ, "N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No holidays added yet"
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
                    .build();

            holidayList.add(holiday);
        }

        loggingService.logGeneralActivity(LogType.SCHOOL_HOLIDAY, LogAction.READ, "N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(holidayList);
    }
}
