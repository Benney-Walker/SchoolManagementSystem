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
public class StaffCaching {
    private String staffName;

    private String staffId;

    private List<String> staffRoles;


}
