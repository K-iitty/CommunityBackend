package com.community.owner.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.owner.domain.entity.SmartQaKnowledge;
import com.community.owner.mapper.SmartQaKnowledgeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class KnowledgeBaseService {
    
    @Autowired
    private SmartQaKnowledgeMapper kbMapper;
    
    @Autowired
    private DocumentContentService contentService;
    
    /**
     * 在知识库中搜索相关文档
     */
    public List<SmartQaKnowledge> searchDocuments(String question) {
        // 提取关键词
        List<String> keywords = extractKeywords(question);
        
        if (keywords.isEmpty()) {
            // 如果没有提取到关键词，返回所有启用的文档
            return kbMapper.selectList(
                new QueryWrapper<SmartQaKnowledge>()
                    .eq("status", "启用")
                    .orderByAsc("sort_order")
                    .orderByDesc("view_count")
            );
        }
        
        // 1. 精确搜索：按标签和标题
        List<SmartQaKnowledge> exactMatches = new ArrayList<>();
        
        for (String keyword : keywords) {
            List<SmartQaKnowledge> results = kbMapper.selectList(
                new QueryWrapper<SmartQaKnowledge>()
                    .eq("status", "启用")
                    .and(w -> w.like("tags", keyword)
                        .or().like("title", keyword)
                        .or().like("description", keyword))
                    .orderByAsc("sort_order")
                    .orderByDesc("view_count")
            );
            exactMatches.addAll(results);
        }
        
        // 去重
        return new ArrayList<>(exactMatches.stream()
            .collect(java.util.stream.Collectors.toMap(
                SmartQaKnowledge::getId,
                k -> k,
                (k1, k2) -> k1
            )).values());
    }
    
    /**
     * 提取问题中的关键词
     */
    private List<String> extractKeywords(String question) {
        List<String> keywords = new ArrayList<>();
        
        // 常见关键词
        String[] commonKeywords = {
            "停车", "费用", "物业", "报修", "投诉", 
            "缴费", "车辆", "房屋", "通知", "账户",
            "打折", "优惠", "如何", "怎么", "申请",
            "办理", "流程", "规定", "政策", "制度",
            "服务", "管理", "规则", "收费", "缴纳"
        };
        
        for (String keyword : commonKeywords) {
            if (question.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }
    
    /**
     * 增加文档查看次数
     */
    public void increaseViewCount(Long docId) {
        SmartQaKnowledge doc = kbMapper.selectById(docId);
        if (doc != null) {
            doc.setViewCount((doc.getViewCount() == null ? 0 : doc.getViewCount()) + 1);
            kbMapper.updateById(doc);
        }
    }
}


