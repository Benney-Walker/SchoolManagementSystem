package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.StaffRoles;

import java.util.Set;

/**
 * Who may reset whose password, and what a new password must look like.
 *
 * - Only a PRINCIPAL or an ADMINISTRATOR may reset passwords (teachers and accountants used
 *   to be able to reset anyone's, including the principal's).
 * - An ADMINISTRATOR may not reset a PRINCIPAL's password, so an administrator account
 *   can't be used to take over the principal's. A PRINCIPAL may reset anyone's.
 * - The target must be at the caller's own school; that part is checked by the lookup.
 */
public final class PasswordResetRules {

    public static final int MIN_LENGTH = 8;

    public static final int MAX_LENGTH = 72;   // BCrypt ignores bytes after the 72nd

    private PasswordResetRules() {
    }

    public static boolean mayResetPasswords(Set<StaffRoles> callerRoles) {
        return callerRoles.contains(StaffRoles.PRINCIPAL) || callerRoles.contains(StaffRoles.ADMINISTRATOR);
    }

    public static boolean mayReset(Set<StaffRoles> callerRoles, Set<StaffRoles> targetRoles) {
        if (callerRoles.contains(StaffRoles.PRINCIPAL)) {
            return true;
        }
        return callerRoles.contains(StaffRoles.ADMINISTRATOR) && !targetRoles.contains(StaffRoles.PRINCIPAL);
    }

    /** Null when the password is acceptable, else the message for the 400. */
    public static String passwordProblem(String password) {
        if (password == null || password.isBlank()) {
            return "New password is required";
        }
        if (password.length() < MIN_LENGTH) {
            return "Password must be at least " + MIN_LENGTH + " characters";
        }
        if (password.length() > MAX_LENGTH) {
            return "Password must be at most " + MAX_LENGTH + " characters";
        }
        if (!password.equals(password.strip())) {
            return "Password can't start or end with a space";
        }
        return null;
    }
}
