package com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Attendance;

import com.codewithben.schoolmanagementsystem.Constants.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyAttendance {

    private LocalDate attendanceDate;

    private List<String> studentsIds;

    private Map<String, AttendanceStatus> attendanceMap;
}
