package com.community.property.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface CommunityService {
    /**
     * 更新社区信息（包含图片上传）
     */
    Map<String, Object> updateCommunityWithImages(Long communityId, String communityName,
        String detailAddress, MultipartFile[] communityImageFiles, String communityImagesToDelete) throws Exception;
}
