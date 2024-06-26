package com.example.douyin_auth.register.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.douyin_auth.register.domain.dto.PhoneLoginUserDTO;
import com.example.douyin_auth.register.domain.po.DyUser;
import com.example.douyin_auth.register.mapper.DyUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author : zxm
 * @date: 2024/3/21 - 16:39
 * @Description: com.example.be.register.security.config
 * @version: 1.0
 */

@Service
@Slf4j
public class UserDetailServiceImplByPhone implements UserDetailsService {
    @Autowired
    private DyUserMapper userMapper;


    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
//        //根据用户名查询用户信息
//        LambdaQueryWrapper<DyUser> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(DyUser::getPhone, phone);
//        DyUser user = userMapper.selectOne(wrapper);
        DyUser user = userMapper.selectUserByPhone(phone);
        return new PhoneLoginUserDTO(user);

    }
}
