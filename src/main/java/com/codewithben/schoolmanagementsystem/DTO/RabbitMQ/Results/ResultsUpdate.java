package com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultsUpdate {
    private String staffId;

    private String subjectId;

    private List<Long> affectedResultsIds;
}
