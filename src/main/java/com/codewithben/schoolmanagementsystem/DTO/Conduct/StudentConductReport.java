package com.codewithben.schoolmanagementsystem.DTO.Conduct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentConductReport {

    private String regular;

    private String punctual;

    private String physicalAppearance;

    private String social;

    private String emotional;

    private String cognitiveSkills;

    private String facilitatorRemark;
}
