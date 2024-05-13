package com.example.douyin_auth.register.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.douyin_auth.register.domain.dto.PhoneLoginUserDTO;
import com.example.douyin_auth.register.domain.dto.UserNameLoginUserDTO;
import com.example.douyin_auth.register.domain.po.DyUser;
import com.example.douyin_auth.register.domain.vo.PhoneLoginUserVO;
import com.example.douyin_auth.register.domain.vo.UserNameLoginUserVo;
import com.example.douyin_auth.register.mapper.DyUserMapper;
import com.example.douyin_auth.register.security.exception.CaptchaNotMatchException;
import com.example.douyin_auth.register.security.service.TokenService;
import com.example.douyin_auth.register.service.UserService;
import com.example.douyin_commons.constant.Constants;
import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.constant.SystemConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.UserDTO;
import com.example.douyin_commons.utils.RegexUtils;
import com.example.douyin_commons.utils.UserHolder;
import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
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

        return BaseResponse.success();
    }

    @Override
    public BaseResponse getCode() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);

        // 生成验证码,及验证码唯一标识
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String key = Constants.CAPTCHA_CODE_KEY + uuid;
        String code = specCaptcha.text().toLowerCase();
        log.info("uuid为 {}",uuid);
        log.info("图形验证码为 {}",code);

        // 保存到redis
        redisTemplate.opsForValue().set(key, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        HashMap<String, Object> map = new HashMap<>();
        map.put("uuid", uuid);
        map.put("img", specCaptcha.toBase64());

        return BaseResponse.success(map);
    }

    @Override
    public BaseResponse login(PhoneLoginUserVO phoneLoginUserVO) {
        String phone = phoneLoginUserVO.getPhone();
        // 1. 从Redis中获取验证码并进行校验
        String cacheCode = (String) redisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = phoneLoginUserVO.getCode();
        if(cacheCode==null || !cacheCode.equals(code)){
            return BaseResponse.fail("验证码错误");
        }

        // 2. 一致则根据手机号查询用户
        DyUser user = dyUserMapper.selectUserByPhone(phone);
//        DyUser user = this.query().eq("phone", phone).one();

        String BC_code = passwordEncoder.encode(code);

        // 3. 判断用户是否存在
        if (user == null){
            user = createUserWithPhone(phone, BC_code);
        }
        else{
            user = updateUserWithPhone(phone, BC_code);
        }

        // 4. 调用AuthenticationManager的authenticate方法，进行用户认证
        Authentication usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(phoneLoginUserVO.getPhone(), phoneLoginUserVO.getCode());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        // 5. 如果认证没有通过，给出错误信息
        if(Objects.isNull(authentication)){
            throw new RuntimeException("登录失败");
        }

        // 6. 如果认证通过，使用userId生成一个JWT （tokenService.createToken方法中缓存了token）
        PhoneLoginUserDTO loginUser = (PhoneLoginUserDTO) authentication.getPrincipal();
        log.info("userId:{}", UserHolder.getUser());
        String jwt = tokenService.createToken(loginUser);
        // 保存用户信息
        UserDTO userDTO = new UserDTO(loginUser.getDyUser().getId(),loginUser.getDyUser().getUserName(), loginUser.getDyUser().getIcon());
        log.info("jwt:{}", jwt);
        redisTemplate.opsForValue().set(RedisConstants.USER_TOKEN_KEY+jwt, userDTO, RedisConstants.USER_TOKEN_TTL, TimeUnit.HOURS);

        // 7. 封装ResponseResult，并返回
        Map<String, String> map = new HashMap<>();
        map.put("authorization", jwt);
        map.put("token_type", "jwt");
        map.put("expire_time", String.valueOf(loginUser.getExpireTime()));
        map.put("userId", user.getId());
        return BaseResponse.success(map);

    }

    @Override
    public BaseResponse loginByUserName(UserNameLoginUserVo userNameLoginUserVo) {
        // 从Redis中获取验证码
        String verifyKey = Constants.CAPTCHA_CODE_KEY + userNameLoginUserVo.getUuid();
        log.info(verifyKey);
        String captcha = (String) redisTemplate.opsForValue().get(verifyKey);
        log.info(captcha);
        if(captcha == null || !userNameLoginUserVo.getCode().equalsIgnoreCase(captcha)) {     // equalsIgnoreCase忽视大小写
            throw new CaptchaNotMatchException("验证码错误!!!!!!!!!!");
        }
        redisTemplate.delete(verifyKey);

        log.info("开始认证");

        log.info(userNameLoginUserVo.getUserName());
        log.info(userNameLoginUserVo.getPassword());
        // 认证
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userNameLoginUserVo.getUserName(), userNameLoginUserVo.getPassword()));

        if(Objects.isNull(authenticate)){
            throw new RuntimeException("登录失败");
        }

        log.info(String.valueOf(authenticate));

        // 生成令牌
        UserNameLoginUserDTO userNameLoginUserDTO = (UserNameLoginUserDTO) authenticate.getPrincipal();
        String jwt = tokenService.createToken(userNameLoginUserDTO);
        log.info(jwt);
        // 保存用户信息
        UserDTO userDTO = new UserDTO(userNameLoginUserDTO.getDyUser().getId(),userNameLoginUserDTO.getDyUser().getUserName(), userNameLoginUserDTO.getDyUser().getIcon());
        redisTemplate.opsForValue().set(RedisConstants.USER_TOKEN_KEY+jwt, userDTO, RedisConstants.USER_TOKEN_TTL, TimeUnit.HOURS);
        DyUser user = dyUserMapper.selectUserByUsername(userNameLoginUserVo.getUserName());

        // 返回响应
        Map<String, Object> map = new HashMap<>();
        map.put("authorization", jwt);
        map.put("token_type", "jwt");
        map.put("expire_time", String.valueOf(userNameLoginUserDTO.getExpireTime()));
        map.put("userId", user.getId());
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
        user.setSex("2");
        user.setBirthday(new Date(1));
        user.setIntroduction("这个人很懒，什么都没有。");

        // 通过Passay库生成密码
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        String password = passwordGenerator.generatePassword(12, new CharacterRule(EnglishCharacterData.Digit), new CharacterRule(EnglishCharacterData.Alphabetical));
        log.info("原始密码{}",password);
        user.setPassword(passwordEncoder.encode(password));

        user.setCode(code);
        dyUserMapper.save(user);
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
