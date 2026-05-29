package com.codewithben.schoolmanagementsystem.Utility;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "status", 401,
                        "message", "Invalid ID or password"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(
            Exception ex,
            HttpServletRequest request
    ) {

        Map<String, Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", 500);
        error.put("error", ex.getMessage());
        error.put("message", "Internal Server Error. Contact developer");
        error.put("path", request.getRequestURI());

        System.out.println(error);
        return ResponseEntity.status(500).body(error);
    }
}
