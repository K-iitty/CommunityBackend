package com.community.owner.controller;

import com.community.owner.service.FileUploadService;
import com.community.owner.service.RedisMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/owner/upload")
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {
    @Autowired
    private RedisMessageService redisMessageService;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 上传问题反馈图片
     */
    @PostMapping("/issue-image")
    @Operation(summary = "上传问题反馈图片", description = "上传问题反馈的图片到阿里云OSS，返回图片URL")
    public Map<String, Object> uploadIssueImage(
            @Parameter(description = "上传的图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return response;
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "请上传图片文件");
                return response;
            }
            
            // 检查文件大小（限制为5MB）
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                response.put("success", false);
                response.put("message", "文件大小不能超过5MB");
                return response;
            }
            
            // 上传文件
            String fileUrl = fileUploadService.uploadIssueImage(file);
            
            response.put("success", true);
            response.put("message", "上传成功");
            response.put("data", fileUrl);
            return response;
            
        } catch (Exception e) {
            log.error("上传问题反馈图片失败", e);
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 上传访问控制照片（证件照）
     */
    @PostMapping("/access-control-photo")
    @Operation(summary = "上传访问控制照片", description = "上传个人信息的证件照到阿里云OSS，返回图片URL")
    public Map<String, Object> uploadAccessControlPhoto(
            @Parameter(description = "上传的图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return response;
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "请上传图片文件");
                return response;
            }
            
            // 检查文件大小（限制为5MB）
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                response.put("success", false);
                response.put("message", "文件大小不能超过5MB");
                return response;
            }
            
            // 上传文件
            String fileUrl = fileUploadService.uploadFile(file, "owner/access_control");
            
            response.put("success", true);
            response.put("message", "上传成功");
            response.put("data", fileUrl);
            return response;
            
        } catch (Exception e) {
            log.error("上传访问控制照片失败", e);
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 上传身份证照片
     */
    @PostMapping("/id-card-photo")
    @Operation(summary = "上传身份证照片", description = "上传个人信息的身份证照片到阿里云OSS，返回图片URL")
    public Map<String, Object> uploadIdCardPhoto(
            @Parameter(description = "上传的图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return response;
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "请上传图片文件");
                return response;
            }
            
            // 检查文件大小（限制为5MB）
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                response.put("success", false);
                response.put("message", "文件大小不能超过5MB");
                return response;
            }
            
            // 上传文件
            String fileUrl = fileUploadService.uploadFile(file, "owner/id_card");
            
            response.put("success", true);
            response.put("message", "上传成功");
            response.put("data", fileUrl);
            return response;
            
        } catch (Exception e) {
            log.error("上传身份证照片失败", e);
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            return response;
        }
    }
}
