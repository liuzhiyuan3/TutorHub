package com.teacher.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.teacher")
public class TeacherServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeacherServerApplication.class, args);
    }
}
