package com.community.owner.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class Knife4jConfig implements WebMvcConfigurer {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8081").description("本地环境")))
                .info(new Info()
                        .title("社区客户端后台管理系统API文档")
                        .description("社区客户端后台管理系统RESTful APIs")
                        .version("1.0")
                        .contact(new Contact()
                                .name("社区开发团队")
                                .url("http://localhost:8081/")
                                .email("developer@community.com"))
                        .license(new License().name("Apache 2.0")
                                .url("http://localhost:8081/")));
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}