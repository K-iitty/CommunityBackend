package com.community.owner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.mapper.MeterReadingMapper;
import com.community.owner.domain.entity.MeterReading;
import com.community.owner.service.MeterReadingService;
import org.springframework.stereotype.Service;

@Service
public class MeterReadingServiceImpl extends ServiceImpl<MeterReadingMapper, MeterReading> implements MeterReadingService {
}


