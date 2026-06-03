package com.codewithben.schoolmanagementsystem.DTO.Staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewStaffList {
    private String staffId;

    private String staffName;

    private List<String> staffRole;

}
