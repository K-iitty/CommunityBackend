package com.community.owner.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工/物业实体�? */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("staff")
public class Staff extends BaseEntity {
    
    /**
     * 登录账号
     */
    @TableField("username")
    private String username;
    
    /**
     * 登录密码
     */
    @TableField("password")
    private String password;
    
    /**
     * 员工姓名
     */
    @TableField("name")
    private String name;
    
    /**
     * 手机�?     */
    @TableField("phone")
    private String phone;
    
    /**
     * 身份证号
     */
    @TableField("id_card")
    private String idCard;
    
    /**
     * 工号
     */
    @TableField("work_no")
    private String workNo;
    
    /**
     * 性别:�?�?保密
     */
    @TableField("gender")
    private String gender;
    
    /**
     * 员工生日
     */
    @TableField("birth_date")
    private LocalDate birthDate;
    
    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;
    
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 微信账号
     */
    @TableField("wechat")
    private String wechat;
    
    /**
     * 电话区号
     */
    @TableField("telephone_area_code")
    private String telephoneAreaCode;
    
    /**
     * 电话号码
     */
    @TableField("telephone_number")
    private String telephoneNumber;
    
    /**
     * 分机号码
     */
    @TableField("telephone_extension")
    private String telephoneExtension;
    
    /**
     * 紧急联系人
     */
    @TableField("emergency_contact")
    private String emergencyContact;
    
    /**
     * 紧急联系电�?     */
    @TableField("emergency_phone")
    private String emergencyPhone;
    
    /**
     * 毕业院校
     */
    @TableField("graduate_school")
    private String graduateSchool;
    
    /**
     * 毕业时间
     */
    @TableField("graduation_date")
    private LocalDate graduationDate;
    
    /**
     * 学历
     */
    @TableField("education_level")
    private String educationLevel;
    
    /**
     * 所学专�?     */
    @TableField("major")
    private String major;
    
    /**
     * 所属部门ID
     */
    @TableField("department_id")
    private Long departmentId;
    
    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;
    
    /**
     * 职位
     */
    @TableField("position")
    private String position;
    
    /**
     * 职称
     */
    @TableField("job_title")
    private String jobTitle;
    
    /**
     * 入职日期
     */
    @TableField("hire_date")
    private LocalDate hireDate;
    
    /**
     * 工作状�?在职/休假/离职
     */
    @TableField("work_status")
    private String workStatus;
    
    /**
     * 是否可担任负责人
     */
    @TableField("is_manager")
    private Integer isManager;
    
    /**
     * 籍贯
     */
    @TableField("native_place")
    private String nativePlace;
    
    /**
     * 账号状�?正常/禁用/锁定
     */
    @TableField("account_status")
    private String accountStatus;
    
    /**
     * 在线状�?0-离线,1-在线
     */
    @TableField("online_status")
    private Integer onlineStatus;
    
    /**
     * 最后登录时�?     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;
    
    /**
     * 登录次数
     */
    @TableField("login_count")
    private Integer loginCount;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 创建�?     */
    @TableField("created_by")
    private Long createdBy;
    
    /**
     * 身份证照片（JSON数组格式�?-2张）
     */
    @TableField("id_card_photos")
    private String idCardPhotos;
    
    /**
     * 证件照等（JSON数组格式�?-多张�?     */
    @TableField("certificate_photos")
    private String certificatePhotos;
}
