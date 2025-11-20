package com.community.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.common.Result;
import com.community.admin.domain.entity.IssueFollowUp;
import com.community.admin.service.IssueFollowUpService;
import com.community.admin.service.RedisMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issueFollowUp")
@Tag(name = "问题跟进管理", description = "问题跟进相关接口")
@ApiSupport(order = 23, author = "社区管理系统开发团队")
public class IssueFollowUpController {
    @Autowired
    private RedisMessageService redisMessageService;

    @Autowired
    private IssueFollowUpService issueFollowUpService;

    /**
     * 分页查询问题跟进信息
     *
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param issueFollowUp 查询条件
     * @return 问题跟进信息分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询问题跟进信息", description = "根据条件分页查询问题跟进信息列表")
    @ApiOperationSupport(order = 1, author = "开发团队")
    @SecurityRequirement(name = "Authorization")
    public Result page(@Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer pageNum,
                       @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                       IssueFollowUp issueFollowUp) {
        IPage<IssueFollowUp> page = new Page<>(pageNum, pageSize);
        IPage<IssueFollowUp> result = issueFollowUpService.selectIssueFollowUpPage(page, issueFollowUp);
        return Result.ok().put("data", result.getRecords())
                .put("total", result.getTotal())
                .put("pageNum", result.getCurrent())
                .put("pageSize", result.getSize());
    }

    /**
     * 根据ID查询问题跟进信息
     *
     * @param id 问题跟进ID
     * @return 问题跟进信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询问题跟进信息", description = "根据ID查询指定问题跟进信息")
    @ApiOperationSupport(order = 2, author = "开发团队")
    @SecurityRequirement(name = "Authorization")
    public Result getById(@Parameter(name = "id", description = "问题跟进ID", required = true, example = "1") @PathVariable Long id) {
        IssueFollowUp issueFollowUp = issueFollowUpService.getById(id);
        return Result.ok().put("data", issueFollowUp);
    }

    /**
     * 新增问题跟进信息
     *
     * @param issueId 问题ID（必填）
     * @param followUpType 跟进类型（必填）
     * @param followUpContent 跟进内容（必填）
     * @param operatorType 操作人类型（必填）
     * @param operatorName 操作人姓名（必填）
     * @param operatorId 操作人ID
     * @param attachments 附件信息JSON格式
     * @param internalNote 内部备注
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "新增问题跟进信息", description = "新增问题跟进信息")
    @ApiOperationSupport(order = 3, author = "开发团队")
    @SecurityRequirement(name = "Authorization")
    public Result add(
            @Parameter(description = "问题ID", required = true) @RequestParam Long issueId,
            @Parameter(description = "跟进类型", required = true) @RequestParam String followUpType,
            @Parameter(description = "跟进内容", required = true) @RequestParam String followUpContent,
            @Parameter(description = "操作人类型", required = true) @RequestParam String operatorType,
            @Parameter(description = "操作人姓名", required = true) @RequestParam String operatorName,
            @Parameter(description = "操作人ID") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "附件信息JSON格式") @RequestParam(required = false) String attachments,
            @Parameter(description = "内部备注") @RequestParam(required = false) String internalNote) {
        
        // 构建问题跟进信息对象
        IssueFollowUp issueFollowUp = new IssueFollowUp();
        issueFollowUp.setIssueId(issueId);
        issueFollowUp.setFollowUpType(followUpType);
        issueFollowUp.setFollowUpContent(followUpContent);
        issueFollowUp.setOperatorType(operatorType);
        issueFollowUp.setOperatorId(operatorId);
        issueFollowUp.setOperatorName(operatorName);
        issueFollowUp.setAttachments(attachments);
        issueFollowUp.setInternalNote(internalNote);
        
        // 保存问题跟进信息
        boolean result = issueFollowUpService.save(issueFollowUp);
        if (result) {
            // 发布实时同步消息
            try {
                redisMessageService.publishAdminChange("CREATE", "IssueFollowUp", issueFollowUp.getId(), issueFollowUp);
                redisMessageService.publishNotification("owner", "ISSUE_FOLLOW_UP", "问题跟进", 
                    "您的问题有新的跟进：" + issueFollowUp.getFollowUpContent(), null);
            } catch (Exception e) {
                System.err.println("发布跟进新增实时消息失败: " + e.getMessage());
            }
            
            return Result.ok("新增成功");
        } else {
            return Result.error("新增失败");
        }
    }

    /**
     * 修改问题跟进信息
     *
     * @param issueFollowUp 问题跟进信息
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "修改问题跟进信息", description = "修改问题跟进信息")
    @ApiOperationSupport(order = 4, author = "开发团队")
    @SecurityRequirement(name = "Authorization")
    public Result update(@Parameter(description = "问题跟进信息") @RequestBody IssueFollowUp issueFollowUp) {
        boolean result = issueFollowUpService.updateById(issueFollowUp);
        if (result) {
            // 发布实时同步消息
            try {
                redisMessageService.publishAdminChange("UPDATE", "IssueFollowUp", issueFollowUp.getId(), issueFollowUp);
            } catch (Exception e) {
                System.err.println("发布跟进更新实时消息失败: " + e.getMessage());
            }
            
            return Result.ok("修改成功");
        } else {
            return Result.error("修改失败");
        }
    }

    /**
     * 根据问题ID查询跟进记录
     *
     * @param issueId 问题ID
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return 跟进记录分页数据
     */
    @GetMapping("/issue/{issueId}")
    @Operation(summary = "根据问题ID查询跟进记录", description = "根据问题ID分页查询跟进记录")
    @ApiOperationSupport(order = 5, author = "开发团队")
    @SecurityRequirement(name = "Authorization")
    public Result getByIssueId(
            @Parameter(description = "问题ID") @PathVariable Long issueId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        IssueFollowUp queryCondition = new IssueFollowUp();
        queryCondition.setIssueId(issueId);
        
        IPage<IssueFollowUp> page = new Page<>(pageNum, pageSize);
        IPage<IssueFollowUp> result = issueFollowUpService.selectIssueFollowUpPage(page, queryCondition);
        
        return Result.ok().put("data", result.getRecords())
                .put("total", result.getTotal())
                .put("pageNum", result.getCurrent())
                .put("pageSize", result.getSize());
    }

    /**
     * 删除问题跟进信息
     *
     * @param id 问题跟进ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除问题跟进信息", description = "根据ID删除问题跟进信息")
    @ApiOperationSupport(order = 6, author = "开发团队")
    @SecurityRequirement(name = "Authorization")
    public Result delete(@Parameter(name = "id", description = "问题跟进ID", required = true, example = "1") @PathVariable Long id) {
        boolean result = issueFollowUpService.removeById(id);
        if (result) {
            // 发布实时同步消息
            try {
                redisMessageService.publishAdminChange("DELETE", "IssueFollowUp", id, null);
            } catch (Exception e) {
                System.err.println("发布跟进删除实时消息失败: " + e.getMessage());
            }
            
            return Result.ok("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}