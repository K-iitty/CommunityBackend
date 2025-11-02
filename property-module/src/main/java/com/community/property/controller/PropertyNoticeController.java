package com.community.property.controller;

import com.community.property.utils.JwtUtil;
import com.community.property.entity.Staff;
import com.community.property.service.StaffService;
import com.community.property.service.CommunityNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.community.property.entity.CommunityNotice;

/**
 * 物业公告查看控制器
 * 与业主公告接口功能一致，供物业员工查看社区公告
 */
@RestController
@RequestMapping("/api/property/notice")
@Tag(name = "物业公告管理", description = "物业员工查看社区公告相关接口")
public class PropertyNoticeController {
    
    @Autowired
    private StaffService staffService;
    
    @Autowired
    private CommunityNoticeService communityNoticeService;

    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取公告列表
     */
    @GetMapping("/notices")
    @Operation(summary = "获取公告列表", description = "分页获取社区公告列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> listNotices(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "页码(从1开始)") 
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") 
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @Parameter(description = "公告类型") 
            @RequestParam(value = "categoryName", required = false) String categoryName,
            @Parameter(description = "关键词搜索") 
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 获取公告列表（物业可查看所有社区的公告）
            // 使用categoryName作为noticeType参数
            Map<String, Object> pageData = communityNoticeService.listNotices(null, categoryName, page, size);
            
            // 如果有keyword，则过滤结果
            if (keyword != null && !keyword.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) pageData.get("list");
                if (list != null) {
                    list.removeIf(notice -> {
                        String title = (String) notice.get("title");
                        return title == null || !title.toLowerCase().contains(keyword.toLowerCase());
                    });
                    pageData.put("list", list);
                    pageData.put("total", list.size());
                }
            }
            
