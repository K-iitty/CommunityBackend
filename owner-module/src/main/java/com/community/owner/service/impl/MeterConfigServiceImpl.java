package com.community.owner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.mapper.MeterConfigMapper;
import com.community.owner.domain.entity.MeterConfig;
import com.community.owner.service.MeterConfigService;
import org.springframework.stereotype.Service;

@Service
public class MeterConfigServiceImpl extends ServiceImpl<MeterConfigMapper, MeterConfig> implements MeterConfigService {
}


