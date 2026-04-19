package com.codewithben.schoolmanagementsystem;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@SpringBootApplication
public class SchoolManagementSystemApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(SchoolManagementSystemApplication.class, args);
    }

}
