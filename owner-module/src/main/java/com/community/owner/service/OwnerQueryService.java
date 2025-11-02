package com.community.owner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * 业主查询Service - 提供复杂的多表关联查询
 * 用于替代N+1查询，提升性能
 */
@Service
public class OwnerQueryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询业主的房屋列表（包含楼栋和社区信息）
     */
    public List<Map<String, Object>> listOwnerHousesWithDetails(Long ownerId, int limit, int offset) {
        String sql = "SELECT h.id, h.community_id, h.building_id, h.room_no, h.full_room_no, " +
                "h.house_code, h.building_area, h.usable_area, h.shared_area, h.house_type, " +
                "h.house_layout, h.house_orientation, h.parking_space_no, h.parking_type, " +
                "h.house_status, h.decoration_status, h.floor_level, h.has_balcony, h.has_garden, " +
                "h.remark, h.floor_plan_image, h.created_at, h.updated_at, " +
                "b.id as building_id_val, b.building_no, b.building_name, b.building_alias, b.building_type, " +
                "b.total_floors, b.building_address, b.has_elevator, " +
                "c.id as community_id_val, c.community_name, c.community_code, c.detail_address, " +
                "c.property_company, c.contact_phone, c.community_images " +
                "FROM house h " +
                "INNER JOIN house_owner ho ON h.id = ho.house_id " +
                "INNER JOIN building b ON h.building_id = b.id " +
                "INNER JOIN community_info c ON h.community_id = c.id " +
                "WHERE ho.owner_id = ? " +
                "ORDER BY h.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, ownerId, limit, offset);
    }

    /**
     * 计数：业主的房屋总数
     */
    public Long countOwnerHouses(Long ownerId) {
        String sql = "SELECT COUNT(1) FROM house_owner ho WHERE ho.owner_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, new Object[]{ownerId}, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * 查询业主的仪表卡片列表（含房屋、楼栋、社区、仪表配置关联）
     */
    public List<Map<String, Object>> listOwnerMeterCardsWithDetails(Long ownerId) {
        String sql = "SELECT m.id, m.community_id, m.house_id, m.building_id, m.config_id, " +
                "m.meter_name, m.category_name, m.meter_type, m.meter_code, m.meter_sn, " +
                "m.location_type, m.install_location, m.install_date, " +
                "m.initial_reading, m.current_reading, m.max_reading, m.total_usage, " +
                "m.unit, m.charge_standard, m.online_status, m.power_status, m.meter_status, " +
                "m.comm_address, m.last_comm_time, m.remark, m.created_at, m.updated_at, " +
                "h.room_no, h.full_room_no, b.building_no, b.building_name, c.community_name, " +
                "mc.unit_price, mc.calculation_method, mc.decimal_places, mc.min_value, mc.max_value, " +
                "mc.status as config_status, mc.remark as config_remark " +
                "FROM meter_info m " +
                "INNER JOIN house h ON m.house_id = h.id " +
                "INNER JOIN house_owner ho ON h.id = ho.house_id " +
                "INNER JOIN building b ON h.building_id = b.id " +
                "INNER JOIN community_info c ON h.community_id = c.id " +
                "LEFT JOIN meter_config mc ON m.config_id = mc.id " +
                "WHERE ho.owner_id = ? " +
                "AND (m.remark IS NULL OR m.remark NOT LIKE '%[申请新增]%')";
        return jdbcTemplate.queryForList(sql, ownerId);
    }

    /**
     * 查询业主的车辆列表（含车位和停车场关联）
     */
    public List<Map<String, Object>> listOwnerVehiclesWithDetails(Long ownerId, int limit, int offset) {
        String sql = "SELECT v.id, v.owner_id, v.plate_number, v.vehicle_type, v.brand, v.model, v.color, " +
                "v.fixed_space_id, v.vehicle_license_no, v.engine_no, v.status, v.register_date, " +
                "v.remark, v.driver_license_image, v.vehicle_images, v.created_at, v.updated_at, " +
                "ps.space_no, ps.full_space_no, pl.lot_name, pl.lot_code, pl.zone_name " +
                "FROM vehicle v " +
                "LEFT JOIN parking_space ps ON v.fixed_space_id = ps.id " +
                "LEFT JOIN parking_lot pl ON ps.parking_lot_id = pl.id " +
                "WHERE v.owner_id = ? AND v.status = '正常' " +
                "ORDER BY v.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, ownerId, limit, offset);
    }

    /**
     * 计数：业主的车辆总数
     */
    public Long countOwnerVehicles(Long ownerId) {
        String sql = "SELECT COUNT(1) FROM vehicle WHERE owner_id = ? AND status = '正常'";
        Long count = jdbcTemplate.queryForObject(sql, new Object[]{ownerId}, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * 查询业主的车位列表（含停车场关联）
     */
    public List<Map<String, Object>> listOwnerParkingSpacesWithDetails(Long ownerId, int limit, int offset) {
        String sql = "SELECT ps.id, ps.parking_lot_id, ps.space_no, ps.full_space_no, ps.space_type, " +
                "ps.space_area, ps.space_status, ps.owner_id, ps.vehicle_id, ps.monthly_fee, " +
                "ps.remark, ps.created_at, ps.updated_at, " +
                "pl.lot_name, pl.lot_code, pl.zone_name, pl.charge_method, pl.charge_standard " +
                "FROM parking_space ps " +
                "INNER JOIN parking_lot pl ON ps.parking_lot_id = pl.id " +
                "WHERE ps.owner_id = ? " +
                "ORDER BY ps.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, ownerId, limit, offset);
    }

    /**
     * 计数：业主的车位总数
     */
    public Long countOwnerParkingSpaces(Long ownerId) {
        String sql = "SELECT COUNT(1) FROM parking_space WHERE owner_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, new Object[]{ownerId}, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * 查询业主所属社区的公告列表（已发布且已审核，包括过期公告）
     */
    public List<Map<String, Object>> listOwnerCommunityNoticesWithDetails(Long ownerId, int limit, int offset) {
        String sql = "SELECT DISTINCT cn.id, cn.community_id, cn.notice_type, cn.title, " +
                "cn.content, cn.notice_images, cn.start_time, cn.end_time, cn.publish_time, " +
                "cn.status, cn.is_top, cn.read_count, cn.activity_date, cn.activity_time, " +
                "cn.activity_location, cn.activity_contact, cn.notice_images, " +
                "cn.created_at, cn.updated_at " +
                "FROM community_notice cn " +
                "INNER JOIN community_info c ON cn.community_id = c.id " +
                "INNER JOIN house h ON c.id = h.community_id " +
                "INNER JOIN house_owner ho ON h.id = ho.house_id " +
                "WHERE ho.owner_id = ? AND ho.is_verified = 1 " +
                "AND cn.status = '已发布' " +
                "AND cn.approval_status = '已审核' " +
                "AND cn.target_audience = '全体业主' " +
                "ORDER BY cn.is_top DESC, cn.publish_time DESC " +
                "LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, ownerId, limit, offset);
    }

    /**
     * 计数：业主所属社区的公告总数
     */
    public Long countOwnerCommunityNotices(Long ownerId) {
        String sql = "SELECT COUNT(DISTINCT cn.id) FROM community_notice cn " +
                "INNER JOIN community_info c ON cn.community_id = c.id " +
                "INNER JOIN house h ON c.id = h.community_id " +
                "INNER JOIN house_owner ho ON h.id = ho.house_id " +
                "WHERE ho.owner_id = ? AND ho.is_verified = 1 " +
                "AND cn.status = '已发布' " +
                "AND cn.approval_status = '已审核' " +
                "AND cn.target_audience = '全体业主'";
        Long count = jdbcTemplate.queryForObject(sql, new Object[]{ownerId}, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * 查询业主的缴费卡片列表（仅包含有抄表记录的仪表）
     * 查询路径：house_owner → meter_info → meter_config + meter_reading
     * 仅返回有抄表记录且费用 > 0 的仪表
     */
    public List<Map<String, Object>> listOwnerBillingCardsWithDetails(Long ownerId) {
        String sql = "SELECT " +
                "m.id, m.community_id, m.house_id, m.building_id, m.config_id, " +
                "m.meter_name, m.category_name, m.meter_type, m.meter_code, m.meter_status, " +
                "m.unit, m.current_reading, m.initial_reading, " +
                "h.room_no, h.full_room_no, " +
                "b.building_no, b.building_name, " +
                "mr.id as reading_id, mr.meter_id, mr.usage_amount, mr.reading_date, mr.previous_reading, " +
                "mr.unit as reading_unit, mr.reading_status, mr.processed, " +
                "mc.unit_price, mc.calculation_method " +
                "FROM house_owner ho " +
                "INNER JOIN house h ON ho.house_id = h.id " +
                "INNER JOIN meter_info m ON h.id = m.house_id " +
                "INNER JOIN building b ON h.building_id = b.id " +
                "INNER JOIN meter_reading mr ON m.id = mr.meter_id " +
                "INNER JOIN meter_config mc ON mc.category_name = m.category_name AND mc.meter_type = m.meter_type " +
                "WHERE " +
                "ho.owner_id = ? " +
                "AND ho.relationship = '业主' " +
                "AND ho.status = '正常' " +
                "AND mr.processed = 0 " +
                "AND m.meter_status = '正常' " +
                "AND mc.status = '启用' " +
                "ORDER BY mr.reading_date DESC, m.id DESC";
        return jdbcTemplate.queryForList(sql, ownerId);
    }

    /**
     * 查询业主的停车位月租费用（待缴费）
     */
    public List<Map<String, Object>> listOwnerParkingBillingWithDetails(Long ownerId) {
        String sql = "SELECT ps.id as parking_space_id, ps.space_no, ps.full_space_no, ps.space_type, " +
                "ps.monthly_fee, ps.space_status, ps.owner_id, ps.vehicle_id, " +
                "pl.id as parking_lot_id, pl.lot_name, pl.lot_code, pl.zone_name, " +
                "pl.community_id, pl.charge_method, pl.charge_standard, " +
                "v.plate_number, v.vehicle_type " +
                "FROM parking_space ps " +
                "INNER JOIN parking_lot pl ON ps.parking_lot_id = pl.id " +
                "LEFT JOIN vehicle v ON ps.vehicle_id = v.id " +
                "WHERE ps.owner_id = ? " +
                "AND ps.space_status IN ('已租', '占用') " +
                "AND ps.monthly_fee > 0 " +
                "ORDER BY ps.id DESC";
        return jdbcTemplate.queryForList(sql, ownerId);
    }

    /**
     * 查询业主的仪表抄表账单（待缴费）- 优化版本
     * 查询路径：house_owner → house → meter_info → meter_reading → meter_config
     * 仅返回未支付的抄表记录（processed = 0）
     * 账单金额 = usage_amount × unit_price
     * 
     * 关键过滤条件：
     * - 业主关系为"业主"（relationship = '业主'）
     * - 关系状态为"正常"（status = '正常'）
     * - 仪表状态为"正常"（meter_status = '正常'）
     * - 配置状态为"启用"（config status = '启用'）
     * - 账单未支付（processed = 0）
     */
    public List<Map<String, Object>> listOwnerMeterBillingWithDetails(Long ownerId) {
        String sql = "SELECT " +
                "mr.id AS reading_id, " +
                "mr.meter_id, " +
                "mi.meter_name, " +
                "mi.category_name, " +
                "mi.meter_type, " +
                "mi.meter_code, " +
                "mi.meter_status, " +
                "mi.house_id, " +
                "h.room_no, " +
                "h.full_room_no, " +
                "h.building_id, " +
                "b.building_no, " +
                "b.building_name, " +
                "mr.previous_reading, " +
                "mr.current_reading, " +
                "mr.usage_amount, " +
                "mr.unit, " +
                "mr.reading_date, " +
                "mr.reading_time, " +
                "mr.reading_status, " +
                "mr.processed, " +
                "mr.created_at, " +
                "mc.unit_price, " +
                "mc.calculation_method, " +
                "mc.status as config_status, " +
                "ROUND(mr.usage_amount * mc.unit_price, 2) AS bill_amount, " +
                "c.community_name " +
                "FROM house_owner ho " +
                "INNER JOIN house h ON ho.house_id = h.id " +
                "INNER JOIN meter_info mi ON mi.house_id = h.id " +
                "INNER JOIN meter_reading mr ON mr.meter_id = mi.id " +
                "INNER JOIN building b ON h.building_id = b.id " +
                "INNER JOIN community_info c ON h.community_id = c.id " +
                "INNER JOIN meter_config mc ON mc.category_name = mi.category_name AND mc.meter_type = mi.meter_type " +
                "WHERE " +
                "ho.owner_id = ? " +
                "AND ho.relationship = '业主' " +
                "AND ho.status = '正常' " +
                "AND mr.processed = 0 " +
                "AND mi.meter_status = '正常' " +
                "AND mc.status = '启用' " +
                "ORDER BY mr.reading_date DESC, mi.category_name";
        return jdbcTemplate.queryForList(sql, ownerId);
    }

    /**
     * 查询社区的物业人员列表（包含部门和职务信息）
     */
    public List<Map<String, Object>> listCommunityStaffWithDetails(Long communityId) {
        String sql = "SELECT s.id, s.name, s.phone, s.email, s.wechat, " +
                "s.position, s.job_title, s.education_level, s.major, s.graduate_school, " +
                "s.avatar, s.telephone_area_code, s.telephone_number, s.telephone_extension, " +
                "d.id as department_id, d.dept_name, d.dept_code, d.remark as dept_remark, " +
                "r.role_name, r.description as role_description " +
                "FROM staff s " +
                "LEFT JOIN department d ON s.department_id = d.id " +
                "LEFT JOIN role r ON s.role_id = r.id " +
                "WHERE s.account_status = '正常' AND s.work_status = '在职' " +
                "AND d.id = (SELECT manager_staff_id FROM community_info WHERE id = ? LIMIT 1) " +
                "ORDER BY s.position DESC, s.job_title DESC";
        try {
            return jdbcTemplate.queryForList(sql, communityId);
        } catch (Exception e) {
            // 如果上述查询失败，返回社区下所有员工
            String fallbackSql = "SELECT s.id, s.name, s.phone, s.email, s.wechat, " +
                    "s.position, s.job_title, s.education_level, s.major, s.graduate_school, " +
                    "s.avatar, s.telephone_area_code, s.telephone_number, s.telephone_extension, " +
                    "d.id as department_id, d.dept_name, d.dept_code, " +
                    "r.role_name, r.description as role_description " +
                    "FROM staff s " +
                    "LEFT JOIN department d ON s.department_id = d.id " +
                    "LEFT JOIN role r ON s.role_id = r.id " +
                    "WHERE s.account_status = '正常' AND s.work_status = '在职' " +
                    "ORDER BY s.position DESC, s.job_title DESC LIMIT 20";
            return jdbcTemplate.queryForList(fallbackSql);
        }
    }

    /**
     * 查询所有物业人员列表（分页）- 供业主查询
     * 业主登录后无需验证房屋即可查询所有物业人员的联系方式
     */
    public List<Map<String, Object>> listAllPropertyStaffWithPagination(int offset, int size) {
        String sql = "SELECT s.id, s.name, s.phone, s.email, s.wechat, " +
                "s.position, s.job_title, s.education_level, s.major, s.graduate_school, " +
                "s.avatar, s.certificate_photos, s.telephone_area_code, s.telephone_number, s.telephone_extension, " +
                "d.id as department_id, d.department_name, d.department_code, " +
                "r.id as role_id, r.role_name, r.description as role_description " +
                "FROM staff s " +
                "LEFT JOIN department d ON s.department_id = d.id " +
                "LEFT JOIN role r ON s.role_id = r.id " +
                "WHERE s.account_status = '正常' AND s.work_status = '在职' " +
                "ORDER BY s.position DESC, s.job_title DESC, s.id ASC " +
                "LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, size, offset);
    }

    /**
     * 统计所有物业人员总数 - 供业主查询
     */
    public Long countAllPropertyStaff() {
        String sql = "SELECT COUNT(1) FROM staff " +
                "WHERE account_status = '正常' AND work_status = '在职'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * 查询业主关联的停车场中状态为非启用的车位（申请记录）
     */
    public List<Map<String, Object>> listOwnerParkingSpacesByLotStatus(Long ownerId, String status, int limit, int offset) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ps.id, ps.parking_lot_id, ps.space_no, ps.full_space_no, ps.space_type, ")
           .append("ps.space_area, ps.space_status, ps.owner_id, ps.vehicle_id, ps.monthly_fee, ")
           .append("ps.remark, ps.created_at, ps.updated_at, ")
           .append("pl.id as lot_id, pl.lot_name, pl.lot_code, pl.zone_name, pl.charge_method, ")
           .append("pl.charge_standard, pl.status ")
           .append("FROM parking_space ps ")
           .append("INNER JOIN parking_lot pl ON ps.parking_lot_id = pl.id ")
           .append("WHERE ps.owner_id = ? ");
        
        // 如果指定了status，按停车场状态过滤；否则查询所有非启用的停车场
        if (status != null && !status.isEmpty()) {
            sql.append("AND pl.status = ? ");
        } else {
            sql.append("AND pl.status != '启用' ");
        }
        
        sql.append("ORDER BY ps.id DESC LIMIT ? OFFSET ?");
        
        if (status != null && !status.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), ownerId, status, limit, offset);
        } else {
            return jdbcTemplate.queryForList(sql.toString(), ownerId, limit, offset);
        }
    }

    /**
     * 统计业主关联的停车场中状态为非启用的车位总数
     */
    public Long countOwnerParkingSpacesByLotStatus(Long ownerId, String status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM parking_space ps ")
           .append("INNER JOIN parking_lot pl ON ps.parking_lot_id = pl.id ")
           .append("WHERE ps.owner_id = ? ");
        
        // 如果指定了status，按停车场状态过滤；否则查询所有非启用的停车场
        if (status != null && !status.isEmpty()) {
            sql.append("AND pl.status = ?");
            Long count = jdbcTemplate.queryForObject(sql.toString(), new Object[]{ownerId, status}, Long.class);
            return count != null ? count : 0L;
        } else {
            sql.append("AND pl.status != '启用'");
            Long count = jdbcTemplate.queryForObject(sql.toString(), new Object[]{ownerId}, Long.class);
            return count != null ? count : 0L;
        }
    }

    /**
     * 查询所有可用的停车位列表（按车位状态space_status过滤）
     * 不受业主身份限制，返回数据库中的所有停车位信息
     */
    public List<Map<String, Object>> listAvailableParkingSpacesByStatus(Long ownerId, String spaceStatus, int limit, int offset) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ps.id, ps.parking_lot_id, ps.space_no, ps.full_space_no, ps.space_type, ")
           .append("ps.space_area, ps.space_status, ps.owner_id, ps.vehicle_id, ps.monthly_fee, ")
           .append("ps.remark, ps.created_at, ps.updated_at, ")
           .append("pl.id as lot_id, pl.community_id, pl.lot_name, pl.lot_code, pl.zone_name, pl.charge_method, ")
           .append("pl.charge_standard, pl.status as lot_status, ")
           .append("c.community_name ")
           .append("FROM parking_space ps ")
           .append("INNER JOIN parking_lot pl ON ps.parking_lot_id = pl.id ")
           .append("INNER JOIN community_info c ON pl.community_id = c.id ")
           .append("WHERE 1=1 ");
        
        // 只在指定status时过滤，否则查询全部
        if (spaceStatus != null && !spaceStatus.isEmpty()) {
            sql.append("AND ps.space_status = ? ");
        }
        
        sql.append("ORDER BY ps.id DESC LIMIT ? OFFSET ?");
        
        if (spaceStatus != null && !spaceStatus.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), spaceStatus, limit, offset);
        } else {
            return jdbcTemplate.queryForList(sql.toString(), limit, offset);
        }
    }

    /**
     * 统计所有可用的停车位总数（按车位状态space_status过滤）
     * 不受业主身份限制
     */
    public Long countAvailableParkingSpacesByStatus(Long ownerId, String spaceStatus) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(ps.id) FROM parking_space ps ")
           .append("INNER JOIN parking_lot pl ON ps.parking_lot_id = pl.id ")
           .append("INNER JOIN community_info c ON pl.community_id = c.id ")
           .append("WHERE 1=1 ");
        
        // 只在指定status时过滤，否则查询全部
        if (spaceStatus != null && !spaceStatus.isEmpty()) {
            sql.append("AND ps.space_status = ? ");
            Long count = jdbcTemplate.queryForObject(sql.toString(), new Object[]{spaceStatus}, Long.class);
            return count != null ? count : 0L;
        } else {
            Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class);
            return count != null ? count : 0L;
        }
    }

    /**
     * 查询业主的问题产生的费用（待缴费）
     * 条件：owner_id为当前业主 AND total_cost > 0 AND cost_payment_status = '未支付'
     */
    public List<Map<String, Object>> listOwnerIssueBillingWithDetails(Long ownerId) {
        String sql = "SELECT oi.id, oi.community_id, oi.owner_id, oi.house_id, " +
                "oi.issue_title, oi.issue_type, oi.sub_type, oi.issue_status, " +
                "oi.has_cost, oi.material_cost, oi.labor_cost, oi.total_cost, oi.cost_payment_status, " +
                "oi.reported_time, oi.actual_complete_time, " +
                "h.room_no, h.full_room_no, b.building_no, b.building_name, " +
                "c.community_name " +
                "FROM owner_issue oi " +
                "INNER JOIN house h ON oi.house_id = h.id " +
                "INNER JOIN building b ON h.building_id = b.id " +
                "INNER JOIN community_info c ON oi.community_id = c.id " +
                "WHERE oi.owner_id = ? " +
                "AND oi.total_cost > 0 " +
                "AND oi.cost_payment_status = '未支付' " +
                "ORDER BY oi.reported_time DESC";
        return jdbcTemplate.queryForList(sql, ownerId);
    }
}
