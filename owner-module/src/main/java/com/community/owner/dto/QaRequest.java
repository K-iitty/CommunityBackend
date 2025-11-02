package com.community.owner.dto;

import lombok.Data;
import java.util.List;

/**
 * 智能问答请求对象
 */
@Data
public class QaRequest {
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 会话ID（用于保持上下文）
     */
    private String sessionId;
    
    /**
     * 历史对话记录（用于上下文记忆）
     */
    private List<ChatMessage> history;
    
    @Data
    public static class ChatMessage {
        /**
         * 角色：user/assistant
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
    }
}

