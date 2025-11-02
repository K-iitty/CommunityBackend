package com.community.property.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface HouseService {
    /**
     * 获取房屋详细信息（包含社区、楼栋、业主等关联信息）
     */
    Map<String, Object> getHouseDetail(Long houseId);

    /**
     * 新增房屋（支持完整字段和图片上传）
     */
    Map<String, Object> addHouseWithImage(Long communityId, Long buildingId, String roomNo, 
            String fullRoomNo, String houseCode, Double buildingArea, Double usableArea, 
            Double sharedArea, String houseType, String houseLayout, String houseOrientation,
            String parkingSpaceNo, String parkingType, String houseStatus, String decorationStatus,
            Integer floorLevel, Integer hasBalcony, Integer hasGarden, String remark, 
            MultipartFile floorPlanImageFile);

    /**
     * 更新房屋信息（支持更多字段和图片上传）
     */
    Map<String, Object> updateHouseWithImages(Long houseId, String roomNo, String fullRoomNo, String houseCode,
            Double buildingArea, Double usableArea, Double sharedArea, String houseType, String houseLayout, 
            String houseOrientation, String parkingSpaceNo, String parkingType, String houseStatus, 
            String decorationStatus, Integer floorLevel, Integer hasBalcony, Integer hasGarden, String remark,
            MultipartFile floorPlanImageFile, Boolean floorPlanImageToDelete);

    /**
     * 更新房屋信息（不支持图片，仅更新基本字段）
     */
    Map<String, Object> updateHouse(Long houseId, String roomNo, String fullRoomNo, String houseCode,
            Double buildingArea, Double usableArea, Double sharedArea, String houseType, 
            String houseLayout, String houseOrientation, String parkingSpaceNo, String parkingType,
            String houseStatus, String decorationStatus, Integer floorLevel, Integer hasBalcony, 
            Integer hasGarden, String remark);

    /**
     * 删除房屋
     */
    Map<String, Object> deleteHouse(Long houseId);
}
