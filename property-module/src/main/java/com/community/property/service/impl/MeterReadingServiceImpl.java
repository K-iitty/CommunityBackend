package com.community.property.service.impl;

import com.community.property.entity.MeterReading;
import com.community.property.mapper.MeterReadingMapper;
import com.community.property.service.MeterReadingService;
import com.community.property.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 抄表记录服务实现类
 */
@Service
public class MeterReadingServiceImpl implements MeterReadingService {

    @Autowired
    private MeterReadingMapper meterReadingMapper;

    @Autowired
    private ImageService imageService;

    @Override
    @Transactional
    public Map<String, Object> addMeterReadingWithImage(Long meterId, Double currentReading, Double usageAmount,
            Long readerId, String readerName, String categoryName, MultipartFile readingImageFile) throws Exception {

        Map<String, Object> response = new HashMap<>();

        // 处理抄表图片（单一图片，VARCHAR字段）
        String readingImagePath = null;
        if (readingImageFile != null && !readingImageFile.isEmpty()) {
            readingImagePath = imageService.uploadImage(readingImageFile, "meter/reading", meterId);
        }

        // 创建抄表记录
        MeterReading meterReading = new MeterReading();
        meterReading.setMeterId(meterId);
        meterReading.setCurrentReading(currentReading != null ? new BigDecimal(currentReading) : null);
        meterReading.setUsageAmount(usageAmount != null ? new BigDecimal(usageAmount) : null);
        meterReading.setReaderId(readerId);
        meterReading.setReaderName(readerName);
        meterReading.setCategoryName(categoryName);
        meterReading.setReadingImage(readingImagePath);
        meterReading.setReadingDate(LocalDate.now());
        meterReading.setReadingTime(LocalDateTime.now());
        meterReading.setReadingType("手动");
        meterReading.setReadingStatus("正常");

        // 保存到数据库
        int result = meterReadingMapper.insert(meterReading);

        if (result > 0) {
            response.put("success", true);
            response.put("message", "抄表记录添加成功");
            response.put("data", meterReading);
        } else {
            response.put("success", false);
            response.put("message", "抄表记录添加失败");
        }
        return response;
    }
}
