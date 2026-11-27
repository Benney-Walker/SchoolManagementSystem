package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Entity.PaymentRecords;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Rules shared by every place fee payments are read, summed, edited or deleted (issue 3).
 *
 * - Deleted payments (soft delete) count nowhere: not in history, totals or balances.
 * - A fee record's amountPaid is always the sum of its payments that aren't deleted, and its
 *   balance is totalAmount minus that, recomputed from scratch, never adjusted by a delta.
 * - Deleting a payment needs a reason (Ben, 2026-11-27).
 */
public final class PaymentRules {

    public static final int MIN_REASON_LENGTH = 3;

    public static final int MAX_REASON_LENGTH = 255;

    private PaymentRules() {
    }

    public static List<PaymentRecords> activePayments(List<PaymentRecords> payments) {
        return payments == null ? List.of() : payments.stream()
                .filter(payment -> payment != null && !payment.isDeleted())
                .toList();
    }

    /* Summed in BigDecimal and rounded to the pesewa, so float noise doesn't creep into balances. */
    public static float totalPaid(List<PaymentRecords> payments) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PaymentRecords payment : activePayments(payments)) {
            sum = sum.add(new BigDecimal(Float.toString(payment.getAmountPaid())));
        }
        return sum.setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    public static float balance(float totalAmount, float totalPaid) {
        return new BigDecimal(Float.toString(totalAmount))
                .subtract(new BigDecimal(Float.toString(totalPaid)))
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    /** Null when the reason is acceptable, else the message for the 400. */
    public static String deletionReasonProblem(String reason) {
        if (reason == null || reason.isBlank()) {
            return "A reason is required to delete a payment";
        }
        int length = reason.strip().length();
        if (length < MIN_REASON_LENGTH) {
            return "Reason must be at least " + MIN_REASON_LENGTH + " characters";
        }
        if (length > MAX_REASON_LENGTH) {
            return "Reason must be at most " + MAX_REASON_LENGTH + " characters";
        }
        return null;
    }
}
