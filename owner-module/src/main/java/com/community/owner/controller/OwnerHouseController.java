package com.community.owner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.owner.service.*;
import com.community.owner.utils.JwtUtil;
import com.community.owner.domain.entity.Building;
import com.community.owner.domain.entity.CommunityInfo;
import com.community.owner.domain.entity.House;
import com.community.owner.domain.entity.HouseOwner;
import com.community.owner.domain.entity.Owner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/owner/houses")
@Tag(name = "业主房屋关联", description = "房屋卡片、详情、申请关联、删除关联")
public class OwnerHouseController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private HouseOwnerService houseOwnerService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private CommunityInfoService communityInfoService;

    @Autowired
    private OwnerQueryService ownerQueryService;

    @Autowired
    private RedisMessageService redisMessageService;

    private Owner getCurrentOwner(String token) {
        String realToken = token.replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(realToken);
        return ownerService.findByUsername(username);
    }

    @GetMapping("/cards")
    @Operation(summary = "房屋卡片列表", description = "仅显示当前业主已验证关联的房屋作为卡片，包含楼栋和社区信息关联")
    public Map<String, Object> listVerifiedHouseCards(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }

            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;
            int offset = (page - 1) * size;

            // 使用OwnerQueryService进行多表JOIN查询，避免N+1问题
            Long total = ownerQueryService.countOwnerHouses(me.getId());
            List<Map<String, Object>> houseRows = ownerQueryService.listOwnerHousesWithDetails(
                    me.getId(), size, offset);

            List<Map<String, Object>> items = new ArrayList<>();
            if (houseRows != null && !houseRows.isEmpty()) {
                for (Map<String, Object> row : houseRows) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", row.get("id"));
                    m.put("communityId", row.get("community_id"));
                    m.put("buildingId", row.get("building_id"));
                    m.put("roomNo", row.get("room_no"));
                    m.put("fullRoomNo", row.get("full_room_no"));
                    m.put("houseType", row.get("house_type"));
                    m.put("houseStatus", row.get("house_status"));
                    m.put("houseCode", row.get("house_code"));
                    m.put("houseLayout", row.get("house_layout"));
                    m.put("houseOrientation", row.get("house_orientation"));
                    m.put("buildingName", row.get("building_name"));
                    m.put("buildingNo", row.get("building_no"));
                    m.put("communityName", row.get("community_name"));
                    m.put("communityCode", row.get("community_code"));
                    m.put("buildingArea", row.get("building_area"));
                    m.put("usableArea", row.get("usable_area"));
                    m.put("sharedArea", row.get("shared_area"));
                    m.put("hasBalcony", row.get("has_balcony"));
                    m.put("hasGarden", row.get("has_garden"));
                    items.add(m);
                }
            }

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", items);

            resp.put("success", true);
            resp.put("data", pageData);
            resp.put("message", "查询成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return resp;
        }
    }

    @GetMapping("/{houseId}")
    @Operation(summary = "房屋详情", description = "展示房屋详细信息，包含关联的楼栋和社区信息，且需已验证关联")
    public Map<String, Object> getHouseDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable("houseId") Long houseId
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            System.out.println("📍 getHouseDetail - 开始加载房屋详情，houseId: " + houseId);
            
            Owner me = getCurrentOwner(token);
            if (me == null) {
                System.out.println("❌ getHouseDetail - 用户不存在");
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }
            System.out.println("✅ getHouseDetail - 获取当前用户，ownerId: " + me.getId());

            QueryWrapper<HouseOwner> qw = new QueryWrapper<>();
            qw.eq("house_id", houseId).eq("owner_id", me.getId());
            long count = houseOwnerService.count(qw);
            System.out.println("🔍 getHouseDetail - 房屋关联检查，houseId: " + houseId + "，ownerId: " + me.getId() + "，验证通过的关联数: " + count);
            
            if (count == 0) {
                System.out.println("❌ getHouseDetail - 用户与该房屋没有关联关系");
                resp.put("success", false);
                resp.put("message", "无权查看该房屋或房屋不存在");
                return resp;
            }
            System.out.println("✅ getHouseDetail - 房屋关联检查通过");

            House h = houseService.getById(houseId);
            if (h == null) {
                System.out.println("❌ getHouseDetail - 房屋不存在，houseId: " + houseId);
                resp.put("success", false);
                resp.put("message", "房屋不存在");
                return resp;
            }
            System.out.println("✅ getHouseDetail - 房屋信息获取成功，房间号: " + h.getRoomNo());

            Building building = buildingService.getById(h.getBuildingId());
            CommunityInfo community = communityInfoService.getById(h.getCommunityId());

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("communityId", h.getCommunityId());
            m.put("buildingId", h.getBuildingId());
            m.put("roomNo", h.getRoomNo());
            m.put("fullRoomNo", h.getFullRoomNo());
            m.put("houseCode", h.getHouseCode());
            m.put("buildingArea", h.getBuildingArea());
            m.put("usableArea", h.getUsableArea());
            m.put("sharedArea", h.getSharedArea());
            m.put("houseType", h.getHouseType());
            m.put("houseLayout", h.getHouseLayout());
            m.put("houseOrientation", h.getHouseOrientation());
            m.put("parkingSpaceNo", h.getParkingSpaceNo());
            m.put("parkingType", h.getParkingType());
            m.put("houseStatus", h.getHouseStatus());
            m.put("decorationStatus", h.getDecorationStatus());
            m.put("floorLevel", h.getFloorLevel());
            m.put("hasBalcony", h.getHasBalcony());
            m.put("hasGarden", h.getHasGarden());
            m.put("remark", h.getRemark());
            m.put("floorPlanImage", h.getFloorPlanImage());
            m.put("createdAt", h.getCreatedAt());
            m.put("updatedAt", h.getUpdatedAt());

            if (building != null) {
                Map<String, Object> buildingInfo = new LinkedHashMap<>();
                buildingInfo.put("id", building.getId());
                buildingInfo.put("buildingNo", building.getBuildingNo());
                buildingInfo.put("buildingName", building.getBuildingName());
                buildingInfo.put("buildingAlias", building.getBuildingAlias());
                buildingInfo.put("buildingType", building.getBuildingType());
                buildingInfo.put("totalFloors", building.getTotalFloors());
                buildingInfo.put("buildingAddress", building.getBuildingAddress());
                buildingInfo.put("hasElevator", building.getHasElevator());
                m.put("building", buildingInfo);
                System.out.println("✅ getHouseDetail - 楼栋信息已添加");
            }

            if (community != null) {
                Map<String, Object> communityInfo = new LinkedHashMap<>();
                communityInfo.put("id", community.getId());
                communityInfo.put("communityName", community.getCommunityName());
                communityInfo.put("communityCode", community.getCommunityCode());
                communityInfo.put("detailAddress", community.getDetailAddress());
                communityInfo.put("propertyCompany", community.getPropertyCompany());
                communityInfo.put("contactPhone", community.getContactPhone());
                m.put("community", communityInfo);
                System.out.println("✅ getHouseDetail - 社区信息已添加");
            }

            resp.put("success", true);
            resp.put("data", m);
            resp.put("message", "查询成功");
            System.out.println("✅ getHouseDetail - 房屋详情查询成功");
            return resp;
        } catch (Exception e) {
            System.out.println("❌ getHouseDetail - 异常: " + e.getMessage());
            e.printStackTrace();
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return resp;
        }
    }

    @PostMapping("/apply")
    @Operation(summary = "申请关联房屋", description = "输入小区ID和房屋ID申请关联；若已被关联则拒绝；创建待批准的关联记录")
    public Map<String, Object> applyHouse(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> req
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }

            Long houseId = req.get("houseId") != null ? Long.parseLong(req.get("houseId").toString()) : null;
            if (houseId == null) {
                resp.put("success", false);
                resp.put("message", "房屋ID必填");
                return resp;
            }

            House h = houseService.getById(houseId);
            if (h == null) {
                resp.put("success", false);
                resp.put("message", "房屋不存在");
                return resp;
            }

            QueryWrapper<HouseOwner> qw = new QueryWrapper<>();
            qw.eq("house_id", houseId).ne("owner_id", me.getId());
            if (houseOwnerService.count(qw) > 0) {
                resp.put("success", false);
                resp.put("message", "该房屋已被其他业主关联");
                return resp;
            }

            QueryWrapper<HouseOwner> selfQw = new QueryWrapper<>();
            selfQw.eq("house_id", houseId).eq("owner_id", me.getId());
            HouseOwner existing = houseOwnerService.getOne(selfQw);
            if (existing != null) {
                resp.put("success", true);
                resp.put("message", existing.getIsVerified() == 1 ? "已验证关联" : "申请待审核");
                return resp;
            }

            HouseOwner ho = new HouseOwner();
            ho.setHouseId(houseId);
            ho.setOwnerId(me.getId());
            ho.setIsVerified(0);
            ho.setStartDate(LocalDate.now());
            boolean ok = houseOwnerService.save(ho);
            if (ok) {
                // 发布实时同步消息
                try {
                    redisMessageService.publishOwnerChange("CREATE", "HouseOwner", ho.getId(), ho);
                    redisMessageService.publishNotification("admin", "HOUSE_APPLY", "房屋关联申请", 
                        "业主申请关联房屋：" + h.getFullRoomNo(), null);
                    redisMessageService.publishNotification("property", "HOUSE_APPLY", "房屋关联申请", 
                        "业主申请关联房屋：" + h.getFullRoomNo(), null);
                } catch (Exception e) {
                    System.err.println("发布房屋申请实时消息失败: " + e.getMessage());
                }
                
                resp.put("success", true);
                resp.put("message", "申请已提交，等待审核");
            } else {
                resp.put("success", false);
                resp.put("message", "申请失败");
            }
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "申请失败: " + e.getMessage());
            return resp;
        }
    }

    @GetMapping("/applications")
    @Operation(summary = "查看我的房屋申请列表", description = "展示该业主所有待审核的房屋关联申请")
    public Map<String, Object> listApplications(
            @RequestHeader("Authorization") String token
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }

            QueryWrapper<HouseOwner> qw = new QueryWrapper<>();
            qw.eq("owner_id", me.getId()).eq("is_verified", 0);
            List<HouseOwner> apps = houseOwnerService.list(qw);

            List<Map<String, Object>> items = new ArrayList<>();
            for (HouseOwner app : apps) {
                House h = houseService.getById(app.getHouseId());
                if (h == null) continue;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("applicationId", app.getId());
                item.put("houseId", h.getId());
                item.put("roomNo", h.getRoomNo());
                item.put("fullRoomNo", h.getFullRoomNo());
                item.put("applyDate", app.getStartDate());
                items.add(item);
            }

            resp.put("success", true);
            resp.put("data", items);
            resp.put("message", "查询成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return resp;
        }
    }

    @GetMapping("/applications/by-status")
    @Operation(summary = "按状态查询房屋申请列表", description = "按审核状态查询该业主的房屋申请；状态值为：审核(未验证)/已验证/正常/到期/终止")
    public Map<String, Object> listApplicationsByStatus(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "verified", required = false) Integer verified,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }

            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;
            int offset = (page - 1) * size;

            QueryWrapper<HouseOwner> qw = new QueryWrapper<>();
            qw.eq("owner_id", me.getId());
            
            // 如果指定了verified参数，按is_verified过滤
            if (verified != null) {
                qw.eq("is_verified", verified);
            }
            
            // 如果指定了status参数，按status过滤
            if (status != null && !status.isEmpty()) {
                qw.eq("status", status);
            }
            
            qw.orderByDesc("created_at");
            
            // 计算总数
            Long total = houseOwnerService.count(qw);
            
            // 分页查询
            qw.last("limit " + offset + ", " + size);
            List<HouseOwner> apps = houseOwnerService.list(qw);

            List<Map<String, Object>> items = new ArrayList<>();
            for (HouseOwner app : apps) {
                House h = houseService.getById(app.getHouseId());
                if (h == null) continue;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("applicationId", app.getId());
                item.put("houseId", h.getId());
                item.put("roomNo", h.getRoomNo());
                item.put("fullRoomNo", h.getFullRoomNo());
                item.put("houseType", h.getHouseType());
                item.put("houseLayout", h.getHouseLayout());
                item.put("buildingArea", h.getBuildingArea());
                item.put("applyDate", app.getStartDate());
                item.put("isVerified", app.getIsVerified());
                item.put("status", app.getStatus());
                item.put("relationship", app.getRelationship());
                item.put("remark", app.getVerifyRemark());
                items.add(item);
            }

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", items);

            resp.put("success", true);
            resp.put("data", pageData);
            resp.put("message", "查询成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return resp;
        }
    }

    @DeleteMapping("/{houseId}")
    @Operation(summary = "删除房屋关联", description = "删除该业主与该房屋的关联关系")
    public Map<String, Object> deleteAssociation(
            @RequestHeader("Authorization") String token,
            @PathVariable("houseId") Long houseId
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }

            // 先获取房屋信息用于通知
            House house = houseService.getById(houseId);
            
            QueryWrapper<HouseOwner> qw = new QueryWrapper<>();
            qw.eq("house_id", houseId).eq("owner_id", me.getId());
            boolean ok = houseOwnerService.remove(qw);
            if (ok) {
                // 发布实时同步消息
                try {
                    redisMessageService.publishOwnerChange("DELETE", "HouseOwner", houseId, null);
                    String roomInfo = house != null ? house.getFullRoomNo() : "房屋ID:" + houseId;
                    redisMessageService.publishNotification("admin", "HOUSE_UNLINK", "房屋关联删除", 
                        "业主删除了房屋关联：" + roomInfo, null);
                    redisMessageService.publishNotification("property", "HOUSE_UNLINK", "房屋关联删除", 
                        "业主删除了房屋关联：" + roomInfo, null);
                } catch (Exception e) {
                    System.err.println("发布房屋关联删除实时消息失败: " + e.getMessage());
                }
                
                resp.put("success", true);
                resp.put("message", "删除成功");
            } else {
                resp.put("success", false);
                resp.put("message", "删除失败或关联不存在");
            }
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "删除失败: " + e.getMessage());
            return resp;
        }
    }

    @GetMapping("/community-staff")
    @Operation(summary = "获取物业联系信息", description = "业主登录后无需验证房屋，即可查询所有物业人员的联系方式信息（支持分页）")
    public Map<String, Object> getCommunitystaffInfo(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }

            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;
            int offset = (page - 1) * size;

            // 查询所有物业人员信息（分页）
            List<Map<String, Object>> staffList = ownerQueryService.listAllPropertyStaffWithPagination(offset, size);
            Long total = ownerQueryService.countAllPropertyStaff();

            List<Map<String, Object>> items = new ArrayList<>();
            if (staffList != null && !staffList.isEmpty()) {
                for (Map<String, Object> staffRow : staffList) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", staffRow.get("id"));
                    item.put("name", staffRow.get("name"));
                    item.put("phone", staffRow.get("phone"));
                    item.put("email", staffRow.get("email"));
                    item.put("wechat", staffRow.get("wechat"));
                    item.put("position", staffRow.get("position"));
                    item.put("jobTitle", staffRow.get("job_title"));
                    item.put("educationLevel", staffRow.get("education_level"));
                    item.put("major", staffRow.get("major"));
                    item.put("graduateSchool", staffRow.get("graduate_school"));
                    
                    // 优先使用certificate_photos中的第一张图片，如果没有则使用avatar
                    String certificatePhotos = (String) staffRow.get("certificate_photos");
                    String avatarUrl = (String) staffRow.get("avatar");
                    
                    if (certificatePhotos != null && !certificatePhotos.isEmpty()) {
                        try {
                            // 解析JSON数组，提取第一张图片
                            String[] photos = certificatePhotos.replaceAll("[\\[\\]\"]", "").split(",");
                            if (photos.length > 0 && !photos[0].trim().isEmpty()) {
                                item.put("avatar", photos[0].trim());
                            } else {
                                item.put("avatar", avatarUrl);
                            }
                        } catch (Exception e) {
                            item.put("avatar", avatarUrl);
                        }
                    } else {
                        item.put("avatar", avatarUrl);
                    }
                    
                    item.put("telephoneAreaCode", staffRow.get("telephone_area_code"));
                    item.put("telephoneNumber", staffRow.get("telephone_number"));
                    item.put("telephoneExtension", staffRow.get("telephone_extension"));
                    item.put("departmentName", staffRow.get("department_name"));
                    item.put("departmentCode", staffRow.get("department_code"));
                    item.put("roleName", staffRow.get("role_name"));
                    items.add(item);
                }
            }

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", items);

            resp.put("success", true);
            resp.put("data", pageData);
            resp.put("message", "查询成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return resp;
        }
    }
}


