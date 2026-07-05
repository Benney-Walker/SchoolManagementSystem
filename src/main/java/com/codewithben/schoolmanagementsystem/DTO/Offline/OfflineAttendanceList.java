package com.codewithben.schoolmanagementsystem.DTO.Offline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineAttendanceList {

    private String staffId;

    private String studentId;

    private String levelId;

    private String status;

    private String dateMarked;
}
