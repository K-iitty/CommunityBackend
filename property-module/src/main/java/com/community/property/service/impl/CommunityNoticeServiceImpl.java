package com.community.property.service.impl;

import com.community.property.mapper.CommunityNoticeMapper;
import com.community.property.domain.entity.CommunityNotice;
import com.community.property.service.CommunityNoticeService;
import com.community.property.service.ImageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 社区公告服务实现类
 */
@Service
public class CommunityNoticeServiceImpl implements CommunityNoticeService {
    
    @Autowired
    private CommunityNoticeMapper communityNoticeMapper;

    @Autowired
    private ImageService imageService;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Map<String, Object> listNotices(Long communityId, String noticeType, Integer page, Integer size) {
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询公告列表
        List<CommunityNotice> notices = communityNoticeMapper.findByConditions(communityId, noticeType, offset, size);
        
        // 构建返回数据
        List<Map<String, Object>> noticeList = new ArrayList<>();
        for (CommunityNotice notice : notices) {
            Map<String, Object> noticeMap = new HashMap<>();
            noticeMap.put("id", notice.getId());
            noticeMap.put("title", notice.getTitle());
            noticeMap.put("noticeType", notice.getNoticeType());
            noticeMap.put("publishTime", notice.getPublishTime());
            noticeMap.put("isTop", notice.getIsTop());
            noticeMap.put("isUrgent", notice.getIsUrgent());
            noticeMap.put("readCount", notice.getReadCount());
            noticeList.add(noticeMap);
        }
        
        // 查询总数
        int total = communityNoticeMapper.countByConditions(communityId, noticeType);
        
        // 构建分页数据
        Map<String, Object> result = new HashMap<>();
        result.put("list", noticeList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        
        return result;
    }
    
    @Override
    public Map<String, Object> getNoticeDetail(Long noticeId) {
        CommunityNotice notice = communityNoticeMapper.findById(noticeId);
        
        if (notice == null) {
            return null;
        }
        
        // 增加阅读次数
        communityNoticeMapper.incrementReadCount(noticeId);
        
        // 构建详情数据
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", notice.getId());
        detail.put("title", notice.getTitle());
        detail.put("content", notice.getContent());
        detail.put("noticeType", notice.getNoticeType());
        detail.put("publishTime", notice.getPublishTime());
        detail.put("startTime", notice.getStartTime());
        detail.put("endTime", notice.getEndTime());
        detail.put("isTop", notice.getIsTop());
        detail.put("isUrgent", notice.getIsUrgent());
        detail.put("readCount", notice.getReadCount());
        detail.put("attachments", notice.getAttachments());
        
        // 处理公告图片 - 数据库存储的是完整URL，直接返回
        if (notice.getNoticeImages() != null && !notice.getNoticeImages().isEmpty()) {
            String noticeImages = notice.getNoticeImages();
            // 直接使用完整URL
            detail.put("noticeImages", noticeImages);
        }
        
        // 如果是活动公告，添加活动信息
        if ("活动公告".equals(notice.getNoticeType())) {
            detail.put("activityDate", notice.getActivityDate());
            detail.put("activityTime", notice.getActivityTime());
            detail.put("activityLocation", notice.getActivityLocation());
            detail.put("activityOrganizer", notice.getActivityOrganizer());
            detail.put("activityContact", notice.getActivityContact());
            detail.put("activityContactPhone", notice.getActivityContactPhone());
        }
        
        return detail;
    }

    @Override
    @Transactional
    public Map<String, Object> addNoticeWithImages(Long communityId, String title, String content, String noticeType,
            String activityDate, String activityTime, String activityLocation, String activityOrganizer,
            String activityContact, String activityContactPhone, String targetAudience, Integer isUrgent,
            Integer isTop, String startTime, String endTime, String remark,
            MultipartFile[] noticeImageFiles) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (communityId == null || title == null || title.isEmpty()) {
                response.put("success", false);
                response.put("message", "社区ID和公告标题为必需");
                return response;
            }

            CommunityNotice notice = new CommunityNotice();
            notice.setCommunityId(communityId);
            // 设置创建人ID为默认值1（系统用户）
            // TODO: 后续可以从当前登录用户获取真实ID，需要先修复数据库外键约束或添加system_admin用户
            notice.setCreatedBy(1L);
            notice.setTitle(title);
            notice.setContent(content);
            notice.setNoticeType(noticeType != null ? noticeType : "通知公告");
            // TODO: 处理publishTime字段
            
            // 处理日期字段 - String转LocalDate
            if (activityDate != null && !activityDate.isEmpty()) {
                notice.setActivityDate(java.time.LocalDate.parse(activityDate));
            }
            notice.setActivityTime(activityTime);
            notice.setActivityLocation(activityLocation);
            notice.setActivityOrganizer(activityOrganizer);
            notice.setActivityContact(activityContact);
            notice.setActivityContactPhone(activityContactPhone);
            notice.setTargetAudience(targetAudience);
            notice.setIsUrgent(isUrgent != null ? isUrgent : 0);
            notice.setIsTop(isTop != null ? isTop : 0);
            
            // 处理时间字段 - String转LocalDateTime
            if (startTime != null && !startTime.isEmpty()) {
                notice.setStartTime(java.time.LocalDateTime.parse(startTime));
            }
            if (endTime != null && !endTime.isEmpty()) {
                notice.setEndTime(java.time.LocalDateTime.parse(endTime));
            }
            notice.setRemark(remark);

            // 处理公告图片（仅允许单张图片）
            if (noticeImageFiles != null && noticeImageFiles.length > 0) {
                List<String> imagePaths = imageService.uploadImages(
                    Arrays.asList(noticeImageFiles[0]), // 只上传第一张
                    "community/notice", communityId);
                if (imagePaths != null && !imagePaths.isEmpty()) {
                    // uploadImages 返回的是完整URL，直接保存
                    notice.setNoticeImages(imagePaths.get(0));
                }
            }

            communityNoticeMapper.insert(notice);

            response.put("success", true);
            response.put("message", "公告添加成功");
            response.put("data", notice);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            e.printStackTrace();
            return response;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateNoticeWithImages(Long noticeId, String title, String content, String noticeType,
            String activityDate, String activityTime, String activityLocation, Integer isUrgent,
            Integer isTop, String endTime, String remark,
            MultipartFile[] noticeImageFiles, String noticeImagesToDelete) {

        Map<String, Object> response = new HashMap<>();

        try {
            CommunityNotice notice = communityNoticeMapper.findById(noticeId);
            if (notice == null) {
                response.put("success", false);
                response.put("message", "公告不存在");
                return response;
            }

            // 处理公告图片（仅允许单张图片）
            String noticeImagesJson = notice.getNoticeImages();
            
            // 如果需要删除原有图片
            if (noticeImagesToDelete != null && !noticeImagesToDelete.isEmpty()) {
                try {
                    List<String> toDelete = objectMapper.readValue(noticeImagesToDelete, 
                        new TypeReference<List<String>>() {});
                    if (toDelete != null && !toDelete.isEmpty()) {
                        // 删除图片文件
                        for (String imagePath : toDelete) {
                            imageService.deleteImage(imagePath);
                        }
                        noticeImagesJson = null;
                    }
                } catch (Exception e) {
                    System.err.println("删除公告图片失败: " + e.getMessage());
                }
            }
            
            // 如果上传了新图片，只保留第一张（单张图片）
            if (noticeImageFiles != null && noticeImageFiles.length > 0) {
                List<String> imagePaths = imageService.uploadImages(
                    Arrays.asList(noticeImageFiles[0]), // 只上传第一张
                    "community/notice",
                    noticeId);
                if (imagePaths != null && !imagePaths.isEmpty()) {
                    // uploadImages 返回的是完整URL，直接保存
                    noticeImagesJson = imagePaths.get(0);
                }
            }
            
            notice.setNoticeImages(noticeImagesJson);

            // 更新其他字段
            if (title != null && !title.isEmpty()) notice.setTitle(title);
            if (content != null && !content.isEmpty()) notice.setContent(content);
            if (noticeType != null && !noticeType.isEmpty()) notice.setNoticeType(noticeType);
            
            if (activityDate != null && !activityDate.isEmpty()) {
                notice.setActivityDate(java.time.LocalDate.parse(activityDate));
            }
            if (activityTime != null && !activityTime.isEmpty()) notice.setActivityTime(activityTime);
            if (activityLocation != null && !activityLocation.isEmpty()) notice.setActivityLocation(activityLocation);
            if (isUrgent != null) notice.setIsUrgent(isUrgent);
            if (isTop != null) notice.setIsTop(isTop);
            
            if (endTime != null && !endTime.isEmpty()) {
                notice.setEndTime(java.time.LocalDateTime.parse(endTime));
            }
            if (remark != null && !remark.isEmpty()) notice.setRemark(remark);

            // 保存到数据库
            int result = communityNoticeMapper.updateById(notice);

            response.put("success", result > 0);
            response.put("message", result > 0 ? "公告更新成功" : "公告更新失败");
            response.put("data", notice);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            e.printStackTrace();
            return response;
        }
    }

    @Override
    public void incrementReadCount(Long noticeId) {
        communityNoticeMapper.incrementReadCount(noticeId);
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeId) {
        try {
            CommunityNotice notice = communityNoticeMapper.findById(noticeId);
            if (notice == null) {
                return;
            }

            // 删除公告关联的图片
            if (notice.getNoticeImages() != null && !notice.getNoticeImages().isEmpty()) {
                try {
                    imageService.deleteImage(notice.getNoticeImages());
                } catch (Exception e) {
                    System.err.println("删除公告图片失败: " + e.getMessage());
                }
            }

            // 删除公告记录
            communityNoticeMapper.deleteById(noticeId);
        } catch (Exception e) {
            System.err.println("删除公告失败: " + e.getMessage());
            throw new RuntimeException("删除公告失败: " + e.getMessage());
        }
    }

    @Override
    public void updateNoticeImages(Long noticeId, String imageUrl) {
        try {
            CommunityNotice notice = new CommunityNotice();
            notice.setId(noticeId);
            notice.setNoticeImages(imageUrl);
            communityNoticeMapper.updateById(notice);
            System.out.println("公告图片更新成功: " + imageUrl);
        } catch (Exception e) {
            System.err.println("更新公告图片失败: " + e.getMessage());
            throw new RuntimeException("更新公告图片失败: " + e.getMessage());
        }
    }
}

