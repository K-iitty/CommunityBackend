package com.community.owner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.owner.service.*;
import com.community.owner.utils.JwtUtil;
import com.community.owner.domain.entity.Owner;
import com.community.owner.domain.entity.ParkingLot;
import com.community.owner.domain.entity.ParkingSpace;
import com.community.owner.domain.entity.Vehicle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/owner/vehicles")
@Tag(name = "业主车辆管理", description = "我的车辆：申请、列表、详情、修改、删除、申请列表")
public class OwnerVehicleController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private VehicleService vehicleService;

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

    public static class VehicleApplyRequest {
        public String plateNumber;
        public String vehicleType;
        public String brand;
        public String model;
        public String color;
        public Long fixedSpaceId;
        public String vehicleLicenseNo;
        public String engineNo;
        public String registerDate; // yyyy-MM-dd
        public String remark;
        public String driverLicenseImage;
        public String vehicleImages; // JSON 数组字符串
    }

    @PostMapping("/apply")
    @Operation(summary = "申请添加车辆", description = "仅保存用户可填写字段，状态置为'申请'，管理员审核通过后置为'正常'")
    public Map<String, Object> applyVehicle(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @RequestBody VehicleApplyRequest req
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }
            if (req == null || req.plateNumber == null || req.plateNumber.trim().isEmpty()) {
                resp.put("success", false);
                resp.put("message", "车牌号必填");
                return resp;
            }

            // 车牌唯一性校验
            QueryWrapper<Vehicle> qwDup = new QueryWrapper<>();
            qwDup.eq("plate_number", req.plateNumber.trim());
            if (vehicleService.count(qwDup) > 0) {
                resp.put("success", false);
                resp.put("message", "车牌号已存在");
                return resp;
            }

            Vehicle v = new Vehicle();
            v.setOwnerId(me.getId());
            v.setPlateNumber(req.plateNumber.trim());
            v.setVehicleType(req.vehicleType);
            v.setBrand(req.brand);
            v.setModel(req.model);
            v.setColor(req.color);
            v.setFixedSpaceId(req.fixedSpaceId);
            v.setVehicleLicenseNo(req.vehicleLicenseNo);
            v.setEngineNo(req.engineNo);
            if (req.registerDate != null && !req.registerDate.isEmpty()) {
                v.setRegisterDate(LocalDate.parse(req.registerDate));
            }
            v.setRemark(req.remark);
            v.setDriverLicenseImage(req.driverLicenseImage);
            v.setVehicleImages(req.vehicleImages);
            v.setStatus("申请");

            boolean ok = vehicleService.save(v);
            if (ok) {
                // 发布实时同步消息
                try {
                    redisMessageService.publishOwnerChange("CREATE", "Vehicle", v.getId(), v);
                    redisMessageService.publishNotification("property", "NEW_VEHICLE_APPLICATION", "新车辆申请", 
                        "业主 " + me.getName() + " 申请添加车辆：" + v.getPlateNumber(), null);
                    redisMessageService.publishNotification("admin", "NEW_VEHICLE_APPLICATION", "新车辆申请", 
                        "业主 " + me.getName() + " 申请添加车辆：" + v.getPlateNumber(), null);
                } catch (Exception e) {
                    System.err.println("发布车辆申请实时消息失败: " + e.getMessage());
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

    @GetMapping
    @Operation(summary = "我的车辆列表", description = "仅展示状态为'正常'的车辆，包含关联的车位信息")
    public Map<String, Object> listMyVehicles(
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

            // 使用OwnerQueryService进行SQL多表JOIN查询，避免N+1问题
            Long total = ownerQueryService.countOwnerVehicles(me.getId());
            List<Map<String, Object>> vehicleRows = ownerQueryService.listOwnerVehiclesWithDetails(
                    me.getId(), size, (page - 1) * size);

            List<Map<String, Object>> items = new ArrayList<>();
            if (vehicleRows != null && !vehicleRows.isEmpty()) {
                for (Map<String, Object> row : vehicleRows) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", row.get("id"));
                    m.put("plateNumber", row.get("plate_number"));
                    m.put("vehicleType", row.get("vehicle_type"));
                    m.put("brand", row.get("brand"));
                    m.put("model", row.get("model"));
                    m.put("color", row.get("color"));
                    m.put("status", row.get("status"));
                    
                    // 添加关联的车位和停车场信息
                    m.put("fixedSpaceNo", row.get("space_no"));
                    m.put("fullSpaceNo", row.get("full_space_no"));
                    m.put("parkingLotName", row.get("lot_name"));
                    m.put("parkingLotCode", row.get("lot_code"));
                    m.put("parkingZoneName", row.get("zone_name"));
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
    @Operation(summary = "车辆详情", description = "需本人且状态为'正常'，包含完整的车位和停车场关联信息")
    public Map<String, Object> getVehicleDetail(
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
            Vehicle v = vehicleService.getById(id);
            if (v == null || !Objects.equals(v.getOwnerId(), me.getId()) || !"正常".equals(v.getStatus())) {
                resp.put("success", false);
                resp.put("message", "无权查看或车辆不存在");
                return resp;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", v.getId());
            m.put("plateNumber", v.getPlateNumber());
            m.put("vehicleType", v.getVehicleType());
            m.put("brand", v.getBrand());
            m.put("model", v.getModel());
            m.put("color", v.getColor());
            m.put("fixedSpaceId", v.getFixedSpaceId());
            m.put("vehicleLicenseNo", v.getVehicleLicenseNo());
            m.put("engineNo", v.getEngineNo());
            m.put("registerDate", v.getRegisterDate());
            m.put("remark", v.getRemark());
            m.put("driverLicenseImage", v.getDriverLicenseImage());
            m.put("vehicleImages", v.getVehicleImages());
            m.put("status", v.getStatus());

            // 关联车位信息
            if (v.getFixedSpaceId() != null) {
                ParkingSpace space = parkingSpaceService.getById(v.getFixedSpaceId());
                if (space != null) {
                    Map<String, Object> spaceInfo = new LinkedHashMap<>();
                    spaceInfo.put("id", space.getId());
                    spaceInfo.put("spaceNo", space.getSpaceNo());
                    spaceInfo.put("fullSpaceNo", space.getFullSpaceNo());
                    spaceInfo.put("spaceType", space.getSpaceType());
                    spaceInfo.put("spaceStatus", space.getSpaceStatus());
                    spaceInfo.put("monthlyFee", space.getMonthlyFee());
                    m.put("parkingSpace", spaceInfo);
                    
                    // 关联停车场信息
                    ParkingLot lot = parkingLotService.getById(space.getParkingLotId());
                    if (lot != null) {
                        Map<String, Object> lotInfo = new LinkedHashMap<>();
                        lotInfo.put("id", lot.getId());
                        lotInfo.put("lotName", lot.getLotName());
                        lotInfo.put("lotCode", lot.getLotCode());
                        lotInfo.put("zoneName", lot.getZoneName());
                        lotInfo.put("address", lot.getAddress());
                        lotInfo.put("detailAddress", lot.getDetailAddress());
                        lotInfo.put("businessHours", lot.getBusinessHours());
                        lotInfo.put("chargeMethod", lot.getChargeMethod());
                        lotInfo.put("chargeStandard", lot.getChargeStandard());
                        lotInfo.put("monthlyFee", lot.getMonthlyFee());
                        lotInfo.put("status", lot.getStatus());
                        m.put("parkingLot", lotInfo);
                    }
                }
            }

            resp.put("success", true);
            resp.put("data", m);
            resp.put("message", "获取成功");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "获取失败: " + e.getMessage());
            return resp;
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改车辆信息(重新申请)", description = "修改后状态置为'申请'，待管理员审核通过才在列表显示")
    public Map<String, Object> updateVehicle(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id,
            @RequestBody VehicleApplyRequest req
    ) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Owner me = getCurrentOwner(token);
            if (me == null) {
                resp.put("success", false);
                resp.put("message", "用户不存在");
                return resp;
            }
            Vehicle v = vehicleService.getById(id);
            if (v == null || !Objects.equals(v.getOwnerId(), me.getId())) {
                resp.put("success", false);
                resp.put("message", "无权修改或车辆不存在");
                return resp;
            }
            // 如果修改了车牌，需校验唯一
            if (req.plateNumber != null && !req.plateNumber.trim().equalsIgnoreCase(v.getPlateNumber())) {
                QueryWrapper<Vehicle> qwDup = new QueryWrapper<>();
                qwDup.eq("plate_number", req.plateNumber.trim());
                if (vehicleService.count(qwDup) > 0) {
                    resp.put("success", false);
                    resp.put("message", "车牌号已存在");
                    return resp;
                }
                v.setPlateNumber(req.plateNumber.trim());
            }
            if (req.vehicleType != null) v.setVehicleType(req.vehicleType);
            if (req.brand != null) v.setBrand(req.brand);
            if (req.model != null) v.setModel(req.model);
            if (req.color != null) v.setColor(req.color);
            if (req.fixedSpaceId != null) v.setFixedSpaceId(req.fixedSpaceId);
            if (req.vehicleLicenseNo != null) v.setVehicleLicenseNo(req.vehicleLicenseNo);
            if (req.engineNo != null) v.setEngineNo(req.engineNo);
            if (req.registerDate != null && !req.registerDate.isEmpty()) v.setRegisterDate(LocalDate.parse(req.registerDate));
            if (req.remark != null) v.setRemark(req.remark);
            if (req.driverLicenseImage != null) v.setDriverLicenseImage(req.driverLicenseImage);
            if (req.vehicleImages != null) v.setVehicleImages(req.vehicleImages);

            v.setStatus("申请");

            boolean ok = vehicleService.updateById(v);
            if (ok) {
                // 发布实时同步消息
                try {
                    redisMessageService.publishOwnerChange("UPDATE", "Vehicle", v.getId(), v);
                    redisMessageService.publishNotification("property", "VEHICLE_UPDATE", "车辆信息更新", 
                        "业主 " + me.getName() + " 更新了车辆信息：" + v.getPlateNumber(), null);
                    redisMessageService.publishNotification("admin", "VEHICLE_UPDATE", "车辆信息更新", 
                        "业主 " + me.getName() + " 更新了车辆信息：" + v.getPlateNumber(), null);
                } catch (Exception e) {
                    System.err.println("发布车辆更新实时消息失败: " + e.getMessage());
                }
                
                resp.put("success", true);
                resp.put("message", "修改已提交，等待审核");
            } else {
                resp.put("success", false);
                resp.put("message", "修改失败");
            }
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "修改失败: " + e.getMessage());
            return resp;
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除车辆", description = "直接删除该车辆记录")
    public Map<String, Object> deleteVehicle(
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
            Vehicle v = vehicleService.getById(id);
            if (v == null || !Objects.equals(v.getOwnerId(), me.getId())) {
                resp.put("success", false);
                resp.put("message", "无权删除或车辆不存在");
                return resp;
            }
            boolean ok = vehicleService.removeById(id);
            if (ok) {
                // 发布实时同步消息
                try {
                    redisMessageService.publishOwnerChange("DELETE", "Vehicle", id, null);
                    redisMessageService.publishNotification("property", "VEHICLE_DELETE", "车辆删除", 
                        "业主 " + me.getName() + " 删除了车辆：" + v.getPlateNumber(), null);
                    redisMessageService.publishNotification("admin", "VEHICLE_DELETE", "车辆删除", 
                        "业主 " + me.getName() + " 删除了车辆：" + v.getPlateNumber(), null);
                } catch (Exception e) {
                    System.err.println("发布车辆删除实时消息失败: " + e.getMessage());
                }
                
                resp.put("success", true);
                resp.put("message", "删除成功");
            } else {
                resp.put("success", false);
                resp.put("message", "删除失败");
            }
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "删除失败: " + e.getMessage());
            return resp;
        }
    }

    @GetMapping("/applications")
    @Operation(summary = "我的车辆申请列表", description = "列出本人状态为'申请'的车辆")
    public Map<String, Object> listMyVehicleApplications(
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
            QueryWrapper<Vehicle> qw = new QueryWrapper<>();
            qw.eq("owner_id", me.getId()).eq("status", "申请").orderByDesc("id");
            List<Vehicle> list = vehicleService.list(qw);
            List<Map<String, Object>> items = new ArrayList<>();
            for (Vehicle v : list) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", v.getId());
                m.put("plateNumber", v.getPlateNumber());
                m.put("vehicleType", v.getVehicleType());
                m.put("brand", v.getBrand());
                m.put("model", v.getModel());
                m.put("color", v.getColor());
                m.put("status", v.getStatus());
                items.add(m);
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
}


