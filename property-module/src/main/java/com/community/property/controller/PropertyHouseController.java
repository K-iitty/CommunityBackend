package com.community.property.controller;

import com.community.property.service.HouseService;
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
 * 物业房屋管理控制器
 * 负责所有房屋相关的CRUD操作
 */
@RestController
@RequestMapping("/api/property/houses")
@Tag(name = "物业房屋管理", description = "物业端的房屋信息管理接口")
public class PropertyHouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取房屋详细信息（包含社区和楼栋信息）
     */
    @GetMapping("/{houseId}")
    @Operation(summary = "获取房屋详细信息", description = "获取房屋完整信息，包含所属社区、楼栋、业主关联等")
    public Map<String, Object> getHouseDetail(
            @Parameter(description = "房屋ID", required = true)
            @PathVariable Long houseId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用service获取完整房屋详情
            Map<String, Object> houseDetail = houseService.getHouseDetail(houseId);
            response.put("success", true);
            response.put("data", houseDetail);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 查询所有房屋列表
     */
    @GetMapping("")
    @Operation(summary = "查询所有房屋列表", description = "分页查询所有房屋信息")
    public Map<String, Object> listAllHouses(
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词（房间号或房屋编码）", required = false)
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 调用propertyService的listAllHouses方法
            return propertyService.listAllHouses(page, size, keyword);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询房屋列表失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 新增房屋（支持完整字段）
     */
    @PostMapping("/add")
    @Operation(summary = "新增房屋", description = "为社区新增房屋，支持户型图上传")
    public Map<String, Object> addHouse(
            @Parameter(description = "社区ID", required = true)
            @RequestParam Long communityId,
            @Parameter(description = "楼栋ID", required = true)
            @RequestParam Long buildingId,
            @Parameter(description = "房间号", required = true)
            @RequestParam String roomNo,
            @Parameter(description = "完整房间号", required = false)
            @RequestParam(required = false) String fullRoomNo,
            @Parameter(description = "房屋编码", required = false)
            @RequestParam(required = false) String houseCode,
            @Parameter(description = "建筑面积", required = true)
            @RequestParam Double buildingArea,
            @Parameter(description = "使用面积", required = false)
            @RequestParam(required = false) Double usableArea,
            @Parameter(description = "公摊面积", required = false)
            @RequestParam(required = false) Double sharedArea,
            @Parameter(description = "房屋类型", required = true)
            @RequestParam String houseType,
            @Parameter(description = "房屋户型", required = false)
            @RequestParam(required = false) String houseLayout,
            @Parameter(description = "房屋朝向", required = false)
            @RequestParam(required = false) String houseOrientation,
            @Parameter(description = "车位号", required = false)
            @RequestParam(required = false) String parkingSpaceNo,
            @Parameter(description = "车位类型", required = false)
            @RequestParam(required = false) String parkingType,
            @Parameter(description = "房屋状态", required = false)
            @RequestParam(required = false, defaultValue = "空置") String houseStatus,
            @Parameter(description = "装修状态", required = false)
            @RequestParam(required = false) String decorationStatus,
            @Parameter(description = "楼层", required = false)
            @RequestParam(required = false) Integer floorLevel,
            @Parameter(description = "是否有阳台", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer hasBalcony,
            @Parameter(description = "是否有花园", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer hasGarden,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "户型图文件", required = false)
            @RequestParam(value = "floorPlanImageFile", required = false) MultipartFile floorPlanImageFile,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return houseService.addHouseWithImage(
                    communityId, buildingId, roomNo, fullRoomNo, houseCode,
                    buildingArea, usableArea, sharedArea, houseType, houseLayout, houseOrientation,
                    parkingSpaceNo, parkingType, houseStatus, decorationStatus, floorLevel,
                    hasBalcony, hasGarden, remark, floorPlanImageFile);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "新增房屋失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 查询业主关联的房屋
     */
    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "查询业主关联的房屋", description = "查询业主关联的所有房屋")
    public Map<String, Object> listOwnerHouses(
            @Parameter(description = "业主ID", required = true)
            @PathVariable Long ownerId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.listOwnerHouses(ownerId);
    }

    /**
     * 为业主关联房屋
     */
    @PostMapping("/associate")
    @Operation(summary = "为业主关联房屋", description = "将房屋与业主关联")
    public Map<String, Object> associateOwnerToHouse(
            @Parameter(description = "业主ID", required = true)
            @RequestParam Long ownerId,
            @Parameter(description = "房屋ID", required = true)
            @RequestParam Long houseId,
            @Parameter(description = "关系类型", required = true)
            @RequestParam String relationship,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return propertyService.associateOwnerToHouse(ownerId, houseId, relationship);
    }

    /**
     * 更新房屋信息（包含户型图上传）
     */
    @PostMapping("/{houseId}/update-with-images")
    @Operation(summary = "更新房屋信息（包含户型图上传）", description = "更新房屋信息并支持上传或替换户型图")
    public Map<String, Object> updateHouseWithImages(
            @Parameter(description = "房屋ID", required = true)
            @PathVariable Long houseId,
            @Parameter(description = "房间号", required = false)
            @RequestParam(required = false) String roomNo,
            @Parameter(description = "完整房间号", required = false)
            @RequestParam(required = false) String fullRoomNo,
            @Parameter(description = "房屋编码", required = false)
            @RequestParam(required = false) String houseCode,
            @Parameter(description = "建筑面积", required = false)
            @RequestParam(required = false) Double buildingArea,
            @Parameter(description = "使用面积", required = false)
            @RequestParam(required = false) Double usableArea,
            @Parameter(description = "公摊面积", required = false)
            @RequestParam(required = false) Double sharedArea,
            @Parameter(description = "房屋类型", required = false)
            @RequestParam(required = false) String houseType,
            @Parameter(description = "房屋户型", required = false)
            @RequestParam(required = false) String houseLayout,
            @Parameter(description = "房屋朝向", required = false)
            @RequestParam(required = false) String houseOrientation,
            @Parameter(description = "车位号", required = false)
            @RequestParam(required = false) String parkingSpaceNo,
            @Parameter(description = "车位类型", required = false)
            @RequestParam(required = false) String parkingType,
            @Parameter(description = "房屋状态", required = false)
            @RequestParam(required = false) String houseStatus,
            @Parameter(description = "装修状态", required = false)
            @RequestParam(required = false) String decorationStatus,
            @Parameter(description = "楼层", required = false)
            @RequestParam(required = false) Integer floorLevel,
            @Parameter(description = "是否有阳台", required = false)
            @RequestParam(required = false) Integer hasBalcony,
            @Parameter(description = "是否有花园", required = false)
            @RequestParam(required = false) Integer hasGarden,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "户型图文件", required = false)
            @RequestParam(value = "floorPlanImageFile", required = false) MultipartFile floorPlanImageFile,
            @Parameter(description = "是否删除户型图", required = false)
            @RequestParam(required = false) Boolean floorPlanImageToDelete,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return houseService.updateHouseWithImages(houseId, roomNo, fullRoomNo, houseCode, buildingArea,
                usableArea, sharedArea, houseType, houseLayout, houseOrientation, parkingSpaceNo, parkingType,
                houseStatus, decorationStatus, floorLevel, hasBalcony, hasGarden, remark, floorPlanImageFile, floorPlanImageToDelete);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除房屋
     */
    @DeleteMapping("/{houseId}/delete")
    @Operation(summary = "删除房屋", description = "删除指定的房屋及其关联的户型图")
    public Map<String, Object> deleteHouse(
            @Parameter(description = "房屋ID", required = true)
            @PathVariable Long houseId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        return houseService.deleteHouse(houseId);
    }

    /**
     * 更新房屋信息（不包含户型图，仅更新基本信息）
     */
    @PostMapping("/{houseId}/update")
    @Operation(summary = "更新房屋信息", description = "更新房屋基本信息，不支持户型图修改，户型图修改请使用update-with-images端点")
    public Map<String, Object> updateHouse(
            @Parameter(description = "房屋ID", required = true)
            @PathVariable Long houseId,
            @Parameter(description = "房间号", required = false)
            @RequestParam(required = false) String roomNo,
            @Parameter(description = "完整房间号", required = false)
            @RequestParam(required = false) String fullRoomNo,
            @Parameter(description = "房屋编码", required = false)
            @RequestParam(required = false) String houseCode,
            @Parameter(description = "建筑面积", required = false)
            @RequestParam(required = false) Double buildingArea,
            @Parameter(description = "使用面积", required = false)
            @RequestParam(required = false) Double usableArea,
            @Parameter(description = "公摊面积", required = false)
            @RequestParam(required = false) Double sharedArea,
            @Parameter(description = "房屋类型", required = false)
            @RequestParam(required = false) String houseType,
            @Parameter(description = "房屋户型", required = false)
            @RequestParam(required = false) String houseLayout,
            @Parameter(description = "房屋朝向", required = false)
            @RequestParam(required = false) String houseOrientation,
            @Parameter(description = "车位号", required = false)
            @RequestParam(required = false) String parkingSpaceNo,
            @Parameter(description = "车位类型", required = false)
            @RequestParam(required = false) String parkingType,
            @Parameter(description = "房屋状态", required = false)
            @RequestParam(required = false) String houseStatus,
            @Parameter(description = "装修状态", required = false)
            @RequestParam(required = false) String decorationStatus,
            @Parameter(description = "楼层", required = false)
            @RequestParam(required = false) Integer floorLevel,
            @Parameter(description = "是否有阳台", required = false)
            @RequestParam(required = false) Integer hasBalcony,
            @Parameter(description = "是否有花园", required = false)
            @RequestParam(required = false) Integer hasGarden,
            @Parameter(description = "备注", required = false)
            @RequestParam(required = false) String remark,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            return houseService.updateHouse(houseId, roomNo, fullRoomNo, houseCode, buildingArea, usableArea, 
                    sharedArea, houseType, houseLayout, houseOrientation, parkingSpaceNo, parkingType, 
                    houseStatus, decorationStatus, floorLevel, hasBalcony, hasGarden, remark);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }
}
