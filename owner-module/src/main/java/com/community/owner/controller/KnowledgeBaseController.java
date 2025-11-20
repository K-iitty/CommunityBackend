package com.community.owner.controller;

import com.community.owner.domain.dto.response.AIDialogResponse;
import com.community.owner.domain.entity.SmartQaKnowledge;
import com.community.owner.service.RedisMessageService;
import com.community.owner.utils.JwtUtil;
import com.community.owner.domain.entity.Owner;
import com.community.owner.service.OwnerService;
import com.community.owner.service.AIDialogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库查询控制器
 * 为 Spring AI 提供知识库上下文信息
 * 独立于现有的 SmartQaController（已使用 Spring AI 流式输出）
 */
@RestController
@RequestMapping("/api/owner/knowledge-base")
@Tag(name = "业主知识库", description = "知识库查询接口，为AI模型提供上下文信息")
public class KnowledgeBaseController {

    
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseController.class);
    
    @Autowired
    private AIDialogService aiDialogService;
    
    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisMessageService redisMessageService;
    
    /**
     * 查询知识库
     * 返回相关的文档内容，可用作 AI 模型的上下文
     */
    @PostMapping("/query")
    @Operation(summary = "查询知识库", description = "根据问题查询知识库中的相关文档")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> queryKnowledgeBase(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "问题", required = true)
            @RequestParam String question) {
        
        Map<String, Object> resp = new LinkedHashMap<>();
        
        try {
            // 验证用户
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                logger.warn("用户验证失败: token无效");
                resp.put("success", false);
                resp.put("message", "未授权");
                return ResponseEntity.status(401).body(resp);
            }
            
            // 验证参数
            if (question == null || question.trim().isEmpty()) {
                resp.put("success", false);
                resp.put("message", "问题不能为空");
                return ResponseEntity.status(400).body(resp);
            }
            
            logger.info("知识库查询: 用户={}, 问题={}", owner.getId(), question);
            
            // 查询知识库
            AIDialogResponse dialogResponse = aiDialogService.queryKnowledgeBase(
                question, 
                owner.getId()
            );
            
            resp.put("success", true);
            resp.put("data", dialogResponse);
            resp.put("message", "查询成功");
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            logger.error("知识库查询异常", e);
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
    
    /**
     * 获取知识库上下文
     * 用于 AI 模型的提示词构建
     */
    @PostMapping("/context")
    @Operation(summary = "获取知识库上下文", description = "获取用于AI模型的知识库参考信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getKnowledgeBaseContext(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "问题", required = true)
            @RequestParam String question) {
        
        Map<String, Object> resp = new LinkedHashMap<>();
        
        try {
            // 验证用户
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                logger.warn("用户验证失败: token无效");
                resp.put("success", false);
                resp.put("message", "未授权");
                return ResponseEntity.status(401).body(resp);
            }
            
            // 验证参数
            if (question == null || question.trim().isEmpty()) {
                resp.put("success", false);
                resp.put("message", "问题不能为空");
                return ResponseEntity.status(400).body(resp);
            }
            
            // 获取知识库上下文
            String context = aiDialogService.getKnowledgeBaseContext(question);
            
            resp.put("success", true);
            resp.put("data", context);
            resp.put("has_context", !context.isEmpty());
            resp.put("message", "获取成功");
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            logger.error("获取知识库上下文异常", e);
            resp.put("success", false);
            resp.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
    
    /**
     * 检查是否存在知识库匹配
     * 用于前端判断是否应该优先显示知识库结果
     */
    @PostMapping("/has-match")
    @Operation(summary = "检查知识库匹配", description = "检查问题是否在知识库中有匹配")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "检查成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> hasKnowledgeBaseMatch(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "问题", required = true)
            @RequestParam String question) {
        
        Map<String, Object> resp = new LinkedHashMap<>();
        
        try {
            // 验证用户
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                resp.put("success", false);
                resp.put("message", "未授权");
                return ResponseEntity.status(401).body(resp);
            }
            
            // 检查是否存在匹配
            boolean hasMatch = aiDialogService.hasKnowledgeBaseMatch(question);
            
            resp.put("success", true);
            resp.put("has_match", hasMatch);
            resp.put("message", "检查完成");
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            logger.error("检查知识库匹配异常", e);
            resp.put("success", false);
            resp.put("message", "检查失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
    
    /**
     * 获取所有启用的知识库文档
     * 用于后台管理和统计
     */
    @GetMapping("/documents")
    @Operation(summary = "获取知识库文档列表", description = "获取所有启用的知识库文档")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getAllDocuments(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> resp = new LinkedHashMap<>();
        
        try {
            // 验证用户
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                resp.put("success", false);
                resp.put("message", "未授权");
                return ResponseEntity.status(401).body(resp);
            }
            
            // 获取所有文档
            List<SmartQaKnowledge> documents = aiDialogService.getAllEnabledDocuments();
            
            resp.put("success", true);
            resp.put("data", documents);
            resp.put("count", documents.size());
            resp.put("message", "获取成功");
            
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            logger.error("获取知识库文档异常", e);
            resp.put("success", false);
            resp.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
}


