package com.community.property.service.impl;

import com.community.property.dto.StaffProfileUpdateRequest;
import com.community.property.dto.StaffInfoUpdateApplyRequest;
import com.community.property.mapper.StaffMapper;
import com.community.property.entity.Staff;
import com.community.property.service.StaffService;
import com.community.property.service.ImageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 员工服务实现类
 */
@Service
public class StaffServiceImpl implements StaffService {
    
    @Autowired
    private StaffMapper staffMapper;
    
    @Autowired
    private ImageService imageService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Staff findByUsername(String username) {
        return staffMapper.findByUsername(username);
    }
    
    @Override
    public Staff findById(Long id) {
        return staffMapper.findById(id);
    }
    
    @Override
    @Transactional
    public boolean updateLoginInfo(Long staffId, String loginIp) {
        return staffMapper.updateLoginInfo(staffId, loginIp) > 0;
    }
    
    @Override
    @Transactional
    public boolean updatePassword(Long staffId, String encodedPassword) {
        return staffMapper.updatePassword(staffId, encodedPassword) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateBasicInfo(Long staffId, StaffProfileUpdateRequest request) {
        Staff staff = new Staff();
        staff.setId(staffId);
        BeanUtils.copyProperties(request, staff);
        return staffMapper.updateBasicInfo(staff) > 0;
    }
    
    @Override
    @Transactional
    public boolean createUpdateApply(Long staffId, StaffInfoUpdateApplyRequest request) {
        // TODO: 实现信息修改申请功能
        // 这里需要创建一个staff_update_apply表来存储申请记录
        // 暂时返回true，待后续实现
        return true;
    }
    
    @Override
    public Map<String, Object> listUpdateApplies(Long staffId, Integer page, Integer size) {
        // TODO: 实现申请列表查询
        // 暂时返回空列表
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", 0);
        return result;
    }
    
    @Override
    public int countByDepartmentId(Long departmentId) {
        return staffMapper.countByDepartmentId(departmentId);
    }
    
    @Override
    public List<Map<String, Object>> listDepartmentMembers(Long departmentId) {
        List<Staff> staffList = staffMapper.findByDepartmentId(departmentId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Staff staff : staffList) {
            Map<String, Object> member = new HashMap<>();
            member.put("id", staff.getId());
            member.put("name", staff.getName());
            member.put("workNo", staff.getWorkNo());
            member.put("position", staff.getPosition());
            member.put("jobTitle", staff.getJobTitle());
            member.put("phone", staff.getPhone());
            member.put("email", staff.getEmail());
            result.add(member);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean updateBasicInfoWithImages(Long staffId, String phone, String email, String gender, 
            String birthDate, String avatar, String wechat, String telephoneAreaCode, 
            String telephoneNumber, String telephoneExtension, String emergencyContact, 
            String emergencyPhone, String graduateSchool, String graduationDate, 
            String educationLevel, String major, String nativePlace,
            String idCardPhotosToDelete, String certificatePhotosToDelete,
            MultipartFile[] idCardPhotoFiles, MultipartFile[] certificatePhotoFiles) throws Exception {
        
        System.out.println("🎯 ===== updateBasicInfoWithImages 被调用 =====");
        System.out.println("📸 idCardPhotosToDelete: " + idCardPhotosToDelete);
        System.out.println("📸 certificatePhotosToDelete: " + certificatePhotosToDelete);
        System.out.println("📸 idCardPhotoFiles: " + (idCardPhotoFiles != null ? idCardPhotoFiles.length : 0) + " 个");
        System.out.println("📸 certificatePhotoFiles: " + (certificatePhotoFiles != null ? certificatePhotoFiles.length : 0) + " 个");
        
        // 获取当前员工信息
        Staff staff = staffMapper.findById(staffId);
        if (staff == null) {
            return false;
        }
        
        // 解析需要删除的图片列表
        List<String> idCardPhotosDeleteList = new ArrayList<>();
        if (idCardPhotosToDelete != null && !idCardPhotosToDelete.isEmpty()) {
            try {
                idCardPhotosDeleteList = objectMapper.readValue(idCardPhotosToDelete, new TypeReference<List<String>>() {});
                System.out.println("🗑️  解析的idCardPhotosDeleteList: " + idCardPhotosDeleteList);
            } catch (Exception e) {
                System.err.println("❌ 解析idCardPhotosToDelete失败: " + e.getMessage());
                // 忽略解析错误
            }
        }
        
        List<String> certificatePhotosDeleteList = new ArrayList<>();
        if (certificatePhotosToDelete != null && !certificatePhotosToDelete.isEmpty()) {
            try {
                certificatePhotosDeleteList = objectMapper.readValue(certificatePhotosToDelete, new TypeReference<List<String>>() {});
                System.out.println("🗑️  解析的certificatePhotosDeleteList: " + certificatePhotosDeleteList);
            } catch (Exception e) {
                System.err.println("❌ 解析certificatePhotosToDelete失败: " + e.getMessage());
                // 忽略解析错误
            }
        }
        
        // 使用ImageService提供的updateSingleImage方法处理身份证照片（只存储一张）
        String updatedIdCardPhotos = null;
        System.out.println("🔍 身份证照片处理逻辑:");
        System.out.println("  - deleteList.isEmpty(): " + idCardPhotosDeleteList.isEmpty());
        System.out.println("  - idCardPhotoFiles != null: " + (idCardPhotoFiles != null));
        System.out.println("  - idCardPhotoFiles.length > 0: " + (idCardPhotoFiles != null && idCardPhotoFiles.length > 0));
        
        if (idCardPhotosDeleteList.isEmpty() && idCardPhotoFiles != null && idCardPhotoFiles.length > 0) {
            // 新上传图片，替换旧图片
            System.out.println("✅ 处理新上传的身份证照片...");
            updatedIdCardPhotos = imageService.updateSingleImage(
                    staff.getIdCardPhotos(),
                    idCardPhotoFiles[0],  // 只取第一张
                    "staff/id_card",
                    staffId
            );
            System.out.println("✅ 身份证照片已更新: " + updatedIdCardPhotos);
        } else if (!idCardPhotosDeleteList.isEmpty()) {
            // 删除图片
            System.out.println("🗑️  删除身份证照片");
            updatedIdCardPhotos = null;
        } else if (staff.getIdCardPhotos() != null) {
            // 保持原有图片
            System.out.println("📌 保持原有身份证照片");
            updatedIdCardPhotos = staff.getIdCardPhotos();
        }
        
        // 使用ImageService提供的updateSingleImage方法处理证件照（只存储一张）
        String updatedCertificatePhotos = null;
        System.out.println("🔍 证件照处理逻辑:");
        System.out.println("  - deleteList.isEmpty(): " + certificatePhotosDeleteList.isEmpty());
        System.out.println("  - certificatePhotoFiles != null: " + (certificatePhotoFiles != null));
        System.out.println("  - certificatePhotoFiles.length > 0: " + (certificatePhotoFiles != null && certificatePhotoFiles.length > 0));
        
        if (certificatePhotosDeleteList.isEmpty() && certificatePhotoFiles != null && certificatePhotoFiles.length > 0) {
            // 新上传图片，替换旧图片
            System.out.println("✅ 处理新上传的证件照...");
            updatedCertificatePhotos = imageService.updateSingleImage(
                    staff.getCertificatePhotos(),
                    certificatePhotoFiles[0],  // 只取第一张
                    "staff/certificate",
                    staffId
            );
            System.out.println("✅ 证件照已更新: " + updatedCertificatePhotos);
        } else if (!certificatePhotosDeleteList.isEmpty()) {
            // 删除图片
            System.out.println("🗑️  删除证件照");
            updatedCertificatePhotos = null;
        } else if (staff.getCertificatePhotos() != null) {
            // 保持原有图片
            System.out.println("📌 保持原有证件照");
            updatedCertificatePhotos = staff.getCertificatePhotos();
        }
        
        // 更新基本信息
        if (phone != null && !phone.isEmpty()) staff.setPhone(phone);
        if (email != null && !email.isEmpty()) staff.setEmail(email);
        if (gender != null && !gender.isEmpty()) staff.setGender(gender);
        if (avatar != null && !avatar.isEmpty()) staff.setAvatar(avatar);
        if (wechat != null && !wechat.isEmpty()) staff.setWechat(wechat);
        if (telephoneAreaCode != null && !telephoneAreaCode.isEmpty()) staff.setTelephoneAreaCode(telephoneAreaCode);
        if (telephoneNumber != null && !telephoneNumber.isEmpty()) staff.setTelephoneNumber(telephoneNumber);
        if (telephoneExtension != null && !telephoneExtension.isEmpty()) staff.setTelephoneExtension(telephoneExtension);
        if (emergencyContact != null && !emergencyContact.isEmpty()) staff.setEmergencyContact(emergencyContact);
        if (emergencyPhone != null && !emergencyPhone.isEmpty()) staff.setEmergencyPhone(emergencyPhone);
        if (graduateSchool != null && !graduateSchool.isEmpty()) staff.setGraduateSchool(graduateSchool);
        if (educationLevel != null && !educationLevel.isEmpty()) staff.setEducationLevel(educationLevel);
        if (major != null && !major.isEmpty()) staff.setMajor(major);
        if (nativePlace != null && !nativePlace.isEmpty()) staff.setNativePlace(nativePlace);
        
        // 处理日期字段
        if (birthDate != null && !birthDate.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                staff.setBirthDate(LocalDate.parse(birthDate, formatter));
            } catch (Exception e) {
                // 忽略日期解析错误
            }
        }
        
        if (graduationDate != null && !graduationDate.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                staff.setGraduationDate(LocalDate.parse(graduationDate, formatter));
            } catch (Exception e) {
                // 忽略日期解析错误
            }
        }
        
        // 更新照片字段
        staff.setIdCardPhotos(updatedIdCardPhotos);
        staff.setCertificatePhotos(updatedCertificatePhotos);
        
        System.out.println("📝 最终更新的员工数据:");
        System.out.println("  - idCardPhotos: " + updatedIdCardPhotos);
        System.out.println("  - certificatePhotos: " + updatedCertificatePhotos);
        
        // 保存到数据库
        boolean result = staffMapper.updateBasicInfo(staff) > 0;
        System.out.println("💾 数据库更新结果: " + result);
        System.out.println("🎯 ===== updateBasicInfoWithImages 完成 =====");
        return result;
    }
}

