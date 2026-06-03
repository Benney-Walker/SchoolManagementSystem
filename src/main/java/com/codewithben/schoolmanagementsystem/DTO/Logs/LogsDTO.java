package com.codewithben.schoolmanagementsystem.DTO.Logs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogsDTO {
    private int id;

    private String time;

    private String date;

    private String type;

    private String message;

    private String status;

    private String createdBy;

}
