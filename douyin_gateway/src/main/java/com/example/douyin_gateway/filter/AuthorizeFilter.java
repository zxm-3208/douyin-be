package com.example.douyin_gateway.filter;

import com.example.douyin_commons.constant.Constants;
import com.example.douyin_commons.constant.RedisConstants;
import com.example.douyin_commons.core.domain.BaseResponse;
import com.example.douyin_commons.core.domain.ResultCode;
import com.example.douyin_commons.core.domain.UserDTO;
import com.example.douyin_commons.utils.UserHolder;
import com.example.douyin_gateway.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * @author : zxm
 * @date: 2024/4/7 - 21:35
 * @Description: com.example.douyin_gateway.filter
 * @version: 1.0
 */
@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Value("${token.expireTime}")
//    private int expireTime;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 2.判断是否登录
        String uriPath = (request.getURI().getPath());
        log.info("请求访问的uri地址:{}",uriPath);
        if(uriPath.contains("/code")||uriPath.contains("/login")||uriPath.contains("/captchaImage")||uriPath.contains("/logout")||uriPath.contains("/logoinbyusername")){
            return chain.filter(exchange);
        }
        // 3. 获取token
        String token = request.getHeaders().getFirst(header);
        // 将标准的JWT（Authorization: Bearer aaa.bbb.ccc）进行转换
        if(!StringUtil.isEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
            token = token.replace(Constants.TOKEN_PREFIX, "");
        // 4. 判断token是否存在
        if(StringUtil.isBlank(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 5. 判断token是否有效
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            Long exp = (Long)claims.get(Constants.EXP);
            long currentTimeMillis = System.currentTimeMillis();
            if(currentTimeMillis > exp){
                log.info("token已过期");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }catch (Exception e){
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 6. 基于token获取redis中的用户信息，如果redis没有，则去mysql查询
        UserDTO userDTO = (UserDTO) redisTemplate.opsForValue().get(RedisConstants.USER_TOKEN_KEY+token);
        // 7. 将查询到的信息转换为UserDTO对象并保存到ThreadLocal中
        UserHolder.saveUser(userDTO);
        log.info("UserHolder:{}", UserHolder.getUser());
        // 8. 放行
        return chain.filter(exchange);
    }

    /**
     * 优先级设置，值越小，优先级越高
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
