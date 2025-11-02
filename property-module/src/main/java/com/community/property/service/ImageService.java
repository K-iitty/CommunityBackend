package com.community.property.service;

import com.aliyun.oss.OSS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * 图片管理服务 - 处理OSS上传、删除和JSON图片数组操作
 * 集成到各个业务操作中，确保图片与业务数据的一致性
 * 
 * ========== 使用指南 ==========
 * 
 * 1. 单一图片字段更新（如floor_plan_image VARCHAR字段）:
 *    String newPath = imageService.updateSingleImage(
 *        currentPath,                 // 当前图片路径，如果要删除则传null
 *        multipartFile,               // 新上传的文件
 *        "house/floorplan",          // 文件夹路径
 *        houseId                      // 实体ID
 *    );
 * 
 * 2. 多个图片字段更新（如id_card_photos TEXT JSON数组字段）:
 *    String newJson = imageService.updateMultipleImages(
 *        currentJson,                 // 当前图片JSON数组字符串
 *        imagesToDelete,              // 要删除的图片路径列表
 *        newImageFiles,               // 新上传的文件列表
 *        maxCount,                    // 最大数量限制（2表示最多2张，-1表示无限制）
 *        "staff/idcard",             // 文件夹路径
 *        staffId                      // 实体ID
 *    );
 * 
 * 3. 手动处理JSON数组:
 *    List<String> images = imageService.jsonToImages(json);       // JSON转列表
 *    String json = imageService.imagesToJson(images);             // 列表转JSON
 *    String newJson = imageService.addImage(json, newPath);       // 添加单个
 *    String newJson = imageService.addImages(json, paths);        // 添加多个
 *    String newJson = imageService.removeImage(json, path);       // 删除单个
 *    String newJson = imageService.removeImages(json, paths);     // 删除多个
 * 
 * ========== 图片字段约束 ==========
 * 根据sql.md定义：
 * - Staff.id_card_photos: 0-2张 (TEXT JSON数组)
 * - Staff.certificate_photos: 0-多张 (TEXT JSON数组)
 * - CommunityInfo.community_images: 0-多张 (TEXT JSON数组)
 * - House.floor_plan_image: 1张 (VARCHAR 单个)
 * - Vehicle.driver_license_image: 1张 (VARCHAR 单个)
 * - Vehicle.vehicle_images: 0-多张 (TEXT JSON数组)
 * - OwnerIssue.issue_images: 多张 (TEXT JSON数组)
 * - OwnerIssue.process_images: 多张 (TEXT JSON数组)
 * - OwnerIssue.result_images: 多张 (TEXT JSON数组)
 * - OwnerIssue.additional_images: 0-多张 (TEXT JSON数组)
 * - CommunityNotice.notice_images: 0-多张 (TEXT JSON数组)
 * - MeterReading.reading_image: 1张 (VARCHAR 单个)
 */
@Service
public class ImageService {

    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将OSS路径转换为完整URL
     * @param osspath OSS对象路径（如：property/notice/images/4/1761798885999_ba6f7222.jpg）
     * @return 完整的可访问URL
     */
    public String getImageUrl(String osspath) {
        if (osspath == null || osspath.isEmpty()) {
            return null;
        }
        
        // 如果已经是完整URL，直接返回
        if (osspath.startsWith("http://") || osspath.startsWith("https://")) {
            return osspath;
        }
        
        // 构建OSS完整URL
        // 从endpoint中提取域名（如：oss-cn-hangzhou.aliyuncs.com）
        String domain = endpoint.replace("https://", "").replace("http://", "");
        String url = String.format("https://%s.%s/%s", bucketName, domain, osspath);
        return url;
    }

    /**
     * 从完整URL中提取相对路径
     * 例如：https://smart-community-system.oss-cn-beijing.aliyuncs.com/community/notice/xxx.jpg
     * 返回：community/notice/xxx.jpg
     */
    public String extractPathFromUrl(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty()) {
            return null;
        }
        
