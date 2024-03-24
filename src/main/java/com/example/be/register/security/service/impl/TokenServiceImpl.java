package com.example.be.register.security.service.impl;

import cn.hutool.jwt.JWTUtil;
import com.example.be.common.constant.Constants;
import com.example.be.common.constant.RedisConstants;
import com.example.be.common.constant.SystemConstants;
import com.example.be.common.utils.UUIDUtils;
import com.example.be.register.domain.dto.LoginUserDTO;
import com.example.be.register.security.service.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.token.Token;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/3/22 - 10:24
 * @Description: com.example.be.register.security.service.impl
 * @version: 1.0
 */
@Component
public class TokenServiceImpl implements TokenService {

    @Autowired
    @Qualifier(value = "redisTemplate")
    private RedisTemplate RedisTemplate;

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expireTime}")
    private int expireTime;

    private static final long MILLIS_SECOND = 1000;

    private static final long MILLIS_MINUTE = MILLIS_SECOND * 60;

    @Override
    public String createToken(LoginUserDTO loginUserDTO) {

        // 设置uuid 用户唯一标识
        String userKey = UUIDUtils.randomUUID();
        loginUserDTO.setToken(userKey);

        // 保存用户信息，刷新令牌有效时间
        refreshToken(loginUserDTO);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, userKey);
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secret).compact();
        return token;
    }

    @Override
    public void refreshToken(LoginUserDTO loginUserDTO) {
        // 更新时间
        loginUserDTO.setLoginTime(System.currentTimeMillis());

        // 过期时间48小时
        loginUserDTO.setExpireTime(loginUserDTO.getLoginTime() + expireTime * MILLIS_MINUTE);

        // 根据UUID缓存
        String userKey = getTokenKey(loginUserDTO.getToken());
        RedisTemplate.opsForValue().set(userKey, loginUserDTO, expireTime, TimeUnit.MINUTES);

//        HashMap<String, Object> claims = new HashMap<>();
//        claims.put(Constants.LOGIN_USER_KEY, userKey);
//        String token = Jwts.builder()
//                .setClaims(claims)
//                .signWith(SignatureAlgorithm.HS256, secret).compact();
//        return token;
    }

    private String getTokenKey(String uuid){
        return Constants.LOGIN_TOKEN_KEY + uuid;
    }
}
