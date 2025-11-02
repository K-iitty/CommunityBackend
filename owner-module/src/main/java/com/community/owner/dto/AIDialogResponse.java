package com.community.owner.dto;

import lombok.Data;
import java.util.List;

@Data
public class AIDialogResponse {
    
    /**
     * 原始问题
     */
    private String question;
    
    /**
     * 回答内容（文档内容或提示）
     */
    private String answer;
    
    /**
     * 数据来源：文档知识库/网络搜索/人工
     */
    private String source;
    
    /**
     * 响应类型：document(文档)/recommendation(推荐)/no_result(无结果)
     */
    private String type;
    
    /**
     * 提示信息
     */
    private String message;
    
    /**
     * 可信度：high/medium/low
     */
    private String confidence;
    
    /**
     * 文档ID
     */
    private Long docId;
    
    /**
     * 文档标题
     */
    private String docTitle;
    
    /**
     * 文档分类
     */
    private String docCategory;
    
    /**
     * 网络搜索推荐结果列表
     */
    private List<SearchRecommendation> recommendations;
    
    /**
     * 物业电话
     */
    private String propertyPhone;
    
    /**
     * 交互操作：redirect_to_contact(跳转联系物业)等
     */
    private String propertyAction;
    
    @Data
    public static class SearchRecommendation {
        /**
         * 标题
         */
        private String title;
        
        /**
         * URL
         */
        private String url;
        
        /**
         * 摘要
         */
        private String snippet;
        
        /**
         * 来源
         */
        private String source;
        
        /**
         * 是否为推荐
         */
        private Boolean isRecommendation;
    }
}


