package com.community.property.mapper;

import com.community.property.domain.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

/**
 * 部门Dao接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM department WHERE id = #{id}")
    Department findById(Long id);
}

