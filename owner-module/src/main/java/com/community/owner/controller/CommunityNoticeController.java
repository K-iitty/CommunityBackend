package com.community.owner.controller;

import com.community.owner.dto.*;
import com.community.owner.dto.NoticeDetailVO;
import com.community.owner.dto.NoticeFilterRequest;
import com.community.owner.dto.NoticeSearchRequest;
import com.community.owner.utils.JwtUtil;
import com.community.owner.entity.Owner;
import com.community.owner.service.CommunityNoticeService;
import com.community.owner.service.OwnerService;
import com.community.owner.service.OwnerQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 社区公告控制器
 */
@RestController
@RequestMapping("/api/owner/notices")
@Tag(name = "社区公告管理", description = "业主查看社区公告相关接口")
public class CommunityNoticeController {
    
    @Autowired
    private CommunityNoticeService communityNoticeService;
    
    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OwnerQueryService ownerQueryService;
    
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    
    /**
     * 将OSS路径转换为完整URL
     */
    private String getImageUrl(String osspath) {
        if (osspath == null || osspath.isEmpty()) {
            return null;
        }
        
        // 如果已经是完整URL，直接返回
        if (osspath.startsWith("http://") || osspath.startsWith("https://")) {
            return osspath;
        }
        
        // 构建OSS完整URL
        // 从endpoint中提取域名（如：oss-cn-beijing.aliyuncs.com）
        String domain = endpoint.replace("https://", "").replace("http://", "");
        String url = String.format("https://%s.%s/%s", bucketName, domain, osspath);
        return url;
    }
    
    /**
     * 分页显示公告列表（显示标题和图片）
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询公告列表", description = "查询公告列表，显示标题和图片，按发布时间倒序排列，置顶公告优先")
    public Map<String, Object> listNotices(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "页码(从1开始)") 
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") 
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("========== 开始查询首页公告 ==========");
            System.out.println("请求参数 - page: " + page + ", size: " + size);
            
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            System.out.println("用户名: " + username);
            
            Owner owner = ownerService.findByUsername(username);
            if (owner == null) {
                System.out.println("错误：用户不存在");
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            System.out.println("业主ID: " + owner.getId());

            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;
            int offset = (page - 1) * size;
            System.out.println("计算得出 offset: " + offset);
            
            // 使用OwnerQueryService进行多表JOIN查询，自动获取业主所属社区的公告
            Long total = ownerQueryService.countOwnerCommunityNotices(owner.getId());
            System.out.println("公告总数: " + total);
            
            List<Map<String, Object>> rawItems = ownerQueryService.listOwnerCommunityNoticesWithDetails(
                    owner.getId(), size, offset);
            System.out.println("查询得到的原始数据数量: " + (rawItems != null ? rawItems.size() : 0));
            if (rawItems != null && !rawItems.isEmpty()) {
                System.out.println("第一条原始数据: " + rawItems.get(0));
            }
            
            // 将原始Map转换为NoticeListVO格式（蛇形命名转驼峰命名）
            List<NoticeListVO> items = new ArrayList<>();
            for (Map<String, Object> raw : rawItems) {
                NoticeListVO vo = new NoticeListVO();
                vo.setId(raw.get("id") != null ? ((Number) raw.get("id")).longValue() : null);
                vo.setTitle((String) raw.get("title"));
                vo.setContent((String) raw.get("content"));
                vo.setNoticeImages(getImageUrl((String) raw.get("notice_images")));
                vo.setNoticeType((String) raw.get("notice_type"));
                // is_urgent 字段可能不存在，需要处理
                if (raw.get("is_urgent") != null) {
                    vo.setIsUrgent(((Number) raw.get("is_urgent")).intValue());
                }
                if (raw.get("is_top") != null) {
                    vo.setIsTop(((Number) raw.get("is_top")).intValue());
                }
                // 处理 publish_time 时间戳转 LocalDateTime
                if (raw.get("publish_time") != null) {
                    Object timeObj = raw.get("publish_time");
                    if (timeObj instanceof java.sql.Timestamp) {
                        vo.setPublishTime(((java.sql.Timestamp) timeObj).toLocalDateTime());
                    } else if (timeObj instanceof Long) {
                        vo.setPublishTime(new java.sql.Timestamp((Long) timeObj).toLocalDateTime());
                    }
                }
                if (raw.get("read_count") != null) {
                    vo.setReadCount(((Number) raw.get("read_count")).intValue());
                }
                items.add(vo);
            }
            System.out.println("转换后的VO数据数量: " + items.size());
            if (!items.isEmpty()) {
                System.out.println("第一条VO数据: " + items.get(0));
            }
            
            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", items);

            response.put("success", true);
            response.put("data", pageData);
            response.put("message", "查询成功");
            System.out.println("========== 首页公告查询完成，返回成功 ==========");
            return response;
        } catch (Exception e) {
            System.out.println("========== 首页公告查询出错 ==========");
            System.out.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 查看公告详情
     */
    @GetMapping("/detail/{noticeId}")
    @Operation(summary = "查看公告详情", description = "查看公告的详细信息，包括标题、内容、图片、活动信息等，并增加阅读次数")
    public Map<String, Object> getNoticeDetail(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "公告ID", required = true)
            @PathVariable("noticeId") Long noticeId) {
        
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
            
            NoticeDetailVO detailVO = communityNoticeService.getNoticeDetail(noticeId);
            
            if (detailVO == null) {
                response.put("success", false);
                response.put("message", "公告不存在");
                return response;
            }
            
            response.put("success", true);
            response.put("data", detailVO);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 搜索公告
     */
    @PostMapping("/search")
    @Operation(summary = "搜索公告", description = "根据关键词模糊查询公告的标题、内容、公告类型")
    public Map<String, Object> searchNotices(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "搜索请求参数", required = true)
            @RequestBody NoticeSearchRequest request) {
        
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
            
            Map<String, Object> pageData = communityNoticeService.searchNotices(1L, request);
            
            response.put("success", true);
            response.put("data", pageData);
            response.put("message", "搜索成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 筛选公告 - 按公告分类进行筛选（仅显示目标受众为全体业主的公告）
     */
    @PostMapping("/filter")
    @Operation(summary = "筛选公告", description = "按公告分类(notice_type)筛选公告，仅显示目标受众为全体业主的公告，按发布时间倒序排列")
    public Map<String, Object> filterNotices(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "筛选请求参数", required = true)
            @RequestBody NoticeFilterRequest request) {
        
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
            
            Map<String, Object> pageData = communityNoticeService.filterNoticesByAudience(1L, request);
            
            response.put("success", true);
            response.put("data", pageData);
            response.put("message", "筛选成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "筛选失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 获取公告分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取公告分类列表", description = "获取所有可用的公告分类")
    public Map<String, Object> getNoticeCategories(
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
            
            // 返回公告分类列表
            List<Map<String, String>> categories = new ArrayList<>();
            categories.add(new HashMap<String, String>() {{
                put("value", "");
                put("label", "全部");
            }});
            categories.add(new HashMap<String, String>() {{
                put("value", "社区公告");
                put("label", "社区公告");
            }});
            categories.add(new HashMap<String, String>() {{
                put("value", "活动公告");
                put("label", "活动公告");
            }});
            categories.add(new HashMap<String, String>() {{
                put("value", "紧急通知");
                put("label", "紧急通知");
            }});
            categories.add(new HashMap<String, String>() {{
                put("value", "温馨提示");
                put("label", "温馨提示");
            }});
            
            response.put("success", true);
            response.put("data", categories);
            response.put("message", "获取分类成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取分类失败: " + e.getMessage());
            return response;
        }
    }
}

