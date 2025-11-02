package com.community.property.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 物业查询服务
 * 封装复杂的多表JOIN查询，避免N+1问题
 */
@Service
public class PropertyQueryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询部门成员详情列表（包含部门、角色信息）
     */
    public List<Map<String, Object>> listDepartmentMembersWithDetails(Long departmentId) {
        String sql = "SELECT DISTINCT " +
                "s.id, s.name, s.work_no, s.phone, s.email, s.position, s.job_title, " +
                "s.hire_date, s.work_status, s.account_status, s.is_manager, " +
                "s.avatar, s.wechat, s.gender, s.birth_date, " +
                "d.id as department_id, d.department_name, d.department_code, " +
                "r.id as role_id, r.role_name, r.role_code, r.role_type " +
                "FROM staff s " +
                "LEFT JOIN department d ON s.department_id = d.id " +
                "LEFT JOIN role r ON s.role_id = r.id " +
                "WHERE s.department_id = ? AND d.status = '启用' AND s.account_status = '正常' " +
                "ORDER BY s.hire_date ASC";
        return jdbcTemplate.queryForList(sql, departmentId);
    }

    /**
     * 查询分配给员工的任务详情列表（包含业主、房屋、社区、跟进信息）
     */
    public List<Map<String, Object>> listStaffTasksWithDetails(Long staffId, String status, int limit, int offset) {
        String sql = "SELECT DISTINCT " +
                "oi.id, oi.issue_title, oi.issue_content, oi.issue_type, oi.sub_type, " +
                "oi.urgency_level, oi.issue_status, oi.work_status, " +
                "oi.reported_time, oi.process_start_time, oi.process_end_time, " +
                "oi.estimated_complete_time, oi.actual_complete_time, " +
                "oi.contact_name, oi.contact_phone, oi.specific_location, " +
                "oi.process_plan, oi.process_result, oi.issue_images, oi.process_images, oi.result_images, " +
                "o.id as owner_id, o.name as owner_name, o.phone as owner_phone, " +
                "h.id as house_id, h.room_no, h.full_room_no, h.house_type, " +
                "c.id as community_id, c.community_name, c.community_code, " +
                "s.id as staff_id, s.name as staff_name, " +
                "d.id as department_id, d.department_name " +
                "FROM owner_issue oi " +
                "LEFT JOIN owner o ON oi.owner_id = o.id " +
                "LEFT JOIN house h ON oi.house_id = h.id " +
                "LEFT JOIN community_info c ON oi.community_id = c.id " +
                "LEFT JOIN staff s ON oi.assigned_staff_id = s.id " +
                "LEFT JOIN department d ON s.department_id = d.id " +
                "WHERE oi.assigned_staff_id = ? " +
                (status != null && !status.isEmpty() ? "AND oi.issue_status = ? " : "") +
                "ORDER BY oi.urgency_level DESC, oi.reported_time DESC " +
                "LIMIT ? OFFSET ?";
        
        if (status != null && !status.isEmpty()) {
            return jdbcTemplate.queryForList(sql, staffId, status, limit, offset);
        } else {
            return jdbcTemplate.queryForList(sql, staffId, limit, offset);
        }
    }

    /**
     * 查询任务总数（用于分页）
     */
    public Long countStaffTasks(Long staffId, String status) {
        String sql = "SELECT COUNT(DISTINCT oi.id) FROM owner_issue oi " +
                "WHERE oi.assigned_staff_id = ? " +
                (status != null && !status.isEmpty() ? "AND oi.issue_status = ? " : "");
        
        if (status != null && !status.isEmpty()) {
            return jdbcTemplate.queryForObject(sql, new Object[]{staffId, status}, Long.class);
        } else {
            return jdbcTemplate.queryForObject(sql, new Object[]{staffId}, Long.class);
        }
    }

    /**
     * 查询任务详情（包含所有关联信息）
     */
    public Map<String, Object> getTaskDetailWithAssociations(Long taskId) {
        String sql = "SELECT " +
                "oi.id, oi.issue_title, oi.issue_content, oi.issue_type, oi.sub_type, " +
                "oi.urgency_level, oi.issue_status, oi.work_status, oi.location_type, oi.specific_location, " +
                "oi.reported_time, oi.response_time, oi.process_start_time, oi.process_end_time, " +
                "oi.estimated_complete_time, oi.actual_complete_time, " +
                "oi.contact_name, oi.contact_phone, oi.best_contact_time, " +
                "oi.process_plan, oi.process_result, oi.actual_hours, " +
                "oi.material_cost, oi.labor_cost, oi.total_cost, oi.cost_payment_status, " +
                "oi.issue_images, oi.process_images, oi.result_images, oi.satisfaction_level, " +
                "o.id as owner_id, o.name as owner_name, o.phone as owner_phone, o.id_card, " +
                "h.id as house_id, h.room_no, h.full_room_no, h.house_code, h.house_type, " +
                "c.id as community_id, c.community_name, c.community_code, c.detail_address, " +
                "b.id as building_id, b.building_no, b.building_name, b.building_alias, " +
                "assigned_staff.id as assigned_staff_id, assigned_staff.name as assigned_staff_name, assigned_staff.phone as assigned_staff_phone, " +
                "processor.id as processor_staff_id, processor.name as processor_staff_name, " +
                "assigned_dept.id as assigned_dept_id, assigned_dept.department_name as assigned_dept_name, " +
                "processor_dept.id as processor_dept_id, processor_dept.department_name as processor_dept_name " +
                "FROM owner_issue oi " +
                "LEFT JOIN owner o ON oi.owner_id = o.id " +
                "LEFT JOIN house h ON oi.house_id = h.id " +
                "LEFT JOIN community_info c ON oi.community_id = c.id " +
                "LEFT JOIN building b ON h.building_id = b.id " +
                "LEFT JOIN staff assigned_staff ON oi.assigned_staff_id = assigned_staff.id " +
                "LEFT JOIN staff processor ON oi.processor_staff_id = processor.id " +
                "LEFT JOIN department assigned_dept ON oi.assigned_department_id = assigned_dept.id " +
                "LEFT JOIN department processor_dept ON processor.department_id = processor_dept.id " +
                "WHERE oi.id = ?";
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, taskId);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * 查询任务跟进记录
     */
    public List<Map<String, Object>> getTaskFollowUps(Long taskId) {
        String sql = "SELECT " +
                "ifu.id, ifu.follow_up_type, ifu.follow_up_content, " +
                "ifu.operator_type, ifu.operator_id, ifu.operator_name, " +
                "ifu.created_at, ifu.internal_note " +
                "FROM issue_follow_up ifu " +
                "WHERE ifu.issue_id = ? " +
                "ORDER BY ifu.created_at ASC";
        return jdbcTemplate.queryForList(sql, taskId);
    }

    /**
     * 获取部门任务统计
     */
    public Map<String, Object> getDepartmentTaskStatistics(Long departmentId) {
        String sql = "SELECT " +
                "COUNT(CASE WHEN oi.issue_status = '待处理' THEN 1 END) as pending_count, " +
                "COUNT(CASE WHEN oi.issue_status = '处理中' THEN 1 END) as processing_count, " +
                "COUNT(CASE WHEN oi.issue_status = '待审核' THEN 1 END) as reviewing_count, " +
                "COUNT(CASE WHEN oi.issue_status = '已完成' THEN 1 END) as completed_count, " +
                "COUNT(DISTINCT oi.id) as total_count, " +
                "AVG(TIMESTAMPDIFF(HOUR, oi.reported_time, oi.actual_complete_time)) as avg_process_hours, " +
                "COUNT(CASE WHEN oi.satisfaction_level >= 4 THEN 1 END) as satisfaction_count " +
                "FROM owner_issue oi " +
                "LEFT JOIN staff s ON oi.assigned_staff_id = s.id " +
                "WHERE s.department_id = ?";
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, departmentId);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * 获取员工个人任务统计
     */
    public Map<String, Object> getStaffTaskStatistics(Long staffId) {
        String sql = "SELECT " +
                "COUNT(CASE WHEN oi.issue_status = '待处理' THEN 1 END) as pending_count, " +
                "COUNT(CASE WHEN oi.issue_status = '处理中' THEN 1 END) as processing_count, " +
                "COUNT(CASE WHEN oi.issue_status = '已完成' THEN 1 END) as completed_count, " +
                "COUNT(CASE WHEN oi.issue_status = '已关闭' THEN 1 END) as closed_count, " +
                "COUNT(DISTINCT oi.id) as total_count, " +
                "AVG(TIMESTAMPDIFF(HOUR, oi.reported_time, oi.actual_complete_time)) as avg_process_hours, " +
                "COUNT(CASE WHEN oi.satisfaction_level >= 4 THEN 1 END) as satisfaction_count, " +
                "COUNT(CASE WHEN oi.satisfaction_level < 4 AND oi.satisfaction_level IS NOT NULL THEN 1 END) as unsatisfied_count " +
                "FROM owner_issue oi " +
                "WHERE oi.assigned_staff_id = ?";
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, staffId);
        return result.isEmpty() ? null : result.get(0);
    }
}
