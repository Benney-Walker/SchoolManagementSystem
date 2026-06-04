package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Contants.HolidayType;
import com.codewithben.schoolmanagementsystem.Entity.SchoolHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolHolidayRepository extends JpaRepository<SchoolHoliday, Integer> {

    Optional<SchoolHoliday> findByHolidayId(int holidayId);

    Optional<SchoolHoliday> findByHolidayNameAndInstitution_InstitutionId(HolidayType holidayName, String institutionId);

    List<SchoolHoliday> findBySemester_SemesterID(String semesterId);
}
