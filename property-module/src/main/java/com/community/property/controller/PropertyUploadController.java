package com.community.property.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.community.property.config.OssConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/property/upload")
@Tag(name = "物业文件上传", description = "物业模块文件上传接口")
public class PropertyUploadController {
    
    @Autowired
    private OSS ossClient;
    
    @Autowired
    private OssConfig ossConfig;
    
    // 最大文件大小: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    /**
     * 上传单个图片
     */
    @PostMapping("/image")
    @Operation(summary = "上传图片", description = "上传单个图片文件到OSS，返回HTTPS图片URL")
    public Map<String, Object> uploadImage(
            @Parameter(description = "图片文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Authorization Token", required = false)
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件为空");
                return response;
            }

            // 验证文件大小
            long fileSize = file.getSize();
            if (fileSize > MAX_FILE_SIZE) {
                response.put("success", false);
                response.put("message", String.format("文件过大，最大支持50MB，当前文件大小: %.2fMB", fileSize / 1024.0 / 1024.0));
                return response;
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "仅支持图片文件");
                return response;
            }

            // 生成唯一的文件名 - 使用统一的路径格式：community/notice/{uuid}_{filename}.ext
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filenameWithoutExt = originalFilename != null ?
                    originalFilename.substring(0, originalFilename.lastIndexOf(".")) : UUID.randomUUID().toString();
            String objectKey = String.format("community/notice/%s_%s%s",
                    UUID.randomUUID().toString(),
                    filenameWithoutExt,
                    fileExtension);

            System.out.println("开始上传文件: " + originalFilename + " (" + (fileSize / 1024.0) + "KB)");
            System.out.println("OSS对象键: " + objectKey);

            // 上传到OSS
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), objectKey, inputStream);
                ossClient.putObject(putObjectRequest);
            }

            // 返回HTTPS OSS路径（虚拟主机风格：https://bucket.endpoint/object）
            String endpoint = ossConfig.getEndpoint().replaceFirst("https?://", "");
            String ossUrl = String.format("https://%s.%s/%s", ossConfig.getBucketName(), endpoint, objectKey);

            System.out.println("文件上传成功: " + ossUrl);

            response.put("success", true);
            response.put("data", new HashMap<String, Object>() {{
                put("imageUrl", ossUrl);
                put("path", objectKey);
            }});
            response.put("message", "上传成功");
            return response;
        } catch (Exception e) {
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            return response;
        }
    }
}
