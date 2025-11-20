package com.community.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.owner.domain.dto.vo.NoticeDetailVO;
import com.community.owner.domain.dto.request.NoticeFilterRequest;
import com.community.owner.domain.dto.vo.NoticeListVO;
import com.community.owner.domain.dto.request.NoticeSearchRequest;
import com.community.owner.domain.entity.CommunityNotice;
import com.community.owner.mapper.CommunityNoticeMapper;
import com.community.owner.service.CommunityNoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 社区公告服务实现类
 */
@Service
public class CommunityNoticeServiceImpl extends ServiceImpl<CommunityNoticeMapper, CommunityNotice> implements CommunityNoticeService {
    
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    
    /**
     * 将OSS路径转换为完整URL
     */
    private String getImageUrl(String osspath) {
        if (osspath == null || osspath.isEmpty()) {
            return null;
        }
        
        // 如果已经是完整URL，直接返回
        if (osspath.startsWith("http://") || osspath.startsWith("https://")) {
            return osspath;
        }
        
        // 构建OSS完整URL
        // 从endpoint中提取域名（如：oss-cn-beijing.aliyuncs.com）
        String domain = endpoint.replace("https://", "").replace("http://", "");
        String url = String.format("https://%s.%s/%s", bucketName, domain, osspath);
        return url;
    }
    
    @Override
    public Map<String, Object> listNotices(Long communityId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        
        // 构建查询条件
        QueryWrapper<CommunityNotice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("community_id", communityId)
                .eq("status", "已发布")
                .eq("approval_status", "已审核")
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now())
                .eq("target_audience", "全体业主")  // 仅显示全体业主的公告
                // 按照置顶和发布时间排序：置顶的在前，同级别按发布时间倒序
                .orderByDesc("is_top")
                .orderByDesc("publish_time");
        
