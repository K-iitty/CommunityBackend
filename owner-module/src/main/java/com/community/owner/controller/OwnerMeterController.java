package com.community.owner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.owner.service.*;
import com.community.owner.utils.JwtUtil;
import com.community.owner.domain.entity.House;
import com.community.owner.domain.entity.HouseOwner;
import com.community.owner.domain.entity.MeterConfig;
import com.community.owner.domain.entity.MeterInfo;
import com.community.owner.domain.entity.Owner;
import com.community.owner.domain.entity.Building;
import com.community.owner.domain.entity.CommunityInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/owner/meters")
@Tag(name = "业主仪表管理", description = "我的仪表：卡片、详情、申请新增/删除")
public class OwnerMeterController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private HouseOwnerService houseOwnerService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private MeterInfoService meterInfoService;

    @Autowired
    private MeterConfigService meterConfigService;

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
    @Operation(summary = "仪表卡片列表", description = "仅展示本人已验证房屋下，且非'申请新增'状态的仪表")
    public Map<String, Object> listMyMeterCards(
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

            // 使用OwnerQueryService进行SQL多表JOIN查询，避免N+1问题
            List<Map<String, Object>> meterRows = ownerQueryService.listOwnerMeterCardsWithDetails(me.getId());
            if (meterRows == null || meterRows.isEmpty()) {
                resp.put("success", true);
                resp.put("data", Collections.emptyList());
                resp.put("message", "暂无仪表");
                return resp;
            }

            List<Map<String, Object>> items = new ArrayList<>();
            for (Map<String, Object> row : meterRows) {
                Map<String, Object> item = new LinkedHashMap<>();
                
                // 仪表基本信息
                item.put("meterId", row.get("id"));
                item.put("houseId", row.get("house_id"));
                item.put("meterName", row.get("meter_name"));
                item.put("categoryName", row.get("category_name"));
                item.put("meterType", row.get("meter_type"));
                item.put("meterCode", row.get("meter_code"));
                item.put("meterSn", row.get("meter_sn"));
                item.put("meterStatus", row.get("meter_status"));
                
                // 仪表读数信息
                item.put("initialReading", row.get("initial_reading"));
                item.put("currentReading", row.get("current_reading"));
                item.put("maxReading", row.get("max_reading"));
                item.put("totalUsage", row.get("total_usage"));
                item.put("unit", row.get("unit"));
                item.put("chargeStandard", row.get("charge_standard"));
                
                // 仪表安装信息
                item.put("locationType", row.get("location_type"));
                item.put("installLocation", row.get("install_location"));
                item.put("installDate", row.get("install_date"));
                item.put("commAddress", row.get("comm_address"));
                item.put("lastCommTime", row.get("last_comm_time"));
                
                // 仪表状态信息
                item.put("onlineStatus", row.get("online_status"));
                item.put("powerStatus", row.get("power_status"));
                
                // 房屋信息
                item.put("roomNo", row.get("room_no"));
                item.put("fullRoomNo", row.get("full_room_no"));
                
                // 楼栋信息
                item.put("buildingNo", row.get("building_no"));
                item.put("buildingName", row.get("building_name"));
                
                // 社区信息
                item.put("communityName", row.get("community_name"));
                
                // 仪表配置信息
                item.put("unitPrice", row.get("unit_price"));
                item.put("calculationMethod", row.get("calculation_method"));
                item.put("decimalPlaces", row.get("decimal_places"));
                item.put("minValue", row.get("min_value"));
                item.put("maxValue", row.get("max_value"));
                item.put("configStatus", row.get("config_status"));
                
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

    @GetMapping("/{meterId}")
    @Operation(summary = "仪表详情", description = "展示仪表信息与对应配置，包含完整的房屋、楼栋、社区关联信息")
    public Map<String, Object> getMeterDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable("meterId") Long meterId
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }
            MeterInfo m = meterInfoService.getById(meterId);
            if (m == null) {
                resp.put("success", false);
                resp.put("message", "仪表不存在");
                return resp;
            }
            QueryWrapper<HouseOwner> hoQ = new QueryWrapper<>();
            hoQ.eq("house_id", m.getHouseId()).eq("owner_id", me.getId());
            if (houseOwnerService.count(hoQ) == 0) {
                resp.put("success", false);
                resp.put("message", "无权查看该仪表");
                return resp;
            }
            MeterConfig cfg = (m.getConfigId() != null) ? meterConfigService.getById(m.getConfigId()) : null;

            // 关联查询房屋、楼栋、社区
            House house = houseService.getById(m.getHouseId());
            Building building = (house != null) ? buildingService.getById(house.getBuildingId()) : null;
            CommunityInfo community = (house != null) ? communityInfoService.getById(house.getCommunityId()) : null;

            Map<String, Object> data = new LinkedHashMap<>();
            Map<String, Object> meter = new LinkedHashMap<>();
            meter.put("id", m.getId());
            meter.put("communityId", m.getCommunityId());
            meter.put("houseId", m.getHouseId());
            meter.put("buildingId", m.getBuildingId());
            meter.put("meterName", m.getMeterName());
            meter.put("categoryName", m.getCategoryName());
            meter.put("meterType", m.getMeterType());
            meter.put("meterCode", m.getMeterCode());
            meter.put("meterSn", m.getMeterSn());
            meter.put("locationType", m.getLocationType());
            meter.put("installLocation", m.getInstallLocation());
            meter.put("installDate", m.getInstallDate());
            meter.put("initialReading", m.getInitialReading());
            meter.put("currentReading", m.getCurrentReading());
            meter.put("unit", m.getUnit());
            meter.put("meterStatus", m.getMeterStatus());
            meter.put("remark", m.getRemark());

            Map<String, Object> config = null;
            if (cfg != null) {
                config = new LinkedHashMap<>();
                config.put("id", cfg.getId());
                config.put("categoryName", cfg.getCategoryName());
                config.put("meterType", cfg.getMeterType());
                config.put("unit", cfg.getUnit());
                config.put("decimalPlaces", cfg.getDecimalPlaces());
                config.put("unitPrice", cfg.getUnitPrice());
                config.put("chargeStandard", cfg.getChargeStandard());
                config.put("status", cfg.getStatus());
            }

            // 添加房屋详细信息
            Map<String, Object> houseInfo = null;
            if (house != null) {
                houseInfo = new LinkedHashMap<>();
                houseInfo.put("id", house.getId());
                houseInfo.put("roomNo", house.getRoomNo());
                houseInfo.put("fullRoomNo", house.getFullRoomNo());
                houseInfo.put("houseCode", house.getHouseCode());
                houseInfo.put("houseType", house.getHouseType());
                houseInfo.put("houseLayout", house.getHouseLayout());
                houseInfo.put("buildingArea", house.getBuildingArea());
                houseInfo.put("usableArea", house.getUsableArea());
            }

            // 添加楼栋详细信息
            Map<String, Object> buildingInfo = null;
            if (building != null) {
                buildingInfo = new LinkedHashMap<>();
                buildingInfo.put("id", building.getId());
                buildingInfo.put("buildingNo", building.getBuildingNo());
                buildingInfo.put("buildingName", building.getBuildingName());
                buildingInfo.put("buildingAlias", building.getBuildingAlias());
                buildingInfo.put("unitNo", building.getUnitNo());
                buildingInfo.put("unitName", building.getUnitName());
                buildingInfo.put("totalFloors", building.getTotalFloors());
            }

            // 添加社区详细信息
            Map<String, Object> communityData = null;
            if (community != null) {
                communityData = new LinkedHashMap<>();
                communityData.put("id", community.getId());
                communityData.put("communityName", community.getCommunityName());
                communityData.put("communityCode", community.getCommunityCode());
                communityData.put("province", community.getProvince());
                communityData.put("city", community.getCity());
                communityData.put("district", community.getDistrict());
                communityData.put("propertyCompany", community.getPropertyCompany());
                communityData.put("contactPhone", community.getContactPhone());
            }

            data.put("meter", meter);
            data.put("config", config);
            data.put("house", houseInfo);
            data.put("building", buildingInfo);
            data.put("community", communityData);

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

    public static class ApplyAddMeterRequest {
        public Long communityId; // 必填
        public Long houseId; // 必填
        public Long buildingId; // 必填
        public Long configId; // 必填
        public String meterName; // 可选
        public String meterType; // 可选（应与配置匹配）
        public String meterCode; // 可选（唯一，如不填可由管理员生成）
        public String meterSn; // 可选
        public String installLocation; // 可选
        public String remark; // 可选
    }

    @PostMapping("/apply-add")
    @Operation(summary = "申请新增仪表", description = "关联本人房屋/楼栋，标记为‘申请新增’，待管理员审核")
    public Map<String, Object> applyAddMeter(
            @RequestHeader("Authorization") String token,
            @RequestBody ApplyAddMeterRequest req
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }
            if (req == null || req.communityId == null || req.houseId == null || req.buildingId == null || req.configId == null) {
                resp.put("success", false);
                resp.put("message", "参数不完整");
                return resp;
            }

            // 校验 house 属于本人（已验证）
            QueryWrapper<HouseOwner> hoQ = new QueryWrapper<>();
            hoQ.eq("owner_id", me.getId()).eq("house_id", req.houseId).eq("is_verified", 1);
            if (houseOwnerService.count(hoQ) == 0) {
                resp.put("success", false);
                resp.put("message", "房屋不属于当前业主或未验证");
                return resp;
            }

            // 校验 house 的 building 与 community 一致性（通过 house 信息验证 buildingId）
            House house = houseService.getById(req.houseId);
            if (house == null || !Objects.equals(house.getBuildingId(), req.buildingId) || !Objects.equals(house.getCommunityId(), req.communityId)) {
                resp.put("success", false);
                resp.put("message", "楼栋/小区与房屋不匹配");
                return resp;
            }

            // 校验配置存在
            MeterConfig cfg = meterConfigService.getById(req.configId);
            if (cfg == null) {
                resp.put("success", false);
                resp.put("message", "仪表配置不存在");
                return resp;
            }

            MeterInfo m = new MeterInfo();
            m.setCommunityId(req.communityId);
            m.setHouseId(req.houseId);
            m.setBuildingId(req.buildingId);
            m.setConfigId(req.configId);
            m.setMeterName(req.meterName != null ? req.meterName : (cfg.getCategoryName() + "仪表"));
            m.setCategoryName(cfg.getCategoryName());
            m.setMeterType(req.meterType != null ? req.meterType : cfg.getMeterType());
            m.setMeterCode(req.meterCode);
            m.setMeterSn(req.meterSn);
            m.setInstallLocation(req.installLocation);
            // 申请新增，不立即生效，remark 标记
            String baseRemark = req.remark == null ? "" : req.remark + " ";
            m.setRemark(baseRemark + "[申请新增] 待管理员审核");

            boolean ok = meterInfoService.save(m);
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

    @DeleteMapping("/{meterId}")
    @Operation(summary = "申请删除仪表", description = "仅提交删除申请，不立即生效")
    public Map<String, Object> applyDeleteMeter(
            @RequestHeader("Authorization") String token,
            @PathVariable("meterId") Long meterId,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }
            MeterInfo m = meterInfoService.getById(meterId);
            if (m == null) {
                resp.put("success", false);
                resp.put("message", "仪表不存在");
                return resp;
            }
            QueryWrapper<HouseOwner> hoQ = new QueryWrapper<>();
            hoQ.eq("house_id", m.getHouseId()).eq("owner_id", me.getId()).eq("is_verified", 1);
            if (houseOwnerService.count(hoQ) == 0) {
                resp.put("success", false);
                resp.put("message", "无权操作该仪表");
                return resp;
            }

            String oldRemark = m.getRemark();
            StringBuilder sb = new StringBuilder();
            if (oldRemark != null && !oldRemark.isEmpty()) sb.append(oldRemark).append(' ');
            sb.append("[申请删除] 待管理员审核");
            if (reason != null && !reason.trim().isEmpty()) sb.append(" 原因:").append(reason.trim());
            m.setRemark(sb.toString());

            boolean ok = meterInfoService.updateById(m);
            if (ok) {
                resp.put("success", true);
                resp.put("message", "删除申请已提交，等待审核");
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

    @GetMapping("/billing/cards")
    @Operation(summary = "仪表缴费账单卡片列表", description = "查询业主名下所有房屋的仪表未支付抄表账单，包含账单金额计算")
    public Map<String, Object> listMeterBillingCards(
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

            // 使用OwnerQueryService进行复杂的多表JOIN查询
            List<Map<String, Object>> billingRows = ownerQueryService.listOwnerMeterBillingWithDetails(me.getId());
            if (billingRows == null || billingRows.isEmpty()) {
                resp.put("success", true);
                resp.put("data", Collections.emptyList());
                resp.put("message", "暂无待缴账单");
                return resp;
            }

            List<Map<String, Object>> items = new ArrayList<>();
            java.math.BigDecimal totalAmount = new java.math.BigDecimal(0);

            for (Map<String, Object> row : billingRows) {
                Map<String, Object> item = new LinkedHashMap<>();
                
                // 抄表记录ID和仪表ID
                item.put("readingId", row.get("reading_id"));
                item.put("meterId", row.get("meter_id"));
                item.put("houseId", row.get("house_id"));
                
                // 仪表基本信息
                item.put("meterName", row.get("meter_name"));
                item.put("categoryName", row.get("category_name"));
                item.put("meterType", row.get("meter_type"));
                item.put("meterCode", row.get("meter_code"));
                item.put("meterStatus", row.get("meter_status"));
                item.put("unit", row.get("unit"));
                
                // 房屋信息
                item.put("roomNo", row.get("room_no"));
                item.put("fullRoomNo", row.get("full_room_no"));
                item.put("buildingNo", row.get("building_no"));
                item.put("buildingName", row.get("building_name"));
                item.put("communityName", row.get("community_name"));
                
                // 抄表读数信息
                item.put("previousReading", row.get("previous_reading"));
                item.put("currentReading", row.get("current_reading"));
                
                // 用量和单价
                item.put("usageAmount", row.get("usage_amount"));
                item.put("unitPrice", row.get("unit_price"));
                
                // 抄表日期和状态
                item.put("readingDate", row.get("reading_date"));
                item.put("readingStatus", row.get("reading_status"));
                item.put("processed", row.get("processed"));
                item.put("createdAt", row.get("created_at"));
                
                // 配置信息
                item.put("calculationMethod", row.get("calculation_method"));
                item.put("configStatus", row.get("config_status"));
                
                // 账单金额：直接使用数据库计算结果（ROUND处理过）
                Object billAmountObj = row.get("total_amount");
                java.math.BigDecimal billAmount = java.math.BigDecimal.ZERO;
                
                if (billAmountObj != null) {
                    if (billAmountObj instanceof java.math.BigDecimal) {
                        billAmount = (java.math.BigDecimal) billAmountObj;
                    } else if (billAmountObj instanceof Number) {
                        billAmount = new java.math.BigDecimal(((Number) billAmountObj).doubleValue());
                    } else if (billAmountObj instanceof String) {
                        billAmount = new java.math.BigDecimal((String) billAmountObj);
                    }
                }
                
                item.put("billAmount", billAmount);
                totalAmount = totalAmount.add(billAmount);
                
                items.add(item);
            }

            resp.put("success", true);
            resp.put("data", items);
            resp.put("totalAmount", totalAmount);
            resp.put("count", items.size());
            resp.put("message", "查询成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return resp;
        }
    }
}


