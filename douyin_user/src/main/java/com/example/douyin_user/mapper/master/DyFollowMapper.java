package com.example.douyin_user.mapper.master;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_user.domain.po.dbAuth.DyFollow;
import com.example.douyin_user.domain.po.dbAuth.DyUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author : zxm
 * @date: 2024/5/12 - 13:27
 * @Description: com.example.douyin_user.mapper.master
 * @version: 1.0
 */
public interface DyFollowMapper  extends BaseMapper<DyFollow> {
    @Insert("INSERT INTO dy_follow (userId, followerId, followCreateTime)  VALUES (#{userId}, #{followerId}, #{time});")
    Integer addFollow(String userId, String followerId, Date time);

    @Delete("DELETE FROM dy_follow u WHERE u.userId = #{userId} and u.followerID = #{authorId};")
    Integer delFollow(String userId, String authorId);

    @Select("select * from dy_follow u where u.userId = #{userId} and u.followerID = #{authorId}")
    DyFollow getFollowByUserIdAndAuthorId(String userId, String authorId);

    @Select("select * from dy_follow u where u.userId = #{userId}")
    List<DyFollow> getFollowByUserId(String userId);

    @Select("select u.followerID from dy_follow u where u.userId = #{userId}")
    List<String> getFollowIdByUserId(String userId);

    @Select("select * from dy_follow u where u.followerID = #{authorId}")
    List<DyFollow> getFansByAuthorId(String authorId);


    List<DyFollow> getFollowInfoByUserAndFollow(String userId);

    List<DyFollow> getFansInfoByUserAndFollow(String userId);


}
