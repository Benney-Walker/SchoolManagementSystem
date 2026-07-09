package com.codewithben.schoolmanagementsystem.DTO.Offline;

import com.codewithben.schoolmanagementsystem.DTO.Attendance.AttendanceRequestList;
import com.codewithben.schoolmanagementsystem.DTO.Attendance.StudentAttendance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineAttendanceList {

    private String levelId;

    private String dateMarked;

    private List<AttendanceRequestList> attendanceList;
}
