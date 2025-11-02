package com.community.property.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题提交请求对象
 */
@Data
@Schema(description = "问题提交请求")
public class IssueSubmitRequest {
    
    @Schema(description = "问题标题", required = true)
    private String issueTitle;
    
    @Schema(description = "问题详情描述", required = true)
    private String issueContent;
    
    @Schema(description = "问题类型:报修服务/投诉建议/咨询服务/其他", required = true)
    private String issueType;
    
    @Schema(description = "问题子类型")
    private String subType;
    
    @Schema(description = "位置类型:室内/公共区域/停车场/其他")
    private String locationType;
    
    @Schema(description = "具体位置")
    private String specificLocation;
    
    @Schema(description = "联系人姓名")
    private String contactName;
    
    @Schema(description = "联系电话", required = true)
    private String contactPhone;
    
    @Schema(description = "最佳联系时间")
    private String bestContactTime;
    
    @Schema(description = "紧急程度:紧急/高/一般/低")
    private String urgencyLevel;
    
    @Schema(description = "关联房屋ID")
    private Long houseId;
    
    @Schema(description = "问题图片(JSON数组)")
    private String issueImages;
}

