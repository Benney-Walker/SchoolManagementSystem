package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Entity.PaymentRecords;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRulesTest {

    private static PaymentRecords payment(float amount, boolean deleted) {
        return PaymentRecords.builder()
                .amountPaid(amount)
                .deletedAt(deleted ? LocalDateTime.now() : null)
                .build();
    }

    @Test
    void deletedPaymentsCountNowhere() {
        List<PaymentRecords> payments = List.of(payment(300, false), payment(200, true));

        assertEquals(1, PaymentRules.activePayments(payments).size());
        assertEquals(300f, PaymentRules.totalPaid(payments));
    }

    @Test
    void balanceIsRecomputedNotAdjusted() {
        // Fee 1000, payments 300 + 200, the 200 deleted: balance 700 (the old code gave 800).
        float paid = PaymentRules.totalPaid(List.of(payment(300, false), payment(200, true)));

        assertEquals(700f, PaymentRules.balance(1000f, paid));
    }

    @Test
    void noFloatNoise() {
        float paid = PaymentRules.totalPaid(List.of(payment(0.1f, false), payment(0.2f, false)));

        assertEquals(0.3f, paid);
        assertEquals(100.0f, PaymentRules.balance(100.3f, paid));
    }

    @Test
    void nullListsAreEmpty() {
        assertEquals(0f, PaymentRules.totalPaid(null));
    }

    @Test
    void reasonIsRequired() {
        assertNotNull(PaymentRules.deletionReasonProblem(null));
        assertNotNull(PaymentRules.deletionReasonProblem("  "));
        assertNotNull(PaymentRules.deletionReasonProblem("ok"));
        assertNotNull(PaymentRules.deletionReasonProblem("x".repeat(256)));
        assertNull(PaymentRules.deletionReasonProblem("Entered twice"));
    }
}
