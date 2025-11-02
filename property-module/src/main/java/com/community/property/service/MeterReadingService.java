package com.community.property.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface MeterReadingService {
    /**
     * 添加抄表记录（包含图片上传）
     */
    Map<String, Object> addMeterReadingWithImage(Long meterId, Double currentReading, Double usageAmount,
        Long readerId, String readerName, String categoryName, MultipartFile readingImageFile) throws Exception;
}
