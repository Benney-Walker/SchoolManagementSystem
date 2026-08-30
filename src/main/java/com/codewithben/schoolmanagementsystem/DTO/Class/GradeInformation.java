package com.codewithben.schoolmanagementsystem.DTO.Class;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeInformation {
    private String className;

    private int classSize;
}
