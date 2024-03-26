package com.example.be.register.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.be.common.constant.Constants;
import com.example.be.common.constant.RedisConstants;
import com.example.be.common.constant.SystemConstants;
import com.example.be.common.core.domain.BaseResponse;
import com.example.be.common.utils.RegexUtils;
import com.example.be.register.domain.dto.LoginUserDTO;
import com.example.be.register.domain.po.DyUser;
import com.example.be.register.domain.vo.LoginUserVO;
import com.example.be.register.mapper.DyUserMapper;
import com.example.be.register.security.service.TokenService;
import com.example.be.register.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/3/19 - 22:37
 * @Description: com.example.be.common.core.service.impl
 * @version: 1.0
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<DyUserMapper, DyUser> implements UserService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DyUserMapper dyUserMapper;

    @Override
    public BaseResponse send(String phone) {

        // 1. 校验手机号
        if(RegexUtils.isPhoneInvalid(phone)) {   // 正则表达式校验
            return BaseResponse.fail("手机号格式错误");
        }

        log.debug("手机号：{}", phone);

        // 2. 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 3. 保存到Redis
        redisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 4. 发送验证码       (搁置，接入云平台短信验证码需要网站备案)
        log.debug("发送短信验证码成功，验证码：{}", code);

        return BaseResponse.success(code);
    }

    @Override
    public BaseResponse login(LoginUserVO loginUserVO) {
        String phone = loginUserVO.getPhone();
        // 1. 从Redis中获取验证码并进行校验
        String cacheCode = (String) redisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginUserVO.getCode();
        if(cacheCode==null || !cacheCode.equals(code)){
            return BaseResponse.fail("验证码错误");
        }

        // 2. 一致则根据手机号查询用户
        DyUser user = this.query().eq("phone", phone).one();

        String BC_code = passwordEncoder.encode(code);

        // 3. 判断用户是否存在
        if (user == null){
            user = createUserWithPhone(phone, BC_code);
        }
        else{
            user = updateUserWithPhone(phone, BC_code);
        }

        // 4. 调用AuthenticationManager的authenticate方法，进行用户认证
        Authentication usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginUserVO.getPhone(), loginUserVO.getCode());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        // 5. 如果认证没有通过，给出错误信息
        if(Objects.isNull(authentication)){
            throw new RuntimeException("登录失败");
        }

        // 6. 如果认证通过，使用userId生成一个JWT （tokenService.createToken方法中缓存了token）
        LoginUserDTO loginUser = (LoginUserDTO) authentication.getPrincipal();
        log.info(String.valueOf(loginUser));
        String jwt = tokenService.createToken(loginUser);
//        LoginUserDTO loginUserDTO = BeanUtil.copyProperties(loginUser, LoginUserDTO.class);


        // 7. 封装ResponseResult，并返回
        Map<String, String> map = new HashMap<>();
        map.put("authorization", jwt);
        map.put("token_type", "jwt");
        map.put("expire_time", String.valueOf(loginUser.getExpireTime()));
        return BaseResponse.success(map);

    }

//    @Override
//    public BaseResponse logout() {
//
//        // 获取当前用户的认证信息
//        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//
//        if(Objects.isNull(authenticationToken)){
//            throw new RuntimeException("获取用户认证信息失败，请重新登录！");
//        }
//
//        LoginUserDTO loginUserDTO = (LoginUserDTO) authenticationToken.getPrincipal();
//        String userId = loginUserDTO.getToken();
//
//        // 删除Redis中的用户信息
//        redisTemplate.delete(Constants.LOGIN_TOKEN_KEY + userId);
//
//        return BaseResponse.success("注销成功");
//    }


    private DyUser createUserWithPhone(String phone, String code){
        DyUser user = new DyUser();
        user.setPhone(phone);
        user.setUserName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        user.setCreateTime(new Timestamp(System.currentTimeMillis()));
        user.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        user.setCode(code);

        this.save(user);
        return user;
    }

    private DyUser updateUserWithPhone(String phone, String code){
        DyUser user = dyUserMapper.selectUserByPhone(phone);
        user.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        user.setCode(code);
        dyUserMapper.UpdateUserByPhone(phone, code);
        return user;
    }
}
