package com.community.property.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * 物业员工个人基本信息修改请求
 * 只包含员工可以自己修改的字段
 * 支持图片上传（multipart/form-data）
 */
@Data
@Schema(description = "物业员工个人基本信息修改请求")
public class StaffProfileUpdateRequest {
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "性别")
    private String gender;
    
    @Schema(description = "出生日期")
    private Date birthDate;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "微信账号")
    private String wechat;
    
    @Schema(description = "电话区号")
    private String telephoneAreaCode;
    
    @Schema(description = "电话号码")
    private String telephoneNumber;
    
    @Schema(description = "分机号码")
    private String telephoneExtension;
    
    @Schema(description = "紧急联系人")
    private String emergencyContact;
    
    @Schema(description = "紧急联系电话")
    private String emergencyPhone;
    
    @Schema(description = "毕业院校")
    private String graduateSchool;
    
    @Schema(description = "毕业时间")
    private Date graduationDate;
    
    @Schema(description = "学历")
    private String educationLevel;
    
    @Schema(description = "所学专业")
    private String major;
    
    @Schema(description = "籍贯")
    private String nativePlace;
    
    @Schema(description = "身份证照片（JSON数组，0-2张）- 已有照片的OSS地址")
    private String idCardPhotos;
    
    @Schema(description = "证件照（JSON数组，0-多张）- 已有照片的OSS地址")
    private String certificatePhotos;
    
    // ==================== 图片上传字段（multipart/form-data） ====================
    
    @Schema(description = "新上传的身份证照片文件（最多2张，将与现有照片合并）")
    private List<MultipartFile> idCardPhotoFiles;
    
    @Schema(description = "新上传的证件照文件（无数量限制，将与现有照片合并）")
    private List<MultipartFile> certificatePhotoFiles;
    
    @Schema(description = "需要删除的身份证照片OSS地址（JSON数组）")
    private String idCardPhotosToDelete;
    
    @Schema(description = "需要删除的证件照OSS地址（JSON数组）")
    private String certificatePhotosToDelete;
}

