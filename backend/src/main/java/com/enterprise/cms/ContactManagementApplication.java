package com.enterprise.cms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContactManagementApplication {

    private static final Logger log = LoggerFactory.getLogger(ContactManagementApplication.class);

    public static void main(String[] args) {
        log.info("Starting Contact Management System Spring Boot application...");
        SpringApplication.run(ContactManagementApplication.class, args);
        log.info("Contact Management System started successfully.");
    }
}
