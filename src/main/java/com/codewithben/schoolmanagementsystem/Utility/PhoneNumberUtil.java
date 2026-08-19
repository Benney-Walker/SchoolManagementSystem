package com.codewithben.schoolmanagementsystem.Utility;

import org.springframework.stereotype.Component;

@Component
public class PhoneNumberUtil {

    private static final String GHANA_CODE = "233";

    public String toInternational(String raw) {
        if (raw == null) {
            return null;
        }

        String digits = raw.replaceAll("[\\s()\\-]", "").replaceFirst("^\\+", "");
        if (digits.isEmpty()) {
            return null;
        }

        if (digits.startsWith("0") && digits.length() == 10) {
            return GHANA_CODE + digits.substring(1);
        }

        if (digits.startsWith(GHANA_CODE) && digits.length() == 12) {
            return digits;
        }

        if (digits.length() == 9) {
            return GHANA_CODE + digits;
        }

        return digits;
    }
}
