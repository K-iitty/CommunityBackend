package com.community.property.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 智能问答请求对象
 */
@Data
@Schema(description = "智能问答请求")
public class QaRequest {
    
    @Schema(description = "问题内容", required = true)
    private String question;
    
    @Schema(description = "对话历史")
    private List<ChatMessage> history;
    
    /**
     * 对话消息
     */
    @Data
    @Schema(description = "对话消息")
    public static class ChatMessage {
        @Schema(description = "角色:user/assistant")
        private String role;
        
        @Schema(description = "消息内容")
        private String content;
    }
}

