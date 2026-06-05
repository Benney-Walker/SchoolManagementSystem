package com.codewithben.schoolmanagementsystem.Utility;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Service.LoggingService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    private final LoggingService loggingService;

    public AuthenticationFailureListener(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();

        System.out.println(username);

        loggingService.logGeneralActivity(
                LogType.STAFF,
                LogAction.LOGIN,
                "N/A",
                username,
                LogStatus.FAILED
        );
    }
}
