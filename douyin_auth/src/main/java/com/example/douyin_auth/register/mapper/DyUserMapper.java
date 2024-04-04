package com.example.douyin_auth.register.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_auth.register.domain.po.DyUser;
import org.apache.ibatis.annotations.Select;

/**
 * @author : zxm
 * @date: 2024/3/21 - 10:17
 * @Description: com.example.be.register.mapper
 * @version: 1.0
 */
public interface DyUserMapper extends BaseMapper<DyUser> {

    /** 
     * @description: 根据手机号查询用户
 * @param phone  
     * @return: com.example.be.register.domain.po.DyUser 
     * @author zxm
     * @date: 2024/3/23 19:51
     */

    @Select("select * from dy_user u where u.phone = #{phone}")
    DyUser selectUserByPhone(String phone);

    @Select("update dy_user set code = #{code} where phone = #{phone}")
    DyUser UpdateUserByPhone(String phone, String code);
}
