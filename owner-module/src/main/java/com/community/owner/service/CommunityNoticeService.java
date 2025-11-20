package com.community.owner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.owner.domain.dto.vo.NoticeDetailVO;
import com.community.owner.domain.dto.request.NoticeFilterRequest;
import com.community.owner.domain.dto.request.NoticeSearchRequest;
import com.community.owner.domain.entity.CommunityNotice;

import java.util.Map;

/**
 * 社区公告服务接口
 */
public interface CommunityNoticeService extends IService<CommunityNotice> {
    
    /**
     * 分页查询公告列表（显示标题和图片）
     * @param communityId 社区ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Map<String, Object> listNotices(Long communityId, Integer page, Integer size);
    
    /**
     * 查看公告详情
     * @param noticeId 公告ID
     * @return 公告详情
     */
    NoticeDetailVO getNoticeDetail(Long noticeId);
    
    /**
     * 搜索公告（模糊查询标题、内容、公告类型）
     * @param communityId 社区ID
     * @param request 搜索请求
     * @return 搜索结果
     */
    Map<String, Object> searchNotices(Long communityId, NoticeSearchRequest request);
    
    /**
     * 根据目标受众筛选公告
     * @param communityId 社区ID
     * @param request 筛选请求
     * @return 筛选结果
     */
    Map<String, Object> filterNoticesByAudience(Long communityId, NoticeFilterRequest request);
}

