package com.example.douyin_auth.register.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esotericsoftware.kryo.util.Null;
import com.example.douyin_auth.common.constant.Constants;
import com.example.douyin_auth.common.utils.UUIDUtils;
import com.example.douyin_auth.register.domain.dto.LoginUserDTO;
import com.example.douyin_auth.register.domain.dto.PhoneLoginUserDTO;
import com.example.douyin_auth.register.domain.dto.UserNameLoginUserDTO;
import com.example.douyin_auth.register.domain.po.DyUser;
import com.example.douyin_auth.register.mapper.DyUserMapper;
import com.example.douyin_auth.register.security.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @author : zxm
 * @date: 2024/3/22 - 10:24
 * @Description: com.example.be.register.security.service.impl
 * @version: 1.0
 */
@Component
@Slf4j
public class TokenServiceImpl implements TokenService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expireTime}")
    private int expireTime;

    @Autowired
    private DyUserMapper userMapper;

    private static final long MILLIS_SECOND = 1000;

    private static final long MILLIS_MINUTE = MILLIS_SECOND * 60;

    @Override
    public String createToken(PhoneLoginUserDTO phoneLoginUserDTO) {
        // 设置uuid 用户唯一标识
        String userKey = UUIDUtils.randomUUID();
        phoneLoginUserDTO.setToken(userKey);

        // 保存用户信息，刷新令牌有效时间
        return refreshToken(phoneLoginUserDTO);

//        HashMap<String, Object> claims = new HashMap<>();
//        claims.put(Constants.LOGIN_USER_KEY, userKey);
//        String token = Jwts.builder()
//                .setClaims(claims)
//                .signWith(SignatureAlgorithm.HS256, secret).compact();
//        return token;
    }

    @Override
    public String createToken(UserNameLoginUserDTO userNameLoginUserDTO) {
        // 设置uuid 用户唯一标识
        String userKey = UUIDUtils.randomUUID();
        userNameLoginUserDTO.setToken(userKey);

        // 保存用户信息，刷新令牌有效时间
        return refreshToken(userNameLoginUserDTO);
    }

    @Override
    public String refreshToken(LoginUserDTO loginUserDTO) {
        // 更新时间
        loginUserDTO.setLoginTime(System.currentTimeMillis());
        // 过期时间48小时
        loginUserDTO.setExpireTime(loginUserDTO.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据UUID缓存
        String userKey = getTokenKey(loginUserDTO.getToken());
//        redisTemplate.opsForValue().set(userKey, loginUserDTO, expireTime, TimeUnit.MINUTES);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, loginUserDTO.getToken());
        claims.put(Constants.PHONE, loginUserDTO.getDyUser().getPhone());
        claims.put(Constants.USERNAME, loginUserDTO.getUsername());
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secret).compact();

        return token;
    }

//    @Override
//    public String refreshToken(UserNameLoginUserDTO userNameLoginUserDTO) {
//        // 更新时间
//        userNameLoginUserDTO.setLoginTime(System.currentTimeMillis());
//        // 过期时间48小时
//        userNameLoginUserDTO.setExpireTime(userNameLoginUserDTO.getLoginTime() + expireTime * MILLIS_MINUTE);
//        // 根据UUID缓存
//        String userKey = getTokenKey(userNameLoginUserDTO.getToken());
//
//        HashMap<String, Object> claims = new HashMap<>();
//        claims.put(Constants.LOGIN_USER_KEY, userNameLoginUserDTO.getToken());
//        claims.put(Constants.USERNAME, userNameLoginUserDTO.getUsername());
//        String token = Jwts.builder()
//                .setClaims(claims)
//                .signWith(SignatureAlgorithm.HS256, secret).compact();
//
//        return token;
//    }


    @Override
    public LoginUserDTO getLoginUserDTO(HttpServletRequest request) {
        String token = getToken(request);
        if(!StringUtil.isEmpty(token)){
            Claims claims = parseToken(token);
//            // 解析对应的用户信息 (Redis)
//            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
//            String userKey = getTokenKey(uuid);
//            return (LoginUserDTO) redisTemplate.opsForValue().get(userKey);

            System.out.println(claims);
            // 解析对应的用户信息 (mysql)
            String phone = (String) claims.get(Constants.PHONE);
            System.out.println(phone);
            //根据手机号查询用户信息
            LambdaQueryWrapper<DyUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DyUser::getPhone, phone);
            DyUser user = userMapper.selectOne(wrapper);
            PhoneLoginUserDTO phoneLoginUserDTO = new PhoneLoginUserDTO(user);
            // 更新时间
            phoneLoginUserDTO.setLoginTime(System.currentTimeMillis());
            // 过期时间48小时
            phoneLoginUserDTO.setExpireTime(phoneLoginUserDTO.getLoginTime() + expireTime * MILLIS_MINUTE);
            // 设置Token
            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
            String userKey = getTokenKey(uuid);
            phoneLoginUserDTO.setToken(userKey);
            return phoneLoginUserDTO;
        }
        return null;
    }

    /**
     * @description: Token剩余时间小于XX，则刷新
     * @author zxm
     * @date 2024/3/25 10:40
     * @version 1.0
    */
    @Override
    public void verifyToken(PhoneLoginUserDTO phoneLoginUserDTO) {
        long expireTime = phoneLoginUserDTO.getExpireTime();
        long currentTimeMillis = System.currentTimeMillis();
        // 相差小于24小时，自动刷新缓存
        if (expireTime-currentTimeMillis <= expireTime * MILLIS_MINUTE / 2){
            refreshToken(phoneLoginUserDTO);
        }
    }

    /**
     * @description: 从令牌中获取数据声明
     * @author zxm
     * @date 2024/3/25 10:14
     * @version 1.0
    */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * @description: 从请求头中获取token
     * @author zxm
     * @date 2024/3/25 10:08
     * @version 1.0
    */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(this.header);
        // 将标准的JWT（Authorization: Bearer aaa.bbb.ccc）进行转换
        if(!StringUtil.isEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
            token = token.replace(Constants.TOKEN_PREFIX, "");
        return token;
    }

    /**
     * @description: 拼接TokenKey
     * @author zxm
     * @date 2024/3/25 10:08
     * @version 1.0
    */
    private String getTokenKey(String uuid){
        return Constants.LOGIN_TOKEN_KEY + uuid;
    }
}
