package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.StaffStatus;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthenticatedStaffProvider {

    private final StaffsRepository staffsRepository;

    public String getStaffId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return authentication.getName();
    }

    public Staffs getStaff() {
        String staffId = getStaffId();
        if (staffId == null) {
            return null;
        }
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null || staff.getStaffStatus() == StaffStatus.INACTIVE) {
            return null;
        }
        return staff;
    }

    public String getInstitutionId() {
        Staffs staff = getStaff();
        if (staff == null || staff.getInstitution() == null) {
            return null;
        }
        return staff.getInstitution().getInstitutionId();
    }
}
