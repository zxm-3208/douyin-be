package com.example.douyin_feed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_feed.domain.po.DyMedia;
import org.apache.ibatis.annotations.Select;

/**
 * @author : zxm
 * @date: 2024/4/9 - 12:31
 * @Description: com.example.douyin_publish.mapper
 * @version: 1.0
 */
public interface MediaFilesMapper extends BaseMapper<DyMedia> {
    @Select("select count(*) from dy_media u where u.md5 = #{MD5}")
    int getCountOfMediaByMD5(String MD5);
}
