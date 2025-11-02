package com.community.property.service;

import com.community.property.dto.StaffProfileUpdateRequest;
import com.community.property.dto.StaffInfoUpdateApplyRequest;
import com.community.property.entity.Staff;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 员工服务接口
 */
public interface StaffService {
    
    /**
     * 根据用户名查询员工
     */
    Staff findByUsername(String username);
    
    /**
     * 根据ID查询员工
     */
    Staff findById(Long id);
    
    /**
     * 更新登录信息
     */
    boolean updateLoginInfo(Long staffId, String loginIp);
    
    /**
     * 更新密码
     */
    boolean updatePassword(Long staffId, String encodedPassword);
    
    /**
     * 更新个人基本信息
     */
    boolean updateBasicInfo(Long staffId, StaffProfileUpdateRequest request);
    
    /**
     * 更新个人基本信息（包含图片上传和删除）
     * @param staffId 员工ID
     * @param idCardPhotosToDelete 需要删除的身份证照片（JSON数组字符串）
     * @param certificatePhotosToDelete 需要删除的证件照（JSON数组字符串）
     * @param idCardPhotoFiles 新上传的身份证照片（0-2张）
     * @param certificatePhotoFiles 新上传的证件照（0-多张）
     */
    boolean updateBasicInfoWithImages(Long staffId, String phone, String email, String gender, 
            String birthDate, String avatar, String wechat, String telephoneAreaCode, 
            String telephoneNumber, String telephoneExtension, String emergencyContact, 
            String emergencyPhone, String graduateSchool, String graduationDate, 
            String educationLevel, String major, String nativePlace,
            String idCardPhotosToDelete, String certificatePhotosToDelete,
            MultipartFile[] idCardPhotoFiles, MultipartFile[] certificatePhotoFiles) throws Exception;
    
    /**
     * 创建信息修改申请
     */
    boolean createUpdateApply(Long staffId, StaffInfoUpdateApplyRequest request);
    
    /**
     * 查询修改申请列表
     */
    Map<String, Object> listUpdateApplies(Long staffId, Integer page, Integer size);
    
    /**
     * 统计部门人数
     */
    int countByDepartmentId(Long departmentId);
    
    /**
     * 获取部门成员列表
     */
    List<Map<String, Object>> listDepartmentMembers(Long departmentId);
}