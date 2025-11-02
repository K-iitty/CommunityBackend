package com.community.property.controller;

import com.community.property.service.VehicleService;
import com.community.property.service.PropertyService;
import com.community.property.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 物业车辆管理控制器
 * 负责所有车辆相关的CRUD操作
 */
@RestController
@RequestMapping("/api/property/vehicles")
@Tag(name = "物业车辆管理", description = "物业端的车辆信息管理接口")
public class PropertyVehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取车辆详细信息
     */
    @GetMapping("/{vehicleId}")
    @Operation(summary = "获取车辆详细信息", description = "获取指定车辆的完整信息，包括业主信息和停车位信息")
    public Map<String, Object> getVehicleDetail(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用service获取完整车辆详情（包含业主、停车位、图片等）
            Map<String, Object> vehicleDetail = vehicleService.getVehicleDetail(vehicleId);
            response.put("success", true);
            response.put("data", vehicleDetail);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 更新车辆基本信息（不包含图片）
     */
    @PostMapping("/{vehicleId}/update")
    @Operation(summary = "更新车辆基本信息", description = "更新车辆的基本信息（不包含图片）")
    public Map<String, Object> updateVehicle(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "车牌号", required = false)
            @RequestParam(required = false) String plateNumber,
            @Parameter(description = "车辆类型", required = false)
            @RequestParam(required = false) String vehicleType,
            @Parameter(description = "品牌", required = false)
            @RequestParam(required = false) String brand,
            @Parameter(description = "型号", required = false)
            @RequestParam(required = false) String model,
            @Parameter(description = "颜色", required = false)
            @RequestParam(required = false) String color,
            @Parameter(description = "行驶证号", required = false)
            @RequestParam(required = false) String vehicleLicenseNo,
            @Parameter(description = "发动机号", required = false)
            @RequestParam(required = false) String engineNo,
            @Parameter(description = "状态", required = false)
            @RequestParam(required = false) String status,
            @Parameter(description = "登记日期", required = false)
            @RequestParam(required = false) String registerDate,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "业主ID", required = false)
            @RequestParam(required = false) Long ownerId,
            @Parameter(description = "固定车位ID", required = false)
            @RequestParam(required = false) Long fixedSpaceId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return vehicleService.updateVehicle(vehicleId, plateNumber, vehicleType, brand, model, 
                    color, vehicleLicenseNo, engineNo, status, registerDate, remark, ownerId, fixedSpaceId);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新车辆失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 为业主添加车辆（支持完整字段）
     */
    @PostMapping("/add")
    @Operation(summary = "为业主添加车辆", description = "为指定业主新增车辆信息，支持图片上传")
    public Map<String, Object> addVehicle(
            @Parameter(description = "业主ID", required = true)
            @RequestParam Long ownerId,
            @Parameter(description = "车牌号", required = true)
            @RequestParam String plateNumber,
            @Parameter(description = "车辆类型", required = false)
            @RequestParam(required = false) String vehicleType,
            @Parameter(description = "品牌", required = false)
            @RequestParam(required = false) String brand,
            @Parameter(description = "型号", required = false)
            @RequestParam(required = false) String model,
            @Parameter(description = "颜色", required = false)
            @RequestParam(required = false) String color,
            @Parameter(description = "固定车位ID", required = false)
            @RequestParam(required = false) Long fixedSpaceId,
            @Parameter(description = "行驶证号", required = false)
            @RequestParam(required = false) String vehicleLicenseNo,
            @Parameter(description = "发动机号", required = false)
            @RequestParam(required = false) String engineNo,
            @Parameter(description = "状态", required = false)
            @RequestParam(required = false, defaultValue = "正常") String status,
            @Parameter(description = "登记日期", required = false)
            @RequestParam(required = false) String registerDate,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "驾照照片", required = false)
            @RequestParam(value = "driverLicenseImageFile", required = false) MultipartFile driverLicenseImageFile,
            @Parameter(description = "车辆图片列表", required = false)
            @RequestParam(value = "vehicleImageFiles", required = false) MultipartFile[] vehicleImageFiles,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用service添加车辆
            Map<String, Object> result = vehicleService.addVehicleWithImages(
                    ownerId, plateNumber, vehicleType, brand, model, color, 
                    fixedSpaceId, vehicleLicenseNo, engineNo, status, registerDate, remark,
                    driverLicenseImageFile, vehicleImageFiles);
            return result;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加车辆失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 一次性提交完整的车辆信息（包含所有字段和图片）
     * 这是推荐的新增车辆方式，确保原子性
     */
    @PostMapping("/add-complete")
    @Operation(summary = "完整新增车辆信息", description = "一次性提交完整的车辆信息包括图片，确保原子性")
    public Map<String, Object> addVehicleComplete(
            @Parameter(description = "业主ID", required = true)
            @RequestParam Long ownerId,
            @Parameter(description = "车牌号", required = true)
            @RequestParam String plateNumber,
            @Parameter(description = "车辆类型", required = false)
            @RequestParam(required = false) String vehicleType,
            @Parameter(description = "品牌", required = false)
            @RequestParam(required = false) String brand,
            @Parameter(description = "型号", required = false)
            @RequestParam(required = false) String model,
            @Parameter(description = "颜色", required = false)
            @RequestParam(required = false) String color,
            @Parameter(description = "固定车位ID", required = false)
            @RequestParam(required = false) Long fixedSpaceId,
            @Parameter(description = "行驶证号", required = false)
            @RequestParam(required = false) String vehicleLicenseNo,
            @Parameter(description = "发动机号", required = false)
            @RequestParam(required = false) String engineNo,
            @Parameter(description = "状态", required = false)
            @RequestParam(required = false, defaultValue = "正常") String status,
            @Parameter(description = "登记日期", required = false)
            @RequestParam(required = false) String registerDate,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "驾照照片", required = false)
            @RequestParam(value = "driverLicenseImageFile", required = false) MultipartFile driverLicenseImageFile,
            @Parameter(description = "车辆图片", required = false)
            @RequestParam(value = "vehicleImageFile", required = false) MultipartFile vehicleImageFile,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 构建MultipartFile数组用于兼容addVehicleWithImages
            MultipartFile[] vehicleImageFiles = null;
            if (vehicleImageFile != null && !vehicleImageFile.isEmpty()) {
                vehicleImageFiles = new MultipartFile[]{vehicleImageFile};
            }
            
            // 调用service添加车辆
            Map<String, Object> result = vehicleService.addVehicleWithImages(
                    ownerId, plateNumber, vehicleType, brand, model, color, 
                    fixedSpaceId, vehicleLicenseNo, engineNo, status, registerDate, remark,
                    driverLicenseImageFile, vehicleImageFiles);
            return result;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加车辆失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 第一步：创建车辆记录（不含图片）
     */
    @PostMapping("/add-basic")
    @Operation(summary = "创建车辆记录", description = "第一步：创建车辆基本信息")
    public Map<String, Object> addVehicleBasic(
            @Parameter(description = "业主ID", required = true)
            @RequestParam Long ownerId,
            @Parameter(description = "车牌号", required = true)
            @RequestParam String plateNumber,
            @Parameter(description = "车辆类型", required = false)
            @RequestParam(required = false) String vehicleType,
            @Parameter(description = "品牌", required = false)
            @RequestParam(required = false) String brand,
            @Parameter(description = "型号", required = false)
            @RequestParam(required = false) String model,
            @Parameter(description = "颜色", required = false)
            @RequestParam(required = false) String color,
            @Parameter(description = "固定车位ID", required = false)
            @RequestParam(required = false) Long fixedSpaceId,
            @Parameter(description = "行驶证号", required = false)
            @RequestParam(required = false) String vehicleLicenseNo,
            @Parameter(description = "发动机号", required = false)
            @RequestParam(required = false) String engineNo,
            @Parameter(description = "状态", required = false)
            @RequestParam(required = false, defaultValue = "正常") String status,
            @Parameter(description = "登记日期", required = false)
            @RequestParam(required = false) String registerDate,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = vehicleService.addVehicleBasic(
                    ownerId, plateNumber, vehicleType, brand, model, color,
                    fixedSpaceId, vehicleLicenseNo, engineNo, status, registerDate, remark);
            return result;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 第二步：为已创建的车辆上传图片
     */
    @PostMapping("/{id}/upload-images")
    @Operation(summary = "上传车辆图片", description = "第二步：为车辆上传驾照和车辆照片")
    public Map<String, Object> uploadVehicleImages(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "驾照照片", required = false)
            @RequestParam(value = "driverLicenseImageFile", required = false) MultipartFile driverLicenseImageFile,
            @Parameter(description = "车辆图片", required = false)
            @RequestParam(value = "vehicleImageFile", required = false) MultipartFile vehicleImageFile,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = vehicleService.uploadVehicleImages(
                    id, driverLicenseImageFile, vehicleImageFile);
            return result;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "上传失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 查询所有车辆列表
     */
    @GetMapping("")
    @Operation(summary = "查询所有车辆列表", description = "分页查询所有车辆信息")
    public Map<String, Object> listAllVehicles(
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词（车牌号或品牌）", required = false)
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用propertyService的listAllVehicles方法
            return propertyService.listAllVehicles(page, size, keyword);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询车辆列表失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 查询业主车辆列表
     */
    @GetMapping("/{ownerId}/list")
    @Operation(summary = "查询业主车辆列表", description = "查询业主的所有车辆信息")
    public Map<String, Object> listOwnerVehicles(
            @Parameter(description = "业主ID", required = true)
            @PathVariable Long ownerId,
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用propertyService的listOwnerVehicles方法
            return propertyService.listOwnerVehicles(ownerId, page, size);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询车辆列表失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 更新车辆信息（包含图片上传）
     */
    @PostMapping("/{vehicleId}/update-with-images")
    @Operation(summary = "更新车辆信息（包含图片上传）", description = "更新车辆信息并支持上传或删除驾照和车辆图片")
    public Map<String, Object> updateVehicleWithImages(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "品牌", required = false)
            @RequestParam(required = false) String brand,
            @Parameter(description = "型号", required = false)
            @RequestParam(required = false) String model,
            @Parameter(description = "颜色", required = false)
            @RequestParam(required = false) String color,
            @Parameter(description = "车辆类型", required = false)
            @RequestParam(required = false) String vehicleType,
            @Parameter(description = "固定车位ID", required = false)
            @RequestParam(required = false) Long fixedSpaceId,
            @Parameter(description = "行驶证号", required = false)
            @RequestParam(required = false) String vehicleLicenseNo,
            @Parameter(description = "发动机号", required = false)
            @RequestParam(required = false) String engineNo,
            @Parameter(description = "状态", required = false)
            @RequestParam(required = false) String status,
            @Parameter(description = "登记日期", required = false)
            @RequestParam(required = false) String registerDate,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "驾照照片", required = false)
            @RequestParam(value = "driverLicenseImageFile", required = false) MultipartFile driverLicenseImageFile,
            @Parameter(description = "车辆图片列表", required = false)
            @RequestParam(value = "vehicleImageFiles", required = false) MultipartFile[] vehicleImageFiles,
            @Parameter(description = "要删除的驾照照片地址", required = false)
            @RequestParam(required = false) String driverLicenseImageToDelete,
            @Parameter(description = "要删除的车辆图片地址", required = false)
            @RequestParam(required = false) String vehicleImagesToDelete,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return vehicleService.updateVehicleWithImages(vehicleId, brand, model, color, vehicleType,
                fixedSpaceId, vehicleLicenseNo, engineNo, status, registerDate, remark,
                driverLicenseImageFile, vehicleImageFiles, driverLicenseImageToDelete, vehicleImagesToDelete);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{vehicleId}/delete")
    @Operation(summary = "删除车辆", description = "删除指定的车辆信息及其关联的图片")
    public Map<String, Object> deleteVehicle(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return vehicleService.deleteVehicle(vehicleId);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }
}
