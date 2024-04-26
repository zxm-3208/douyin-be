package com.example.douyin_feed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_feed.domain.po.DyMedia;
import com.example.douyin_feed.domain.po.MediaJoinPublish;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
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

    @Select("select id from dy_media u")
    String[] getAllMediaId();

    @Select("select media_url from dy_media u where u.id = #{id}")
    String getMediaUrlByid(String id);

    @Select("select media_url from dy_media u where u.author = #{userId}")
    String[] getMediaUrlByUserId(String userId);

    @Select("select * from dy_media u where u.id = #{mediaId}")
    DyMedia[] getAllByid(String mediaId);

    @Select("select media_url from dy_media u")
    String[] getAllMediaUrl();

    List<MediaJoinPublish> findMediaUrlAndUpdateTime();
}
