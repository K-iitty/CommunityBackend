package com.community.owner.domain.dto.request;

import lombok.Data;

/**
 * 问题提交请求对象
 */
@Data
public class IssueSubmitRequest {
    
    /**
     * 问题标题
     */
    private String issueTitle;
    
    /**
     * 问题详情描述
     */
    private String issueContent;
    
    /**
     * 问题类型:缴费问题/维修问题/通知问题/投诉建议/咨询服务/其他
     */
    private String issueType;
    
    /**
     * 问题子类型
     */
    private String subType;
    
    /**
     * 位置类型:室内/公共区域/停车场/其他
     */
    private String locationType;
    
    /**
     * 具体位置
     */
    private String specificLocation;
    
    /**
     * 联系人姓名
     */
    private String contactName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 最佳联系时间
     */
    private String bestContactTime;
    
    /**
     * 紧急程度:紧急/高/一般/低
     */
    private String urgencyLevel;
    
    /**
     * 关联房屋ID
     */
    private Long houseId;
    
    /**
     * 附加图片(阿里云OSS的URL)
     */
    private String additionalImages;
}

