package com.community.owner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.owner.service.*;
import com.community.owner.utils.JwtUtil;
import com.community.owner.domain.entity.HouseOwner;
import com.community.owner.domain.entity.MeterConfig;
import com.community.owner.domain.entity.MeterInfo;
import com.community.owner.domain.entity.MeterReading;
import com.community.owner.domain.entity.Owner;
import com.community.owner.domain.entity.House;
import com.community.owner.domain.entity.Building;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/owner/billing")
@Tag(name = "业主缴费管理", description = "缴费卡片、详情、计算与历史记录")
public class OwnerBillingController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private HouseOwnerService houseOwnerService;

    @Autowired
    private MeterInfoService meterInfoService;

    @Autowired
    private MeterConfigService meterConfigService;

    @Autowired
    private MeterReadingService meterReadingService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private BuildingService buildingService;

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
    @Operation(summary = "缴费卡片列表", description = "按业主已验证的房屋，汇总其关联的仪表，生成待缴费卡片")
    public Map<String, Object> listBillingCards(
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

            List<Map<String, Object>> cards = new ArrayList<>();

            // 1. 查询仪表账单（仅包含有抄表记录的仪表）
            List<Map<String, Object>> meterItems = ownerQueryService.listOwnerBillingCardsWithDetails(me.getId());
            if (meterItems != null && !meterItems.isEmpty()) {
                for (Map<String, Object> row : meterItems) {
                    Map<String, Object> card = new LinkedHashMap<>();
                    card.put("billType", "meter");  // 账单类型：仪表
                    card.put("meterId", row.get("id"));
                    card.put("houseId", row.get("house_id"));
                    card.put("meterName", row.get("meter_name"));
                    card.put("categoryName", row.get("category_name"));
                    card.put("meterType", row.get("meter_type"));
                    card.put("unit", row.get("unit"));
                    card.put("roomNo", row.get("room_no"));
                    card.put("fullRoomNo", row.get("full_room_no"));
                    card.put("buildingNo", row.get("building_no"));
                    card.put("buildingName", row.get("building_name"));
                    
                    // 从meter_reading获取用量信息
                    BigDecimal usage = row.get("usage_amount") != null ? 
                            new BigDecimal(row.get("usage_amount").toString()) : BigDecimal.ZERO;
                    
                    // 获取单价（从meter_config）
                    BigDecimal unitPrice = row.get("unit_price") != null ? 
                            new BigDecimal(row.get("unit_price").toString()) : BigDecimal.ZERO;
                    
                    // 计算应缴金额 = 用量 × 单价
                    BigDecimal amount = usage.multiply(unitPrice);
                    
                    // 仅添加有待缴费用的账单
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        card.put("usage", usage);
                        card.put("unitPrice", unitPrice);
                        card.put("amount", amount);
                        card.put("lastReadingDate", row.get("reading_date"));
                        card.put("readingUnit", row.get("reading_unit"));
                        cards.add(card);
                    }
                }
            }

            // 2. 查询停车位账单
            List<Map<String, Object>> parkingItems = ownerQueryService.listOwnerParkingBillingWithDetails(me.getId());
            if (parkingItems != null && !parkingItems.isEmpty()) {
                for (Map<String, Object> row : parkingItems) {
                    Map<String, Object> card = new LinkedHashMap<>();
                    card.put("billType", "parking");  // 账单类型：停车位
                    card.put("parkingSpaceId", row.get("parking_space_id"));
                    card.put("spaceNo", row.get("space_no"));
                    card.put("fullSpaceNo", row.get("full_space_no"));
                    card.put("spaceType", row.get("space_type"));
                    card.put("lotName", row.get("lot_name"));
                    card.put("lotCode", row.get("lot_code"));
                    card.put("zoneName", row.get("zone_name"));
                    card.put("plateNumber", row.get("plate_number"));
                    card.put("vehicleType", row.get("vehicle_type"));
                    card.put("chargeMethod", row.get("charge_method"));
                    card.put("chargeStandard", row.get("charge_standard"));
                    
                    // 停车位账单金额为月租费用
                    BigDecimal monthlyFee = row.get("monthly_fee") != null ? 
                            new BigDecimal(row.get("monthly_fee").toString()) : BigDecimal.ZERO;
                    
                    // 仅添加有待缴费用的账单
                    if (monthlyFee.compareTo(BigDecimal.ZERO) > 0) {
                        card.put("amount", monthlyFee);
                        cards.add(card);
                    }
                }
            }

            // 3. 查询问题反馈费用账单
            List<Map<String, Object>> issueItems = ownerQueryService.listOwnerIssueBillingWithDetails(me.getId());
            if (issueItems != null && !issueItems.isEmpty()) {
                for (Map<String, Object> row : issueItems) {
                    Map<String, Object> card = new LinkedHashMap<>();
                    card.put("billType", "issue");  // 账单类型：问题反馈
                    card.put("issueId", row.get("id"));
                    card.put("houseId", row.get("house_id"));
                    card.put("issueTitle", row.get("issue_title"));
                    card.put("issueType", row.get("issue_type"));
                    card.put("subType", row.get("sub_type"));
                    card.put("roomNo", row.get("room_no"));
                    card.put("fullRoomNo", row.get("full_room_no"));
                    card.put("buildingNo", row.get("building_no"));
                    card.put("buildingName", row.get("building_name"));
                    card.put("communityName", row.get("community_name"));
                    
                    // 获取费用信息
                    BigDecimal materialCost = row.get("material_cost") != null ? 
                            new BigDecimal(row.get("material_cost").toString()) : BigDecimal.ZERO;
                    BigDecimal laborCost = row.get("labor_cost") != null ? 
                            new BigDecimal(row.get("labor_cost").toString()) : BigDecimal.ZERO;
                    BigDecimal totalCost = row.get("total_cost") != null ? 
                            new BigDecimal(row.get("total_cost").toString()) : BigDecimal.ZERO;
                    
                    card.put("materialCost", materialCost);
                    card.put("laborCost", laborCost);
                    card.put("amount", totalCost);
                    card.put("reportedTime", row.get("reported_time"));
                    card.put("completedTime", row.get("actual_complete_time"));
                    
                    // 仅添加有待缴费用的账单
                    if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                        cards.add(card);
                    }
                }
            }

            // 如果没有任何账单
            if (cards.isEmpty()) {
                resp.put("success", true);
                resp.put("data", Collections.emptyList());
                resp.put("message", "无待缴费项目");
                return resp;
            }

            resp.put("success", true);
            resp.put("data", cards);
            resp.put("message", "查询成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return resp;
        }
    }

    private Map<String, Object> buildMeterDueCard(MeterInfo meter) {
        if (meter == null) return null;
        MeterConfig cfg = null;
        if (meter.getConfigId() != null) cfg = meterConfigService.getById(meter.getConfigId());

        QueryWrapper<MeterReading> rdQ = new QueryWrapper<>();
        rdQ.eq("meter_id", meter.getId()).orderByDesc("reading_date").last("limit 1");
        MeterReading last = meterReadingService.getOne(rdQ);

        BigDecimal unitPrice = cfg != null && cfg.getUnitPrice() != null ? cfg.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal usage = last != null && last.getUsageAmount() != null ? last.getUsageAmount() : BigDecimal.ZERO;
        BigDecimal amount = unitPrice.multiply(usage);

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("meterId", meter.getId());
        card.put("houseId", meter.getHouseId());
        card.put("meterName", meter.getMeterName());
        card.put("categoryName", meter.getCategoryName());
        card.put("meterType", meter.getMeterType());
        card.put("unit", cfg != null ? cfg.getUnit() : meter.getUnit());
        card.put("lastReadingDate", last != null ? last.getReadingDate() : null);
        card.put("usage", usage);
        card.put("unitPrice", unitPrice);
        card.put("amount", amount);
        
        House house = houseService.getById(meter.getHouseId());
        if (house != null) {
            card.put("roomNo", house.getRoomNo());
            card.put("fullRoomNo", house.getFullRoomNo());
            Building building = buildingService.getById(house.getBuildingId());
            if (building != null) {
                card.put("buildingNo", building.getBuildingNo());
                card.put("buildingName", building.getBuildingName());
            }
        }
        
        return card;
    }

    @GetMapping("/meters")
    @Operation(summary = "我的仪表", description = "按业主已验证房屋列出关联仪表")
    public Map<String, Object> listMyMeters(
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

            // 使用OwnerQueryService进行单个多表JOIN查询
            List<Map<String, Object>> meterRows = ownerQueryService.listOwnerMeterCardsWithDetails(me.getId());
            if (meterRows == null || meterRows.isEmpty()) {
                resp.put("success", true);
                resp.put("data", Collections.emptyList());
                resp.put("message", "无关联仪表");
                return resp;
            }

            List<Map<String, Object>> items = new ArrayList<>();
            for (Map<String, Object> row : meterRows) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("meterId", row.get("id"));
                item.put("houseId", row.get("house_id"));
                item.put("meterName", row.get("meter_name"));
                item.put("categoryName", row.get("category_name"));
                item.put("meterType", row.get("meter_type"));
                item.put("meterCode", row.get("meter_code"));
                item.put("roomNo", row.get("room_no"));
                item.put("fullRoomNo", row.get("full_room_no"));
                item.put("buildingNo", row.get("building_no"));
                item.put("buildingName", row.get("building_name"));
                item.put("meterStatus", row.get("meter_status"));
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

    @GetMapping("/meter/{meterId}")
    @Operation(summary = "仪表账单详情", description = "展示最近读数、用量、单价、应缴金额等")
    public Map<String, Object> meterDetail(
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
            MeterInfo meter = meterInfoService.getById(meterId);
            if (meter == null) {
                resp.put("success", false);
                resp.put("message", "仪表不存在");
                return resp;
            }
            QueryWrapper<HouseOwner> hoQ = new QueryWrapper<>();
            hoQ.eq("house_id", meter.getHouseId()).eq("owner_id", me.getId()).eq("is_verified", 1);
            if (houseOwnerService.count(hoQ) == 0) {
                resp.put("success", false);
                resp.put("message", "无权查看该仪表");
                return resp;
            }
            Map<String, Object> card = buildMeterDueCard(meter);
            resp.put("success", true);
            resp.put("data", card);
            resp.put("message", "获取成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "获取失败: " + e.getMessage());
            return resp;
        }
    }

    @GetMapping("/history")
    @Operation(summary = "历史缴费记录", description = "按业主展示历史抄表记录与金额（示例以用量*单价估算）")
    public Map<String, Object> history(
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

            QueryWrapper<HouseOwner> hoQ = new QueryWrapper<>();
            hoQ.eq("owner_id", me.getId()).eq("is_verified", 1);
            List<HouseOwner> relations = houseOwnerService.list(hoQ);
            Set<Long> houseIds = new HashSet<>();
            for (HouseOwner r : relations) if (r.getHouseId() != null) houseIds.add(r.getHouseId());
            if (houseIds.isEmpty()) {
                Map<String, Object> pageData = new LinkedHashMap<>();
                pageData.put("page", page);
                pageData.put("size", size);
                pageData.put("total", 0);
                pageData.put("pages", 0);
                pageData.put("items", Collections.emptyList());
                resp.put("success", true);
                resp.put("data", pageData);
                resp.put("message", "无记录");
                return resp;
            }

            QueryWrapper<MeterInfo> miQ = new QueryWrapper<>();
            miQ.in("house_id", houseIds);
            List<MeterInfo> meters = meterInfoService.list(miQ);
            Map<Long, MeterInfo> meterMap = new HashMap<>();
            for (MeterInfo m : meters) meterMap.put(m.getId(), m);

            QueryWrapper<MeterReading> rdQ = new QueryWrapper<>();
            rdQ.in("meter_id", meterMap.keySet()).orderByDesc("reading_date").last("limit " + size + " offset " + ((page - 1) * size));
            List<MeterReading> readings = meterReadingService.list(rdQ);

            QueryWrapper<MeterReading> rdCountQ = new QueryWrapper<>();
            rdCountQ.in("meter_id", meterMap.keySet());
            long total = meterReadingService.count(rdCountQ);

            List<Map<String, Object>> items = new ArrayList<>();
            for (MeterReading r : readings) {
                MeterInfo m = meterMap.get(r.getMeterId());
                MeterConfig cfg = (m != null && m.getConfigId() != null) ? meterConfigService.getById(m.getConfigId()) : null;
                BigDecimal unitPrice = cfg != null && cfg.getUnitPrice() != null ? cfg.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal usage = r.getUsageAmount() != null ? r.getUsageAmount() : BigDecimal.ZERO;
                BigDecimal amount = unitPrice.multiply(usage);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("meterId", r.getMeterId());
                row.put("meterName", m != null ? m.getMeterName() : null);
                row.put("categoryName", m != null ? m.getCategoryName() : null);
                row.put("readingDate", r.getReadingDate());
                row.put("usage", usage);
                row.put("unit", cfg != null ? cfg.getUnit() : (m != null ? m.getUnit() : null));
                row.put("unitPrice", unitPrice);
                row.put("amount", amount);
                row.put("readerId", r.getReaderId());
                row.put("readerName", r.getReaderName());
                items.add(row);
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


