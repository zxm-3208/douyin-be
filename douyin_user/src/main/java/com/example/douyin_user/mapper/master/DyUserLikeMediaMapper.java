package com.example.douyin_user.mapper.master;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_user.domain.po.dbAuth.DyUserLikeMedia;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

/**
 * @author : zxm
 * @date: 2024/5/9 - 15:02
 * @Description: com.example.douyin_user.mapper.master
 * @version: 1.0
 */
public interface DyUserLikeMediaMapper extends BaseMapper<DyUserLikeMedia> {

        @Insert("INSERT INTO dy_user_like_media (userId, mediaId)  VALUES (#{userId}, #{mediaId});")
        Integer addLikeMeida(String userId, String mediaId);

        @Delete("DELETE FROM dy_user_like_media u WHERE u.userId = #{userId} and u.mediaId = #{mediaId};")
        Integer delLikeMeida(String userId, String mediaId);

}

