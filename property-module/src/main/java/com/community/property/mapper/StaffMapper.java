package com.community.property.mapper;

import com.community.property.domain.entity.Staff;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 员工信息Mapper
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
    
    /**
     * 根据用户名查询
     */
    @Select("SELECT * FROM staff WHERE username = #{username}")
    Staff findByUsername(String username);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM staff WHERE id = #{id}")
    Staff findById(Long id);
    
    /**
     * 更新登录信息
     */
    @Update("UPDATE staff SET last_login_time = NOW(), last_login_ip = #{loginIp}, " +
            "login_count = login_count + 1, online_status = 1 " +
            "WHERE id = #{staffId}")
    int updateLoginInfo(@Param("staffId") Long staffId, @Param("loginIp") String loginIp);
    
    /**
     * 更新密码
     */
    @Update("UPDATE staff SET password = #{encodedPassword}, updated_at = NOW() " +
            "WHERE id = #{staffId}")
    int updatePassword(@Param("staffId") Long staffId, @Param("encodedPassword") String encodedPassword);
    
    /**
     * 更新基本信息
     */
    @UpdateProvider(type = StaffDaoProvider.class, method = "updateBasicInfo")
    int updateBasicInfo(Staff staff);
    
    /**
     * 统计部门人数
     */
    @Select("SELECT COUNT(*) FROM staff WHERE department_id = #{departmentId} AND work_status = '在职'")
    int countByDepartmentId(Long departmentId);
    
    /**
     * 查询部门成员
     */
    @Select("SELECT id, name, work_no, position, job_title, phone, email FROM staff " +
            "WHERE department_id = #{departmentId} AND work_status = '在职' " +
            "ORDER BY work_no")
    List<Staff> findByDepartmentId(Long departmentId);
    
    class StaffDaoProvider {
        public String updateBasicInfo(Staff staff) {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE staff SET ");
            
            if (staff.getPhone() != null) {
                sql.append("phone = #{phone}, ");
            }
            if (staff.getEmail() != null) {
                sql.append("email = #{email}, ");
            }
            if (staff.getGender() != null) {
                sql.append("gender = #{gender}, ");
            }
            if (staff.getBirthDate() != null) {
                sql.append("birth_date = #{birthDate}, ");
            }
            if (staff.getAvatar() != null) {
                sql.append("avatar = #{avatar}, ");
            }
            if (staff.getWechat() != null) {
                sql.append("wechat = #{wechat}, ");
            }
            if (staff.getTelephoneAreaCode() != null) {
                sql.append("telephone_area_code = #{telephoneAreaCode}, ");
            }
            if (staff.getTelephoneNumber() != null) {
                sql.append("telephone_number = #{telephoneNumber}, ");
            }
            if (staff.getTelephoneExtension() != null) {
                sql.append("telephone_extension = #{telephoneExtension}, ");
            }
            if (staff.getEmergencyContact() != null) {
                sql.append("emergency_contact = #{emergencyContact}, ");
            }
            if (staff.getEmergencyPhone() != null) {
                sql.append("emergency_phone = #{emergencyPhone}, ");
            }
            if (staff.getGraduateSchool() != null) {
                sql.append("graduate_school = #{graduateSchool}, ");
            }
            if (staff.getGraduationDate() != null) {
                sql.append("graduation_date = #{graduationDate}, ");
            }
            if (staff.getEducationLevel() != null) {
                sql.append("education_level = #{educationLevel}, ");
            }
            if (staff.getMajor() != null) {
                sql.append("major = #{major}, ");
            }
            if (staff.getNativePlace() != null) {
                sql.append("native_place = #{nativePlace}, ");
            }
            if (staff.getIdCardPhotos() != null) {
                sql.append("id_card_photos = #{idCardPhotos}, ");
            }
            if (staff.getCertificatePhotos() != null) {
                sql.append("certificate_photos = #{certificatePhotos}, ");
            }
            
            sql.append("updated_at = NOW() WHERE id = #{id}");
            return sql.toString();
        }
    }
}