            // 将list重命名为items以匹配前端期望
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) pageData.get("list");
            pageData.remove("list");
            pageData.put("items", list);
            
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
     * 获取公告详情
     */
    @GetMapping("/detail/{noticeId}")
    @Operation(summary = "获取公告详情", description = "获取指定公告的详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "公告不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getNoticeDetail(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "公告ID", required = true)
            @PathVariable("noticeId") Long noticeId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 获取公告详情
            Map<String, Object> notice = communityNoticeService.getNoticeDetail(noticeId);
            
            if (notice == null) {
                response.put("success", false);
                response.put("message", "公告不存在");
                return response;
            }
            
            response.put("success", true);
            response.put("data", notice);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 增加公告阅读计数
     */
    @PostMapping("/{noticeId}/read")
    @Operation(summary = "增加阅读计数", description = "增加公告的阅读次数")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> incrementReadCount(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "公告ID", required = true)
            @PathVariable("noticeId") Long noticeId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 增加阅读计数
            communityNoticeService.incrementReadCount(noticeId);
            
            response.put("success", true);
            response.put("message", "阅读计数已增加");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "增加阅读计数失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 新增公告（支持完整字段和图片上传）
     */
    @PostMapping("/add")
    @Operation(summary = "新增公告", description = "发布新的社区公告，支持图片上传")
    public Map<String, Object> addNotice(
            @Parameter(description = "社区ID", required = true)
            @RequestParam Long communityId,
            @Parameter(description = "公告标题", required = true)
            @RequestParam String title,
            @Parameter(description = "公告内容", required = true)
            @RequestParam String content,
            @Parameter(description = "公告类型", required = true)
            @RequestParam String noticeType,
            @Parameter(description = "生效时间", required = true)
            @RequestParam String startTime,
            @Parameter(description = "失效时间", required = true)
            @RequestParam String endTime,
            @Parameter(description = "活动日期", required = false)
            @RequestParam(required = false) String activityDate,
            @Parameter(description = "活动时间", required = false)
            @RequestParam(required = false) String activityTime,
            @Parameter(description = "活动地点", required = false)
            @RequestParam(required = false) String activityLocation,
            @Parameter(description = "活动组织者", required = false)
            @RequestParam(required = false) String activityOrganizer,
            @Parameter(description = "活动联系人", required = false)
            @RequestParam(required = false) String activityContact,
            @Parameter(description = "活动联系电话", required = false)
            @RequestParam(required = false) String activityContactPhone,
            @Parameter(description = "目标受众", required = false)
            @RequestParam(required = false, defaultValue = "全体业主") String targetAudience,
            @Parameter(description = "是否紧急", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer isUrgent,
            @Parameter(description = "是否置顶", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer isTop,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "公告图片文件列表", required = false)
            @RequestParam(value = "noticeImageFiles", required = false) MultipartFile[] noticeImageFiles,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return communityNoticeService.addNoticeWithImages(
                    communityId, title, content, noticeType, activityDate, activityTime,
                    activityLocation, activityOrganizer, activityContact, activityContactPhone,
                    targetAudience, isUrgent, isTop, startTime, endTime, remark,
                    noticeImageFiles);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "新增公告失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 新增公告（通过JSON请求体）
     */
    @PostMapping("/add-json")
    @Operation(summary = "新增公告（JSON方式）", description = "通过JSON请求体发布新的社区公告")
    public Map<String, Object> addNoticeJson(
            @RequestBody Map<String, Object> requestData,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 从请求体中提取参数
            Object communityIdObj = requestData.get("communityId");
            Long communityId = communityIdObj != null ? ((Number) communityIdObj).longValue() : null;
            
            String title = (String) requestData.get("title");
            String content = (String) requestData.get("content");
            String noticeType = (String) requestData.get("noticeType");
            String startTime = (String) requestData.get("startTime");
            String endTime = (String) requestData.get("endTime");
            String activityDate = (String) requestData.get("activityDate");
            String activityTime = (String) requestData.get("activityTime");
            String activityLocation = (String) requestData.get("activityLocation");
            String activityOrganizer = (String) requestData.get("activityOrganizer");
            String activityContact = (String) requestData.get("activityContact");
            String activityContactPhone = (String) requestData.get("activityContactPhone");
            String targetAudience = (String) requestData.get("targetAudience");
            Object isUrgentObj = requestData.get("isUrgent");
            Object isTopObj = requestData.get("isTop");
            String remark = (String) requestData.get("remark");
            
            // 提取imageUrls（图片URL列表）
            @SuppressWarnings("unchecked")
            java.util.List<String> imageUrls = (java.util.List<String>) requestData.get("imageUrls");
            
            Integer isUrgent = isUrgentObj != null ? ((Number) isUrgentObj).intValue() : 0;
            Integer isTop = isTopObj != null ? ((Number) isTopObj).intValue() : 0;
            
            // 调用service创建公告
            Map<String, Object> result = communityNoticeService.addNoticeWithImages(
                    communityId, title, content, noticeType, activityDate, activityTime,
                    activityLocation, activityOrganizer, activityContact, activityContactPhone,
                    targetAudience != null ? targetAudience : "全体业主", isUrgent, isTop, startTime, endTime, remark,
                    null);
            
            // 如果创建成功且有图片URL，更新图片字段
            if (result != null && (boolean) result.get("success") && imageUrls != null && !imageUrls.isEmpty()) {
                try {
                    Object noticeDataObj = result.get("data");
                    if (noticeDataObj != null) {
                        Long noticeId = null;
                        
                        // data可能是CommunityNotice对象或Map
                        if (noticeDataObj instanceof com.community.property.entity.CommunityNotice) {
                            CommunityNotice notice = (com.community.property.entity.CommunityNotice) noticeDataObj;
                            noticeId = notice.getId();
                        } else if (noticeDataObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> noticeData = (Map<String, Object>) noticeDataObj;
                            if (noticeData.containsKey("id")) {
                                noticeId = ((Number) noticeData.get("id")).longValue();
                            }
                        }
                        
                        if (noticeId != null) {
                            // 仅保存第一张图片URL
                            String imageUrl = imageUrls.get(0);
                            communityNoticeService.updateNoticeImages(noticeId, imageUrl);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("保存公告图片失败: " + e.getMessage());
                    e.printStackTrace();
                    // 不影响公告创建的成功状态
                }
            }
            
            return result;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "新增公告失败: " + e.getMessage());
            e.printStackTrace();
            return response;
        }
    }

    /**
     * 更新公告（包含图片上传）
     */
    @PostMapping("/{noticeId}/update-with-images")
    @Operation(summary = "更新公告（包含图片上传）", description = "更新公告内容并支持上传或删除公告图片")
    public Map<String, Object> updateNoticeWithImages(
            @Parameter(description = "公告ID", required = true)
            @PathVariable Long noticeId,
            @Parameter(description = "公告标题", required = false)
            @RequestParam(required = false) String title,
            @Parameter(description = "公告内容", required = false)
            @RequestParam(required = false) String content,
            @Parameter(description = "公告类型", required = false)
            @RequestParam(required = false) String noticeType,
            @Parameter(description = "活动日期", required = false)
            @RequestParam(required = false) String activityDate,
            @Parameter(description = "活动时间", required = false)
            @RequestParam(required = false) String activityTime,
            @Parameter(description = "活动地点", required = false)
            @RequestParam(required = false) String activityLocation,
            @Parameter(description = "是否紧急", required = false)
            @RequestParam(required = false) Integer isUrgent,
            @Parameter(description = "是否置顶", required = false)
            @RequestParam(required = false) Integer isTop,
            @Parameter(description = "失效时间", required = false)
            @RequestParam(required = false) String endTime,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "公告图片文件列表", required = false)
            @RequestParam(value = "noticeImageFiles", required = false) MultipartFile[] noticeImageFiles,
            @Parameter(description = "要删除的公告图片JSON数组", required = false)
            @RequestParam(required = false) String noticeImagesToDelete,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return communityNoticeService.updateNoticeWithImages(
                    noticeId, title, content, noticeType, activityDate, activityTime,
                    activityLocation, isUrgent, isTop, endTime, remark,
                    noticeImageFiles, noticeImagesToDelete);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 更新公告（通过JSON请求体）
     */
    @PostMapping("/{noticeId}/update")
    @Operation(summary = "更新公告（JSON方式）", description = "通过JSON请求体更新公告内容")
    public Map<String, Object> updateNotice(
            @Parameter(description = "公告ID", required = true)
            @PathVariable Long noticeId,
            @RequestBody Map<String, Object> requestData,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 从请求体中提取参数
            String title = (String) requestData.get("title");
            String content = (String) requestData.get("content");
            String noticeType = (String) requestData.get("noticeType");
            String activityDate = (String) requestData.get("activityDate");
            String activityTime = (String) requestData.get("activityTime");
            String activityLocation = (String) requestData.get("activityLocation");
            String activityOrganizer = (String) requestData.get("activityOrganizer");
            String activityContact = (String) requestData.get("activityContact");
            String activityContactPhone = (String) requestData.get("activityContactPhone");
            Object isUrgentObj = requestData.get("isUrgent");
            Object isTopObj = requestData.get("isTop");
            String endTime = (String) requestData.get("endTime");
            String remark = (String) requestData.get("remark");
            
            Integer isUrgent = isUrgentObj != null ? ((Number) isUrgentObj).intValue() : null;
            Integer isTop = isTopObj != null ? ((Number) isTopObj).intValue() : null;
            
            // 调用服务更新（不包含图片）
            return communityNoticeService.updateNoticeWithImages(
                    noticeId, title, content, noticeType, activityDate, activityTime,
                    activityLocation, isUrgent, isTop, endTime, remark,
                    null, null);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除公告
     */
    @PostMapping("/{noticeId}/delete")
    @Operation(summary = "删除公告", description = "删除指定的社区公告及其关联的图片")
    public Map<String, Object> deleteNotice(
            @Parameter(description = "公告ID", required = true)
            @PathVariable Long noticeId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 删除公告
            communityNoticeService.deleteNotice(noticeId);
            
            response.put("success", true);
            response.put("message", "公告删除成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }
}

