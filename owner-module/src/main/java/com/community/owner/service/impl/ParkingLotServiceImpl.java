package com.community.owner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.mapper.ParkingLotMapper;
import com.community.owner.entity.ParkingLot;
import com.community.owner.service.ParkingLotService;
import org.springframework.stereotype.Service;

@Service
public class ParkingLotServiceImpl extends ServiceImpl<ParkingLotMapper, ParkingLot> implements ParkingLotService {
}


