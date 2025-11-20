package com.community.owner.controller;

import com.community.owner.service.RedisMessageService;
import com.community.owner.utils.JwtUtil;
import com.community.owner.domain.entity.Owner;
import com.community.owner.service.OwnerService;
import com.community.owner.service.OwnerQueryService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/owner")
@Tag(name = "业主信息管理", description = "业主个人信息查看与修改接口")
public class OwnerController {
    
    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OwnerQueryService ownerQueryService;

    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Autowired
    private RedisMessageService redisMessageService;
    
    /**
     * 查看个人信息
     */
    @GetMapping("/profile")
    @Operation(summary = "查看个人信息", description = "业主查看自己的个人信息")
    public Map<String, Object> getProfile(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            
            Owner owner = ownerService.findByUsername(username);
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            response.put("success", true);
            response.put("data", owner);
            response.put("message", "获取成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 分页查询当前登录业主的房屋信息
     */
    @GetMapping("/houses")
    @Operation(summary = "分页查询我的房屋", description = "查询当前登录业主绑定的房屋列表")
    public Map<String, Object> listMyHouses(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "页码(从1开始)") @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);

            Owner owner = ownerService.findByUsername(username);
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }

            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;
            int offset = (page - 1) * size;

            // 使用OwnerQueryService进行多表JOIN查询
            Long total = ownerQueryService.countOwnerHouses(owner.getId());
            List<Map<String, Object>> items = ownerQueryService.listOwnerHousesWithDetails(
                    owner.getId(), size, offset);

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", items);

            response.put("success", true);
            response.put("data", pageData);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 修改个人信息
     */
    @PutMapping("/profile")
    @Operation(summary = "修改个人信息", description = "业主修改自己的个人信息，但不能修改认证状态")
    public Map<String, Object> updateProfile(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "更新的业主信息", required = true)
            @RequestBody Owner updatedOwner) {
        Map<String, Object> response = new HashMap<>();
        try {
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            
            Owner owner = ownerService.findByUsername(username);
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            updatedOwner.setId(owner.getId());
            updatedOwner.setUsername(owner.getUsername());
            updatedOwner.setPassword(owner.getPassword());
            updatedOwner.setVerifyStatus(owner.getVerifyStatus());
            
            boolean updated = ownerService.updateById(updatedOwner);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "修改成功");
            } else {
                response.put("success", false);
                response.put("message", "修改失败");
            }
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "修改失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 上传照片到OSS
     */
    @PostMapping("/upload-photo")
    @Operation(summary = "上传照片", description = "上传照片到OSS，返回照片路径")
    public Map<String, Object> uploadPhoto(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "照片文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "照片类型: avatar/idCard", required = false)
            @RequestParam(value = "type", required = false, defaultValue = "avatar") String type) {
        Map<String, Object> response = new HashMap<>();
        try {
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            
            Owner owner = ownerService.findByUsername(username);
            if (owner == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }

            // 验证文件
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件为空");
                return response;
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "仅支持图片文件");
                return response;
            }

            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String objectKey = String.format("owner/%d/%s/%s%s", 
                    owner.getId(), type, UUID.randomUUID().toString(), fileExtension);

            // 上传到OSS
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream);
                PutObjectResult result = ossClient.putObject(putObjectRequest);
            }

            // 返回OSS路径
            String ossPath = objectKey;

            response.put("success", true);
            response.put("data", new HashMap<String, Object>() {{
                put("path", ossPath);
                put("url", endpoint.replaceFirst("https?://", "https://") + "/" + bucketName + "/" + ossPath);
            }});
            response.put("message", "上传成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            e.printStackTrace();
            return response;
        }
    }
}