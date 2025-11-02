package com.community.property.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface VehicleService {
    /**
     * 获取车辆详细信息（包含业主和停车位信息）
     */
    Map<String, Object> getVehicleDetail(Long vehicleId);

    /**
     * 第一步：创建车辆记录（不含图片）
     */
    Map<String, Object> addVehicleBasic(Long ownerId, String plateNumber, String vehicleType, 
            String brand, String model, String color, Long fixedSpaceId, String vehicleLicenseNo, 
            String engineNo, String status, String registerDate, String remark);

    /**
     * 第二步：为已创建的车辆上传图片
     */
    Map<String, Object> uploadVehicleImages(Long vehicleId, MultipartFile driverLicenseImageFile,
            MultipartFile vehicleImageFile);

    /**
     * 新增车辆（支持完整字段和图片上传）
     */
    Map<String, Object> addVehicleWithImages(Long ownerId, String plateNumber, String vehicleType, 
            String brand, String model, String color, Long fixedSpaceId, String vehicleLicenseNo, 
            String engineNo, String status, String registerDate, String remark,
            MultipartFile driverLicenseImageFile, MultipartFile[] vehicleImageFiles);

    /**
     * 更新车辆基本信息（不包含图片）
     */
    Map<String, Object> updateVehicle(Long vehicleId, String plateNumber, String vehicleType, 
            String brand, String model, String color, String vehicleLicenseNo, String engineNo, 
            String status, String registerDate, String remark, Long ownerId, Long fixedSpaceId);

    /**
     * 更新车辆信息（支持完整字段和图片上传）
     */
    Map<String, Object> updateVehicleWithImages(Long vehicleId, String brand, String model, String color, 
            String vehicleType, Long fixedSpaceId, String vehicleLicenseNo, String engineNo, String status,
            String registerDate, String remark, MultipartFile driverLicenseImageFile, 
            MultipartFile[] vehicleImageFiles, String driverLicenseImageToDelete, String vehicleImagesToDelete);

    /**
     * 删除车辆
     */
    Map<String, Object> deleteVehicle(Long vehicleId);
}
