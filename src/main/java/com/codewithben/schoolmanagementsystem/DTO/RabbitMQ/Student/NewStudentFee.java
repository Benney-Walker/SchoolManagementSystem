package com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewStudentFee {

    private String studentId;

    private String levelId;
}
