package com.codewithben.schoolmanagementsystem.DTO.Attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatesMarked {

    private String dateMarked;

    private String dayMarked;

    private String staffName;
}
