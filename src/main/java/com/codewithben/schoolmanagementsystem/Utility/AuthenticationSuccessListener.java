package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Service.LoggingService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final LoggingService loggingService;

    public AuthenticationSuccessListener(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();

        loggingService.logActivity(
                "AUTHENTICATION",
                "N/A",
                auth.getName(),
                "SUCCESS"
        );
    }
}
