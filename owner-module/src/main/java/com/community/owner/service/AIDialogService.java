package com.community.owner.service;

import com.community.owner.dto.AIDialogResponse;
import com.community.owner.entity.SmartQaKnowledge;
import com.community.owner.entity.SmartQaSearchHistory;
import com.community.owner.mapper.SmartQaKnowledgeMapper;
import com.community.owner.mapper.SmartQaSearchHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI对话服务 - 知识库查询层
 * 用于查询本地知识库文档，为 Spring AI 提供上下文信息
 * 不修改现有的 SmartQaService（已使用 Spring AI 配置）
 */
@Slf4j
@Service
public class AIDialogService {
    
    @Autowired
    private KnowledgeBaseService kbService;
    
    @Autowired
    private SmartQaKnowledgeMapper kbMapper;
    
    @Autowired
    private SmartQaSearchHistoryMapper historyMapper;
    
    @Autowired
    private DocumentContentService contentService;
    
    @Value("${property.service.phone:029-1234-5678}")
    private String propertyPhone;
    
    /**
     * 从知识库中查询相关文档
     * 用于为 Spring AI 模型提供知识库上下文
     * 
     * @param question 用户问题
     * @param ownerId 业主ID
     * @return 包含知识库信息的响应对象
     */
    public AIDialogResponse queryKnowledgeBase(String question, Long ownerId) {
        AIDialogResponse response = new AIDialogResponse();
        response.setQuestion(question);
        
        try {
            // 在知识库中搜索相关文档
            List<SmartQaKnowledge> docResults = kbService.searchDocuments(question);
            
            if (!docResults.isEmpty()) {
                // 找到相关文档
                SmartQaKnowledge selectedDoc = docResults.get(0);
                
                try {
                    // 提取文档内容
                    String docContent = contentService.extractDocumentContent(selectedDoc);
                    String summary = contentService.getDocumentSummary(docContent, 2000);
                    
                    // 构建响应
                    response.setAnswer(summary);
                    response.setSource("文档知识库");
                    response.setDocId(selectedDoc.getId());
                    response.setDocTitle(selectedDoc.getTitle());
                    response.setDocCategory(selectedDoc.getCategory());
                    response.setType("document");
                    response.setConfidence("high");
                    response.setMessage("知识库中找到相关内容，可用作参考。");
                    
                    // 记录搜索历史
                    saveSearchHistory(ownerId, question, "doc", selectedDoc.getId());
                    
                    // 增加查看次数
                    kbService.increaseViewCount(selectedDoc.getId());
                    
                    log.info("知识库查询成功: 用户={}, 文档={}", ownerId, selectedDoc.getId());
                    
                } catch (Exception e) {
                    log.error("文档内容提取失败", e);
                    response.setType("error");
                    response.setMessage("文档提取失败: " + e.getMessage());
                }
            } else {
                // 知识库中未找到相关文档
                response.setType("not_found");
                response.setMessage("知识库中未找到相关文档");
                response.setConfidence("low");
                
                // 记录搜索历史
                saveSearchHistory(ownerId, question, "none", null);
                
                log.info("知识库查询无结果: 用户={}, 问题={}", ownerId, question);
            }
            
        } catch (Exception e) {
            log.error("知识库查询异常", e);
            response.setType("error");
            response.setMessage("查询异常: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取知识库信息用于 AI 提示词
     * Spring AI 可以利用这些信息进行更准确的回答
     * 
     * @param question 用户问题
     * @return 知识库上下文字符串
     */
    public String getKnowledgeBaseContext(String question) {
        try {
            List<SmartQaKnowledge> docs = kbService.searchDocuments(question);
            
            if (docs.isEmpty()) {
                return "";  // 没有相关文档
            }
            
            StringBuilder context = new StringBuilder();
            context.append("【知识库参考信息】\n");
            
            // 最多包含前3个相关文档的摘要
            for (int i = 0; i < Math.min(3, docs.size()); i++) {
                SmartQaKnowledge doc = docs.get(i);
                context.append(String.format("文档%d: %s (%s)\n", 
                    i + 1, doc.getTitle(), doc.getCategory()));
                context.append(String.format("摘要: %s\n\n", doc.getDescription()));
            }
            
            return context.toString();
            
        } catch (Exception e) {
            log.warn("获取知识库上下文失败", e);
            return "";
        }
    }
    
    /**
     * 检查是否存在知识库匹配
     * 用于判断是否应该优先使用知识库信息
     * 
     * @param question 用户问题
     * @return 是否找到匹配的文档
     */
    public boolean hasKnowledgeBaseMatch(String question) {
        try {
            List<SmartQaKnowledge> docs = kbService.searchDocuments(question);
            return !docs.isEmpty();
        } catch (Exception e) {
            log.warn("检查知识库匹配失败", e);
            return false;
        }
    }
    
    /**
     * 获取知识库中的所有启用文档
     * 用于后台统计和管理
     * 
     * @return 启用的文档列表
     */
    public List<SmartQaKnowledge> getAllEnabledDocuments() {
        try {
            return kbService.searchDocuments("");  // 查询所有启用的文档
        } catch (Exception e) {
            log.error("获取所有文档失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 保存搜索历史
     */
    private void saveSearchHistory(Long ownerId, String query, String queryType, Long docId) {
        try {
            SmartQaSearchHistory history = new SmartQaSearchHistory();
            history.setOwnerId(ownerId);
            history.setQuery(query);
            history.setQueryType(queryType);
            history.setDocId(docId);
            historyMapper.insert(history);
        } catch (Exception e) {
            log.warn("保存搜索历史失败", e);
        }
    }
}
