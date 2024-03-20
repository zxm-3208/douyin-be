package com.example.be.register.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.example.be.common.constant.RedisConstants;
import com.example.be.common.core.domain.BaseResponse;
import com.example.be.common.utils.RegexUtils;
import com.example.be.register.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/3/19 - 22:37
 * @Description: com.example.be.common.core.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public BaseResponse send(String phone) {

        // 1. 校验手机号
        if(RegexUtils.isPhoneInvalid(phone)) {   // 正则表达式校验
            return BaseResponse.fail("手机号格式错误");
        }

        // 2. 生成验证码
        String code = RandomUtil.randomNumbers(6);
        log.debug("验证码：{}", code);

        // 3. 保存到Redis
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 4. 发送验证码       (搁置，接入云平台短信验证码需要网站备案)
        log.debug("发送短信验证码成功，验证码：{}", code);

        return BaseResponse.success();
    }
}
