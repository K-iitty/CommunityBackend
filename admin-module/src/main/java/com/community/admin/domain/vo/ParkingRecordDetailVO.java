package com.community.admin.domain.vo;

import com.community.admin.domain.entity.ParkingRecord;
import com.community.admin.domain.entity.Vehicle;
import com.community.admin.domain.entity.Owner;
import com.community.admin.domain.entity.ParkingSpace;
import com.community.admin.domain.entity.ParkingLot;
import lombok.Data;

import java.io.Serializable;

/**
 * 停车记录详细信息VO类
 */
@Data
public class ParkingRecordDetailVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 停车记录信息
     */
    private ParkingRecord parkingRecord;
    
    /**
     * 车辆信息
     */
    private Vehicle vehicle;
    
    /**
     * 业主信息
     */
    private Owner owner;
    
    /**
     * 车位信息
     */
    private ParkingSpace parkingSpace;
    
    /**
     * 停车场信息
     */
    private ParkingLot parkingLot;
}