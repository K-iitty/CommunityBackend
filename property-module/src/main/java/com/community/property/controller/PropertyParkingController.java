package com.community.property.controller;

import com.community.property.service.PropertyService;
import com.community.property.service.RedisMessageService;
import com.community.property.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 物业停车位管理控制器
 * 负责所有停车位相关的CRUD操作
 */
@RestController
@RequestMapping("/api/property/parking")
@Tag(name = "物业停车位管理", description = "物业端的停车位信息管理接口")
public class PropertyParkingController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisMessageService redisMessageService;

    /**
     * 获取停车场详细信息（包含社区信息）
     */
    @GetMapping("/lot/{lotId}")
    @Operation(summary = "获取停车场详细信息", description = "获取停车场完整信息，包含所属社区、计费配置等")
    public Map<String, Object> getParkingLotDetail(
            @Parameter(description = "停车场ID", required = true)
            @PathVariable Long lotId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用service获取停车场详情
            Map<String, Object> lotDetail = propertyService.getParkingLotDetail(lotId);
            response.put("success", true);
            response.put("data", lotDetail);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取停车位详细信息
     */
    @GetMapping("/space/{spaceId}")
    @Operation(summary = "获取停车位详细信息", description = "获取停车位完整信息，包含停车场、业主、车辆等关联信息")
    public Map<String, Object> getParkingSpaceDetail(
            @Parameter(description = "停车位ID", required = true)
            @PathVariable Long spaceId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用service获取停车位详情
            Map<String, Object> spaceDetail = propertyService.getParkingSpaceDetail(spaceId);
            response.put("success", true);
            response.put("data", spaceDetail);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 新增停车场（支持完整字段）
     */
    @PostMapping("/lot/add")
    @Operation(summary = "新增停车场", description = "为社区新增停车场，支持配置收费标准等")
    public Map<String, Object> addParkingLot(
            @Parameter(description = "社区ID", required = true)
            @RequestParam Long communityId,
            @Parameter(description = "停车场名称", required = true)
            @RequestParam String lotName,
            @Parameter(description = "停车场编码", required = false)
            @RequestParam(required = false) String lotCode,
            @Parameter(description = "车库类别", required = false)
            @RequestParam(required = false) String lotCategory,
            @Parameter(description = "区域名称", required = false)
            @RequestParam(required = false) String zoneName,
            @Parameter(description = "区域编码", required = false)
            @RequestParam(required = false) String zoneCode,
            @Parameter(description = "联系人", required = false)
            @RequestParam(required = false) String contactPerson,
            @Parameter(description = "联系电话", required = false)
            @RequestParam(required = false) String contactPhone,
            @Parameter(description = "地址", required = false)
            @RequestParam(required = false) String address,
            @Parameter(description = "详细地址", required = false)
            @RequestParam(required = false) String detailAddress,
            @Parameter(description = "总车位数", required = true)
            @RequestParam Integer totalSpaces,
            @Parameter(description = "固定车位数", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer fixedSpaces,
            @Parameter(description = "临时车位数", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer tempSpaces,
            @Parameter(description = "营业时间", required = false)
            @RequestParam(required = false) String businessHours,
            @Parameter(description = "计费方式", required = false)
            @RequestParam(required = false) String chargeMethod,
            @Parameter(description = "月租费用", required = false)
            @RequestParam(required = false) Double monthlyFee,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return propertyService.addParkingLot(
                    communityId, lotName, lotCode, lotCategory, zoneName, zoneCode,
                    contactPerson, contactPhone, address, detailAddress,
                    totalSpaces, fixedSpaces, tempSpaces, businessHours,
                    chargeMethod, monthlyFee, remark);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "新增停车场失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 新增车位（支持完整字段）
     */
    @PostMapping("/space/add")
    @Operation(summary = "新增车位", description = "在指定停车场新增车位")
    public Map<String, Object> addParkingSpace(
            @Parameter(description = "停车场ID", required = true)
            @RequestParam Long parkingLotId,
            @Parameter(description = "车位编号", required = true)
            @RequestParam String spaceNo,
            @Parameter(description = "完整车位编号", required = false)
            @RequestParam(required = false) String fullSpaceNo,
            @Parameter(description = "车位类型", required = false)
            @RequestParam(required = false, defaultValue = "固定") String spaceType,
            @Parameter(description = "车位面积", required = false)
            @RequestParam(required = false) Double spaceArea,
            @Parameter(description = "车位状态", required = false)
            @RequestParam(required = false, defaultValue = "空闲") String spaceStatus,
            @Parameter(description = "业主ID", required = false)
            @RequestParam(required = false) Long ownerId,
            @Parameter(description = "车辆ID", required = false)
            @RequestParam(required = false) Long vehicleId,
            @Parameter(description = "月租费用", required = false)
            @RequestParam(required = false) Double monthlyFee,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.addParkingSpace(
                parkingLotId, spaceNo, fullSpaceNo, spaceType, spaceArea,
                spaceStatus, ownerId, vehicleId, monthlyFee, remark);
    }

    /**
     * 查询车位列表
     */
    @GetMapping("/spaces/{parkingLotId}")
    @Operation(summary = "查询车位列表", description = "查询停车场的所有车位")
    public Map<String, Object> listParkingSpaces(
            @Parameter(description = "停车场ID", required = true)
            @PathVariable Long parkingLotId,
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.listParkingSpaces(parkingLotId, page, size);
    }

    /**
     * 查询所有车位列表（支持状态筛选）
     */
    @GetMapping("/spaces")
    @Operation(summary = "查询所有车位列表", description = "查询所有停车场的车位，支持按状态筛选")
    public Map<String, Object> listAllParkingSpaces(
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "车位状态（空闲/已租/占用/维修）", required = false)
            @RequestParam(required = false) String status,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.listAllParkingSpaces(page, size, status);
    }
}
