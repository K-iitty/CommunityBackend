package com.community.property.service.impl;

import com.community.property.entity.Vehicle;
import com.community.property.mapper.VehicleMapper;
import com.community.property.service.VehicleService;
import com.community.property.service.ImageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.community.property.mapper.ParkingSpaceMapper;
import com.community.property.entity.ParkingSpace;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 车辆服务实现类
 */
@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private ImageService imageService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;

    @Override
    public Map<String, Object> getVehicleDetail(Long vehicleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Vehicle vehicle = vehicleMapper.selectById(vehicleId);
            if (vehicle == null) {
                return null;
            }
            
            // 构建返回数据 - 将图片路径转换为完整URL
            response.put("id", vehicle.getId());
            response.put("createdAt", vehicle.getCreatedAt());
            response.put("updatedAt", vehicle.getUpdatedAt());
            response.put("ownerId", vehicle.getOwnerId());
            response.put("plateNumber", vehicle.getPlateNumber());
            response.put("vehicleType", vehicle.getVehicleType());
            response.put("brand", vehicle.getBrand());
            response.put("model", vehicle.getModel());
            response.put("color", vehicle.getColor());
            response.put("fixedSpaceId", vehicle.getFixedSpaceId());
            response.put("vehicleLicenseNo", vehicle.getVehicleLicenseNo());
            response.put("engineNo", vehicle.getEngineNo());
            response.put("status", vehicle.getStatus());
            response.put("registerDate", vehicle.getRegisterDate());
            response.put("remark", vehicle.getRemark());
            
            // 处理驾驶证图片 - 转换为完整URL
            if (vehicle.getDriverLicenseImage() != null && !vehicle.getDriverLicenseImage().isEmpty()) {
                response.put("driverLicenseImage", imageService.getImageUrl(vehicle.getDriverLicenseImage()));
            }
            
            // 处理车辆图片 - 转换为完整URL（可能是JSON数组或单个路径）
            if (vehicle.getVehicleImages() != null && !vehicle.getVehicleImages().isEmpty()) {
                String vehicleImages = vehicle.getVehicleImages();
                try {
                    // 尝试解析为JSON数组
                    if (vehicleImages.startsWith("[")) {
                        List<String> imageList = objectMapper.readValue(vehicleImages, new TypeReference<List<String>>() {});
                        List<String> fullUrls = new java.util.ArrayList<>();
                        for (String path : imageList) {
                            fullUrls.add(imageService.getImageUrl(path));
                        }
                        response.put("vehicleImages", fullUrls);
                    } else {
                        // 单个字符串，直接转换为URL
                        response.put("vehicleImages", imageService.getImageUrl(vehicleImages));
                    }
                } catch (Exception e) {
                    // 如果是单个字符串，直接转换为URL
                    response.put("vehicleImages", imageService.getImageUrl(vehicleImages));
                }
            }
            
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> updateVehicle(Long vehicleId, String plateNumber, String vehicleType,
            String brand, String model, String color, String vehicleLicenseNo, String engineNo,
            String status, String registerDate, String remark, Long ownerId, Long fixedSpaceId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Vehicle vehicle = vehicleMapper.selectById(vehicleId);
            if (vehicle == null) {
                response.put("success", false);
                response.put("message", "车辆不存在");
                return response;
            }

            // 更新基本字段
            if (plateNumber != null && !plateNumber.isEmpty()) {
                vehicle.setPlateNumber(plateNumber);
            }
            if (vehicleType != null && !vehicleType.isEmpty()) {
                vehicle.setVehicleType(vehicleType);
            }
            if (brand != null && !brand.isEmpty()) {
                vehicle.setBrand(brand);
            }
            if (model != null && !model.isEmpty()) {
                vehicle.setModel(model);
            }
            if (color != null && !color.isEmpty()) {
                vehicle.setColor(color);
            }
            if (vehicleLicenseNo != null && !vehicleLicenseNo.isEmpty()) {
                vehicle.setVehicleLicenseNo(vehicleLicenseNo);
            }
            if (engineNo != null && !engineNo.isEmpty()) {
                vehicle.setEngineNo(engineNo);
            }
            if (status != null && !status.isEmpty()) {
                vehicle.setStatus(status);
            }
            if (registerDate != null && !registerDate.isEmpty()) {
                vehicle.setRegisterDate(java.time.LocalDate.parse(registerDate));
            }
            if (remark != null && !remark.isEmpty()) {
                vehicle.setRemark(remark);
            }
            if (ownerId != null) {
                vehicle.setOwnerId(ownerId);
            }
            if (fixedSpaceId != null) {
                vehicle.setFixedSpaceId(fixedSpaceId);
            }

            int result = vehicleMapper.updateById(vehicle);

            response.put("success", result > 0);
            response.put("message", result > 0 ? "车辆信息更新成功" : "车辆信息更新失败");
            response.put("data", vehicle);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> addVehicleWithImages(Long ownerId, String plateNumber, String vehicleType, 
            String brand, String model, String color, Long fixedSpaceId, String vehicleLicenseNo, 
            String engineNo, String status, String registerDate, String remark,
            MultipartFile driverLicenseImageFile, MultipartFile[] vehicleImageFiles) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证参数
            if (ownerId == null || plateNumber == null || plateNumber.isEmpty()) {
                response.put("success", false);
                response.put("message", "业主ID和车牌号为必需");
                return response;
            }

            Vehicle vehicle = new Vehicle();
            vehicle.setOwnerId(ownerId);
            vehicle.setPlateNumber(plateNumber);
            vehicle.setVehicleType(vehicleType != null ? vehicleType : "小型车");
            vehicle.setBrand(brand);
            vehicle.setModel(model);
            vehicle.setColor(color);
            vehicle.setFixedSpaceId(fixedSpaceId);
            vehicle.setVehicleLicenseNo(vehicleLicenseNo);
            vehicle.setEngineNo(engineNo);
            vehicle.setStatus(status != null ? status : "正常");
            if (registerDate != null && !registerDate.isEmpty()) {
                vehicle.setRegisterDate(java.time.LocalDate.parse(registerDate));
            }
            vehicle.setRemark(remark);

            // 处理驾照照片
            if (driverLicenseImageFile != null && !driverLicenseImageFile.isEmpty()) {
                String driverLicensePath = imageService.uploadImages(
                    Arrays.asList(driverLicenseImageFile), "vehicle/driver_license", ownerId)
                    .stream().findFirst().orElse(null);
                vehicle.setDriverLicenseImage(driverLicensePath);
            }

            // 处理车辆图片（单张图片，保存为字符串）
            if (vehicleImageFiles != null && vehicleImageFiles.length > 0) {
                String vehicleImagePath = imageService.uploadImages(
                    Arrays.asList(vehicleImageFiles[0]), "vehicle/info", ownerId)
                    .stream().findFirst().orElse(null);
                vehicle.setVehicleImages(vehicleImagePath);
            }

            vehicleMapper.insert(vehicle);

            response.put("success", true);
            response.put("message", "车辆添加成功");
            response.put("data", vehicle);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateVehicleWithImages(Long vehicleId, String brand, String model,
            String color, String vehicleType, Long fixedSpaceId, String vehicleLicenseNo, 
            String engineNo, String status, String registerDate, String remark,
            MultipartFile driverLicenseImageFile,
            MultipartFile[] vehicleImageFiles, String driverLicenseImageToDelete, String vehicleImagesToDelete) {

        Map<String, Object> response = new HashMap<>();
        
        try {
            Vehicle vehicle = vehicleMapper.selectById(vehicleId);
            if (vehicle == null) {
                response.put("success", false);
                response.put("message", "车辆不存在");
                return response;
            }

            // 处理驾照照片（单一图片，VARCHAR字段）
            if (driverLicenseImageToDelete != null && !driverLicenseImageToDelete.isEmpty()) {
                // 删除旧驾照照片
                imageService.deleteImage(driverLicenseImageToDelete);
                vehicle.setDriverLicenseImage(null);
            }
            if (driverLicenseImageFile != null && !driverLicenseImageFile.isEmpty()) {
                // 上传新驾照照片
                String newPath = imageService.uploadImages(
                    Arrays.asList(driverLicenseImageFile), "vehicle/driver_license", vehicleId)
                    .stream().findFirst().orElse(null);
                vehicle.setDriverLicenseImage(newPath);
            }

            // 处理车辆图片（单一图片，VARCHAR字段 - 虽然是text，但前端限制1张）
            if (vehicleImagesToDelete != null && !vehicleImagesToDelete.isEmpty()) {
                // 删除旧车辆图片
                imageService.deleteImage(vehicleImagesToDelete);
                vehicle.setVehicleImages(null);
            }
            if (vehicleImageFiles != null && vehicleImageFiles.length > 0) {
                // 上传新车辆图片（仅上传第一张）
                String newPath = imageService.uploadImages(
                    Arrays.asList(vehicleImageFiles[0]), "vehicle/info", vehicleId)
                    .stream().findFirst().orElse(null);
                vehicle.setVehicleImages(newPath);
            }

            // 更新其他字段
            if (brand != null && !brand.isEmpty()) vehicle.setBrand(brand);
            if (model != null && !model.isEmpty()) vehicle.setModel(model);
            if (color != null && !color.isEmpty()) vehicle.setColor(color);
            if (vehicleType != null && !vehicleType.isEmpty()) vehicle.setVehicleType(vehicleType);
            if (fixedSpaceId != null) vehicle.setFixedSpaceId(fixedSpaceId);
            if (vehicleLicenseNo != null && !vehicleLicenseNo.isEmpty()) vehicle.setVehicleLicenseNo(vehicleLicenseNo);
            if (engineNo != null && !engineNo.isEmpty()) vehicle.setEngineNo(engineNo);
            if (status != null && !status.isEmpty()) vehicle.setStatus(status);
            if (registerDate != null && !registerDate.isEmpty()) {
                vehicle.setRegisterDate(java.time.LocalDate.parse(registerDate));
            }
            if (remark != null && !remark.isEmpty()) vehicle.setRemark(remark);

            // 保存到数据库
            vehicleMapper.updateById(vehicle);

            response.put("success", true);
            response.put("message", "车辆信息更新成功");
            response.put("data", vehicle);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> addVehicleBasic(Long ownerId, String plateNumber, String vehicleType,
            String brand, String model, String color, Long fixedSpaceId, String vehicleLicenseNo,
            String engineNo, String status, String registerDate, String remark) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (ownerId == null || plateNumber == null || plateNumber.isEmpty()) {
                response.put("success", false);
                response.put("message", "业主ID和车牌号为必需");
                return response;
            }

            Vehicle vehicle = new Vehicle();
            vehicle.setOwnerId(ownerId);
            vehicle.setPlateNumber(plateNumber);
            vehicle.setVehicleType(vehicleType != null ? vehicleType : "小型车");
            vehicle.setBrand(brand);
            vehicle.setModel(model);
            vehicle.setColor(color);
            vehicle.setFixedSpaceId(fixedSpaceId);
            vehicle.setVehicleLicenseNo(vehicleLicenseNo);
            vehicle.setEngineNo(engineNo);
            vehicle.setStatus(status != null ? status : "正常");
            if (registerDate != null && !registerDate.isEmpty()) {
                vehicle.setRegisterDate(java.time.LocalDate.parse(registerDate));
            }
            vehicle.setRemark(remark);

            vehicleMapper.insert(vehicle);

            response.put("success", true);
            response.put("message", "车辆创建成功");
            response.put("data", vehicle);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> uploadVehicleImages(Long vehicleId, MultipartFile driverLicenseImageFile,
            MultipartFile vehicleImageFile) {
        Map<String, Object> response = new HashMap<>();
        try {
            Vehicle vehicle = vehicleMapper.selectById(vehicleId);
            if (vehicle == null) {
                response.put("success", false);
                response.put("message", "车辆不存在");
                return response;
            }

            // 上传驾照图片
            if (driverLicenseImageFile != null && !driverLicenseImageFile.isEmpty()) {
                String driverLicensePath = imageService.uploadImages(
                        Arrays.asList(driverLicenseImageFile), "vehicle/driver_license", vehicle.getOwnerId())
                        .stream().findFirst().orElse(null);
                vehicle.setDriverLicenseImage(driverLicensePath);
            }

            // 上传车辆图片
            if (vehicleImageFile != null && !vehicleImageFile.isEmpty()) {
                String vehicleImagePath = imageService.uploadImages(
                        Arrays.asList(vehicleImageFile), "vehicle/info", vehicle.getOwnerId())
                        .stream().findFirst().orElse(null);
                vehicle.setVehicleImages(vehicleImagePath);
            }

            vehicleMapper.updateById(vehicle);

            response.put("success", true);
            response.put("message", "图片上传成功");
            response.put("data", vehicle);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> deleteVehicle(Long vehicleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Vehicle vehicle = vehicleMapper.selectById(vehicleId);
            if (vehicle == null) {
                response.put("success", false);
                response.put("message", "车辆不存在");
                return response;
            }

            // 删除车辆图片
            if (vehicle.getDriverLicenseImage() != null && !vehicle.getDriverLicenseImage().isEmpty()) {
                imageService.deleteImage(vehicle.getDriverLicenseImage());
            }
            if (vehicle.getVehicleImages() != null && !vehicle.getVehicleImages().isEmpty()) {
                imageService.deleteImage(vehicle.getVehicleImages());
            }

            // 清空关联的停车位的vehicle_id
            QueryWrapper<ParkingSpace> wrapper = new QueryWrapper<>();
            wrapper.eq("vehicle_id", vehicleId);
            ParkingSpace parkingSpace = new ParkingSpace();
            parkingSpace.setVehicleId(null);
            parkingSpaceMapper.update(parkingSpace, wrapper);

            // 删除车辆记录
            int result = vehicleMapper.deleteById(vehicleId);

            response.put("success", result > 0);
            response.put("message", result > 0 ? "车辆删除成功" : "车辆删除失败");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }
}
