package com.community.property.controller;

import com.community.property.mapper.OwnerMapper;
import com.community.property.domain.entity.Owner;
import com.community.property.service.RedisMessageService;
import com.community.property.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 物业业主管理控制器
 * 负责物业端对业主信息的查询操作
 */
@RestController
@RequestMapping("/api/property/owners")
@Tag(name = "物业业主管理", description = "物业端的业主信息查询接口")
public class PropertyOwnerController {

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisMessageService redisMessageService;

    /**
     * 获取所有业主列表（分页）
     */
    @GetMapping("")
    @Operation(summary = "获取所有业主列表", description = "分页获取所有业主信息")
    public Map<String, Object> listOwners(
            @Parameter(description = "页码", required = false)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "1000") Integer size,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 创建分页对象
            Page<Owner> pageObj = new Page<>(page, size);
            
            // 查询所有业主（只查询已激活的业主）
            QueryWrapper<Owner> wrapper = new QueryWrapper<>();
            wrapper.eq("status", "已激活").or().isNull("status");
            
            Page<Owner> result = ownerMapper.selectPage(pageObj, wrapper);
            
            // 构建返回数据
            List<Map<String, Object>> items = new ArrayList<>();
            for (Owner owner : result.getRecords()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", owner.getId());
                item.put("realName", owner.getName());
                item.put("phoneNumber", owner.getPhone());
                item.put("username", owner.getUsername());
                item.put("status", owner.getStatus());
                items.add(item);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("items", items);
            data.put("total", result.getTotal());
            data.put("page", page);
            data.put("size", size);
            data.put("totalPages", (result.getTotal() + size - 1) / size);
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取业主详情
     */
    @GetMapping("/{ownerId}")
    @Operation(summary = "获取业主详情", description = "获取指定业主的详细信息")
    public Map<String, Object> getOwnerDetail(
            @Parameter(description = "业主ID", required = true)
            @PathVariable Long ownerId,
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        try {
            Owner owner = ownerMapper.selectById(ownerId);
            if (owner == null) {
                response.put("success", false);
                response.put("message", "业主不存在");
                return response;
            }
            
            response.put("success", true);
            response.put("data", owner);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
}
