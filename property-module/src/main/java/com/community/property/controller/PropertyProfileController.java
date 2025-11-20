package com.community.property.controller;

import com.community.property.service.*;
import com.community.property.utils.JwtUtil;
import com.community.property.domain.entity.Staff;
import com.community.property.domain.entity.Department;
import com.community.property.domain.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 物业个人信息控制器
 */
@RestController
@RequestMapping("/api/property/profile")
@Tag(name = "物业个人信息管理", description = "物业员工查看和修改个人信息相关接口")
public class PropertyProfileController {
    
    @Autowired
    private StaffService staffService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ImageService imageService;

    @Autowired
    private RedisMessageService redisMessageService;
    
    /**
     * 获取个人完整信息
     */
    @GetMapping("/my-info")
    @Operation(summary = "获取个人完整信息", description = "获取当前登录物业员工的完整个人信息，包括基本信息、部门信息、角色信息等")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> getMyInfo(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 构建返回信息
            Map<String, Object> staffInfo = new HashMap<>();
            
            // 基本信息
            staffInfo.put("id", staff.getId());
            staffInfo.put("name", staff.getName());
            staffInfo.put("username", staff.getUsername());
            staffInfo.put("phone", staff.getPhone());
            staffInfo.put("email", staff.getEmail());
            staffInfo.put("idCard", staff.getIdCard());
            staffInfo.put("workNo", staff.getWorkNo());
            staffInfo.put("gender", staff.getGender());
            staffInfo.put("birthDate", staff.getBirthDate());
            staffInfo.put("avatar", staff.getAvatar());
            
            // 联系信息
            staffInfo.put("wechat", staff.getWechat());
            staffInfo.put("telephoneAreaCode", staff.getTelephoneAreaCode());
            staffInfo.put("telephoneNumber", staff.getTelephoneNumber());
            staffInfo.put("telephoneExtension", staff.getTelephoneExtension());
            staffInfo.put("emergencyContact", staff.getEmergencyContact());
            staffInfo.put("emergencyPhone", staff.getEmergencyPhone());
            
            // 教育信息
            staffInfo.put("graduateSchool", staff.getGraduateSchool());
            staffInfo.put("graduationDate", staff.getGraduationDate());
            staffInfo.put("educationLevel", staff.getEducationLevel());
            staffInfo.put("major", staff.getMajor());
            
            // 工作信息
            staffInfo.put("position", staff.getPosition());
            staffInfo.put("jobTitle", staff.getJobTitle());
            staffInfo.put("hireDate", staff.getHireDate());
            staffInfo.put("workStatus", staff.getWorkStatus());
            staffInfo.put("isManager", staff.getIsManager());
            staffInfo.put("nativePlace", staff.getNativePlace());
            
            // 状态信息
            staffInfo.put("accountStatus", staff.getAccountStatus());
            staffInfo.put("onlineStatus", staff.getOnlineStatus());
            staffInfo.put("lastLoginTime", staff.getLastLoginTime());
            staffInfo.put("loginCount", staff.getLoginCount());
            
            // 备注
            staffInfo.put("remark", staff.getRemark());
            
            // 图片信息 - 转换为完整URL
            if (staff.getIdCardPhotos() != null && !staff.getIdCardPhotos().isEmpty()) {
                staffInfo.put("idCardPhotos", convertImagesToUrls(staff.getIdCardPhotos()));
            }
            if (staff.getCertificatePhotos() != null && !staff.getCertificatePhotos().isEmpty()) {
                staffInfo.put("certificatePhotos", convertImagesToUrls(staff.getCertificatePhotos()));
            }
            
            // 获取部门信息
            if (staff.getDepartmentId() != null) {
                Department department = departmentService.findById(staff.getDepartmentId());
                if (department != null) {
                    Map<String, Object> deptInfo = new HashMap<>();
                    deptInfo.put("id", department.getId());
                    deptInfo.put("departmentName", department.getDepartmentName());
                    deptInfo.put("departmentCode", department.getDepartmentCode());
                    deptInfo.put("description", department.getDescription());
                    staffInfo.put("department", deptInfo);
                }
            }
            
            // 获取角色信息
            if (staff.getRoleId() != null) {
                Role role = roleService.findById(staff.getRoleId());
                if (role != null) {
                    Map<String, Object> roleInfo = new HashMap<>();
                    roleInfo.put("id", role.getId());
                    roleInfo.put("roleName", role.getRoleName());
                    roleInfo.put("roleCode", role.getRoleCode());
                    roleInfo.put("roleType", role.getRoleType());
                    roleInfo.put("description", role.getDescription());
                    staffInfo.put("role", roleInfo);
                }
            }
            
            response.put("success", true);
            response.put("data", staffInfo);
            response.put("message", "查询成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 修改个人基本信息（支持multipart/form-data和图片上传）
     * 用于修改个人信息的同时上传或删除图片
     */
    @PostMapping("/update-basic-with-images")
    @Operation(summary = "修改个人基本信息（包含图片上传）", description = "修改个人基本信息并同时上传或删除图片，支持multipart/form-data格式")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "修改成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Map<String, Object> updateBasicInfoWithImages(
            @Parameter(description = "Authorization Token", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "个人信息修改请求（支持multipart字段）")
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) String wechat,
            @RequestParam(required = false) String telephoneAreaCode,
            @RequestParam(required = false) String telephoneNumber,
            @RequestParam(required = false) String telephoneExtension,
            @RequestParam(required = false) String emergencyContact,
            @RequestParam(required = false) String emergencyPhone,
            @RequestParam(required = false) String graduateSchool,
            @RequestParam(required = false) String graduationDate,
            @RequestParam(required = false) String educationLevel,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String nativePlace,
            @RequestParam(required = false) String idCardPhotosToDelete,
            @RequestParam(required = false) String certificatePhotosToDelete,
            @RequestParam(value = "idCardPhotoFiles", required = false) MultipartFile[] idCardPhotoFiles,
            @RequestParam(value = "certificatePhotoFiles", required = false) MultipartFile[] certificatePhotoFiles) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析用户信息
            String realToken = token.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(realToken);
            Staff staff = staffService.findByUsername(username);
            
            if (staff == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            // 调用Service处理图片上传和删除，以及信息更新
            boolean success = staffService.updateBasicInfoWithImages(staff.getId(), 
                    phone, email, gender, birthDate, avatar, wechat, 
                    telephoneAreaCode, telephoneNumber, telephoneExtension,
                    emergencyContact, emergencyPhone, graduateSchool, graduationDate,
                    educationLevel, major, nativePlace,
                    idCardPhotosToDelete, certificatePhotosToDelete,
                    idCardPhotoFiles, certificatePhotoFiles);
            
            if (success) {
                response.put("success", true);
                response.put("message", "个人信息更新成功");
            } else {
                response.put("success", false);
                response.put("message", "个人信息更新失败");
            }
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 将图片相对路径转换为完整的URL字符串
     * @param imagePath 图片相对路径（如 staff/certificate/uuid_filename.jpg）
     * @return 完整的HTTPS URL
     */
    private String convertImagesToUrls(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        // 直接转换为完整URL
        return imageService.getImageUrl(imagePath);
    }
}

