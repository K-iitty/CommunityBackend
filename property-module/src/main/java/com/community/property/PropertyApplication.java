package com.community.property;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 物业端应用启动类
 */
@SpringBootApplication
//@ComponentScan(basePackages = "com.community")
public class PropertyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PropertyApplication.class, args);
    }
}

