package com.community.property.service.impl;

import com.community.property.domain.entity.House;
import com.community.property.mapper.HouseMapper;
import com.community.property.service.HouseService;
import com.community.property.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 房屋服务实现类
 */
@Service
public class HouseServiceImpl implements HouseService {

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private ImageService imageService;

    @Override
    public Map<String, Object> getHouseDetail(Long houseId) {
        Map<String, Object> houseDetail = new HashMap<>();
        try {
            House house = houseMapper.selectById(houseId);
            if (house == null) {
                houseDetail.put("id", null);
                return houseDetail;
            }
            
            // 构建详细信息Map，包含所有房屋字段
            houseDetail.put("id", house.getId());
            houseDetail.put("communityId", house.getCommunityId());
            houseDetail.put("buildingId", house.getBuildingId());
            houseDetail.put("roomNo", house.getRoomNo());
            houseDetail.put("fullRoomNo", house.getFullRoomNo());
            houseDetail.put("houseCode", house.getHouseCode());
            houseDetail.put("buildingArea", house.getBuildingArea());
            houseDetail.put("usableArea", house.getUsableArea());
            houseDetail.put("sharedArea", house.getSharedArea());
            houseDetail.put("houseType", house.getHouseType());
            houseDetail.put("houseLayout", house.getHouseLayout());
            houseDetail.put("houseOrientation", house.getHouseOrientation());
            houseDetail.put("parkingSpaceNo", house.getParkingSpaceNo());
            houseDetail.put("parkingType", house.getParkingType());
            houseDetail.put("houseStatus", house.getHouseStatus());
            houseDetail.put("decorationStatus", house.getDecorationStatus());
            houseDetail.put("floorLevel", house.getFloorLevel());
            houseDetail.put("hasBalcony", house.getHasBalcony());
            houseDetail.put("hasGarden", house.getHasGarden());
            houseDetail.put("floorPlanImage", house.getFloorPlanImage());
            houseDetail.put("remark", house.getRemark());
            houseDetail.put("createdAt", house.getCreatedAt());
            houseDetail.put("updatedAt", house.getUpdatedAt());
            
            return houseDetail;
        } catch (Exception e) {
            houseDetail.put("id", null);
            houseDetail.put("error", "查询失败: " + e.getMessage());
            return houseDetail;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> addHouseWithImage(Long communityId, Long buildingId, String roomNo, 
            String fullRoomNo, String houseCode, Double buildingArea, Double usableArea, 
            Double sharedArea, String houseType, String houseLayout, String houseOrientation,
            String parkingSpaceNo, String parkingType, String houseStatus, String decorationStatus,
            Integer floorLevel, Integer hasBalcony, Integer hasGarden, String remark, 
            MultipartFile floorPlanImageFile) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证必需参数
            if (communityId == null || buildingId == null || roomNo == null || roomNo.isEmpty()) {
                response.put("success", false);
                response.put("message", "社区ID、楼栋ID和房间号为必需");
                return response;
            }

            House house = new House();
            house.setCommunityId(communityId);
            house.setBuildingId(buildingId);
            house.setRoomNo(roomNo);
            house.setFullRoomNo(fullRoomNo);
            house.setHouseCode(houseCode);
            // 转换为BigDecimal
            if (buildingArea != null) house.setBuildingArea(new java.math.BigDecimal(buildingArea));
            if (usableArea != null) house.setUsableArea(new java.math.BigDecimal(usableArea));
            if (sharedArea != null) house.setSharedArea(new java.math.BigDecimal(sharedArea));
            house.setHouseType(houseType);
            house.setHouseLayout(houseLayout);
            house.setHouseOrientation(houseOrientation);
            house.setParkingSpaceNo(parkingSpaceNo);
            house.setParkingType(parkingType);
            house.setHouseStatus(houseStatus != null ? houseStatus : "未入住");
            house.setDecorationStatus(decorationStatus);
            house.setFloorLevel(floorLevel);
            house.setHasBalcony(hasBalcony);
            house.setHasGarden(hasGarden);
            house.setRemark(remark);

            // 处理户型图
            if (floorPlanImageFile != null && !floorPlanImageFile.isEmpty()) {
                String floorPlanPath = imageService.uploadImages(
                    Arrays.asList(floorPlanImageFile), "house/floor_plan", communityId)
                    .stream().findFirst().orElse(null);
                house.setFloorPlanImage(floorPlanPath);
            }

            houseMapper.insert(house);

            response.put("success", true);
            response.put("message", "房屋添加成功");
            response.put("data", house);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateHouseWithImages(Long houseId, String roomNo, String fullRoomNo, String houseCode,
            Double buildingArea, Double usableArea, Double sharedArea, String houseType, String houseLayout,
            String houseOrientation, String parkingSpaceNo, String parkingType, String houseStatus, 
            String decorationStatus, Integer floorLevel, Integer hasBalcony, Integer hasGarden, String remark,
            MultipartFile floorPlanImageFile, Boolean floorPlanImageToDelete) {

        Map<String, Object> response = new HashMap<>();

        try {
            House house = houseMapper.selectById(houseId);
            if (house == null) {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return response;
            }

            // 更新基本信息字段
            if (roomNo != null && !roomNo.isEmpty()) house.setRoomNo(roomNo);
            if (fullRoomNo != null && !fullRoomNo.isEmpty()) house.setFullRoomNo(fullRoomNo);
            if (houseCode != null && !houseCode.isEmpty()) house.setHouseCode(houseCode);
            if (buildingArea != null) house.setBuildingArea(new java.math.BigDecimal(buildingArea));
            if (usableArea != null) house.setUsableArea(new java.math.BigDecimal(usableArea));
            if (sharedArea != null) house.setSharedArea(new java.math.BigDecimal(sharedArea));

            // 处理户型图（单一图片，VARCHAR字段）
            if (floorPlanImageFile != null && !floorPlanImageFile.isEmpty()) {
                String newPath = imageService.updateSingleImage(house.getFloorPlanImage(),
                    floorPlanImageFile, "house/floor_plan", houseId);
                house.setFloorPlanImage(newPath);
            } else if (floorPlanImageToDelete != null && floorPlanImageToDelete) {
                // 删除原有户型图
                if (house.getFloorPlanImage() != null && !house.getFloorPlanImage().isEmpty()) {
                    try {
                        imageService.deleteImage(house.getFloorPlanImage());
                    } catch (Exception e) {
                        System.err.println("删除户型图失败: " + e.getMessage());
                    }
                }
                house.setFloorPlanImage(null);
            }

            // 更新其他字段
            if (houseType != null && !houseType.isEmpty()) house.setHouseType(houseType);
            if (houseLayout != null && !houseLayout.isEmpty()) house.setHouseLayout(houseLayout);
            if (houseOrientation != null && !houseOrientation.isEmpty()) house.setHouseOrientation(houseOrientation);
            if (parkingSpaceNo != null && !parkingSpaceNo.isEmpty()) house.setParkingSpaceNo(parkingSpaceNo);
            if (parkingType != null && !parkingType.isEmpty()) house.setParkingType(parkingType);
            if (houseStatus != null && !houseStatus.isEmpty()) house.setHouseStatus(houseStatus);
            if (decorationStatus != null && !decorationStatus.isEmpty()) house.setDecorationStatus(decorationStatus);
            if (floorLevel != null) house.setFloorLevel(floorLevel);
            if (hasBalcony != null) house.setHasBalcony(hasBalcony);
            if (hasGarden != null) house.setHasGarden(hasGarden);
            if (remark != null && !remark.isEmpty()) house.setRemark(remark);

            // 保存到数据库
            houseMapper.updateById(house);

            response.put("success", true);
            response.put("message", "房屋信息更新成功");
            response.put("data", house);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> deleteHouse(Long houseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            House house = houseMapper.selectById(houseId);
            if (house == null) {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return response;
            }

            // 删除户型图文件
            if (house.getFloorPlanImage() != null && !house.getFloorPlanImage().isEmpty()) {
                try {
                    imageService.deleteImage(house.getFloorPlanImage());
                } catch (Exception e) {
                    System.err.println("删除户型图失败: " + e.getMessage());
                }
            }

            // 删除房屋记录
            int result = houseMapper.deleteById(houseId);

            response.put("success", result > 0);
            response.put("message", result > 0 ? "房屋删除成功" : "房屋删除失败");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateHouse(Long houseId, String roomNo, String fullRoomNo, String houseCode,
            Double buildingArea, Double usableArea, Double sharedArea, String houseType, 
            String houseLayout, String houseOrientation, String parkingSpaceNo, String parkingType,
            String houseStatus, String decorationStatus, Integer floorLevel, Integer hasBalcony, 
            Integer hasGarden, String remark) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            House house = houseMapper.selectById(houseId);
            if (house == null) {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return response;
            }

            // 更新所有提供的字段
            if (roomNo != null && !roomNo.isEmpty()) house.setRoomNo(roomNo);
            if (fullRoomNo != null && !fullRoomNo.isEmpty()) house.setFullRoomNo(fullRoomNo);
            if (houseCode != null && !houseCode.isEmpty()) house.setHouseCode(houseCode);
            if (buildingArea != null) house.setBuildingArea(new java.math.BigDecimal(buildingArea));
            if (usableArea != null) house.setUsableArea(new java.math.BigDecimal(usableArea));
            if (sharedArea != null) house.setSharedArea(new java.math.BigDecimal(sharedArea));
            if (houseType != null && !houseType.isEmpty()) house.setHouseType(houseType);
            if (houseLayout != null && !houseLayout.isEmpty()) house.setHouseLayout(houseLayout);
            if (houseOrientation != null && !houseOrientation.isEmpty()) house.setHouseOrientation(houseOrientation);
            if (parkingSpaceNo != null && !parkingSpaceNo.isEmpty()) house.setParkingSpaceNo(parkingSpaceNo);
            if (parkingType != null && !parkingType.isEmpty()) house.setParkingType(parkingType);
            if (houseStatus != null && !houseStatus.isEmpty()) house.setHouseStatus(houseStatus);
            if (decorationStatus != null && !decorationStatus.isEmpty()) house.setDecorationStatus(decorationStatus);
            if (floorLevel != null) house.setFloorLevel(floorLevel);
            if (hasBalcony != null) house.setHasBalcony(hasBalcony);
            if (hasGarden != null) house.setHasGarden(hasGarden);
            if (remark != null && !remark.isEmpty()) house.setRemark(remark);

            // 保存到数据库
            houseMapper.updateById(house);

            response.put("success", true);
            response.put("message", "房屋信息更新成功");
            response.put("data", house);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }
}
