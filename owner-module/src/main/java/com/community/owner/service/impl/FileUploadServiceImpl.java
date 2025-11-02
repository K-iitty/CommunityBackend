package com.community.owner.service.impl;

import com.aliyun.oss.OSS;
import com.community.owner.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 文件上传服务实现
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    @Autowired
    private OSS ossClient;
    
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }
            
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 构建OSS对象key
            String objectKey = folder + "/" + fileName;
            
            // 上传文件到OSS
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, objectKey, inputStream);
            
            // 构建文件的URL
            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + objectKey;
            
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String uploadIssueImage(MultipartFile file) {
        return uploadFile(file, "owner/issue");
    }
}
