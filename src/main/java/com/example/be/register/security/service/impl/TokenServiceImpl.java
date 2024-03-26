package com.example.be.register.security.service.impl;

import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.be.common.constant.Constants;
import com.example.be.common.constant.RedisConstants;
import com.example.be.common.constant.SystemConstants;
import com.example.be.common.utils.UUIDUtils;
import com.example.be.register.domain.dto.LoginUserDTO;
import com.example.be.register.domain.po.DyUser;
import com.example.be.register.mapper.DyUserMapper;
import com.example.be.register.security.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.token.Token;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

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
    public String createToken(LoginUserDTO loginUserDTO) {
        // 设置uuid 用户唯一标识
        String userKey = UUIDUtils.randomUUID();
        loginUserDTO.setToken(userKey);

        // 保存用户信息，刷新令牌有效时间
        return refreshToken(loginUserDTO);

//        HashMap<String, Object> claims = new HashMap<>();
//        claims.put(Constants.LOGIN_USER_KEY, userKey);
//        String token = Jwts.builder()
//                .setClaims(claims)
//                .signWith(SignatureAlgorithm.HS256, secret).compact();
//        return token;
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
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secret).compact();

        return token;
    }

    @Override
    public LoginUserDTO getLoginUserDTO(HttpServletRequest request) {
        String token = getToken(request);
        if(!StringUtil.isEmpty(token)){
            Claims claims = parseToken(token);
//            // 解析对应的用户信息 (Redis)
//            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
//            String userKey = getTokenKey(uuid);
//            return (LoginUserDTO) redisTemplate.opsForValue().get(userKey);

            // 解析对应的用户信息 (mysql)
            String phone = (String) claims.get(Constants.PHONE);
            //根据手机号查询用户信息
            LambdaQueryWrapper<DyUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DyUser::getPhone, phone);
            DyUser user = userMapper.selectOne(wrapper);
            LoginUserDTO loginUserDTO = new LoginUserDTO(user);
            // 更新时间
            loginUserDTO.setLoginTime(System.currentTimeMillis());
            // 过期时间48小时
            loginUserDTO.setExpireTime(loginUserDTO.getLoginTime() + expireTime * MILLIS_MINUTE);
            // 设置Token
            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
            String userKey = getTokenKey(uuid);
            loginUserDTO.setToken(userKey);
            return new LoginUserDTO(user);

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
    public void verifyToken(LoginUserDTO loginUserDTO) {
        long expireTime = loginUserDTO.getExpireTime();
        long currentTimeMillis = System.currentTimeMillis();
        // 相差小于24小时，自动刷新缓存
        if (expireTime-currentTimeMillis <= expireTime * MILLIS_MINUTE / 2){
            refreshToken(loginUserDTO);
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
