package com.community.owner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.owner.domain.entity.HouseOwner;

public interface HouseOwnerService extends IService<HouseOwner> {
    /**
     * 当前业主是否已与该房屋以“业主”关系绑定（已验证）
     */
    boolean isHouseOwnedByVerifiedOwner(Long houseId, Long ownerId);

    /**
     * 是否存在任何已验证的同类关系（用于禁止重复绑定为“业主”）
     */
    boolean existsVerifiedRelationship(Long houseId, Long relationshipOwnerId, String relationship);
}


