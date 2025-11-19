package com.community.admin.common.service;

import com.community.admin.common.util.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS服务类
 * 
 * 提供文件上传和删除等OSS操作的业务层封装
 * 作为OssUtil工具类的上层服务，对外提供更简洁的接口
 * 
 * 关键点:
 * 1. 使用@Service注解标记为Spring服务组件
 * 2. 依赖注入OssUtil工具类，实现功能复用
 * 3. 提供上传文件和删除文件两个主要方法
 * 4. 封装了从文件URL中提取文件名的逻辑
 */
@Service
public class OssService {
    
    @Autowired
    private OssUtil ossUtil;
    
    /**
     * 上传文件到OSS
     * 
     * @param file 要上传的文件对象
     * @param folder 文件夹路径，例如 "images/" 或 "documents/"
     * @return 文件在OSS中的完整访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        return ossUtil.uploadFile(file, folder);
    }
    
    /**
     * 删除OSS中的文件
     * 
     * @param fileUrl 文件的完整URL
     * 
     * 难点说明:
     * 1. 需要从完整的URL中提取文件名，因为OSS删除操作只需要文件名
     * 2. URL格式可能因OSS配置而异，需要正确解析
     * 3. 如果URL无效或无法提取文件名，则不执行删除操作
     */
    public void deleteFile(String fileUrl) {
        // 从URL中提取文件名
        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName != null) {
            ossUtil.deleteFile(fileName);
        }
    }
    
    /**
     * 从URL中提取文件名
     * 
     * 例如: https://your-bucket-name.oss-cn-hangzhou.aliyuncs.com/images/uuid_filename.jpg
     * 提取: uuid_filename.jpg
     * 
     * @param fileUrl 文件的完整URL
     * @return 文件名，如果无法提取则返回null
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        // 查找最后一个斜杠的位置
        int lastSlashIndex = fileUrl.lastIndexOf("/");
        if (lastSlashIndex != -1 && lastSlashIndex < fileUrl.length() - 1) {
            // 返回斜杠后的部分作为文件名
            return fileUrl.substring(lastSlashIndex + 1);
        }
        
        return null;
    }
}