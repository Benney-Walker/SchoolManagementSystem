package com.codewithben.schoolmanagementsystem.Utility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String json = """
                {
                  "timestamp": "%s",
                  "status": 401,
                  "error": "Session expired. Login again",
                  "message": "%s",
                  "path": "%s"
                }
                """.formatted(
                LocalDateTime.now(),
                authException.getMessage(),
                request.getRequestURI()
        );

        log.warn("Unauthorized request to {} {}: {}",
                request.getMethod(), request.getRequestURI(), authException.getMessage());

        response.getWriter().write(json);
    }
}