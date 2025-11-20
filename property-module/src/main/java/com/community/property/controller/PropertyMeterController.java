package com.community.property.controller;

import com.community.property.service.PropertyService;
import com.community.property.service.RedisMessageService;
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
 * 物业仪表管理控制器
 * 负责所有仪表相关的CRUD操作（仪表配置、为业主添加仪表、抄表记录等）
 */
@RestController
@RequestMapping("/api/property/meter")
@Tag(name = "物业仪表管理", description = "物业端的仪表配置、抄表记录等管理接口")
public class PropertyMeterController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisMessageService redisMessageService;

    /**
     * 获取仪表详细信息（包含配置信息）
     */
    @GetMapping("/{meterId}")
    @Operation(summary = "获取仪表详细信息", description = "获取仪表完整信息，包含配置、最近读数等")
    public Map<String, Object> getMeterDetail(
            @Parameter(description = "仪表ID", required = true)
            @PathVariable Long meterId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用service获取仪表详情
            Map<String, Object> meterDetail = propertyService.getMeterDetail(meterId);
            response.put("success", true);
            response.put("data", meterDetail);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 为业主添加仪表（支持完整字段）
     */
    @PostMapping("/add-to-owner")
    @Operation(summary = "为业主添加仪表", description = "为指定房屋添加仪表信息")
    public Map<String, Object> addMeterToOwner(
            @Parameter(description = "社区ID", required = true)
            @RequestParam Long communityId,
            @Parameter(description = "房屋ID", required = true)
            @RequestParam Long houseId,
            @Parameter(description = "仪表配置ID", required = true)
            @RequestParam Long meterConfigId,
            @Parameter(description = "分类名称", required = true)
            @RequestParam String categoryName,
            @Parameter(description = "仪表种类", required = true)
            @RequestParam String meterType,
            @Parameter(description = "仪表编码", required = true)
            @RequestParam String meterCode,
            @Parameter(description = "仪表名称", required = true)
            @RequestParam String meterName,
            @Parameter(description = "仪表序列号", required = false)
            @RequestParam(required = false) String meterSn,
            @Parameter(description = "安装位置", required = false)
            @RequestParam(required = false) String installLocation,
            @Parameter(description = "安装日期", required = false)
            @RequestParam(required = false) String installDate,
            @Parameter(description = "起始读数", required = false)
            @RequestParam(required = false, defaultValue = "0") Double initialReading,
            @Parameter(description = "通信地址", required = false)
            @RequestParam(required = false) String commAddress,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.addMeterToOwner(
                communityId, houseId, meterConfigId, categoryName, meterType, 
                meterCode, meterSn, meterName,
                installLocation, installDate, initialReading, commAddress, remark);
    }

    /**
     * 查询业主仪表列表
     */
    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "查询业主仪表列表", description = "查询业主关联的所有仪表")
    public Map<String, Object> listOwnerMeters(
            @Parameter(description = "业主ID", required = true)
            @PathVariable Long ownerId,
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.listOwnerMeters(ownerId, page, size);
    }

    /**
     * 查询所有业主及其仪表列表（分页）
     */
    @GetMapping("/owner-list")
    @Operation(summary = "查询所有业主仪表列表", description = "分页查询所有业主及其仪表信息")
    public Map<String, Object> getMeterOwnerList(
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.getAllOwnersWithMeters(page, size);
    }

    /**
     * 添加仪表配置
     */
    @PostMapping("/config/add")
    @Operation(summary = "添加仪表配置", description = "新增仪表配置信息")
    public Map<String, Object> addMeterConfig(
            @Parameter(description = "分类名称", required = true)
            @RequestParam String categoryName,
            @Parameter(description = "仪表种类", required = true)
            @RequestParam String meterType,
            @Parameter(description = "产品ID", required = false)
            @RequestParam(required = false) String productId,
            @Parameter(description = "计量单位", required = true)
            @RequestParam String unit,
            @Parameter(description = "单价", required = true)
            @RequestParam Double unitPrice,
            @Parameter(description = "小数位数", required = false)
            @RequestParam(required = false, defaultValue = "2") Integer decimalPlaces,
            @Parameter(description = "收费标准", required = false)
            @RequestParam(required = false) String chargeStandard,
            @Parameter(description = "计算方式", required = false)
            @RequestParam(required = false) String calculationMethod,
            @Parameter(description = "通信协议", required = false)
            @RequestParam(required = false) String commProtocol,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.addMeterConfig(
                categoryName, meterType, productId, unit, unitPrice,
                decimalPlaces, chargeStandard, calculationMethod, commProtocol, remark);
    }

    /**
     * 查询仪表配置列表
     */
    @GetMapping("/configs")
    @Operation(summary = "查询仪表配置列表", description = "查询所有启用的仪表配置")
    public Map<String, Object> listMeterConfigs(
            @Parameter(description = "分类名称", required = false)
            @RequestParam(required = false) String categoryName,
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.listMeterConfigs(categoryName, page, size);
    }

    /**
     * 添加抄表记录（支持图片上传）
     */
    @PostMapping("/reading/add")
    @Operation(summary = "添加抄表记录", description = "新增仪表抄表记录，支持图片上传")
    public Map<String, Object> addMeterReading(
            @Parameter(description = "仪表ID", required = true)
            @RequestParam Long meterId,
            @Parameter(description = "上次读数", required = true)
            @RequestParam Double previousReading,
            @Parameter(description = "当前读数", required = true)
            @RequestParam Double currentReading,
            @Parameter(description = "抄表日期", required = true)
            @RequestParam String readingDate,
            @Parameter(description = "用量", required = true)
            @RequestParam Double usageAmount,
            @Parameter(description = "抄表时间", required = false)
            @RequestParam(required = false) String readingTime,
            @Parameter(description = "计量单位", required = false)
            @RequestParam(required = false) String unit,
            @Parameter(description = "抄表类型", required = false)
            @RequestParam(required = false, defaultValue = "手动") String readingType,
            @Parameter(description = "抄表状态", required = false)
            @RequestParam(required = false, defaultValue = "正常") String readingStatus,
            @Parameter(description = "异常原因", required = false)
            @RequestParam(required = false) String abnormalReason,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "抄表图片", required = false)
            @RequestParam(value = "readingImage", required = false) MultipartFile readingImage,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        String realToken = token.replace("Bearer ", "");
        Long staffId = jwtUtil.getStaffIdFromToken(realToken);

        return propertyService.addMeterReading(
                meterId, previousReading, currentReading, staffId, usageAmount, readingDate, readingTime,
                unit, readingType, readingStatus, abnormalReason, remark, readingImage);
    }

    /**
     * 查询抄表记录
     */
    @GetMapping("/readings/{meterId}")
    @Operation(summary = "查询抄表记录", description = "查询指定仪表的抄表历史记录")
    public Map<String, Object> listMeterReadings(
            @Parameter(description = "仪表ID", required = true)
            @PathVariable Long meterId,
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.listMeterReadings(meterId, page, size);
    }

    /**
     * 编辑仪表配置
     */
    @PutMapping("/config/{configId}/update")
    @Operation(summary = "编辑仪表配置", description = "修改已存在的仪表配置信息")
    public Map<String, Object> updateMeterConfig(
            @Parameter(description = "配置ID", required = true)
            @PathVariable Long configId,
            @Parameter(description = "分类名称", required = false)
            @RequestParam(required = false) String categoryName,
            @Parameter(description = "仪表种类", required = false)
            @RequestParam(required = false) String meterType,
            @Parameter(description = "产品ID", required = false)
            @RequestParam(required = false) String productId,
            @Parameter(description = "计量单位", required = false)
            @RequestParam(required = false) String unit,
            @Parameter(description = "单价", required = false)
            @RequestParam(required = false) Double unitPrice,
            @Parameter(description = "小数位数", required = false)
            @RequestParam(required = false) Integer decimalPlaces,
            @Parameter(description = "收费标准", required = false)
            @RequestParam(required = false) String chargeStandard,
            @Parameter(description = "计算方式", required = false)
            @RequestParam(required = false) String calculationMethod,
            @Parameter(description = "通信协议", required = false)
            @RequestParam(required = false) String commProtocol,
            @Parameter(description = "状态", required = false)
            @RequestParam(required = false) String status,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.updateMeterConfig(configId, categoryName, meterType, productId, unit, 
                unitPrice, decimalPlaces, chargeStandard, calculationMethod, commProtocol, status, remark);
    }

    /**
     * 删除仪表配置
     */
    @DeleteMapping("/config/{configId}/delete")
    @Operation(summary = "删除仪表配置", description = "删除指定的仪表配置")
    public Map<String, Object> deleteMeterConfig(
            @Parameter(description = "配置ID", required = true)
            @PathVariable Long configId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.deleteMeterConfig(configId);
    }

    /**
     * 编辑仪表信息
     */
    @PutMapping("/{meterId}/update")
    @Operation(summary = "编辑仪表信息", description = "修改已存在的仪表信息")
    public Map<String, Object> updateMeterInfo(
            @Parameter(description = "仪表ID", required = true)
            @PathVariable Long meterId,
            @Parameter(description = "仪表名称", required = false)
            @RequestParam(required = false) String meterName,
            @Parameter(description = "仪表序列号", required = false)
            @RequestParam(required = false) String meterSn,
            @Parameter(description = "安装位置", required = false)
            @RequestParam(required = false) String installLocation,
            @Parameter(description = "安装日期", required = false)
            @RequestParam(required = false) String installDate,
            @Parameter(description = "通信地址", required = false)
            @RequestParam(required = false) String commAddress,
            @Parameter(description = "仪表状态", required = false)
            @RequestParam(required = false) String meterStatus,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.updateMeterInfo(meterId, meterName, meterSn, installLocation, 
                installDate, commAddress, meterStatus, remark);
    }

    /**
     * 删除仪表
     */
    @DeleteMapping("/{meterId}/delete")
    @Operation(summary = "删除仪表", description = "删除指定的仪表")
    public Map<String, Object> deleteMeterInfo(
            @Parameter(description = "仪表ID", required = true)
            @PathVariable Long meterId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.deleteMeterInfo(meterId);
    }

    /**
     * 获取社区列表（用于仪表配置页面的社区选择）
     */
    @GetMapping("/communities")
    @Operation(summary = "获取社区列表", description = "获取所有社区列表，用于仪表配置时选择")
    public Map<String, Object> getCommunityList(
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.listCommunities(page, size);
    }

    /**
     * 获取房屋列表（用于仪表配置页面的房屋选择）
     */
    @GetMapping("/houses")
    @Operation(summary = "获取房屋列表", description = "获取社区内的房屋列表，用于仪表配置时选择")
    public Map<String, Object> getHouseList(
            @Parameter(description = "社区ID", required = false)
            @RequestParam(required = false) Long communityId,
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.getHouseList(communityId, page, size);
    }
}
