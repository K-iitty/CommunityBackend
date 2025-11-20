package com.community.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.mapper.HouseOwnerMapper;
import com.community.owner.domain.entity.HouseOwner;
import com.community.owner.service.HouseOwnerService;
import org.springframework.stereotype.Service;

@Service
public class HouseOwnerServiceImpl extends ServiceImpl<HouseOwnerMapper, HouseOwner> implements HouseOwnerService {

    @Override
    public boolean isHouseOwnedByVerifiedOwner(Long houseId, Long ownerId) {
        QueryWrapper<HouseOwner> qw = new QueryWrapper<>();
        qw.eq("house_id", houseId)
          .eq("owner_id", ownerId)
          .eq("relationship", "业主")
          .eq("is_verified", 1);
        return count(qw) > 0;
    }

    @Override
    public boolean existsVerifiedRelationship(Long houseId, Long relationshipOwnerId, String relationship) {
        QueryWrapper<HouseOwner> qw = new QueryWrapper<>();
        qw.eq("house_id", houseId)
          .eq("owner_id", relationshipOwnerId)
          .eq("relationship", relationship)
          .eq("is_verified", 1);
        return count(qw) > 0;
    }
}


