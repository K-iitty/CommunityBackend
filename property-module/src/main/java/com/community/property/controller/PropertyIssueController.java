package com.community.property.controller;

import com.community.property.dto.IssueDetailVO;
import com.community.property.dto.IssueFollowUpRequest;
import com.community.property.service.PropertyIssueService;
import com.community.property.service.ImageService;
import com.community.property.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物业反馈处理控制器
 */
@RestController
@RequestMapping("/api/property/issues")
@Tag(name = "物业反馈处理", description = "物业端的反馈处理、分配、跟进等接口")
public class PropertyIssueController {

    @Autowired
    private PropertyIssueService propertyIssueService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 查询所有已分配的问题
     */
    @GetMapping("/all")
    @Operation(summary = "查询所有已分配的问题", description = "物业端查询所有已分配的问题列表")
    public Map<String, Object> getAllIssues(
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "问题状态过滤", required = false)
            @RequestParam(required = false) String status,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = propertyIssueService.listAllIssues(page, size, status);
            response.put("success", true);
            response.put("data", data);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }


    /**
     * 获取问题统计数据
     */
    @GetMapping("/statistics/summary")
    @Operation(summary = "获取问题统计数据", description = "获取各状态问题的统计数量")
    public Map<String, Object> getIssueStatistics(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return propertyIssueService.getIssueStatistics();
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取问题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取问题详情", description = "查询特定问题的详细信息")
    public Map<String, Object> getIssueDetail(
            @Parameter(description = "问题ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            IssueDetailVO detail = propertyIssueService.getIssueDetail(id);
            if (detail == null) {
                response.put("success", false);
                response.put("message", "问题不存在");
                return response;
            }
            response.put("success", true);
            response.put("data", detail);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 开始处理问题
     */
    @PostMapping("/{id}/start-processing")
    @Operation(summary = "开始处理问题", description = "员工开始处理问题，状态变更为处理中")
    public Map<String, Object> startProcessing(
            @Parameter(description = "问题ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "处理方案", required = true)
            @RequestParam String planDescription,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        String realToken = token.replace("Bearer ", "");
        // 从Token获取员工ID（需要在JWT中包含staffId）
        Long staffId = jwtUtil.getStaffIdFromToken(realToken);

        return propertyIssueService.startProcessing(id, staffId, planDescription);
    }

    /**
     * 提交处理结果（支持multipart文件上传）
     */
    @PostMapping("/{id}/submit-result-with-images")
    @Operation(summary = "提交处理结果（包含图片上传）", description = "员工提交问题处理结果，支持上传处理过程和结果图片")
    public Map<String, Object> submitProcessResultWithImages(
            @Parameter(description = "问题ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "处理方案", required = false)
            @RequestParam(required = false) String planDescription,
            @Parameter(description = "处理结果描述", required = true)
            @RequestParam String resultDescription,
            @Parameter(description = "处理过程图片（多张）", required = false)
            @RequestParam(value = "processImages", required = false) MultipartFile[] processImages,
            @Parameter(description = "处理结果图片（多张）", required = false)
            @RequestParam(value = "resultImages", required = false) MultipartFile[] resultImages,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            String realToken = token.replace("Bearer ", "");
            Long staffId = jwtUtil.getStaffIdFromToken(realToken);

            // 上传处理过程图片
            String processImagesJson = null;
            if (processImages != null && processImages.length > 0) {
                List<String> paths = imageService.uploadImages(Arrays.asList(processImages), "issue/process", id);
                processImagesJson = imageService.imagesToJson(paths);
            }

            // 上传处理结果图片
            String resultImagesJson = null;
            if (resultImages != null && resultImages.length > 0) {
                List<String> paths = imageService.uploadImages(Arrays.asList(resultImages), "issue/result", id);
                resultImagesJson = imageService.imagesToJson(paths);
            }

            // 调用Service提交处理结果
            return propertyIssueService.submitProcessResultWithImages(id, staffId, resultDescription, 
                processImagesJson, resultImagesJson, planDescription);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交失败: " + e.getMessage());
            return response;
        }
    }


    /**
     * 标记为已解决
     */
    @PostMapping("/{id}/mark-resolved")
    @Operation(summary = "标记问题为已解决", description = "物业处理完成后标记问题为已解决")
    public Map<String, Object> markAsResolved(
            @Parameter(description = "问题ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        String realToken = token.replace("Bearer ", "");
        Long staffId = jwtUtil.getStaffIdFromToken(realToken);

        return propertyIssueService.markAsResolved(id, staffId);
    }

    /**
     * 添加追加记录
     */
    @PostMapping("/{id}/follow-up")
    @Operation(summary = "添加追加记录", description = "为问题添加跟进记录")
    public Map<String, Object> addFollowUp(
            @Parameter(description = "问题ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "跟进记录信息", required = true)
            @RequestBody IssueFollowUpRequest request,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            String realToken = token.replace("Bearer ", "");
            Long staffId = jwtUtil.getStaffIdFromToken(realToken);
            String staffName = "物业员工";  // 从Token获取员工名称，这里先用默认值

            // 调用Service添加跟进记录
            return propertyIssueService.addFollowUp(id, request, staffId, staffName);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加追加记录失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 查询问题的追加记录列表
     */
    @GetMapping("/{id}/follow-ups")
    @Operation(summary = "查询追加记录列表", description = "查询特定问题的所有追加记录")
    public Map<String, Object> getFollowUps(
            @Parameter(description = "问题ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "100") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用Service查询追加记录
            return propertyIssueService.getIssueFollowUps(id, page, size);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询追加记录失败: " + e.getMessage());
            return response;
        }
    }
}
