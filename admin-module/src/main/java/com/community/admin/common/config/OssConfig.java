package com.community.admin.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 
 * 用于配置和初始化阿里云OSS客户端
 * 通过application.yml中的配置自动注入相关属性
 * 
 * 配置示例:
 * aliyun:
 *   oss:
 *     endpoint: oss-cn-hangzhou.aliyuncs.com
 *     bucket-name: your-bucket-name
 *     access-key-id: your-access-key-id
 *     access-key-secret: your-access-key-secret
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    
    /**
     * OSS服务接入点
     * 例如: oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;
    
    /**
     * 存储空间名称
     */
    private String bucketName;
    
    /**
     * 访问密钥ID
     */
    private String accessKeyId;
    
    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;
    
    /**
     * 创建并配置OSS客户端Bean
     * 
     * 关键点:
     * 1. 使用OSSClientBuilder构建OSS客户端实例
     * 2. 从环境变量中获取访问密钥，提高安全性
     * 3. 通过@Bean注解将OSS客户端注册为Spring Bean，便于其他组件注入使用
     * 
     * 注意: 实际项目中推荐从环境变量或配置中心获取敏感信息，而不是直接写在配置文件中
     * 
     * @return OSS客户端实例
     */
    @Bean
    public OSS ossClient() {
        // 创建OSSClient实例
        return new OSSClientBuilder()
                .build(endpoint, System.getenv("OSS_ACCESS_KEY_ID"), 
                      System.getenv("OSS_ACCESS_KEY_SECRET"));
    }
}