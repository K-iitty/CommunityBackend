package com.community.property.mapper;

import com.community.property.entity.OwnerIssue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 业主问题Mapper
 */
@Mapper
public interface OwnerIssueMapper extends BaseMapper<OwnerIssue> {
    
    /**
     * 根据分配员工ID查询任务列表
     */
    @SelectProvider(type = OwnerIssueDaoProvider.class, method = "findByStaffId")
    List<OwnerIssue> findByStaffId(@Param("staffId") Long staffId, 
                                    @Param("status") String status, 
                                    @Param("offset") int offset, 
                                    @Param("size") int size);
    
    /**
     * 统计员工任务数
     */
    @SelectProvider(type = OwnerIssueDaoProvider.class, method = "countByStaffId")
    int countByStaffId(@Param("staffId") Long staffId, @Param("status") String status);
    
    /**
     * 统计部门任务数
     */
    @Select("SELECT COUNT(*) FROM owner_issue WHERE assigned_department_id = #{departmentId}")
    int countByDepartmentId(Long departmentId);
    
    /**
     * 根据ID和员工ID查询（验证归属）
     */
    @Select("SELECT * FROM owner_issue WHERE id = #{taskId} AND assigned_staff_id = #{staffId}")
    OwnerIssue findByIdAndStaffId(@Param("taskId") Long taskId, @Param("staffId") Long staffId);
    
    /**
     * 更新任务
     */
    @UpdateProvider(type = OwnerIssueDaoProvider.class, method = "update")
    int update(OwnerIssue issue);
    
    class OwnerIssueDaoProvider {
        public String findByStaffId(@Param("staffId") Long staffId, 
                                    @Param("status") String status, 
                                    @Param("offset") int offset, 
                                    @Param("size") int size) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM owner_issue WHERE assigned_staff_id = #{staffId} ");
            
            if (status != null) {
                sql.append("AND issue_status = #{status} ");
            }
            
            sql.append("ORDER BY reported_time DESC LIMIT #{offset}, #{size}");
            return sql.toString();
        }
        
        public String countByStaffId(@Param("staffId") Long staffId, @Param("status") String status) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) FROM owner_issue WHERE assigned_staff_id = #{staffId} ");
            
            if (status != null) {
                sql.append("AND issue_status = #{status} ");
            }
            
            return sql.toString();
        }
        
        public String update(OwnerIssue issue) {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE owner_issue SET ");
            
            if (issue.getIssueStatus() != null) {
                sql.append("issue_status = #{issueStatus}, ");
            }
            if (issue.getWorkStatus() != null) {
                sql.append("work_status = #{workStatus}, ");
            }
            if (issue.getProcessorStaffId() != null) {
                sql.append("processor_staff_id = #{processorStaffId}, ");
            }
            if (issue.getProcessStartTime() != null) {
                sql.append("process_start_time = #{processStartTime}, ");
            }
            if (issue.getProcessEndTime() != null) {
                sql.append("process_end_time = #{processEndTime}, ");
            }
            if (issue.getProcessPlan() != null) {
                sql.append("process_plan = #{processPlan}, ");
            }
            if (issue.getProcessResult() != null) {
                sql.append("process_result = #{processResult}, ");
            }
            if (issue.getActualHours() != null) {
                sql.append("actual_hours = #{actualHours}, ");
            }
            if (issue.getHasCost() != null) {
                sql.append("has_cost = #{hasCost}, ");
            }
            if (issue.getMaterialCost() != null) {
                sql.append("material_cost = #{materialCost}, ");
            }
            if (issue.getLaborCost() != null) {
                sql.append("labor_cost = #{laborCost}, ");
            }
            if (issue.getTotalCost() != null) {
                sql.append("total_cost = #{totalCost}, ");
            }
            if (issue.getProcessImages() != null) {
                sql.append("process_images = #{processImages}, ");
            }
            if (issue.getResultImages() != null) {
                sql.append("result_images = #{resultImages}, ");
            }
            if (issue.getActualCompleteTime() != null) {
                sql.append("actual_complete_time = #{actualCompleteTime}, ");
            }
            
            sql.append("updated_at = NOW() WHERE id = #{id}");
            return sql.toString();
        }
    }
}