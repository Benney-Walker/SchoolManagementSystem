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
}
