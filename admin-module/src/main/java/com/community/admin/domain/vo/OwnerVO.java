package com.community.admin.domain.vo;

import com.community.admin.domain.entity.Owner;
import lombok.Data;

import java.io.Serializable;

/**
 * 业主信息VO类
 */
@Data
public class OwnerVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 业主信息
     */
    private Owner owner;
}