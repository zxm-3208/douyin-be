package com.example.douyin_user.mapper.master;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_user.domain.po.dbAuth.DyUserLikeMedia;
import com.example.douyin_user.domain.po.dbMedia.DyMedia;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author : zxm
 * @date: 2024/5/9 - 15:02
 * @Description: com.example.douyin_user.mapper.master
 * @version: 1.0
 */
public interface DyUserLikeMediaMapper extends BaseMapper<DyUserLikeMedia> {

        @Insert("INSERT INTO dy_user_like_media (userId, mediaId, updateTime)  VALUES (#{userId}, #{mediaId}, #{time});")
        Integer addLikeMeida(String userId, String mediaId, Date time);

        @Delete("DELETE FROM dy_user_like_media u WHERE u.userId = #{userId} and u.mediaId = #{mediaId};")
        Integer delLikeMeida(String userId, String mediaId);

        @Select("select * from dy_user_like_media u where u.mediaId = #{mediaId}")
        List<DyUserLikeMedia> getMediaLikeCountBymediaId(String mediaId);
}

