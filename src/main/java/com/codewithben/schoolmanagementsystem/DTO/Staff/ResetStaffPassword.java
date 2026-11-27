package com.codewithben.schoolmanagementsystem.DTO.Staff;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Body of POST /api/admin/v1/reset-staff-password: {"staffId": "ST100101", "newPassword": "..."}.
 * In the body, not the query string, so the password never lands in access logs.
 * Length and spacing rules are in PasswordResetRules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetStaffPassword {

    /** The staff member whose password is reset (not the caller). */
    @NotBlank(message = "Staff Id is required")
    private String staffId;

    @NotBlank(message = "New password is required")
    @ToString.Exclude
    private String newPassword;
}
