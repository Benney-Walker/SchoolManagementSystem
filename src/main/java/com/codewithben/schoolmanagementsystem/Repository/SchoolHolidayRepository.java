package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Constants.HolidayType;
import com.codewithben.schoolmanagementsystem.Entity.SchoolHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolHolidayRepository extends JpaRepository<SchoolHoliday, Integer> {

    Optional<SchoolHoliday> findByHolidayId(int holidayId);

    Optional<SchoolHoliday> findByStartDateAndEndDateAndHolidayNameAndInstitution_InstitutionId(
            LocalDate startDate, LocalDate endDate, HolidayType holidayName, String institutionId
    );

    List<SchoolHoliday> findBySemester_SemesterID(String semesterId);
}