        // 计算分页
        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);
        
        // 查询数据
        List<CommunityNotice> notices = list(queryWrapper);
        
        // 转换为VO对象
        List<NoticeListVO> noticeListVOs = notices.stream().map(notice -> {
            NoticeListVO vo = new NoticeListVO();
            vo.setId(notice.getId());
            vo.setTitle(notice.getTitle());
            vo.setContent(notice.getContent());
            vo.setNoticeImages(getImageUrl(notice.getNoticeImages()));
            vo.setNoticeType(notice.getNoticeType());
            vo.setIsUrgent(notice.getIsUrgent());
            vo.setIsTop(notice.getIsTop());
            vo.setPublishTime(notice.getPublishTime());
            vo.setReadCount(notice.getReadCount());
            return vo;
        }).collect(Collectors.toList());
        
        // 查询总数
        QueryWrapper<CommunityNotice> countWrapper = new QueryWrapper<>();
        countWrapper.eq("community_id", communityId)
                .eq("status", "已发布")
                .eq("approval_status", "已审核")
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now())
                .eq("target_audience", "全体业主");  // 仅计数全体业主的公告
        long total = count(countWrapper);
        
        // 构建分页结果
        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("page", page);
        pageData.put("size", size);
        pageData.put("total", total);
        pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
        pageData.put("items", noticeListVOs);
        
        return pageData;
    }
    
    @Override
    @Transactional
    public NoticeDetailVO getNoticeDetail(Long noticeId) {
        CommunityNotice notice = getById(noticeId);
        if (notice == null) {
            return null;
        }
        
        // 增加阅读次数
        notice.setReadCount(notice.getReadCount() == null ? 1 : notice.getReadCount() + 1);
        updateById(notice);
        
        // 转换为详情VO
        NoticeDetailVO detailVO = new NoticeDetailVO();
        BeanUtils.copyProperties(notice, detailVO);
        
        // 转换图片URL
        if (notice.getNoticeImages() != null && !notice.getNoticeImages().isEmpty()) {
            detailVO.setNoticeImages(getImageUrl(notice.getNoticeImages()));
        }
        
        return detailVO;
    }
    
    @Override
    public Map<String, Object> searchNotices(Long communityId, NoticeSearchRequest request) {
        Integer page = request.getPage();
        Integer size = request.getSize();
        String keyword = request.getKeyword();
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        
        // 构建查询条件
        QueryWrapper<CommunityNotice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("community_id", communityId)
                .eq("status", "已发布")
                .eq("approval_status", "已审核")
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now());
        
        // 添加搜索条件（搜索标题、内容、公告类型）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("content", keyword)
                    .or()
                    .like("notice_type", keyword));
        }
        
        // 排序
        queryWrapper.orderByDesc("is_top")
                .orderByDesc("publish_time");
        
        // 计算分页
        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);
        
        // 查询数据
        List<CommunityNotice> notices = list(queryWrapper);
        
        // 转换为VO对象
        List<NoticeListVO> noticeListVOs = notices.stream().map(notice -> {
            NoticeListVO vo = new NoticeListVO();
            vo.setId(notice.getId());
            vo.setTitle(notice.getTitle());
            vo.setContent(notice.getContent());
            vo.setNoticeImages(getImageUrl(notice.getNoticeImages()));
            vo.setNoticeType(notice.getNoticeType());
            vo.setIsUrgent(notice.getIsUrgent());
            vo.setIsTop(notice.getIsTop());
            vo.setPublishTime(notice.getPublishTime());
            vo.setReadCount(notice.getReadCount());
            return vo;
        }).collect(Collectors.toList());
        
        // 查询总数
        QueryWrapper<CommunityNotice> countWrapper = new QueryWrapper<>();
        countWrapper.eq("community_id", communityId)
                .eq("status", "已发布")
                .eq("approval_status", "已审核")
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now());
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            countWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("content", keyword)
                    .or()
                    .like("notice_type", keyword));
        }
        
        long total = count(countWrapper);
        
        // 构建分页结果
        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("page", page);
        pageData.put("size", size);
        pageData.put("total", total);
        pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
        pageData.put("items", noticeListVOs);
        
        return pageData;
    }
    
    @Override
    public Map<String, Object> filterNoticesByAudience(Long communityId, NoticeFilterRequest request) {
        Integer page = request.getPage();
        Integer size = request.getSize();
        String noticeType = request.getNoticeType();
        String targetBuilding = request.getTargetBuilding();
        String targetOwnerType = request.getTargetOwnerType();
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        
        // 构建查询条件
        QueryWrapper<CommunityNotice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("community_id", communityId)
                .eq("status", "已发布")
                .eq("approval_status", "已审核")
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now());
        
        // 添加公告类型筛选条件（按 notice_type 分类）
        if (noticeType != null && !noticeType.trim().isEmpty()) {
            queryWrapper.eq("notice_type", noticeType);
        }
        
        // 添加目标受众筛选条件 - 仅显示全体业主的公告
        queryWrapper.eq("target_audience", "全体业主");
        
        // 添加目标楼栋筛选条件（JSON数组包含查询）
        if (targetBuilding != null && !targetBuilding.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("target_buildings", targetBuilding)
                    .or()
                    .isNull("target_buildings")
                    .or()
                    .eq("target_buildings", ""));
        }
        
        // 添加目标业主类型筛选条件
        if (targetOwnerType != null && !targetOwnerType.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("target_owner_types", targetOwnerType)
                    .or()
                    .isNull("target_owner_types")
                    .or()
                    .eq("target_owner_types", ""));
        }
        
        // 排序：按是否置顶和发布时间倒序排列（最新发布的在前）
        queryWrapper.orderByDesc("is_top")
                .orderByDesc("publish_time");
        
        // 计算分页
        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + size + " OFFSET " + offset);
        
        // 查询数据
        List<CommunityNotice> notices = list(queryWrapper);
        
        // 转换为VO对象
        List<NoticeListVO> noticeListVOs = notices.stream().map(notice -> {
            NoticeListVO vo = new NoticeListVO();
            vo.setId(notice.getId());
            vo.setTitle(notice.getTitle());
            vo.setContent(notice.getContent());
            vo.setNoticeImages(getImageUrl(notice.getNoticeImages()));
            vo.setNoticeType(notice.getNoticeType());
            vo.setIsUrgent(notice.getIsUrgent());
            vo.setIsTop(notice.getIsTop());
            vo.setPublishTime(notice.getPublishTime());
            vo.setReadCount(notice.getReadCount());
            return vo;
        }).collect(Collectors.toList());
        
        // 查询总数
        QueryWrapper<CommunityNotice> countWrapper = new QueryWrapper<>();
        countWrapper.eq("community_id", communityId)
                .eq("status", "已发布")
                .eq("approval_status", "已审核")
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now());
        
        if (noticeType != null && !noticeType.trim().isEmpty()) {
            countWrapper.eq("notice_type", noticeType);
        }
        
        // 仅计数全体业主的公告
        countWrapper.eq("target_audience", "全体业主");
        
        if (targetBuilding != null && !targetBuilding.trim().isEmpty()) {
            countWrapper.and(wrapper -> wrapper
                    .like("target_buildings", targetBuilding)
                    .or()
                    .isNull("target_buildings")
                    .or()
                    .eq("target_buildings", ""));
        }
        
        if (targetOwnerType != null && !targetOwnerType.trim().isEmpty()) {
            countWrapper.and(wrapper -> wrapper
                    .like("target_owner_types", targetOwnerType)
                    .or()
                    .isNull("target_owner_types")
                    .or()
                    .eq("target_owner_types", ""));
        }
        
        long total = count(countWrapper);
        
        // 构建分页结果
        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("page", page);
        pageData.put("size", size);
        pageData.put("total", total);
        pageData.put("pages", size == 0 ? 0 : ((total + size - 1) / size));
        pageData.put("items", noticeListVOs);
        
        return pageData;
    }
}

