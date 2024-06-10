package com.example.douyin_chat_gate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.douyin_chat_commons.domain.po.DyUser;
import org.apache.ibatis.annotations.Select;

/**
 * @author : zxm
 * @date: 2024/6/9 - 21:14
 * @Description: com.example.douyin_chat_gate.mapper
 * @version: 1.0
 */
public interface ChatMapper extends BaseMapper<DyUser> {

    @Select("select * from dy_user u where u.id = #{id}")
    DyUser selectByUserId(String id);

}
