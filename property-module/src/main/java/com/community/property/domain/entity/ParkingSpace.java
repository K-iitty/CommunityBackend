package com.community.property.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 车位实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parking_space")
public class ParkingSpace extends BaseEntity {
    
    /**
     * 停车场ID
     */
    @TableField("parking_lot_id")
    private Long parkingLotId;
    
    /**
     * 车位编号
     */
    @TableField("space_no")
    private String spaceNo;
    
    /**
     * 完整车位编号
     */
    @TableField("full_space_no")
    private String fullSpaceNo;
    
    /**
     * 车位类型:固定/临时/VIP
     */
    @TableField("space_type")
    private String spaceType;
    
    /**
     * 车位面积(㎡)
     */
    @TableField("space_area")
    private BigDecimal spaceArea;
    
    /**
     * 车位状态:空闲/已租/占用/维修
     */
    @TableField("space_status")
    private String spaceStatus;
    
    /**
     * 关联业主ID
     */
    @TableField("owner_id")
    private Long ownerId;
    
    /**
     * 关联车辆ID
     */
    @TableField("vehicle_id")
    private Long vehicleId;
    
    /**
     * 月租费用
     */
    @TableField("monthly_fee")
    private BigDecimal monthlyFee;
    
    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;
}