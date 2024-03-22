package com.example.be.register.security.UserDetailsService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.be.register.domain.dto.LoginUserDTO;
import com.example.be.register.domain.po.DyUser;
import com.example.be.register.domain.vo.LoginUserVO;
import com.example.be.register.mapper.DyUserMapper;
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
        //根据用户名查询用户信息
        LambdaQueryWrapper<DyUser> wrapper = new LambdaQueryWrapper<>();        // TODO 可能不可以用LoginUserVO
        wrapper.eq(DyUser::getPhone, phone);
        DyUser user = userMapper.selectOne(wrapper);

//        // 如果查不到数据就抛出异常,不过在service中为查到就自动注册
//        if(Objects.isNull(user)){
//            throw new RuntimeException("该手机号未注册");
//        }

        return new LoginUserDTO(user);

    }
}
