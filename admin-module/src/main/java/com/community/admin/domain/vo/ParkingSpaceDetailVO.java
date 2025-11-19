package com.community.admin.domain.vo;

import com.community.admin.domain.entity.ParkingSpace;
import com.community.admin.domain.entity.ParkingLot;
import lombok.Data;

import java.io.Serializable;

/**
 * 车位详细信息VO类
 */
@Data
public class ParkingSpaceDetailVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 车位信息
     */
    private ParkingSpace parkingSpace;
    
    /**
     * 停车场信息
     */
    private ParkingLot parkingLot;
}