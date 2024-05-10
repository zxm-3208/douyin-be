package com.example.douyin_publish.mapper.master;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_publish.domain.po.DyMedia;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:31
 * @Description: com.example.douyin_publish.mapper
 * @version: 1.0
 */
public interface MediaFilesMapper extends BaseMapper<DyMedia> {
    @Select("select count(*) from dy_media u where u.md5 = #{MD5}")
    int getCountOfMediaByMD5(String MD5);

    @Select("select id from dy_media u where u.md5 = #{md5}")
    String getMediaIdByMd5(String md5);

    @Select("select mediaUrl from dy_media u where u.id = #{mediaId}")
    String getUrlByMediaId(String mediaId);

    @Select("select * from dy_media u where u.id = #{id}")
    DyMedia selectById(String id);

    @Insert("insert into dy_media(id, mediaUrl, status, md5, author, likeCount, forwardCount, readCount) values(#{id}, #{mediaUrl}, #{status}, #{md5}, #{author}, #{likeCount}, #{forwardCount}, #{readCount})")
    int insert(DyMedia dyMedia);

    List<DyMedia> findUrlAndUpdateTimeByUserLike(String userId);
}
