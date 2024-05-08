package com.example.douyin_user.mapper.second;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_user.domain.po.dbMedia.DyMedia;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author : zxm
 * @date: 2024/5/8 - 9:30
 * @Description: com.example.douyin_user.mapper
 * @version: 1.0
 */
public interface MediaFilesMapper extends BaseMapper<DyMedia> {

    @Update("UPDATE dy_media SET likeCount = likeCount + 1 WHERE id = #{id}")
    Integer addMediaLikeById(String id);

    @Update("UPDATE dy_media SET likeCount = likeCount - 1 WHERE id = #{id}")
    Integer delMediaLikeById(String id);
}
