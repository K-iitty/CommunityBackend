package com.community.admin.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * OSS工具类
 * 
 * 提供阿里云OSS对象存储的基本操作封装
 * 包括文件上传、删除和生成预签名URL等功能
 * 
 * 关键点:
 * 1. 使用@Component注解注册为Spring组件
 * 2. 通过@Autowired注入OSS客户端实例
 * 3. 通过@Value注解从配置文件读取OSS相关配置
 * 4. 使用SLF4J进行日志记录
 * 5. 全面的异常处理，区分OSS异常和其他异常
 * 
 * 难点说明:
 * 1. 文件名唯一性: 使用UUID保证文件名唯一，避免文件覆盖
 * 2. 异常处理: 区分OSSException和其他Exception，提供详细的错误信息
 * 3. 资源管理: 正确处理InputStream等资源
 * 4. URL生成: 正确构造OSS文件访问URL
 */
@Component
public class OssUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(OssUtil.class);
    
    /**
     * OSS客户端实例
     * 通过@Autowired自动注入，在OssConfig中配置
     */
    @Autowired
    private OSS ossClient;
    
    /**
     * OSS存储桶名称
     */
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    
    /**
     * OSS接入点
     */
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    
    /**
     * 上传文件到OSS
     * 
     * @param file Spring MultipartFile对象
     * @param folder 文件夹路径，例如 "images/" 或 "documents/"
     * @return 文件在OSS中的完整访问URL
     * 
     * 实现细节:
     * 1. 使用UUID生成唯一文件名，避免文件名冲突
     * 2. 构造完整的文件路径: folder + UUID + 原始文件名
     * 3. 通过ossClient.putObject方法上传文件
     * 4. 构造并返回文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 生成唯一文件名
            String fileName = folder + UUID.randomUUID() + "_" + originalFilename;
            
            // 上传文件
            PutObjectResult result = ossClient.putObject(bucketName, fileName, file.getInputStream());
            
            // 构造文件访问URL
            String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
            logger.info("文件上传成功: {}", url);
            return url;
        } catch (OSSException oe) {
            logger.error("OSS错误: {}", oe.getMessage());
            logger.error("错误代码: {}", oe.getErrorCode());
            logger.error("请求ID: {}", oe.getRequestId());
            logger.error("主机ID: {}", oe.getHostId());
            throw new RuntimeException("文件上传失败: " + oe.getMessage(), oe);
        } catch (IOException e) {
            logger.error("IO错误: {}", e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        } catch (Exception e) {
            logger.error("未知错误: {}", e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    /**
     * 上传文件流到OSS
     * 
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param folder 文件夹路径
     * @return 文件在OSS中的完整访问URL
     * 
     * 适用场景:
     * 当文件以流的形式存在时使用此方法，例如处理网络文件或临时文件
     */
    public String uploadFile(InputStream inputStream, String fileName, String folder) {
        try {
            // 生成唯一文件名
            String uniqueFileName = folder + UUID.randomUUID() + "_" + fileName;
            
            // 上传文件
            PutObjectResult result = ossClient.putObject(bucketName, uniqueFileName, inputStream);
            
            // 构造文件访问URL
            String url = "https://" + bucketName + "." + endpoint + "/" + uniqueFileName;
            logger.info("文件上传成功: {}", url);
            return url;
        } catch (OSSException oe) {
            logger.error("OSS错误: {}", oe.getMessage());
            logger.error("错误代码: {}", oe.getErrorCode());
            logger.error("请求ID: {}", oe.getRequestId());
            logger.error("主机ID: {}", oe.getHostId());
            throw new RuntimeException("文件上传失败: " + oe.getMessage(), oe);
        } catch (Exception e) {
            logger.error("文件上传异常: {}", e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    /**
     * 删除OSS中的文件
     * 
     * @param fileName 文件名（包含路径）
     * 
     * 注意事项:
     * 1. fileName应为完整路径，例如 "images/uuid_filename.jpg"
     * 2. 删除操作是不可逆的，需要谨慎使用
     */
    public void deleteFile(String fileName) {
        try {
            ossClient.deleteObject(bucketName, fileName);
            logger.info("文件删除成功: {}", fileName);
        } catch (OSSException oe) {
            logger.error("OSS错误: {}", oe.getMessage());
            logger.error("错误代码: {}", oe.getErrorCode());
            logger.error("请求ID: {}", oe.getRequestId());
            logger.error("主机ID: {}", oe.getHostId());
            throw new RuntimeException("文件删除失败: " + oe.getMessage(), oe);
        } catch (Exception e) {
            logger.error("文件删除异常: {}", e.getMessage());
            throw new RuntimeException("文件删除失败", e);
        }
    }
    
    /**
     * 生成文件的预签名URL，用于临时访问私有文件
     * 
     * @param fileName 文件名（包含路径）
     * @param expiration 过期时间（毫秒）
     * @return 预签名URL
     * 
     * 应用场景:
     * 1. 为私有文件提供临时访问链接
     * 2. 控制文件访问权限和时效
     * 3. 避免暴露原始文件URL
     */
    public String generatePresignedUrl(String fileName, long expiration) {
        try {
            // 设置URL过期时间
            Date expirationDate = new Date(System.currentTimeMillis() + expiration);
            // 生成以GET方法访问对象的签名URL
            URL url = ossClient.generatePresignedUrl(bucketName, fileName, expirationDate);
            return url.toString();
        } catch (Exception e) {
            logger.error("生成预签名URL异常: {}", e.getMessage());
            throw new RuntimeException("生成预签名URL失败", e);
        }
    }
}