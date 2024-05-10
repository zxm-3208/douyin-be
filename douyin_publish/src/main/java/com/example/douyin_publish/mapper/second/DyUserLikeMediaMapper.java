package com.example.douyin_publish.mapper.second;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_publish.domain.po.DyPublish;
import com.example.douyin_publish.domain.po.DyUserLikeMedia;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/5/9 - 15:02
 * @Description: com.example.douyin_user.mapper.master
 * @version: 1.0
 */
public interface DyUserLikeMediaMapper extends BaseMapper<DyUserLikeMedia> {
    @Select("select * from dy_user_like_media u where u.userId = #{userId}")
    List<DyUserLikeMedia> selectByUserId(String userId);
}

