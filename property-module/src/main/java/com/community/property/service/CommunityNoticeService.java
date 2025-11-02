package com.community.property.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 社区公告服务接口
 */
public interface CommunityNoticeService {
    
    /**
     * 获取公告列表
     * @param communityId 社区ID（可为null，查询所有）
     * @param noticeType 公告类型（可为null，查询所有类型）
     * @param page 页码
     * @param size 每页数量
     * @return 分页数据
     */
    Map<String, Object> listNotices(Long communityId, String noticeType, Integer page, Integer size);
    
    /**
     * 获取公告详情
     * @param noticeId 公告ID
     * @return 公告详情
     */
    Map<String, Object> getNoticeDetail(Long noticeId);

    /**
     * 新增公告（支持完整字段和图片上传）
     */
    Map<String, Object> addNoticeWithImages(Long communityId, String title, String content, String noticeType,
            String activityDate, String activityTime, String activityLocation, String activityOrganizer,
            String activityContact, String activityContactPhone, String targetAudience, Integer isUrgent,
            Integer isTop, String startTime, String endTime, String remark,
            MultipartFile[] noticeImageFiles);

    /**
     * 修改公告（支持完整字段和图片上传）
     */
    Map<String, Object> updateNoticeWithImages(Long noticeId, String title, String content, String noticeType,
            String activityDate, String activityTime, String activityLocation, Integer isUrgent,
            Integer isTop, String endTime, String remark,
            MultipartFile[] noticeImageFiles, String noticeImagesToDelete);

    /**
     * 增加公告阅读计数
     * @param noticeId 公告ID
     */
    void incrementReadCount(Long noticeId);

    /**
     * 删除公告
     * @param noticeId 公告ID
     */
    void deleteNotice(Long noticeId);
    
    /**
     * 更新公告图片
     * @param noticeId 公告ID
     * @param imageUrl 图片URL
     */
    void updateNoticeImages(Long noticeId, String imageUrl);
}

