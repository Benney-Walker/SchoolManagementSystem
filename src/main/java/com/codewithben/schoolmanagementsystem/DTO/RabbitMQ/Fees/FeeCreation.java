package com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeCreation {

    private String semesterId;

    private List<String> levelIds;
}
