package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.StaffRoles;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.codewithben.schoolmanagementsystem.Constants.StaffRoles.*;
import static org.junit.jupiter.api.Assertions.*;

class PasswordResetRulesTest {

    @Test
    void onlyPrincipalsAndAdministratorsMayResetPasswords() {
        assertTrue(PasswordResetRules.mayResetPasswords(Set.of(PRINCIPAL)));
        assertTrue(PasswordResetRules.mayResetPasswords(Set.of(ADMINISTRATOR, TEACHING_STAFF)));
        assertFalse(PasswordResetRules.mayResetPasswords(Set.of(TEACHING_STAFF)));
        assertFalse(PasswordResetRules.mayResetPasswords(Set.of(ACCOUNTANT)));
        assertFalse(PasswordResetRules.mayResetPasswords(Set.<StaffRoles>of()));
    }

    @Test
    void administratorCannotResetAPrincipal() {
        assertFalse(PasswordResetRules.mayReset(Set.of(ADMINISTRATOR), Set.of(PRINCIPAL)));
        assertFalse(PasswordResetRules.mayReset(Set.of(ADMINISTRATOR), Set.of(PRINCIPAL, TEACHING_STAFF)));
        assertTrue(PasswordResetRules.mayReset(Set.of(ADMINISTRATOR), Set.of(TEACHING_STAFF)));
        assertTrue(PasswordResetRules.mayReset(Set.of(ADMINISTRATOR), Set.of(ADMINISTRATOR)));
    }

    @Test
    void principalCanResetAnyone() {
        assertTrue(PasswordResetRules.mayReset(Set.of(PRINCIPAL), Set.of(PRINCIPAL)));
        assertTrue(PasswordResetRules.mayReset(Set.of(PRINCIPAL), Set.of(ACCOUNTANT)));
    }

    @Test
    void passwordRules() {
        assertNull(PasswordResetRules.passwordProblem("Tarkwa2026"));
        assertNotNull(PasswordResetRules.passwordProblem(null));
        assertNotNull(PasswordResetRules.passwordProblem("   "));
        assertNotNull(PasswordResetRules.passwordProblem("short7!"));
        assertNotNull(PasswordResetRules.passwordProblem(" Tarkwa2026"));
        assertNotNull(PasswordResetRules.passwordProblem("x".repeat(73)));
    }
}
