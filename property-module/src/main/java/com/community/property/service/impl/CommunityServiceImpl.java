package com.community.property.service.impl;

import com.community.property.entity.CommunityInfo;
import com.community.property.mapper.CommunityInfoMapper;
import com.community.property.service.CommunityService;
import com.community.property.service.ImageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社区信息服务实现类
 */
@Service
public class CommunityServiceImpl implements CommunityService {

    @Autowired
    private CommunityInfoMapper communityInfoMapper;

    @Autowired
    private ImageService imageService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Map<String, Object> updateCommunityWithImages(Long communityId, String communityName,
            String detailAddress, MultipartFile[] communityImageFiles, String communityImagesToDelete) throws Exception {

        Map<String, Object> response = new HashMap<>();

        CommunityInfo community = communityInfoMapper.selectById(communityId);
        if (community == null) {
            response.put("success", false);
            response.put("message", "社区不存在");
            return response;
        }

        // 处理社区图片（多张图片，TEXT JSON数组字段）
        String communityImagesJson = imageService.updateMultipleImages(
            community.getCommunityImages(),
            communityImagesToDelete != null ? objectMapper.readValue(communityImagesToDelete, 
                new TypeReference<List<String>>() {}) : null,
            communityImageFiles != null ? Arrays.asList(communityImageFiles) : null,
            -1,  // -1表示无限制
            "community/images",
            communityId
        );
        community.setCommunityImages(communityImagesJson);

        // 更新其他字段
        if (communityName != null && !communityName.isEmpty()) community.setCommunityName(communityName);
        if (detailAddress != null && !detailAddress.isEmpty()) community.setDetailAddress(detailAddress);

        // 保存到数据库
        communityInfoMapper.updateById(community);

        response.put("success", true);
        response.put("message", "社区信息更新成功");
        response.put("data", community);
        return response;
    }
}
