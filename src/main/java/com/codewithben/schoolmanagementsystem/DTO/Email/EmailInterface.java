package com.codewithben.schoolmanagementsystem.DTO.Email;

public interface EmailInterface {

    void sendEmail(String to, String subject, String body);
}
