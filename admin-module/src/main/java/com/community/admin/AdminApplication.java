package com.community.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// Knife4j增强注解
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;

@SpringBootApplication
//@ComponentScan(basePackages = {"com.community.admin"})
@MapperScan(basePackages = {"com.community.admin.mapper"})
@EnableCaching
@EnableKnife4j
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
