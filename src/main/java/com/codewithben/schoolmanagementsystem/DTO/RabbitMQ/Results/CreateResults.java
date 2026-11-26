package com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateResults {

    private String semesterId;

    private String levelId;

    private List<String> studentIds;

    // Staff member who marked the attendance; recorded as the creator of new results.
    // Null in messages queued before this field existed (the class teacher is used then).
    private String staffId;
}
