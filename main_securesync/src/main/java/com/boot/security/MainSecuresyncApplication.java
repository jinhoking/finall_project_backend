package com.boot.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.boot.security"}) // 패키지명을 정확히 입력
public class MainSecuresyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainSecuresyncApplication.class, args);
    }
}