        // 如果不是完整URL，直接返回
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            return fullUrl;
        }
        
        // 从 bucket-name.endpoint/path 中提取 path
        // 例如：https://smart-community-system.oss-cn-beijing.aliyuncs.com/community/notice/xxx.jpg
        String domain = bucketName + "." + endpoint.replace("https://", "").replace("http://", "");
        if (fullUrl.contains(domain + "/")) {
            return fullUrl.substring(fullUrl.indexOf(domain + "/") + domain.length() + 1);
        }
        
        return fullUrl;
    }

    /**
     * 上传单个文件到OSS
     * @param file 上传的文件
     * @param folder 文件夹路径（如：staff/idcard, vehicle/license）
     * @param entityId 实体ID
     * @return OSS路径
     */
    public String uploadImage(MultipartFile file, String folder, Long entityId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("仅支持图片文件");
        }

        // 生成文件名：folder/{uuid}_{original_filename_without_extension}.ext
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filenameWithoutExt = originalFilename != null ?
                originalFilename.substring(0, originalFilename.lastIndexOf(".")) : UUID.randomUUID().toString();
        String objectKey = String.format("%s/%s_%s%s",
                folder, 
                UUID.randomUUID().toString(),
                filenameWithoutExt,
                fileExtension);

        // 上传到OSS
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(bucketName, objectKey, inputStream);
        }

        // 返回完整的阿里云URL而不是对象键
        return getImageUrl(objectKey);
    }

    /**
     * 批量上传文件到OSS
     */
    public List<String> uploadImages(List<MultipartFile> files, String folder, Long entityId) throws Exception {
        List<String> paths = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return paths;
        }
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                paths.add(uploadImage(file, folder, entityId));
            }
        }
        return paths;
    }

    /**
     * 从OSS删除文件
     */
    public boolean deleteImage(String osspath) {
        try {
            if (osspath != null && !osspath.isEmpty()) {
                // 如果是完整URL，需要提取OSS对象键
                String objectKey = osspath;
                if (osspath.startsWith("http://") || osspath.startsWith("https://")) {
                    // 从URL中提取对象键
                    // URL格式: https://bucket-name.oss-cn-beijing.aliyuncs.com/property/staff/idcard/21/1698765432123_abc123.jpg
                    // 需要提取: property/staff/idcard/21/1698765432123_abc123.jpg
                    int bucketIndex = osspath.indexOf(bucketName);
                    if (bucketIndex != -1) {
                        int pathStart = osspath.indexOf("/", bucketIndex + bucketName.length());
                        if (pathStart != -1) {
                            objectKey = osspath.substring(pathStart + 1);
                        }
                    }
                }
                ossClient.deleteObject(bucketName, objectKey);
            }
            return true;
        } catch (Exception e) {
            // 记录错误但不抛出异常
            return false;
        }
    }

    /**
     * 批量删除文件
     */
    public void deleteImages(List<String> paths) {
        if (paths == null) {
            return;
        }
        for (String path : paths) {
            deleteImage(path);
        }
    }

    /**
     * 将图片路径列表转为JSON字符串
     */
    public String imagesToJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从JSON字符串解析为图片路径列表
     */
    public List<String> jsonToImages(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 添加单个图片到JSON数组
     */
    public String addImage(String currentJson, String newImagePath) {
        List<String> images = jsonToImages(currentJson);
        if (newImagePath != null && !newImagePath.isEmpty()) {
            images.add(newImagePath);
        }
        return imagesToJson(images);
    }

    /**
     * 添加多个图片到JSON数组
     */
    public String addImages(String currentJson, List<String> newImagePaths) {
        List<String> images = jsonToImages(currentJson);
        if (newImagePaths != null) {
            images.addAll(newImagePaths);
        }
        return imagesToJson(images);
    }

    /**
     * 从JSON数组中删除指定图片
     */
    public String removeImage(String currentJson, String imagePathToRemove) {
        List<String> images = jsonToImages(currentJson);
        images.removeIf(img -> img.equals(imagePathToRemove));
        return imagesToJson(images);
    }

    /**
     * 从JSON数组中删除多个图片
     */
    public String removeImages(String currentJson, List<String> imagePathsToRemove) {
        List<String> images = jsonToImages(currentJson);
        if (imagePathsToRemove != null) {
            for (String path : imagePathsToRemove) {
                images.removeIf(img -> img.equals(path));
            }
        }
        return imagesToJson(images);
    }

    /**
     * 替换所有图片
     */
    public String replaceImages(List<String> newImages) {
        return imagesToJson(newImages);
    }

    /**
     * 生成OSS图片的完整URL
     */
    public String generateImageUrl(String osspath) {
        if (osspath == null || osspath.isEmpty()) {
            return null;
        }
        return endpoint.replaceFirst("https?://", "https://") + "/" + bucketName + "/" + osspath;
    }

    /**
     * 批量生成图片的完整URL
     */
    public List<String> generateImageUrls(List<String> paths) {
        List<String> urls = new ArrayList<>();
        if (paths != null) {
            for (String path : paths) {
                urls.add(generateImageUrl(path));
            }
        }
        return urls;
    }

    /**
     * 验证图片数量
     */
    public boolean validateImageCount(String json, int maxCount) {
        List<String> images = jsonToImages(json);
        return images.size() <= maxCount;
    }

    /**
     * 验证单一图片字段（VARCHAR字段，只能有0或1张图片）
     */
    public boolean validateSingleImage(String imagePath) {
        return imagePath == null || imagePath.isEmpty() || !imagePath.contains("/");
    }

    /**
     * 处理单一图片的更新（用于VARCHAR字段）
     * @param currentImagePath 当前图片路径
     * @param newImageFile 新上传的图片
     * @param folder OSS文件夹
     * @param entityId 实体ID
     * @return 新的图片路径
     */
    public String updateSingleImage(String currentImagePath, MultipartFile newImageFile,
            String folder, Long entityId) throws Exception {
        // 如果有新图片，先删除旧图片，再上传新图片
        if (newImageFile != null && !newImageFile.isEmpty()) {
            if (currentImagePath != null && !currentImagePath.isEmpty()) {
                deleteImage(currentImagePath);
            }
            return uploadImage(newImageFile, folder, entityId);
        }
        // 没有新图片则保持原有图片
        return currentImagePath;
    }

    /**
     * 处理多个图片的更新（用于TEXT JSON数组字段）
     * @param currentJson 当前图片JSON数组
     * @param imagesToDelete 需要删除的图片路径列表
     * @param newImageFiles 新上传的图片列表
     * @param maxCount 最大数量限制（-1表示无限制）
     * @param folder OSS文件夹
     * @param entityId 实体ID
     * @return 更新后的图片JSON数组
     */
    public String updateMultipleImages(String currentJson, List<String> imagesToDelete,
            List<MultipartFile> newImageFiles, int maxCount, String folder, Long entityId) throws Exception {

        List<String> images = jsonToImages(currentJson);

        // 删除指定的图片
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            for (String path : imagesToDelete) {
                deleteImage(path);
                images.remove(path);
            }
        }

        // 上传新图片
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            // 检查数量限制
            if (maxCount > 0 && images.size() + newImageFiles.size() > maxCount) {
                throw new IllegalArgumentException(
                    String.format("图片数量不能超过%d张，当前已有%d张，尝试添加%d张",
                    maxCount, images.size(), newImageFiles.size())
                );
            }
            List<String> uploadedPaths = uploadImages(newImageFiles, folder, entityId);
            images.addAll(uploadedPaths);
        }

        return imagesToJson(images);
    }
}
