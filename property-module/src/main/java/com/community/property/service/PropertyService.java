package com.community.property.service;

import com.community.property.domain.dto.request.StaffProfileUpdateRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 物业服务综合接口
 */
public interface PropertyService {

    // ==================== 员工个人信息相关 ====================
    
    /**
     * 获取当前员工信息
     */
    Map<String, Object> getStaffProfile(Long staffId);

    /**
     * 更新员工个人信息
     */
    Map<String, Object> updateStaffProfile(Long staffId, StaffProfileUpdateRequest request);

    /**
     * 获取部门信息
     */
    Map<String, Object> getDepartmentInfo(Long departmentId);

    /**
     * 查询所有部门（用于列表选择）
     */
    Map<String, Object> listDepartments();

    // ==================== 社区管理 ====================
    
    /**
     * 获取社区列表（用于仪表配置时的社区选择）
     */
    Map<String, Object> listCommunities(Integer page, Integer size);

    // ==================== 抄表记录相关 ====================
    
    /**
     * 获取仪表详细信息（包含配置和最近读数）
     */
    Map<String, Object> getMeterDetail(Long meterId);
    
    /**
     * 为业主添加仪表
     */
    Map<String, Object> addMeterToOwner(Long communityId, Long houseId, Long meterConfigId, 
            String categoryName, String meterType, String meterCode, String meterSn, String meterName, 
            String installLocation, String installDate, Double initialReading, String commAddress, String remark);

    /**
     * 查询业主仪表列表
     */
    Map<String, Object> listOwnerMeters(Long ownerId, Integer page, Integer size);

    /**
     * 查询所有业主及其仪表列表
     */
    Map<String, Object> getAllOwnersWithMeters(Integer page, Integer size);

    /**
     * 新增仪表配置（支持完整字段）
     */
    Map<String, Object> addMeterConfig(String categoryName, String meterType, String productId, 
            String unit, Double unitPrice, Integer decimalPlaces, String chargeStandard, 
            String calculationMethod, String commProtocol, String remark);

    /**
     * 查询仪表配置列表
     */
    Map<String, Object> listMeterConfigs(String categoryName, Integer page, Integer size);

    /**
     * 添加抄表记录
     */
    Map<String, Object> addMeterReading(Long meterId, Double previousReading, Double currentReading, Long staffId, 
            Double usageAmount, String readingDate, String readingTime, String unit, String readingType, String readingStatus, 
            String abnormalReason, String remark, MultipartFile readingImage);

    /**
     * 查询抄表记录
     */
    Map<String, Object> listMeterReadings(Long meterId, Integer page, Integer size);

    /**
     * 编辑仪表配置
     */
    Map<String, Object> updateMeterConfig(Long configId, String categoryName, String meterType, String productId, 
            String unit, Double unitPrice, Integer decimalPlaces, String chargeStandard, 
            String calculationMethod, String commProtocol, String status, String remark);

    /**
     * 删除仪表配置
     */
    Map<String, Object> deleteMeterConfig(Long configId);

    /**
     * 编辑仪表信息
     */
    Map<String, Object> updateMeterInfo(Long meterId, String meterName, String meterSn, String installLocation, 
            String installDate, String commAddress, String meterStatus, String remark);

    /**
     * 删除仪表
     */
    Map<String, Object> deleteMeterInfo(Long meterId);

    // ==================== 为业主添加车辆和车位 ====================
    
    /**
     * 为业主新增车辆
     */
    Map<String, Object> addVehicle(Long ownerId, String plateNumber, String brand, String model, String color);

    /**
     * 查询业主车辆列表
     */
    Map<String, Object> listOwnerVehicles(Long ownerId, Integer page, Integer size);

    /**
     * 查询所有车辆列表
     */
    Map<String, Object> listAllVehicles(Integer page, Integer size, String keyword);

    /**
     * 新增停车场（支持完整字段）
     */
    Map<String, Object> addParkingLot(Long communityId, String lotName, String lotCode, String lotCategory, 
            String zoneName, String zoneCode, String contactPerson, String contactPhone, String address, 
            String detailAddress, Integer totalSpaces, Integer fixedSpaces, Integer tempSpaces, 
            String businessHours, String chargeMethod, Double monthlyFee, String remark);

    /**
     * 获取停车场详细信息
     */
    Map<String, Object> getParkingLotDetail(Long lotId);

    /**
     * 获取停车位详细信息
     */
    Map<String, Object> getParkingSpaceDetail(Long spaceId);

    /**
     * 新增车位信息（支持完整字段）
     */
    Map<String, Object> addParkingSpace(Long parkingLotId, String spaceNo, String fullSpaceNo, 
            String spaceType, Double spaceArea, String spaceStatus, Long ownerId, Long vehicleId, 
            Double monthlyFee, String remark);

    /**
     * 查询车位列表
     */
    Map<String, Object> listParkingSpaces(Long parkingLotId, Integer page, Integer size);

    /**
     * 查询所有车位列表（支持状态筛选）
     */
    Map<String, Object> listAllParkingSpaces(Integer page, Integer size, String status);

    // ==================== 为业主关联房屋 ====================
    
    /**
     * 为业主关联房屋
     */
    Map<String, Object> associateOwnerToHouse(Long ownerId, Long houseId, String relationship);

    /**
     * 查询业主关联的房屋
     */
    Map<String, Object> listOwnerHouses(Long ownerId);

    /**
     * 查询所有房屋列表
     */
    Map<String, Object> listAllHouses(Integer page, Integer size, String keyword);

    /**
     * 更新房屋信息
     */
    Map<String, Object> updateHouseInfo(Long houseId, String houseType, String houseLayout, String houseOrientation);

    /**
     * 获取房屋列表（用于仪表配置时选择）
     */
    Map<String, Object> getHouseList(Long communityId, Integer page, Integer size);

    // ==================== 公告管理 ====================
    
    /**
     * 新增公告
     */
    Map<String, Object> addNotice(Long communityId, String title, String content, String noticeType);

    /**
     * 修改公告
     */
    Map<String, Object> updateNotice(Long noticeId, String title, String content);

    /**
     * 删除公告
     */
    Map<String, Object> deleteNotice(Long noticeId);

    /**
     * 查询所有公告
     */
    Map<String, Object> listNotices(Long communityId, Integer page, Integer size);

    /**
     * 获取公告详情
     */
    Map<String, Object> getNoticeDetail(Long noticeId);
}
