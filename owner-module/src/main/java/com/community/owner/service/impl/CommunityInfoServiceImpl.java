package com.community.owner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.entity.CommunityInfo;
import com.community.owner.mapper.CommunityInfoMapper;
import com.community.owner.service.CommunityInfoService;
import org.springframework.stereotype.Service;

@Service
public class CommunityInfoServiceImpl extends ServiceImpl<CommunityInfoMapper, CommunityInfo> implements CommunityInfoService {
}
