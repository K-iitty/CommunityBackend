package com.community.owner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.mapper.ParkingSpaceMapper;
import com.community.owner.entity.ParkingSpace;
import com.community.owner.service.ParkingSpaceService;
import org.springframework.stereotype.Service;

@Service
public class ParkingSpaceServiceImpl extends ServiceImpl<ParkingSpaceMapper, ParkingSpace> implements ParkingSpaceService {
}


