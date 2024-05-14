package com.example.douyin_user.mapper.master;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_user.domain.po.dbAuth.DyUser;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * @author : zxm
 * @date: 2024/5/8 - 9:57
 * @Description: com.example.douyin_user.mapper.master
 * @version: 1.0
 */
public interface DyUserMapper extends BaseMapper<DyUser> {

    @Select("select * from dy_user u where u.id = #{userId}")
    DyUser getEdit(String userId);

    @Update("update dy_user u set u.icon=#{iconUrl}, u.userName=#{editName}, u.introduction=#{editIntro}, u.sex=#{gender}, u.birthday=#{birthday} where u.id=#{userId}")
    Integer updateByUserId(String userId, String iconUrl, String editName, String editIntro, String gender, Date birthday);
}
