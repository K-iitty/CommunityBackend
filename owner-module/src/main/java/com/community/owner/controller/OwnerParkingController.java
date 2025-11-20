package com.community.owner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.owner.service.*;
import com.community.owner.utils.JwtUtil;
import com.community.owner.domain.entity.Owner;
import com.community.owner.domain.entity.ParkingLot;
import com.community.owner.domain.entity.ParkingSpace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/owner/parking-spaces")
@Tag(name = "业主车位管理", description = "申请/列表/详情(含停车场信息)")
public class OwnerParkingController {

    private static final Logger logger = LoggerFactory.getLogger(OwnerParkingController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private ParkingSpaceService parkingSpaceService;

    @Autowired
    private ParkingLotService parkingLotService;

    @Autowired
    private OwnerQueryService ownerQueryService;

    @Autowired
    private RedisMessageService redisMessageService;

    private Owner getCurrentOwner(String token) {
        String realToken = token.replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(realToken);
        return ownerService.findByUsername(username);
    }

    public static class ApplySpaceRequest {
        public Long parkingLotId; // 必填
        public Long parkingSpaceId; // 必填：要绑定的已有车位记录 ID
        public Long vehicleId; // 可选：关联车辆
        public String remark;
    }

    @PostMapping("/apply")
    @Operation(summary = "申请关联车位", description = "输入停车场ID与车位ID；若该车位已被关联则拒绝；创建申请（不在列表展示，需管理员同意）")
    public Map<String, Object> applySpace(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @RequestBody ApplySpaceRequest req
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }
            if (req == null || req.parkingLotId == null || req.parkingSpaceId == null) {
                resp.put("success", false);
                resp.put("message", "停车场ID与车位ID必填");
                return resp;
            }

            ParkingSpace space = parkingSpaceService.getById(req.parkingSpaceId);
            if (space == null || !Objects.equals(space.getParkingLotId(), req.parkingLotId)) {
                resp.put("success", false);
                resp.put("message", "车位不存在或不属于该停车场");
                return resp;
            }

            // 一个车位只能被一个业主关联：若已有关联业主且非本人则不可申请
            if (space.getOwnerId() != null && !Objects.equals(space.getOwnerId(), me.getId())) {
                resp.put("success", false);
                resp.put("message", "该车位已被其他业主关联");
                return resp;
            }

            // 若本人已有一条对该 space 的申请（ownerId 相同且 space 的 ownerId 为 null 或本人），去重返回
            // 这里通过“占位申请”的方式：将 space 的 ownerId 设为本人，space_status 暂不变，由管理员审核通过后决定是否保持
            if (Objects.equals(space.getOwnerId(), me.getId())) {
                resp.put("success", true);
                resp.put("message", "申请已提交，等待审核");
                return resp;
            }

            space.setOwnerId(me.getId());
            if (req.vehicleId != null) {
                space.setVehicleId(req.vehicleId);
            }
            if (req.remark != null) {
                space.setRemark(req.remark);
            }
            // 使用 remark 加注：申请中（管理员处理后才算通过）
            String oldRemark = space.getRemark();
            space.setRemark((oldRemark == null ? "" : oldRemark + " ") + "[申请] 待管理员审核");

            boolean ok = parkingSpaceService.updateById(space);
            if (ok) {
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

    @GetMapping
    @Operation(summary = "我的车位列表", description = "仅显示管理员已同意（实际已关联到该业主）的车位")
    public Map<String, Object> listMySpaces(
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

            // 使用OwnerQueryService进行SQL多表JOIN查询
            Long total = ownerQueryService.countOwnerParkingSpaces(me.getId());
            List<Map<String, Object>> spaceRows = ownerQueryService.listOwnerParkingSpacesWithDetails(
                    me.getId(), size, (page - 1) * size);

            List<Map<String, Object>> items = new ArrayList<>();
            if (spaceRows != null && !spaceRows.isEmpty()) {
                for (Map<String, Object> row : spaceRows) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", row.get("id"));
                    m.put("parkingLotId", row.get("parking_lot_id"));
                    m.put("spaceNo", row.get("space_no"));
                    m.put("fullSpaceNo", row.get("full_space_no"));
                    m.put("spaceType", row.get("space_type"));
                    m.put("spaceArea", row.get("space_area"));
                    m.put("spaceStatus", row.get("space_status"));
                    m.put("monthlyFee", row.get("monthly_fee"));
                    m.put("remark", row.get("remark"));
                    
                    // 添加关联的停车场信息
                    m.put("lotName", row.get("lot_name"));
                    m.put("lotCode", row.get("lot_code"));
                    m.put("zoneName", row.get("zone_name"));
                    m.put("chargeMethod", row.get("charge_method"));
                    m.put("chargeStandard", row.get("charge_standard"));
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

    @GetMapping("/{id}")
    @Operation(summary = "车位详情(含停车场)", description = "本人已关联的车位才可查看，并附带停车场详情")
    public Map<String, Object> getSpaceDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }
            ParkingSpace s = parkingSpaceService.getById(id);
            if (s == null || !Objects.equals(s.getOwnerId(), me.getId())) {
                resp.put("success", false);
                resp.put("message", "无权查看或车位不存在");
                return resp;
            }
            Map<String, Object> data = new LinkedHashMap<>();
            Map<String, Object> space = new LinkedHashMap<>();
            space.put("id", s.getId());
            space.put("parkingLotId", s.getParkingLotId());
            space.put("spaceNo", s.getSpaceNo());
            space.put("fullSpaceNo", s.getFullSpaceNo());
            space.put("spaceType", s.getSpaceType());
            space.put("spaceStatus", s.getSpaceStatus());
            space.put("vehicleId", s.getVehicleId());
            space.put("monthlyFee", s.getMonthlyFee());
            space.put("remark", s.getRemark());

            ParkingLot lot = parkingLotService.getById(s.getParkingLotId());
            Map<String, Object> lotMap = null;
            if (lot != null) {
                lotMap = new LinkedHashMap<>();
                lotMap.put("id", lot.getId());
                lotMap.put("communityId", lot.getCommunityId());
                lotMap.put("lotName", lot.getLotName());
                lotMap.put("lotCode", lot.getLotCode());
                lotMap.put("zoneName", lot.getZoneName());
                lotMap.put("address", lot.getAddress());
                lotMap.put("detailAddress", lot.getDetailAddress());
                lotMap.put("businessHours", lot.getBusinessHours());
                lotMap.put("chargeMethod", lot.getChargeMethod());
                lotMap.put("chargeStandard", lot.getChargeStandard());
                lotMap.put("monthlyFee", lot.getMonthlyFee());
                lotMap.put("status", lot.getStatus());
            }

            data.put("space", space);
            data.put("lot", lotMap);

            resp.put("success", true);
            resp.put("data", data);
            resp.put("message", "获取成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "获取失败: " + e.getMessage());
            return resp;
        }
    }

    @GetMapping("/applications")
    @Operation(summary = "我的车位申请列表", description = "列出本人的车位申请，等待管理员审核")
    public Map<String, Object> listMyApplications(
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

            // 查询本人的待审核车位（ownerId为本人且remark中含有申请标记）
            QueryWrapper<ParkingSpace> qw = new QueryWrapper<>();
            qw.eq("owner_id", me.getId())
              .like("remark", "[申请]");

            List<ParkingSpace> applications = parkingSpaceService.list(qw);

            List<Map<String, Object>> items = new ArrayList<>();
            if (applications != null && !applications.isEmpty()) {
                for (ParkingSpace space : applications) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", space.getId());
                    m.put("spaceNo", space.getSpaceNo());
                    m.put("fullSpaceNo", space.getFullSpaceNo());
                    m.put("spaceType", space.getSpaceType());
                    m.put("spaceStatus", space.getSpaceStatus());
                    m.put("remark", space.getRemark());
                    items.add(m);
                }
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
    @Operation(summary = "按停车场状态查询车位申请列表", description = "查询该业主关联的停车场中状态为非启用的车位申请，支持分页")
    public Map<String, Object> listApplicationsByStatus(
            @RequestHeader("Authorization") String token,
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

            // 查询该业主关联的停车场状态为非启用的车位
            // 使用OwnerQueryService的SQL查询
            List<Map<String, Object>> spaceRows = ownerQueryService.listOwnerParkingSpacesByLotStatus(
                    me.getId(), status, size, offset);
            Long total = ownerQueryService.countOwnerParkingSpacesByLotStatus(me.getId(), status);

            List<Map<String, Object>> items = new ArrayList<>();
            if (spaceRows != null && !spaceRows.isEmpty()) {
                for (Map<String, Object> row : spaceRows) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", row.get("id"));
                    m.put("parkingLotId", row.get("parking_lot_id"));
                    m.put("spaceNo", row.get("space_no"));
                    m.put("fullSpaceNo", row.get("full_space_no"));
                    m.put("spaceType", row.get("space_type"));
                    m.put("spaceArea", row.get("space_area"));
                    m.put("spaceStatus", row.get("space_status"));
                    m.put("monthlyFee", row.get("monthly_fee"));
                    m.put("remark", row.get("remark"));
                    
                    // 添加关联的停车场信息
                    m.put("lotName", row.get("lot_name"));
                    m.put("lotCode", row.get("lot_code"));
                    m.put("zoneName", row.get("zone_name"));
                    m.put("chargeMethod", row.get("charge_method"));
                    m.put("chargeStandard", row.get("charge_standard"));
                    m.put("lotStatus", row.get("status")); // 停车场状态
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

    @GetMapping("/search-available")
    @Operation(summary = "查询所有停车位", description = "查询数据库中所有的停车位，按车位状态(space_status)过滤，支持分页")
    public Map<String, Object> searchAvailableParkingSpaces(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "spaceStatus", required = false) String spaceStatus,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                logger.warn("获取业主信息失败：用户不存在");
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }

            logger.info("业主查询所有停车位 - ownerId: {}, spaceStatus: {}, page: {}, size: {}", me.getId(), spaceStatus, page, size);

            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;
            int offset = (page - 1) * size;

            // 查询所有停车位（按状态筛选）
            List<Map<String, Object>> spaceRows = ownerQueryService.listAvailableParkingSpacesByStatus(
                    me.getId(), spaceStatus, size, offset);
            Long total = ownerQueryService.countAvailableParkingSpacesByStatus(me.getId(), spaceStatus);

            logger.info("查询结果 - 找到 {} 条停车位记录，总数: {}", spaceRows != null ? spaceRows.size() : 0, total);

            List<Map<String, Object>> items = new ArrayList<>();
            if (spaceRows != null && !spaceRows.isEmpty()) {
                for (Map<String, Object> row : spaceRows) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", row.get("id"));
                    m.put("parkingLotId", row.get("parking_lot_id"));
                    m.put("spaceNo", row.get("space_no"));
                    m.put("fullSpaceNo", row.get("full_space_no"));
                    m.put("spaceType", row.get("space_type"));
                    m.put("spaceArea", row.get("space_area"));
                    m.put("spaceStatus", row.get("space_status"));
                    m.put("monthlyFee", row.get("monthly_fee"));
                    m.put("remark", row.get("remark"));
                    
                    // 添加关联的停车场和社区信息
                    m.put("lotName", row.get("lot_name"));
                    m.put("lotCode", row.get("lot_code"));
                    m.put("zoneName", row.get("zone_name"));
                    m.put("chargeMethod", row.get("charge_method"));
                    m.put("chargeStandard", row.get("charge_standard"));
                    m.put("lotStatus", row.get("lot_status")); // 停车场状态
                    m.put("communityName", row.get("community_name")); // 社区名称
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
            logger.error("查询停车位异常", e);
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return resp;
        }
    }
}


