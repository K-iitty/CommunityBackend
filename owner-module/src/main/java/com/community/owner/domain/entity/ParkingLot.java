package com.community.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 停车场实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parking_lot")
public class ParkingLot extends BaseEntity {
    
    /**
     * 所属小区ID
     */
    @TableField("community_id")
    private Long communityId;
    
    /**
     * 停车场名�?     */
    @TableField("lot_name")
    private String lotName;
    
    /**
     * 停车场编�?     */
    @TableField("lot_code")
    private String lotCode;
    
    /**
     * 车库类别
     */
    @TableField("lot_category")
    private String lotCategory;
    
    /**
     * 车库区域名称
     */
    @TableField("zone_name")
    private String zoneName;
    
    /**
     * 区域编码
     */
    @TableField("zone_code")
    private String zoneCode;
    
    /**
     * 联系�?     */
    @TableField("contact_person")
    private String contactPerson;
    
    /**
     * 联系人电�?     */
    @TableField("contact_phone")
    private String contactPhone;
    
    /**
     * 地址
     */
    @TableField("address")
    private String address;
    
    /**
     * 详细地址
     */
    @TableField("detail_address")
    private String detailAddress;
    
    /**
     * 经度坐标
     */
    @TableField("longitude")
    private BigDecimal longitude;
    
    /**
     * 纬度坐标
     */
    @TableField("latitude")
    private BigDecimal latitude;
    
    /**
     * 车位数量
     */
    @TableField("total_spaces")
    private Integer totalSpaces;
    
    /**
     * 固定车位�?     */
    @TableField("fixed_spaces")
    private Integer fixedSpaces;
    
    /**
     * 临时车位�?     */
    @TableField("temp_spaces")
    private Integer tempSpaces;
    
    /**
     * 起始编号
     */
    @TableField("start_no")
    private Integer startNo;
    
    /**
     * 结束编号
     */
    @TableField("end_no")
    private Integer endNo;
    
    /**
     * 所在楼�?     */
    @TableField("floor_level")
    private Integer floorLevel;
    
    /**
     * 营业时间
     */
    @TableField("business_hours")
    private String businessHours;
    
    /**
     * 是否允许外来车辆进入
     */
    @TableField("is_allow_foreign")
    private Integer isAllowForeign;
    
    /**
     * 计费方式
     */
    @TableField("charge_method")
    private String chargeMethod;
    
    /**
     * 收费标准
     */
    @TableField("charge_standard")
    private String chargeStandard;
    
    /**
     * 首段时长(分钟)
     */
    @TableField("first_duration")
    private Integer firstDuration;
    
    /**
     * 首段费用
     */
    @TableField("first_fee")
    private BigDecimal firstFee;
    
    /**
     * 单位时长(分钟)
     */
    @TableField("unit_duration")
    private Integer unitDuration;
    
    /**
     * 单位费用
     */
    @TableField("unit_fee")
    private BigDecimal unitFee;
    
    /**
     * 每日封顶费用
     */
    @TableField("daily_max_fee")
    private BigDecimal dailyMaxFee;
    
    /**
     * 月租费用
     */
    @TableField("monthly_fee")
    private BigDecimal monthlyFee;
    
    /**
     * 免费时长(分钟)
     */
    @TableField("free_duration")
    private Integer freeDuration;
    
    /**
     * 状�?启用/停用/维修
     */
    @TableField("status")
    private String status;
    
    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;
}
