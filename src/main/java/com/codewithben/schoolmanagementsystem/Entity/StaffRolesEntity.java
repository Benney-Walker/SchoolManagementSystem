package com.codewithben.schoolmanagementsystem.Entity;

import com.codewithben.schoolmanagementsystem.Contants.StaffRoles;
import jakarta.persistence.*;

@Entity
public class StaffRolesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "Staffs_staffId")
    private Staffs staff;

    @Enumerated(EnumType.STRING)
    private StaffRoles staffRole;

    public StaffRolesEntity() {}

    public StaffRolesEntity(Staffs staff, StaffRoles staffRole) {
        this.staff = staff;
        this.staffRole = staffRole;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Staffs getStaff() {
        return staff;
    }

    public void setStaff(Staffs staff) {
        this.staff = staff;
    }

    public StaffRoles getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(StaffRoles staffRole) {
        this.staffRole = staffRole;
    }
}
