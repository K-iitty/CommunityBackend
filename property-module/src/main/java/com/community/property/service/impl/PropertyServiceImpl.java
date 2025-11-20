package com.community.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.property.domain.entity.*;
import com.community.property.mapper.*;
import com.community.property.domain.dto.request.StaffProfileUpdateRequest;
import com.community.property.service.PropertyService;
import com.community.property.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物业服务实现类
 */
@Service
public class PropertyServiceImpl implements PropertyService {

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private MeterInfoMapper meterInfoMapper;

    @Autowired
    private MeterConfigMapper meterConfigMapper;

    @Autowired
    private MeterReadingMapper meterReadingMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    @Autowired
    private HouseOwnerMapper houseOwnerMapper;

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private CommunityNoticeMapper communityNoticeMapper;

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private CommunityInfoMapper communityInfoMapper;

    @Autowired
    private ImageService imageService;

    // ==================== 员工个人信息相关 ====================

    @Override
    public Map<String, Object> getStaffProfile(Long staffId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                response.put("success", false);
                response.put("message", "员工不存在");
                return response;
            }

            // 获取部门信息
            Department dept = null;
            if (staff.getDepartmentId() != null) {
                dept = departmentMapper.selectById(staff.getDepartmentId());
            }

            Map<String, Object> staffInfo = new LinkedHashMap<>();
            staffInfo.put("id", staff.getId());
            staffInfo.put("name", staff.getName());
            staffInfo.put("phone", staff.getPhone());
            staffInfo.put("email", staff.getEmail());
            staffInfo.put("wechat", staff.getWechat());
            staffInfo.put("position", staff.getPosition());
            staffInfo.put("jobTitle", staff.getJobTitle());
            staffInfo.put("hireDate", staff.getHireDate());
            staffInfo.put("workStatus", staff.getWorkStatus());
            staffInfo.put("departmentId", staff.getDepartmentId());
            if (dept != null) {
                staffInfo.put("departmentName", dept.getDepartmentName());
            }
            staffInfo.put("avatar", staff.getAvatar());
            staffInfo.put("gender", staff.getGender());

            response.put("success", true);
            response.put("data", staffInfo);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateStaffProfile(Long staffId, StaffProfileUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                response.put("success", false);
                response.put("message", "员工不存在");
                return response;
            }

            // 更新允许修改的字段
            if (request.getPhone() != null) {
                staff.setPhone(request.getPhone());
            }
            if (request.getEmail() != null) {
                staff.setEmail(request.getEmail());
            }
            if (request.getWechat() != null) {
                staff.setWechat(request.getWechat());
            }
            if (request.getAvatar() != null) {
                staff.setAvatar(request.getAvatar());
            }
            if (request.getGender() != null) {
                staff.setGender(request.getGender());
            }

            staffMapper.updateById(staff);

