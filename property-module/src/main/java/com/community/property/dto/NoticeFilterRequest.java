package com.community.property.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公告筛选请求对象
 */
@Data
@Schema(description = "公告筛选请求")
public class NoticeFilterRequest {
    
    @Schema(description = "目标受众")
    private String targetAudience;
    
    @Schema(description = "目标楼栋")
    private String targetBuildings;
    
    @Schema(description = "目标业主类型")
    private String targetOwnerTypes;
    
    @Schema(description = "页码", required = true)
    private Integer page;
    
    @Schema(description = "每页数量", required = true)
    private Integer size;
}

