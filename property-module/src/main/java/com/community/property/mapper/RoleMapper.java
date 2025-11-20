package com.community.property.mapper;

import com.community.property.domain.entity.Role;
import org.apache.ibatis.annotations.*;

/**
 * 角色Dao接口
 */
@Mapper
public interface RoleMapper {
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM role WHERE id = #{id}")
    Role findById(Long id);
}

