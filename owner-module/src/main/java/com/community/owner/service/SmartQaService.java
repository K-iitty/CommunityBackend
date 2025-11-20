package com.community.owner.service;

import com.community.owner.domain.dto.request.QaRequest;
import reactor.core.publisher.Flux;

/**
 * 智能问答服务接口
 */
public interface SmartQaService {
    
    /**
     * 流式问答（支持上下文记忆）
     * @param request 问答请求
     * @param ownerId 业主ID
     * @return 流式响应
     */
    Flux<String> streamChat(QaRequest request, Long ownerId);
}

