package com.community.owner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
//@ComponentScan(basePackages = "com.community")
@SpringBootApplication(scanBasePackages = "com.community.owner")
public class OwnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OwnerApplication.class, args);
    }
}