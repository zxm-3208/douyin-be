package com.example.douyin_user.mapper.master;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_user.domain.po.dbAuth.DyLikeListOfUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

/**
 * @author : zxm
 * @date: 2024/5/9 - 15:01
 * @Description: com.example.douyin_user.mapper.master
 * @version: 1.0
 */
public interface DyLikeListOfUserMapper extends BaseMapper<DyLikeListOfUser> {

    @Insert("INSERT INTO dy_like_list_of_user (mediaId, userId)  VALUES (#{mediaId}, #{userId});")
    Integer addUser(String mediaId, String userId);

    @Delete("DELETE FROM dy_like_list_of_user u WHERE u.mediaId = #{mediaId} and u.userId = #{userId};")
    Integer delUser(String mediaId, String userId);
}

