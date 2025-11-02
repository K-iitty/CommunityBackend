package com.community.owner.controller;

import com.community.owner.dto.QaRequest;
import com.community.owner.utils.JwtUtil;
import com.community.owner.entity.Owner;
import com.community.owner.service.OwnerService;
import com.community.owner.service.SmartQaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 智能问答控制器
 * 使用 Spring AI 进行流式输出
 * 知识库查询请见 KnowledgeBaseController
 */
@RestController
@RequestMapping("/api/owner/smart-qa")
@Tag(name = "业主智能问答", description = "AI智能问答接口，支持流式输出和异步输出")
public class SmartQaController {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartQaController.class);
    
    @Autowired
    private SmartQaService smartQaService;
    
    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 智能问答（流式输出）
     * 使用 Spring AI 提供的 LLM 模型进行回答
     * 支持上下文记忆和知识库参考
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "智能问答（流式输出）", description = "输入问题，AI以流式方式逐字返回答案，支持上下文记忆")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "问答成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Flux<ServerSentEvent<String>> chat(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "问答请求", required = true)
            @RequestBody QaRequest request) {
        
        return Flux.defer(() -> {
            try {
                logger.info("收到流式问答请求: {}", request.getQuestion());
                
                // 1. 解析并验证用户信息
                String realToken = token.replace("Bearer ", "");
                String username = jwtUtil.getUsernameFromToken(realToken);
                Owner owner = ownerService.findByUsername(username);
                
                if (owner == null) {
                    logger.warn("用户验证失败: token无效");
                    return Flux.just(
                        ServerSentEvent.<String>builder()
                            .event("error")
                            .data("用户不存在，请重新登录。")
                            .build()
                    );
                }
                
                // 2. 验证请求参数
                if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                    logger.warn("请求参数为空");
                    return Flux.just(
                        ServerSentEvent.<String>builder()
                            .event("error")
                            .data("问题不能为空，请输入您的问题。")
                            .build()
                    );
                }
                
                logger.info("开始为用户 {} (ID: {}) 处理问题", owner.getName(), owner.getId());
                
                // 3. 调用智能问答服务（流式输出）
                return smartQaService.streamChat(request, owner.getId())
                        .filter(chunk -> chunk != null && !chunk.trim().isEmpty())
                        .map(chunk -> ServerSentEvent.<String>builder()
                            .event("message")
                            .data(chunk)
                            .build())
                        .concatWith(Flux.just(ServerSentEvent.<String>builder()
                            .event("done")
                            .data("[DONE]")
                            .build()))
                        .doOnComplete(() -> logger.info("流式输出完成"))
                        .doOnError(e -> logger.error("流式输出错误: {}", e.getMessage(), e))
                        .onErrorResume(e -> {
                            logger.error("智能问答服务异常", e);
                            return Flux.just(
                                ServerSentEvent.<String>builder()
                                    .event("error")
                                    .data("抱歉，问答服务暂时不可用。错误：" + e.getMessage())
                                    .build()
                            );
                        });
                        
            } catch (Exception e) {
                logger.error("智能问答控制器异常", e);
                return Flux.just(
                    ServerSentEvent.<String>builder()
                        .event("error")
                        .data("系统异常，请稍后再试。")
                        .build()
                );
            }
        })
        .timeout(Duration.ofMinutes(5))
        .onErrorResume(e -> {
            logger.error("流式响应最外层异常", e);
            return Flux.just(
                ServerSentEvent.<String>builder()
                    .event("error")
                    .data("服务异常，请稍后再试。")
                    .build()
            );
        });
    }
    
    /**
     * 智能问答（异步/非流式输出）
     * 专门供WeChat小程序使用，避免流式编码问题
     * 直接返回完整的JSON响应
     */
    @PostMapping(value = "/chat-async")
    @Operation(summary = "智能问答（异步输出）", description = "输入问题，AI返回完整答案，适合小程序调用")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "问答成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> chatAsync(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "问答请求", required = true)
            @RequestBody QaRequest request) {
        
        Map<String, Object> resp = new LinkedHashMap<>();
        
        try {
            logger.info("收到异步问答请求: {}", request.getQuestion());
            
            // 1. 解析并验证用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Owner owner = ownerService.findByUsername(username);
            
            if (owner == null) {
                logger.warn("用户验证失败: token无效");
                resp.put("success", false);
                resp.put("message", "用户不存在，请重新登录。");
                return ResponseEntity.status(401).body(resp);
            }
            
            // 2. 验证请求参数
            if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                logger.warn("请求参数为空");
                resp.put("success", false);
                resp.put("message", "问题不能为空，请输入您的问题。");
                return ResponseEntity.status(400).body(resp);
            }
            
            logger.info("开始为用户 {} (ID: {}) 处理异步问题", owner.getName(), owner.getId());
            
            // 3. 调用智能问答服务，异步收集所有流式数据
            CompletableFuture<String> future = new CompletableFuture<>();
            StringBuilder fullResponse = new StringBuilder();
            
            smartQaService.streamChat(request, owner.getId())
                    .filter(chunk -> chunk != null && !chunk.trim().isEmpty())
                    .subscribe(
                        chunk -> {
                            logger.debug("收到流式块: {}", chunk.length());
                            fullResponse.append(chunk);
                        },
                        error -> {
                            logger.error("流式处理错误: {}", error.getMessage(), error);
                            future.completeExceptionally(error);
                        },
                        () -> {
                            logger.info("异步问答处理完成，共{}个字符", fullResponse.length());
                            future.complete(fullResponse.toString());
                        }
                    );
            
            // 等待异步处理完成（最多60秒）
            String answer = future.orTimeout(60, java.util.concurrent.TimeUnit.SECONDS).get();
            
            if (answer == null || answer.isEmpty()) {
                logger.warn("异步问答无响应");
                resp.put("success", false);
                resp.put("message", "无法获取回复，请稍后重试。");
                return ResponseEntity.status(500).body(resp);
            }
            
            resp.put("success", true);
            resp.put("data", new LinkedHashMap<String, Object>() {{
                put("content", answer);
                put("timestamp", System.currentTimeMillis());
            }});
            resp.put("message", "问答成功");
            
            logger.info("异步问答响应成功: 用户={}, 字符数={}", owner.getId(), answer.length());
            return ResponseEntity.ok(resp);
            
        } catch (java.util.concurrent.ExecutionException e) {
            logger.error("异步问答执行异常: {}", e.getMessage(), e);
            // 检查是否是超时异常
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                resp.put("success", false);
                resp.put("message", "请求超时，请稍后重试。");
                return ResponseEntity.status(504).body(resp);
            }
            resp.put("success", false);
            resp.put("message", "系统异常，请稍后再试。");
            return ResponseEntity.status(500).body(resp);
        } catch (Exception e) {
            logger.error("异步问答异常", e);
            resp.put("success", false);
            resp.put("message", "系统异常，请稍后再试。");
            return ResponseEntity.status(500).body(resp);
        }
    }
}

