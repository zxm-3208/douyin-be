package com.example.douyin_feed.mapper.second;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_feed.domain.po.DyUser;
import com.example.douyin_feed.domain.po.DyUserLikeMedia;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author : zxm
 * @date: 2024/5/14 - 19:11
 * @Description: com.example.douyin_feed.mapper.second
 * @version: 1.0
 */
public interface DyUserMapper extends BaseMapper<DyUser> {

    @Select("select * from dy_user u where u.id = #{userId}")
    DyUser getUserdById(String userId);

    @Select("select * from dy_user u")
    List<DyUser> getUser();

}
