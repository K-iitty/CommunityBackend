package com.community.property.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物业员工系统信息修改申请请求
 * 包含需要管理员审批才能修改的字段
 */
@Data
@Schema(description = "物业员工系统信息修改申请请求")
public class StaffInfoUpdateApplyRequest {
    
    @Schema(description = "申请类型：部门调动/职务变更/职称变更/其他", required = true)
    private String applyType;
    
    @Schema(description = "申请原因", required = true)
    private String applyReason;
    
    @Schema(description = "期望调整的部门ID（部门调动时填写）")
    private Long targetDepartmentId;
    
    @Schema(description = "期望调整的职位（职务变更时填写）")
    private String targetPosition;
    
    @Schema(description = "期望调整的职称（职称变更时填写）")
    private String targetJobTitle;
    
    @Schema(description = "期望调整的角色ID（角色变更时填写）")
    private Long targetRoleId;
    
    @Schema(description = "补充说明")
    private String additionalNote;
    
    @Schema(description = "附件（JSON数组）")
    private String attachments;
}

