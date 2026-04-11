package com.codewithben.schoolmanagementsystem;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SchoolManagementSystemApplication {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @PostConstruct
    public void debugEnv() {
        System.out.println(">>> DB URL: " + dbUrl);
        System.out.println(">>> DB USER: " + dbUser);
        System.out.println(">>> DB PASS LENGTH: " +
                (System.getenv("MYSQL_ROOT_PASSWORD") != null ?
                        System.getenv("MYSQL_ROOT_PASSWORD").length() : "NULL"));
    }

    public static void main(String[] args) {
        SpringApplication.run(SchoolManagementSystemApplication.class, args);
    }

}
