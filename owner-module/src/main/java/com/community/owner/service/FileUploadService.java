package com.community.owner.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    
    /**
     * 上传文件到阿里云OSS
     * 
     * @param file 上传的文件
     * @param folder 文件夹（如：owner/issue）
     * @return 阿里云OSS文件的URL
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * 上传问题反馈图片
     * 
     * @param file 上传的文件
     * @return 阿里云OSS文件的URL
     */
    String uploadIssueImage(MultipartFile file);
}
