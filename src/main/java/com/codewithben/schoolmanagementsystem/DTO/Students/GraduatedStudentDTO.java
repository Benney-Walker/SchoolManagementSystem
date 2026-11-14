package com.codewithben.schoolmanagementsystem.DTO.Students;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of GET /api/student/v1/graduated-students. The graduation fields are
 * null for students who graduated before graduation details were recorded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraduatedStudentDTO {

    private String studentId;

    private String studentName;

    private String gender;

    private String parentName;

    private String parentPhoneNumber;

    private String graduationClassName;

    private String graduationAcademicYear;

    // yyyy-MM-dd
    private String graduationDate;
}
