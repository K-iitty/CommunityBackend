package com.community.property.mapper;

import com.community.property.entity.CommunityNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 社区公告Mapper
 */
@Mapper
public interface CommunityNoticeMapper extends BaseMapper<CommunityNotice> {
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM community_notice WHERE id = #{id}")
    CommunityNotice findById(Long id);
    
    /**
     * 根据条件查询公告列表
     */
    @SelectProvider(type = CommunityNoticeDaoProvider.class, method = "findByConditions")
    List<CommunityNotice> findByConditions(@Param("communityId") Long communityId, 
                                            @Param("noticeType") String noticeType, 
                                            @Param("offset") int offset, 
                                            @Param("size") Integer size);
    
    /**
     * 统计公告数量
     */
    @SelectProvider(type = CommunityNoticeDaoProvider.class, method = "countByConditions")
    int countByConditions(@Param("communityId") Long communityId, 
                          @Param("noticeType") String noticeType);
    
    /**
     * 增加阅读次数
     */
    @Update("UPDATE community_notice SET read_count = read_count + 1 WHERE id = #{noticeId}")
    int incrementReadCount(Long noticeId);
    
    class CommunityNoticeDaoProvider {
        public String findByConditions(@Param("communityId") Long communityId, 
                                       @Param("noticeType") String noticeType, 
                                       @Param("offset") int offset, 
                                       @Param("size") Integer size) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM community_notice WHERE status = '已发布' ");
            sql.append("AND NOW() BETWEEN start_time AND end_time ");
            
            if (communityId != null) {
                sql.append("AND community_id = #{communityId} ");
            }
            if (noticeType != null) {
                sql.append("AND notice_type = #{noticeType} ");
            }
            
            sql.append("ORDER BY is_top DESC, is_urgent DESC, publish_time DESC ");
            sql.append("LIMIT #{offset}, #{size}");
            return sql.toString();
        }
        
        public String countByConditions(@Param("communityId") Long communityId, 
                                        @Param("noticeType") String noticeType) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) FROM community_notice WHERE status = '已发布' ");
            sql.append("AND NOW() BETWEEN start_time AND end_time ");
            
            if (communityId != null) {
                sql.append("AND community_id = #{communityId} ");
            }
            if (noticeType != null) {
                sql.append("AND notice_type = #{noticeType} ");
            }
            
            return sql.toString();
        }
    }
}