            response.put("success", true);
            response.put("message", "个人信息更新成功");
            response.put("data", staff);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> getDepartmentInfo(Long departmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Department dept = departmentMapper.selectById(departmentId);
            if (dept == null) {
                response.put("success", false);
                response.put("message", "部门不存在");
                return response;
            }

            Map<String, Object> deptInfo = new LinkedHashMap<>();
            deptInfo.put("id", dept.getId());
            deptInfo.put("departmentName", dept.getDepartmentName());
            deptInfo.put("departmentCode", dept.getDepartmentCode());
            deptInfo.put("description", dept.getDescription());
            deptInfo.put("status", dept.getStatus());

            response.put("success", true);
            response.put("data", deptInfo);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listDepartments() {
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "启用");
            queryWrapper.orderByAsc("sort_order");

            List<Department> depts = departmentMapper.selectList(queryWrapper);
            List<Map<String, Object>> deptList = depts.stream().map(dept -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", dept.getId());
                map.put("departmentName", dept.getDepartmentName());
                map.put("departmentCode", dept.getDepartmentCode());
                return map;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("data", deptList);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    // ==================== 仪表相关操作 ====================

    @Override
    public Map<String, Object> listOwnerMeters(Long ownerId, Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            // 先查询业主关联的房屋
            QueryWrapper<HouseOwner> houseOwnerQuery = new QueryWrapper<>();
            houseOwnerQuery.eq("owner_id", ownerId);
            List<HouseOwner> houseOwners = houseOwnerMapper.selectList(houseOwnerQuery);

            List<Long> houseIds = houseOwners.stream().map(HouseOwner::getHouseId).collect(Collectors.toList());

            final int finalPage = page;
            final int finalSize = size;
            
            if (houseIds.isEmpty()) {
                response.put("success", true);
                response.put("data", new LinkedHashMap<String, Object>() {{
                    put("page", finalPage);
                    put("size", finalSize);
                    put("total", 0L);
                    put("items", new ArrayList<>());
                }});
                return response;
            }

            // 查询这些房屋对应的仪表
            QueryWrapper<MeterInfo> meterQuery = new QueryWrapper<>();
            meterQuery.in("house_id", houseIds);
            meterQuery.orderByDesc("created_at");

            int offset = (finalPage - 1) * finalSize;
            meterQuery.last("LIMIT " + finalSize + " OFFSET " + offset);

            List<MeterInfo> meters = meterInfoMapper.selectList(meterQuery);
            long total = meterInfoMapper.selectCount(new QueryWrapper<MeterInfo>().in("house_id", houseIds));

            List<Map<String, Object>> meterList = meters.stream().map(meter -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", meter.getId());
                map.put("meterName", meter.getMeterName());
                map.put("categoryName", meter.getCategoryName());
                map.put("meterCode", meter.getMeterCode());
                map.put("currentReading", meter.getCurrentReading());
                map.put("unit", meter.getUnit());
                map.put("meterStatus", meter.getMeterStatus());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", finalPage);
            pageData.put("size", finalSize);
            pageData.put("total", total);
            pageData.put("pages", finalSize == 0 ? 0 : ((total + finalSize - 1) / finalSize));
            pageData.put("items", meterList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> getAllOwnersWithMeters(Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 20;

            // 查询所有业主
            QueryWrapper<Owner> ownerQuery = new QueryWrapper<>();
            ownerQuery.orderByDesc("created_at");
            
            int offset = (page - 1) * size;
            ownerQuery.last("LIMIT " + size + " OFFSET " + offset);
            
            List<Owner> owners = ownerMapper.selectList(ownerQuery);
            long totalOwners = ownerMapper.selectCount(new QueryWrapper<>());

            final int finalPage = page;
            final int finalSize = size;

            // 构建返回数据，包含每个业主的仪表信息
            List<Map<String, Object>> ownersList = owners.stream().map(owner -> {
                Map<String, Object> ownerMap = new LinkedHashMap<>();
                ownerMap.put("ownerId", owner.getId());
                ownerMap.put("ownerName", owner.getName());
                ownerMap.put("houseName", ""); // 默认为空

                // 获取该业主的房屋
                List<HouseOwner> houseOwners = houseOwnerMapper.selectList(
                    new QueryWrapper<HouseOwner>().eq("owner_id", owner.getId())
                );
                List<Long> houseIds = houseOwners.stream()
                    .map(HouseOwner::getHouseId)
                    .collect(Collectors.toList());

                // 获取该业主名下的所有仪表（初始化为空列表）
                List<Map<String, Object>> meters = new ArrayList<>();
                
                if (!houseIds.isEmpty()) {
                    // 获取第一个房屋的信息用于显示
                    House house = houseMapper.selectById(houseIds.get(0));
                    if (house != null) {
                        ownerMap.put("houseName", house.getRoomNo() != null ? house.getRoomNo() : "");
                    }

                    // 查询仪表
                    List<MeterInfo> meterList = meterInfoMapper.selectList(
                        new QueryWrapper<MeterInfo>().in("house_id", houseIds)
                    );
                    
                    if (meterList != null && !meterList.isEmpty()) {
                        meters = meterList.stream().map(meter -> {
                            Map<String, Object> meterMap = new LinkedHashMap<>();
                            meterMap.put("meterId", meter.getId());
                            meterMap.put("meterName", meter.getMeterName());
                            meterMap.put("meterCode", meter.getMeterCode());
                            meterMap.put("meterType", meter.getMeterType());
                            meterMap.put("categoryName", meter.getCategoryName());
                            meterMap.put("initialReading", meter.getInitialReading() != null ? meter.getInitialReading() : 0);
                            meterMap.put("currentReading", meter.getCurrentReading() != null ? meter.getCurrentReading() : 0);
                            meterMap.put("createdAt", meter.getCreatedAt());
                            meterMap.put("lastCommTime", meter.getLastCommTime());
                            meterMap.put("meterStatus", meter.getMeterStatus());
                            meterMap.put("unit", meter.getUnit());
                            return meterMap;
                        }).collect(Collectors.toList());
                    }
                }

                ownerMap.put("meters", meters);
                return ownerMap;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", finalPage);
            pageData.put("size", finalSize);
            pageData.put("total", totalOwners);
            pageData.put("pages", finalSize == 0 ? 0 : ((totalOwners + finalSize - 1) / finalSize));
            pageData.put("items", ownersList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listMeterConfigs(String categoryName, Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<MeterConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "启用");
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                queryWrapper.eq("category_name", categoryName);
            }
            queryWrapper.orderByAsc("category_name");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<MeterConfig> configs = meterConfigMapper.selectList(queryWrapper);
            long total = meterConfigMapper.selectCount(new QueryWrapper<MeterConfig>()
                    .eq("status", "启用")
                    .eq(categoryName != null && !categoryName.trim().isEmpty(), "category_name", categoryName));

            List<Map<String, Object>> configList = configs.stream().map(config -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", config.getId());
                map.put("categoryName", config.getCategoryName());
                map.put("meterType", config.getMeterType());
                map.put("unit", config.getUnit());
                map.put("unitPrice", config.getUnitPrice());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", configList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> addMeterToOwner(Long communityId, Long houseId, Long meterConfigId, 
            String categoryName, String meterType, String meterCode, String meterSn, String meterName, 
            String installLocation, String installDate, Double initialReading, String commAddress, String remark) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (communityId == null || houseId == null || meterConfigId == null || meterCode == null) {
                response.put("success", false);
                response.put("message", "社区ID、房屋ID、配置ID、仪表编码为必需");
                return response;
            }

            MeterInfo meter = new MeterInfo();
            meter.setCommunityId(communityId);
            meter.setHouseId(houseId);
            meter.setConfigId(meterConfigId);
            meter.setCategoryName(categoryName);
            meter.setMeterType(meterType);
            meter.setMeterCode(meterCode);
            meter.setMeterSn(meterSn);
            meter.setMeterName(meterName);
            meter.setInstallLocation(installLocation);
            // TODO: 处理installDate字段
            if (initialReading != null) meter.setInitialReading(new java.math.BigDecimal(initialReading));
            meter.setCommAddress(commAddress);
            meter.setRemark(remark);

            meterInfoMapper.insert(meter);

            response.put("success", true);
            response.put("message", "仪表添加成功");
            response.put("data", meter);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> addMeterConfig(String categoryName, String meterType, String productId, 
            String unit, Double unitPrice, Integer decimalPlaces, String chargeStandard, 
            String calculationMethod, String commProtocol, String remark) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (categoryName == null || meterType == null) {
                response.put("success", false);
                response.put("message", "分类名称和仪表类型为必需");
                return response;
            }

            MeterConfig config = new MeterConfig();
            config.setCategoryName(categoryName);
            config.setMeterType(meterType);
            config.setProductId(productId);
            config.setUnit(unit);
            if (unitPrice != null) config.setUnitPrice(new java.math.BigDecimal(unitPrice));
            config.setDecimalPlaces(decimalPlaces);
            config.setChargeStandard(chargeStandard);
            config.setCalculationMethod(calculationMethod);
            config.setCommProtocol(commProtocol);
            config.setRemark(remark);

            meterConfigMapper.insert(config);

            response.put("success", true);
            response.put("message", "配置添加成功");
            response.put("data", config);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> addMeterReading(Long meterId, Double previousReading, Double currentReading, Long staffId, 
            Double usageAmount, String readingDate, String readingTime, String unit, String readingType, String readingStatus, 
            String abnormalReason, String remark, MultipartFile readingImage) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证必填字段
            if (meterId == null || previousReading == null || currentReading == null || 
                usageAmount == null || readingDate == null || staffId == null) {
                response.put("success", false);
                response.put("message", "仪表ID、上次读数、当前读数、用量、抄表日期、抄表人为必需");
                return response;
            }

            // 获取员工信息（reader_name）
            Staff staff = staffMapper.selectById(staffId);
            String readerName = (staff != null) ? staff.getName() : "";

            // 获取仪表信息（category_name）
            MeterInfo meterInfo = meterInfoMapper.selectById(meterId);
            String categoryName = (meterInfo != null) ? meterInfo.getCategoryName() : "";

            MeterReading reading = new MeterReading();
            reading.setMeterId(meterId);
            reading.setPreviousReading(new java.math.BigDecimal(previousReading));
            reading.setCurrentReading(new java.math.BigDecimal(currentReading));
            reading.setUsageAmount(new java.math.BigDecimal(usageAmount));
            reading.setReaderId(staffId);
            reading.setReaderName(readerName);  // 添加员工姓名
            reading.setCategoryName(categoryName);  // 添加仪表分类
            
            // 处理readingDate
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(readingDate);
                reading.setReadingDate(date);
            } catch (Exception e) {
                reading.setReadingDate(java.time.LocalDate.now());
            }
            
            // 处理readingTime - 现在是可选的
            if (readingTime != null && !readingTime.trim().isEmpty()) {
                try {
                    java.time.LocalDateTime dateTime;
                    if (readingTime.contains("T")) {
                        dateTime = java.time.LocalDateTime.parse(readingTime);
                    } else if (readingTime.contains(":")) {
                        // HH:mm 或 HH:mm:ss 格式
                        String[] parts = readingTime.split(":");
                        int hour = Integer.parseInt(parts[0]);
                        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                        int second = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                        dateTime = java.time.LocalDateTime.parse(readingDate + "T" + 
                            String.format("%02d:%02d:%02d", hour, minute, second));
                    } else {
                        dateTime = java.time.LocalDateTime.now();
                    }
                    reading.setReadingTime(dateTime);
                } catch (Exception e) {
                    reading.setReadingTime(java.time.LocalDateTime.now());
                }
            } else {
                reading.setReadingTime(java.time.LocalDateTime.now());
            }
            
            reading.setReadingType(readingType != null ? readingType : "手动");
            reading.setReadingStatus(readingStatus != null ? readingStatus : "正常");
            reading.setAbnormalReason(abnormalReason);
            reading.setRemark(remark);
            reading.setUnit(unit);

            // 处理读数图片
            if (readingImage != null && !readingImage.isEmpty()) {
                String imagePath = imageService.uploadImages(
                    java.util.Arrays.asList(readingImage), "meter/reading", meterId)
                    .stream().findFirst().orElse(null);
                reading.setReadingImage(imagePath);
            }

            meterReadingMapper.insert(reading);

            response.put("success", true);
            response.put("message", "读数添加成功");
            response.put("data", reading);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listMeterReadings(Long meterId, Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<MeterReading> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("meter_id", meterId);
            queryWrapper.orderByDesc("reading_date");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<MeterReading> readings = meterReadingMapper.selectList(queryWrapper);
            long total = meterReadingMapper.selectCount(new QueryWrapper<MeterReading>().eq("meter_id", meterId));

            List<Map<String, Object>> readingList = readings.stream().map(reading -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", reading.getId());
                map.put("previousReading", reading.getPreviousReading());
                map.put("currentReading", reading.getCurrentReading());
                map.put("usageAmount", reading.getUsageAmount());
                map.put("readingDate", reading.getReadingDate());
                map.put("readerName", reading.getReaderName());
                map.put("readingStatus", reading.getReadingStatus());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", readingList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateMeterConfig(Long configId, String categoryName, String meterType, String productId,
            String unit, Double unitPrice, Integer decimalPlaces, String chargeStandard,
            String calculationMethod, String commProtocol, String status, String remark) {
        Map<String, Object> response = new HashMap<>();
        try {
            MeterConfig config = meterConfigMapper.selectById(configId);
            if (config == null) {
                response.put("success", false);
                response.put("message", "仪表配置不存在");
                return response;
            }

            if (categoryName != null) config.setCategoryName(categoryName);
            if (meterType != null) config.setMeterType(meterType);
            if (productId != null) config.setProductId(productId);
            if (unit != null) config.setUnit(unit);
            if (unitPrice != null) config.setUnitPrice(new java.math.BigDecimal(unitPrice));
            if (decimalPlaces != null) config.setDecimalPlaces(decimalPlaces);
            if (chargeStandard != null) config.setChargeStandard(chargeStandard);
            if (calculationMethod != null) config.setCalculationMethod(calculationMethod);
            if (commProtocol != null) config.setCommProtocol(commProtocol);
            if (status != null) config.setStatus(status);
            if (remark != null) config.setRemark(remark);

            meterConfigMapper.updateById(config);

            response.put("success", true);
            response.put("message", "仪表配置更新成功");
            response.put("data", config);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> deleteMeterConfig(Long configId) {
        Map<String, Object> response = new HashMap<>();
        try {
            MeterConfig config = meterConfigMapper.selectById(configId);
            if (config == null) {
                response.put("success", false);
                response.put("message", "仪表配置不存在");
                return response;
            }

            // 检查是否有仪表使用此配置
            QueryWrapper<MeterInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("config_id", configId);
            long count = meterInfoMapper.selectCount(queryWrapper);
            if (count > 0) {
                response.put("success", false);
                response.put("message", "此配置已被使用，无法删除");
                return response;
            }

            meterConfigMapper.deleteById(configId);

            response.put("success", true);
            response.put("message", "仪表配置删除成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateMeterInfo(Long meterId, String meterName, String meterSn, String installLocation,
            String installDate, String commAddress, String meterStatus, String remark) {
        Map<String, Object> response = new HashMap<>();
        try {
            MeterInfo meter = meterInfoMapper.selectById(meterId);
            if (meter == null) {
                response.put("success", false);
                response.put("message", "仪表不存在");
                return response;
            }

            if (meterName != null) meter.setMeterName(meterName);
            if (meterSn != null) meter.setMeterSn(meterSn);
            if (installLocation != null) meter.setInstallLocation(installLocation);
            if (installDate != null) {
                try {
                    meter.setInstallDate(java.time.LocalDate.parse(installDate));
                } catch (Exception e) {
                    // 如果解析失败，忽略此字段
                }
            }
            if (commAddress != null) meter.setCommAddress(commAddress);
            if (meterStatus != null) meter.setMeterStatus(meterStatus);
            if (remark != null) meter.setRemark(remark);

            meterInfoMapper.updateById(meter);

            response.put("success", true);
            response.put("message", "仪表信息更新成功");
            response.put("data", meter);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> deleteMeterInfo(Long meterId) {
        Map<String, Object> response = new HashMap<>();
        try {
            MeterInfo meter = meterInfoMapper.selectById(meterId);
            if (meter == null) {
                response.put("success", false);
                response.put("message", "仪表不存在");
                return response;
            }

            // 删除相关的抄表记录
            QueryWrapper<MeterReading> readingWrapper = new QueryWrapper<>();
            readingWrapper.eq("meter_id", meterId);
            meterReadingMapper.delete(readingWrapper);

            // 删除仪表
            meterInfoMapper.deleteById(meterId);

            response.put("success", true);
            response.put("message", "仪表删除成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> getHouseList(Long communityId, Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 20;

            QueryWrapper<House> queryWrapper = new QueryWrapper<>();
            if (communityId != null) {
                queryWrapper.eq("community_id", communityId);
            }
            queryWrapper.orderByAsc("house_code");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<House> houses = houseMapper.selectList(queryWrapper);
            long total = houseMapper.selectCount(communityId == null ? new QueryWrapper<>() : 
                    new QueryWrapper<House>().eq("community_id", communityId));

            List<Map<String, Object>> houseList = houses.stream().map(house -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", house.getId());
                map.put("houseCode", house.getHouseCode());
                map.put("houseName", house.getFullRoomNo() != null ? house.getFullRoomNo() : house.getRoomNo());
                map.put("buildingId", house.getBuildingId());
                map.put("roomNo", house.getRoomNo());
                map.put("fullRoomNo", house.getFullRoomNo());
                map.put("floorLevel", house.getFloorLevel());
                map.put("buildingArea", house.getBuildingArea());
                map.put("houseType", house.getHouseType());
                map.put("houseLayout", house.getHouseLayout());
                map.put("houseStatus", house.getHouseStatus());
                map.put("communityId", house.getCommunityId());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", houseList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    // ==================== 抄表记录相关 ====================

    @Override
    public Map<String, Object> getMeterDetail(Long meterId) {
        Map<String, Object> response = new HashMap<>();
        try {
            MeterInfo meter = meterInfoMapper.selectById(meterId);
            if (meter == null) {
                response.put("success", false);
                response.put("message", "仪表不存在");
                return response;
            }
            // TODO: 关联查询meter_config和最近的读数
            response.put("success", true);
            response.put("data", meter);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    // ==================== 车辆和车位相关 ====================

    @Override
    @Transactional
    public Map<String, Object> addVehicle(Long ownerId, String plateNumber, String brand, String model, String color) {
        Map<String, Object> response = new HashMap<>();
        try {
            Owner owner = ownerMapper.selectById(ownerId);
            if (owner == null) {
                response.put("success", false);
                response.put("message", "业主不存在");
                return response;
            }

            Vehicle vehicle = new Vehicle();
            vehicle.setOwnerId(ownerId);
            vehicle.setPlateNumber(plateNumber);
            vehicle.setBrand(brand);
            vehicle.setModel(model);
            vehicle.setColor(color);
            vehicle.setVehicleType("小型车");
            vehicle.setStatus("正常");
            vehicle.setRegisterDate(LocalDate.now());

            vehicleMapper.insert(vehicle);

            response.put("success", true);
            response.put("message", "车辆添加成功");
            response.put("data", vehicle);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listOwnerVehicles(Long ownerId, Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<Vehicle> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("owner_id", ownerId);
            queryWrapper.eq("status", "正常");
            queryWrapper.orderByDesc("created_at");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<Vehicle> vehicles = vehicleMapper.selectList(queryWrapper);
            long total = vehicleMapper.selectCount(new QueryWrapper<Vehicle>()
                    .eq("owner_id", ownerId)
                    .eq("status", "正常"));

            List<Map<String, Object>> vehicleList = vehicles.stream().map(vehicle -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", vehicle.getId());
                map.put("plateNumber", vehicle.getPlateNumber());
                map.put("brand", vehicle.getBrand());
                map.put("model", vehicle.getModel());
                map.put("color", vehicle.getColor());
                map.put("vehicleType", vehicle.getVehicleType());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", vehicleList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listAllVehicles(Integer page, Integer size, String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<Vehicle> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "正常");
            
            // 如果有搜索关键词，按车牌号或品牌搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(q -> q
                    .like("plate_number", keyword)
                    .or()
                    .like("brand", keyword)
                );
            }
            
            queryWrapper.orderByDesc("created_at");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<Vehicle> vehicles = vehicleMapper.selectList(queryWrapper);
            long total = vehicleMapper.selectCount(new QueryWrapper<Vehicle>()
                    .eq("status", "正常")
                    .and(keyword != null && !keyword.trim().isEmpty(), 
                        q -> q.like("plate_number", keyword).or().like("brand", keyword)));

            List<Map<String, Object>> vehicleList = vehicles.stream().map(vehicle -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", vehicle.getId());
                map.put("plateNumber", vehicle.getPlateNumber());
                map.put("brand", vehicle.getBrand() != null ? vehicle.getBrand() : "");
                map.put("model", vehicle.getModel() != null ? vehicle.getModel() : "");
                map.put("color", vehicle.getColor() != null ? vehicle.getColor() : "");
                map.put("vehicleType", vehicle.getVehicleType());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", vehicleList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    // ==================== 停车场相关 ====================

    @Override
    @Transactional
    public Map<String, Object> addParkingLot(Long communityId, String lotName, String lotCode, String lotCategory, 
            String zoneName, String zoneCode, String contactPerson, String contactPhone, String address, 
            String detailAddress, Integer totalSpaces, Integer fixedSpaces, Integer tempSpaces, 
            String businessHours, String chargeMethod, Double monthlyFee, String remark) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (communityId == null || lotName == null || totalSpaces == null) {
                response.put("success", false);
                response.put("message", "社区ID、停车场名称、总车位数为必需");
                return response;
            }

            ParkingLot lot = new ParkingLot();
            lot.setCommunityId(communityId);
            lot.setLotName(lotName);
            lot.setLotCode(lotCode);
            lot.setLotCategory(lotCategory);
            lot.setZoneName(zoneName);
            lot.setZoneCode(zoneCode);
            lot.setContactPerson(contactPerson);
            lot.setContactPhone(contactPhone);
            lot.setAddress(address);
            lot.setDetailAddress(detailAddress);
            lot.setTotalSpaces(totalSpaces);
            lot.setFixedSpaces(fixedSpaces != null ? fixedSpaces : 0);
            lot.setTempSpaces(tempSpaces != null ? tempSpaces : 0);
            lot.setBusinessHours(businessHours);
            lot.setChargeMethod(chargeMethod);
            if (monthlyFee != null) lot.setMonthlyFee(new java.math.BigDecimal(monthlyFee));
            lot.setRemark(remark);

            parkingLotMapper.insert(lot);

            response.put("success", true);
            response.put("message", "停车场添加成功");
            response.put("data", lot);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> getParkingLotDetail(Long lotId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ParkingLot lot = parkingLotMapper.selectById(lotId);
            if (lot == null) {
                response.put("success", false);
                response.put("message", "停车场不存在");
                return response;
            }
            // TODO: 关联查询社区信息
            response.put("success", true);
            response.put("data", lot);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> getParkingSpaceDetail(Long spaceId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ParkingSpace space = parkingSpaceMapper.selectById(spaceId);
            if (space == null) {
                response.put("success", false);
                response.put("message", "停车位不存在");
                return response;
            }
            // TODO: 关联查询停车场、业主、车辆信息
            response.put("success", true);
            response.put("data", space);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> addParkingSpace(Long parkingLotId, String spaceNo, String fullSpaceNo, 
            String spaceType, Double spaceArea, String spaceStatus, Long ownerId, Long vehicleId, 
            Double monthlyFee, String remark) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (parkingLotId == null || spaceNo == null) {
                response.put("success", false);
                response.put("message", "停车场ID和车位号为必需");
                return response;
            }

            ParkingSpace space = new ParkingSpace();
            space.setParkingLotId(parkingLotId);
            space.setSpaceNo(spaceNo);
            space.setFullSpaceNo(fullSpaceNo);
            space.setSpaceType(spaceType);
            if (spaceArea != null) space.setSpaceArea(new java.math.BigDecimal(spaceArea));
            space.setSpaceStatus(spaceStatus != null ? spaceStatus : "空闲");
            space.setOwnerId(ownerId);
            space.setVehicleId(vehicleId);
            if (monthlyFee != null) space.setMonthlyFee(new java.math.BigDecimal(monthlyFee));
            space.setRemark(remark);

            parkingSpaceMapper.insert(space);

            response.put("success", true);
            response.put("message", "车位添加成功");
            response.put("data", space);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listParkingSpaces(Long parkingLotId, Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<ParkingSpace> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parking_lot_id", parkingLotId);
            queryWrapper.orderByAsc("space_no");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<ParkingSpace> spaces = parkingSpaceMapper.selectList(queryWrapper);
            long total = parkingSpaceMapper.selectCount(new QueryWrapper<ParkingSpace>().eq("parking_lot_id", parkingLotId));

            List<Map<String, Object>> spaceList = spaces.stream().map(space -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", space.getId());
                map.put("spaceNo", space.getSpaceNo());
                map.put("spaceType", space.getSpaceType());
                map.put("spaceStatus", space.getSpaceStatus());
                map.put("monthlyFee", space.getMonthlyFee());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", spaceList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listAllParkingSpaces(Integer page, Integer size, String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<ParkingSpace> queryWrapper = new QueryWrapper<>();
            
            // 支持按状态筛选
            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("space_status", status);
            }
            
            queryWrapper.orderByAsc("space_no");

            long total = parkingSpaceMapper.selectCount(queryWrapper);

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<ParkingSpace> spaces = parkingSpaceMapper.selectList(queryWrapper);

            List<Map<String, Object>> spaceList = spaces.stream().map(space -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("parkingLotId", space.getParkingLotId());
                map.put("spaceNumber", space.getSpaceNo());
                map.put("fullSpaceNo", space.getFullSpaceNo());
                map.put("spaceType", space.getSpaceType() != null ? space.getSpaceType() : "固定");
                map.put("spaceArea", space.getSpaceArea());
                map.put("status", space.getSpaceStatus() != null ? space.getSpaceStatus() : "空闲");
                map.put("monthlyFee", space.getMonthlyFee());
                map.put("remark", space.getRemark());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", spaceList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    // ==================== 房屋关联相关 ====================

    @Override
    @Transactional
    public Map<String, Object> associateOwnerToHouse(Long ownerId, Long houseId, String relationship) {
        Map<String, Object> response = new HashMap<>();
        try {
            Owner owner = ownerMapper.selectById(ownerId);
            if (owner == null) {
                response.put("success", false);
                response.put("message", "业主不存在");
                return response;
            }

            House house = houseMapper.selectById(houseId);
            if (house == null) {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return response;
            }

            HouseOwner houseOwner = new HouseOwner();
            houseOwner.setHouseId(houseId);
            houseOwner.setOwnerId(ownerId);
            houseOwner.setRelationship(relationship);
            houseOwner.setStartDate(LocalDate.now());
            houseOwner.setStatus("正常");

            houseOwnerMapper.insert(houseOwner);

            response.put("success", true);
            response.put("message", "房屋关联成功");
            response.put("data", houseOwner);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "关联失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listOwnerHouses(Long ownerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<HouseOwner> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("owner_id", ownerId);
            queryWrapper.eq("status", "正常");

            List<HouseOwner> houseOwners = houseOwnerMapper.selectList(queryWrapper);

            List<Map<String, Object>> houseList = houseOwners.stream().map(ho -> {
                House house = houseMapper.selectById(ho.getHouseId());
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", house.getId());
                map.put("roomNo", house.getRoomNo());
                map.put("fullRoomNo", house.getFullRoomNo());
                map.put("houseType", house.getHouseType());
                map.put("houseLayout", house.getHouseLayout());
                map.put("buildingArea", house.getBuildingArea());
                map.put("relationship", ho.getRelationship());
                return map;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("data", houseList);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listAllHouses(Integer page, Integer size, String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<House> queryWrapper = new QueryWrapper<>();
            
            // 如果有搜索关键词，按房间号或房屋编码搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(q -> q
                    .like("room_no", keyword)
                    .or()
                    .like("house_code", keyword)
                );
            }
            
            queryWrapper.orderByDesc("created_at");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<House> houses = houseMapper.selectList(queryWrapper);
            long total = houseMapper.selectCount(new QueryWrapper<House>()
                    .and(keyword != null && !keyword.trim().isEmpty(), 
                        q -> q.like("room_no", keyword).or().like("house_code", keyword)));

            List<Map<String, Object>> houseList = houses.stream().map(house -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", house.getId());
                map.put("roomNo", house.getRoomNo());
                map.put("fullRoomNo", house.getFullRoomNo() != null ? house.getFullRoomNo() : "");
                map.put("houseCode", house.getHouseCode() != null ? house.getHouseCode() : "");
                map.put("houseType", house.getHouseType());
                map.put("houseLayout", house.getHouseLayout() != null ? house.getHouseLayout() : "");
                map.put("buildingArea", house.getBuildingArea());
                map.put("usableArea", house.getUsableArea() != null ? house.getUsableArea() : 0);
                map.put("houseStatus", house.getHouseStatus() != null ? house.getHouseStatus() : "");
                map.put("createdAt", house.getCreatedAt());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", houseList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateHouseInfo(Long houseId, String houseType, String houseLayout, String houseOrientation) {
        Map<String, Object> response = new HashMap<>();
        try {
            House house = houseMapper.selectById(houseId);
            if (house == null) {
                response.put("success", false);
                response.put("message", "房屋不存在");
                return response;
            }

            if (houseType != null) {
                house.setHouseType(houseType);
            }
            if (houseLayout != null) {
                house.setHouseLayout(houseLayout);
            }
            if (houseOrientation != null) {
                house.setHouseOrientation(houseOrientation);
            }

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

    // ==================== 公告管理 ====================

    @Override
    @Transactional
    public Map<String, Object> addNotice(Long communityId, String title, String content, String noticeType) {
        Map<String, Object> response = new HashMap<>();
        try {
            CommunityNotice notice = new CommunityNotice();
            notice.setCommunityId(communityId);
            notice.setCreatedBy(1L); // 默认系统用户，实际应该从登录信息获取
            notice.setTitle(title);
            notice.setContent(content);
            notice.setNoticeType(noticeType);
            notice.setTargetAudience("全体业主");
            notice.setPublishTime(LocalDateTime.now());
            notice.setStartTime(LocalDateTime.now());
            notice.setEndTime(LocalDateTime.now().plusDays(30));
            notice.setStatus("已发布");
            notice.setApprovalStatus("已审核");

            communityNoticeMapper.insert(notice);

            response.put("success", true);
            response.put("message", "公告发布成功");
            response.put("data", notice);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发布失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateNotice(Long noticeId, String title, String content) {
        Map<String, Object> response = new HashMap<>();
        try {
            CommunityNotice notice = communityNoticeMapper.selectById(noticeId);
            if (notice == null) {
                response.put("success", false);
                response.put("message", "公告不存在");
                return response;
            }

            if (title != null) {
                notice.setTitle(title);
            }
            if (content != null) {
                notice.setContent(content);
            }

            communityNoticeMapper.updateById(notice);

            response.put("success", true);
            response.put("message", "公告更新成功");
            response.put("data", notice);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> deleteNotice(Long noticeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            CommunityNotice notice = communityNoticeMapper.selectById(noticeId);
            if (notice == null) {
                response.put("success", false);
                response.put("message", "公告不存在");
                return response;
            }

            notice.setStatus("已撤回");
            communityNoticeMapper.updateById(notice);

            response.put("success", true);
            response.put("message", "公告删除成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listNotices(Long communityId, Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<CommunityNotice> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("community_id", communityId);
            queryWrapper.ne("status", "已撤回");
            queryWrapper.orderByDesc("publish_time");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<CommunityNotice> notices = communityNoticeMapper.selectList(queryWrapper);
            long total = communityNoticeMapper.selectCount(new QueryWrapper<CommunityNotice>()
                    .eq("community_id", communityId)
                    .ne("status", "已撤回"));

            List<Map<String, Object>> noticeList = notices.stream().map(notice -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", notice.getId());
                map.put("title", notice.getTitle());
                map.put("noticeType", notice.getNoticeType());
                map.put("publishTime", notice.getPublishTime());
                map.put("status", notice.getStatus());
                map.put("readCount", notice.getReadCount());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", noticeList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> getNoticeDetail(Long noticeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            CommunityNotice notice = communityNoticeMapper.selectById(noticeId);
            if (notice == null) {
                response.put("success", false);
                response.put("message", "公告不存在");
                return response;
            }

            Map<String, Object> noticeInfo = new LinkedHashMap<>();
            noticeInfo.put("id", notice.getId());
            noticeInfo.put("title", notice.getTitle());
            noticeInfo.put("content", notice.getContent());
            noticeInfo.put("noticeType", notice.getNoticeType());
            noticeInfo.put("publishTime", notice.getPublishTime());
            noticeInfo.put("status", notice.getStatus());
            noticeInfo.put("targetAudience", notice.getTargetAudience());

            response.put("success", true);
            response.put("data", noticeInfo);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> listCommunities(Integer page, Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;

            QueryWrapper<CommunityInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "启用");
            queryWrapper.orderByDesc("created_at");

            int offset = (page - 1) * size;
            queryWrapper.last("LIMIT " + size + " OFFSET " + offset);

            List<CommunityInfo> communities = communityInfoMapper.selectList(queryWrapper);
            long total = communityInfoMapper.selectCount(new QueryWrapper<CommunityInfo>().eq("status", "启用"));

            List<Map<String, Object>> communityList = communities.stream().map(community -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", community.getId());
                map.put("communityName", community.getCommunityName());
                map.put("communityCode", community.getCommunityCode());
                map.put("detailAddress", community.getDetailAddress());
                map.put("managerName", community.getManagerName());
                map.put("managerPhone", community.getManagerPhone());
                map.put("contactPhone", community.getContactPhone());
                map.put("status", community.getStatus());
                map.put("createdAt", community.getCreatedAt());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> pageData = new LinkedHashMap<>();
            pageData.put("page", page);
            pageData.put("size", size);
            pageData.put("total", total);
            pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
            pageData.put("items", communityList);

            response.put("success", true);
            response.put("data", pageData);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
}
